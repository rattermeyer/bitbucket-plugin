package com.cloudbees.jenkins.plugins.filter.repository;

import com.cloudbees.jenkins.plugins.cause.BitbucketTriggerCause;
import com.cloudbees.jenkins.plugins.cause.repository.RepositoryCause;
import com.cloudbees.jenkins.plugins.payload.BitBucketPayload;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;

/**
 * Created by Shyri Villar on 15/03/2016.
 */
public class RepositoryPushActionFilter extends RepositoryActionFilter {

    @DataBoundConstructor
    public RepositoryPushActionFilter() {
    }

    @Override
    public boolean shouldTriggerBuild(BitBucketPayload bitbucketPayload) {
        return true;
    }

    @Override
    public BitbucketTriggerCause getCause(File pollingLog, BitBucketPayload bitBucketPayload) throws IOException {
        return new RepositoryCause(pollingLog, bitBucketPayload);
    }

    @Extension
    public static class ActionFilterDescriptorImpl extends RepositoryActionDescriptor {
        public String getDisplayName() { return "Push"; }
    }
}
