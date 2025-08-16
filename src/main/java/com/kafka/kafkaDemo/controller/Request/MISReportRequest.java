package com.kafka.kafkaDemo.controller.Request;

public class MISReportRequest {

    private Long id;
    private String reportName;
    private String source;
    private String status;
    private String url;

    // Default constructor
    public MISReportRequest() {
    }

    // All-args constructor
    public MISReportRequest(Long id, String reportName, String source, String status, String url) {
        this.id = id;
        this.reportName = reportName;
        this.source = source;
        this.status = status;
        this.url = url;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // toString
    @Override
    public String toString() {
        return "MISReportRequest{" +
                "id=" + id +
                ", reportName='" + reportName + '\'' +
                ", source='" + source + '\'' +
                ", status='" + status + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
