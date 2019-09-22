package Objects;

import System.User;
import System.Utility;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

public class Blob extends Item {

    private String content;
    public Blob(String name, Item.Type type,  User lastUpdater, String lastUpdateDate, String content){
        super(name, sha1Hex(content.trim()), type, lastUpdater, lastUpdateDate);
        this.content=content.trim();
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public void zipAnItem(String pathForSavingFile) throws IOException {
        //BLob
        String fileName = sha1Hex(content);
        File temp = File.createTempFile(sha1Hex(content), ".txt");
        Utility.writeToFile(content, temp.getPath());
        temp.deleteOnExit();
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



    public void createBlobOnWC(String location) throws IOException {
        Utility.writeToFile(content, location);
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content=content;
    }
}
