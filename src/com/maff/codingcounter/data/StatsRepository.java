package com.maff.codingcounter.data;

import java.io.IOException;

public interface StatsRepository {
    CodingStats load() throws IOException;
    void save(CodingStats stats) throws IOException;
}
