package com.freedom.health4j.api.impl.checkStyle;

import com.freedom.health4j.Constants;
import com.freedom.health4j.api.Tool;
import com.freedom.health4j.api.impl.common.AbstractHealthChecker;
import com.freedom.health4j.model.ReportItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * a health checker named `checkstyle`.
 */
@Tool(name = "checkstyle")
public class CheckStyleHealthChecker extends AbstractHealthChecker {

    private static final Log logger = LogFactory.getLog(CheckStyleHealthChecker.class);

    public CheckStyleHealthChecker(String toolName, Map<String, String> commonConfig) {
        super(toolName, commonConfig);
    }

    @Override
    public Collection<ReportItem> call() throws Exception {
        verify();
        invoke();

        if (enableMerge) {
            return extract();
        }

        return Collections.emptySet();
    }

    @Override
    public void invoke() {
        String outputFormat = this.exclusiveConfig.getProperty(this.toolName + ".reporttype");
        String confBasePathStr = this.commonConfig.get(Constants.COMMON_CONF_BASE_PATH_KEY);
        String ruleFileName = this.exclusiveConfig.getProperty(this.toolName + ".rulesets");
        String ruleFilePath = Paths.get(confBasePathStr, ruleFileName).toAbsolutePath().toString();
        String fightArea = this.commonConfig.get(Constants.COMMON_FIGHT_AREA);

        String cmd = String.format(cmdPattern, outputFormat, ruleFilePath, fightArea, reportPath);
        logger.info("the command is : " + cmd);

        try {
            this.execCmd(cmd);
        } catch (IOException e) {
            logger.error("when invoking a command for tool [checkstyle], it occurs a Exception : " + e.toString());
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public void verify() {
        super.verify();
    }

    @Override
    public Collection<ReportItem> extract() throws IOException {
        Document doc = getReportDocument();

        List<Node> fileNodes = doc.selectNodes("//checkstyle/file");

        Collection<ReportItem> reportItems = new ArrayList<>(fileNodes.size());

        for (Node file : fileNodes) {
            Element fileElement = (Element) file;

            //if should be ignore then just contiue
            if (shouldBeIgnored(fileElement.attributeValue("name"))) {
                continue;
            }

            List<Node> errorNodes = file.selectNodes("error");
            for (Node error : errorNodes) {
                ReportItem reportItem = new ReportItem();
                Element e = (Element) error;
                reportItem.setToolName(this.toolName);
                reportItem.setMessage(e.attributeValue("message"));
                int line = Integer.valueOf(e.attributeValue("line"));
                reportItem.setBeginLine(line);
                reportItem.setEndLine(line);
                reportItem.setBeginColumn(Integer.MAX_VALUE);
                reportItem.setEndColumn(Integer.MAX_VALUE);
                reportItem.setPriority(e.attributeValue("severity"));
                String relativePath = fileElement.attributeValue("name").replace(this.projectPath, "");
                reportItem.setFilePath(relativePath);

                logger.debug("report item info : " + reportItem);
                reportItems.add(reportItem);
            }
        }

        return reportItems;
    }
}
