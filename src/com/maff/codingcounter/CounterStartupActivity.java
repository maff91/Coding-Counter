package com.maff.codingcounter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class CounterStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        // Ensure the service is running.
        CodingCounterService.getInstance();
    }
}
