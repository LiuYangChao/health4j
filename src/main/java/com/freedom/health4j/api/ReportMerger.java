package com.freedom.health4j.api;

import com.freedom.health4j.model.ReportInfo;

/**
 * tool's report merger.
 */
public interface ReportMerger {

    /**
     * merge multi report
     */
    public void merge(ReportInfo reportInfo);

}
