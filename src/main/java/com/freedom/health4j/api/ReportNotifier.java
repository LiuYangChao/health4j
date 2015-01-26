package com.freedom.health4j.api;

import java.util.Properties;

/**
 * report notifier interface.
 */
public interface ReportNotifier {

    /**
     * notify report
     */
    public void doNotify();

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
