package com.adaptor.deadrecall.core.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/** Optional compatibility transport for an externally owned death-backpack commit transaction. */
public interface DeathBackpackCaptureTransport {
    boolean commit(ServerPlayer player, ServerLevel level, BlockPos position, List<ItemStack> contents);

    static void register(DeathBackpackCaptureTransport adapter) { Holder.adapter = adapter; }
    static Optional<DeathBackpackCaptureTransport> current() { return Optional.ofNullable(Holder.adapter); }

    final class Holder {
        private static volatile DeathBackpackCaptureTransport adapter;
        private Holder() { }
    }
}
