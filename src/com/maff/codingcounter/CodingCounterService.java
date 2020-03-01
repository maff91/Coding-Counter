package com.maff.codingcounter;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.maff.codingcounter.data.CodingStats;
import com.maff.codingcounter.data.LegacyStatsRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CopyOnWriteArraySet;

@State(
        name = "CodingCounter",
        storages = {
                @Storage(
                        value = "codingCounter.xml",
                        roamingType = RoamingType.DISABLED
                )
        }
)
public class CodingCounterService implements PersistentStateComponent<CodingStats>, DumbAware, Disposable {

    public static CodingCounterService getInstance() {
        return ServiceManager.getService(CodingCounterService.class);
    }

    private StatsCounter statsCounter;
    private Callback callback;
    private MessageBusConnection messageBusConnection;

    public CodingCounterService() {
        messageBusConnection = ApplicationManager.getApplication().getMessageBus().connect();
        messageBusConnection.subscribe(AnActionListener.TOPIC, actionListener);
    }

    @Nullable
    @Override
    public CodingStats getState() {
        return getStats();
    }

    @Override
    public void loadState(@NotNull CodingStats codingStats) {
        statsCounter = new StatsCounter(new CodingStats(codingStats));
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

    public void resetStats() {
        statsCounter = new StatsCounter(new CodingStats());
        notifyChange();
    }

    public CodingStats getStats() {
        return statsCounter.getStats();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void notifyChange() {
        if (callback != null) {
            callback.onStatsChanged(statsCounter.getStats());
        }
    }

    private AnActionListener actionListener = new AnActionListener() {
        @Override
        public void beforeActionPerformed(@NotNull AnAction action, @NotNull DataContext dataContext, @NotNull AnActionEvent event) {
            if (statsCounter != null) {
                statsCounter.onAction(action, dataContext, event);
            }
            notifyChange();
        }

        @Override
        public void beforeEditorTyping(char c, @NotNull DataContext dataContext) {
            if (statsCounter != null) {
                statsCounter.onType(c, dataContext);
            }
            notifyChange();
        }
    };

    @Override
    public void dispose() {
        messageBusConnection.disconnect();
    }

    public interface Callback {
        void onStatsChanged(CodingStats stats);
    }
}
