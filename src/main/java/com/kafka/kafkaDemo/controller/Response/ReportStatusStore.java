package com.kafka.kafkaDemo.controller.Response;

import java.util.concurrent.ConcurrentHashMap;

public class ReportStatusStore {

    private static final ConcurrentHashMap<String, ReportStatusMessage> statusMap = new ConcurrentHashMap<>();

    public static void updateStatus(String reportId, ReportStatusMessage status) {
        statusMap.put(reportId, status);
    }

    public static ReportStatusMessage getStatus(String reportId) {
        return statusMap.get(reportId);
    }
}
