package org.jfrog.hudson.pipeline.types;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jfrog.build.extractor.clientConfiguration.IncludeExcludePatterns;

import java.io.Serializable;

/**
 * Created by Tamirh on 14/08/2016.
 */
public class Filter implements Serializable {
    protected IncludeExcludePatterns patternFilter;

    public Filter() {
        this.patternFilter = new IncludeExcludePatterns("", "");
    }

    @Whitelisted
    public Filter addInclude(String includePattern) {
        patternFilter.addIncludePatterns(includePattern);
        return this;
    }

    @Whitelisted
    public Filter addExclude(String excludePattern) {
        patternFilter.addExcludePatterns(excludePattern);
        return this;
    }

    @Whitelisted
    public Filter clear() {
        patternFilter = new IncludeExcludePatterns("", "");
        return this;
    }

    public IncludeExcludePatterns getPatternFilter() {
        return patternFilter;
    }
}
