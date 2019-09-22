package MainScene;
import System.BasicMAGitManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import System.Commit;

public class MainSceneController {

    private Stage m_Stage;
    private BasicMAGitManager m_Logic;
    private Parent m_root;

    private Task<Boolean> fileLoadTask;
    boolean m_isGameLoaded;
    private File m_file;

    private final static int SLEEP_PERIOD = 100;
    private final static int SLEEP_INTERVAL = 20;

    @FXML
    private Button ChangeUserNameButton;
    @FXML
    private Button LoadNewRepositoryFromXMLFileButton;
    @FXML
    private Button InitializeNewRepositoryButton;
    @FXML
    private Button ChangeRepositoryButton;
    @FXML
    private Button ExportRepositoryForNewXMLFileButton;
    @FXML
    private Button ShowCurrentCommitFileSystemButton;
    @FXML
    private Button DoCommitButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button CloneButton;
    @FXML
    private Button FetchButton;
    @FXML
    private Button PullButton;
    @FXML
    private Button PushButton;
    @FXML
    private Button MergeButton;
    @FXML
    private Button checkConflictsButton;
    @FXML
    private Button ShowBranchesFileSystemButton;
    @FXML
    private Button CreateNewBranchButton;
    @FXML
    private Button DeleteBranchButton;
    @FXML
    private Button ResetBranchLocationButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button saveNewCommitMessage;
    @FXML
    private ProgressBar ProgressBarXml;
    @FXML
    private Label InfoLabel;
    @FXML
    private Label branchMergeLabel;
    @FXML
    private Label InfoCollaborationLabel;
    @FXML
    private Label theBranchesLabel;
    @FXML
    private Label workingCopyStatusLabel;
    @FXML
    private Label branchToDeteleLabel;
    @FXML
    private Label newCommitLabel;
    @FXML
    private TextField newCommitMessageText;
    @FXML
    private TextField branchMergeText;
    @FXML
    private TextField branchToDeleteText;
    @FXML
    private TextField inputTextField;
    @FXML
    private Label progressPercentLabel;
    @FXML
    private Label mergeLabel;
    @FXML
    private Label thiersLabel;
    @FXML
    private Label ouersLabel;
    @FXML
    private Label originLabel;
    @FXML
    private Label afterMergeLabel;
    @FXML
    private Label branchPointtedCommitLabel;
    @FXML
    private TextField commitSha1Text;
    @FXML
    private TextField commitmessageText;
    @FXML
    private TextField commitDateOfCreationText;
    @FXML
    private TextField commitCreatedByText;
    @FXML
    private TextField RepositoryUserNameText;
    @FXML
    private TextField RepositoryNameText;
    @FXML
    private TextField branchPointedCommitText;
    @FXML
    private TextArea workingCopyText;
    @FXML
    private TextArea branchText;
    @FXML
    private GridPane commitGridPane;
    @FXML
    private GridPane repositoryDetailsGridPane;
    @FXML
    private ScrollPane workingCopyScroll;
    @FXML
    private ScrollPane branchScroll;
    @FXML
    private ScrollPane ouersScroll;
    @FXML
    private ScrollPane theirsScroll;
    @FXML
    private ScrollPane originScroll;
    @FXML
    private ScrollPane afterMergeScroll;

    private BasicMAGitManager logic;

    public void SetSage(Stage i_stg) {
        m_Stage = i_stg;
    }

    public void MainSceneController() { }

    @FXML
    public void initialize() {
        logic = new BasicMAGitManager();
        turnOffLabelsSettingTab();
        turnOffProgressSettingTab();
        turnOffGridsCommitTab();
        turnOffLabelsCommitTab();
        turnOffWCCommitTab();
        turnOffDeleteLabelsBranchTab();
        turnOffDeleteScrollBranchTab();
        turnOffMergeLabels();
        InfoCollaborationLabel.setVisible(false);
    }

    public void onLoadNewRepositoryFromXMLFile() {
        turnOffLabelsSettingTab();
        turnOnProgressSettingTab();

        ProgressBarXml.progressProperty().unbind();
        ProgressBarXml.setProgress(0);

        FileChooser fileChooser = new FileChooser();

        //the window of the file chooser
        fileChooser.setTitle("Search for a XML file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML files", "*.xml"));

