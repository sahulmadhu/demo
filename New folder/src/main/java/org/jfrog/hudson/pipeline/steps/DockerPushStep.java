package org.jfrog.hudson.pipeline.steps;

import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jfrog.build.api.Module;
import org.jfrog.hudson.CredentialsConfig;
import org.jfrog.hudson.pipeline.ArtifactoryConfigurator;
import org.jfrog.hudson.pipeline.Utils;
import org.jfrog.hudson.pipeline.docker.DockerImage;
import org.jfrog.hudson.pipeline.docker.utils.DockerAgentUtils;
import org.jfrog.hudson.pipeline.docker.utils.DockerUtils;
import org.jfrog.hudson.pipeline.types.ArtifactoryServer;
import org.jfrog.hudson.pipeline.types.buildInfo.BuildInfo;
import org.jfrog.hudson.pipeline.types.buildInfo.BuildInfoAccessor;
import org.jfrog.hudson.util.JenkinsBuildInfoLog;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Properties;

/**
 * Created by romang on 5/2/16.
 */
public class DockerPushStep extends AbstractStepImpl {

    private final String image;
    private final ArtifactoryServer server;
    private CredentialsConfig credentialsConfig;
    private String host;
    private BuildInfo buildInfo;
    private String targetRepo;
    // Properties to attach to the deployed docker layers.
    private ArrayListMultimap<String, String> properties;

    @DataBoundConstructor
    public DockerPushStep(String image, CredentialsConfig credentialsConfig, String host, String targetRepo,
                          BuildInfo buildInfo, ArrayListMultimap<String, String> properties, ArtifactoryServer server) {

        this.image = image;
        this.credentialsConfig = credentialsConfig;
        this.host = host;
        this.targetRepo = targetRepo;
        this.buildInfo = buildInfo;
        this.properties = properties;
        this.server = server;
    }

    public ArtifactoryServer getServer() {
        return server;
    }

    public String getImage() {
        return image;
    }

    public ArrayListMultimap<String, String> getProperties() {
        return properties;
    }

    public CredentialsConfig getCredentialsConfig() {
        return credentialsConfig;
    }

    public String getHost() {
        return host;
    }

    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    public String getTargetRepo() {
        return targetRepo;
    }

    public static class Execution extends AbstractSynchronousNonBlockingStepExecution<BuildInfo> {
        private static final long serialVersionUID = 1L;

        @Inject(optional = true)
        private transient DockerPushStep step;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient Run build;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient FilePath ws;

        @Override
        protected BuildInfo run() throws Exception {
            if (step.getImage() == null) {
                getContext().onFailure(new MissingArgumentException("Missing 'image' parameter"));
                return null;
            }

            if (step.getTargetRepo() == null) {
                getContext().onFailure(new MissingArgumentException("Missing 'targetRepo' parameter"));
                return null;
            }
            JenkinsBuildInfoLog log = new JenkinsBuildInfoLog(listener);
            BuildInfo buildInfo = Utils.prepareBuildinfo(build, step.getBuildInfo());

            ArtifactoryServer server = step.getServer();
            String username = server.createCredentialsConfig().provideUsername(build.getParent());
            String password = server.createCredentialsConfig().providePassword(build.getParent());

            String imageId = DockerAgentUtils.getImageIdFromAgent(launcher, step.getImage(), step.getHost());
            DockerAgentUtils.pushImage(launcher, log, step.getImage(), username, password, step.getHost());
            DockerImage image = new DockerImage(imageId, step.getImage(), step.getTargetRepo(), buildInfo.hashCode());

            String parentId = DockerAgentUtils.getParentIdFromAgent(launcher, imageId, step.getHost());
            if (!StringUtils.isEmpty(parentId)) {
                Properties properties = new Properties();
                properties.put("docker.image.parent", DockerUtils.getShaValue(parentId));
                image.addBuildInfoModuleProps(properties);
            }

            String timestamp = Long.toString(buildInfo.getStartDate().getTime());
            ArtifactoryConfigurator config = new ArtifactoryConfigurator(Utils.prepareArtifactoryServer(null, server));
            Module module = image.generateBuildInfoModule(build, listener, config, buildInfo.getName(), buildInfo.getNumber(), timestamp);

            if (module.getArtifacts().size() == 0) {
                log.warn("Could not find docker image: " + step.getImage() + "in Artifactory.");
            }

            BuildInfoAccessor buildInfoAccessor = new BuildInfoAccessor(buildInfo);
            buildInfoAccessor.getModules().add(module);

            log.info("Successfully pushed docker image: " + step.getImage());
            return buildInfo;
        }
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(DockerPushStep.Execution.class);
        }

        @Override
        public String getFunctionName() {
            return "dockerPushStep";
        }

        @Override
        public String getDisplayName() {
            return "Artifactory docker push";
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }
    }
}

