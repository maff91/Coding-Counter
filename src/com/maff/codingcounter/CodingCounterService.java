package com.maff.codingcounter;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.maff.codingcounter.data.CodingStats;
import com.maff.codingcounter.data.LegacyStatsRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "CodingCounter",
        storages = {
                @Storage(
                        value = "codingCounter.xml",
                        roamingType = RoamingType.DISABLED
                )
        }
)
public class CodingCounterService implements PersistentStateComponent<CodingStats> {
    public static CodingCounterService getInstance() {
        return ServiceManager.getService(CodingCounterService.class);
    }

    private IdeActivityTracker tracker;

    @Nullable
    @Override
    public CodingStats getState() {
        return getStats();
    }

    @Override
    public void loadState(@NotNull CodingStats codingStats) {
        tracker = new IdeActivityTracker(codingStats);
    }

    @Override
    public void noStateLoaded() {
        CodingStats codingStats;

        // Try to load legacy config
        try {
            String legacyPath = PathManager.getPluginsPath() + "/coding-counter/stats.json";
            codingStats = new LegacyStatsRepository(legacyPath).load();
        } catch (Exception ex) {
            codingStats = new CodingStats();
        }

        loadState(codingStats);
    }

    public CodingStats getStats() {
        return tracker.getStats();
    }

    public void resetStats() {
        tracker.resetStats();
    }
}
