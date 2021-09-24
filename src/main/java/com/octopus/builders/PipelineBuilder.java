package com.octopus.builders;

import com.octopus.repoaccessors.RepoAccessor;

/**
 * This interface defines a pipeline builder. Each builder is responsible for detecting files in a repo
 * that indicate that it can build a suitable pipeline.
 */
public interface PipelineBuilder {
    /**
     * Determine if this builder can build a pipeline for the given repo
     * @param accessor The repo to inspect
     * @return true if this builder can build a pipeline, and false otherwise
     */
    Boolean canBuild(RepoAccessor accessor);

    /**
     * Builds the pipeline from a given repo
     * @param accessor The repo to build the pipeline for
     * @return The Jenkins pipeline generated from the repo
     */
    String generate(RepoAccessor accessor);
}
