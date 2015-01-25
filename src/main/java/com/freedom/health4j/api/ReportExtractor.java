package com.freedom.health4j.api;

import com.freedom.health4j.model.ReportItem;

import java.io.IOException;
import java.util.Collection;

/**
 * report extractor.
 * Created by yanghua on 1/22/15.
 */
public interface ReportExtractor {

    /**
     * extract every bug item and collect them into a set
     *
     * @return the set of bug items
     * @throws IOException
     */
    public Collection<ReportItem> extract() throws IOException;

}
