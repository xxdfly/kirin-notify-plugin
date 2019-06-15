package com.kirin.plugins.notify.bean;

public class ProjectAppStatusIn {
    private String status;
    private Long projectAppId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProjectAppId() {
        return projectAppId;
    }

    public void setProjectAppId(Long projectAppId) {
        this.projectAppId = projectAppId;
    }
}
