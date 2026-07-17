package com.adaptor.deadrecall.item.copper;

import com.adaptor.deadrecall.item.ModItems;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CopperGolemAuthorityPressureGameTest {
    private static final BlockPos GOLEM_POS = new BlockPos(2, 2, 2);
    private static final int PRESSURE_GOLEM_COUNT = 64;

    @GameTest(maxTicks = 40)
    public void staleRevisionCannotMutateModeRunningOrLlmState(GameTestHelper helper) {
        CopperGolem golem = createGolem(helper, GOLEM_POS);
        ServerPlayer player = createBoundPlayer(helper, golem, GOLEM_POS.offset(1, 0, 0));
        try {
            CopperGolemData.migrate(golem);
            int initialRevision = CopperGolemData.revision(golem);
            CopperGolemWrenchHandler.setTransportEnabledFromUi(player, golem.getUUID(), true, initialRevision);
            int acceptedRevision = CopperGolemData.revision(golem);
            require(helper, acceptedRevision > initialRevision, "Accepted running mutation did not advance revision");
            require(helper, CopperGolemWrenchHandler.isTransportEnabled(golem), "Accepted running mutation was not applied");

            CopperGolemWrenchHandler.setTransportEnabledFromUi(player, golem.getUUID(), false, initialRevision);
            CopperGolemWrenchHandler.setModeFromUi(player, golem.getUUID(), CopperGolemMode.GATHERING.id(), initialRevision);
            CopperGolemWrenchHandler.setGatheringLlmFromUi(player, golem.getUUID(), true, "stale prompt", initialRevision);

            require(helper, CopperGolemData.revision(golem) == acceptedRevision,
                    "Stale UI mutation changed the authoritative revision");
            require(helper, CopperGolemWrenchHandler.isTransportEnabled(golem),
                    "Stale running mutation changed authoritative state");
            require(helper, CopperGolemWrenchHandler.getMode(golem) == CopperGolemMode.SORTING,
                    "Stale mode mutation changed authoritative state");
            CompoundTag tag = CopperGolemData.readEntityTag(golem);
            require(helper, !tag.getBooleanOr("deadrecall_gathering_llm_enabled", false)
                            && tag.getStringOr("deadrecall_gathering_llm_prompt", "").isBlank(),
                    "Stale LLM mutation changed authoritative state");
            helper.succeed();
        } finally {
            player.discard();
            golem.discard();
        }
    }

    @GameTest(maxTicks = 50)
    public void sameTickPlayersUseFirstAcceptedRevisionAndRejectRemainingMutations(GameTestHelper helper) {
        CopperGolem golem = createGolem(helper, GOLEM_POS);
        ServerPlayer first = createBoundPlayer(helper, golem, GOLEM_POS.offset(1, 0, 0));
        ServerPlayer second = createBoundPlayer(helper, golem, GOLEM_POS.offset(-1, 0, 0));
        try {
            CopperGolemData.migrate(golem);
            int sharedRevision = CopperGolemData.revision(golem);
            CopperGolemWrenchHandler.setTransportEnabledFromUi(first, golem.getUUID(), true, sharedRevision);
            CopperGolemWrenchHandler.setTransportEnabledFromUi(second, golem.getUUID(), false, sharedRevision);
            CopperGolemWrenchHandler.setModeFromUi(second, golem.getUUID(), CopperGolemMode.GATHERING.id(), sharedRevision);

            require(helper, CopperGolemWrenchHandler.isTransportEnabled(golem),
                    "Second same-tick mutation overwrote the first accepted running state");
            require(helper, CopperGolemWrenchHandler.getMode(golem) == CopperGolemMode.SORTING,
                    "Second same-tick mutation changed mode using a consumed revision");
            require(helper, CopperGolemData.revision(golem) == sharedRevision + 1,
                    "Same-tick competing mutations advanced revision more than once");
            helper.succeed();
        } finally {
            first.discard();
            second.discard();
            golem.discard();
        }
    }

    @GameTest(maxTicks = 180)
    public void manyGatheringGolemsRemainBoundedAndDiscardedEntriesArePruned(GameTestHelper helper) {
        List<CopperGolem> golems = new ArrayList<>();
        for (int index = 0; index < PRESSURE_GOLEM_COUNT; index++) {
            int x = 2 + (index % 8) * 2;
            int z = 2 + (index / 8) * 2;
            BlockPos pos = new BlockPos(x, 2, z);
            helper.setBlock(pos.below(), Blocks.STONE);
            CopperGolem golem = createGolem(helper, pos);
            configurePressureGathering(helper, golem, pos);
            golems.add(golem);
        }

        helper.runAtTickTime(80, () -> {
            Map<?, ?> tracked = trackedGolems();
            require(helper, tracked.size() >= PRESSURE_GOLEM_COUNT,
                    "Controller failed to track the pressure fixture");
            for (CopperGolem golem : golems) {
                require(helper, golem.isAlive() && !golem.isRemoved(),
                        "Pressure scanning removed or killed a managed copper golem");
                long cursor = CopperGolemData.readEntityTag(golem)
                        .getLongOr("deadrecall_gathering_scan_index", 0L);
                require(helper, cursor >= 0L, "Pressure scanning produced an invalid negative cursor");
            }
            for (int index = 0; index < PRESSURE_GOLEM_COUNT / 2; index++) {
                golems.get(index).discard();
            }
        });

        helper.runAtTickTime(130, () -> {
            Map<?, ?> tracked = trackedGolems();
            int surviving = 0;
            for (CopperGolem golem : golems) {
                if (!golem.isRemoved()) {
                    surviving++;
                }
            }
            require(helper, surviving == PRESSURE_GOLEM_COUNT / 2,
                    "Pressure fixture survivor count changed unexpectedly");
            require(helper, tracked.size() <= surviving,
                    "Controller retained discarded copper golem tracking entries");
            for (CopperGolem golem : golems) {
                CopperGolemWrenchHandler.untrackCopperGolem(golem);
                golem.discard();
            }
            helper.succeed();
        });
    }

    private static void configurePressureGathering(GameTestHelper helper, CopperGolem golem, BlockPos relativePos) {
        BlockPos absolute = helper.absolutePos(relativePos);
        CompoundTag tag = CopperGolemData.readEntityTag(golem);
        tag.putString(CopperGolemData.TAG_MODE, CopperGolemMode.GATHERING.id());
        tag.putBoolean(CopperGolemData.TAG_TRANSPORT_ENABLED, true);
        tag.putString("deadrecall_gathering_area_dim", helper.getLevel().dimension().identifier().toString());
        writePos(tag, absolute.offset(-1, 0, -1),
                "deadrecall_gathering_corner_a_x", "deadrecall_gathering_corner_a_y", "deadrecall_gathering_corner_a_z");
        writePos(tag, absolute.offset(1, 1, 1),
                "deadrecall_gathering_corner_b_x", "deadrecall_gathering_corner_b_y", "deadrecall_gathering_corner_b_z");
        CopperGolemData.writeStringList(tag, "deadrecall_gathering_manual_targets",
                List.of("minecraft:stone"), 64);
        CopperGolemData.writeItemStack(tag, "deadrecall_gathering_tool_stack", new ItemStack(Items.IRON_PICKAXE));
        CopperGolemData.writeItemStack(tag, CopperGolemData.TAG_FUEL_STACK, new ItemStack(Items.COAL));
        CopperGolemData.writeEntityTag(golem, tag);
        invokeWrench("trackCopperGolem", new Class<?>[]{CopperGolem.class}, golem);
    }

    private static ServerPlayer createBoundPlayer(GameTestHelper helper, CopperGolem golem, BlockPos relativePos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos absolute = helper.absolutePos(relativePos);
        player.snapTo(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D, 0.0F, 0.0F);
        ItemStack wrench = new ItemStack(ModItems.COPPER_WRENCH);
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        invokeWrench("setSelectedGolem", new Class<?>[]{ItemStack.class, UUID.class}, wrench, golem.getUUID());
        return player;
    }

    private static CopperGolem createGolem(GameTestHelper helper, BlockPos relativePos) {
        Object entityType = BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.fromNamespaceAndPath("minecraft", "copper_golem"));
        if (entityType == null) {
            throw helper.assertionException("Missing minecraft:copper_golem entity type");
        }
        try {
            for (Constructor<?> constructor : CopperGolem.class.getDeclaredConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 2
                        || !parameterTypes[0].isInstance(entityType)
                        || !parameterTypes[1].isInstance(helper.getLevel())) {
                    continue;
                }
                constructor.setAccessible(true);
                CopperGolem golem = (CopperGolem) constructor.newInstance(entityType, helper.getLevel());
                BlockPos absolute = helper.absolutePos(relativePos);
                golem.snapTo(absolute.getX() + 0.5D, absolute.getY(), absolute.getZ() + 0.5D, 0.0F, 0.0F);
                require(helper, helper.getLevel().addFreshEntity(golem),
                        "Could not add copper golem pressure fixture");
                return golem;
            }
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Could not construct copper golem fixture", exception);
        }
        throw helper.assertionException("No compatible CopperGolem constructor was found");
    }

    @SuppressWarnings("unchecked")
    private static Map<UUID, ?> trackedGolems() {
        try {
            Class<?> controller = Class.forName("com.adaptor.deadrecall.item.copper.CopperGolemController");
            Field field = controller.getDeclaredField("TRACKED_COPPER_GOLEMS");
            field.setAccessible(true);
            return (Map<UUID, ?>) field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Could not inspect CopperGolemController tracking state", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T invokeWrench(String name, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Method method = CopperGolemWrenchHandler.class.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return (T) method.invoke(null, arguments);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Could not invoke CopperGolemWrenchHandler#" + name, exception);
        }
    }

    private static void writePos(CompoundTag tag, BlockPos pos, String xKey, String yKey, String zKey) {
        tag.putInt(xKey, pos.getX());
        tag.putInt(yKey, pos.getY());
        tag.putInt(zKey, pos.getZ());
    }

    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) {
            throw helper.assertionException(message);
        }
    }
}
