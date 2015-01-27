package com.freedom.health4j;

import com.freedom.health4j.api.HealthChecker;
import com.freedom.health4j.api.ReportMerger;
import com.freedom.health4j.api.ReportNotifier;
import com.freedom.health4j.api.Tool;
import com.freedom.health4j.model.ReportInfo;
import com.freedom.health4j.model.ReportItem;
import com.freedom.health4j.util.ClazzUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * the main class.
 */
public class App {

    private static final Properties commonConfig = new Properties();
    private static final Log        logger       = LogFactory.getLog(App.class);
    private static List<String>                     toolNames;
    private static Map<String, Map<String, String>> exclusiveConfigs;

    public static void main(String[] args) {
        loadConfig(args);

        filterExclusiveConfig();

        List<HealthChecker> checkers = scan();

        //checkup
        ReportInfo reportInfo = checkup(checkers);

        //merge
        merge(reportInfo);

        //notify
        doNotify();
    }

    private static Properties loadConfig(String[] args) {
        if (args == null || args.length == 0) {
            throw new RuntimeException("load config file error : must specify the config file path");
        }

        String configFilePath = args[0];

        InputStream is = null;
        InputStreamReader reader = null;
        try {
            is = new FileInputStream(configFilePath);
            reader = new InputStreamReader(is, "UTF-8");
            commonConfig.load(reader);

            //config log4j
            String confBasePathStr = commonConfig.getProperty(Constants.COMMON_CONF_BASE_PATH_KEY);
            String logConfigFileName = commonConfig.getProperty(Constants.COMMON_LOG_CONFIG_FILE);
            String logFilePathStr = Paths.get(confBasePathStr, logConfigFileName).toAbsolutePath().toString();
            PropertyConfigurator.configure(logFilePathStr);
        } catch (IOException e) {
            logger.error("when loading `common.properties`, occurs a IOException : " + e.toString());
            throw new RuntimeException(e.toString());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }

                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.error("when closing stream , it occurs a exception : " + e.toString());
            }
        }

        return commonConfig;
    }

    private static List<HealthChecker> scan() {
        Set<Class> classes = ClazzUtil.traverse("com.freedom.health4j.api.impl", HealthChecker.class);

        List<HealthChecker> checkers = new ArrayList<>(classes.size());
        try {
            for (Class clazz : classes) {
                Annotation annotation = clazz.getAnnotation(Tool.class);

                if (annotation == null) {
                    continue;
                }

                Tool tool = (Tool) annotation;

                if (!toolNames.contains(tool.name())) {
                    continue;
                }

                Constructor<HealthChecker> constructor = clazz.getConstructor(String.class, Map.class);
                Map<String, String> exclusiveConfig = exclusiveConfigs.get(tool.name());
                HealthChecker service = constructor.newInstance(tool.name(), exclusiveConfig);

                checkers.add(service);
            }
        } catch (Exception e) {
            logger.error("When scaning the package `com.freedom.health4j.api.impl` occurs a Exception : "
                             + e.toString());
            throw new RuntimeException(e.toString());
        }

        return checkers;
    }

    private static Map<String, Map<String, String>> filterExclusiveConfig() {
        String toolNamesStr = commonConfig.get(Constants.COMMON_TOOLS_KEY).toString();
        String[] toolNameArr = toolNamesStr.split(",");
        toolNames = Arrays.asList(toolNameArr);

        exclusiveConfigs = new HashMap<String, Map<String, String>>(toolNameArr.length);

        for (String toolName : toolNameArr) {
            Map<String, String> config = new HashMap<String, String>();
            for (Object key : commonConfig.keySet()) {
                if (key.toString().startsWith("health4j") ||
                    key.toString().startsWith(toolName)) {
                    String val = commonConfig.getProperty(key.toString());
                    config.put(key.toString(), val);
                }
            }

            exclusiveConfigs.put(toolName, config);
        }

        return exclusiveConfigs;
    }

    private static ReportInfo checkup(List<HealthChecker> checkers) {
        ExecutorService executor = Executors.newFixedThreadPool(checkers.size());
        List<Future<Collection<ReportItem>>> futures = new ArrayList<>(checkers.size());
        for (HealthChecker checker : checkers) {
            futures.add(executor.submit(checker));
        }

        int timeoutOfSecond = Integer.parseInt(commonConfig.getProperty("health4j.timeoutpertool"));

        Collection<ReportItem> joinedReportItems = new ArrayList<>();
        //wait all finish
        try {
            for (Future<Collection<ReportItem>> future : futures) {
                Collection<ReportItem> tmp = future.get(timeoutOfSecond, TimeUnit.SECONDS);
                if (tmp == null) {
                    continue;
                }
                joinedReportItems.addAll(tmp);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.error("when it was checking , occurs an exception : " + e.toString());
        } finally {
            executor.shutdown();
        }

        ReportInfo reportInfo = new ReportInfo();
        reportInfo.setBasePath(commonConfig.getProperty(Constants.COMMON_FIGHT_AREA));
        reportInfo.setGeneratedTool("health4j");
        reportInfo.setGeneratedDate(new Date());
        reportInfo.setReportItems(joinedReportItems);
        reportInfo.setProjectName(commonConfig.getProperty(Constants.COMMON_PROJECT_KEY));
        reportInfo.setVersion(commonConfig.getProperty(Constants.COMMON_VERSION_KEY));
        reportInfo.setAnalysisTools(commonConfig.getProperty(Constants.COMMON_TOOLS_KEY));

        return reportInfo;
    }

    private static void merge(ReportInfo reportInfo) {
        if (Boolean.valueOf(commonConfig.getProperty(Constants.COMMON_ENABLE_MERGE_KEY))) {
            ServiceLoader<ReportMerger> serviceLoader = ServiceLoader.load(ReportMerger.class);
            Iterator<ReportMerger> mergerIterator = serviceLoader.iterator();

            if (!mergerIterator.hasNext()) {
                throw new RuntimeException("can not load service provider for service : ReportMerger");
            }

            ReportMerger merger = mergerIterator.next();
            merger.setCommonConfig(commonConfig);
            merger.merge(reportInfo);
        }
    }

    private static void doNotify() {
        if (Boolean.valueOf(commonConfig.getProperty(Constants.COMMON_ENABLE_NOTIFY_KEY))) {
            ServiceLoader<ReportNotifier> serviceLoader = ServiceLoader.load(ReportNotifier.class);
            Iterator<ReportNotifier> notifierIterator = serviceLoader.iterator();

            if (!notifierIterator.hasNext()) {
                throw new RuntimeException("can not load service provider for ReportNotifier");
            }

            ReportNotifier notifier = notifierIterator.next();
            notifier.setCommonConfig(commonConfig);
            notifier.doNotify();
        }
    }
}
