package Objects;
import System.User;

abstract public class Item implements IZipable {
    public enum TypeOfChangeset {
        DELETED,
        NEW,
        MODIFIED,
    }

    public enum Type {
        BLOB,
        FOLDER,
    }
    protected String name;
    protected String sha1;
    protected Type type;
    protected TypeOfChangeset typeOfChangeset;
    protected User lastUpdater;
    protected String lastUpdateDate;
    protected String path;

    public Item(String name, String sha1, Item.Type type,  User lastUpdater, String lastUpdateDate) {
        this.name = name;
        this.lastUpdater = lastUpdater;
        this.lastUpdateDate = lastUpdateDate;
        this.type = type;
        this.sha1 = sha1;
    }

    public Item(){}

    public Type getType() { return type; }
    public String getName() { return name; }
    public String getSha1() { return sha1; }
    public String getPath() { return path; }
    public void setPath(String value){ path=value;}
    public void setTypeOfChangeset(TypeOfChangeset typeOfChangeset){ this.typeOfChangeset=typeOfChangeset;}
    public Item.TypeOfChangeset getTypeOfChangeset() {return typeOfChangeset;}
    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        String delimiter = ",";

        res.append(name + delimiter + sha1 + delimiter + type.toString() + delimiter + lastUpdater.toString() + delimiter + lastUpdateDate);

        return res.toString();
    }

    public String LineInZip() {
        StringBuilder res = new StringBuilder();
        String delimiter = ",";

        res.append(name + delimiter + sha1 + delimiter + type.toString() + delimiter + lastUpdater.toString() + delimiter + lastUpdateDate);

        return res.toString();
    }

    public void showInfo() {
        System.out.println("The file path is: " + path);
        System.out.println("The file type is: "+type);
        System.out.println("The file sha1 is: "+sha1);
        System.out.println("Last updater is: "+lastUpdater.getName());
        System.out.println("Last updated in: "+lastUpdateDate+"\n");
        System.out.println("------------------------>");
    }
}