package com.darkbladedev.cinematic.adapters.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.NettyManager;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import com.github.retrooper.packetevents.util.LogManager;
import io.github.retrooper.packetevents.impl.netty.buffer.ByteBufAllocationOperatorImpl;
import io.github.retrooper.packetevents.impl.netty.buffer.ByteBufOperatorImpl;
import org.mockito.Mockito;

public final class PacketEventsTestHelper {
    private static boolean initialized = false;

    private PacketEventsTestHelper() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        PacketEventsAPI<?> mockApi = Mockito.mock(PacketEventsAPI.class);
        ServerManager serverManager = Mockito.mock(ServerManager.class);
        Mockito.when(serverManager.getVersion()).thenReturn(ServerVersion.V_1_20_4);
        Mockito.when(mockApi.getServerManager()).thenReturn(serverManager);

        PacketEventsSettings settings = new PacketEventsSettings()
                .customResourceProvider(path -> PacketEvents.class.getClassLoader().getResourceAsStream(path));
        Mockito.when(mockApi.getSettings()).thenReturn(settings);

        LogManager logManager = Mockito.mock(LogManager.class);
        Mockito.when(mockApi.getLogManager()).thenReturn(logManager);

        NettyManager nettyManager = Mockito.mock(NettyManager.class);
        Mockito.when(nettyManager.getByteBufAllocationOperator()).thenReturn(new ByteBufAllocationOperatorImpl());
        Mockito.when(nettyManager.getByteBufOperator()).thenReturn(new ByteBufOperatorImpl());
        Mockito.when(mockApi.getNettyManager()).thenReturn(nettyManager);

        PacketEvents.setAPI(mockApi);
        initialized = true;
    }
}
