package com.precision.ore.common.items;

import com.precision.core.api.items.CustomMetaItem;
import com.precision.ore.OreModule;
import com.precision.ore.common.tools.DrillHeadBehavior;
import com.precision.ore.common.tools.PrimitiveDrillBehavior;
import com.precision.ore.common.tools.ProspectorScannerBehavior;
import gregtech.api.GTValues;
import gregtech.api.items.metaitem.ElectricStats;
import gregtech.api.items.metaitem.MetaItem;

public class OreMetaItems {

    public static MetaItem<?> ITEMS;

    static {
        ITEMS = new CustomMetaItem(OreModule.MODID);
        ITEMS.setRegistryName("ore_metaitem");
    }

    public static MetaItem<?>.MetaValueItem DRILL_HEAD;
    public static MetaItem<?>.MetaValueItem PRIMITIVE_DRILL;
    public static MetaItem<?>.MetaValueItem PROSPECTOR_LV;
    public static MetaItem<?>.MetaValueItem PROSPECTOR_HV;
    public static MetaItem<?>.MetaValueItem PROSPECTOR_LUV;

    public static void init(){
        OreModule.logger.info("Ore Module Registering Items...");
        DRILL_HEAD = ITEMS.addItem(0, "drill_head").addComponents(new DrillHeadBehavior()).setMaxStackSize(1);
        PRIMITIVE_DRILL = ITEMS.addItem(1, "primitive_drill").addComponents(PrimitiveDrillBehavior.INSTANCE).setMaxStackSize(1);
        PROSPECTOR_LV = ITEMS.addItem(2, "prospector.lv").addComponents(ElectricStats.createElectricItem(100_000L, GTValues.LV), new ProspectorScannerBehavior(2, GTValues.LV)).setMaxStackSize(1);
        PROSPECTOR_HV = ITEMS.addItem(3, "prospector.hv").addComponents(ElectricStats.createElectricItem(1_600_000L, GTValues.HV), new ProspectorScannerBehavior(3, GTValues.HV)).setMaxStackSize(1);
        PROSPECTOR_LUV = ITEMS.addItem(4, "prospector.luv").addComponents(ElectricStats.createElectricItem(1_000_000_000L, GTValues.LuV), new ProspectorScannerBehavior(5, GTValues.LuV)).setMaxStackSize(1);
    }
}
