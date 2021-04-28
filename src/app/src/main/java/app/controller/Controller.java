package app.controller;

import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpConnectTimeoutException;
import java.net.URL;

import java.util.LinkedList;
import java.util.ResourceBundle;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


public class Controller implements Initializable{
    @FXML
    private Label labeltext;

    @FXML
    private Label coinratelabel;

    @FXML
    private Button closebutton;

    @FXML
    private Button settingbutton;

    @FXML
    private Label amountLabel;

    @FXML
    private Label yen;

    @FXML
    private Label netassetsLabel;

    @FXML
    private ImageView xrpicon;

    @FXML
    private ImageView up;

    @FXML
    private ImageView down;

    @FXML
    private ImageView timeout;

    @FXML
    LineChart<String, Number> lineChart;

    @FXML
    NumberAxis yAxis;

    private Timeline timeline;
    private String pair = "xrp_jpy";
    private Double coinamount = 337.237;//337.237
    //private Double previousrate = 0.0;
    private LinkedList<Double> rate_list = new LinkedList<Double>();
    String URL = "https://coincheck.com";
    String endpoint = "/api/rate/";
    Stage thisStage;
    Double chartRange = 0.0;
    Integer chartXsize = 100;

    public void setThisStage(Stage stage){
        thisStage = stage;
    }

    HttpClient client = HttpClient.newHttpClient();

    HttpRequest req = HttpRequest
            .newBuilder(URI.create(URL+endpoint+pair))
            .timeout(java.time.Duration.ofSeconds(1))
            .build();

    HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    /*
    coincheckのAPIにxrp_jpyペアのコインレートを要求するgetリクエストを送り
    coinratelabelの内容を更新する
    coinamoutとレートから資産額の表示を行う
    previousrateからレートの上げ下げを判定、レート表示の右横に赤矢印、緑矢印で
    表示を行う
    
    メニューからcoinamoutを設定できるようにする(別ウィンドウ必須)
    */
    public void update_coinrate_text(String pair) throws Exception { 
        HttpResponse<String> res = client.send(req, bodyHandler);
    
        String pars = res.body().replaceAll("[^0-9.0-9]", "");
        Double rate = Double.parseDouble(pars);
        //rate
        Double roundrate = Math.round(rate*1000.0)/1000.0;
        //Net Assets
        Double netassets =  Math.round((rate * coinamount)*100.0)/100.0;

        //up-down
        /*
        案１　up-downをラインチャートに移植、ノードの平均値とそれより高いノードと低いノードの量でupdownを判定
        案２　前半50ノードと後半50ノードの平均値を比べる
        
        if (previousrate - rate < 0){
            up.setVisible(true);
            down.setVisible(false);
        }
        if (previousrate - rate > 0){
            up.setVisible(false);
            down.setVisible(true);
        }

        previousrate = roundrate;*/

        //update labels
        coinratelabel.setText(String.valueOf(roundrate));
        amountLabel.setText(String.valueOf(coinamount));
        netassetsLabel.setText(String.valueOf(netassets));

        //update_linechart
        update_linechart(roundrate);

    }

    //レートを折れ線グラフ表示
    public void update_linechart(Double rate){
        XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
        //rate_listがchartXsizeになるようにchartXsize以上になった場合に先頭の要素を削除
        if (rate_list.size() >= chartXsize){
            rate_list.removeFirst();
            rate_list.add(rate);
        }else{
            rate_list.add(rate);
        }

        //new updown
        Double beforeSum = 0.0;
        Double afterSum = 0.0;
        Integer listSize = rate_list.size();

        lineChart.getData().clear();
        //dataセット前にchartの表示範囲を自動調節
        yAxis.setLowerBound(rate-chartRange);
        yAxis.setUpperBound(rate+chartRange);

        Integer seconds = 0;

        for(Double r : rate_list){
            series.getData().add(new XYChart.Data<String, Number>(seconds.toString(), r));
            if (seconds < listSize / 2){
                beforeSum += r;
            }if (seconds >= listSize / 2){
                afterSum += r;
            }
            seconds++;
        }
        lineChart.getData().add(series);

        if (beforeSum/(chartXsize/2) > afterSum/(chartXsize/2)){
            up.setVisible(false);
            down.setVisible(true);
        }if (beforeSum/(chartXsize/2) < afterSum/(chartXsize/2)){
            up.setVisible(true);
            down.setVisible(false);
        }
    }

    //その他のエラー時のメッセージ
    public void update_coinrate_errormsg(){
        coinratelabel.setText("APIError");
    }

    //タイムアウト時にインジケーターON
    public void connect_timeout_erroricon(){
        timeout.setVisible(true);
    }

    //propertiesファイルからamountを読み込んでセット
    public void settingFileLoad() throws IOException{
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
        coinamount = Double.parseDouble(amount);

        String range = settings.getProperty("range");
        chartRange = Double.parseDouble(range);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources){
        //最初にインジケーターアイコンを初期化
        up.setVisible(false);
        down.setVisible(false);
        timeout.setVisible(false);

        //amoutをPropertiesファイルから読み込み
        try {
            settingFileLoad();
        } catch (Exception e) {
            e.getStackTrace();
        }
        
        /*
        timelineなどのマルチスレッド機能を使ってバックグラウンドでget_coin_rate()
        を定期実行しcoinratelabelの値を更新し続ける
        */
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);

        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        new EventHandler<ActionEvent>(){
                            @Override public void handle(ActionEvent event) {
                                //繰り返す処理を記載
                                try {
                                    update_coinrate_text(pair);
                                } catch (HttpConnectTimeoutException e) {
                                    //timeout処理
                                    connect_timeout_erroricon();
                                } catch (Exception e) {
                                    update_coinrate_errormsg();
                                    e.printStackTrace();
                                }

                            }
                        }));
        timeline.play();
    }

    //Event
    @FXML
    protected void onCloseButtonClick(ActionEvent evt) {
        Platform.exit();
    }

    @FXML
    protected void onSettingButtonClick(ActionEvent evt) {
        /*Setting window */
        try {
            Parent settingRoot = FXMLLoader.load(getClass().getResource("/app/view/Setting.fxml"));
            Scene newscene = new Scene(settingRoot);
            Stage settingStage = new Stage();
        
            settingStage.setScene(newscene);

            //settingStage.initModality(Modality.APPLICATION_MODAL);

            settingStage.setTitle("Setting");
            settingStage.setResizable(false);
            //親Stage取得後ずらしてsettingStage表示
            //settingStage.initOwner(thisStage);
            settingStage.setX(thisStage.getX() - 7);
            settingStage.setY(thisStage.getY() + 163);
            
            //設定のリロード
            settingStage.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue == true && newValue == false) {
                    try {
                        settingFileLoad();
                    } catch (Exception e) {
                        e.getStackTrace();
                    }   
                }
            });

            settingStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
            
    }

}
