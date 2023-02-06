package com.precision.ore.api.capability;

import codechicken.lib.vec.Cuboid6;

public interface IDrillHeadHolder {

    Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    boolean hasDrillHead();

    int getDrillHeadLevel();

    int getDrillHeadColor();

    void damageDrillHead(int damage);
}
