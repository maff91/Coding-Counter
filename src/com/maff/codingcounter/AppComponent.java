package com.maff.codingcounter;

;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.maff.codingcounter.data.CodingStats;
import com.maff.codingcounter.data.JsonStatsRepository;
import com.maff.codingcounter.data.StatsRepository;
import com.maff.codingcounter.ui.StatsWindowFactory;
import org.jetbrains.annotations.NotNull;

public class AppComponent implements ApplicationComponent, StatsWindowFactory.Callback
{
    private StatsRepository repository;
    private IdeActivityTracker tracker;

    @Override
    public void initComponent() {
        String statsPath = PathManager.getPluginsPath() + "/coding-counter/stats.json";
        repository = new JsonStatsRepository(statsPath);
        tracker = new IdeActivityTracker(repository, ApplicationManager.getApplication());
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Coding Counter";
    }

    @Override
    public CodingStats getStats() {
        return tracker.getStats();
    }

    @Override
    public void onStatsResetClicked() {
        tracker.resetStats();
    }
}

