package com.darkbladedev.cinematic.adapters.actor;

import com.darkbladedev.cinematic.actors.ActorAction;
import com.darkbladedev.cinematic.actors.ActorPose;
import com.darkbladedev.cinematic.actors.EquipmentSlot;
import com.darkbladedev.cinematic.actors.ItemUsageState;
import com.darkbladedev.cinematic.adapters.packet.PacketEventsBridge;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.netty.buffer.ByteBufAllocationOperator;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.joml.Vector3d;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VirtualPlayerActorPacketTest {

    @BeforeAll
    static void setupPacketEvents() {
        com.darkbladedev.cinematic.adapters.packet.PacketEventsTestHelper.init();
    }

    @Test
    @DisplayName("VirtualPlayerActor generates valid spawn and player info packets with unlisted status")
    void generatesSpawnAndPlayerInfoPackets() {
        UUID actorId = UUID.randomUUID();
        SkinCacheService skinCache = new SkinCacheService();
        skinCache.cache("Steve", new SkinCacheService.SkinData("dummy_texture_base64", "dummy_signature"));

        VirtualPlayerActor actor = new VirtualPlayerActor(
                actorId,
                "Steve",
                new Vector3d(100.5, 65.0, -200.5),
                45.0f,
                -10.0f,
                List::of,
                new PacketEventsBridge(),
                skinCache
        );

        WrapperPlayServerPlayerInfoUpdate infoAdd = actor.createPlayerInfoUpdatePacket();
        assertThat(infoAdd.getActions()).contains(
                WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
        );
        assertThat(infoAdd.getEntries()).hasSize(1);
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry = infoAdd.getEntries().get(0);
        assertThat(entry.isListed()).isFalse();
        assertThat(entry.getGameProfile().getTextureProperties()).hasSize(1);
        assertThat(entry.getGameProfile().getTextureProperties().get(0).getValue()).isEqualTo("dummy_texture_base64");

        WrapperPlayServerSpawnEntity spawn = actor.createSpawnPacket();
        assertThat(spawn.getEntityId()).isEqualTo(actor.entityId());
        assertThat(spawn.getEntityType()).isEqualTo(EntityTypes.PLAYER);
        assertThat(spawn.getUUID()).contains(actorId);
        assertThat(spawn.getPosition().getX()).isEqualTo(100.5);
        assertThat(spawn.getPosition().getY()).isEqualTo(65.0);
        assertThat(spawn.getPosition().getZ()).isEqualTo(-200.5);

        WrapperPlayServerEntityHeadLook headLook = actor.createHeadLookPacket();
        assertThat(headLook.getEntityId()).isEqualTo(actor.entityId());
        assertThat(headLook.getHeadYaw()).isEqualTo(45.0f);
    }

    @Test
    @DisplayName("VirtualPlayerActor generates strongly-typed metadata for poses and item usages")
    void generatesMetadataPackets() {
        VirtualPlayerActor actor = new VirtualPlayerActor(
                UUID.randomUUID(),
                "Alex",
                new Vector3d(0, 64, 0),
                0f,
                0f,
                List::of,
                new PacketEventsBridge()
        );

        actor.setPose(ActorPose.CROUCHING);
        actor.setItemUsage(ItemUsageState.BLOCKING);

        WrapperPlayServerEntityMetadata metadataPacket = actor.createMetadataPacket();
        assertThat(metadataPacket.getEntityId()).isEqualTo(actor.entityId());
        List<EntityData<?>> entityData = metadataPacket.getEntityMetadata();
        assertThat(entityData).hasSize(4);

        // Byte flags (Index 0) should have crouching bit (0x02)
        EntityData<?> flagsData = entityData.stream().filter(d -> d.getIndex() == 0).findFirst().orElseThrow();
        assertThat(((Byte) flagsData.getValue()) & 0x02).isNotZero();

        // Pose (Index 6)
        EntityData<?> poseData = entityData.stream().filter(d -> d.getIndex() == 6).findFirst().orElseThrow();
        assertThat(poseData.getValue()).isEqualTo(EntityPose.CROUCHING);

        // Hand state (Index 8) should have active hand bit (0x01)
        EntityData<?> handData = entityData.stream().filter(d -> d.getIndex() == 8).findFirst().orElseThrow();
        assertThat(((Byte) handData.getValue()) & 0x01).isNotZero();

        // Skin parts (Index 17) should be enabled
        EntityData<?> skinData = entityData.stream().filter(d -> d.getIndex() == 17).findFirst().orElseThrow();
        assertThat(skinData.getValue()).isEqualTo((byte) 0x7F);
    }

    @Test
    @DisplayName("VirtualPlayerActor creates valid relative move, teleport, animation, and equipment packets")
    void generatesMotionAndActionPackets() {
        VirtualPlayerActor actor = new VirtualPlayerActor(
                UUID.randomUUID(),
                "Knight",
                new Vector3d(10, 64, 10),
                90f,
                15f,
                List::of,
                new PacketEventsBridge()
        );

        WrapperPlayServerEntityRelativeMoveAndRotation relMove = actor.createRelMovePacket((short) 4096, (short) 0, (short) -4096);
        assertThat(relMove.getEntityId()).isEqualTo(actor.entityId());
        assertThat(relMove.getDeltaX()).isEqualTo((short) 4096);

        WrapperPlayServerEntityTeleport teleport = actor.createTeleportPacket();
        assertThat(teleport.getEntityId()).isEqualTo(actor.entityId());
        assertThat(teleport.getPosition().getX()).isEqualTo(10.0);

        WrapperPlayServerEntityAnimation anim = actor.createAnimationPacket(ActorAction.SWING_MAIN_HAND);
        assertThat(anim.getEntityId()).isEqualTo(actor.entityId());
        assertThat(anim.getType()).isEqualTo(WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM);

        WrapperPlayServerEntityAnimation animHurt = actor.createAnimationPacket(ActorAction.HURT);
        assertThat(animHurt.getType()).isEqualTo(WrapperPlayServerEntityAnimation.EntityAnimationType.HURT);

        WrapperPlayServerEntityEquipment equip = actor.createEquipmentPacket(EquipmentSlot.MAIN_HAND, "DIAMOND_SWORD");
        assertThat(equip.getEntityId()).isEqualTo(actor.entityId());
        assertThat(equip.getEquipment()).hasSize(1);
        assertThat(equip.getEquipment().get(0).getSlot()).isEqualTo(com.github.retrooper.packetevents.protocol.player.EquipmentSlot.MAIN_HAND);
    }

    @Test
    @DisplayName("VirtualPlayerActor generates destroy and player info removal packets on despawn")
    void generatesDespawnPackets() {
        VirtualPlayerActor actor = new VirtualPlayerActor(
                UUID.randomUUID(),
                "Ghost",
                new Vector3d(0, 0, 0),
                0f,
                0f,
                List::of,
                new PacketEventsBridge()
        );

        WrapperPlayServerDestroyEntities destroy = actor.createDestroyPacket();
        assertThat(destroy.getEntityIds()).containsExactly(actor.entityId());

        WrapperPlayServerPlayerInfoRemove removeInfo = actor.createPlayerInfoRemovePacket();
        assertThat(removeInfo.getProfileIds()).containsExactly(actor.id());

        actor.despawn();
        assertThat(actor.isValid()).isFalse();
    }
}
