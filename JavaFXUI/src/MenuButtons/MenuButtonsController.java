//package MenuButtons;
//
//import MainScene.AlertPromptDialog;
//import System.BasicMAGitManager;
//import javafx.application.Platform;
//import javafx.beans.binding.Bindings;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.concurrent.Task;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.ProgressBar;
//import javafx.scene.control.TextArea;
//import javafx.stage.FileChooser;
//import javafx.stage.Stage;
//
//import java.io.File;
//
//public class MenuButtonsController {
//
//    private Task<Boolean> fileLoadTask;
//    Stage m_Stage;
//    boolean m_isGameLoaded;
//    File m_file;
//
//    private final static int SLEEP_PERIOD = 100;
//    private final static int SLEEP_INTERVAL = 20;
//
//    @FXML
//    private Button ChangeUserNameButton;
//    @FXML
//    private Button LoadNewRepositoryFromXMLFileButton;
//    @FXML
//    private Button InitializeNewRepositoryButton;
//    @FXML
//    private Button ChangeRepositoryButton;
//    @FXML
//    private Button ExportRepositoryForNewXMLFileButton;
//    @FXML
//    private Button ShowCurrentCommitFileSystemButton;
//    @FXML
//    private Button DoCommitButton;
//    @FXML
//    private Button ShowBranchesFileSystemButton;
//    @FXML
//    private Button CreateNewBranchButton;
//    @FXML
//    private Button DeleteBranchButton;
//    @FXML
//    private Button ResetBranchLocationButton;
//    @FXML
//    private ProgressBar ProgressBarXml;
//    @FXML
//    private Label statusLabel;
//    @FXML
//    private Label commandLabel;
//    @FXML
//    private Label outputLabel;
//    @FXML
//    private Label progressPercentLabel;
//    @FXML
//    private TextArea inputArea;
//
//    private BasicMAGitManager logic;
//    private SimpleStringProperty StatusProperty;
// //   private SimpleStringProperty InputProperty;
//    private SimpleStringProperty OutputProperty;
//    private SimpleStringProperty CommandProperty;
//
//    public void SetSage(Stage i_stg) {
//        m_Stage = i_stg;
//    }
//
//    public MenuButtonsController() {
//        StatusProperty = new SimpleStringProperty();
//        CommandProperty = new SimpleStringProperty();
//        OutputProperty = new SimpleStringProperty();
//    // InputProperty = new SimpleLongProperty(0);
//    }
//    @FXML
//    public void initialize() {
//        statusLabel.textProperty().bind(StatusProperty);
//        commandLabel.textProperty().bind(CommandProperty);
//        outputLabel.textProperty().bind(OutputProperty);
//      //  inputArea.textProperty().bind(Bindings.format("%,d", InputProperty));
//    }
//
//    public void onLoadNewRepositoryFromXMLFile(ActionEvent actionEvent) {
//        ProgressBarXml.setVisible(true);
//        LoadNewRepositoryFromXMLFileButton.setDisable(true);
//
//        ProgressBarXml.progressProperty().unbind();
//        ProgressBarXml.setProgress(0);
//
//        logic = new BasicMAGitManager();
//        FileChooser fileChooser = new FileChooser();
//
//        //the window of the file chooser
//        fileChooser.setTitle("Search for a XML file");
//        fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("XML files", "*.xml"));
//
//        File fileIn = fileChooser.showOpenDialog(m_Stage);
//        m_file = fileIn;
//        fileLoadTask = LoadNewRepositoryFromXMLFileTask(fileIn);
//
//        ProgressBarXml.progressProperty().unbind();
//        ProgressBarXml.progressProperty().bind(fileLoadTask.progressProperty());
//
//        progressPercentLabel.textProperty().bind(
//                Bindings.concat(
//                        Bindings.format(
//                                "%.0f",
//                                Bindings.multiply(fileLoadTask.progressProperty(),
//                                        100)),
//                        " %"));
//
//        fileLoadTask.messageProperty().addListener((observable, oldValue, newValue) -> statusLabel.setText(newValue));
//        new Thread(fileLoadTask).start();
//        LoadNewRepositoryFromXMLFileButton.setDisable(false);
//        this.progressPercentLabel.textProperty().unbind();
//
//        //AlertPromptDialog.show(m_Stage,"You Loaded the xml file successfully !","/ResourcesPic/mine.gif");
//    }
//
//    private Task<Boolean> LoadNewRepositoryFromXMLFileTask(File fileIn) {
//        //  throws UnmarshalException,ConfigException,FileNotFoundException,FileTypeException,XmlFileSerializerException, JAXBException
//        return new Task<Boolean>() {
//            @Override
//            protected Boolean call() throws Exception {
//                try {
//                    for (int i = 0; i < SLEEP_INTERVAL; i++) {
//                        Thread.sleep(SLEEP_PERIOD);
//                        updateMessage("Loading file... " + ((float) i / SLEEP_INTERVAL) * 100 + "%");
//                        updateProgress(i + 1, SLEEP_INTERVAL);
//                    }
//                    //load the game acoording to the path we get from the user
//                    logic.LoadMAGit(fileIn.getAbsolutePath());
//                    m_isGameLoaded = true;
//                    //make the 'change repository' button visible
//                    Platform.runLater(() -> ChangeRepositoryButton.setDisable(false));
//                } catch (Exception e) {
//                    statusLabel.setVisible(true);
//                    String s = e.getMessage();
//                    Platform.runLater(() -> statusLabel.setText("Error : " + s));
//                    m_isGameLoaded = false;
//                } finally {
//                    Platform.runLater(() -> ProgressBarXml.setVisible(false));
//                    if (m_isGameLoaded) {
//                        Platform.runLater(() -> statusLabel.setText("File was loaded successfully!"));
//                    }
//                }
//                return true;
//            }
//        };
//    }
//
//    public void onChangeUserName(ActionEvent actionEvent) {
//      //  commandLabel.setVisible(true);
//        commandLabel.setText("Please enter new User name: ");
//        logic.setUserName(inputArea.getText());
//        commandLabel.setVisible(false);
//    }
//
//    public void DoCommit(ActionEvent actionEvent) {
//        //logic.DoCommit();
//    }
//
//    public void onExportRepositoryForNewXMLFile(ActionEvent actionEvent) {
//
//    }
//
//    public void onChangeRepository(ActionEvent actionEvent) {
//        //logic.ChangeRepository();
//    }
//
//    public void onInitializeNewRepository(ActionEvent actionEvent) {
//    }
//
//    public void onShowCurrentCommitFileSystem(ActionEvent actionEvent) {
//        logic.ShowCurrentCommitFileSystem();
//    }
//
//    public void onDeleteBranch(ActionEvent actionEvent) {
//        //logic.DeleteBranch();
//    }
//
//    public void onResetBranchLocation(ActionEvent actionEvent) {
//    }
//
//    public void onCreateNewBranch(ActionEvent actionEvent) {
//        //logic.CreateNewBranch();
//    }
//
//    public void onShowBranchesFileSystem(ActionEvent actionEvent) {
//        logic.ShowAllBranchesFileSystem();
//    }
//}