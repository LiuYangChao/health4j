package com.freedom.health4j.api;


import com.freedom.health4j.model.ReportItem;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * do health checker with a tool.
 */
public interface HealthChecker extends Callable<Collection<ReportItem>>, CommandInvoker,
                                       EnvVerifier, ReportExtractor {

}
