package com.kollekki.tormentum.BlockEntities;

import com.kollekki.tormentum.Recipes.RitualRecipe;
import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class CrackedSkullBlockEntity extends BlockEntity {

    private ItemStack heldItem = ItemStack.EMPTY;

    private boolean ritualActive = false;
    private int ritualTicksRemaining = 0;
    private RitualRecipe activeRecipe = null;
    private int bloodConsumedCount = 0;

    private static final DustParticleOptions BLOOD_DUST_TRAVEL =
            new DustParticleOptions(0xFFDD0000, 1.2f);
    private static final DustParticleOptions BLOOD_DUST_AMBIENT =
            new DustParticleOptions(0xFF8B0000, 0.8f);
    private static final DustParticleOptions BLOOD_DUST_BURST =
            new DustParticleOptions(0xFFFF0000, 2.0f);

    public CrackedSkullBlockEntity(BlockPos pos, BlockState state) {
        super(Tormentum.CRACKED_SKULL_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        return this.heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            if (!this.level.isClientSide() && !stack.isEmpty()) {
                checkAndStartRitual();
            }
        }
    }

    private void setHeldItemDirect(ItemStack stack) {
        this.heldItem = stack;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isRitualActive() {
        return ritualActive;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrackedSkullBlockEntity entity) {
        if (level.isClientSide() || !entity.ritualActive) return;

        ServerLevel serverLevel = (ServerLevel) level;

        entity.ritualTicksRemaining--;

        if (entity.activeRecipe != null && entity.bloodConsumedCount < entity.activeRecipe.bloodConsumption) {
            int totalBlood = entity.activeRecipe.bloodConsumption;
            int totalTime  = entity.activeRecipe.ritualTime;
            int elapsed    = totalTime - entity.ritualTicksRemaining;
            int shouldHaveConsumed = Math.min(
                    (int) ((float) elapsed / totalTime * totalBlood + 0.99f),
                    totalBlood
            );

            while (entity.bloodConsumedCount < shouldHaveConsumed) {
                BlockPos bloodPos = entity.findBloodBlockInSquare();
                if (bloodPos == null) {
                    entity.failRitual(serverLevel);
                    return;
                }
                serverLevel.removeBlock(bloodPos, false);
                spawnTravelParticles(serverLevel, bloodPos, pos);
                entity.bloodConsumedCount++;
            }
        }

        if (entity.ritualTicksRemaining % 5 == 0) {
            serverLevel.sendParticles(BLOOD_DUST_AMBIENT,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    3, 0.3, 0.2, 0.3, 0.01);
        }

        if (entity.ritualTicksRemaining <= 0) {
            entity.completeRitual(serverLevel);
        }
    }

    private static void spawnTravelParticles(ServerLevel serverLevel, BlockPos from, BlockPos to) {
        double fx = from.getX() + 0.5;
        double fy = from.getY() + 0.5;
        double fz = from.getZ() + 0.5;
        double tx = to.getX() + 0.5;
        double ty = to.getY() + 0.4;
        double tz = to.getZ() + 0.5;

        double dx   = tx - fx;
        double dy   = ty - fy;
        double dz   = tz - fz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist == 0) return;

        for (int i = 0; i < 8; i++) {
            double jx = dx / dist + (Math.random() - 0.5) * 0.12;
            double jy = dy / dist + (Math.random() - 0.5) * 0.12;
            double jz = dz / dist + (Math.random() - 0.5) * 0.12;
            serverLevel.sendParticles(BLOOD_DUST_TRAVEL, fx, fy, fz, 0, jx, jy, jz, 0.18);
        }
    }

    private void checkAndStartRitual() {
        if (ritualActive || level == null || level.isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level;

        var allRituals = serverLevel.getServer().getRecipeManager()
                .getRecipes()
                .stream()
                .filter(h -> h.value() instanceof RitualRecipe)
                .map(h -> (RitualRecipe) h.value())
                .toList();

        for (RitualRecipe recipe : allRituals) {
            if (!matchesCatalyst(recipe))   continue;
            if (!matchesSkullBlock(recipe)) continue;

            int half = recipe.chalkSquareSize / 2;

            if (!checkChalkSquare(half))                             continue;
            if (!checkCustomBlocks(half, recipe.customBlocksNeeded)) continue;

            activeRecipe         = recipe;
            ritualTicksRemaining = recipe.ritualTime;
            ritualActive         = true;
            bloodConsumedCount   = 0;

            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            serverLevel.playSound(null, worldPosition,
                    SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.BLOCKS, 1.0f, 0.75f);
            return;
        }
    }

    private boolean matchesCatalyst(RitualRecipe recipe) {
        if (recipe.ritualCatalyst == null || heldItem.isEmpty()) return false;
        return BuiltInRegistries.ITEM
                .getOptional(recipe.ritualCatalyst)
                .map(item -> heldItem.getItem() == item)
                .orElse(false);
    }

    private boolean matchesSkullBlock(RitualRecipe recipe) {
        if (recipe.ritualSkull == null) return false;
        return BuiltInRegistries.BLOCK
                .getOptional(recipe.ritualSkull)
                .map(block -> getBlockState().is(block))
                .orElse(false);
    }

    private boolean checkChalkSquare(int half) {
        if (level == null || half <= 0) return false;
        Block chalk = Tormentum.CHALK_BLOCK.get();
        int cx = worldPosition.getX(), cy = worldPosition.getY(), cz = worldPosition.getZ();

        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                if (Math.abs(x) != half && Math.abs(z) != half) continue;
                if (!level.getBlockState(new BlockPos(cx + x, cy, cz + z)).is(chalk))
                    return false;
            }
        }
        return true;
    }

    private boolean checkCustomBlocks(int half, List<Identifier> blockIds) {
        if (blockIds.isEmpty() || level == null) return true;
        int cx = worldPosition.getX(), cy = worldPosition.getY(), cz = worldPosition.getZ();

        for (Identifier id : blockIds) {
            Block required = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
            if (required == null) return false;
            boolean found = false;
            scan:
            for (int x = -half; x <= half; x++) {
                for (int z = -half; z <= half; z++) {
                    if (level.getBlockState(new BlockPos(cx + x, cy, cz + z)).is(required)) {
                        found = true;
                        break scan;
                    }
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private BlockPos findBloodBlockInSquare() {
        if (level == null || activeRecipe == null) return null;
        Block puddle = Tormentum.BLOOD_PUDDLE.get();
        Block stain  = Tormentum.BLOOD_STAIN.get();
        int half = activeRecipe.chalkSquareSize / 2;
        int cx = worldPosition.getX(), cy = worldPosition.getY(), cz = worldPosition.getZ();

        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos p = new BlockPos(cx + x, cy, cz + z);
                BlockState bs = level.getBlockState(p);
                if (bs.is(puddle) || bs.is(stain)) candidates.add(p);
            }
        }

        if (candidates.isEmpty()) return null;
        return candidates.get(level.getRandom().nextInt(candidates.size()));
    }

    private void failRitual(ServerLevel serverLevel) {
        serverLevel.playSound(null, worldPosition,
                SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.BLOCKS, 1.0f, 0.5f);

        serverLevel.sendParticles(BLOOD_DUST_AMBIENT,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                20, 0.5, 0.3, 0.5, 0.08);

        ritualActive         = false;
        ritualTicksRemaining = 0;
        activeRecipe         = null;
        bloodConsumedCount   = 0;

        setHeldItemDirect(ItemStack.EMPTY);

        setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }


    private void completeRitual(ServerLevel serverLevel) {
        if (activeRecipe == null) return;
        RitualRecipe recipe = activeRecipe;

        ritualActive         = false;
        ritualTicksRemaining = 0;
        activeRecipe         = null;
        bloodConsumedCount   = 0;

        if (recipe.resultItem != null) {
            BuiltInRegistries.ITEM
                    .getOptional(recipe.resultItem)
                    .ifPresent(item -> setHeldItemDirect(item.getDefaultInstance()));
        } else {
            setHeldItemDirect(ItemStack.EMPTY);
        }

        if (recipe.resultEntity != null) {
            BuiltInRegistries.ENTITY_TYPE
                    .getOptional(recipe.resultEntity)
                    .ifPresent(type -> {
                        var entity = type.create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
                        if (entity != null) {
                            entity.snapTo(
                                    worldPosition.getX() + 0.5,
                                    worldPosition.getY() + 1.0,
                                    worldPosition.getZ() + 0.5,
                                    0F, 0F
                            );
                            serverLevel.addFreshEntity(entity);
                        }
                    });
        }

        serverLevel.playSound(null, worldPosition,
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.5f, 0.6f);
        serverLevel.sendParticles(BLOOD_DUST_BURST,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5,
                60, 0.8, 0.4, 0.8, 0.12);

        setChanged();
        serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.heldItem.isEmpty()) {
            output.store("HeldItem", ItemStack.OPTIONAL_CODEC, this.heldItem);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.heldItem = input.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}