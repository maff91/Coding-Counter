package com.maff.codingcounter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory
{
    private ToolWindow window;
    private Tree treeDrawer;
    private JPanel windowContent;
    private JButton clearStatsButton;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow)
    {
        this.window = toolWindow;

        // Create content
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(windowContent, "", false);
        window.getContentManager().addContent(content);

        // Setup button
        clearStatsButton.setBorder(BorderFactory.createEmptyBorder(24, 36, 8, 36));
    }

    @Override
    public void init(ToolWindow window)
    {
        // Do nothing
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project)
    {
        // Suitable for all projects
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart()
    {
        return false;
    }

    private void createUIComponents()
    {
        // Setup the tree view
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("CodingStats");

        DefaultMutableTreeNode todayNode = new DefaultMutableTreeNode("Today");
        DefaultMutableTreeNode weekNode = new DefaultMutableTreeNode("Week");
        DefaultMutableTreeNode monthNode = new DefaultMutableTreeNode("Month");
        DefaultMutableTreeNode allTimesNode = new DefaultMutableTreeNode("AllTimes");

        rootNode.add(todayNode);
        rootNode.add(weekNode);
        rootNode.add(monthNode);
        rootNode.add(allTimesNode);

        DefaultMutableTreeNode[] subnodes = new DefaultMutableTreeNode[] {
                new DefaultMutableTreeNode("Symbols typed:"),
                new DefaultMutableTreeNode("Backspace/Del:"),
                new DefaultMutableTreeNode("Immediate backspaces:"),
                new DefaultMutableTreeNode("Copy/Cut symbols:"),
                new DefaultMutableTreeNode("Paste symbols:"),
                new DefaultMutableTreeNode("Total removed:"),
                new DefaultMutableTreeNode("Total inserted:")
        };

        for (DefaultMutableTreeNode node : subnodes)
        {
            todayNode.add(node);
        }

        treeDrawer = new Tree(rootNode);
    }
}
