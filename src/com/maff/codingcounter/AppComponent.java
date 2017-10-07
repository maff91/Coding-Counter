package com.maff.codingcounter;

;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.maff.codingcounter.data.JsonStatsRepository;
import com.maff.codingcounter.data.StatsRepository;
import org.jetbrains.annotations.NotNull;

public class AppComponent implements ApplicationComponent
{
    private StatsRepository repository;
    private IdeActivityTracker tracker;

    @Override
    public void initComponent() {
        String statsPath = PathManager.getPluginsPath() + "/coding-counter/stats.json";
        repository = new JsonStatsRepository(statsPath);
        tracker = new IdeActivityTracker(repository, ApplicationManager.getApplication());
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Coding Counter";
    }
}

