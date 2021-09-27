package com.octopus.repoaccessors;

import io.vavr.control.Try;

/**
 * An abstraction for accessing files in a repo.
 */
public interface RepoAccessor {
    /**
     * Returns the contents of a file from the given path
     * @param path The repo file pathg
     * @return The file contents
     */
    Try<String> getFile(String path);
    String getRepoPath();
}
