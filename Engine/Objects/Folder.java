package Objects;

import System.User;
import System.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

public class Folder extends Item {

    private boolean isRoot = false;
    private List<Item> ItemsArray = new ArrayList<>();

    public void setAItemsArray(List<Item> newList) {
        ItemsArray = newList;
    }


    public Folder(String name, String sha1, Item.Type type, User lastUpdater, String lastUpdateDate) {
        super(name, sha1, type, lastUpdater, lastUpdateDate);
    }

    public Folder(List<Item> ItemsArray) {
        super();
        this.setAItemsArray(ItemsArray);
        this.type = Type.FOLDER;
        this.sha1 = sha1Hex(this.toString());
    }

    public void setRoot(boolean value) {
        isRoot = value;
    }

    public void setName(String value) {
        name = value;
    }

    public void setlastUpdateDate(String value) {
        lastUpdateDate = value;
    }

    public void setlastUpdater(User value) {
        lastUpdater = value;
    }

    public void setSha1(String value) {
        sha1 = value;
    }

    @Override
    public void zipAnItem(String pathForSavingFile) throws IOException {
        //Folder
        String content = this.toString();
        String fileName = sha1Hex(content);
        File temp = File.createTempFile(sha1, ".txt");
        Utility.writeToFile(content, temp.getPath());
        temp.deleteOnExit();
        try {
            //    if (!checkIfSha1Exists(sha1))
            {
                File i_FileForZip=new File(temp.getAbsolutePath());
                File fileToZip = new File(i_FileForZip.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(pathForSavingFile + "\\" + fileName);
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0)
                {
                    zipOut.write(bytes, 0, length);
                }

                zipOut.close();
                fis.close();
                fos.close();
            }
        } catch (Exception ex) {
            try {
                throw new Exception("Fail to zip a Folder");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public String toString() {
        String res = "";
        String delimiter = "\r\n";

        for (Item item : ItemsArray) {
            res += item.LineInZip();
            res += delimiter;

        }
        return res.trim();
    }


    public List<Item> getItemsArray() {
        return ItemsArray;
    }
    public boolean isFolderRoot() {
        return isRoot;
    }


    public String getName() {
        return this.name;
    }
    public void showInfo() {
        if(!this.isRoot) {
            super.showInfo();
        }
    }
    public void addItemToItemList(Item item)
    {
        this.ItemsArray.add(item);
    }
    public void removeItemFromItemList(Blob blob)
    {
        this.ItemsArray.remove(blob);// todo check if item removed
    }
    public void removeFolderFromItemList(Folder folder)
    {
        this.ItemsArray.remove(folder);// todo check if item removed
    }

}
