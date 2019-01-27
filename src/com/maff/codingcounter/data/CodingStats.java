package com.maff.codingcounter.data;

import java.util.HashMap;
import java.util.Map;

public class CodingStats
{
    public int version = 1;
    public long lastEventTime;
    public Map<Period, PeriodStats> periods;

    public CodingStats()
    {
        periods = new HashMap<>();
    }

    public CodingStats(CodingStats other)
    {
        this();

        version = other.version;
        lastEventTime = other.lastEventTime;

        for (Map.Entry<Period, PeriodStats> entry : other.periods.entrySet()) {
            periods.put(entry.getKey(), new PeriodStats(entry.getValue()));
        }
    }
}
