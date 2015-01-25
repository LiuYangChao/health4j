package com.freedom.health4j.api.impl.findBugs;

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
 * a health checker named `findbugs`.
 * Created by yanghua on 1/23/15.
 */
@Tool(name = "findbugs")
public class FindBugsHealthChecker extends AbstractHealthChecker {

    private static final Log logger = LogFactory.getLog(FindBugsHealthChecker.class);

    public FindBugsHealthChecker(String toolName, Map<String, String> commonConfig) {
        super(toolName, commonConfig);
    }

    @Override
    public Collection<ReportItem> call() throws IOException {
        verify();
        invoke();

        if (enableMerge) {
            return extract();
        }

        return Collections.emptySet();
    }

    @Override
    public void invoke() {
        String cmd = String.format(cmdPattern, reportPath, this.projectPath);
        logger.info(" the command is : " + cmd);

        try {
            this.execCmd(cmd);
        } catch (IOException e) {
            logger.error("when invoking a command for tool [findbugs], it occurs a Exception : " + e.toString());
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

        List<Node> bugNodes = doc.selectNodes("//BugCollection/BugInstance");

        Collection<ReportItem> reportItems = new ArrayList<>(bugNodes.size());

        //every <file></file> node
        for (Node bug : bugNodes) {
            Element bugElement = (Element) bug;
            Node sourceLineNode = bug.selectSingleNode("Method/SourceLine");
            Element sourceLineElement = (Element) sourceLineNode;

            String message = bugElement.attributeValue("category") + "-" + bugElement.attributeValue("type");

            ReportItem reportItem = new ReportItem();
            reportItem.setToolName(this.toolName);
            reportItem.setMessage(message);
            reportItem.setBeginLine(Integer.valueOf(sourceLineElement.attributeValue("start")));
            reportItem.setEndLine(Integer.valueOf(sourceLineElement.attributeValue("end")));
            reportItem.setBeginColumn(Integer.MAX_VALUE);
            reportItem.setEndColumn(Integer.MAX_VALUE);
            reportItem.setRule(bugElement.attributeValue("type"));
            reportItem.setPackageName(sourceLineElement.attributeValue("classname"));
            reportItem.setExternalInfoUrl("");
            reportItem.setPriority(bugElement.attributeValue("priority"));
            reportItem.setFilePath(sourceLineElement.attributeValue("sourcepath"));

            reportItems.add(reportItem);
        }

        return reportItems;
    }
}
