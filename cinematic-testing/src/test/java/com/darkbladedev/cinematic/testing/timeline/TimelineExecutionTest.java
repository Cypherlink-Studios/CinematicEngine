package com.darkbladedev.cinematic.testing.timeline;

import com.darkbladedev.cinematic.core.model.Scene;
import com.darkbladedev.cinematic.core.model.Track;
import com.darkbladedev.cinematic.core.runtime.TimelineContext;
import com.darkbladedev.cinematic.testing.support.TestTimelineRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimelineExecutionTest {
    @Test
    void tracksExecuteExactlyOnExpectedTicks() {
        RecordingTrack track = new RecordingTrack("ordered-track", 2L, 4L);
        Scene scene = new Scene("timeline-order", 6L, List.of(track));
        TestTimelineRunner runner = new TestTimelineRunner();

        runner.play(scene, (currentScene, localTick) -> TestTimelineRunner.emptyContext(localTick));
        runner.advanceTicks(7L);
        runner.stop();

        assertThat(track.evaluatedTicks()).containsExactly(2L, 3L, 4L);
    }

    @Test
    void sceneEvaluationIsReproducibleAcrossRuns() {
        List<Long> firstRun = executeTicks(new RecordingTrack("repro-track-a", 1L, 5L));
        List<Long> secondRun = executeTicks(new RecordingTrack("repro-track-b", 1L, 5L));

        assertThat(firstRun).containsExactlyElementsOf(secondRun);
    }

    private List<Long> executeTicks(RecordingTrack track) {
        Scene scene = new Scene("timeline-repro", 6L, List.of(track));
        TestTimelineRunner runner = new TestTimelineRunner();
        runner.play(scene, (currentScene, localTick) -> TestTimelineRunner.emptyContext(localTick));
        runner.advanceTicks(7L);
        runner.stop();
        return track.evaluatedTicks();
    }

    private static final class RecordingTrack implements Track {
        private final String id;
        private final long startTick;
        private final long endTick;
        private final List<Long> evaluatedTicks;

        private RecordingTrack(String id, long startTick, long endTick) {
            this.id = id;
            this.startTick = startTick;
            this.endTick = endTick;
            this.evaluatedTicks = new ArrayList<>();
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public long startTick() {
            return startTick;
        }

        @Override
        public long endTick() {
            return endTick;
        }

        @Override
        public void evaluate(long tick, TimelineContext context) {
            evaluatedTicks.add(tick);
        }

        private List<Long> evaluatedTicks() {
            return List.copyOf(evaluatedTicks);
        }
    }
}
