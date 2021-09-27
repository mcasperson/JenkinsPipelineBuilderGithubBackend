package com.octopus.repoaccessors;

import io.vavr.control.Try;

/**
 * An abstraction for accessing files in a repo.
 */
public interface RepoAccessor {
    void setRepo(String repo);
    /**
     * Returns the contents of a file from the given path
     * @param path The repo file path
     * @return The file contents
     */
    Try<String> getFile(String path);
    boolean testFile(String path);
    String getRepoPath();
    String getDefaultBranch();
}
