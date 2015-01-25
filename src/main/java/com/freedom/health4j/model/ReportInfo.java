package com.freedom.health4j.model;

import java.util.Collection;
import java.util.Date;

/**
 * a report after running.
 * Created by yanghua on 1/22/15.
 */
public class ReportInfo extends BaseModel {

    private String projectName;
    private String basePath;
    private String generatedTool;
    private Date   generatedDate;
    private String version;
    private String analysisTools;

    private Collection<ReportItem> reportItems;

    public ReportInfo() {
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getGeneratedTool() {
        return generatedTool;
    }

    public void setGeneratedTool(String generatedTool) {
        this.generatedTool = generatedTool;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public Collection<ReportItem> getReportItems() {
        return reportItems;
    }

    public void setReportItems(Collection<ReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAnalysisTools() {
        return analysisTools;
    }

    public void setAnalysisTools(String analysisTools) {
        this.analysisTools = analysisTools;
    }
}
