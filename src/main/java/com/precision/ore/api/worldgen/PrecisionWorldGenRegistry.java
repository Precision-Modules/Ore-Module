package com.precision.ore.api.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.Pair;
import com.precision.ore.OreModule;
import com.precision.ore.api.worldgen.vein.BedrockOreDepositDefinition;
import gregtech.api.util.FileUtility;
import gregtech.api.worldgen.config.IWorldgenDefinition;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.IOUtils;

public class PrecisionWorldGenRegistry {

	public static final PrecisionWorldGenRegistry INSTANCE = new PrecisionWorldGenRegistry();

	private static final int ORE_VEIN_VERSION = 2;

	private PrecisionWorldGenRegistry() {}

	private final Map<Integer, String> namedDimensions = new HashMap<>();
	private final Map<Integer, Pair<Integer, Integer>> layerOperations = new HashMap<>();

	private final List<BedrockOreDepositDefinition> registeredBedrockOreVeinDefinitions = new ArrayList<>();
	private final List<BedrockOreDepositDefinition> addonRegisteredBedrockOreVeinDefinitions = new ArrayList<>();
	private final List<BedrockOreDepositDefinition> removedBedrockOreVeinDefinitions = new ArrayList<>();

	public void initializeRegistry() {
		OreModule.logger.info("Ore Module Initialize Worldgen Registry...");
		try {
			reinitializeRegisteredVeins();
		} catch (IOException e) {
			// :3
		}
	}

	/**
	 * Handles the setup of precision.ore generation files in the config folder. Either creates the default files and
	 * reads them, or reads any modified files made by users
	 *
	 * <p>After reading all json worldgen files in the folder, they are initialized, creating vein definitions
	 *
	 * @throws IOException
	 */
	public void reinitializeRegisteredVeins() throws IOException {
		OreModule.logger.info("Reloading virtual precision.ore generation files from config...");
		Path configPath = Loader.instance().getConfigDir().toPath().resolve(OreModule.MODID);
		// The folder where worldgen definitions are stored
		Path worldgenRootPath = configPath.resolve("worldgen");
		// The Path for the file used to name dimensions for the JEI precision.ore gen page
		Path dimensionsFile = worldgenRootPath.resolve("dimensions.json");
		// The Path for the file used to get min and max operations for layer
		Path layersFile = worldgenRootPath.resolve("layers.json");

		// Lock file used to determine if the worldgen files need to be regenerated
		// This is used to ensure modifications to precision.ore gen in modpacks are not overwritten
		Path jarFileExtractLock = worldgenRootPath.resolve("worldgen_extracted.json");
		if (!Files.exists(worldgenRootPath)) Files.createDirectories(worldgenRootPath);

		// Remove the old extract lock file if it exists to remove clutter
		Path jarFileExtractLockOLD = worldgenRootPath.resolve("worldgen_extracted.txt");
		Files.deleteIfExists(jarFileExtractLockOLD);

		// The folder where all physical veins are stored
		Path veinPath = worldgenRootPath.resolve("vein");
		if (!Files.exists(veinPath)) Files.createDirectories(veinPath);

		// Checks if the dimension file exists. If not, creates the file and extracts the defaults from the mod jar
		if (!Files.exists(dimensionsFile)) {
			Files.createFile(dimensionsFile);
			extractJarVeinDefinitions(configPath, dimensionsFile);
		}

		if (!Files.exists(layersFile)) {
			Files.createFile(layersFile);
			extractJarVeinDefinitions(configPath, layersFile);
		}

		if (Files.exists(jarFileExtractLock)) {
			JsonObject extractLock = FileUtility.tryExtractFromFile(jarFileExtractLock);
			if (extractLock != null) {
				boolean needsUpdate = false;
				if (extractLock.get("veinVersion").getAsInt() != ORE_VEIN_VERSION) {
					extractJarVeinDefinitions(configPath, veinPath);
					needsUpdate = true;
				}
				// bump the version(s) on the lock file if needed
				if (needsUpdate) {
					extractJarVeinDefinitions(configPath, jarFileExtractLock);
				}
			}
		} else {
			if (ORE_VEIN_VERSION > 1) {
				extractJarVeinDefinitions(configPath, veinPath);
			}
			// create extraction lock since it doesn't exist
			Files.createFile(jarFileExtractLock);
			extractJarVeinDefinitions(configPath, jarFileExtractLock);
		}

		// attempt extraction if worldgen root directory is empty
		if (!Files.list(worldgenRootPath.resolve(veinPath)).findFirst().isPresent()) {
			extractJarVeinDefinitions(configPath, veinPath);
		}

		// Read the dimensions name from the dimensions file
		gatherNamedDimensions(dimensionsFile);
		gatherLayerOperations(layersFile);

		if (!removedBedrockOreVeinDefinitions.isEmpty()) {
			removeExistingFiles(veinPath, removedBedrockOreVeinDefinitions);
		}

		// Gather the worldgen vein files from the various folders in the config
		List<Path> veinFiles = Files.walk(veinPath)
				.filter(path -> path.toString().endsWith(".json"))
				.filter(Files::isRegularFile)
				.collect(Collectors.toList());

		for (Path worldgenDefinition : veinFiles) {

			// Tries to extract the json worldgen definition from the file
			JsonObject element = FileUtility.tryExtractFromFile(worldgenDefinition);
			if (element == null) {
				break;
			}

			String depositName = veinPath.relativize(worldgenDefinition).toString();

			try {
				BedrockOreDepositDefinition deposit = new BedrockOreDepositDefinition(depositName);

				if (deposit.initializeFromConfig(element)) {
					registeredBedrockOreVeinDefinitions.add(deposit);
				}
			} catch (RuntimeException exception) {
				OreModule.logger.error(
						"Failed to parse worldgen definition {} on path {}",
						depositName,
						worldgenDefinition,
						exception);
			}
		}

		addAddonFiles(
				worldgenRootPath, addonRegisteredBedrockOreVeinDefinitions, addonRegisteredBedrockOreVeinDefinitions);

		OreModule.logger.info("Loaded {} bedrock worldgen definitions", registeredBedrockOreVeinDefinitions.size());
		OreModule.logger.info(
				"Loaded {} bedrock worldgen definitions from addon mods",
				addonRegisteredBedrockOreVeinDefinitions.size());
	}

