package com.example.mediaplayerlab2;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class MediaPlayerController implements Initializable {
   private String path = "";
   private MediaPlayer mediaPlayer;
    @FXML
    private MediaView mediaView;

    @FXML
    private VBox vBoxParent;
    @FXML
    private HBox hboxControls;
    @FXML
    private HBox hboxVolume;
    @FXML
    private Button buttonPPR;
    @FXML
    private Label labelCurrentTime;
    @FXML
    private Label labelTotalTime;
    @FXML
    private Label labelSpeed;
    @FXML
    private Label labelFullScreen;
    @FXML
    private Label labelVolume;
    @FXML
   private Slider progressBar;
    @FXML
   private Slider volumeSlider;
    private boolean atEndOfVideo = false;
    private boolean isPlaying = true;
    private boolean isMuted = true;

    private ImageView ivPlay;
    private ImageView ivPause;
    private ImageView ivRestart;
    private ImageView ivVolume;
    private ImageView ivFullScreen;
    private ImageView ivMute;
    private ImageView ivExit;


   public void chooseFileMethod(ActionEvent event) throws MalformedURLException {
       mediaPlayer.pause();
       FileChooser fileChooser = new FileChooser();
       File file = fileChooser.showOpenDialog(null);

       path = file.toURI().toString();

       Media media = new Media(path);
       mediaPlayer = new MediaPlayer(media);
       mediaView.setMediaPlayer(mediaPlayer);
       progressBar.setValue(0);
       mediaPlayer.play();
       isPlaying = true;
       buttonPPR.setGraphic(ivPause);

       bindCurrentTimeLabel();
       if(mediaPlayer.getVolume()!= 0.0){
           labelVolume.setGraphic(ivVolume);
           isMuted = false;
       }
       addListeners();
       labelSpeed.setText("1X");
   }


   public void addListeners() {
       mediaPlayer.totalDurationProperty().addListener(new ChangeListener<Duration>() {
           @Override
           public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
               progressBar.setMax(newValue.toSeconds());
               labelTotalTime.setText(getTime(newValue));
           }
       });
       progressBar.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
           @Override
           public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
               if(!newValue){
                   mediaPlayer.seek(Duration.seconds(progressBar.getValue()));
               }
           }
       });

       progressBar.valueProperty().addListener(new ChangeListener<Number>() {
           @Override
           public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
               double currentTime = mediaPlayer.getCurrentTime().toSeconds();
               if(Math.abs(currentTime - newValue.doubleValue())>0.5){
                   mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
               }
               labelMatchEndVideo(labelCurrentTime.getText(), labelTotalTime.getText());
           }
       });

       mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
           @Override
           public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {

               if(!progressBar.isValueChanging()){
                   progressBar.setValue(newValue.toSeconds());
               }
               labelMatchEndVideo(labelCurrentTime.getText(), labelTotalTime.getText());
           }
       });

       mediaPlayer.setOnEndOfMedia(new Runnable() {
           @Override
           public void run() {
               buttonPPR.setGraphic(ivRestart);
               atEndOfVideo = true;
               if(!labelCurrentTime.textProperty().equals(labelTotalTime.textProperty())){
                   labelCurrentTime.textProperty().unbind();
                   labelCurrentTime.setText(getTime(mediaPlayer.getTotalDuration())+ " / ");
               }
           }
       });
   }

    public void slowRate(ActionEvent event){
        mediaPlayer.setRate(0.5);
    }
    public void fastForward(ActionEvent event){
        mediaPlayer.setRate(2);
    }

    public void skip10(ActionEvent event){
       mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(10)));
    }
    public void back10(ActionEvent event){
        mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(-10)));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        path = file.toURI().toString();

        Media media = new Media(path);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();

        final int IV_SIZE = 25;

        Image imagePlay = new Image(new File("src/main/resources/image/play-btn.png").toURI().toString());
        ivPlay = new ImageView(imagePlay);
        ivPlay.setFitHeight(IV_SIZE);
        ivPlay.setFitWidth(IV_SIZE);

        Image imageStop = new Image(new File("src/main/resources/image/stop-btn.png").toURI().toString());
        ivPause = new ImageView(imageStop);
        ivPause.setFitHeight(IV_SIZE);
        ivPause.setFitWidth(IV_SIZE);

        Image imageRestart = new Image(new File("src/main/resources/image/restart-btn.png").toURI().toString());
        ivRestart = new ImageView(imageRestart);
        ivRestart.setFitHeight(IV_SIZE);
        ivRestart.setFitWidth(IV_SIZE);

        Image imageVolume = new Image(new File("src/main/resources/image/volume.png").toURI().toString());
        ivVolume = new ImageView(imageVolume);
        ivVolume.setFitHeight(IV_SIZE);
        ivVolume.setFitWidth(IV_SIZE);

        Image imageFullScreen = new Image(new File("src/main/resources/image/fullscreen.png").toURI().toString());
        ivFullScreen = new ImageView(imageFullScreen);
        ivFullScreen.setFitHeight(IV_SIZE);
        ivFullScreen.setFitWidth(IV_SIZE);

        Image imageMute = new Image(new File("src/main/resources/image/mute.png").toURI().toString());
        ivMute = new ImageView(imageMute);
        ivMute.setFitHeight(IV_SIZE);
        ivMute.setFitWidth(IV_SIZE);

        Image imageExit = new Image(new File("src/main/resources/image/exitscreen.png").toURI().toString());
        ivExit = new ImageView(imageExit);
        ivExit.setFitHeight(IV_SIZE);
        ivExit.setFitWidth(IV_SIZE);

        buttonPPR.setGraphic(ivPause);
        labelVolume.setGraphic(ivMute);
        labelSpeed.setText("1X");
        labelFullScreen.setGraphic(ivFullScreen);

        hboxVolume.getChildren().remove(volumeSlider);
        mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());

        bindCurrentTimeLabel();
        addListeners();

        buttonPPR.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Button buttonPlay = (Button) event.getSource();
                if(atEndOfVideo){
                    progressBar.setValue(0);
                    atEndOfVideo = false;
                    isPlaying = false;
                    bindCurrentTimeLabel();
                }
                if(isPlaying){
                    buttonPlay.setGraphic(ivPlay);
                    mediaPlayer.pause();
                    isPlaying = false;
                } else {
                    buttonPlay.setGraphic(ivPause);
                    mediaPlayer.play();
                    isPlaying = true;
                }
            }
        });

        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue());
                if(mediaPlayer.getVolume()!= 0.0){
                    labelVolume.setGraphic(ivVolume);
                    isMuted = false;
                } else {
                    labelVolume.setGraphic(ivMute);
                    isMuted = true;
                }
            }
        });

        labelSpeed.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(labelSpeed.getText().equals("1X")){
                    labelSpeed.setText("2X");
                    mediaPlayer.setRate(2.0);
                } else {
                    labelSpeed.setText("1X");
                    mediaPlayer.setRate(1.0);
                }
            }
        });

        labelVolume.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(isMuted){
                    labelVolume.setGraphic(ivVolume);
                    volumeSlider.setValue((0.2));
                    isMuted = false;
                } else {
                    labelVolume.setGraphic(ivMute);
                    volumeSlider.setValue((0));
                    isMuted = true;
                }
            }
        });

        labelVolume.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(hboxVolume.lookup("#volumeSlider")==null){
                    hboxVolume.getChildren().add(volumeSlider);
                    volumeSlider.setValue(mediaPlayer.getVolume());
                }
            }
        });

        hboxVolume.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                hboxVolume.getChildren().remove(volumeSlider);
            }
        });

        vBoxParent.sceneProperty().addListener(new ChangeListener<Scene>() {
            @Override
            public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
                if(oldValue == null && newValue!=null){
                    mediaView.fitHeightProperty().bind(newValue.heightProperty().subtract(hboxControls.heightProperty().add(20)));
                }
            }
        });

        labelFullScreen.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Label label = (Label) event.getSource();
                Stage stage = (Stage) label.getScene().getWindow();
                if(stage.isFullScreen()){
                    stage.setFullScreen(false);
                    labelFullScreen.setGraphic(ivFullScreen);
                } else {
                    stage.setFullScreen(true);
                    labelFullScreen.setGraphic(ivExit);
                }
                stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if(event.getCode() == KeyCode.ESCAPE){
                            labelFullScreen.setGraphic(ivFullScreen);
                        }
                    }
                });
            }
        });

    }

    public void bindCurrentTimeLabel (){
       labelCurrentTime.textProperty().bind(Bindings.createStringBinding(new Callable<String>() {
           @Override
           public String call() throws Exception {
               return getTime(mediaPlayer.getCurrentTime()) + " / ";
           }
       }, mediaPlayer.currentTimeProperty()));
    }

    public String getTime(Duration time){
        int hours = (int) time.toHours();
        int minutes = (int) time.toMinutes();
        int sec = (int) time.toSeconds();

        if(sec>59) sec = sec%60;
        if(minutes>59) minutes=minutes%60;
        if(hours>59) hours=hours%60;

        if(hours>0) return String.format("%d:%02d:%02d", hours, minutes, sec);
        else return String.format("%02d:%02d", minutes, sec);
    }

    public void labelMatchEndVideo(String labelTime, String labelTotalTime){
       for(int i=0; i<labelTotalTime.length(); i++){
           if(labelTime.charAt(i) != labelTotalTime.charAt(i)) {
               atEndOfVideo = false;
               if(isPlaying) buttonPPR.setGraphic((ivPause));
               else buttonPPR.setGraphic(ivPlay);
               break;
           } else {
               atEndOfVideo = true;
               buttonPPR.setGraphic(ivRestart);
           }

       }
    }

}