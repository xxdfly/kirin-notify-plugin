package com.kirin.plugins.notify;

import com.alibaba.fastjson.JSON;
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.kirin.plugins.notify.utils.CredentialsListBoxModel;
import com.kirin.plugins.notify.utils.HttpClientUtils;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.kirin.plugins.notify.utils.CredentialUtils.lookupSystemCredentials;
import static org.apache.commons.lang.StringUtils.isNotBlank;


public class KirinNotifier extends Notifier {

    @CheckForNull
    private String callbackUrl;

    @CheckForNull
    private String credentialsId;

    @CheckForNull
    private Long projectAppId;

    @CheckForNull
    private String jenkinsURL;


    @CheckForNull
    public String getCallbackUrl() {
        return callbackUrl;
    }

    @DataBoundSetter
    public void setCallbackUrl(@CheckForNull String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(@CheckForNull String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @CheckForNull
    public Long getProjectAppId() {
        return projectAppId;
    }

    @DataBoundSetter
    public void setProjectAppId(@CheckForNull Long projectAppId) {
        this.projectAppId = projectAppId;
    }

    @CheckForNull
    public String getJenkinsURL() {
        return jenkinsURL;
    }

    @DataBoundSetter
    public void setJenkinsURL(@CheckForNull String jenkinsURL) {
        this.jenkinsURL = jenkinsURL;
    }

    private String aToken;


    @DataBoundConstructor
    public KirinNotifier(String callbackUrl, String credentialsId, Long projectAppId, String jenkinsURL) {
        super();
        this.callbackUrl = callbackUrl;
        this.credentialsId = credentialsId;
        this.projectAppId = projectAppId;
        this.jenkinsURL = jenkinsURL;
    }

    public KirinService newKirinService(AbstractBuild build, TaskListener listener) {
        if (isNotBlank(credentialsId)) {
            Credentials credentials = lookupSystemCredentials(credentialsId);
            if (credentials instanceof UsernamePasswordCredentials) {
                final UsernamePasswordCredentials cred = (UsernamePasswordCredentials) credentials;
                HashMap<String, String> authParam = new HashMap<>();
                authParam.put("grant_type", "password");
                authParam.put("username", "devops_" + cred.getUsername());
                authParam.put("password", String.valueOf(cred.getPassword()));
                authParam.put("scope", "select");
                authParam.put("client_id", "client_1");
                authParam.put("client_secret", String.valueOf(cred.getPassword()));
                try {
                    URI uri = new URI(callbackUrl);
                    String authUrl = "http://" + uri.getHost() + ":" + uri.getPort() + "/oauth/token";
                    String result = HttpClientUtils.postRequest(authUrl, authParam);
                    aToken = JSON.parseObject(result).get("access_token").toString();
                } catch (URIException e) {
                    e.printStackTrace();
                }
            }
        }
        return new KirinServiceImpl(jenkinsURL, callbackUrl, projectAppId, aToken, listener, build);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }


    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public KirinNotifierDescriptor getDescriptor() {
        return (KirinNotifierDescriptor) super.getDescriptor();
    }

    @Extension
    public static class KirinNotifierDescriptor extends BuildStepDescriptor<Publisher> {


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Kirin通知器配置";
        }

        public String getDefaultURL() {
            Jenkins instance = Jenkins.getInstance();
            assert instance != null;
            if(instance.getRootUrl() != null){
                return instance.getRootUrl();
            }else{
                return "";
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath ItemGroup context) {
            List<StandardCredentials> credentials =
                    CredentialsProvider.lookupCredentials(StandardCredentials.class, context, ACL.SYSTEM,
                            Collections.<DomainRequirement>emptyList());
            return new CredentialsListBoxModel()
                    .withEmptySelection()
                    .withMatching(CredentialsMatchers.always(), credentials);
        }

    }
}
