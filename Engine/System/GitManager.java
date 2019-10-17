package System;

import java.util.*;
import java.util.stream.Collectors;

public class GitManager {

    private final Map<String,String> GitSetName; // file name+ url
    private final Map<String,BasicMAGitManager> GitMap; //file name + BasicMAGitManager
    private final Map<String,NotificationManager> UsersNotifications; //file name + BasicMAGitManager

    public GitManager() {
        GitSetName = new HashMap<>();
        GitMap = new HashMap<>();
        UsersNotifications = new HashMap<>();
    }

    public void addRepository(String fileName, String Url, String userName) throws Exception {
        BasicMAGitManager BasicManager = new BasicMAGitManager(userName);
        BasicManager.LoadMAGit(Url, userName);
        GitSetName.put(fileName, Url);
        GitMap.put(fileName,BasicManager);
    }

    public void addUserNotification(String username, String message) {
        if (!UsersNotifications.containsKey(username)) {
            NotificationManager manager = new NotificationManager();
            manager.addNotification(new Notification(message, new Date(System.currentTimeMillis())));
            UsersNotifications.put(username, manager);
        } else {
            UsersNotifications.get(username).addNotification(new Notification(message, new Date(System.currentTimeMillis())));
        }
    }

    public List<Notification> getUserNotification(String username) {
        if (!UsersNotifications.containsKey(username)) {
            NotificationManager manager = new NotificationManager();
            UsersNotifications.put(username,manager);
        }
        return UsersNotifications.get(username).getNotificationList();
    }
    public List<BasicMAGitManager> getRepositoriesByUserName(String userName) {

        return GitMap.entrySet()
                .stream()
                .filter(e -> e.getValue().getUser().getName().equalsIgnoreCase(userName))
                     .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public BasicMAGitManager getRepositoryByUserName(String userName,String repoName) {
        List<BasicMAGitManager> repoList = getRepositoriesByUserName(userName);
        for(BasicMAGitManager repo : repoList){
            if(repo.getRepositoryName().equalsIgnoreCase(repoName)){
                return repo;
            }
        }
        return null;
    }
    public boolean isRepositoryExists(String username) {
        return GitSetName.containsKey(username);
    }
}

