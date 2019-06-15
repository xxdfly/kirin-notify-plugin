package com.kirin.plugins.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kirin.plugins.notify.bean.ProjectAppStatusIn;
import com.kirin.plugins.notify.utils.HttpClientUtils;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

/**
 * Created by xiaodong.xuexd on 19/2/26.
 */
public class KirinServiceImpl implements KirinService {

    private String jenkinsURL;

    private String callbackUrl;

    private Long projectAppId;

    private TaskListener listener;

    private AbstractBuild build;

    private String aToken;


    public KirinServiceImpl(String jenkinsURL, String callbackUrl, Long projectAppId, String aToken, TaskListener listener, AbstractBuild build) {
        this.jenkinsURL = jenkinsURL;
        this.listener = listener;
        this.build = build;
        this.callbackUrl = callbackUrl;
        this.projectAppId = projectAppId;
        this.aToken = aToken;
    }

    @Override
    public void start() {
        String link = getBuildUrl();
        sendLinkMessage(link, "COMPILING");
    }

    private String getBuildUrl() {
        if (jenkinsURL.endsWith("/")) {
            return jenkinsURL + build.getUrl();
        } else {
            return jenkinsURL + "/" + build.getUrl();
        }
    }

    @Override
    public void success() {
        String link = getBuildUrl();
        sendLinkMessage(link, "COMPILE_SUCCESS");
    }

    @Override
    public void failed() {
        String link = getBuildUrl();
        sendLinkMessage(link, "COMPILE_FAIL");
    }

    @Override
    public void abort() {
        String link = getBuildUrl();
        sendLinkMessage(link, "COMPILE_FAIL");
    }


    private void sendLinkMessage(String link, String status) {

        try {
            ProjectAppStatusIn appStatusIn = new ProjectAppStatusIn();
            appStatusIn.setProjectAppId(projectAppId);
            appStatusIn.setStatus(status);
            String json = JSON.toJSONString(appStatusIn);
            String result = HttpClientUtils.postJsonRequestWithToken(callbackUrl, JSONObject.parseObject(json), aToken);
            System.out.println(result);
        } catch (Exception e) {
            listener.getLogger().println("error happen when wait for deploy project, " + e.getMessage());
            return;
        }
    }

}
