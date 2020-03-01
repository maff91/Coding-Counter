package com.maff.codingcounter;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.diagnostic.Logger;
import com.maff.codingcounter.data.CodingStats;

public class IdeActivityTracker
{
    private Logger log = Logger.getInstance(IdeActivityTracker.class);

    private StatsCounter statsCounter;

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
                                                            }

                                                            @Override
                                                            public void beforeEditorTyping(char c, DataContext dataContext) {
                                                                statsCounter.onType(c, dataContext);
                                                            }
                                                        },
                () -> {
                    // IGNORE
                });
    }

//    private void notifyError(Throwable error)
//    {
//        ApplicationManager.getApplication().invokeLater(() -> {
//            String messageString = error.getMessage();
//            String title = "Coding Counter";
//            String groupDisplayId = "Coding Counter";
//
//            Notification notification = new Notification(groupDisplayId, title, messageString, NotificationType.ERROR);
//            ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
//        });
//    }

    public CodingStats getStats()
    {
        return statsCounter.getStats();
    }

    public void resetStats()
    {
        statsCounter = new StatsCounter(new CodingStats());
    }
}
