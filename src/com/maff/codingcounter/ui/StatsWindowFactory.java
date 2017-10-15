package com.maff.codingcounter.ui;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import com.maff.codingcounter.AppComponent;
import com.maff.codingcounter.data.CodingStats;
import com.maff.codingcounter.data.Period;
import com.maff.codingcounter.data.PeriodStats;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StatsWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory
{
    private final static int UI_UPDATE_PERIOD = 5; // Sec

    private ToolWindow window;
    private JComponent windowContent;

    private JButton clearStatsButton;

    private ScheduledFuture updateTask;

    private Map<Period, DefaultTableModel> tables = new HashMap<>();

    private Callback callback;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow)
    {
        this.window = toolWindow;

        createUi();

        // Create content
        SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(true);
        toolWindowPanel.setContent(windowContent);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowPanel, "", false);
        window.getContentManager().addContent(content);

        callback = ApplicationManager.getApplication().getComponent(AppComponent.class);

        clearStatsButton.addActionListener((action) -> {
            callback.onStatsResetClicked();
            updateData();
        });

        // Schedule periodic update task
        updateTask = JobScheduler.getScheduler().scheduleWithFixedDelay(
                () -> {
                    if(window.isVisible()) {
                        updateData();
                    }
                },
                0,
                UI_UPDATE_PERIOD,
                TimeUnit.SECONDS);
    }

    private void createUi()
    {
        JComponent contentWrapper = new JPanel();
        contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.PAGE_AXIS));

        windowContent = new JBScrollPane(
                contentWrapper,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        contentWrapper.add(Box.createRigidArea(new Dimension(1, 8)));

        JLabel waringText = new JLabel(UiStrings.WARNING_UI_PERIOD);
        waringText.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentWrapper.add(waringText);

        Font captionFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

        for(Period period : Period.values())
        {
            contentWrapper.add(Box.createRigidArea(new Dimension(1, 12)));

            JLabel periodNameLabel = new JLabel(UiStrings.PERIOD_LABELS.get(period));
            periodNameLabel.setFont(captionFont);
            periodNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentWrapper.add(periodNameLabel);

            contentWrapper.add(Box.createRigidArea(new Dimension(1, 12)));

            DefaultTableModel tableModel = new DefaultTableModel() {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            tableModel.addColumn(UiStrings.COLUMN_LABEL);
            tableModel.addColumn(UiStrings.COLUMN_VALUE);

            JBTable table = new JBTable(tableModel);
            table.setStriped(true);
            table.setFocusable(false);
            table.setRowSelectionAllowed(false);

            tables.put(period, tableModel);

            contentWrapper.add(table.getTableHeader());
            contentWrapper.add(table);
        }

        clearStatsButton = new JButton("Reset statistics");
        clearStatsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentWrapper.add(Box.createRigidArea(new Dimension(1, 12)));
        contentWrapper.add(clearStatsButton);
        contentWrapper.add(Box.createRigidArea(new Dimension(1, 8)));
    }

    private void updateData()
    {
        //Obtain fresh data
        CodingStats stats = callback.getStats();

        for (Map.Entry<Period, PeriodStats> entry : stats.periods.entrySet())
        {
            // Get table
            DefaultTableModel tableModel = tables.get(entry.getKey());
            if(tableModel == null) {
                continue;
            }

            // Ensure size
            if(tableModel.getRowCount() != 7) {
                tableModel.setRowCount(7);
                tableModel.fireTableStructureChanged();

                tableModel.setValueAt(UiStrings.LABEL_STAT_TYPE, 0, 0);
                tableModel.setValueAt(UiStrings.LABEL_STAT_BACK_DEL, 1, 0);
                tableModel.setValueAt(UiStrings.LABEL_STAT_BACK_IMMEDIATE, 2, 0);
                tableModel.setValueAt(UiStrings.LABEL_STAT_COPY_CUT, 3,0);
                tableModel.setValueAt(UiStrings.LABEL_STAT_PASTE, 4, 0);
                tableModel.setValueAt(UiStrings.LABEL_STAT_REMOVE, 5, 0);
                tableModel.setValueAt(UiStrings.LABEL_STAT_INSERTED, 6, 0);
            }

            // Fill with data
            PeriodStats periodStats = entry.getValue();
            tableModel.setValueAt(prettifyLong(periodStats.type), 0, 1);
            tableModel.setValueAt(prettifyLong(periodStats.backDel), 1, 1);
            tableModel.setValueAt(prettifyLong(periodStats.backImmediate), 2, 1);
            tableModel.setValueAt(prettifyLong(periodStats.copyCut), 3, 1);
            tableModel.setValueAt(prettifyLong(periodStats.paste), 4, 1);
            tableModel.setValueAt(prettifyLong(periodStats.remove), 5, 1);
            tableModel.setValueAt(prettifyLong(periodStats.insert), 6, 1);
        }
    }

    private String prettifyLong(long val)
    {
        if(val < 1000) {
            return String.valueOf(val);
        }
        else if(val < 1000000) {
            return String.format("%.2fK", val / 1000.0);
        }
        else {
            return String.format("%.2fM", val / 1000000.0);
        }
    }

    public interface Callback
    {
        CodingStats getStats();
        void onStatsResetClicked();
    }
}