        File fileIn = fileChooser.showOpenDialog(m_Stage);
        if (fileIn != null) {
            fileLoadTask = LoadNewRepositoryFromXMLFileTask(fileIn);
            bindProgress();
        } else {
            InfoLabel.setText("Can not load file!");
            turnOffProgressSettingTab();
        }
        turnOffLabelsSettingTab();
    }

    public void onChangeRepository() {
        turnOffLabelsSettingTab();
        turnOnProgressSettingTab();

        ProgressBarXml.progressProperty().unbind();
        ProgressBarXml.setProgress(0);

        DirectoryChooser fileChooser = new DirectoryChooser();

        //the window of the file chooser
        fileChooser.setTitle("Search for repository");

        File fileIn = fileChooser.showDialog(m_Stage);
        if (fileIn != null) {
            fileLoadTask = ChangeRepositoryTask(fileIn);
            bindProgress();
        } else {
            InfoLabel.setText("Can not change repository!");
            turnOffProgressSettingTab();
        }
        turnOffLabelsSettingTab();
    }

    public void onCreateNewRepository() {
        turnOnLabelsSettingTab();
        saveButton.setText("Create");
        InfoLabel.setText("Please enter name for the new repository: ");

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    logic.createNewRepository(inputTextField.getText());
                    turnOffLabelsSettingTab();
                    m_isGameLoaded = true;
                    ChangeUserNameButton.setDisable(false);
                    InfoLabel.setVisible(true);
                    InfoLabel.setText("The new repository has been created successfully!");
                } catch (Exception ex) {
                    turnOffLabelsSettingTab();
                    InfoLabel.setVisible(true);
                    InfoLabel.setText("Error: " + ex + "\n An error occurred while trying to creat a new repository!");
                }
            }
        };
        saveButton.setOnAction(event);
    }

    private Task<Boolean> LoadNewRepositoryFromXMLFileTask(File fileIn) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    for (int i = 0; i < SLEEP_INTERVAL; i++) {
                        Thread.sleep(SLEEP_PERIOD);
                        updateMessage("Loading file... " + ((float) i / SLEEP_INTERVAL) * 100 + "%");
                        updateProgress(i + 1, SLEEP_INTERVAL);
                    }

                    logic.LoadMAGit(fileIn.getAbsolutePath());
                    if (AlertPromptDialog.show(m_Stage, "There is repository in the given location. \\n What do you want to do next?", "load") == 0) {
                        logic.DeleteRepositoryAndCreateNew(fileIn.getAbsolutePath());
                    } else { // == 1
                        //the user need to do commit in commit iab!!
                        logic.ChangeRepository(fileIn.getAbsolutePath());
                    }
                    m_isGameLoaded = true;
                    ChangeUserNameButton.setDisable(false);
                } catch (Exception e) {
                    InfoLabel.setVisible(true);
                    String s = e.getMessage();
                    Platform.runLater(() -> InfoLabel.setText("Error : " + s));
                    m_isGameLoaded = false;
                } finally {
                    InfoLabel.setVisible(true);
                    Platform.runLater(() -> ProgressBarXml.setVisible(false));
                    Platform.runLater(() -> progressPercentLabel.setVisible(false));

                    if (m_isGameLoaded) {
                        Platform.runLater(() -> InfoLabel.setText("File was loaded successfully!"));
                    }
                }
                return true;
            }
        };
    }

    private Task<Boolean> ChangeRepositoryTask(File fileIn) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    for (int i = 0; i < SLEEP_INTERVAL; i++) {
                        Thread.sleep(SLEEP_PERIOD);
                        updateMessage("Loading repository... " + ((float) i / SLEEP_INTERVAL) * 100 + "%");
                        updateProgress(i + 1, SLEEP_INTERVAL);
                    }
                    //load the game acoording to the path we get from the user
                    logic.ChangeRepository(fileIn.getAbsolutePath());
                    m_isGameLoaded = true;
                    ChangeUserNameButton.setDisable(false);
                } catch (Exception e) {
                    InfoLabel.setVisible(true);
                    String s = e.getMessage();
                    Platform.runLater(() -> InfoLabel.setText("Error : " + s));
                    m_isGameLoaded = false;
                } finally {
                    InfoLabel.setVisible(true);
                    Platform.runLater(() -> ProgressBarXml.setVisible(false));
                    Platform.runLater(() -> progressPercentLabel.setVisible(false));

                    if (m_isGameLoaded) {
                        Platform.runLater(() -> InfoLabel.setText("The repository has been changed successfully!"));
                    }
                }
                return true;
            }
        };
    }

    //todo
    private Task<Boolean> cloneRepositoryTask(File fileIn) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    logic.cloneRepository(fileIn.getAbsolutePath(),"","");
                } catch (Exception e) {
                    InfoCollaborationLabel.setVisible(true);
                    String s = e.getMessage();
                    Platform.runLater(() -> InfoCollaborationLabel.setText("Error : " + s));
                } finally {
                    InfoCollaborationLabel.setVisible(true);

                    if (m_isGameLoaded) {
                        Platform.runLater(() -> InfoCollaborationLabel.setText("Cloned successfully!"));
                    }
                }
                return true;
            }
        };
    }

    private void bindProgress() {
        ProgressBarXml.progressProperty().unbind();
        ProgressBarXml.progressProperty().bind(fileLoadTask.progressProperty());

        progressPercentLabel.textProperty().bind(
                Bindings.concat(
                        Bindings.format(
                                "%.0f",
                                Bindings.multiply(fileLoadTask.progressProperty(),
                                        100)),
                        " %"));

        fileLoadTask.messageProperty().addListener((observable, oldValue, newValue) -> progressPercentLabel.setText(newValue));
        Thread thread = new Thread(fileLoadTask);
        thread.start();
        this.progressPercentLabel.textProperty().unbind();
    }

    public void onChangeUserName() {
        turnOnLabelsSettingTab();
        InfoLabel.setText("Please enter new User name: ");

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    logic.setUserName(inputTextField.getText());
                    turnOffLabelsSettingTab();
                    InfoLabel.setVisible(true);
                    InfoLabel.setText("The user name updated successfully!");
                } catch (Exception ex) {
                    turnOffLabelsSettingTab();
                    InfoLabel.setVisible(true);
                    InfoLabel.setText("An error occurred while trying to update the user name!");
                }
            }
        };
        saveButton.setOnAction(event);
    }

    public void onDoCommit() {
        turnOffGridsCommitTab();
        turnOffWCCommitTab();
        turnOnLabelsCommitTab();
    }

    public void onSaveNewMessage() {
        try {
            turnOffLabelsCommitTab();
            logic.DoCommit(newCommitMessageText.getText());
            newCommitLabel.setVisible(true);
            newCommitLabel.setText("The new commit saved successfully!");
        } catch (Exception ex) {
            turnOffLabelsCommitTab();
            newCommitLabel.setVisible(true);
            newCommitLabel.setText("Error: " + ex.getMessage());
        }
    }

    public void onShowCurrentCommitFileSystem() {
        turnOffWCCommitTab();
        turnOffLabelsCommitTab();
        turnOnGridsCommitTab();
        branchToDeteleLabel.setVisible(false);
        setRepositoryGridPane();
        setCommitGridPane();
    }

    private void setCommitGridPane() {
        Commit newCommit = logic.showCommitData();
        this.commitSha1Text.setText(newCommit.getSha1());
        this.commitmessageText.setText(newCommit.getMessage());
        this.commitDateOfCreationText.setText(newCommit.getDateCreated());
        this.commitCreatedByText.setText(newCommit.getCreatedBy());
    }

    private void setRepositoryGridPane() {
        RepositoryNameText.setText(logic.getRepositoryName());
        RepositoryUserNameText.setText(logic.getUser().toString());
    }

    public void onShowWorkingCopyStatus() throws IOException {
        turnOnWCCommitTab();
        turnOffLabelsCommitTab();
        turnOffGridsCommitTab();
        workingCopyText.setText(logic.ShowWorkingCopyStatus());
    }

    public void onDeleteBranch() {
        turnOnDeleteLabelsBranchTab();
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    turnOffDeleteLabelsBranchTab();
                    logic.DeleteBranch(branchToDeleteText.getText());
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("The branch was deleted successfully!");
                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("Error: " + ex.getMessage());
                }
            }
        };
        deleteButton.setOnAction(event);
    }

    public void onCheckout() {
        turnOnDeleteLabelsBranchTab();
        turnOffDeleteScrollBranchTab();
        setCheckoutButtons();

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    turnOffDeleteLabelsBranchTab();
                    if(logic.CheckoutHeadBranch(branchToDeleteText.getText())){
                        if(AlertPromptDialog.show(m_Stage, "There are uncommitted changes", "checkout")==0){
                            //delete all changes
                            logic.deleteWorkingCopyChanges();
                            logic.CheckoutHeadBranch(branchToDeleteText.getText());
                        }
                        else{ // == 1 : commit
                            //the user need to do commit in commit iab!!
                        }
                    }
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("Checkout successfully!");
                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("Error: " + ex.getMessage());
                }
            }
        };
        deleteButton.setOnAction(event);
    }

    //todo
    public void onResetBranchLocation() {
        turnOnDeleteLabelsBranchTab();
        setResetButtons();

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    if(logic.resetHeadBranch(branchToDeleteText.getText())) {
                        if(AlertPromptDialog.show(m_Stage, "There are uncommitted changes", "reset")==0){
                            //delete all changes
                            logic.deleteWorkingCopyChanges();
                            logic.resetHeadBranch(branchToDeleteText.getText());
                        }
                        else{ // == 1 : commit
                            //the user need to do commit in commit iab!!
                        }
                    }
                    turnOffDeleteLabelsBranchTab();
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("Reset the head branch successfully!");
                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("An error occurred while trying reset the head branch!");
                }
            }
        };
        deleteButton.setOnAction(event);
    }

    public void onClone(){

    }
    public void onFetch(){

    }
    public void onPush(){

    }
    public void onPull(){

    }
    public void onMerge() throws Exception {
//        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
//            public void handle(ActionEvent e) {
//                try {
//                    turnOffDeleteLabelsBranchTab();
//                    if(logic.CheckoutHeadBranch(branchToDeleteText.getText())){
//                        if(AlertPromptDialog.show(m_Stage, "There are uncommitted changes", "checkout")==0){
//                            //delete all changes
//                            logic.deleteWorkingCopyChanges();
//                            logic.CheckoutHeadBranch(branchToDeleteText.getText());
//                        }
//                        else{ // == 1 : commit
//                            //the user need to do commit in commit iab!!
//                        }
//                    }
//                    branchToDeteleLabel.setVisible(true);
//                    branchToDeteleLabel.setText("Checkout successfully!");
//                } catch (Exception ex) {
//                    turnOffDeleteLabelsBranchTab();
//                    branchToDeteleLabel.setVisible(true);
//                    branchToDeteleLabel.setText("Error: " + ex.getMessage());
//                }
//            }
//        };
 //       deleteButton.setOnAction(event);
        try {

            if(logic.merge(branchToDeleteText.getText())){
                        if(AlertPromptDialog.show(m_Stage, "There are uncommitted changes", "checkout")==0){
                            logic.deleteWorkingCopyChanges();
                            logic.CheckoutHeadBranch(branchToDeleteText.getText());
                        }
                        else{
                            //the user need to do commit in commit iab!!
                        }
                    }
            turnOffMergeLabels();
            mergeLabel.setVisible(true);
            mergeLabel.setText("The new branch created successfully!");
        } catch (Exception ex) {
            turnOffMergeLabels();
            mergeLabel.setVisible(true);
            mergeLabel.setText("An error occurred while trying to add new branch!");
        }
    }

    public void onCheckConflicts() {
        try {
            if (logic.merge(branchMergeText.getText())) {
                if (AlertPromptDialog.show(m_Stage, "There is repository in the given location. \\n What do you want to do next?" , "conflict") == 0) {
                    logic.deleteNotMagitDir();
                } else { // == 1 : commit
                    //the user need to do commit in commit iab!!
                }
            }

            MergeButton.setDisable(false);
            turnOnCollebrationLabels();
        } catch (Exception ex) {
            mergeLabel.setVisible(true);
            mergeLabel.setText("Error: " + ex.getMessage());
            branchMergeText.setText("");
        }
    }

    public void onCreateNewBranch() {
        turnOffDeleteScrollBranchTab();
        setAddNewBranchLabels();

        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    logic.CreateNewBranch(branchToDeleteText.getText(), branchPointedCommitText.getText());
                    turnOffDeleteLabelsBranchTab();
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("The new branch created successfully!");
                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
                    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("An error occurred while trying to add new branch!");
                }
            }
        };
        deleteButton.setOnAction(event);
    }

    public void onShowBranchesFileSystem() {
        turnOnScrollBranchTab();
        turnOffDeleteLabelsBranchTab();
        branchText.setText(logic.ShowAllBranchesFileSystem());
    }

    private void turnOnLabelsSettingTab() {
        InfoLabel.setVisible(true);
        inputTextField.setVisible(true);
        saveButton.setVisible(true);
        saveButton.setText("Save");
    }

    private void turnOffLabelsSettingTab() {
        InfoLabel.setVisible(false);
        inputTextField.setVisible(false);
        saveButton.setVisible(false);
    }

    private void turnOffProgressSettingTab() {
        progressPercentLabel.setVisible(false);
        ProgressBarXml.setVisible(false);
    }

    private void turnOnProgressSettingTab() {
        progressPercentLabel.setVisible(true);
        ProgressBarXml.setVisible(true);
    }

    private void turnOnGridsCommitTab() {
        commitGridPane.setVisible(true);
        repositoryDetailsGridPane.setVisible(true);
    }

    private void turnOffGridsCommitTab() {
        commitGridPane.setVisible(false);
        repositoryDetailsGridPane.setVisible(false);
    }

    private void turnOffLabelsCommitTab() {
        newCommitLabel.setVisible(false);
        newCommitMessageText.setVisible(false);
        saveNewCommitMessage.setVisible(false);
    }

    private void turnOnLabelsCommitTab() {
        newCommitLabel.setVisible(true);
        newCommitMessageText.setVisible(true);
        saveNewCommitMessage.setVisible(true);
    }

    private void turnOnDeleteLabelsBranchTab() {
        branchToDeteleLabel.setVisible(true);
        branchToDeteleLabel.setText("Branch to delete :");
        deleteButton.setVisible(true);
        deleteButton.setText("Delete branch !");
        branchToDeleteText.setVisible(true);
        branchToDeleteText.setText("");
        branchPointedCommitText.setVisible(false);
        branchPointtedCommitLabel.setVisible(false);
    }

    private void turnOffDeleteLabelsBranchTab() {
        branchToDeteleLabel.setVisible(false);
        deleteButton.setVisible(false);
        branchToDeleteText.setVisible(false);
        branchPointtedCommitLabel.setVisible(false);
        branchPointedCommitText.setVisible(false);
    }

    private void turnOnScrollBranchTab() {
        theBranchesLabel.setVisible(true);
        branchScroll.setVisible(true);
        branchText.setVisible(true);
    }

    private void turnOffDeleteScrollBranchTab() {
        theBranchesLabel.setVisible(false);
        branchScroll.setVisible(false);
        branchText.setVisible(false);
        branchPointedCommitText.setVisible(false);
        branchPointtedCommitLabel.setVisible(false);
    }

    private void setCheckoutButtons(){
        branchToDeteleLabel.setVisible(true);
        branchToDeteleLabel.setText("The branch name :");
        deleteButton.setVisible(true);
        deleteButton.setText("Checkout");
        branchToDeleteText.setVisible(true);
        branchToDeleteText.setText("");
    }

    private void setResetButtons(){
        branchToDeteleLabel.setVisible(true);
        branchToDeteleLabel.setText("The destination branch(commit):");
        deleteButton.setVisible(true);
        deleteButton.setText("Reset");
        branchToDeleteText.setVisible(true);
        branchToDeleteText.setText("");
    }

    private void turnOnWCCommitTab() {
        workingCopyText.setVisible(true);
        workingCopyScroll.setVisible(true);
        workingCopyStatusLabel.setVisible(true);
    }

    private void turnOffWCCommitTab() {
        workingCopyText.setVisible(false);
        workingCopyScroll.setVisible(false);
        workingCopyStatusLabel.setVisible(false);
    }

    private void setAddNewBranchLabels(){
        branchToDeteleLabel.setVisible(true);
        branchToDeteleLabel.setText("Branch name");
        deleteButton.setVisible(true);
        deleteButton.setText("Add");
        branchToDeleteText.setVisible(true);
        branchToDeleteText.setText("");
        branchPointtedCommitLabel.setVisible(true);
        branchPointedCommitText.setVisible(true);
        branchPointedCommitText.setText("");
    }

    private void turnOnCollebrationLabels(){
        checkConflictsButton.setVisible(true);
        MergeButton.setVisible(true);
        thiersLabel.setVisible(true);
        ouersLabel.setVisible(true);
        originLabel.setVisible(true);
        afterMergeLabel.setVisible(true);

        theirsScroll.setVisible(true);
        ouersScroll.setVisible(true);
        afterMergeScroll.setVisible(true);
        originScroll.setVisible(true);
        turnOffConflictLabels();
    }

    private void turnOffMergeLabels(){
        turnOnConflictLabels();

        MergeButton.setVisible(false);
        thiersLabel.setVisible(false);
        ouersLabel.setVisible(false);
        originLabel.setVisible(false);
        afterMergeLabel.setVisible(false);
        mergeLabel.setVisible(false);

        theirsScroll.setVisible(false);
        ouersScroll.setVisible(false);
        afterMergeScroll.setVisible(false);
        originScroll.setVisible(false);
    }

    private void turnOffConflictLabels(){
        checkConflictsButton.setVisible(false);
        branchMergeText.setVisible(false);
        branchMergeLabel.setVisible(false);
    }

    private void turnOnConflictLabels(){
        checkConflictsButton.setVisible(true);
        branchMergeText.setVisible(true);
        branchMergeLabel.setVisible(true);
    }
}
