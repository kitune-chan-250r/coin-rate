package app.controller;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.net.URL;
import java.io.FileInputStream;

public class SttingController implements Initializable {
    @FXML
    private TextField amountField;

    @FXML
    private TextField chartRangefield;

    @FXML
    private Button subButton;

    @FXML
    protected void onGetAmount(ActionEvent evt){
        String coin_amount = amountField.getText();
        String chartRange = chartRangefield.getText();

        try {
            registSetting(coin_amount, chartRange);
        } catch (Exception e) {
            e.getStackTrace();
        }

        Stage settingStage = (Stage) subButton.getScene().getWindow();
        settingStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        try {
            loadSetting();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void loadSetting() throws Exception {
        Properties settings = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream("Settings.properties");
            settings.load(in);
        } catch (Exception e){
            e.getStackTrace();
        } finally {
            if (in != null){
                in.close();
            }
        }

        String amount = settings.getProperty("amount");
        String range = settings.getProperty("range");

        amountField.setText(amount);
        chartRangefield.setText(range);
    }
    
    public void registSetting(String coin_amount, String chartRange) throws IOException{
        Properties settings = new Properties();
        //設定項目
        settings.setProperty("amount", coin_amount);
        settings.setProperty("range", chartRange);

        FileOutputStream outputSetting = null;

        try {
            outputSetting = new FileOutputStream("Settings.properties");
            settings.store(outputSetting, "saved");
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            outputSetting.close();
        }
    }
    
}