	/**
	 * Extracts files from the Gregtech jar and places them in the specified location
	 *
	 * @param configPath The path of the config root for the Gregtech mod
	 * @param targetPath The path of the target location where the files will be initialized
	 * @throws IOException
	 */
	private static void extractJarVeinDefinitions(Path configPath, Path targetPath) throws IOException {
		// The path of the worldgen folder in the config folder
		Path worldgenRootPath = configPath.resolve("worldgen");
		// The path of the physical vein folder in the config folder
		Path oreVeinRootPath = worldgenRootPath.resolve("vein");
		// The path of the named dimensions file in the config folder
		Path dimensionsRootPath = worldgenRootPath.resolve("dimensions.json");
		// The path of the named dimensions file in the config folder
		Path layersRootPath = worldgenRootPath.resolve("layers.json");
		// THe path of the lock file in the config folder
		Path extractLockPath = worldgenRootPath.resolve("worldgen_extracted.json");
		FileSystem zipFileSystem = null;
		try {
			URI sampleUri = PrecisionWorldGenRegistry.class
					.getResource("/assets/ore-module/")
					.toURI();
			// The Path for representing the worldgen folder in the assets folder in the Gregtech resources folder in
			// the jar
			Path worldgenJarRootPath;
			// The Path for representing the vein folder in the vein folder in the assets folder in the Gregtech
			// resources folder in the jar
			Path oreVeinJarRootPath;
			if (sampleUri.getScheme().equals("jar") || sampleUri.getScheme().equals("zip")) {
				zipFileSystem = FileSystems.newFileSystem(sampleUri, Collections.emptyMap());
				worldgenJarRootPath = zipFileSystem.getPath("/assets/ore-module/worldgen");
				oreVeinJarRootPath = zipFileSystem.getPath("/assets/ore-module/worldgen/vein");
			} else if (sampleUri.getScheme().equals("file")) {
				worldgenJarRootPath = Paths.get(PrecisionWorldGenRegistry.class
						.getResource("/assets/ore-module/worldgen")
						.toURI());
				oreVeinJarRootPath = Paths.get(PrecisionWorldGenRegistry.class
						.getResource("/assets/ore-module/worldgen/vein")
						.toURI());
			} else {
				throw new IllegalStateException(
						"Unable to locate absolute path to worldgen root directory: " + sampleUri);
			}

			// Attempts to extract the worldgen definition jsons
			if (targetPath.compareTo(oreVeinRootPath) == 0) {
				OreModule.logger.info(
						"Attempting extraction of standard worldgen definitions from {} to {}",
						oreVeinJarRootPath,
						oreVeinRootPath);
				// Find all the default worldgen files in the assets folder
				List<Path> jarFiles = Files.walk(oreVeinJarRootPath)
						.filter(Files::isRegularFile)
						.collect(Collectors.toList());

				// Replaces or creates the default worldgen files
				for (Path jarFile : jarFiles) {
					Path worldgenPath = oreVeinRootPath.resolve(
							oreVeinJarRootPath.relativize(jarFile).toString());
					Files.createDirectories(worldgenPath.getParent());
					Files.copy(jarFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);
				}
				OreModule.logger.info(
						"Extracted {} builtin worldgen vein definitions into vein folder", jarFiles.size());
			}
			// Attempts to extract the named dimensions json folder
			else if (targetPath.compareTo(dimensionsRootPath) == 0) {
				OreModule.logger.info(
						"Attempting extraction of standard dimension definitions from {} to {}",
						worldgenJarRootPath,
						dimensionsRootPath);

				Path dimensionFile = worldgenJarRootPath.resolve("dimensions.json");

				Path worldgenPath = dimensionsRootPath.resolve(
						worldgenJarRootPath.relativize(worldgenJarRootPath).toString());
				Files.copy(dimensionFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);

				OreModule.logger.info("Extracted builtin dimension definitions into worldgen folder");
			} else if (targetPath.compareTo(layersRootPath) == 0) {
				OreModule.logger.info(
						"Attempting extraction of standard layers definitions from {} to {}",
						worldgenJarRootPath,
						layersRootPath);

				Path layersFile = worldgenJarRootPath.resolve("layers.json");

				Path worldgenPath = layersRootPath.resolve(
						worldgenJarRootPath.relativize(worldgenJarRootPath).toString());
				Files.copy(layersFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);
			}
			// Attempts to extract lock txt file
			else if (targetPath.compareTo(extractLockPath) == 0) {
				Path extractLockFile = worldgenJarRootPath.resolve("worldgen_extracted.json");

				Path worldgenPath = extractLockPath.resolve(
						worldgenJarRootPath.relativize(worldgenJarRootPath).toString());
				Files.copy(extractLockFile, worldgenPath, StandardCopyOption.REPLACE_EXISTING);

				OreModule.logger.info("Extracted jar lock file into worldgen folder");
			}

		} catch (URISyntaxException impossible) {
			// this is impossible, since getResource always returns valid URI
			throw new RuntimeException(impossible);
		} finally {
			if (zipFileSystem != null) {
				// close zip file system to avoid issues
				IOUtils.closeQuietly(zipFileSystem);
			}
		}
	}

