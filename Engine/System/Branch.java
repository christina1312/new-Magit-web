
package System;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

public class Branch {
    private String name;
    private Commit pCommit;
    private boolean isActive ;
    private boolean isRemote;
    private boolean isTracking;
    private String trackingAfter;

    public Commit getpCommit() {
        return pCommit;
    }
    public String getName() { return name; }
    public void setpCommit(Commit newCommit) { pCommit=newCommit; }
    public boolean getIsActive() { return isActive; }

    public Branch(String name, Commit pCommit, boolean isActive, boolean isRemote, boolean isTracking, String trackingAfter) {
        this.name = name;
        this.pCommit = pCommit;
        this.isActive = isActive;
        this.isRemote = isRemote;
        this.isTracking = isTracking;
        this.trackingAfter = trackingAfter;
    }

    public Branch(String name, Commit pCommit, boolean isActive) {
        this.name = name;
        this.pCommit = pCommit;
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        String delimiter = ", ";
        res.append(name + delimiter + pCommit + delimiter
                + isActive+ delimiter);
        return res.toString();
    }
//    public void showInfo() {
//        System.out.println("The branch name is: "+name);
//        System.out.println("The pointed commit sha1 is: "+sha1Hex(this.getpCommit().toString()));
//        System.out.println("The message of the pointed commit: "+pCommit.getMessage() + "\n");
//    }


    public String showInfo() {
        return "The branch name is: "+name+ "\n"+
                "The pointed commit sha1 is: "+sha1Hex(this.getpCommit().toString())+ "\n"+
                "The message of the pointed commit: "+pCommit.getMessage() + "\n";
        //System.out.println("The branch name is: "+name);
        //  System.out.println("The pointed commit sha1 is: "+sha1Hex(this.getpCommit().toString()));
        //  System.out.println("The message of the pointed commit: "+pCommit.getMessage() + "\n");
    }

    public boolean isTracking() {
        return isTracking;
    }
    public boolean isRemote() {
        return isRemote;
    }
    public String getTrackingAfter() {
        return trackingAfter;
    }
}