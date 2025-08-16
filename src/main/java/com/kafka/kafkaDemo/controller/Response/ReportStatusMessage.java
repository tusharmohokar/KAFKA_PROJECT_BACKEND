package com.kafka.kafkaDemo.controller.Response;

    public class ReportStatusMessage {

        private String reportId;   // Unique ID of the report
        private String status;     // e.g. IN_PROGRESS, SUCCESS, FAILED_ATTEMPT, FAILED_FINAL
        private String details;    // More info (e.g. error message or report URL)
        private boolean generated; // true if final report is generated

        // Default constructor (needed for JSON serialization/deserialization)
        public ReportStatusMessage() {
        }

        // All-args constructor
        public ReportStatusMessage(String reportId, String status, String details, boolean generated) {
            this.reportId = reportId;
            this.status = status;
            this.details = details;
            this.generated = generated;
        }

        // Getters & Setters
        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public boolean isGenerated() {
            return generated;
        }

        public void setGenerated(boolean generated) {
            this.generated = generated;
        }

        // toString (useful for logs & debugging)
        @Override
        public String toString() {
            return "ReportStatusMessage{" +
                    "reportId='" + reportId + '\'' +
                    ", status='" + status + '\'' +
                    ", details='" + details + '\'' +
                    ", generated=" + generated +
                    '}';
        }
}
