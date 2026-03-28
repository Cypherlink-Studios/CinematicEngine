package com.darkbladedev.cinematic.testing.determinism;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.testing.support.NumericTolerance;
import com.darkbladedev.cinematic.testing.support.TestTimelineRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class StabilityLoadTest {
    @Test
    void highTickLoadMaintainsPrecisionAcrossMultipleTracks() {
        List<AccumulatingTrack> tracks = List.of(
                new AccumulatingTrack("track-1"),
                new AccumulatingTrack("track-2"),
                new AccumulatingTrack("track-3"),
                new AccumulatingTrack("track-4"),
                new AccumulatingTrack("track-5")
        );
        Scene scene = new Scene("stability-1000", 1000L, new ArrayList<>(tracks));
        TestTimelineRunner runner = new TestTimelineRunner();

        runner.play(scene, (currentScene, localTick) -> TestTimelineRunner.emptyContext(localTick));
        runner.advanceTicks(1001L);
        runner.stop();

        double expectedSum = (1000.0D * 1001.0D) / 2.0D;
        for (AccumulatingTrack track : tracks) {
            assertThat(track.evaluatedTicks()).isEqualTo(1000);
            assertThat(track.sumOfTicks()).isCloseTo(expectedSum, offset(NumericTolerance.DOUBLE_EPSILON));
        }
    }

    private static final class AccumulatingTrack implements Track {
        private final String id;
        private int evaluatedTicks;
        private double sumOfTicks;

        private AccumulatingTrack(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public long startTick() {
            return 1L;
        }

        @Override
        public long endTick() {
            return 1000L;
        }

        @Override
        public void evaluate(long tick, TimelineContext context) {
            evaluatedTicks++;
            sumOfTicks += tick;
        }

        private int evaluatedTicks() {
            return evaluatedTicks;
        }

        private double sumOfTicks() {
            return sumOfTicks;
        }
    }
}
