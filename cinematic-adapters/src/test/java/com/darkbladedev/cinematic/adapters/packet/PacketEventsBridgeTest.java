package com.darkbladedev.cinematic.adapters.packet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PacketEventsBridgeTest {

    @Test
    @DisplayName("PacketEventsBridge isAvailable safely returns false when PacketEvents is not present")
    void isAvailableReturnsFalseWhenNotPresent() {
        PacketEventsBridge bridge = new PacketEventsBridge();
        assertThat(bridge.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("PacketEventsBridge sendPacket and broadcastPacket gracefully handle nulls without throwing")
    void handlesNullsGracefully() {
        PacketEventsBridge bridge = new PacketEventsBridge();
        bridge.sendPacket(null, null);
        bridge.broadcastPacket(null, null);
        bridge.broadcastPacket(List.of(), null);
        assertThat(bridge.isAvailable()).isFalse();
    }
}
