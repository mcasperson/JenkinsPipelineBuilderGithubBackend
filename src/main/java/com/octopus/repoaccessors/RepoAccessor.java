package com.octopus.repoaccessors;

/**
 * An abstraction for accessing files in a repo.
 */
public interface RepoAccessor {
    /**
     * Returns the contents of a file from the given path
     * @param path The repo file pathg
     * @return The file contents
     */
    String getFile(String path);
}
