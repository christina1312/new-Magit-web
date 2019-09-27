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
import java.lang.reflect.Array;
import java.util.ArrayList;

import System.Commit;

public class MainSceneController {

    private Stage m_Stage;
    private BasicMAGitManager logic;
    private Parent m_root;

    private Task<Boolean> fileLoadTask;
    private boolean m_isGameLoaded;
    private File m_file;

    private final static int SLEEP_PERIOD = 100;
    private final static int SLEEP_INTERVAL = 20;

    @FXML
    private Button ChangeUserNameButton;
    @FXML
    private Button saveConflictButton;
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
    private TextArea theirsText;
    @FXML
    private TextArea ouersText;
    @FXML
    private TextArea originText;
    @FXML
    private TextArea afterMergeText;
    @FXML
    private ScrollPane originScroll;
    @FXML
    private ScrollPane afterMergeScroll;
    @FXML
    private Tab RepositirySettingsTab;
    @FXML
    private Tab FilesAndCommitTab;
    @FXML
    private Tab BranchesTab;
    @FXML
    private Tab MergaTab;
    @FXML
    private Tab CollabortionTab;
    @FXML
    private TabPane tabPane;

    public void SetSage(Stage i_stg) {
        m_Stage = i_stg;
    }

    public void MainSceneController() {
    }

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
        turnOffMergeLabels(false);
        makeTabsDisible();
        InfoCollaborationLabel.setVisible(false);

    }

    public void onLoadNewRepositoryFromXMLFile() throws InterruptedException {
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
            InfoLabel.setText("");
            AlertPromptDialog.show(m_Stage, "Could not load file!", "loadXML");

            turnOffProgressSettingTab();
        }
        turnOffLabelsSettingTab();
    }

    private Task<Boolean> LoadNewRepositoryFromXMLFileTask(File fileIn) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    for (int i = 0; i < SLEEP_INTERVAL; i++) {
                        Thread.sleep(SLEEP_PERIOD);
                        updateMessage("Loading file... " + ((float) i / SLEEP_INTERVAL) * 100 + "%");
                        updateProgress(i + 1, SLEEP_INTERVAL);
                    }

                    if (logic.LoadMAGit(fileIn.getAbsolutePath())) {
                        if (AlertPromptDialog.show(m_Stage, "There is repository in the given location. What do you want to do next?", "load") == 0) {
                            logic.DeleteRepositoryAndCreateNew(fileIn.getAbsolutePath());
                        } else { // == 1
                            //the user need to do commit in commit iab!!
                            logic.ChangeRepository(fileIn.getAbsolutePath());
                        }
                    }
                    Platform.runLater(() -> {
                        m_isGameLoaded = true;
                        ChangeUserNameButton.setDisable(false);
                        makeTabsVisible();
                        ProgressBarXml.setVisible(false);
                        progressPercentLabel.setVisible(false);

                        if (m_isGameLoaded) {
                            AlertPromptDialog.show(m_Stage, "File was loaded successfully!", "loadXML");
                           // InfoLabel.setVisible(true);
                          //  InfoLabel.setText("File was loaded successfully!");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                      //  InfoLabel.setVisible(true);
                      //  InfoLabel.setText("Error : " + e.getMessage());
                        AlertPromptDialog.show(m_Stage, "Error : " + e.getMessage(), "loadXML");
                        m_isGameLoaded = false;
                    });
                }
//
                return true;
            }
        };
    }

    public void onChangeRepository() throws InterruptedException {
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
            InfoLabel.setText(" ");
            AlertPromptDialog.show(m_Stage, "Could not change repository!", "loadXML");

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

                Platform.runLater(() -> {
                    try {
                        logic.createNewRepository(inputTextField.getText());
                        m_isGameLoaded = true;
                        ChangeUserNameButton.setDisable(false);
                        turnOffLabelsSettingTab();
                        makeTabsVisible();
                     //   InfoLabel.setVisible(true);
                        //InfoLabel.setText("The new repository has been created successfully!");
                        AlertPromptDialog.show(m_Stage, "The new repository has been created successfully!", "loadXML");

                    } catch (IOException ex) {
                        turnOffLabelsSettingTab();
                    //    InfoLabel.setVisible(true);
                      //  InfoLabel.setText("Error: " + ex + "\n An error occurred while trying to creat a new repository!");
                        AlertPromptDialog.show(m_Stage, "Error: " + ex + "\n An error occurred while trying to creat a new repository!", "loadXML");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        };
        saveButton.setOnAction(event);
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
                    Platform.runLater(() -> {
                        try {
                            logic.ChangeRepository(fileIn.getAbsolutePath());
                            m_isGameLoaded = true;
                            ChangeUserNameButton.setDisable(false);
                            makeTabsVisible();
                            InfoLabel.setVisible(true);
                            ProgressBarXml.setVisible(false);
                            progressPercentLabel.setVisible(false);
                            if (m_isGameLoaded) {
                              //  InfoLabel.setText("The repository has been changed successfully!");
                                AlertPromptDialog.show(m_Stage, "The repository has been changed successfully!", "loadXML");

                            }
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                           //     InfoLabel.setVisible(true);
                              //  InfoLabel.setText("Error : " + e.getMessage());
                                AlertPromptDialog.show(m_Stage, "Error : " + e.getMessage(), "loadXML");

                                turnOffProgressSettingTab();
                            });
                            m_isGameLoaded = false;
                        }
                    });
                } catch (Exception e) {

                    Platform.runLater(() -> {
                      //  InfoLabel.setVisible(true);
                     //   InfoLabel.setText("Error : " + e.getMessage());
                        AlertPromptDialog.show(m_Stage, "Error : " + e.getMessage(), "loadXML");
                        turnOffProgressSettingTab();
                    });
                    m_isGameLoaded = false;
                }
                return true;
            }
        };
    }

