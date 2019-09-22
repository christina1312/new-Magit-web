package System;

import Objects.Blob;
import Objects.Folder;
import Objects.Item;
import Objects.RemoteReference;
import org.apache.commons.io.FileUtils;
import xmlFormat.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
public class Repository {

    private String name;
    private String location;
    private Branch activeBranch;
    private List<Branch> branchesList;
    private List<Commit> commitsList;
    private User user;
    private boolean repositoryChanged = false;
    private RemoteReference remote;
    private MagitRepository magitRepository;
    private String objectPath, branchPath;
    private static String magitPath = "";
    private Set<String> hashBlob;
    private Set<String> hashFolder;
    private Set<String> hashCommit;
    private Set<String> hashBranch;

    public Repository(User user){
        branchesList = new ArrayList<>();
        commitsList = new ArrayList<>();
        remote = new RemoteReference();
        this.user = user;
    }

    private Scanner s = new Scanner(System.in);

    private Branch getActiveBranch() {
        return activeBranch;
    }

    private void setActiveBranch(Branch activeBranch) {
        this.activeBranch = activeBranch;
    }

    public String getLocation() {
        return location;
    }

    private void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String user) {
        this.user.setName(user);
    }

    private List<Branch> getBranchesList() {
        return branchesList;
    }

    private List<Commit> getCommitsList() {
        return commitsList;
    }

    private void addItemToBranchList(Branch branch) {
        branchesList.add(branch);
        Collections.sort(branchesList, new Comparator<Branch>() {
            @Override
            public int compare(Branch i1, Branch i2) {
                String compareName = (i1).getName();
                return compareName.compareTo(i2.getName());
            }
        });
    }

    private void addItemToCommitList(Commit commit) {
        commitsList.add(commit);
        commitsList.sort(new Comparator<Commit>() {
            @Override
            public int compare(Commit i1, Commit i2) {
                String compareMessage = (i1).getMessage();
                return compareMessage.compareTo(i2.getMessage());
            }
        });
    }

    public void LoadMAGit(String xmlPath) throws Exception {
        LoadFile(xmlPath);
        if (!checkIfEmptyRepository() && !repositoryChanged) {
            ZipAllItemInRepository(this.location + "\\.magit\\objects");
            CreateWC();
        }
    }

    public void ChangeRepository(String path) throws Exception {
        String sha1;
        String dataFromBranch;
        String headBranch = "";
        String branchName;
        boolean isActive;
        boolean isRemote;
        boolean isTracking;
        String trackingAfter = null;
        magitPath = path + "\\.magit";

        String pathBranches = path + "\\.magit\\branches";
        File[] filesBranches = new File(pathBranches).listFiles();
        String unzippedfiless = path + "\\.magit\\unzippedfiles";
        File unZipFilesDir = new File(unzippedfiless);
        unZipFilesDir.mkdirs();

        List<Branch> branchesNames = new ArrayList<>();

        for (File branchFile : filesBranches) {// RTB
            if (branchFile.getName().equalsIgnoreCase("HEAD.txt")) {
                headBranch = readFromFile(branchFile.getPath());
            }
        }
        for (File branchFile : filesBranches) {
            isActive = false;
            branchName = branchFile.getName().substring(0, (branchFile.getName().length() - 4));
            if (!branchFile.getName().equalsIgnoreCase("HEAD.txt")) {
                if (branchName.equalsIgnoreCase(headBranch)) {
                    isActive = true;
                }
                dataFromBranch = readFromFile(branchFile.getPath());
                String[] parts = dataFromBranch.split(", ");
                isRemote = parts[1].equalsIgnoreCase("TRUE");
                isTracking = parts[2].equalsIgnoreCase("TRUE");
                if (isTracking)
                    trackingAfter = parts[3];
                Branch newBranch = new Branch(branchName, null, isActive, isRemote, isTracking, trackingAfter);
                sha1 = parts[0];
                findNextCommit(sha1, newBranch, path, unZipFilesDir, true);
                if (isActive)
                    this.activeBranch = newBranch;
                branchesNames.add(newBranch);
            }
        }
        String contentName = path;
        String[] partsForName = contentName.split("\\\\");
        this.name = partsForName[partsForName.length - 1];
        this.location = path;
        this.branchesList = branchesNames;
        this.branchPath = String.format("%s\\branches\\", magitPath);
        this.objectPath = String.format("%s\\objects\\", magitPath);

        repositoryChanged = true;
    }

    private void findNextCommit(String sha1, Branch newBranch, String path, File unZipFilesDir, boolean isFirstBranch) throws Exception {

        String pathObjects = path + "\\.magit\\objects";
        File[] filesObjects = new File(pathObjects).listFiles();
        String unzippedfiles = path + "\\.magit\\unzippedfiles";
        File[] unzippedfilesObjects;
        String unzipName;

        try {
            for (File objectFile : filesObjects) {
                if (sha1.equalsIgnoreCase(objectFile.getName())) {
                    unzipName = unZip(objectFile.getPath(), unZipFilesDir); /// *********** change the path !!
                    unzippedfilesObjects = new File(unzippedfiles).listFiles();
                    for (File unzippedCommit : unzippedfilesObjects) {
                        if (unzippedCommit.getName().equalsIgnoreCase(unzipName)) {
                            String content = readFromFile(unzippedCommit.getPath());
                            Commit newCommit = enterUnzipCommitToCommitsList(content, path);
                            if (isFirstBranch) {
                                newBranch.setpCommit(newCommit);
                            }
                            if (!checkIfCommitExists(newCommit)) {
                                // commitsList.add(newCommit);
                                addCommitToCommitList(newCommit);
                            }
                            findNextCommit(newCommit.getPrecedingCommit(), newBranch, path, unZipFilesDir, false);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception("Could not unzip Folder");
        }
    }
    private Commit enterUnzipCommitToCommitsList(String content, String path) throws Exception {
        String contentName = path;
        String[] partsForName = contentName.split("\\\\");
        String name = partsForName[partsForName.length - 1];

        String[] parts = content.split(",");
        String folderSha1 = (parts[0]);
        Folder folder = findFolderInObjectFolder(folderSha1, path);
        folder.setName(name);
        folder.setRoot(true);
        folder.setPath(path);
        String messege = parts[2];
        User user = new User(parts[4]);
        String date = (parts[3]);
        String precedingCommit = parts[1];

        return new Commit(folder, messege, user, date, precedingCommit, "");
    }
    private boolean checkIfCommitExists(Commit commit) {

        for (Commit currentCommit : commitsList) {
            if (sha1Hex(currentCommit.sha1ToString()).equalsIgnoreCase(sha1Hex(commit.sha1ToString())))
                return true;
        }
        return false;
    }

    private List<Item> enterUnzipItemToItemsList(String content, String path, File unZipFilesDir) throws Exception {
        List<Item> newitems = new ArrayList<>();
        String blobContent;
        Folder folderToAdd;
        Blob newBlob;
        String[] parts = content.split("\n");
        try {
            for (String part : parts) {
                String[] subParts = part.split(",");
                if (subParts[2].equalsIgnoreCase("BLOB")) {
                    blobContent = findBlobContent(subParts[1], path + "\\" + subParts[0], unZipFilesDir);
                    newBlob = new Blob(subParts[0], Item.Type.BLOB, new User(subParts[3]), subParts[4], blobContent);
                    newBlob.setPath(path + "\\" + subParts[0]);
                    newitems.add(newBlob);
                }
                if (subParts[2].equalsIgnoreCase("FOLDER")) {
                    folderToAdd = findFolderInObjectFolder(subParts[1], path + "\\" + subParts[0]);
                    folderToAdd.setName(subParts[0]);
                    folderToAdd.setlastUpdateDate(subParts[4]);
                    folderToAdd.setlastUpdater(new User(subParts[3]));
                    folderToAdd.setSha1(sha1Hex(folderToAdd.toString()));
                    folderToAdd.setPath(path + "\\" + folderToAdd.getName());
                    newitems.add(folderToAdd);
                }
            }
        } catch (Exception ex) {
            throw new Exception("Could not unzip Folder");
        }
        return newitems;
    }

    private String findBlobContent(String sha1, String path, File unZipFilesDir) throws Exception {
        String res = null;
        String unzipName = "";
        String pathObjects = magitPath + "\\objects";
        File[] filesObjects = new File(pathObjects).listFiles();
        String unzippedfiles = magitPath + "\\unzippedfiles";
        File[] unzippedfilesObjects;

        try {
            for (File objectFile : filesObjects) {
                if (sha1.equalsIgnoreCase(objectFile.getName())) {
                    unzipName = unZip(objectFile.getPath(), unZipFilesDir); /// *********** change the path !!
                    unzippedfilesObjects = new File(unzippedfiles).listFiles();
                    for (File unzippedCommit : unzippedfilesObjects)//אפשר אולי שאנזיפ יחזיר את הקובץ שהוא עשה לו אנזיפ
                    {
                        if (unzippedCommit.getName().equalsIgnoreCase(unzipName)) {
                            res = readFromFile(unzippedCommit.getPath());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception("Could not unzip Blob");
        }
        return res;
    }

    private Folder findFolderInObjectFolder(String FolderSha1, String path) throws Exception {
        Folder newFolder = null;
        String unzipName = "";

        String contentName = path;
        String[] parts = contentName.split("\\\\");
        String name = parts[parts.length - 1];

        if (FolderSha1.equalsIgnoreCase("") || FolderSha1 == null) {
            System.out.println(("Cannnot find folder"));//  return null;
        }
        String objectsPath = magitPath + "\\objects";
        String unzippedfiles = magitPath + "\\unzippedfiles";
        File unZipFilesDir = new File(unzippedfiles);
        File[] filesObjects = new File(objectsPath).listFiles();
        File[] unzippedfilesObjects;

        try {
            for (File objectFile : filesObjects) {
                if (FolderSha1.equalsIgnoreCase(objectFile.getName())) {
                    unzipName = unZip(objectFile.getPath(), unZipFilesDir); /// *********** change the path !!
                    unzippedfilesObjects = new File(unzippedfiles).listFiles();
                    for (File unzippedCommit : unzippedfilesObjects)//אפשר אולי שאנזיפ יחזיר את הקובץ שהוא עשה לו אנזיפ
                    {
                        if (unzippedCommit.getName().contains(unzipName)) {
                            String content = readFromFile(unzippedCommit.getPath());
                            List<Item> newitems = enterUnzipItemToItemsList(content, path, unZipFilesDir);
                            newFolder = new Folder(newitems);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception("Could not unzip Folder and build the repository instance");
        }
        return newFolder;
    }

    public void ShowCurrentCommitFileSystem() {

        System.out.println("The repository location is:  \n" + this.location);
        if (this.getName() != null) {
            System.out.println("The repository name is:  \n" + this.name);
        }
        Folder rootFolder = this.getActiveBranch().getpCommit().getRootFolder();
        ShowCurrentCommitFileSystemRec(rootFolder, this.getLocation());
    }

    private void ShowCurrentCommitFileSystemRec(Item item, String location) {
        item.showInfo();

        if (item.getType().toString().equalsIgnoreCase(Item.Type.FOLDER.toString())) {
            Folder folder = (Folder) item;
            for (Item currItem : folder.getItemsArray()) {
                ShowCurrentCommitFileSystemRec(currItem, location + "\\" + item.getName());
            }
        }
    }

//    public void ShowAllBranchesFileSystem() {
//        for (Branch branch : this.getBranchesList()) {
//            if (branch.getIsActive()) {
//                System.out.println("The head branch is: " + activeBranch.getName());
//            }
//            branch.showInfo();
//        }
//    }

    public String ShowAllBranchesFileSystem() {
        StringBuilder res=new StringBuilder();
        for (Branch branch : this.getBranchesList()) {
            if (branch.getIsActive()) {
                // System.out.println("The head branch is: " + activeBranch.getName());
                res.append("The head branch is: " + activeBranch.getName() + "\n");
            }
            //branch.showInfo();
            res.append(branch.showInfo());
        }
        return res.toString();
    }

    public void createNewRepository(String path) throws Exception {
        new File(String.format("%s\\.magit", path)).mkdirs();
        makeDirectories(path);
        createSingleBranch(path);
        ChangeRepository(path);
    }

    private void createSingleBranch(String path) throws IOException {
        String delimiter = ", ";

        String masterPath = path + "\\.magit\\branches\\master.txt";
        String headPath= path + "\\.magit\\branches\\HEAD.txt";

        //for folders
        //TODO verify null
        String branchToString = null + delimiter + "false" + delimiter + "false" + delimiter + "false";
        File file = new File(masterPath);
        file.createNewFile();
        Utility.writeToFile(branchToString, masterPath);

        File headBranch = new File(headPath);
        headBranch.createNewFile();
        Utility.writeToFile("master", headPath);
    }

    public void CreateNewBranch(String branchName, String pointtedBranch) throws Exception {
        for (Branch branch : branchesList) {
            if (branchName.equalsIgnoreCase(branch.getName())) {
                throw new Exception("The branch name is already exists ");
            }
            File file = new File(branchPath + branchName + ".txt");
            file.createNewFile();
            try {
                Branch newBranch = findBranch(pointtedBranch);

                Utility.writeToFile(sha1Hex(newBranch.getpCommit().toString()),
                        branchPath + branchName + ".txt");
                AddBranchToList(branchName);
                break;
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    private Branch findBranch(String name) throws Exception {
        for (Branch branch : branchesList) {
            if (name.equalsIgnoreCase(branch.getName())) {
                return branch;
            }
        }
        throw new Exception("The branch was not found !!");
    }

    public void DeleteBranch(String branchName) throws Exception {
        boolean flag = false;
        if (branchName.equalsIgnoreCase(activeBranch.getName()) ||
                branchName.equalsIgnoreCase("HEAD")) {
            throw new Exception("You cant delete head branch! ");
        }
        String pathBranches = this.getLocation() + "\\.magit\\branches";
        File[] filesBranches = new File(pathBranches).listFiles();

        for (File file : filesBranches) {
            if (file.getName().equalsIgnoreCase(branchName + ".txt")) {
                file.delete();
                flag = true;
            }
            if (flag)
                break;
        }
        if (!flag)
            throw new Exception("This branch doesnt exists!");

        deleteBranchFromList(branchName);
    }

    //chris
    public boolean resetHeadBranch(String name) throws Exception {
        Boolean isThereOpenChanges = true;

        //find changes
        File file = new File(location);
        Folder newFolder = createCommittedWC(file);
        if (sha1Hex(newFolder.toString()).equalsIgnoreCase(sha1Hex(activeBranch.getpCommit().getRootFolder().toString()))) { // todo  verify
            isThereOpenChanges = false;
        }
        if (isThereOpenChanges) {
            return true;
        }
        //for instance
        this.setActiveBranch(findBranch(name));
        Utility.writeToFile(this.getActiveBranch().getName(), location + "\\.magit\\branches\\HEAD.txt");

        //for WC
        deleteWorkingCopyChanges();
        createWCRec(this.getActiveBranch().getpCommit().getRootFolder(), this.getLocation(), true);

        return false;
    }

    public boolean CheckoutHeadBranch(String branchName) throws Exception {
        Boolean isThereOpenChanges = true;
        boolean isBranchFound=false;
        File file = new File(location);
        Folder newFolder = createCommittedWC(file);
        if (sha1Hex(newFolder.toString()).equalsIgnoreCase(sha1Hex(activeBranch.getpCommit().getRootFolder().toString()))) { // todo  verify
            isThereOpenChanges = false;
        }
        if (isThereOpenChanges) {
            return true;
        } else {
            for (Branch currBranch : branchesList) {
                if (currBranch.getName().equalsIgnoreCase(branchName)) {
                    isBranchFound=true;
                }
            }
            if (!isBranchFound) {
                throw new Exception("Branch name were not found, Please try again!");
            }
            deleteWorkingCopyChanges();
            switchBranch(branchName);
        }
        return false;
    }

    public void deleteWorkingCopyChanges() {
        File[] filesRepo = new File(location).listFiles();
        for (File currFile : filesRepo) {
            if (!currFile.getName().equalsIgnoreCase(".magit")) {
                deleteDirectory(currFile);
            }
        }
    }

    //chris
    private void switchBranch(String branchName) throws Exception {
        Branch branch;

            for (Branch currBranch : branchesList) {
                if (currBranch.getName().equalsIgnoreCase(branchName)) {
                    File[] filesRepo = new File(location).listFiles();
                    for (File currFile : filesRepo) {
                        if (!currFile.getName().equalsIgnoreCase(".magit")) {
                            deleteDirectory(currFile);
                        }
                    }
                    branch = currBranch;
                    createWCRec(branch.getpCommit().getRootFolder(), this.getLocation(), true);
                    Utility.writeToFile(branchName, location + "\\.magit\\branches\\HEAD.txt");
                    this.activeBranch = branch;
                }
            }
    }

    public void ShowActiveBranchHistory() {
        Commit commit = this.getActiveBranch().getpCommit();
        boolean found;
        commit.showInfo();

        while (commit != null) {
            found = false;
            for (Commit pCommit : commitsList) {
                if (commit.getPrecedingCommit() != null) {
                    if (commit.getPrecedingCommit().equalsIgnoreCase(sha1Hex(pCommit.toString()))) {
                        commit = pCommit;
                        commit.showInfo();
                        found = true;
                    }
                } else {
                    commit = null;
                    break;
                }
            }
            if (!found) {
                commit = null;
            }
        }

    }

    // Aux function !!
    private void LoadFile(String xmlPath) throws Exception {

        File file = new File(xmlPath);
        try {
            if (!file.exists())
                throw new Exception("File is not exists \n");
            if (!ContainsXml(xmlPath))
                throw new Exception("File is not finish with '.xml' ending \n");
            magitRepository = deserializeFromFile(file); // need to from repository

            if (magitRepository != null) {
                CreateRealRepository(magitRepository.getLocation());

            } else
                throw new Exception("The xml invalid! \n");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void ZipAllItemInRepository(String path) throws IOException {
        for (Commit commit : this.getCommitsList()) {
            commit.zipNewItems(commit.getRootFolder(), path);
            commit.zipAnItem(path);
        }
    }

    public void CreateWC() throws IOException {
        createWCRec(this.getActiveBranch().getpCommit().getRootFolder(), this.getLocation(), true);
    }

    private void createWCRec(Item item, String location, Boolean isRoot) throws IOException {
        if (item.getType().toString().equalsIgnoreCase(Item.Type.FOLDER.toString())) {
            Folder folder = (Folder) item;
            if (!isRoot) {
                new File(String.format(location /*+ "\\" + item.getName()*/)).mkdirs();
            }
            for (Item currItem : folder.getItemsArray()) {
                createWCRec(currItem, location + "\\" + currItem.getName(), false);
            }
        } else {
            Blob blob = (Blob) item;
            blob.createBlobOnWC(location);
        }
    }

    private boolean CreateRealRepository(String address) throws Exception {

        validateXmlFile();
        int inputFromUser;
        File directory = new File(address);
        File subDirectory = new File(directory.getPath() + "\\.magit");
        File[] filesRepo = new File(address).listFiles();

        if (subDirectory.exists() && subDirectory.isDirectory()) {
            return true;

        } else// there is no .magit
        {
            if (filesRepo != null ? filesRepo.length > 0 : false) {
                throw new Exception("The is already files in this location. You cannot create repository there !");
            } else {
                buildRepositoryinGivenPath();
            }
        }
        return false;
    }

    public void DeleteRepositoryAndCreateNew(String address) throws Exception {
        deleteDirectory(new File(address));
        buildRepositoryinGivenPath();
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    //chris
    public void deleteNotMagitDir() {
        File[] filesRepo = new File(location).listFiles();
        for (File currFile : filesRepo) {
            if (!currFile.getName().equalsIgnoreCase(".magit")) {
                deleteDirectory(currFile);
            }
        }
    }
    //chris
    private void buildRepositoryinGivenPath() throws Exception {
        try {
            this.name = magitRepository.getName();
            makeDirectories(this.getLocation());
            if (!checkIfEmptyRepository()) {
                BuildCommitList();
                BuildBranchList();
                CreateBranchesFiles();
                setRemoteReference();
            }
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private void setRemoteReference() {
        if (magitRepository.getMagitRemoteReference() != null) {
            this.remote.setLocation(magitRepository.getMagitRemoteReference().getLocation());
            this.remote.setName(magitRepository.getMagitRemoteReference().getName());
        }
    }

    private void setRemoteReference(String name, String location) {
        this.remote.setLocation(location);
        this.remote.setName(name);
    }

    //chris
    private Folder createRec(MagitSingleFolder rootFolder, String path) throws Exception {
        MagitSingleFolder tempFolder = rootFolder;
        List<Item> foldersList = new ArrayList();
        Folder newFolder;
        String name;
        boolean found = false;

        for (xmlFormat.Item item : rootFolder.getItems().getItem()) {
            found = false;
            if (item.getType().equalsIgnoreCase(Item.Type.BLOB.toString()))
                try {
                    foldersList.add(createBlob(item.getId(), path));
                } catch (Exception ex) {
                    //System.out.println(ex.getMessage());
                    throw new Exception(ex.getMessage());
                }
            else if (item.getType().equalsIgnoreCase(Item.Type.FOLDER.toString())) {
                for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
                    if (item.getId().equalsIgnoreCase(folder.getId())) {
                        foldersList.add(createRec(folder, path + "\\" + folder.getName()));
                        found = true;
                    }
                    if (found)
                        break;
                }
            }
        }

        name = tempFolder.getName();

        if (name == null) {
            String content = this.location;
            String[] parts = content.split("\\\\");

            name = parts[parts.length - 1];
        }

        newFolder = new Folder(name, "", Item.Type.FOLDER, new User(tempFolder.getLastUpdater()), getTime(tempFolder.getLastUpdateDate()));
        Collections.sort(foldersList, new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                String compareName = (i1).getName();
                return compareName.compareTo(i2.getName());
            }
        });

        newFolder.setAItemsArray(foldersList);
        newFolder.setSha1(sha1Hex(newFolder.toString()));
        newFolder.setPath(path);
        return newFolder;
    }

    private Item createBlob(String id, String path) throws Exception {
        Blob newBlob = null;
        for (MagitBlob blob : magitRepository.getMagitBlobs().getMagitBlob()) {
            if (blob.getId().equalsIgnoreCase(id)) {
                // blob.getContent().replaceAll("\\s+", "");
                newBlob = new Blob(blob.getName(), Item.Type.BLOB, new User(blob.getLastUpdater()), getTime(blob.getLastUpdateDate()), blob.getContent());
                newBlob.setPath(path + "\\" + newBlob.getName());
            }
        }
        return newBlob;
    }

    private void CreateBranchesFiles() throws IOException {
        String delimiter = ", ";
        String path = branchPath;
        for (Branch branch : this.getBranchesList()) {
            String branchToString = sha1Hex(branch.getpCommit().toString()) + delimiter + branch.isRemote() +
                    delimiter + branch.isTracking() + delimiter + branch.getTrackingAfter();

            File file = new File(path + branch.getName() + ".txt");
            file.createNewFile();
            Utility.writeToFile(branchToString, branchPath + branch.getName() + ".txt");
        }
    }

    private void BuildBranchList() throws Exception {
        boolean isActive;
        Commit currentCommit = null;
        Branch currentBranch = null;
        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {
            isActive = false;
            for (Commit commit : this.getCommitsList()) {
                if (branch.getPointedCommit().getId().equalsIgnoreCase(commit.getId()))
                    currentCommit = commit;
            }
            String head = magitRepository.getMagitBranches().getHead();
            if (branch.getName().equalsIgnoreCase(head.toUpperCase())) {
                isActive = true;
                File file = new File(branchPath + "HEAD.txt");
                file.createNewFile();
                Utility.writeToFile(head, branchPath + "HEAD.txt");
            }


            currentBranch = new Branch(branch.getName(), currentCommit,
                    isActive, branch.isIsRemote(), branch.isTracking(), branch.getTrackingAfter());
            if (branch.getName().equalsIgnoreCase(head.toUpperCase())) {
                this.setActiveBranch(currentBranch);
                this.addItemToBranchList(currentBranch);
            }
        }
    }

    //chris
    private void BuildCommitList() throws Exception {
        MagitSingleFolder rootFolder = null;
        Folder newFolder = null;
        Commit newCommit;
        for (MagitSingleCommit commit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
                if (folder.getId().equalsIgnoreCase(commit.getRootFolder().getId()))
                    rootFolder = folder;

            }
            newFolder = createRec(rootFolder, magitRepository.getLocation());
            try {
                newFolder.setRoot(true);
            } catch (Exception ex) {
                //System.out.println(ex.getMessage());
                throw new Exception(ex.getMessage());
            }
            newCommit = createNewCommit(commit, newFolder, magitRepository.getLocation());
            this.addItemToCommitList(newCommit);
        }
    }

    private void validateXmlFile() throws Exception {

        hashBlob = new HashSet<>();
        hashFolder = new HashSet<>();
        hashCommit = new HashSet<>();
        hashBranch = new HashSet<>();

        if (magitRepository.getLocation().length() > 0)
            this.setLocation(magitRepository.getLocation());
        else
            throw new Exception("Invalid file. Location can not be empty!");

        checkBlobId();
        checkFolderId();
        checkCommitId();
        checkBranch();
        CheckIfFolderPointsToBlobOrFolder();
        CheckIfCommitPointsToFolder();
        CheckIfBranchPointsToCommit();

    }

    private void CheckIfFolderPointsToBlobOrFolder() throws Exception {
        boolean flag;

        for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
            flag = false;

            for (xmlFormat.Item item : folder.getItems().getItem()) {
                if (item.getType().equalsIgnoreCase(Item.Type.BLOB.toString())) {
                    for (MagitBlob blob : magitRepository.getMagitBlobs().getMagitBlob()) {
                        if (item.getId().equalsIgnoreCase(blob.getId())) {
                            flag = true;
                        }
                    }
                    if (!flag)
                        throw new Exception("Invalid file. Folder can not point to blob that doesn't exists! \n");

                    if (item.getType().equalsIgnoreCase(Item.Type.FOLDER.toString())) {
                        for (MagitSingleFolder magitFolder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
                            if (item.getId().equalsIgnoreCase(magitFolder.getId())) {
                                flag = true;
                            }
                            if (folder.getId().equalsIgnoreCase((item.getId())))
                                throw new Exception("Invalid file. Folder can not point to itself! \n");

                        }
                        if (!flag)
                            throw new Exception("Invalid file. Folder can not point to folder that doesn't exists! \n");
                    }
                }
            }
        }
    }

    private boolean checkIfEmptyRepository() {
        return !(magitRepository.getMagitCommits().getMagitSingleCommit().size() > 0);
    }

    public boolean checkIfEmptyRepositoryForUi() {

        File[] filesInRepos = new File(location).listFiles();
        if (filesInRepos.length != 1) {
            return false;
        }
        for (File file : filesInRepos) {
            if (!file.getName().equalsIgnoreCase(".magit")) {
                return false;
            } else {
                File[] filesInMagit = new File(location + "\\.magit").listFiles();
                for (File currFile : filesInMagit) {
                    if (currFile.getName().equalsIgnoreCase("branches")) {
                        File[] filesInBranches = new File(location + "\\.magit\\branches").listFiles();
                        if (filesInBranches.length != 0) {
                            return false;
                        }
                    }
                    if (currFile.getName().equalsIgnoreCase("objects")) {
                        File[] filesInObjects = new File(location + "\\.magit\\objects").listFiles();
                        if (filesInObjects.length != 0) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void CheckIfBranchPointsToCommit() throws Exception {
        boolean flag, branchFlag = false;
        String head = magitRepository.getMagitBranches().getHead();

        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {
            flag = false;
            if (branch.getName().equalsIgnoreCase(head)) {
                branchFlag = true;
            }
            for (MagitSingleCommit commit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
                {
                    if (branch.getPointedCommit() != null)
                        if (branch.getPointedCommit().getId().equalsIgnoreCase(commit.getId()))
                            flag = true;
                }
            }
            if (!checkIfEmptyRepository()) {
                if (!flag) {
                    throw new Exception("Invalid file. Branch can not point to commit that doesn't exists! \n");
                }
            }
        }
        if (!branchFlag) {
            throw new Exception("Invalid file. Head branch must point to existed branch! \n");
        }

    }

    private void CheckIfCommitPointsToFolder() throws Exception {
        boolean flag;

        for (MagitSingleCommit commit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            flag = false;

            for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
                if (commit.getRootFolder().getId().equalsIgnoreCase(folder.getId()))
                    if (folder.isIsRoot())
                        flag = true;
                    else
                        throw new Exception("Invalid file. Commit can not points to folder that is not root! \n");

            }
            if (!flag)
                throw new Exception("Invalid file. Commit can not point to folder that doesn't exists! \n");

        }
    }

    private void checkFolderId() throws Exception {
        for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
            if (hashFolder.contains(folder.getId()))
                throw new Exception("Invalid file. There is 2 Folders with the same id");
            else
                hashFolder.add(folder.getId());
        }
    }

    private void checkCommitId() throws Exception {
        for (MagitSingleCommit commit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            if (hashCommit.contains(commit.getId()))
                throw new Exception("Invalid file. There is 2 Commits with the same id");
            else
                hashCommit.add(commit.getId());
        }
    }

    private void checkBranch() throws Exception {
        for (MagitSingleBranch branch : magitRepository.getMagitBranches().getMagitSingleBranch()) {
            if (hashBranch.contains(branch.getName()))
                throw new Exception("Invalid file. There is 2 Commits with the same id");
            else
                hashBranch.add(branch.getName());
        }
    }

    private void checkBlobId() throws Exception {
        for (MagitBlob blob : magitRepository.getMagitBlobs().getMagitBlob()) {
            if (hashBlob.contains(blob.getId()))
                throw new Exception("Invalid file. There is 2 Blobs with the same id");
            else
                hashBlob.add(blob.getId());
        }
    }

    private void deleteBranchFromList(String name) {
        boolean stop = false;
        for (Branch branch : branchesList) {
            if (branch.getName().equalsIgnoreCase(name)) {
                commitsList.remove(branch.getpCommit());
                branchesList.remove(branch);
                stop = true;
            }
            if (stop)
                break;
        }
    }

    private void AddBranchToList(String branchName) {
        branchesList.add(new Branch(branchName, activeBranch.getpCommit(), false, false, false, null));
    }

    private Commit createNewCommit(MagitSingleCommit commit, Folder folder, String path) throws Exception {

        Commit previous = findPreviousCommit(commit, path);

        return new Commit(
                folder,
                commit.getMessage(),
                new User(commit.getAuthor()),
                getTime(commit.getDateOfCreation()),
                (previous != null ? sha1Hex(previous.toString()) : null),
                commit.getId()
        );
    }

    private Commit findPreviousCommit(MagitSingleCommit commit, String path) throws Exception {
        boolean found = false;
        String id = commit.getPrecedingCommits() != null ?
                (commit.getPrecedingCommits().getPrecedingCommit() != null ?
                        (commit.getPrecedingCommits().getPrecedingCommit().size() > 0 ?
                                commit.getPrecedingCommits().getPrecedingCommit().get(0).getId() : null) : null) : null;

        if (id == null || id.equalsIgnoreCase(commit.getId()) || id.isEmpty())
            return null;
        MagitSingleFolder rootFolder = null;
        Folder newFolder;
        for (MagitSingleCommit magitCommit : magitRepository.getMagitCommits().getMagitSingleCommit()) {
            if (id.equalsIgnoreCase(magitCommit.getId())) {
                for (MagitSingleFolder folder : magitRepository.getMagitFolders().getMagitSingleFolder()) {
                    if (folder.getId().equalsIgnoreCase(magitCommit.getRootFolder().getId())) {
                        rootFolder = folder;
                        found = true;
                    }
                    if (found)
                        break;
                }
                if (!rootFolder.isIsRoot()) {
                    newFolder = createRec(rootFolder, path + "\\" + rootFolder.getName());
                } else {
                    newFolder = createRec(rootFolder, path);
                    newFolder.setRoot(true);
                }
                return createNewCommit(magitCommit, newFolder, path);
            }
        }
        return null;
    }

    //utility
    private boolean ContainsXml(String i_address) {
        String check = i_address.toUpperCase();
        return (check.contains(".XML") && check.lastIndexOf(".XML") == check.length() - 4);
    }

    private static MagitRepository deserializeFromFile(File in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(MagitRepository.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (MagitRepository) u.unmarshal(in);
    }

    private String getTime(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        return sdf1.format(sdf.parse(time));
    }

    static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        return sdf.format(new Date());
    }

    private void makeDirectories(String location) {
        magitPath = location + "\\.magit";

        new File(String.format("%s\\objects", magitPath)).mkdirs();
        new File(String.format("%s\\branches", magitPath)).mkdirs();

        objectPath = String.format("%s\\objects\\", magitPath);
        branchPath = String.format("%s\\branches\\", magitPath);
    }

    private static String unZip(String filePath, File destDir) throws IOException {

        File newFile = null;

        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ZipEntry zipEntry = null;
        try {
            zipEntry = zis.getNextEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (zipEntry != null) {
            try {
                newFile = newFile(destDir, zipEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(newFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int len = 0;
            while (true) {
                try {
                    if (!((len = zis.read(buffer)) > 0)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(buffer, 0, len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                zipEntry = zis.getNextEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile.getName();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static String readFromFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    private List<Item> scanWCRec(File file, String path) throws IOException {
        List<Item> itemsArray = new ArrayList<>();
        File[] filesInDir = new File(file.getPath()).listFiles();
        for (File currFile : filesInDir) {
            if (!currFile.isDirectory()) { //blob
                itemsArray.add(createBlobFromFile(currFile, path));
            } else if (!currFile.getName().equalsIgnoreCase(".magit")) { // directory and not .magit
                itemsArray.addAll(scanWCRec(currFile, currFile.getPath()));
            }
        }
        return itemsArray;
    }

    public String ShowWorkingCopyStatus() throws IOException {
        HashMap<String, String> wcMap = new HashMap<>();
        HashMap<String, String> committedMap = new HashMap<>();
        File currWC = new File(this.location);
        boolean flagChange = false;
        StringBuilder res = new StringBuilder();
        List<Item> currWCItems = scanWCRec(currWC, location);
        if (currWCItems.size() > 0) {
            Collections.sort(currWCItems, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    String compareName = (i1).getName();
                    return compareName.compareTo(i2.getName());
                }
            });
        }
        for (Item WCItem : currWCItems) {
            String fPath = WCItem.getPath();
            String sha1 = WCItem.getSha1();
            wcMap.put(fPath, sha1);
        }
        List<Item> currycombedItems = getItemsListFromFolder(this.getActiveBranch().getpCommit().getRootFolder());
        if (currycombedItems.size() > 0) {
            Collections.sort(currycombedItems, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    String compareName = (i1).getName();
                    return compareName.compareTo(i2.getName());
                }
            });
        }
        for (Item committedItem : currycombedItems) {
            String fPath = committedItem.getPath();
            String sha1 = committedItem.getSha1();
            committedMap.put(fPath, sha1);
        }
        for (Map.Entry pair : wcMap.entrySet()) {
            if (wcMap.containsKey(pair.getKey())) {
                if (!committedMap.containsKey(pair.getKey())) {
                    // System.out.println("New file : " + pair.getKey());
                    res.append("New file : " + pair.getKey() + "\n");
                    flagChange = true;
                } else // if(committedMap.containsKey((pair.getKey())))
                {
                    if (!committedMap.containsValue(pair.getValue())) {
                        //  System.out.println("Modified : " + pair.getKey());
                        res.append("Modified : " + pair.getKey() + "\n");
                        flagChange = true;
                    }
                }
            }
        }
        for (Map.Entry pair : committedMap.entrySet()) {
            if (!wcMap.containsKey(pair.getKey())) {
                //      System.out.println("Deleted : " + pair.getKey());
                res.append("Deleted : " + pair.getKey() + "\n");
                flagChange = true;
            }
        }
        if (!flagChange) {
            //   System.out.println("No changes!");
            res.append("No changes! \n");
        }
        return res.toString();
    }

    public Blob createBlobFromFile(File file, String location) throws IOException {
        BasicFileAttributes attr = null;
        String name = file.getName();
        Path path = Paths.get(file.getPath());
        String createdDate;
        String content = readFromFile(file.getPath());

        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
            createdDate = attr.creationTime().toString();

        } catch (IOException ex) {
            throw ex;
        }

        Blob blob = new Blob(name, Item.Type.BLOB, this.user, createdDate, content);
        blob.setPath(location + "\\" + blob.getName());

        return blob;
    }

    private List<Item> getItemsListFromFolder(Folder folder) {
        List<Item> itemsList = new ArrayList<>();
        for (Item item : folder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase("Folder")) {
                itemsList.addAll(getItemsListFromFolder((Folder) item));
            } else {
                itemsList.add(item);
            }
        }
        return itemsList;
    }

    public void DoCommit(String commitDescription) throws Exception {
        File file = new File(this.location);
        Folder newFolder;
        newFolder = createCommittedWC(file);
        newFolder.setRoot(true);
        Commit commit = new Commit(newFolder, commitDescription, this.user, getTime(), sha1Hex(activeBranch.getpCommit().toString()));
        commit.setPrecedingCommit(sha1Hex(this.getActiveBranch().getpCommit().toString()));
        if (sha1Hex(newFolder.toString()).equalsIgnoreCase(sha1Hex(activeBranch.getpCommit().getRootFolder().toString()))) {
            throw new Exception("There is no changes since the last commit.\n");
        }
        commit.deleteEmptyFoldersInCommitRec(commit.getRootFolder());
        commit.zipNewItems(commit.getRootFolder(), location + "\\.magit\\objects");
        commit.zipAnItem(location + "\\.magit\\objects");
        commitsList.add(commit);
        this.activeBranch.setpCommit(commit);
        Utility.writeToFile(sha1Hex(this.activeBranch.getpCommit().toString()), branchPath + this.activeBranch.getName() + ".txt");
    }

    private Folder createCommittedWC(File fileWCPath) throws IOException, ParseException {
        File[] filesInDir = new File(fileWCPath.getPath()).listFiles();
        File[] filesInObjects;
        String content;
        Blob newBlob;
        Folder newFolder = null, tempFolder, resFolder;
        boolean blobFound = false, folderFound = false;

        List<Item> ItemsArray = new ArrayList<>();
        for (File file : filesInDir) {
            blobFound = false;
            folderFound = false;
            if (!file.isDirectory()) {
                content = readFromFile(file.getPath());
                filesInObjects = new File(this.location + "\\.magit\\objects").listFiles();
                for (File fileObject : filesInObjects) {
                    if (fileObject.getName().equalsIgnoreCase(sha1Hex(content))) {
                        blobFound = true;
                    }
                }
                if (!blobFound) {
                    newBlob = new Blob(file.getName(), Item.Type.BLOB, user, getTime(), content);
                    newBlob.setPath(fileWCPath + "\\" + file.getName());
                    ItemsArray.add(newBlob);
                } else if (blobFound) {
                    newBlob = findBlobInRepository(file.getPath(),sha1Hex(content));
                    ItemsArray.add(newBlob);
                }
            } else if (file.isDirectory() && !file.getName().equalsIgnoreCase(".magit")) {
                {
                    resFolder = createCommittedWC(file);
                    if (resFolder != null) {
                        ItemsArray.add(resFolder);
                    }
                }
            }
        }

        ItemsArray.sort(new Comparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                String compareName = (i1).getName();
                return compareName.compareTo(i2.getName());
            }
        });

        if (ItemsArray.size() == 0) {
            newFolder = null;
        }

        else {
            tempFolder = new Folder(ItemsArray);
            filesInObjects = new File(this.location + "\\.magit\\objects").listFiles();
            for (File fileObject : filesInObjects) {
                if (fileObject.getName().equalsIgnoreCase(sha1Hex(tempFolder.toString()))) {
                    folderFound = true;
                }
            }
            if (folderFound) {
                newFolder = findFolderInRepository(sha1Hex(tempFolder.toString()), fileWCPath.getPath());
            }
            if (!folderFound || newFolder == null) {
                newFolder = new Folder(fileWCPath.getName(), "", Item.Type.FOLDER, this.user, getTime());
                newFolder.setAItemsArray(ItemsArray);
                newFolder.setPath(fileWCPath.getPath());
                newFolder.setSha1(sha1Hex(newFolder.toString()));
            }
        }
        return newFolder;
    }

    private Folder findFolderInRoot(Folder folder, String Path, String sha1) {
        Folder resFol = null;
        if (folder.getPath().equalsIgnoreCase(Path) && sha1.equalsIgnoreCase(sha1Hex(folder.toString()))) {
            return folder;
        }
        for (Item item : folder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase(Item.Type.FOLDER.toString())) {
                if (item.getPath().equalsIgnoreCase(Path) && sha1.equalsIgnoreCase(sha1Hex(item.toString()))) {
                    return (Folder) item;
                }
                resFol = findFolderInRoot((Folder) item, Path, sha1);

            }
        }
        return resFol;
    }


    private Folder findFolderInRepository(String sha1, String Path) {
        Folder currFolder = null;
        for (Commit commit : commitsList) {
            currFolder = findFolderInRoot(commit.getRootFolder(), Path, sha1);
            if (currFolder != null) {
                return currFolder;
            }
        }
        return currFolder;
    }

    private Blob findBlobInRepository(String Path, String sha1) {
        Blob blob = null;
        for (Commit commit : commitsList) {
            blob = findBLobInRoot(commit.getRootFolder(), Path, sha1);
            if (blob != null) {
                return blob;
            }
        }
        return blob;
    }

    private Blob findBLobInRoot(Folder folder, String Path, String sha1) {
        Blob resBlobx = null;
        for (Item item : folder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase(Item.Type.BLOB.toString())) {
                if (item.getPath().equalsIgnoreCase(Path) && sha1.equalsIgnoreCase(item.getSha1())) {
                    return (Blob) item;
                }
            } else {
                resBlobx = findBLobInRoot((Folder) item, Path, sha1);
            }
        }
        return resBlobx;
    }

    public static boolean checkIfSha1Exists(String sha1) {
        String pathObjects = magitPath + "\\objects";
        File[] filesObjects = new File(pathObjects).listFiles();

        if (filesObjects.length > 0) {
            for (File file : filesObjects) {
                if (file.getName().equalsIgnoreCase(sha1))
                    return true;
            }
        }
        return false;
    }

    public boolean merge(String branchNameToMerge) throws Exception {
        Branch branchToMerge;
        Commit ancestorCommit;
        try {
            branchToMerge = findBranchByName(branchNameToMerge);
            if (branchToMerge == null || branchNameToMerge == "") {
                throw new Exception("branch was not found");
            }
        } catch (Exception ex) {
            throw ex;
        }

        Boolean isThereOpenChanges = true;
        File file = new File(location);
        Folder newFolder = createCommittedWC(file);
        if (sha1Hex(newFolder.toString()).equalsIgnoreCase(sha1Hex(activeBranch.getpCommit().getRootFolder().toString()))) {
            isThereOpenChanges = false;
        }
        if (isThereOpenChanges) {
            return true;
        }
        try {
            ancestorCommit = findAncestorFather(branchToMerge.getpCommit());
            List<Item> headBranchChanges = compareToAncestorFather(this.getActiveBranch().getpCommit(), ancestorCommit);
            List<Item> branchToMergeChanges = compareToAncestorFather(branchToMerge.getpCommit(), ancestorCommit);
            Commit resCommit = ancestorCommit;
            Commit commitAfterMerge = handleConflicts(headBranchChanges, branchToMergeChanges, resCommit);
            commitsList.add(commitAfterMerge);
            Utility.writeToFile(sha1Hex(this.activeBranch.getpCommit().toString()), branchPath + this.activeBranch.getName() + ".txt");
            activeBranch.setpCommit(commitAfterMerge);
        } catch (Exception ex) {
            throw new Exception("Could not find commit for merge!");   }
        deleteNotMagitDir();
        CreateWC();

        return false;
    }

    private Branch findBranchByName(String branchNameToMerge) {
        for (Branch branch : branchesList) {
            if (branchNameToMerge.equalsIgnoreCase(branch.getName())) {
                return branch;
            }
        }
        return null;
    }

    public List<Item> compareToAncestorFather(Commit commitToCompare, Commit ancestorCommit) {
        HashMap<String, String> commitToCompareMap = new HashMap<>();
        HashMap<String, String> ancestorMap = new HashMap<>();
        boolean flagChange = false;
        List<Item> resList = new ArrayList<>();
        List<Item> commitToCompareItems = getItemsListFromFolder(commitToCompare.getRootFolder());
        if (commitToCompareItems.size()>1) {
            Collections.sort(commitToCompareItems, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    String compareName = (i1).getName();
                    return compareName.compareTo(i2.getName());
                }
            });}
        List<Item> ancestorCommitItems = getItemsListFromFolder(ancestorCommit.getRootFolder());
        if (ancestorCommitItems.size()>1) {
            Collections.sort(ancestorCommitItems, new Comparator<Item>() {
                @Override
                public int compare(Item i1, Item i2) {
                    String compareName = (i1).getName();
                    return compareName.compareTo(i2.getName());
                }
            });
        }
        for (Item commitToCompareItem : commitToCompareItems) {
            String fPath = commitToCompareItem.getPath();
            String sha1 = commitToCompareItem.getSha1();
            commitToCompareMap.put(fPath, sha1);
        }
        for (Item ancestorCommitItem : ancestorCommitItems) {
            String fPath = ancestorCommitItem.getPath();
            String sha1 = ancestorCommitItem.getSha1();
            ancestorMap.put(fPath, sha1);
        }

        for (Map.Entry pair : commitToCompareMap.entrySet()) {
            if (commitToCompareMap.containsKey(pair.getKey())) {
                if (!ancestorMap.containsKey(pair.getKey())) {
                    Blob blob = findBlobInRootFolder(commitToCompare.getRootFolder(), pair.getKey().toString());
                    blob.setTypeOfChangeset(Item.TypeOfChangeset.NEW);
                    resList.add(blob);  //verify pair.getKey().toString()
                    //  System.out.println("New file : " + pair.getKey());
                    flagChange = true;
                } else // if(committedMap.containsKey((pair.getKey())))
                {
                    if (!ancestorMap.containsValue(pair.getValue())) {
                        Blob blob = findBlobInRootFolder(commitToCompare.getRootFolder(), pair.getKey().toString());
                        blob.setTypeOfChangeset(Item.TypeOfChangeset.MODIFIED);
                        resList.add(blob);
                        //  System.out.println("Modified : " + pair.getKey());
                        flagChange = true;
                    }
                }
            }
        }
        for (Map.Entry pair : ancestorMap.entrySet()) {
            if (!commitToCompareMap.containsKey(pair.getKey())) {
                Blob blob = findBlobInRootFolder(ancestorCommit.getRootFolder(), pair.getKey().toString());
                blob.setTypeOfChangeset(Item.TypeOfChangeset.DELETED);
                resList.add(blob);
                //             System.out.println("Deleted : " + pair.getKey());
                flagChange = true;
            }
        }
        if (!flagChange) {
            System.out.println("No changes!");//todo  case one of the commits is same as father commit
            //   return ;
        }
        return resList;
    }
    private Blob findBlobInRootFolder(Folder folder, String Path) {

        for (Item item : folder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase(Item.Type.BLOB.toString())) {
                if (item.getPath().equalsIgnoreCase(Path))
                    return (Blob) item;
            } else {
                return findBlobInRootFolder((Folder) item, Path);
            }
        }
        return null;
    }

    private Commit findAncestorFather(Commit inputCommit) {
        Commit currentCommit = this.activeBranch.getpCommit();
        Commit startingCommit = inputCommit;
        while (currentCommit != null) {
            while (inputCommit != null) {
                if (sha1Hex(currentCommit.getRootFolder().toString()).equalsIgnoreCase(sha1Hex(inputCommit.getRootFolder().toString()))) {
                    return inputCommit;
                }
                inputCommit = findCommitInCommitList(inputCommit.getPrecedingCommit());
            }
            currentCommit = findCommitInCommitList(currentCommit.getPrecedingCommit());
            inputCommit = findCommitInCommitList(startingCommit.getPrecedingCommit());
            startingCommit = inputCommit;
        }
        return null;
    }

    private Commit findCommitInCommitList(String sha1) {

        for (Commit commit : this.commitsList) {
            if (sha1Hex(commit.toString()).equalsIgnoreCase(sha1))
                return commit;
        }
        return null;
    }

    private Commit handleConflicts(List<Item> headBranchChanges, List<Item> branchToMergeChanges, Commit commitAfterMerge) throws IOException {
        Blob headBranchBlob = null;
        Blob branchToMergeBlob = null;
        headBranchChanges.sort(new Comparator<Item>() {
            public int compare(Item i1, Item i2) {
                String comparePath = (i1).getPath();
                return comparePath.compareTo(i2.getPath());
            }
        });
        branchToMergeChanges.sort(new Comparator<Item>() {
            public int compare(Item i1, Item i2) {
                String comparePath = (i1).getPath();
                return comparePath.compareTo(i2.getPath());
            }
        });
        int headBranchChangesIndex = 0, branchToMergeChangesIndex = 0;
        int headBranchChangesSize = headBranchChanges.size(), branchToMergeChangesSize = branchToMergeChanges.size();
        while (headBranchChangesIndex != headBranchChangesSize && branchToMergeChangesIndex != branchToMergeChangesSize) {
            headBranchBlob = (Blob) headBranchChanges.get(headBranchChangesIndex);
            branchToMergeBlob = (Blob) branchToMergeChanges.get(branchToMergeChangesIndex);
            if (headBranchBlob.getPath().compareTo(branchToMergeBlob.getPath()) > 0) {
                commitAfterMerge.updateBlobInCommit(branchToMergeBlob);
                branchToMergeChangesIndex++;
            } else if (headBranchBlob.getPath().compareTo(branchToMergeBlob.getPath()) < 0) {
                commitAfterMerge.updateBlobInCommit(headBranchBlob);
                headBranchChangesIndex++;
            } else {
                Blob blobInAncestorFather = findBlobInRootFolder(commitAfterMerge.getRootFolder(), headBranchBlob.getPath());
                Blob solvedConflictBlob = solveConflicts(headBranchBlob, branchToMergeBlob, blobInAncestorFather);

                solvedConflictBlob.setPath(headBranchBlob.getPath());
                commitAfterMerge.updateBlobInCommit(solvedConflictBlob);
                branchToMergeChangesIndex++;
                headBranchChangesIndex++;
            }
        }

        while (headBranchChangesIndex != headBranchChangesSize) {
            headBranchBlob = (Blob) headBranchChanges.get(headBranchChangesIndex);
            commitAfterMerge.updateBlobInCommit(headBranchBlob);
            headBranchChangesIndex++;
        }
        while (branchToMergeChangesIndex != branchToMergeChangesSize) {
            branchToMergeBlob = (Blob) branchToMergeChanges.get(branchToMergeChangesIndex);
            commitAfterMerge.updateBlobInCommit(branchToMergeBlob);
            branchToMergeChangesIndex++;
        }
        return commitAfterMerge;
    }

    private Blob solveConflicts(Blob headBranchBlob, Blob branchToMergeBlob, Blob blobInAncsterFather) {

        String headBranchBlobChange = headBranchBlob.getTypeOfChangeset().toString();
        String branchToMergeBlobChange = branchToMergeBlob.getTypeOfChangeset().toString();
        if (headBranchBlobChange.equalsIgnoreCase("new")) {
            if (branchToMergeBlobChange.equalsIgnoreCase("new")) {
                System.out.println("The File: " + headBranchBlob.getPath() + " has created in both branches, choose what do you wanna do:");
                System.out.println(headBranchBlob.getPath() + " in the base branch is: " + headBranchBlob.getContent() + "\n-----------------------------------");
                System.out.println(branchToMergeBlob.getPath() + " in the branch to merge is: " + branchToMergeBlob.getContent() + "\n-----------------------------------");
                System.out.println("copy and paste the text you would like to have in the new file : ");
                String content = "new + new ";
                Blob blob = new Blob(headBranchBlob.getName(), headBranchBlob.getType(), user, getTime(), content);
                blob.setTypeOfChangeset(Item.TypeOfChangeset.NEW);
                blob.setPath(headBranchBlob.getPath());
                return blob;
            }
            //todo verify if we can get another conflicts here (new + deleted,new + modified)
        }
        if (headBranchBlobChange.equalsIgnoreCase("deleted"))
        {
            if (branchToMergeBlobChange.equalsIgnoreCase("modified")) {
                System.out.println("The File: " + headBranchBlob.getPath() + " has deleted in head branch, and modified on the other, choose what do you wanna do:");
                System.out.println(blobInAncsterFather.getPath() + " in the mutual commit is: " + blobInAncsterFather.getContent() + "\n-----------------------------------");
                System.out.println(headBranchBlob.getPath() + " in the base branch is: " + headBranchBlob.getContent() + "\n-----------------------------------");
                System.out.println(branchToMergeBlob.getPath() + " in the branch to merge is: " + branchToMergeBlob.getContent() + "\n-----------------------------------");
                System.out.println("copy and paste the text you would like to have in the new file : ");
                String content = "deleted in head + modified in branchhtomerged"; //todo need to get from user the desired file
                //todo if "deleted" chosen need to delete file else need to create and return new blob
                Blob blob = new Blob(headBranchBlob.getName(), headBranchBlob.getType(), user, getTime(), content);
                blob.setTypeOfChangeset(Item.TypeOfChangeset.MODIFIED);
                blob.setPath(headBranchBlob.getPath());

                return blob;
            }
            if (branchToMergeBlobChange.equalsIgnoreCase("deleted")) {
                System.out.println("File deleted in both branches, file will be deleted.");
                return branchToMergeBlob;
            }
        }
        if (headBranchBlobChange.equalsIgnoreCase("modified")) {
            if (branchToMergeBlobChange.equalsIgnoreCase("modified")) {
                System.out.println("The File: " + headBranchBlob.getPath() + " has modified in both branches. ");
                System.out.println(blobInAncsterFather.getPath() + " in the mutual commit is:\n " + blobInAncsterFather.getContent() + "\n-----------------------------------");
                System.out.println(headBranchBlob.getPath() + " in the base branch is:\n " + headBranchBlob.getContent() + "\n-----------------------------------");
                System.out.println(branchToMergeBlob.getPath() + " in the branch to merge is: " + branchToMergeBlob.getContent() + "\n-----------------------------------");
                System.out.println("copy and paste the text you would like to have in the new file : ");
                String content = "modified x2"; //todo need to get from user the desired file
                Blob blob = new Blob(headBranchBlob.getName(), headBranchBlob.getType(), user, getTime(), content);
                blob.setTypeOfChangeset(Item.TypeOfChangeset.MODIFIED);
                blob.setPath(headBranchBlob.getPath());
                return blob;

            }
            if (branchToMergeBlobChange.equalsIgnoreCase("deleted")) {
                System.out.println("The File: " + headBranchBlob.getPath() + " has deleted in  branchtomerge, and modified on the other, choose what do you wanna do:");
                System.out.println(blobInAncsterFather.getPath() + " in the mutual commit is: " + blobInAncsterFather.getContent() + "\n-----------------------------------");
                System.out.println(headBranchBlob.getPath() + " in the base branch is: " + headBranchBlob.getContent() + "\n-----------------------------------");
                System.out.println(branchToMergeBlob.getPath() + " in the branch to merge is: " + branchToMergeBlob.getContent() + "\n-----------------------------------");
                System.out.println("copy and paste the text you would like to have in the new file : ");
                String content = "modified in head + deleted on branchhtomerged"; //todo need to get from user the desired file
                //todo if "deleted" chosen need to delete file else need to create and return new blob
                Blob blob = new Blob(headBranchBlob.getName(), headBranchBlob.getType(), user, getTime(), content);
                blob.setTypeOfChangeset(Item.TypeOfChangeset.MODIFIED);
                return blob;
            }
        }
        return null;
    }

    private void addCommitToCommitList(Commit newCommit){
        if(!this.getCommitsList().contains(newCommit))
            this.getCommitsList().add(newCommit);
    }

    //check directory todo
    public void cloneRepository(String pathRR, String pathLR, String RepositoryName) throws Exception {
        File file = new File(pathLR);
        if (!file.isDirectory()) {
            File NameDir = new File(pathLR);
            NameDir.mkdirs();
        }
        makeDirectories(pathLR);
        String branchFileSource = pathLR + "\\.magit\\branches";
        String branchFileTarget = pathLR + "\\.magit\\branches\\" + RepositoryName;

        File srcDir = new File(pathRR);
        File destDir = new File(pathLR);

        try { // for WC
            FileUtils.copyDirectory(srcDir, destDir);
            File[] filesRepo = new File(branchFileSource).listFiles();
            if (filesRepo.length > 0) {
                for (File fileToCopy : filesRepo) {
                    FileUtils.copyFile(fileToCopy, new File(branchFileTarget, fileToCopy.getName()));
                }
            } else // check
                throw new Exception("The repository that you want to clone from is empty!");

        } catch (IOException e) {
            e.printStackTrace();
        }
        ChangeRepository(pathRR); // for the instance
        setRemoteReference(RepositoryName, pathRR);
        this.location = pathLR;
        this.name = RepositoryName;

    }

    public void fetch() throws Exception {
        String pathRR = this.remote.getLocation() + "\\.magit\\branches";
        String pathLR = this.location + "\\.magit\\branches";
        File[] branchesRR = new File(pathRR).listFiles();

        for (File branch : branchesRR) {
            Path p = Paths.get(branch.getPath());
            File tmpFile = new File(pathLR + "\\" + p.getFileName().toString());
            if (!tmpFile.exists()) { //add new branch
                copyAllSubTreeToLR(p.getFileName().toString());
            } else { //the branch is exists
                calculateAndCopyToLR(p.getFileName().toString());
            }
        }
    }

    private void copyAllSubTreeToLR(String name) throws Exception {
        Branch newBranch;
        String trackingAfter = null;
        boolean isTracking, isRemote;
        String unzippedfiles = magitPath + "\\unzippedfiles";
        File unZipFilesDir = new File(unzippedfiles);
        File tmpFile = new File(this.remote.getLocation() + "\\.magit\\branches\\" + name);
        String pathLR = this.location + "\\.magit\\branches\\";

        //copy the instance:
        String dataFromBranch = readFromFile(tmpFile.getPath());
        String[] parts = dataFromBranch.split(", ");
        String sha1 = parts[0];
        isRemote = parts[1].equalsIgnoreCase("TRUE");
        isTracking = parts[2].equalsIgnoreCase("TRUE");
        if (isTracking)
            trackingAfter = parts[3];

        newBranch = new Branch(tmpFile.getName(), null, false, isRemote, isTracking, trackingAfter);
        findNextCommit(sha1, newBranch, this.remote.getLocation(), unZipFilesDir, true);
        this.branchesList.add(newBranch);
        FileUtils.copyFile(tmpFile, new File(pathLR, tmpFile.getName()));

        //copy the folders in objects
        ZipAllItemInRepository(this.location + "\\.magit\\objects"); // ???
    }

    private void calculateAndCopyToLR(String name) throws Exception {
        Branch newBranch;
        String unzippedfiles = magitPath + "\\unzippedfiles";
        File unZipFilesDir = new File(unzippedfiles);
        String pathRR = this.remote.getLocation() + "\\.magit\\branches\\";
        String pathLR = this.location + "\\.magit\\branches\\";

        File tmpFileRR = new File(pathRR + name);
        File tmpFileLR = new File(pathLR + name);
        String[] partsRR = readFromFile(tmpFileRR.getPath()).split(", ");
        String[] partsLR = readFromFile(tmpFileLR.getPath()).split(", ");
        File tmpFile = new File(pathRR + name);

        String dataFromBranch = readFromFile(tmpFile.getPath());
        String[] parts = dataFromBranch.split(", ");
        String sha1 = parts[0];

        if (partsLR[0].equalsIgnoreCase(partsRR[0])) // do nothing
            return;
        else {
            newBranch = findBranch(name);
            findNextCommit(sha1, newBranch, this.remote.getLocation(), unZipFilesDir, true);
            CreateSpecificBranchesFile(newBranch, this.location + "\\.magit\\branches");
        }
        ZipAllItemInRepository(this.location + "\\.magit\\objects"); // ???
    }

    private void CreateSpecificBranchesFile(Branch branch, String path) throws IOException {
        String delimiter = ", ";
        String branchToString = sha1Hex(branch.getpCommit().toString()) + delimiter + branch.isRemote() +
                delimiter + branch.isTracking() + delimiter + branch.getTrackingAfter();

        Files.deleteIfExists(Paths.get(path + branch.getName() + ".txt"));

        File file = new File(path + branch.getName() + ".txt");
        file.createNewFile();
        Utility.writeToFile(branchToString, path + branch.getName() + ".txt");
    }

    public Commit showCommitData(){
        return this.getActiveBranch().getpCommit();
    }
}
