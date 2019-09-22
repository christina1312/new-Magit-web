package desktop;
import MainScene.MainSceneController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader Loader = new FXMLLoader();
        Loader.setLocation(getClass().getResource("/MainScene/MainSceneV2.fxml"));
        Parent root = Loader.load();
        MainSceneController crt = Loader.getController();
        crt.SetSage(primaryStage);
        primaryStage.setTitle("MAGit");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


