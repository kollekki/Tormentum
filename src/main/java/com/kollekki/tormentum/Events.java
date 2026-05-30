package com.kollekki.tormentum;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.kollekki.tormentum.Tormentum.CRACKED_SKULL_BLOCK;

@EventBusSubscriber(modid = "tormentum")
public class Events {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(Blocks.WITHER_SKELETON_SKULL) && !state.is(Blocks.WITHER_SKELETON_WALL_SKULL)) {
            return;
        }

        if (!(event.getItemStack().getItem() instanceof AxeItem)) {
            return;
        }

        if (!level.isClientSide()) {
            int facing = Mth.floor((event.getEntity().getYRot() * 8.0F / 360.0F) + 0.5D) & 7;

            BlockState newState = CRACKED_SKULL_BLOCK.get()
                    .defaultBlockState()
                    .setValue(com.kollekki.tormentum.Blocks.CrackedSkullBlock.FACING, facing);

            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);

            event.getItemStack().hurtAndBreak(1, event.getEntity(),
                    event.getEntity().getEquipmentSlotForItem(event.getItemStack()));
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static final Map<UUID, Vec3> LAST_MOTION = new HashMap<>();

    public static void cacheMotion(LivingEntity entity) {
        LAST_MOTION.put(entity.getUUID(), entity.getDeltaMovement());
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (!entity.hasEffect(Tormentum.BLEEDING)) return;

        Vec3 oldMotion = LAST_MOTION.getOrDefault(entity.getUUID(), Vec3.ZERO);

        entity.setDeltaMovement(oldMotion.x, 0, oldMotion.z);

        entity.setOnGround(true);
    }
    private static final Map<UUID, Boolean> WAS_SNEAKING = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        boolean isSneaking = player.isShiftKeyDown();
        boolean wasSneaking = WAS_SNEAKING.getOrDefault(player.getUUID(), false);

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        if (
                !wasSneaking &&
                        isSneaking &&
                        helmet.is(Tormentum.KOLLEKKI_ITEM.get())
        ) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    Tormentum.KOLLEKKI_SOUND.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        WAS_SNEAKING.put(player.getUUID(), isSneaking);
    }
}