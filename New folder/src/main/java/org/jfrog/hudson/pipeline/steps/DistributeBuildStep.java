package org.jfrog.hudson.pipeline.steps;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jfrog.hudson.pipeline.Utils;
import org.jfrog.hudson.pipeline.executors.DistributionExecutor;
import org.jfrog.hudson.pipeline.types.ArtifactoryServer;
import org.jfrog.hudson.pipeline.types.DistributionConfig;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by yahavi on 12/04/2017.
 */

public class DistributeBuildStep extends AbstractStepImpl {

    private ArtifactoryServer server;
    private DistributionConfig distributionConfig;

    @DataBoundConstructor
    public DistributeBuildStep(DistributionConfig distributionConfig, ArtifactoryServer server) {
        this.distributionConfig = distributionConfig;
        this.server = server;
    }

    public ArtifactoryServer getServer() {
        return server;
    }

    public DistributionConfig getDistributionConfig() {
        return distributionConfig;
    }

    public static class Execution extends AbstractSynchronousNonBlockingStepExecution<Boolean> {
        private static final long serialVersionUID = 1L;

        @StepContextParameter
        private transient Run build;

        @StepContextParameter
        private transient TaskListener listener;

        @Inject(optional = true)
        private transient DistributeBuildStep step;

        @Override
        protected Boolean run() throws Exception {
            DistributionConfig distributionConfig = step.getDistributionConfig();

            if (StringUtils.isEmpty(distributionConfig.getBuildName())) {
                getContext().onFailure(new MissingArgumentException("Distribution build name is mandatory"));
                return false;
            }

            if (StringUtils.isEmpty(distributionConfig.getBuildNumber())) {
                getContext().onFailure(new MissingArgumentException("Distribution build number is mandatory"));
                return false;
            }

            if (StringUtils.isEmpty(distributionConfig.getTargetRepo())) {
                getContext().onFailure(new MissingArgumentException("Distribution target repository is mandatory"));
                return false;
            }

            new DistributionExecutor(Utils.prepareArtifactoryServer(null, step.getServer()), build, listener, getContext(), distributionConfig).execution();
            return true;
        }
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(DistributeBuildStep.Execution.class);
        }

        @Override
        // The step is invoked by ArtifactoryServer by the step name
        public String getFunctionName() {
            return "artifactoryDistributeBuild";
        }

        @Override
        public String getDisplayName() {
            return "Distribute build";
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }
    }

}
