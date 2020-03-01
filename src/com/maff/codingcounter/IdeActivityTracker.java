package com.maff.codingcounter;

import com.intellij.concurrency.JobScheduler;
import com.intellij.ide.util.projectWizard.actions.ProjectSpecificAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.impl.ProjectWindowAction;
import com.intellij.platform.AttachProjectAction;
import com.maff.codingcounter.data.CodingStats;
import com.maff.codingcounter.data.StatsRepository;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IdeActivityTracker implements Disposable
{
    private static final int STATS_SAVE_PERIOD = 300; // Sec
    private Logger log = Logger.getInstance(IdeActivityTracker.class);

    private StatsRepository repository;
    private StatsCounter statsCounter;

    private ScheduledFuture<?> saveFuture;

    public IdeActivityTracker(StatsRepository repository, Disposable parentDisposable)
    {
        this.repository = repository;

        //Load previously saved stats
        CodingStats stats;
        try {
            stats = repository.load();
        }
        catch (Exception e) {
            stats = new CodingStats();

            notifyError(e);
            log.error(e);
        }

        statsCounter = new StatsCounter(stats);

        startActionListener(parentDisposable);
        startPeriodicSave();

        Disposer.register(parentDisposable, this);
    }

    private void startPeriodicSave()
    {
        saveFuture = JobScheduler.getScheduler().scheduleWithFixedDelay(
                this::saveStats,
                STATS_SAVE_PERIOD,
                STATS_SAVE_PERIOD,
                TimeUnit.SECONDS);
    }

    private void startActionListener(Disposable parentDisposable)
    {

//        ProjectManager.getInstance().addpr(s, new ProjectManagerListener() {
//            @Override
//            public void projectOpened(Project project) {
//
//            }
//        });

        ActionManager.getInstance().addAnActionListener(new AnActionListener() {
                    @Override
                    public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                        statsCounter.onAction(action, dataContext, event);
                    }

                    @Override
                    public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                        if(action instanceof AttachProjectAction){
                            notifyError(new Throwable("AttachProjectAction"));
                        }
                        if(action instanceof ProjectSpecificAction){
                            notifyError(new Throwable("ProjectSpecificAction"));
                        }
                        if(action instanceof ProjectWindowAction){
                            notifyError(new Throwable("ProjectWindowAction"));
                        }
                    }

                    @Override
                    public void beforeEditorTyping(char c, DataContext dataContext) {
                        statsCounter.onType(c, dataContext);
                    }
                },
                parentDisposable);
    }

    private void saveStats() {
        try {
            repository.save(statsCounter.getStats());
        }
        catch (Exception e) {
            notifyError(e);
            log.error(e);
        }
    }

    private void notifyError(Throwable error)
    {
        ApplicationManager.getApplication().invokeLater(() -> {
            String messageString = error.getMessage();
            String title = "Coding Counter";
            String groupDisplayId = "Coding Counter";

            Notification notification = new Notification(groupDisplayId, title, messageString, NotificationType.ERROR);
            ApplicationManager.getApplication().getMessageBus().syncPublisher(Notifications.TOPIC).notify(notification);
        });
    }

    @Override
    public void dispose()
    {
        saveFuture.cancel(false);
        saveStats();
    }

    public CodingStats getStats()
    {
        return statsCounter.getStats();
    }

    public void resetStats()
    {
        statsCounter = new StatsCounter(new CodingStats());
    }
}
