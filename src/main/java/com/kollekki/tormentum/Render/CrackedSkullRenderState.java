package com.kollekki.tormentum.Render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState; // fixed
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class CrackedSkullRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public ItemStack heldItem = ItemStack.EMPTY;
    public long gameTime = 0;
    public float partialTick = 0;
}