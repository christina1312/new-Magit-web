package System;

import java.util.*;
public class RepositoryManager {

    private final Map<String,String> RepositorySetName; // file name+ url
    private final Map<String,BasicMAGitManager> RepositoryMap; //file name + BasicMAGitManager

    public RepositoryManager() {
        RepositorySetName = new HashMap<>();
        RepositoryMap = new HashMap<>();
    }

    public void addRepository(String fileName, String Url) throws Exception {
        BasicMAGitManager BasicManager = new BasicMAGitManager();
        BasicManager.LoadMAGit(Url);
        RepositorySetName.put(fileName, Url);
        RepositoryMap.put(fileName,BasicManager);
    }

    public void removeRepository(String username) {
        RepositorySetName.remove(username);
        RepositoryMap.remove(username);
    }

    public Map<String,String> getRepository() {
        return Collections.unmodifiableMap(RepositorySetName);
    }

    public boolean isRepositoryExists(String username) {
        return RepositorySetName.containsKey(username);
    }
}