	private void removeExistingFiles(Path root, @Nonnull List<? extends IWorldgenDefinition> definitions) {
		for (IWorldgenDefinition definition : definitions) {
			Path filePath = root.resolve(Paths.get(definition.getDepositName()));

			try {
				if (Files.exists(filePath)) {
					Files.delete(filePath);
					OreModule.logger.info("Removed oregen file at {}", definition.getDepositName());
				}
			} catch (IOException exception) {
				OreModule.logger.error("Failed to remove oregen file at {}", definition.getDepositName());
			}
		}
	}

	private <T extends IWorldgenDefinition> void addAddonFiles(
			Path root, @Nonnull List<T> definitions, @Nonnull List<T> registeredDefinitions) {
		for (IWorldgenDefinition definition : definitions) {

			JsonObject element = FileUtility.tryExtractFromFile(root.resolve(definition.getDepositName()));

			if (element == null) {
				OreModule.logger.error(
						"Addon mod tried to register bad precision.ore definition at {}", definition.getDepositName());
				definitions.remove(definition);
				continue;
			}

			try {
				definition.initializeFromConfig(element);
				registeredDefinitions.add((T) definition);
			} catch (RuntimeException exception) {
				OreModule.logger.error(
						"Failed to parse addon worldgen definition {}", definition.getDepositName(), exception);
			}
		}
	}

	/**
	 * Gathers the designated named dimensions from the designated json file
	 *
	 * @param dimensionsFile The Path to the dimensions.json file
	 */
	private void gatherNamedDimensions(Path dimensionsFile) {
		JsonObject element = FileUtility.tryExtractFromFile(dimensionsFile);
		if (element == null) {
			return;
		}

		try {
			JsonArray dims = element.getAsJsonArray("dims");
			for (JsonElement dim : dims) {
				namedDimensions.put(
						dim.getAsJsonObject().get("dimID").getAsInt(),
						dim.getAsJsonObject().get("dimName").getAsString());
			}
		} catch (RuntimeException exception) {
			OreModule.logger.error("Failed to parse named dimensions", exception);
		}
	}

	/**
	 * Gathers the designated named dimensions from the designated json file
	 *
	 * @param layersFile The Path to the layers.json file
	 */
	private void gatherLayerOperations(Path layersFile) {
		JsonObject element = FileUtility.tryExtractFromFile(layersFile);
		if (element == null) {
			return;
		}

		try {
			JsonArray layers = element.getAsJsonArray("layers");
			for (JsonElement layer : layers) {
				int min = layer.getAsJsonObject()
						.get("operations")
						.getAsJsonObject()
						.get("min")
						.getAsInt();
				int max = layer.getAsJsonObject()
						.get("operations")
						.getAsJsonObject()
						.get("max")
						.getAsInt();
				layerOperations.put(layer.getAsJsonObject().get("layer").getAsInt(), Pair.of(min, max));
			}
		} catch (RuntimeException exception) {
			OreModule.logger.error("Failed to parse layer operations", exception);
		}
	}

	public static List<BedrockOreDepositDefinition> getBedrockOreVeinDeposit() {
		return Collections.unmodifiableList(INSTANCE.registeredBedrockOreVeinDefinitions);
	}

	public static Map<Integer, String> getNamedDimensions() {
		return INSTANCE.namedDimensions;
	}

	public static Map<Integer, Pair<Integer, Integer>> getLayerOperations() {
		return INSTANCE.layerOperations;
	}

	public static Set<Integer> getLayers() {
		return INSTANCE.layerOperations.keySet();
	}
}
