package com.maff.codingcounter.data;

import java.util.HashMap;
import java.util.Map;

public class CodingStats
{
    public long lastEventTime;
    public Map<Period, PeriodStats> periods;

    public CodingStats()
    {
        periods = new HashMap<>();
    }
}
