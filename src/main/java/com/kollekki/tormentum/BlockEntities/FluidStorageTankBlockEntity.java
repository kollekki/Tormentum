package com.kollekki.tormentum.BlockEntities;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidStorageTankBlockEntity extends BlockEntity {

    public static final int CAPACITY = 10000;

    private FluidStack storedFluid = FluidStack.EMPTY;

    public FluidStorageTankBlockEntity(BlockPos pos, BlockState state) {
        super(Tormentum.FLUID_STORAGE_TANK_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidStorageTankBlockEntity be) {
    }

    public FluidStack getStoredFluid() {
        return storedFluid;
    }

    public int fill(FluidStack resource) {

        if (!resource.getFluid().isSame(Tormentum.BLOOD_SOURCE.get())) {
            return 0;
        }

        if (storedFluid.isEmpty()) {
            int amount = Math.min(resource.getAmount(), CAPACITY);

            storedFluid = new FluidStack(resource.getFluid(), amount);

            setChanged();

            return amount;
        }

        if (!storedFluid.getFluid().isSame(resource.getFluid())) {
            return 0;
        }

        int space = CAPACITY - storedFluid.getAmount();

        if (space <= 0) {
            return 0;
        }

        int amount = Math.min(space, resource.getAmount());

        storedFluid.grow(amount);

        setChanged();

        return amount;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("BloodAmount", storedFluid.getAmount());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        int amount = input.getIntOr("BloodAmount", 0);

        if (amount > 0) {
            storedFluid = new FluidStack(Tormentum.BLOOD_SOURCE.get(), amount);
        } else {
            storedFluid = FluidStack.EMPTY;
        }
    }
}