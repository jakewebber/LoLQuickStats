package application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.sourceforge.tess4j.TesseractException;
import net.rithms.riot.constant.Region;
import java.util.*;

import javax.imageio.ImageIO;

/**
 * Controller for JavaFX components and GUI
 * @author Jacob Webber */
public class QuickStatsController implements Initializable {
	@FXML 
	private BorderPane mainpane; 
	private Stage stage;
	@SuppressWarnings("unused")
	private Scene scene;
	private LeagueData data;
	private ClipBoardImage clipboardimage = new ClipBoardImage();
	private java.awt.Image screenshot;
	private boolean dialogShowing = false;
	private Properties props = new Properties();
	public String riotDirectory = "";
	public String region = "";
	public String version = "1.0";
	public boolean firstOpen = false;
	public int totalSummoners = 0;
	public int totalChampions = 0;
	public double totalWinRate = 0;
	public int totalRank = 0;
	public int totalLP = 0;
	public int totalTier = 0;
	public int emptyWinLosses = 0;
	public Image[] masteryImages = new Image[5];
	Image currentMasteryImage = null;

	private static final RiotApi api = RiotAPIKey.getAPI();

	@SuppressWarnings({ "rawtypes", "static-access", "unused", "unchecked" })
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		try {
			// If settings properties exist, load them
			if(new File(System.getProperty("user.dir") + "/user.properties").exists()){
				props = loadProperties(System.getProperty("user.dir"));
				region = props.getProperty("Region", "NA");
				riotDirectory = props.getProperty("Riot directory", "C:/Riot Games/");
			}else{ //If they do not, try to set defaults.
				firstOpen = true;
				riotDirectory = "C:/Riot Games/";
				region  = "NA";
				if(!new File("C:/Riot Games/").exists()){ // Default riot dir not found
					riotDirectory = getRiotDirectory();
				}
			}
			//Initial API call for setting up League data variables
			initializeLeagueData();


			BackgroundImage background = new BackgroundImage(new Image("/images/background.jpg",1280,900,false,true),
					BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
					BackgroundSize.DEFAULT);
			mainpane.setBackground(new Background(background));
			VBox rightpane = new VBox(); // Champion info pane
			rightpane.setMinWidth(300); 
			rightpane.setPadding(new Insets(0, 15, 0, 0));
			VBox leftpane = new VBox(); // Summoner and champ tables pane
			leftpane.setMaxWidth(350);
			leftpane.setMaxHeight(700);
			VBox bottompane = new VBox(60); // Averages info pane
			bottompane.setMaxHeight(100);
			bottompane.setMinHeight(50);
			VBox centerpane = new VBox(); // Summoner rank info pane
			centerpane.setMaxWidth(200);

			/* Creating new header toolbar */
			ToolBar toolBar = new ToolBar();
			int height = 25;
			toolBar.setPrefHeight(height);
			toolBar.setMinHeight(height);
			toolBar.setMaxHeight(height);
			HBox alignRight = new HBox();
			Pane pane = new Pane(alignRight);
			alignRight.setHgrow(pane, Priority.ALWAYS);
			Text title = new Text("LoL Quick Stats");
			title.setStyle("-fx-font-family: arial;"
					+ "-fx-font-size: 22px;"
					+ "-fx-fill: linear-gradient(from 0% 0% to 100% 100%, goldenrod 0%, gold 50%, peru 100%);"
					+ "-fx-stroke-width: 1;");
			toolBar.getItems().addAll(title, pane, new WindowButtons());
			toolBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
			/* Handle toolbar drag and drop operations */
			final Delta dragDelta = new Delta();
			toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent mouseEvent) {
					// record a delta distance for the drag and drop operation.
					dragDelta.x = stage.getX() - mouseEvent.getScreenX();
					dragDelta.y = stage.getY() - mouseEvent.getScreenY();
				}
			});
			toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override public void handle(MouseEvent mouseEvent) {
					stage.setX(mouseEvent.getScreenX() + dragDelta.x);
					stage.setY(mouseEvent.getScreenY() + dragDelta.y);
				}
			});
			mainpane.setTop(toolBar);

			JFXListView<VBox> summonerlist = new JFXListView<VBox>();
			summonerlist.setPadding(new Insets(0, 0, 20, 0));
			summonerlist.setMinWidth(300);
			summonerlist.setMaxWidth(300);
			summonerlist.setMinHeight(640);
			//summoner1
			VBox summoner1 = new VBox();
			JFXTextField summoner1field = new JFXTextField();   
			summoner1field.setPromptText("Summoner 1");
			JFXComboBox<String> champ1box = new JFXComboBox<String>();
			champ1box.getItems().addAll(data.champNames);
			champ1box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ1box);
			HBox summoner1FieldBox = new HBox();
			summoner1FieldBox.setAlignment(Pos.CENTER);
			summoner1field.setMinWidth(250);
			summoner1FieldBox.getChildren().addAll(summoner1field, new ImageView("/images/empty.png")); 
			summoner1.getChildren().addAll(summoner1FieldBox, champ1box);
			//summoner2
			VBox summoner2 = new VBox();
			JFXTextField summoner2field = new JFXTextField();   
			summoner2field.setPromptText("Summoner 2");   
			JFXComboBox<String> champ2box = new JFXComboBox<String>();
			champ2box.getItems().addAll(data.champNames);
			champ2box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ2box);
			HBox summoner2FieldBox = new HBox();
			summoner2FieldBox.setAlignment(Pos.CENTER);
			summoner2field.setMinWidth(250);
			summoner2FieldBox.getChildren().addAll(summoner2field, new ImageView("/images/empty.png")); 
			summoner2.getChildren().addAll(summoner2FieldBox, champ2box);
			//summoner3
			VBox summoner3 = new VBox();
			JFXTextField summoner3field = new JFXTextField(); 
			summoner3field.setPromptText("Summoner 3");     
			JFXComboBox<String> champ3box = new JFXComboBox<String>();
			champ3box.getItems().addAll(data.champNames);
			champ3box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ3box);
			HBox summoner3FieldBox = new HBox();
			summoner3FieldBox.setAlignment(Pos.CENTER);
			summoner3field.setMinWidth(250);
			summoner3FieldBox.getChildren().addAll(summoner3field, new ImageView("/images/empty.png")); 
			summoner3.getChildren().addAll(summoner3FieldBox, champ3box);
			//summoner4
			VBox summoner4 = new VBox();
			JFXTextField summoner4field = new JFXTextField();   
			summoner4field.setPromptText("Summoner 4"); 
			JFXComboBox<String> champ4box = new JFXComboBox<String>();
			champ4box.getItems().addAll(data.champNames);
			champ4box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ4box);
			HBox summoner4FieldBox = new HBox();
			summoner4FieldBox.setAlignment(Pos.CENTER);
			summoner4field.setMinWidth(250);
			summoner4FieldBox.getChildren().addAll(summoner4field, new ImageView("/images/empty.png")); 
			summoner4.getChildren().addAll(summoner4FieldBox, champ4box);
			//summoner5
			VBox summoner5 = new VBox();
			JFXTextField summoner5field = new JFXTextField();   
			summoner5field.setPromptText("Summoner 5");     
			JFXComboBox<String> champ5box = new JFXComboBox<String>();
			champ5box.getItems().addAll(data.champNames);
			champ5box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ5box);
			HBox summoner5FieldBox = new HBox();
			summoner5FieldBox.setAlignment(Pos.CENTER);
			summoner5field.setMinWidth(250);
			summoner5FieldBox.getChildren().addAll(summoner5field, new ImageView("/images/empty.png")); 
			summoner5.getChildren().addAll(summoner5FieldBox, champ5box);

			/* Create modifiable vector of summoner fields */
			Vector<JFXTextField> summonerFields = new Vector<JFXTextField>();
			summonerFields.addElement(summoner1field);
			summonerFields.addElement(summoner2field);
			summonerFields.addElement(summoner3field);
			summonerFields.addElement(summoner4field);
			summonerFields.addElement(summoner5field);
			/* Create modifiable vector of champ fields */
			Vector<ComboBox> champFields = new Vector<ComboBox>();
			champFields.addElement(champ1box);
			champFields.addElement(champ2box);
			champFields.addElement(champ3box);
			champFields.addElement(champ4box);
			champFields.addElement(champ5box);

			summonerlist.getItems().addAll(summoner1, summoner2, summoner3, summoner4, summoner5);
			BorderPane.setMargin(summonerlist, new Insets(20));

			/* Button for importing image and calling OCR methods */
			JFXButton importButton = new JFXButton("Import Clipboard");
			importButton.setOnMouseClicked(event -> {
				try {
					screenshot = clipboardimage.getImageFromClipboard();
					if(screenshot != null) {
						data.screenImage(screenshot);
						for(int i = 0; i < 5; i++){
							if(summonerFields.get(i).getText().equals("")){
								summonerFields.get(i).setText(data.summonerNames.get(i));
							}
						}
						for(int i = 0; i < 5; i++){
							if(champFields.get(i).getValue() == null){
								System.out.println("champion set: " + data.summonerChampNames.get(i));
								champFields.get(i).setValue((data.summonerChampNames.get(i)));
							}
						}
					}else{
						createDialog("NOPE", "No image found on the clipboard. \n"
								+ "Take the screenshot after opening QuickStats.", 
								new Image("/images/teemo.jpg"), null);
					}

				} catch (IOException e) {
					createDialog("ERROR", "Error importing image. \n"
							+ "I blame you. What were you uploading?", 
							new Image("/images/teemo.jpg"), null);
				} catch (TesseractException e) {
					createDialog("WHOOPS", "Error with Tesseract OCR. \n"
							+ "That definitely shouldn't happen.\n", 
							new Image("/images/teemo.jpg"), null);
					e.printStackTrace();
				}
			});
			importButton.setId("analyze-button");

			/* Button for updating summoner info and GUI */
			JFXButton fetchButton = new JFXButton("Fetch Data");
			fetchButton.setId("fetch-button");
			fetchButton.setOnAction(event -> {

				// Removing all old graphics
				rightpane.getChildren().clear(); 
				centerpane.getChildren().clear();
				for(VBox vbox : summonerlist.getItems()){
					HBox hbox = (HBox) vbox.getChildren().get(0);
					ImageView summonerStatusView = (ImageView) hbox.getChildren().get(1);
					summonerStatusView.setImage(new Image("/images/empty.png"));
				}

				/* Reset count values */
				totalSummoners = 0;
				totalChampions = 0;
				totalWinRate = 0;
				totalTier = 0;
				totalRank = 0;
				totalLP = 0;
				emptyWinLosses = 0;

				// loop over summoners and champions for info. 
				for(int i = 0; i < 5; i++){
					VBox vbox = summonerlist.getItems().get(i); // current VBox within summonerlist
					HBox hbox = (HBox) vbox.getChildren().get(0); // HBox within VBox
					ImageView summonerStatusView = (ImageView) hbox.getChildren().get(1); //Imageview within HBox

					System.out.println("Info " + i);
					String champ = "";
					if(champFields.get(i).getValue() != null){
						champ = champFields.get(i).getValue().toString();
					}
					String summoner = summonerFields.get(i).getText();	
					SummonerInfo info = new SummonerInfo();
					if(!summoner.equals("")){
						info = data.getSummonerData(summoner, champ); // Riot API calls
					}
					try{
						if(info.errorcode!= -1){ // A RiotApiException was thrown
							handleRiotException(new RiotApiException(info.errorcode), summoner);
						}
					}catch(NullPointerException e){

					}
					// Handling champion info and picture
					if(champ.equalsIgnoreCase("") || info == null || info.errorcode == 404){ // champ or summoner empty or not found
						StackPane emptybox = new StackPane();
						emptybox.setMinHeight(136);
						emptybox.setMaxHeight(136);
						rightpane.getChildren().add(emptybox);
					}else{
						totalChampions++;
						System.out.println("Champ: " + champFields.get(i).getValue().toString());
						StackPane imageStackPane = new StackPane();
						BorderPane imageBorderPane = new BorderPane();
						imageStackPane.setMinSize(600,  100);
						imageBorderPane.setMaxSize(600,  100);
						imageStackPane.setId("image-box");
						Image champsplash = data.getChampSplashArt(champ);
						ImageView champSplashView = new ImageView(champsplash);
						String masteryLevel = Integer.toString(info.champmasterylevel);
						String masteryURL = "images/mastery/level" + masteryLevel + ".png";
						Image masteryImage = new Image(masteryURL);
						ImageView masteryView = new ImageView(masteryImage);


						JFXButton masteryPointsButton = new JFXButton("View Full Mastery Profile");
						masteryPointsButton.setAlignment(Pos.CENTER);
						masteryPointsButton.setId("link-mastery-button");
						masteryPointsButton.setOnMouseClicked(evt ->{
							URI u = null;
							try {
								u = new URI("https://www.masterypoints.com/player/" +  URLEncoder.encode(summoner, "UTF-8") + "/" + region);
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							try {
								java.awt.Desktop.getDesktop().browse(u);
							} catch (IOException e) {
								e.printStackTrace();
							}
						});
						Button masteryButton = new Button();
						masteryButton.setId(Integer.toString(i)); //marker for masterypoints.com image
						masteryButton.setStyle("-fx-background-color:transparent; -fx-focus-color: transparent");
						masteryButton.setGraphic(masteryView);
						masteryButton.setTooltip(new Tooltip(capitalize(summoner) + " Mastery Summary"));
						masteryButton.setOnMouseClicked(evt ->{
							String masteryPointsURL = "";
							try {
								masteryPointsURL = "https://www.masterypoints.com/image/profile/" +  URLEncoder.encode(summoner, "UTF-8") + "/" + region;
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							//masteryImages[]
							createURLDialog(null, null, Integer.parseInt(masteryButton.getId()), masteryPointsURL, masteryPointsButton);
							masteryButton.setGraphic(new ImageView(new Image("/images/mastery/level" + masteryLevel + "click.png")));
						});
						masteryButton.setOnMouseEntered(evt->{
							masteryButton.setGraphic(new ImageView(new Image("/images/mastery/level" + masteryLevel + "hover.png")));
						});
						masteryButton.setOnMouseExited(evt->{
							masteryButton.setGraphic(new ImageView(new Image("/images/mastery/level" + masteryLevel + ".png")));
						});
						StackPane.setAlignment(masteryButton, Pos.CENTER_LEFT);
						Label masteryPoints = new Label(NumberFormat.getNumberInstance(Locale.US).format(info.champmasterypoints) + " Points");
						masteryPoints.setId("info-label");
						masteryPoints.setTextAlignment(TextAlignment.CENTER);
						VBox champMasteryBox = new VBox();
						champMasteryBox.setMinWidth(140);
						champMasteryBox.setId("mastery-box");
						champMasteryBox.setAlignment(Pos.CENTER);
						champMasteryBox.getChildren().addAll(masteryButton, masteryPoints);
						imageBorderPane.setLeft(champMasteryBox);
						Label championGamesTotal = new Label(info.champgamestotal + " Games");
						championGamesTotal.setId("info-label");
						Label championGames = new Label("W " + info.champgameswon + "  /  " + info.champgameslost + " L");
						championGames.setId("info-label");
						championGames.setTextAlignment(TextAlignment.CENTER);
						double winlossratio = (double) info.champgameswon / (double) info.champgamestotal;
						if(Double.isNaN(winlossratio)){ // Divide by zero check
							winlossratio = 0;
						}
						if(info.rankedtotalwins != 0 || info.rankedtotalwins != 0){ // add winrate only if enough games
							totalWinRate+= winlossratio;
						}else{
							emptyWinLosses++;
						}
						ProgressBar winloss = new ProgressBar(winlossratio);
						Label winpercent = new Label(String.format("%.2f", winlossratio*100) + "%");
						winpercent.setId("info-label");
						// Game Stats Box
						VBox gameStatsBox = new VBox();
						gameStatsBox.setMinWidth(140);
						gameStatsBox.setId("game-stats-box");
						gameStatsBox.setAlignment(Pos.CENTER);
						gameStatsBox.getChildren().addAll(championGamesTotal, championGames, winloss, winpercent);
						imageBorderPane.setRight(gameStatsBox);

						Label summonerLabel = new Label(capitalize(summoner));
						summonerLabel.setId("summoner-label");
						imageBorderPane.setCenter(summonerLabel);
						imageBorderPane.setAlignment(summonerLabel, Pos.BOTTOM_CENTER);
						imageStackPane.getChildren().addAll(champSplashView, imageBorderPane);
						rightpane.getChildren().add(imageStackPane);
					}

					// Handling general summoner info and pictures
					if(summoner.equalsIgnoreCase("") || summoner == null || info == null || info.errorcode == 404){ // no summoner
						StackPane emptybox = new StackPane();
						emptybox.setMinHeight(142);
						centerpane.getChildren().add(emptybox);
						if(info.errorcode == 404){ //summoner not found in riot's database
							summonerStatusView.setImage(new Image("/images/exclamation.png"));
						}
					}else{
						totalSummoners++;
						summonerStatusView.setImage(new Image("/images/green.png"));
						StackPane summonerbox = new StackPane();
						summonerbox.setPadding(new Insets(0));
						summonerbox.setId("summoner-box");
						summonerbox.setMinHeight(142);
						String tierURL = "images/tier/";

						if(info.ranktier.equalsIgnoreCase("master") || info.ranktier.equalsIgnoreCase("challenger") || info.ranktier.equalsIgnoreCase("provisional")){
							tierURL = tierURL  + info.ranktier.toLowerCase() + ".png";
						}else{
							tierURL = tierURL + info.ranktier.toLowerCase() + "_" + info.rankdivision.toLowerCase() + ".png";
						}
						if(info.ranktier != ""){
							totalTier+= tierToInt(info.ranktier);
						}
						if(info.rankdivision != ""){
							totalRank+= romanToInt(info.rankdivision);
						}
						totalLP+= info.ranklp;
						System.out.println(tierURL);
						Image image = new Image(tierURL);
						ImageView rankView = new ImageView(image);
						Label rankinfo = new Label(info.rankdivision.substring(0, 1).toUpperCase() + info.rankdivision.substring(1)
						+ " " + info.ranktier + " " + info.ranklp + " LP");
						rankinfo.setId("rank-label");
						//Button link to opgg for each summoner
						Button opggButton = new JFXButton("", new ImageView(new Image("/images/opgg.png")));
						opggButton.setId("opgg-button");
						opggButton.setPadding(new Insets(0));
						opggButton.setOnMouseClicked(evt ->{
							URI u = null;
							try {
								u = new URI("http://" + region.toLowerCase() + ".op.gg/summoner/userName=" +  URLEncoder.encode(summoner, "UTF-8"));
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							try {
								java.awt.Desktop.getDesktop().browse(u);
							} catch (IOException e) {
								e.printStackTrace();
							}
							opggButton.setGraphic(new ImageView(new Image("/images/opggclick.png")));

						});
						opggButton.setOnMouseReleased(evt ->{
							opggButton.setGraphic(new ImageView(new Image("/images/opgg.png")));

						});
						opggButton.setOnMouseEntered(evt ->{
							opggButton.setGraphic(new ImageView(new Image("/images/opgghover.png")));

						});
						opggButton.setOnMouseExited(evt ->{
							opggButton.setGraphic(new ImageView(new Image("/images/opgg.png")));
						});
						summonerbox.getChildren().addAll(rankView, rankinfo, opggButton);
						StackPane.setAlignment(opggButton, Pos.TOP_RIGHT);
						StackPane.setAlignment(rankinfo,  Pos.BOTTOM_CENTER);
						centerpane.getChildren().add(summonerbox);
					}
				}
				/* Setting up bottom pane averages */
				if(totalSummoners != 0){
					VBox bottomBox = new VBox();
					bottomBox.setAlignment(Pos.TOP_CENTER);

					double averageWinRate = totalWinRate / (totalChampions - emptyWinLosses);
					Label averageWinRateLabel = new Label("Average Win Rate: " + String.format("%.2f", averageWinRate*100) + "%");
					averageWinRateLabel.setId("summoner-label");
					int averageTier = totalTier / totalSummoners;
					int averageRank = totalRank / totalSummoners;
					int averageLP = totalLP / totalSummoners;
					Label averageRankLabel = new Label("Average Rank: " + intToTier(averageTier).toUpperCase() + " " + intToRoman(averageRank).toUpperCase() + " " + averageLP + " LP");
					averageRankLabel.setId("summoner-label");
					bottomBox.getChildren().add(averageWinRateLabel);
					bottomBox.getChildren().add(averageRankLabel);
					bottompane.getChildren().add(bottomBox);
				}

			});

			/* Clear all current input for the device */
			JFXButton resetButton = new JFXButton("Reset");
			resetButton.setId("reset-button");
			resetButton.setMaxHeight(10);
			resetButton.setOnAction(event ->{
				summoner1field.clear();
				summoner2field.clear();
				summoner3field.clear();
				summoner4field.clear();
				summoner5field.clear();
				champ1box.setValue(null);
				champ2box.setValue(null);
				champ3box.setValue(null);
				champ4box.setValue(null);
				champ5box.setValue(null);
				fetchButton.fire();
				bottompane.getChildren().clear();
				masteryImages = new Image[5];
			});

			BorderPane.setMargin(leftpane, new Insets(20, 20, 0, 20));
			HBox hButtonBox = new HBox();
			hButtonBox.setSpacing(5);
			Random rand = new Random();
			VBox vButtonBox = new VBox();
			vButtonBox.setAlignment(Pos.CENTER);
			vButtonBox.setSpacing(5);
			vButtonBox.getChildren().addAll(hButtonBox, resetButton);
			hButtonBox.getChildren().addAll(importButton, fetchButton);
			leftpane.setAlignment(Pos.CENTER);
			leftpane.getChildren().addAll(summonerlist, vButtonBox);
			mainpane.setLeft(leftpane);
			mainpane.setRight(rightpane);
			mainpane.setCenter(centerpane);
			mainpane.setBottom(bottompane);


		} catch (Exception e) {
			System.out.println("Exception occurred in Initialize");
			e.printStackTrace();
		}
	}
	/* ---------------------------------------------------------------------------
	END OF INITIALIZE */



	/** Pass the main stage and scene to the controller */
	public void setStage(Stage stage, Scene scene) {
		this.scene = scene;
		this.stage = stage;
	}

	/** Create a JFXDialog box in the current StackPane
	 * @param header text for dialog title
	 * @param message text for dialog body
	 * @param icon image for right side */
	public JFXDialog createDialog(String header, String message, Image background, JFXButton button){
		if(dialogShowing){ // Don't display multiple dialogs
			return null;
		}
		dialogShowing = true;
		JFXDialog dialog = new JFXDialog();
		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		if(header != null){
			Label headerLabel = new Label(header);
			headerLabel.setId("header-label");;
			headerLabel.setAlignment(Pos.CENTER);
			HBox headerbox = new HBox();
			headerbox.getChildren().add(headerLabel);
			headerbox.setAlignment(Pos.CENTER);
			dialogLayout.setHeading(headerbox);
		}

		BorderPane mainbody = new BorderPane();
		mainbody.setMinSize(800,  400);
		if(message != null){
			Label messageLabel = new Label(message);
			mainbody.setCenter(messageLabel);
			BorderPane.setAlignment(messageLabel, Pos.CENTER_LEFT);
		}

		dialogLayout.setBackground(new Background(new BackgroundImage(background, BackgroundRepeat.NO_REPEAT, 
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(750, 400, false, false, true, true))));

		//mainbody.setRight(new ImageView(icon));
		if(button != null){
			mainbody.setBottom(button);
		}
		dialogLayout.setBody(mainbody);
		dialog.requestFocus();
		dialog.setContent(dialogLayout);
		dialog.setTransitionType(DialogTransition.CENTER);

		dialog.show((StackPane) mainpane.getParent());
		dialog.setOnDialogClosed(event ->{
			dialogShowing = false;
		});
		return dialog;
	}

	/** Creates a dialog for the masterypoints.com profile */
	public void createURLDialog(String header, String message, int arrayLoc, String imgURL, JFXButton button){
		if(dialogShowing){ // Don't display multiple dialogs
			return;
		}
		dialogShowing = true;
		JFXDialog dialog = new JFXDialog();
		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		if(header != null){
			Label headerLabel = new Label(header);
			headerLabel.setId("header-label");;
			headerLabel.setAlignment(Pos.CENTER);
			HBox headerbox = new HBox();
			headerbox.getChildren().add(headerLabel);
			headerbox.setAlignment(Pos.CENTER);
			dialogLayout.setHeading(headerbox);
		}
		BorderPane mainbody = new BorderPane();

		Label messageLabel = new Label("Loading...");
		mainbody.setCenter(messageLabel);
		BorderPane.setAlignment(messageLabel, Pos.CENTER);

		ImageView masteryView = new ImageView();
		masteryView.setImage(new Image("images/loading.gif"));
		if(masteryImages[arrayLoc] == null){ //masteryImage array loc not set yet
			final Thread thread = new Thread(){
				public void run(){
					updateImage(imgURL);
					Platform.runLater(() -> {
						masteryView.setImage(currentMasteryImage);
						mainbody.setCenter(masteryView);
					});
					masteryImages[arrayLoc] = currentMasteryImage;
					System.out.println("completed task");
				}
			};
			thread.start();

		}else{
			masteryView.setImage(masteryImages[arrayLoc]);
			mainbody.setCenter(masteryView);
		}
		mainbody.setMinSize(400,  120);

		if(button != null){
			mainbody.setBottom(button);
			BorderPane.setAlignment(button, Pos.CENTER);
		}
		dialogLayout.setStyle("-fx-background-color: rgb(0, 0, 0)");
		dialogLayout.setBody(mainbody);
		dialog.requestFocus();
		dialog.setContent(dialogLayout);
		dialog.setTransitionType(DialogTransition.CENTER);
		dialog.show((StackPane) mainpane.getParent());
		dialog.setOnDialogClosed(event ->{
			dialogShowing = false;
		});
	}

	/** Called by thread process to prevent GUI from freezing in masterypoints dialog*/
	public void updateImage(String url){
		currentMasteryImage = new Image(url);
	}


	/** Capitalize the first letter of all words in a given string. */
	public static String capitalize(String text){
		String c = (text != null)? text.trim() : "";
		String[] words = c.split(" ");
		String result = "";
		for(String w : words){
			result += (w.length() > 1? w.substring(0, 1).toUpperCase(Locale.US) + w.substring(1, w.length()).toLowerCase(Locale.US) : w) + " ";
		}
		return result.trim();
	}

	/** Try to initialize LeagueData with proper Riot Directory 
	 * @throws IOException 
	 * @throws MalformedURLException */
	public void initializeLeagueData() throws MalformedURLException, IOException{
		try{
			api.setRegion(Region.valueOf(region));
			data = new LeagueData(riotDirectory, ImageIO.read(this.getClass().getResource("/images/blankimg.jpg")));
			System.out.println("data success");
			if(data.champIcons == null || data.champSplashArt == null){
				riotDirectoryDialog();
			}
		}catch(RiotApiException e){
			handleRiotException(e, null);
		}
	}

	/** Save the settings properties for QuickStats. 
	 * @param workingDir Properties save location
	 * @param riotDir for loading Riot image data
	 * @param region for Riot API user region */
	public void saveProperties(String workingDir, String riotDir, String region) {
		try {            
			// Create a properties file
			props.setProperty("Riot directory", riotDir);
			props.setProperty("Region", region);
			File f = new File(workingDir + "//user.properties");
			OutputStream out = new FileOutputStream( f );
			props.store(out, "QuickStats Settings");
		}
		catch (Exception e ) {
			e.printStackTrace();
		}
	}

	/** Load properties from a given directory (should be working dir)
	 * @param workingDir
	 * @return Properties from given directory */
	public Properties loadProperties(String workingDir) {
		try {            
			InputStream in = new FileInputStream(workingDir + "\\user.properties");
			Properties properties = new Properties();
			System.out.println(workingDir + "\\user.properties");
			properties.load(in);
			in.close();
			return properties;	       
		}
		catch (Exception e ) {
			e.printStackTrace();
		}
		return null;
	}

	/** Open a File Chooser to select a directory  */
	public String getRiotDirectory() {
		DirectoryChooser  chooser = new DirectoryChooser();
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.setTitle("Select Riot Games Directory");
		File file = chooser.showDialog(stage);
		if (file != null) {
			return file.getAbsolutePath();
		}
		return null;
	}

	/** Display a JFXDialog notifying the user to select a valid Riot Games directory. */
	public void riotDirectoryDialog(){
		JFXDialog dialog = new JFXDialog();
		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		Label headerLabel = new Label("Invalid Riot Directory");
		headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		HBox headerbox = new HBox();
		headerbox.setPadding(new Insets(1, 1, 1, 1));
		headerbox.getChildren().add(headerLabel);
		dialogLayout.setHeading(headerbox);
		BorderPane mainbody = new BorderPane();
		Label messageLabel = new Label("The specified directory for Riot Games did not have "
				+ "the necessary files. Please select the Riot Games directory.");
		messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
		mainbody.setCenter(messageLabel);
		mainbody.setRight(new ImageView("/images/teemo.jpg"));
		JFXButton selectDir = new JFXButton("Select Riot Directory");
		selectDir.setId("directory-button");
		selectDir.setOnAction(event ->{
			selectDir.setDisable(true);
			messageLabel.setText("Please wait...");
			riotDirectory = getRiotDirectory();
			try {
				initializeLeagueData();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			dialog.close();
		});
		mainbody.setBottom(selectDir);
		dialogLayout.setBody(mainbody);
		dialog.requestFocus();
		dialog.setContent(dialogLayout);
		dialog.setTransitionType(DialogTransition.CENTER);
		dialog.setStyle("-fx-background-color: rgba(0.0,0.0,0.0,.5);"
				+ "	-fx-padding: 1px 1px 1px 1px;");
		dialog.show((StackPane) mainpane.getParent());
	}


	/** Create and display JFXDialogs for different Riot API error codes.
	 * @param e error.
	 */
	public void handleRiotException(RiotApiException e, String name){
		System.out.println(e.getErrorCode());
		if(e.getErrorCode() == 601){
			createDialog("YOU NEED A MAP", "There's an I/O exception.\n"
					+ "Probably internet related.\n"
					+ "Check your connection.", 
					new Image("/images/ezreal.jpg"), null);
		}else if(e.getErrorCode() == 429){
			createDialog("RIOT PLS", "My API rate is limited.\n"
					+ "Try again soon.", 
					new Image("/images/teemo.jpg"), null);
		}else if(e.getErrorCode() == 603){
			createDialog("RIOT PLS", "API request timed out.\n"
					+ "Riot's API servers may be having trouble.", 
					new Image("/images/teemo.jpg"), null);
		}else if(e.getErrorCode() == 404){
			createDialog("POOR LOST SOULS", "The summoner " + name + " was not found.\n"
					+ "You may need to manually fix the summoner name.",
					new Image("/images/thresh.jpg"), null);
		}else if(e.getErrorCode() == 403){
			JFXButton linkButton = new JFXButton("LoLQuickStats Github");
			linkButton.setId("link-button");
			linkButton.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent actionEvent) {
					URI u = null;
					try {
						u = new URI("https://github.com/jakewebber/LoLQuickStats#readme");
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					try {
						java.awt.Desktop.getDesktop().browse(u);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			createDialog("OUTDATED VERSION?", "Make sure that you have the latest"
					+ " update. \n"
					+ "Your version is " + version + "\n"
					+ "Otherwise, Riot's API may be unavailable at this time.",
					new Image("/images/zilean.jpg"), linkButton);
		}else if(e.getErrorCode() == 503){
			createDialog("SERVICE UNAVAILABLE", "Riot couldn't even.\n"
					+ "Just chill and try later.",
					new Image("/images/bard.jpg"), null);
		}else{
			createDialog("OK.", "I didn't handle this error.\n"
					+ "Error code is " + e.getErrorCode() + " " + e.getMessage()
					+ "\nGood Luck.",
					new Image("/images/rammus.jpg"), null);
		}
	}

	/** Return an integer value (low rank to high rank) for a rank string. */
	public int tierToInt(String rank){
		int n = -1;
		if(rank.equalsIgnoreCase("provisional")){
			n = 0;
		}else if(rank.equalsIgnoreCase("bronze")){
			n = 1;
		}else if(rank.equalsIgnoreCase("silver")){
			n = 2;
		}else if(rank.equalsIgnoreCase("gold")){
			n = 3;
		}else if(rank.equalsIgnoreCase("platinum")){
			n = 4;
		}else if(rank.equalsIgnoreCase("diamond")){
			n = 5;
		}else if(rank.equalsIgnoreCase("master")){
			n = 6;
		}else if(rank.equalsIgnoreCase("challenger")){
			n = 7;
		}
		return n;
	}
	/** Return an string value (low rank to high rank) for a int rank (1-7). */
	public String intToTier(int n){
		String rank = "";
		if(n == 0){
			rank = "provisional";
		}else if(n == 1){
			rank = "bronze";
		}else if(n == 2){
			rank = "silver";
		}else if(n == 3){
			rank = "gold";
		}else if(n == 4){
			rank = "platinum";
		}else if(n == 5){
			rank = "diamond";
		}else if(n == 6){
			rank = "master";
		}else if(n == 7){
			rank = "challenger";
		}
		return rank;
	}
	/** Return an integer value (1-5) from roman numeral rank. */
	public int romanToInt(String roman){
		int n = 0;
		if(roman.equalsIgnoreCase("i")){
			n = 1;
		}else if(roman.equalsIgnoreCase("ii")){
			n = 2;
		}else if(roman.equalsIgnoreCase("iii")){
			n = 3;
		}else if(roman.equalsIgnoreCase("iv")){
			n = 4;
		}else if(roman.equalsIgnoreCase("v")){
			n = 5;
		}
		return n;
	}

	/** Return a roman numeral rank from integer value (1-5). */
	public String intToRoman(int n){
		String roman = "";
		if(n == 1){
			roman = "I";
		}else if(n == 2){
			roman = "II";
		}else if(n == 3){
			roman = "III";
		}else if(n == 4){
			roman = "IV";
		}else if(n == 5){
			roman = "V";
		}
		return roman;
	}

	/** Create toolbar window buttons */
	class WindowButtons extends HBox {
		public WindowButtons() {
			JFXComboBox<String>  regionComboBox = new JFXComboBox<String>();
			regionComboBox.setMaxHeight(20);
			regionComboBox.getItems().addAll(	
					Region.NA.toString().toUpperCase(),
					Region.EUW.toString().toUpperCase(),
					Region.EUNE.toString().toUpperCase(),
					Region.BR.toString().toUpperCase(),
					Region.KR.toString().toUpperCase(),
					Region.LAN.toString().toUpperCase(),
					Region.LAS.toString().toUpperCase(),
					Region.OCE.toString().toUpperCase(),
					Region.PBE.toString().toUpperCase(),
					Region.RU.toString().toUpperCase(),
					Region.TR.toString().toUpperCase()
					);
			regionComboBox.valueProperty().setValue(api.getRegion().toString().toUpperCase());
			regionComboBox.valueProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
					System.out.println(ov.getValue());
					api.setRegion(Region.valueOf(ov.getValue().toString()));
					region = ov.getValue().toString();
					System.out.println(api.getRegion().toString());
				}
			});
			api.setRegion(Region.valueOf(region));

			/* Create about button */
			JFXButton aboutBtn = new JFXButton("?");
			aboutBtn.setId("about-button");
			aboutBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					JFXDialog dialog = new JFXDialog();
					dialog.setMinHeight(500);

					Label description = new Label("Welcome to LoLQuickStats Beta 1.0, \n"
							+ "The fastest way to discover in-depth statistics about your team from the in-game lobby.\n"
							+ "Information like rank status, individual champion win rates, and mastery level for your \n"
							+ "entire game lobby with just a few clicks.\n\n"
							+ "No one but Lee Sin should go in totally blind.\n"
							+ "                                                                  "
							+ "                                                              Cheers,\n"
							+ "                                                                  "
							+ "                                                              Jacob Webber");
					Label disclaimer = new Label(
							"LoLQuickStats isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involved in \n"
									+ "producing or managing League of Legends. League of Legends and Riot Games are trademarks or registered trademarks of Riot Games, Inc. \n"
									+ "League of Legends � Riot Games, Inc.");
					disclaimer.setId("disclaimer-text");

					JFXButton linkButton = new JFXButton("Github Page");
					linkButton.setId("website-button");
					linkButton.setOnAction(new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent actionEvent) {
							URI u = null;
							try {
								u = new URI("https://github.com/jakewebber/LoLQuickStats#readme");
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
							try {
								java.awt.Desktop.getDesktop().browse(u);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
					JFXButton reportbutton = new JFXButton("Email Me");
					reportbutton.setId("website-button");
					reportbutton.setOnAction(new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent actionEvent) {
							Desktop desktop = Desktop.getDesktop();
							String message = "mailto:jacobwwebber@gmail.com?subject=LoLQuickStats%20Bug%20Report";
							URI uri = URI.create(message);
							try {
								desktop.mail(uri);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
					
					JFXButton donateButton = new JFXButton("Be My Hero");
					donateButton.setId("website-button");
					donateButton.setOnAction(new EventHandler<ActionEvent>(){
						@Override
						public void handle(ActionEvent actionEvent) {
							URI u = null;
							try {
								u = new URI("https://www.paypal.me/jacobwwebber");
							} catch (URISyntaxException e) {
								e.printStackTrace();
							}
							try {
								java.awt.Desktop.getDesktop().browse(u);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});

					description.setPadding(new Insets(20));
					description.setAlignment(Pos.CENTER);
					description.setId("info-text");

					JFXTabPane tabPane = new JFXTabPane();
					tabPane.setMinHeight(500);
					VBox aboutbox = new VBox();
					aboutbox.setAlignment(Pos.TOP_CENTER);
					ImageView banner = new ImageView(new Image("/images/banner.png"));
					aboutbox.setPadding(new Insets(10, 10, 10, 10));
					aboutbox.getChildren().addAll(banner, description, disclaimer, linkButton);

					aboutbox.setBackground(new Background(new BackgroundImage(new Image("/images/background2.jpg"), BackgroundRepeat.NO_REPEAT, 
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(750, 400, false, false, true, true))));
				
					Tab abouttab = new Tab("About");
					abouttab.setContent(aboutbox);
					Tab instructionstab = new Tab("Instructions");
					StackPane instructionspane = new StackPane();
					instructionspane.setBackground(new Background(new BackgroundImage(new Image("/images/ryze.png"), BackgroundRepeat.NO_REPEAT, 
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(750, 400, false, false, true, true))));
					VBox instructionsbox = new VBox(40);
					instructionsbox.setPadding(new Insets(200, 10, 10, 10));
					instructionsbox.setAlignment(Pos.BOTTOM_CENTER);
					ScrollPane sp = new ScrollPane();
					instructionspane.getChildren().addAll(sp);
					sp.setMaxHeight(550);
					VBox.setVgrow(sp, Priority.ALWAYS);
					sp.setFitToHeight(true);
					sp.setFitToWidth(true);
					sp.setContent(instructionsbox);
					sp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
					HBox instr1box = new HBox(10);
					Label instr = new Label("Manually enter summoner names and champions as they lock in,\n"
							+ "or use alt + printscreen to get a screenshot of your League of Legends lobby.\n"
							+ "Press Import Clipboard to pull the screenshot from your clipboard and analyze.\n"
							+ "Fix errors in the name parsing, if any (OCR is finicky).\n");
					instr.setId("about-text");
					instr.setAlignment(Pos.CENTER);
					ImageView instr1view = new ImageView(new Image("/images/instr1.png"));
					instr1view.setEffect(new DropShadow());
					instr1box.getChildren().addAll(instr1view, instr);
					HBox instr2box = new HBox(10);
					Label instr2 = new Label("Press Fetch Data to get stats about your team and the champions they're playing.\n"
							+ "You can Fetch Data for any amount of information filled out about your team,\n"
							+ "so tier, rank, and division are available at any time during your game lobby.");
					instr2.setId("about-text");
					instr2.setAlignment(Pos.CENTER);
					ImageView instr2view = new ImageView(new Image("/images/instr2.png"));
					instr2view.setEffect(new DropShadow());
					instr2box.getChildren().addAll(instr2view, instr2);				
					VBox instr3box = new VBox(10);
					instr3box.setAlignment(Pos.CENTER);
					Label instr3 = new Label("OP.GG buttons link to the summoner's OP.GG for detailed game stats.\n"
							+ "Champion mastery buttons link to their MasteryPoints profile summary.\n"
							+ "Average winrate and rank for your team are listed at the bottom.");
					instr3.setId("about-text");
					instr3.setAlignment(Pos.CENTER);
					HBox instr3textbox = new HBox();
					instr3textbox.setMinHeight(100);
					instr3textbox.setAlignment(Pos.CENTER);
					instr3textbox.getChildren().add(instr3);
					ImageView instr3view = new ImageView(new Image("/images/instr3.png"));
					instr3view.setEffect(new DropShadow());
					instr3box.getChildren().addAll(instr3textbox, instr3view);
					HBox instr4box = new HBox(10);
					Label instr4 = new Label("Press Clear to empty all fields and current fetched information.\n"
							+ "Import Clipboard will not overwrite existing fields, so clear before importing.");
					instr4.setId("about-text");
					instr4.setAlignment(Pos.CENTER);
					ImageView instr4view = new ImageView(new Image("/images/instr4.png"));
					instr4view.setEffect(new DropShadow());
					instr4box.getChildren().addAll(instr4view, instr4);
					instructionsbox.getChildren().addAll(instr1box, instr2box, instr4box, instr3box);
					instructionstab.setContent(instructionspane);
					
					/* Bug report tab */
					VBox reportbox = new VBox(30);
					reportbox.setAlignment(Pos.TOP_CENTER);
					reportbox.setPadding(new Insets(10, 10, 10, 10));
					Label errorLabel = new Label("Found a bug?\n"
							+ "Email me at jacobwwebber@gmail.com with a description.\n"
							+ "I'll do what I can to hunt it down.");
					errorLabel.setId("about-text");
					reportbox.getChildren().addAll(errorLabel, reportbutton);

					reportbox.setBackground(new Background(new BackgroundImage(new Image("/images/khazix.jpg"), BackgroundRepeat.NO_REPEAT, 
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(750, 400, false, false, true, true))));
				
					Tab reporttab = new Tab("Bug Report");
					reporttab.setContent(reportbox);
					
					
					/* Donate tab */
					VBox donatebox = new VBox(30);
					donatebox.setAlignment(Pos.TOP_CENTER);
					donatebox.setPadding(new Insets(10, 10, 10, 10));
					Label donateLabel = new Label("Was LoLQuickStats useful?\n"
							+ "A Teemo dies to Darius dunk for every donation.\n"
							+ "Help eradicate Teemo from the Rift.\n"
							+ "Help me move out of my mom's basement.");
					donateLabel.setId("about-text");
					donatebox.getChildren().addAll(donateLabel, donateButton);

					donatebox.setBackground(new Background(new BackgroundImage(new Image("/images/darius.jpg"), BackgroundRepeat.NO_REPEAT, 
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(750, 400, false, false, true, true))));
				
					Tab donatetab = new Tab("Donate");
					donatetab.setContent(donatebox);
					
					tabPane.getTabs().addAll(abouttab, instructionstab, reporttab, donatetab);

					dialog.requestFocus();
					dialog.setContent(tabPane);
					dialog.setTransitionType(DialogTransition.TOP);
					dialog.show((StackPane) mainpane.getParent());
				}
			});

			/* Display about dialog on first open */
			if(firstOpen){
				firstOpen = false;
				aboutBtn.fire();
			}
			/* Create close button */
			JFXButton closeBtn = new JFXButton("X");
			closeBtn.setId("close-button");
			closeBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					saveProperties(System.getProperty("user.dir"), riotDirectory, region);
					Platform.exit();
				}
			});
			JFXButton minimizeBtn = new JFXButton("_");
			minimizeBtn.setId("minimize-button");
			minimizeBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					stage.setIconified(true);
				}
			});

			Pane separator = new Pane();
			separator.setMinSize(15,  0);
			this.getChildren().addAll(regionComboBox, aboutBtn, separator, minimizeBtn, closeBtn);
		}
	}

}

/** Records relative x and y coordinates. */
class Delta { double x, y; }  