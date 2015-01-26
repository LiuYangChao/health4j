package com.freedom.health4j.api;

import com.freedom.health4j.model.ReportInfo;

import java.util.Properties;

/**
 * tool's report merger.
 */
public interface ReportMerger {

    /**
     * merge multi report
     */
    public void merge(ReportInfo reportInfo);

    /**
     * getter of commonConfig
     *
     * @return the instance of Properties
     */
    public Properties getCommonConfig();

    /**
     * setter of commonConfig
     *
     * @param commonConfig the instance of Properties
     */
    public void setCommonConfig(Properties commonConfig);

}
