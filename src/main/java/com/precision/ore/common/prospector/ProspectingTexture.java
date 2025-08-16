package com.precision.ore.common.prospector;

import com.precision.ore.api.worldgen.vein.BedrockOreDepositDefinition;
import com.precision.ore.api.worldgen.vein.BedrockOreVeinHandler;
import gregtech.api.unification.material.Material;
import gregtech.client.utils.RenderUtil;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ProspectingTexture extends AbstractTexture {

	public static final String SELECTED_ALL = "[all]";

	private String selected = SELECTED_ALL;
	private boolean darkMode;
	private int imageWidth = -1;
	private int imageHeight = -1;
	private int playerXGui;
	private int playerYGui;
	private final ProspectorMode mode;
	private final int radius;
	public final String[][][] data;

	public ProspectingTexture(ProspectorMode mode, int radius, boolean darkMode) {
		this.darkMode = darkMode;
		this.radius = radius;
		this.mode = mode;
		this.data = new String[radius * 2 - 1][radius * 2 - 1][mode == ProspectorMode.ORE ? 2 : 3];
	}

	public void updateTexture(PacketProspecting packet) {
		int playerChunkX = packet.playerChunkX;
		int playerChunkZ = packet.playerChunkZ;
		playerXGui = packet.posX - (playerChunkX - this.radius + 1) * 16 + (packet.posX > 0 ? 1 : 0);
		playerYGui = packet.posZ - (playerChunkZ - this.radius + 1) * 16 + (packet.posX > 0 ? 1 : 0);

		int ox;
		if ((packet.chunkX > 0 && playerChunkX > 0) || (packet.chunkX < 0 && playerChunkX < 0)) {
			ox = Math.abs(Math.abs(packet.chunkX) - Math.abs(playerChunkX));
		} else {
			ox = Math.abs(playerChunkX) + Math.abs(packet.chunkX);
		}
		if (playerChunkX > packet.chunkX) {
			ox = -ox;
		}

		int oy;
		if ((packet.chunkZ > 0 && playerChunkZ > 0) || (packet.chunkZ < 0 && playerChunkZ < 0)) {
			oy = Math.abs(Math.abs(packet.chunkZ) - Math.abs(playerChunkZ));
		} else {
			oy = Math.abs(playerChunkZ) + Math.abs(packet.chunkZ);
		}
		if (playerChunkZ > packet.chunkZ) {
			oy = -oy;
		}

		int currentColumn = (this.radius - 1) + ox;
		int currentRow = (this.radius - 1) + oy;
		if (currentRow < 0) {
			return;
		}
		data[currentColumn][currentRow] = packet.data;
		loadTexture(null);
	}

	private BufferedImage getImage() {
		int size = (this.radius * 2 - 1) * 16;
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();

		Graphics2D graphics2D = image.createGraphics();
		Color backgroundColor = new Color((darkMode ? Color.DARK_GRAY.getRGB() : Color.WHITE.getRGB()));
		for (int i = 0; i < radius * 2 - 1; i++) {
			for (int j = 0; j < radius * 2 - 1; j++) {
				int x = i * 16;
				int y = j * 16;
				// draw bg
				graphics2D.setColor(backgroundColor);
				graphics2D.fillRect(x, y, 16, 16);

				// draw ore

				if (this.mode == ProspectorMode.ORE) {
					String[] entry = data[i][j];

					if (entry != null && entry[0] != null) {

						BedrockOreDepositDefinition deposit = BedrockOreVeinHandler.getDepositByName(entry[0]);

						if (deposit != null) {

							if (selected.equals(SELECTED_ALL) || selected.equals(deposit.getDepositName())) {
								Material material = deposit.getStoredOres().get(0);
								graphics2D.setColor(new Color(material.getMaterialRGB()));
								graphics2D.fillRect(x, y, 16, 16);
							}
						}
					}
				}
				drawGrid(raster, x, y);
			}
		}
		return image;
	}

	private void setSampleDarkened(WritableRaster raster, int x, int y) {
		raster.setSample(x, y, 0, raster.getSample(x, y, 0) / 2);
		raster.setSample(x, y, 1, raster.getSample(x, y, 1) / 2);
		raster.setSample(x, y, 2, raster.getSample(x, y, 2) / 2);
	}

	private void drawGrid(WritableRaster raster, int x, int y) {
		for (int i = 0; i < 16; ++i) {
			setSampleDarkened(raster, x + i, y);
			setSampleDarkened(raster, x, y + i);
		}
	}

	@Override
	public void loadTexture(@Nullable IResourceManager resourceManager) {
		this.deleteGlTexture();
		int tId = getGlTextureId();
		if (tId < 0) return;
		TextureUtil.uploadTextureImageAllocate(getGlTextureId(), getImage(), false, false);
		imageWidth = (radius * 2 - 1) * 16;
		imageHeight = (radius * 2 - 1) * 16;
	}

	public void loadTexture(@Nullable IResourceManager resourceManager, String selected) {
		this.selected = selected;
		loadTexture(resourceManager);
	}

	public void loadTexture(@Nullable IResourceManager resourceManager, boolean darkMode) {
		this.darkMode = darkMode;
		loadTexture(resourceManager);
	}

	public String getSelected() {
		return selected;
	}

	public void draw(int x, int y) {
		if (this.glTextureId < 0) return;
		GlStateManager.bindTexture(this.getGlTextureId());
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
		if (this.mode == ProspectorMode.FLUID) { // draw fluids in grid
			for (int cx = 0; cx < this.radius * 2 - 1; cx++) {
				for (int cz = 0; cz < this.radius * 2 - 1; cz++) {
					if (this.data[cx][cz] != null && this.data[cx][cz][0] != null) {
						Fluid fluid = FluidRegistry.getFluid(this.data[cx][cz][0]);
						if (selected.equals(SELECTED_ALL) || selected.equals(fluid.getName())) {
							RenderUtil.drawFluidForGui(
									new FluidStack(fluid, 1), 1, x + cx * 16 + 1, y + cz * 16 + 1, 16, 16);
						}
					}
				}
			}
		}
		// draw red vertical line
		if (playerXGui % 16 > 7 || playerXGui % 16 == 0) {
			Gui.drawRect(x + playerXGui - 1, y, x + playerXGui, y + imageHeight, Color.RED.getRGB());
		} else {
			Gui.drawRect(x + playerXGui, y, x + playerXGui + 1, y + imageHeight, Color.RED.getRGB());
		}
		// draw red horizontal line
		if (playerYGui % 16 > 7 || playerYGui % 16 == 0) {
			Gui.drawRect(x, y + playerYGui - 1, x + imageWidth, y + playerYGui, Color.RED.getRGB());
		} else {
			Gui.drawRect(x, y + playerYGui, x + imageWidth, y + playerYGui + 1, Color.RED.getRGB());
		}
	}
}
