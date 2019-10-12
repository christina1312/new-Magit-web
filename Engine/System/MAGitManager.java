package System;

import java.util.*;
public class MAGitManager {

    private final Map<String,String> RepositorySetName; // file name+ url
    private final Map<String,BasicMAGitManager> ReposirotyMap; //file name + BasicMAGitManager

    public MAGitManager() {
        RepositorySetName = new HashMap<>();
        ReposirotyMap = new HashMap<>();
    }

    public void addRepository(String fileName, String Url ,String userName) throws Exception {
        BasicMAGitManager BasicManager = new BasicMAGitManager();
        BasicManager.LoadMAGit(Url);
        RepositorySetName.put(fileName, Url);
        ReposirotyMap.put(fileName,BasicManager);
    }

    public void removeRepository(String username) {
        RepositorySetName.remove(username);
        ReposirotyMap.remove(username);
    }

    public Map<String,String> getReposiroty() {
        return Collections.unmodifiableMap(RepositorySetName);
    }

    public boolean isRepositoryExists(String username) {
        return RepositorySetName.containsKey(username);
    }
}

