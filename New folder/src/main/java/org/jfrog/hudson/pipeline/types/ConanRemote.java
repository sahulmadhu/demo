package org.jfrog.hudson.pipeline.types;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsScript;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by tamirh on 15/03/2017.
 */

public class ConanRemote implements Serializable {
    private transient CpsScript cpsScript;
    private String conanHome;
    public ConanRemote() {
    }

    public void setCpsScript(CpsScript cpsScript) {
        this.cpsScript = cpsScript;
    }

    public void setConanHome(String conanHome) {
        this.conanHome = conanHome;
    }

    @Whitelisted
    public String add(Map<String, Object> args) {
        if (!args.containsKey("server") || !args.containsKey("repo")) {
            throw new IllegalArgumentException("server and repo are mandatory arguments.");
        }
        ArtifactoryServer server = (ArtifactoryServer) args.get("server");
        String serverName = UUID.randomUUID().toString();
        String repo = (String) args.get("repo");
        cpsScript.invokeMethod("ConanAddRemote", getAddRemoteExecutionArguments(server, serverName, repo));
        cpsScript.invokeMethod("ConanAddUser", getAddUserExecutionArguments(server, serverName));
        return serverName;
    }

    private Map<String, Object> getAddRemoteExecutionArguments(ArtifactoryServer server, String serverName, String repo) {
        String serverUrl = buildConanRemoteUrl(server, repo);
        Map<String, Object> stepVariables = new LinkedHashMap<String, Object>();
        stepVariables.put("serverUrl", serverUrl);
        stepVariables.put("serverName", serverName);
        stepVariables.put("conanHome", conanHome);
        return stepVariables;
    }

    private String buildConanRemoteUrl(ArtifactoryServer server, String repo) {
        StringBuilder serverURL = new StringBuilder(server.getUrl());
        if (!StringUtils.endsWith(serverURL.toString(), "/")) {
            serverURL.append("/");
        }
        serverURL.append("api/conan/").append(repo);
        return serverURL.toString();
    }

    private Map<String, Object> getAddUserExecutionArguments(ArtifactoryServer server, String serverName) {
        Map<String, Object> stepVariables = new LinkedHashMap<String, Object>();
        stepVariables.put("server", server);
        stepVariables.put("serverName", serverName);
        stepVariables.put("conanHome", conanHome);
        return stepVariables;
    }
}