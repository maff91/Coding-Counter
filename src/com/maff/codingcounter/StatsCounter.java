package com.maff.codingcounter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actions.*;
import com.intellij.openapi.ide.CopyPasteManager;
import com.maff.codingcounter.data.CodingStats;
import com.maff.codingcounter.data.Period;
import com.maff.codingcounter.data.PeriodStats;

import java.awt.datatransfer.DataFlavor;
import java.util.Calendar;

public class StatsCounter {
    private static final int IMMEDIATE_BACKSPACE_THRESHOLD = 1000; // Ms

    private CodingStats stats;
    private Calendar lastEventTime;
    private long lastTypeTime;

    public StatsCounter(CodingStats stats)
    {
        this.stats = stats;

        preFillStats();

        lastEventTime = Calendar.getInstance();
        if(stats.lastEventTime > 0) {
            lastEventTime.setTimeInMillis(stats.lastEventTime);
        }
        else  {
            stats.lastEventTime = lastEventTime.getTimeInMillis();
        }

        ensureTimePeriods();
    }

    private void preFillStats()
    {
        for(Period period : Period.values()) {
            if(!stats.periods.containsKey(period)) {
                stats.periods.put(period, createEmptyPeriod());
            }
        }
    }

    private PeriodStats createEmptyPeriod()
    {
        //Reserved to implement more complicated logic in future if will need to
        return new PeriodStats();
    }

    /**
     * Resets time periods if we entered a new one
     */
    private void ensureTimePeriods()
    {
        Calendar newEventTime = Calendar.getInstance();

        boolean forceUpdate = false;

        if(newEventTime.get(Calendar.MONTH) != lastEventTime.get(Calendar.MONTH)) {
            stats.periods.put(Period.Month, new PeriodStats());
            forceUpdate = true;
        }

        if(forceUpdate || newEventTime.get(Calendar.WEEK_OF_YEAR) != lastEventTime.get(Calendar.WEEK_OF_YEAR)) {
            stats.periods.put(Period.Week, new PeriodStats());
            forceUpdate = true;
        }

        if(forceUpdate || newEventTime.get(Calendar.DAY_OF_YEAR) != lastEventTime.get(Calendar.DAY_OF_YEAR)) {
            stats.periods.put(Period.Today, new PeriodStats());
        }

        lastEventTime = newEventTime;
        stats.lastEventTime = lastEventTime.getTimeInMillis();
    }

    public void onType(char c, DataContext dataContext)
    {
        ensureTimePeriods();

        Editor editor = TextComponentEditorAction.getEditorFromContext(dataContext);
        int caretCount = editor.getCaretModel().getCaretCount();

        for (PeriodStats period : stats.periods.values()) {

            period.type++;
            period.insert += caretCount;
        }

        lastTypeTime = System.currentTimeMillis();
    }

    public void onAction(AnAction action, DataContext dataContext, AnActionEvent event)
    {
        if (action == null) {
            return;
        }

        if(action instanceof BackspaceAction || action instanceof DeleteAction) {
            ensureTimePeriods();

            Editor editor = TextComponentEditorAction.getEditorFromContext(dataContext);
            int selectedCount = editor.getSelectionModel().getSelectedText(true).length();
            int caretCount = editor.getCaretModel().getCaretCount();

            boolean isImmediate = (System.currentTimeMillis() - lastTypeTime) < IMMEDIATE_BACKSPACE_THRESHOLD;

            for (PeriodStats period : stats.periods.values()) {
                period.backDel += editor.getCaretModel().getCaretCount();
                period.remove += selectedCount;

                if(isImmediate) {
                    period.backImmediate += caretCount;
                }
            }
        }
        else if(action instanceof CopyAction || action instanceof CutAction)
        {
            ensureTimePeriods();

            Editor editor = TextComponentEditorAction.getEditorFromContext(dataContext);
            int selectedCount = editor.getSelectionModel().getSelectedText(true).length();

            for (PeriodStats period : stats.periods.values()) {
                period.copyCut += selectedCount;
            }
        }
        else if(action instanceof PasteAction)
        {
            ensureTimePeriods();

            Editor editor = TextComponentEditorAction.getEditorFromContext(dataContext);

            int selectedCount = editor.getSelectionModel().getSelectedText(true).length();
            int pasteCount = 0;

            CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
            if(copyPasteManager.areDataFlavorsAvailable(DataFlavor.stringFlavor)) {
                pasteCount = ((String)copyPasteManager.getContents(DataFlavor.stringFlavor)).length();
            }

            for (PeriodStats period : stats.periods.values()) {
                period.remove += selectedCount;
                period.paste += pasteCount;
            }
        }
    }

    public CodingStats getStats()
    {
        return stats;
    }
}
