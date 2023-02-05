package precision.ore.common.items;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.util.ResourceLocation;
import precision.ore.OreModule;

public class OreMetaItem extends StandardMetaItem {

    @Override
    public ResourceLocation createItemModelPath(MetaItem<?>.MetaValueItem metaValueItem, String postfix) {
        return new ResourceLocation(OreModule.MODID, formatModelPath(metaValueItem) + postfix);
    }
}
