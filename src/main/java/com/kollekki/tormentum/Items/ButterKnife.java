package com.kollekki.tormentum.Items;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ButterKnife extends Item {
    public ButterKnife(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(Tormentum.BLEEDING, 100, 0, false, false, false));
        }
        return InteractionResult.SUCCESS;
    }
}