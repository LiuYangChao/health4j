package com.freedom.health4j.model;

/**
 * the model of every bug item.
 * Created by yanghua on 1/22/15.
 */
public class ReportItem extends BaseModel {

    private int    beginLine;
    private int    endLine;
    private int    beginColumn;
    private int    endColumn;
    private String priority;
    private String toolName;
    private String rule;
    private String packageName;
    private String filePath;
    private String externalInfoUrl;
    private String message;

    public ReportItem() {
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getBeginLine() {
        return beginLine;
    }

    public void setBeginLine(int beginLine) {
        this.beginLine = beginLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public void setBeginColumn(int beginColumn) {
        this.beginColumn = beginColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getExternalInfoUrl() {
        return externalInfoUrl;
    }

    public void setExternalInfoUrl(String externalInfoUrl) {
        this.externalInfoUrl = externalInfoUrl;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "ReportItem{" +
            "beginLine=" + beginLine +
            ", endLine=" + endLine +
            ", beginColumn=" + beginColumn +
            ", endColumn=" + endColumn +
            ", priority='" + priority + '\'' +
            ", toolName='" + toolName + '\'' +
            ", rule='" + rule + '\'' +
            ", packageName='" + packageName + '\'' +
            ", filePath='" + filePath + '\'' +
            ", externalInfoUrl='" + externalInfoUrl + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