//                } finally {
//                    InfoLabel.setVisible(true);
//                    Platform.runLater(() -> {
//                        ProgressBarXml.setVisible(false);
//                        progressPercentLabel.setVisible(false);
//                        if (m_isGameLoaded) {
//                            InfoLabel.setText("The repository has been changed successfully!");
//                        }});
//                                    }
//                return true;
//            }
                    // };

    //todo
    private Task<Boolean> cloneRepositoryTask(File fileIn) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() {
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

    private void bindProgress()  {
        ProgressBarXml.progressProperty().unbind();
        ProgressBarXml.progressProperty().bind(fileLoadTask.progressProperty());

        progressPercentLabel.textProperty().bind(
                Bindings.concat(
                        Bindings.format(
                                "%.0f",
                                Bindings.multiply(fileLoadTask.progressProperty(),
                                Bindings.multiply(fileLoadTask.progressProperty(),
                                        100)),
                        " %")));

        fileLoadTask.messageProperty().addListener((observable, oldValue, newValue) -> progressPercentLabel.setText(newValue));
        Thread thread = new Thread(fileLoadTask);
        thread.start();
      //  thread.join();
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
                  //  InfoLabel.setVisible(true);
                    //InfoLabel.setText("The user name updated successfully!");
                    AlertPromptDialog.show(m_Stage, "The user name updated successfully!", "loadXML");

                } catch (Exception ex) {
                    turnOffLabelsSettingTab();
                  //  InfoLabel.setVisible(true);
                   // InfoLabel.setText("An error occurred while trying to update the user name!");
                    AlertPromptDialog.show(m_Stage, "An error occurred while trying to update the user name!", "loadXML");

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
         //   newCommitLabel.setVisible(true);
         //   newCommitLabel.setText("The new commit saved successfully!");
            AlertPromptDialog.show(m_Stage, "The new commit saved successfully!", "loadXML");

        } catch (Exception ex) {
            turnOffLabelsCommitTab();
         //   newCommitLabel.setVisible(true);
          //  newCommitLabel.setText("Error: " + ex.getMessage());
            AlertPromptDialog.show(m_Stage, "Error : " + ex.getMessage(), "loadXML");

            ex.printStackTrace();
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
                   // branchToDeteleLabel.setVisible(true);
                    //branchToDeteleLabel.setText("The branch was deleted successfully!");
                    AlertPromptDialog.show(m_Stage,"The branch was deleted successfully!", "loadXML");

                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
            //        branchToDeteleLabel.setVisible(true);
                 //   branchToDeteleLabel.setText("Error: " + ex.getMessage());
                    AlertPromptDialog.show(m_Stage, "Error : " + ex.getMessage(), "loadXML");

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
               //     branchToDeteleLabel.setVisible(true);
                 //   branchToDeteleLabel.setText("Checkout successfully!");
                    AlertPromptDialog.show(m_Stage, "Checkout successfully!", "loadXML");

                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
                 //   branchToDeteleLabel.setVisible(true);
                //    branchToDeteleLabel.setText("Error: " + ex.getMessage());
                    AlertPromptDialog.show(m_Stage, "Error : " + ex.getMessage(), "loadXML");

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
           //         branchToDeteleLabel.setVisible(true);
              //      branchToDeteleLabel.setText("Reset the head branch successfully!");
                    AlertPromptDialog.show(m_Stage,"Reset the head branch successfully!", "loadXML");

                } catch (Exception ex) {
                 //   turnOffDeleteLabelsBranchTab();
                //    branchToDeteleLabel.setVisible(true);
                    branchToDeteleLabel.setText("An error occurred while trying reset the head branch!");
                    AlertPromptDialog.show(m_Stage, "An error occurred while trying reset the head branch!", "loadXML");

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
    public void onCheckConflicts() {
        try {
            ArrayList<String> res = logic.startMerge(branchMergeText.getText(), 0);

            if (res == null) {
                mergeLabel.setVisible(true);
                mergeLabel.setText("No changes to merge!");
            } else if (res.get(0).equalsIgnoreCase("TRUE")) {
                if (AlertPromptDialog.show(m_Stage, "There are uncommitted changes", "checkout") == 0) {
                    logic.deleteWorkingCopyChanges();
                    logic.CheckoutHeadBranch(branchMergeText.getText());
                } else { //the user need to do commit in commit iab!!
                }
            } else {// father, me, branch to merge
                turnOnMergeLabels();
                originText.setText(res.get(0));
                ouersText.setText(res.get(1));
                theirsText.setText(res.get(2));

                mergeLabel.setVisible(true);
                mergeLabel.setText("Copy the selected changes to the After merge area :)");

            }
           // turnOffMergeLabels();
        } catch (Exception ex) {
            turnOffMergeLabels(false);
            mergeLabel.setVisible(true);
            mergeLabel.setText(ex.getMessage());
        }
    }

    public void onSaveConflict() {
        try {
            ArrayList<String> res;
            if(!logic.handleSingleConflict(afterMergeText.getText())){
                MergeButton.setVisible(true);
                setMergeScroll();
            }

            while (logic.checkIfThereIsMoreConflicts()) {
                setMergeScroll();
                res = logic.handleSecondConflict();
              //  turnOnMergeLabels();
                originText.setText(res.get(0));
                ouersText.setText(res.get(1));
                theirsText.setText(res.get(2));
            }

            MergeButton.setVisible(true);
        } catch (Exception ex) {
            mergeLabel.setVisible(true);
            mergeLabel.setText("Error: " + ex.getMessage());
            mergeLabel.setText("");
        }
    }

    public void onMerge() {
        try {
            logic.setAfterMerge();
         //   mergeLabel.setVisible(true);
         //   mergeLabel.setText("Merged successfully!");
            AlertPromptDialog.show(m_Stage, "Merged successfully!", "loadXML");

            turnOffMergeLabels(true);
        } catch (Exception ex) {
        //    mergeLabel.setVisible(true);
       //     mergeLabel.setText("Error: " + ex.getMessage());
       //     mergeLabel.setText("");
            AlertPromptDialog.show(m_Stage, "Error: " + ex.getMessage(), "loadXML");

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
                    //branchToDeteleLabel.setVisible(true);
                    //branchToDeteleLabel.setText("The new branch created successfully!");
                    AlertPromptDialog.show(m_Stage, "The new branch created successfully!", "loadXML");

                } catch (Exception ex) {
                    turnOffDeleteLabelsBranchTab();
                 //   branchToDeteleLabel.setVisible(true);
                //    branchToDeteleLabel.setText("An error occurred while trying to add new branch!");
                    AlertPromptDialog.show(m_Stage, "An error occurred while trying to add new branch!", "loadXML");

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
        InfoLabel.setText("");
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
        newCommitLabel.setText("Commit message:");
        newCommitMessageText.setVisible(true);
        newCommitMessageText.setText("");
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
        branchText.setText("");
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
        workingCopyText.setText("");
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

    private void turnOnMergeLabels(){
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
        saveConflictButton.setVisible(true);
        turnOffConflictLabels();
    }

    private void setMergeScroll(){
        theirsText.setText(" ");
        ouersText.setText(" ");
        originText.setText(" ");
        afterMergeText.setText(" ");
    }
    private void turnOffMergeLabels(boolean isMerge){
        turnOnConflictLabels();

        MergeButton.setVisible(false);
        thiersLabel.setVisible(false);
        ouersLabel.setVisible(false);
        originLabel.setVisible(false);
        afterMergeLabel.setVisible(false);

        if(!isMerge){
            mergeLabel.setVisible(false);
        }
        theirsScroll.setVisible(false);
        ouersScroll.setVisible(false);
        afterMergeScroll.setVisible(false);
        originScroll.setVisible(false);
        saveConflictButton.setVisible(false);
    }

    private void turnOffConflictLabels(){
        checkConflictsButton.setVisible(false);
        MergeButton.setVisible(false);
        branchMergeText.setVisible(false);
        branchMergeLabel.setVisible(false);
    }

    private void turnOnConflictLabels(){
        checkConflictsButton.setVisible(true);
        branchMergeText.setVisible(true);
        branchMergeText.setText("");
        branchMergeLabel.setVisible(true);
    }

    private void makeTabsVisible(){
        FilesAndCommitTab.setDisable(false);
        BranchesTab.setDisable(false);
        MergaTab.setDisable(false);
        CollabortionTab.setDisable(false);
    }

    private void makeTabsDisible(){
        FilesAndCommitTab.setDisable(true);
        BranchesTab.setDisable(true);
        MergaTab.setDisable(true);
       // CollabortionTab.setDisable(true);
    }
}