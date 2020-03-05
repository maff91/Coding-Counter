package com.maff.codingcounter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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
    private final Object statsMutex = new Object();

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
            stats.periods.put(Period.MONTH, new PeriodStats());
            forceUpdate = true;
        }

        if(forceUpdate || newEventTime.get(Calendar.WEEK_OF_YEAR) != lastEventTime.get(Calendar.WEEK_OF_YEAR)) {
            stats.periods.put(Period.WEEK, new PeriodStats());
            forceUpdate = true;
        }

        if(forceUpdate || newEventTime.get(Calendar.DAY_OF_YEAR) != lastEventTime.get(Calendar.DAY_OF_YEAR)) {
            stats.periods.put(Period.TODAY, new PeriodStats());
        }

        lastEventTime = newEventTime;
        stats.lastEventTime = lastEventTime.getTimeInMillis();
    }

    public boolean onType(char c, DataContext dataContext)
    {
        ensureTimePeriods();

        Editor editor = TextComponentEditorAction.getEditorFromContext(dataContext);
        int caretCount = editor.getCaretModel().getCaretCount();

        for (PeriodStats period : stats.periods.values()) {

            period.type++;
            period.insert += caretCount;
        }

        lastTypeTime = System.currentTimeMillis();
        return true;
    }

    public boolean onAction(AnAction action, DataContext dataContext, AnActionEvent event)
    {
        if (action == null) {
            return false;
        }

        Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
        if(editor == null) {
            //The event is related to another IDE input rather than editor
            return false;
        }

        String selectedText = editor.getSelectionModel().getSelectedText(true);
        int selectedCount = selectedText != null ? selectedText.length() : 0;

        boolean handled = false;

        if(action instanceof BackspaceAction || action instanceof DeleteAction) {
            ensureTimePeriods();

            int caretCount = editor.getCaretModel().getCaretCount();

            boolean isImmediate = (System.currentTimeMillis() - lastTypeTime) < IMMEDIATE_BACKSPACE_THRESHOLD;

            synchronized (statsMutex) {
                for (PeriodStats period : stats.periods.values()) {
                    period.backDel += 1;
                    period.remove += selectedCount > 0 ? selectedCount : caretCount;

                    if (isImmediate) {
                        period.backImmediate += 1;
                    }
                }
                handled = true;
            }
        }
        else if(action instanceof CutAction)
        {
            ensureTimePeriods();

            synchronized (statsMutex) {
                for (PeriodStats period : stats.periods.values()) {
                    period.cut += selectedCount;
                    period.remove += selectedCount;
                }
            }
            handled = true;
        }
        else if(action instanceof PasteAction)
        {
            ensureTimePeriods();

            int pasteCount = 0;

            CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
            if(copyPasteManager.areDataFlavorsAvailable(DataFlavor.stringFlavor)) {
                pasteCount = ((String)copyPasteManager.getContents(DataFlavor.stringFlavor)).length();
            }

            synchronized (statsMutex) {
                for (PeriodStats period : stats.periods.values()) {
                    period.remove += selectedCount;
                    period.paste += pasteCount;
                }
            }
            handled = true;
        }
        return handled;
    }

    /**
     * Thread safe, returns a copy!
     * @return Copy instance of the latest stats
     */
    public CodingStats getStats()
    {
        synchronized (statsMutex) {
            return new CodingStats(stats);
        }
    }
}
