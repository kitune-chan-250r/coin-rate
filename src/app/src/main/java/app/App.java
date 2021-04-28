package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;
import javafx.stage.Stage;
import javafx.scene.Parent;
import app.controller.Controller;

public class App extends Application {

    private double dragStartX;
    private double dragStartY;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Image icon = new Image("/app/view/icon/32x32.png");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/view/Main.fxml"));
        Parent root = loader.load();//FXMLLoader.load();

        stage.setTitle("CoinRateJavaFX");
        stage.setResizable(false); //disable window resize
        stage.initStyle(StageStyle.UNDECORATED);//disable taskbar
        stage.setAlwaysOnTop(true); //enable always on top
        stage.getIcons().add(icon); //set icon
        

        Controller cntr = (Controller) loader.getController();
        cntr.setThisStage(stage);
        Scene scene = new Scene(root);
        //css
        scene.getStylesheets().add(getClass().getResource("/app/view/application.css").toExternalForm());

        /*
        stageのinit styleがundecoratedの場合マウスでウィンドウ移動が
        できなくなるため自分で設定
        */
        scene.setOnMousePressed(e -> {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
        });
        scene.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragStartX);
            stage.setY(e.getScreenY() - dragStartY);
        });

        stage.setScene(scene);
        stage.show();
    }
}
