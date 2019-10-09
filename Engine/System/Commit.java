
package System;
import Objects.Blob;
import Objects.Folder;
import Objects.IZipable;
import Objects.Item;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static System.Repository.getTime;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

public class Commit implements IZipable {
    private Folder rootFolder;
    private String precedingCommit;
    private String secondPrecedingCommit;
    private String message;
    private String dateCreated;
    private User user;
    private String id;

    public Commit(Folder rootFolder, String message, User user, String dateCreated, String precedingCommit, String secondPrecedingCommit) {
        this.rootFolder = rootFolder;
        this.message = message;
        this.user = user;
        this.dateCreated = dateCreated;
        this.precedingCommit = precedingCommit;
        this.secondPrecedingCommit = secondPrecedingCommit;
    }

    public Commit(Folder rootFolder, String message, User user, String dateCreated, String precedingCommit, String id, String secondPrecedingCommit) {
        this(rootFolder, message, user, dateCreated, precedingCommit, secondPrecedingCommit);
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setDateCreated(String date) {
        this.dateCreated = date;
    }

    public void setPrecedingCommit(String precedingCommit) {
        this.precedingCommit = precedingCommit;
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        String delimiter = ",";
        res.append(sha1Hex(this.rootFolder.toString()) + delimiter + precedingCommit + delimiter + message + delimiter
                + dateCreated + delimiter + user.toString()+delimiter + secondPrecedingCommit);

        return res.toString();
    }

    public String sha1ToString() {
        StringBuilder res = new StringBuilder();
        res.append(rootFolder.getSha1());

        return res.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrecedingCommit() {
        return precedingCommit;
    }
    public String getSecondPrecedingCommit() {
        return secondPrecedingCommit;
    }

    @Override
    public void zipAnItem(String pathForSavingFile) throws IOException {
        //Commit
        String tempStr = this.toString();
        String fileName = sha1Hex(this.toString());
        File temp = File.createTempFile(sha1Hex(tempStr), ".txt");
        temp.deleteOnExit();
        Utility.writeToFile(tempStr, temp.getPath());
        try {
                File i_FileForZip = new File(temp.getAbsolutePath());
                File fileToZip = new File(i_FileForZip.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(pathForSavingFile + "\\" + fileName);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.close();
                fis.close();
                fos.close();
        } catch (Exception ex) {
          // throw new Exception("Fail to zip a commit");
        }
    }

    public void showInfo() {
        System.out.println("The commit sha1 is: " + sha1Hex(this.toString()));
        System.out.println("The message of the commit: " + message);
        System.out.println("Date of creation: " + dateCreated);
        System.out.println("Created by: " + user.toString());
        System.out.println("------------------------>");
    }

    public void zipNewItems(Folder folder, String path) throws IOException {
        if (folder.isFolderRoot()) {
            folder.zipAnItem(path);
        }
        for (Item item : folder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase("Folder")) {
                zipNewItems((Folder) item, path);
                item.zipAnItem(path);
            } else {
                item.zipAnItem(path);
            }

        }
    }

    public void updateBlobInCommit(Blob blob) {
        String path = blob.getPath();
        String[] partsForPath = path.split("\\\\");
        int indexToStart = 0;
        Folder currFolder = this.rootFolder;
        for (int i = 0; i < partsForPath.length; i++) {
            if (partsForPath[i].equalsIgnoreCase(currFolder.getName())) {
                indexToStart = i + 1;
                break;
            }
        }
        createBlobInCommitRec(currFolder, partsForPath, indexToStart, blob);
    }

    private void createBlobInCommitRec(Folder currFolder, String[] partsForPath, int indexToStart, Blob blob) {
        if (indexToStart == partsForPath.length - 1) {
            if (blob.getTypeOfChangeset().toString().equalsIgnoreCase("new")) {
                currFolder.addItemToItemList(blob);
            }
            if (blob.getTypeOfChangeset().toString().equalsIgnoreCase("modified")) {
                currFolder.removeItemFromItemList(blob);
                currFolder.addItemToItemList(blob);
            }
            if (blob.getTypeOfChangeset().toString().equalsIgnoreCase("deleted")) {
                currFolder.removeItemFromItemList(blob);
                //todo delete empty folder?
            }
            return;
        }
        boolean isFolderFound = false;
        for (Item item : currFolder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase("Folder") && item.getName().equalsIgnoreCase(partsForPath[indexToStart])) {
                currFolder = (Folder) item;
                isFolderFound = true;
            }
        }
        if (!isFolderFound) {
            Folder folder = new Folder(partsForPath[indexToStart + 1], "", Item.Type.FOLDER, user, getTime());//Todo  צריך ליצור תיקייה חדשה
            currFolder.addItemToItemList(folder);
            currFolder = folder;
        }
        createBlobInCommitRec(currFolder, partsForPath, indexToStart + 1, blob);
        currFolder.setSha1(sha1Hex(currFolder.toString()));
    }


    public boolean deleteEmptyFoldersInCommitRec(Folder folder) {
        if (folder.getItemsArray().size() == 0) {
            return true;
        }

        for (Item item : folder.getItemsArray()) {
            if (item.getType().toString().equalsIgnoreCase(Item.Type.FOLDER.toString())) {
                if (deleteEmptyFoldersInCommitRec((Folder) item)) {
                    folder.removeFolderFromItemList((Folder) item);
                }
            }
        }
        return false;
    }

    public String getSha1(){ return sha1Hex(this.toString());}

    public String getDateCreated(){ return this.dateCreated;}

    public String getCreatedBy(){ return this.user.toString();}

    public  void fixItemsPaths(String wrongPath, String correctPath)
    {
        this.rootFolder.fixItemsPathes(wrongPath,correctPath);
        this.rootFolder.changePath(wrongPath,correctPath);

    }
}




