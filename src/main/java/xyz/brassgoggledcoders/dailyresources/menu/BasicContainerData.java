package xyz.brassgoggledcoders.dailyresources.menu;

import net.minecraft.world.inventory.ContainerData;

import java.util.Arrays;

public class BasicContainerData implements ContainerData {
    private final int[] values;

    public BasicContainerData(int size, int start) {
        this.values = new int[size];
        Arrays.fill(this.values, start);
    }


    @Override
    public int get(int pIndex) {
        return values[pIndex];
    }

    @Override
    public void set(int pIndex, int pValue) {
        values[pIndex] = pValue;
    }

    @Override
    public int getCount() {
        return values.length;
    }
}
