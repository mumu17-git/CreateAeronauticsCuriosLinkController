package com.mumu17.cacl.util;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.WeakHashMap;
import java.util.Collections;

/**
 * 実Levelを保持しつつ、ワールド未登録の仮想 BlockEntity を管理するコンテナ。
 * 実ワールドへ setBlockEntity せず、アイテム状態から復元したBEを安全に扱う。
 */
public final class DummyLevel {
    private final Level realLevel;
    private boolean clientSide;
    private final Map<BlockPos, BlockEntity> virtualBlockEntities = new ConcurrentHashMap<>();
    // BlockEntity -> DummyLevel の逆引き。WeakHashMap を使い、BE がGC可能になるようにする
    private static final Map<BlockEntity, DummyLevel> ownerMap_be = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<UUID, DummyLevel> ownerMap_player = Collections.synchronizedMap(new WeakHashMap<>());

    public DummyLevel(Level realLevel) {
        this(realLevel, realLevel.isClientSide);
    }

    public DummyLevel(Level realLevel, boolean clientSide) {
        this.realLevel = realLevel;
        this.clientSide = clientSide;
    }

    public Level getLevel() {
        return realLevel;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public void setBlockEntity(BlockPos pos, BlockEntity blockEntity, Player player) {
        virtualBlockEntities.put(pos.immutable(), blockEntity);
        ownerMap_be.put(blockEntity, this);
        ownerMap_player.put(player.getUUID(), this);
    }

    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return virtualBlockEntities.get(pos);
    }

    public void removeBlockEntity(BlockPos pos, Player player) {
        BlockEntity removed = virtualBlockEntities.remove(pos);
        if (removed != null) ownerMap_be.remove(removed);
        if (player != null) ownerMap_player.remove(player.getUUID());
    }

    public @Nullable LinkedTypewriterBlockEntity placeLinkedTypewriter(BlockPos pos, BlockEntity blockEntity, Player player) {
        if (!(blockEntity instanceof LinkedTypewriterBlockEntity linkedTypewriterBlockEntity)) {
            return null;
        }
        linkedTypewriterBlockEntity.setLevel(realLevel);
        // setBlockEntity(pos, linkedTypewriterBlockEntity, player);
        return linkedTypewriterBlockEntity;
    }

    /**
     * 指定の BlockEntity が配置されている DummyLevel を取得します。存在しなければ null。
     */
    public static @Nullable DummyLevel getDummyLevelFor(BlockEntity be) {
        return ownerMap_be.get(be);
    }
    /**
     * 指定の Player が持つ DummyLevel を取得します。存在しなければ null。
     */
    public static @Nullable DummyLevel getDummyLevelFor(Player player, boolean sideUpdate) {
        if (player == null) return null;
        if (ownerMap_player.get(player.getUUID()) == null) return null;
        if (sideUpdate) ownerMap_player.get(player.getUUID()).clientSide = player.level().isClientSide;
        return ownerMap_player.get(player.getUUID());
    }

    public  static @Nullable DummyLevel getDummyLevelFor(Player player) {
        return getDummyLevelFor(player, true);
    }
}

