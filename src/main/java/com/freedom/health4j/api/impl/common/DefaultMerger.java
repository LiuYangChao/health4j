package com.freedom.health4j.api.impl.common;

import com.freedom.health4j.Constants;
import com.freedom.health4j.api.ReportMerger;
import com.freedom.health4j.model.ReportInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * default merger.
 */
public class DefaultMerger implements ReportMerger {

    private static final Log logger = LogFactory.getLog(DefaultMerger.class);

    private Properties commonConfig;
    private String     templateDirPathStr;
    private String     templateFileName;

    public DefaultMerger(Properties commonConfig) {
        this.commonConfig = commonConfig;
        this.templateDirPathStr = this.commonConfig.getProperty(Constants.COMMON_CONF_BASE_PATH_KEY);
        this.templateFileName = this.commonConfig.getProperty(Constants.COMMON_REPORT_TEMPLATE_KEY);
    }

    @Override
    public void merge(ReportInfo reportInfo) {
        VelocityEngine engine = getRenderEngine();
        Template reportTemplate = engine.getTemplate(templateFileName);
        VelocityContext ctx = new VelocityContext();

        ctx.put("reportInfo", reportInfo);

        String reportBasePathStr = commonConfig.getProperty(Constants.COMMON_REPORT_BASE_PATH_KEY);
        String reportPathStr = commonConfig.getProperty(Constants.COMMON_REPORT_PATH_KEY);
        Path reportPath = Paths.get(reportBasePathStr, reportPathStr);

        BufferedWriter writer = null;
        try {
            Files.deleteIfExists(reportPath);

            writer = Files.newBufferedWriter(reportPath,
                                             Charset.forName("UTF-8"),
                                             StandardOpenOption.CREATE_NEW);

            reportTemplate.merge(ctx, writer);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.error("[merge] occurs a IOException : " + e.toString());
                }
        }

    }

    private VelocityEngine getRenderEngine() {
        VelocityEngine engine = new VelocityEngine();

        engine.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        engine.setProperty("resource.loader", "file");
        engine.setProperty("file.resource.loader.class",
                           "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        engine.setProperty("file.resource.loader.path", templateDirPathStr);
        engine.setProperty("file.resource.loader.cache", "true");
        engine.setProperty("file.resource.loader.modificationCheckInterval", "30");

        engine.init();

        return engine;
    }
}
