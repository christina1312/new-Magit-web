package System;

import java.util.*;
import java.util.stream.Collectors;

public class GitManager {

    private final Map<String,String> GitSetName; // file name+ url
    private final Map<String,BasicMAGitManager> GitMap; //file name + BasicMAGitManager

    public GitManager() {
        GitSetName = new HashMap<>();
        GitMap = new HashMap<>();
    }

    public void addRepository(String fileName, String Url, String userName) throws Exception {
        BasicMAGitManager BasicManager = new BasicMAGitManager(userName);
        BasicManager.LoadMAGit(Url, userName);
        GitSetName.put(fileName, Url);
        GitMap.put(fileName,BasicManager);
    }

    public void removeRepository(String username) {
        GitSetName.remove(username);
        GitMap.remove(username);
    }

    public List<BasicMAGitManager> getRepositoriesByUserName(String userName) {

        return GitMap.entrySet()
                .stream()
                .filter(e -> e.getValue().getUser().getName().equalsIgnoreCase(userName))
                     .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public boolean isRepositoryExists(String username) {
        return GitSetName.containsKey(username);
    }
}

