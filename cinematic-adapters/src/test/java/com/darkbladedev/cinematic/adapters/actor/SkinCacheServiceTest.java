package com.darkbladedev.cinematic.adapters.actor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SkinCacheServiceTest {

    @Test
    @DisplayName("SkinCacheService caches in memory and recovers from disk")
    void cachesInMemoryAndPersistsToDisk() throws IOException {
        Path tempDir = Files.createTempDirectory("skin-cache-");
        SkinCacheService service1 = new SkinCacheService(tempDir);

        SkinCacheService.SkinData testData = new SkinCacheService.SkinData("testBase64Value", "testSignature");
        service1.cache("Steve", testData);

        Optional<SkinCacheService.SkinData> inMem = service1.getCached("steve");
        assertThat(inMem).isPresent();
        assertThat(inMem.get().value()).isEqualTo("testBase64Value");
        assertThat(inMem.get().signature()).isEqualTo("testSignature");

        // Fresh instance reading from same disk directory
        SkinCacheService service2 = new SkinCacheService(tempDir);
        Optional<SkinCacheService.SkinData> fromDisk = service2.getCached("STEVE");
        assertThat(fromDisk).isPresent();
        assertThat(fromDisk.get().value()).isEqualTo("testBase64Value");
        assertThat(fromDisk.get().signature()).isEqualTo("testSignature");

        // Miss
        assertThat(service2.getCached("unknown_player")).isEmpty();
    }
}
