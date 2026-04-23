package com.kollekki.tormentum.items;

import com.kollekki.tormentum.Tormentum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ChalkItem extends Item {

	public ChalkItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos clickedPos = context.getClickedPos();
		Player player = context.getPlayer();

		if (context.getClickedFace() != Direction.UP) {
			return InteractionResult.PASS;
		}

		BlockPos placePos = clickedPos.above();

		BlockState existingState = level.getBlockState(placePos);
		boolean canReplace = existingState.canBeReplaced(new BlockPlaceContext(context));

		if (!existingState.isAir() && !canReplace) {
			return InteractionResult.FAIL;
		}

		if (!level.isClientSide()) {
			BlockPlaceContext placeContext = new BlockPlaceContext(context) {
				@Override
				public BlockPos getClickedPos() {
					return placePos;
				}
			};

			BlockState state = Tormentum.CHALK_BLOCK.get().getStateForPlacement(placeContext);

			if (state != null) {
				level.setBlockAndUpdate(placePos, state);
			}

			level.playSound(
				null,
				clickedPos,
				SoundEvents.SAND_PLACE,
				SoundSource.PLAYERS,
				0.8f,
				1.0f + 0.3f * (player != null ? player.getRandom().nextFloat() : 0f)
			);
		}

		return InteractionResult.SUCCESS;
	}
}