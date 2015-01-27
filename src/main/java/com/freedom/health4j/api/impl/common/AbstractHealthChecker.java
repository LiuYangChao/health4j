package com.freedom.health4j.api.impl.common;

import com.freedom.health4j.Constants;
import com.freedom.health4j.api.HealthChecker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * a abstract checker which support a basic implementation of a checker.
 * Created by yanghua on 1/21/15.
 */
public abstract class AbstractHealthChecker implements HealthChecker {

    private static final Log logger = LogFactory.getLog(AbstractHealthChecker.class);

    protected String              toolName;
    protected String              projectPath;
    protected Map<String, String> commonConfig;
    protected Properties          exclusiveConfig;
    protected boolean             enableMerge;
    protected String[]            ignoredFileTypes;
    protected String[]            ignoredFilePaths;
    protected String[]            ignoredFileSegments;
    protected String              reportPath;
    protected String              cmdPattern;

    public AbstractHealthChecker(String toolName, Map<String, String> commonConfig) {
        this.commonConfig = commonConfig;
        this.toolName = toolName;
        this.projectPath = this.commonConfig.get(Constants.COMMON_FIGHT_AREA);

        String confPathStr = this.commonConfig.get(Constants.COMMON_CONF_BASE_PATH_KEY);
        String toolConfigName = this.commonConfig.get(this.toolName + "." + Constants.EXCLUSIVE_CONFIG_KEY);
        Path toolConfigPath = Paths.get(confPathStr, toolConfigName);

        this.exclusiveConfig = new Properties();

        InputStream is = null;
        try {
            is = new FileInputStream(toolConfigPath.toAbsolutePath().toString());
            this.exclusiveConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
    }

    @Override
    public void verify() {
        enableMerge = Boolean.valueOf(this.commonConfig.get(Constants.COMMON_ENABLE_MERGE_KEY));

        String joinedIgnoredFileType = this.commonConfig.get(Constants.COMMON_IGNORED_FILE_TYPE_KEY);
        if (joinedIgnoredFileType != null && joinedIgnoredFileType.length() > 0) {
            ignoredFileTypes = joinedIgnoredFileType.split(",");
        } else {
            ignoredFileTypes = new String[0];
        }

        String joinedIgnoredFilePath = this.commonConfig.get(Constants.COMMON_IGNORED_FILE_PATH_KEY);
        if (joinedIgnoredFilePath != null && joinedIgnoredFilePath.length() > 0) {
            ignoredFilePaths = joinedIgnoredFilePath.split(",");
        } else {
            ignoredFilePaths = new String[0];
        }

        String joinedIgnoredFileSegment = this.commonConfig.get(Constants.COMMON_IGNORED_FILE_SEGMENT_KEY);
        if (joinedIgnoredFileSegment != null && joinedIgnoredFileSegment.length() > 0) {
            ignoredFileSegments = joinedIgnoredFileSegment.split(",");
        } else {
            ignoredFileSegments = new String[0];
        }

        String reportBasePath = commonConfig.get(Constants.COMMON_REPORT_BASE_PATH_KEY);

        if (!this.exclusiveConfig.containsKey(this.toolName + ".reportfile")) {
            throw new RuntimeException("verify environment error : missed key : " + this.toolName + ".reportfile");
        }
        String outputFileName = this.exclusiveConfig.get(this.toolName + ".reportfile").toString();
        reportPath = Paths.get(reportBasePath, outputFileName).toAbsolutePath().toString();

        if (!this.exclusiveConfig.containsKey(this.toolName + ".execpattern")) {
            throw new RuntimeException("verify environment error : missed key : " + this.toolName + ".execpattern");
        }
        cmdPattern = this.exclusiveConfig.getProperty(this.toolName + ".execpattern");
    }

    protected void execCmd(String cmdStr) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process shellProcess = runtime.exec(cmdStr);
        try {
            shellProcess.waitFor();
            String errStr = translateFromStream(shellProcess.getErrorStream());
            String infoStr = translateFromStream(shellProcess.getInputStream());

            if (logger.isDebugEnabled()) {
                logger.debug(" error info : " + errStr);
                logger.debug(" info : " + infoStr);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected boolean shouldBeIgnored(String fullPathStr) {
        if (fullPathStr == null || fullPathStr.isEmpty()) {
            return true;
        }

        boolean tmp1 = false, tmp2 = false, tmp3 = false;
        for (int i = 0; i < ignoredFileTypes.length; i++) {
            if (fullPathStr.endsWith(ignoredFileTypes[i])) {
                tmp1 = true;
                break;
            }
        }

        for (int j = 0; j < ignoredFilePaths.length; j++) {
            if (fullPathStr.startsWith(ignoredFilePaths[j])) {
                tmp2 = true;
                break;
            }
        }

        for (int k = 0; k < ignoredFileSegments.length; k++) {
            if (fullPathStr.contains(ignoredFileSegments[k])) {
                tmp3 = true;
                break;
            }
        }

        return tmp1 || tmp2 || tmp3;
    }

    private static String translateFromStream(InputStream stream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);

        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        while (bufferedInputStream.read(buffer) != -1) {
            sb.append(new String(buffer, "utf-8"));
        }

        return sb.toString();
    }

    protected Document getReportDocument() throws IOException {
        SAXReader reader = new SAXReader();
        try {
            return reader.read(new File(reportPath));
        } catch (DocumentException e) {
            throw new IOException(e.toString());
        }
    }

}
