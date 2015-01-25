package com.freedom.health4j.api.impl.pmd;


import com.freedom.health4j.api.Tool;
import com.freedom.health4j.api.impl.common.AbstractHealthChecker;
import com.freedom.health4j.model.ReportItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.IOException;
import java.util.*;

/**
 * a health checker named `pmd`.
 */
@Tool(name = "pmd")
public class PmdHealthChecker extends AbstractHealthChecker {

    private static final Log logger = LogFactory.getLog(PmdHealthChecker.class);

    public PmdHealthChecker(String toolName, Map<String, String> commonConfig) {
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
    public void verify() {
        super.verify();
    }

    @Override
    public void invoke() {
        String ruleSetsStr = this.exclusiveConfig.getProperty(this.toolName + ".rulesets");
        String reportType = this.exclusiveConfig.getProperty(this.toolName + ".reporttype");
        String cmd = String.format(cmdPattern, this.projectPath, reportType, reportPath, ruleSetsStr);
        logger.info(" the command is : " + cmd);

        try {
            this.execCmd(cmd);
        } catch (IOException e) {
            logger.error("when invoking a command for tool [pmd], it occurs a Exception : " + e.toString());
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public Collection<ReportItem> extract() throws IOException {
        Document doc = getReportDocument();

        List<Node> fileNodes = doc.selectNodes("//pmd/file");

        Collection<ReportItem> reportItems = new ArrayList<>(fileNodes.size());

        //every <file></file> node
        for (Node file : fileNodes) {
            Element fileElement = (Element) file;

            //if should be ignore then just contiue
            if (shouldBeIgnored(fileElement.attributeValue("name"))) {
                continue;
            }

            List<Node> violationNodes = file.selectNodes("violation");
            for (Node violation : violationNodes) {
                ReportItem reportItem = new ReportItem();
                Element e = (Element) violation;
                reportItem.setToolName(this.toolName);
                reportItem.setMessage(e.getText());
                reportItem.setBeginLine(Integer.valueOf(e.attributeValue("beginline")));
                reportItem.setEndLine(Integer.valueOf(e.attributeValue("endline")));
                reportItem.setBeginColumn(Integer.valueOf(e.attributeValue("begincolumn")));
                reportItem.setEndColumn(Integer.valueOf(e.attributeValue("endcolumn")));
                reportItem.setRule(e.attributeValue("rule"));
                reportItem.setPackageName(e.attributeValue("package"));
                reportItem.setExternalInfoUrl(e.attributeValue("externalInfoUrl"));
                reportItem.setPriority(e.attributeValue("priority"));
                String relativePath = fileElement.attributeValue("name").replace(this.projectPath, "");
                reportItem.setFilePath(relativePath);

                logger.debug("report item info : " + reportItem);
                reportItems.add(reportItem);
            }
        }

        return reportItems;
    }
}
