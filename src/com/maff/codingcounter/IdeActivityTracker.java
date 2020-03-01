package com.maff.codingcounter;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.maff.codingcounter.data.CodingStats;

public class IdeActivityTracker
{
    private StatsCounter statsCounter;
    private Callback callback;

    public IdeActivityTracker(CodingStats stats)
    {
        statsCounter = new StatsCounter(stats);
        startActionListener();
    }

    private void startActionListener()
    {
        ActionManager.getInstance().addAnActionListener(new AnActionListener() {
                                                            @Override
                                                            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                                                                statsCounter.onAction(action, dataContext, event);
                                                                notifyChange();
                                                            }

                                                            @Override
                                                            public void beforeEditorTyping(char c, DataContext dataContext) {
                                                                statsCounter.onType(c, dataContext);
                                                                notifyChange();
                                                            }
                                                        },
                () -> {
                    // IGNORE
                });
    }

    private void notifyChange() {
        if(callback != null) {
            callback.onStatsChanged(statsCounter.getStats());
        }
    }

    public CodingStats getStats()
    {
        return statsCounter.getStats();
    }

    public void resetStats()
    {
        statsCounter = new StatsCounter(new CodingStats());
        notifyChange();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onStatsChanged(CodingStats stats);
    }
}
