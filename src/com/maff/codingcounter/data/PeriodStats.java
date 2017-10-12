package com.maff.codingcounter.data;

public class PeriodStats
{
    public int type;
    public int backDel;
    public int backImmediate;
    public int copyCut;
    public int paste;
    public int remove;
    public int insert;

    public PeriodStats() {
    }

    public PeriodStats(PeriodStats other) {
        type = other.type;
        backDel = other.backDel;
        backImmediate = other.backImmediate;
        copyCut = other.copyCut;
        paste = other.paste;
        remove = other.remove;
        insert = other.insert;
    }
}
