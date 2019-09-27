
package System;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BasicMAGitManager {

    private Repository repository;
    private User user;

    public BasicMAGitManager() {
        user = new User();
        repository = new Repository(user);
    }

    //chris
    public boolean LoadMAGit(String xmlPath) throws Exception {
        if (checkIfPathExists(xmlPath)) {
            return repository.LoadMAGit(xmlPath);
        } else
            throw new Exception("Could not find file in this path");
    }

    public void ChangeRepository(String path) throws Exception {

        if (checkIfReposoitoryExists(path)) {
            repository.ChangeRepository(path);
        } else
            throw new Exception("The repository in the given path doesn't exist");
    }

    public void ShowCurrentCommitFileSystem() {
        repository.ShowCurrentCommitFileSystem();
    }

    public String ShowWorkingCopyStatus() throws IOException {
        return repository.ShowWorkingCopyStatus();
    }

    public void DoCommit(String commitDescription) throws Exception {
        repository.DoCommit(commitDescription);
    }

    public String ShowAllBranchesFileSystem() {
        return repository.ShowAllBranchesFileSystem();
    }

    public void CreateNewBranch(String branchName, String pointtedBranch) throws Exception {
        try {
            repository.CreateNewBranch(branchName, pointtedBranch);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void createNewRepository(String path) throws Exception {
        if (!Files.isDirectory(Paths.get(path))) {
            repository.createNewRepository(path);
        } else {
            throw new Exception("There is already directory in this path!");
        }
    }

    public void DeleteBranch(String branchName) throws Exception {
        try {
            repository.DeleteBranch(branchName);
        } catch (Exception ex) {
            throw ex;
        }

    }

    public boolean CheckoutHeadBranch(String branchName) throws Exception {
        return repository.CheckoutHeadBranch(branchName);
    }

    public void ShowActiveBranchHistory() {
        repository.ShowActiveBranchHistory();
    }

    public User getUser() {
        return user;
    }

    public boolean resetHeadBranch(String name) throws Exception {
        return repository.resetHeadBranch(name);
    }

    private boolean checkIfReposoitoryExists(String path) {
        File directory = new File(path + "\\.magit");
        File subDirectoryBranches = new File(directory.getPath() + "\\branches");
        File subDirectoryObjects = new File(directory.getPath() + "\\objects");

        if (!subDirectoryBranches.exists() || !subDirectoryBranches.isDirectory() ||
                !subDirectoryObjects.exists() || !subDirectoryObjects.isDirectory()) {
            return false;
        }
        return true;
    }

    private boolean checkIfPathExists(String path) throws Exception {
        File pathFile = new File(path);
        return pathFile.exists();
    }


    public void setUserName(String userName) {
        this.user.setName(userName);
    }

    public boolean isEmptyRepository() {
        return repository.checkIfEmptyRepositoryForUi();
    }

    public boolean merge(String branchToMerge) throws Exception {
        return repository.merge(branchToMerge);
    }

    public void cloneRepository(String pathRR, String pathLR, String RepositoryName) throws Exception {
        repository.cloneRepository(pathRR, pathLR, RepositoryName);
    }

    public void DeleteRepositoryAndCreateNew(String address) throws Exception {
        repository.DeleteRepositoryAndCreateNew(address);
    }

    public void fetch() throws Exception { // need to clone or load repo
        repository.fetch();
    }

    public String getRepositoryLocation() {
        return repository.getLocation();
    }

    public void deleteNotMagitDir() {
        repository.deleteNotMagitDir();
    }

    public String getRepositoryName() {
        return repository.getName();
    }

    public Commit showCommitData() {
        return repository.showCommitData();
    }

    public void deleteWorkingCopyChanges() {
        repository.deleteWorkingCopyChanges();
    }

    public ArrayList<String> startMerge(String branchToMerge, int init) throws Exception {
        return repository.startMerge(branchToMerge, init);
    }
    public ArrayList<String> handleSecondConflict() throws Exception {
        return repository.handleSecondConflict();
    }

    public boolean handleSingleConflict(String afterMergeText) throws Exception {
        if(afterMergeText!= null) {
            repository.handleSingleConflict(afterMergeText);
            return true;
        }
        return false;
    }
    public void setAfterMerge() throws Exception {
        repository.setAfterMerge();
    }
    public boolean checkIfThereIsMoreConflicts() {
        return repository.checkIfThereIsMoreConflicts();
    }
}


