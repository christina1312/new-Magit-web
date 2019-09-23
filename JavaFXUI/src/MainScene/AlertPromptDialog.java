package MainScene;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

public class AlertPromptDialog extends Stage {
    private static final int WIDTH_DEFAULT = 400;

    private static Label label;
    private static AlertPromptDialog popup;
    private static int result;
    private static Image img;
    private static String url;
    public static final int NO = 0;
    public static final int YES = 1;
    public static BorderPane borderPane;

    private AlertPromptDialog(String type) {
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        label = new Label();
        label.setWrapText(true);
        label.setGraphicTextGap(40);
        label.styleProperty().bind(Bindings.format("-fx-font-size: 15 ; -fx-font-weight: bold; -fx-background-color: linear-gradient(#61a2b1, #2A5058) "));
        label.setAlignment(Pos.CENTER);

        Button continueButton;
        Button commitButton;

        continueButton = new Button("Continue (all changes will be deleted)");
        continueButton.setMinSize(100, 50);
        continueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                result = 0;
                AlertPromptDialog.this.close();
            }
        });

        if(type.equalsIgnoreCase("checkout")){
            commitButton = new Button("Commit the changes");
            commitButton.setMinSize(100, 30);
            commitButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    result = 1;
                    AlertPromptDialog.this.close();
                }
            });
        }
        else if(type.equalsIgnoreCase("load")){
            commitButton = new Button("Delete the existing repository and create new repository");
            commitButton.setMinSize(300, 30);
            commitButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    result = 0;
                    AlertPromptDialog.this.close();
                }
            });

            continueButton = new Button("Use the existing repository");
            continueButton.setMinSize(100, 30);
            continueButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    result = 1;
                    AlertPromptDialog.this.close();
                }
            });

        }
        else{
            commitButton = new Button("Cancel");
            continueButton.setMinSize(100, 30);
            commitButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    result = 1;
                    AlertPromptDialog.this.close();
                }
            });
        }
        borderPane = new BorderPane();
        BorderPane dropShadowPane = new BorderPane();
        dropShadowPane.setTop(label);

        HBox hbox = new HBox();
        hbox.setSpacing(15);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(continueButton);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(commitButton);

        dropShadowPane.setBottom(hbox);
        borderPane.setCenter(dropShadowPane);


        Scene scene = new Scene(borderPane);
        //  scene.getStylesheets().add(getResource("Alert.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    public static int show(Stage owner, String msg,String type) {
        Platform.runLater(()->{
            if (popup == null) {
                popup = new AlertPromptDialog(type);
            }
            label.setText(msg);

            // calculate width of string
            final Text text = new Text(msg);
            text.snapshot(null, null);
            int width = (int) text.getLayoutBounds().getWidth()+150;
            int height = 120;

            popup.setWidth(width);
            popup.setHeight(height);

            // make sure this stage is centered on top of its owner
            popup.setX(owner.getX() + (owner.getWidth() / 2 - popup.getWidth() / 2));
            popup.setY(owner.getY() + (owner.getHeight() / 2 - popup.getHeight() / 2 -80));
            popup.showAndWait();
        });
        return result;
    }

}