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
import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.exception.APIException;
import com.robrua.orianna.type.exception.APIException.Status;
import com.robrua.orianna.type.exception.OriannaException;
import com.jfoenix.controls.JFXDialog.DialogTransition;

import javafx.animation.FadeTransition;
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
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;
import javafx.util.Duration;
import net.sourceforge.tess4j.TesseractException;
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
	public boolean canHoverSplash = true;
	public int totalSummoners = 0;
	public int totalChampions = 0;
	public double champTotalWinRate = 0;
	public double rankedTotalWinRate = 0;
	public int totalRank = 0;
	public int totalLP = 0;
	public int totalTier = 0;
	public int emptyWinLosses = 0;
	public int rankedEmptyWinLosses = 0;
	public Image[] masteryImages = new Image[5];
	public ArrayList<ImageView> splashViews = new ArrayList<ImageView>();

	Image currentMasteryImage = null;

	public double width = 800;
	public double height = 600;
	public ImageView dragView = new ImageView();

	Boolean resizebottom = false;
	private double dx;
	private double dy;
	private double xOffset = 0;
	private double yOffset = 0;


	@SuppressWarnings({ "rawtypes", "static-access", "unused", "unchecked" })
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		mainpane.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(0), new Insets(0))));
		System.out.println("width: " + width + " height: " + height);
		try {
			// If settings properties exist, load them
			if(new File(System.getProperty("user.dir") + "/user.properties").exists()){
				props = loadProperties(System.getProperty("user.dir"));
				region = props.getProperty("Region", "NA");
				riotDirectory = props.getProperty("Riot directory", "C:/Riot Games/");
				width = Double.valueOf(props.getProperty("Width"));
				height = Double.valueOf(props.getProperty("Height"));
				mainpane.setPrefWidth(width);
				mainpane.setPrefHeight(height);
			}else{ //If they do not, try to set defaults.
				firstOpen = true;
				riotDirectory = "C:/Riot Games/";
				region  = "NA";
				width =  (screenBounds.getWidth() / 1.5);
				height = (screenBounds.getHeight() / 1.22);
				if(!new File("C:/Riot Games/").exists()){ // Default riot dir not found
					riotDirectory = getRiotDirectory();
				}
			}
			//Initial API call for setting up League data variables
			try{
				initializeLeagueData();
			}catch(OriannaException e){
				createDialog("YOU NEED A MAP", "There's an I/O exception.\n"
						+ "Probably internet related.\n"
						+ "Check your connection.", 
						new Image("/images/ezreal.jpg"), null);
				e.printStackTrace();
			}

			Effect frostEffect = new BoxBlur(60, 60, 3);

			VBox rightpane = new VBox(); // Champion info pane
			VBox rightpaneholder = new VBox();
			rightpaneholder.getChildren().add(rightpane);
			rightpaneholder.setVgrow(rightpaneholder,  Priority.NEVER);
			VBox.setVgrow(rightpane, Priority.NEVER);
			rightpane.setPadding(new Insets(10, 15, 10, 0));
			rightpane.setMinHeight(50);
			rightpane.setPrefWidth(450);
			BorderPane.setAlignment(rightpane, Pos.TOP_RIGHT);
			VBox leftpane = new VBox(5); // Summoner and champ tables pane
			leftpane.setId("main-box");
			leftpane.setMaxHeight(Double.MAX_VALUE);
			leftpane.setMinHeight(450);
			leftpane.setMinWidth(250);
			leftpane.setMaxWidth(250);
			VBox bottompane = new VBox(); // Averages info pane
			bottompane.setPadding(new Insets(5, 0, 0, 0));
			HBox bottomBox = new HBox();
			bottomBox.setMinHeight(25);
			bottomBox.prefWidthProperty().bind(mainpane.minWidthProperty());
			bottomBox.setPadding(new Insets(5, 0, 0, 0));
			bottomBox.setAlignment(Pos.CENTER);
			bottomBox.setId("bottom-box");
			bottompane.getChildren().add(bottomBox);

			/* Creating new header toolbar */
			ToolBar toolBar = new ToolBar();
			int toolbarheight = 25;
			toolBar.setPrefHeight(toolbarheight);
			toolBar.setMinHeight(toolbarheight);
			toolBar.setMaxHeight(toolbarheight);

			HBox alignRight = new HBox();
			Pane pane = new Pane(alignRight);
			alignRight.setHgrow(pane, Priority.ALWAYS);
			Text title = new Text("LoL Quick Stats");
			title.setStyle("-fx-font-family: trebuchet;"
					+ "-fx-font-size: 22px;"
					+ "-fx-fill: linear-gradient(from 0% 0% to 100% 100%, goldenrod 0%, gold 50%, peru 100%);"
					+ "-fx-stroke-width: 1;");
			WindowButtons windowbuttons = new WindowButtons();
			windowbuttons.maxWidthProperty().bind(mainpane.widthProperty());
			toolBar.getItems().addAll(title, pane, windowbuttons);
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
			summonerlist.minWidthProperty().bind(mainpane.maxWidthProperty());
			summonerlist.minHeightProperty().bind(mainpane.maxHeightProperty());
			VBox summoner1 = new VBox();

			JFXTextField summoner1field = new JFXTextField(); 
			summoner1field.setId("summoner-field");
			summoner1field.setPromptText("Summoner 1");
			JFXComboBox<String> champ1box = new JFXComboBox<String>();
			champ1box.getItems().addAll(data.champNames);
			champ1box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ1box);
			HBox summoner1FieldBox = new HBox();
			summoner1FieldBox.setAlignment(Pos.CENTER);
			ImageView emptyView = new ImageView("/images/empty.png");
			summoner1FieldBox.getChildren().addAll(summoner1field, emptyView); 
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

			/* Button for importing image and calling OCR methods */
			JFXButton importButton = new JFXButton("Import");
			importButton.setOnMouseClicked(event -> {
				try {
					screenshot = clipboardimage.getImageFromClipboard();
					if(screenshot != null) {
						double diff = data.screenImage(screenshot);
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
						if(diff >= 20){
							createDialog("LOW IMAGE ACCURACY", "Champion comparison accuracy was very low from the image.\n"
									+ "Make sure your screenshot is of your League of Legends lobby using alt + printscreen.", 
									new Image("/images/blitz.jpg"), null);
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
				bottomBox.getChildren().clear();
				rightpane.getChildren().clear(); 
				splashViews.clear();
				for(VBox vbox : summonerlist.getItems()){
					HBox hbox = (HBox) vbox.getChildren().get(0);
					ImageView summonerStatusView = (ImageView) hbox.getChildren().get(1);
					summonerStatusView.setImage(new Image("/images/empty.png"));
				}
				Label rankedHeaderLabel = new Label("         Ranked Stats");
				rankedHeaderLabel.setId("info2-label");
				rankedHeaderLabel.setAlignment(Pos.CENTER);
				Label champHeaderLabel = new Label("Champion Stats");
				champHeaderLabel.setId("info2-label");
				champHeaderLabel.setAlignment(Pos.CENTER);
				HBox headerBox = new HBox(500);
				headerBox.getChildren().addAll(rankedHeaderLabel, champHeaderLabel);
				rightpane.getChildren().add(headerBox);

				/* Reset count values */
				totalSummoners = 0;
				totalChampions = 0;
				champTotalWinRate = 0;
				totalTier = 0;
				totalRank = 0;
				totalLP = 0;
				emptyWinLosses = 0;
				rankedTotalWinRate = 0;
				rankedEmptyWinLosses = 0;

				/* Create GUI for each of the 5 summoners */
				for(int i = 0; i < 5; i++){
					VBox vbox = summonerlist.getItems().get(i); // current VBox within summonerlist
					HBox hbox = (HBox) vbox.getChildren().get(0); // HBox within VBox
					ImageView summonerStatusView = (ImageView) hbox.getChildren().get(1); //Imageview within HBox
					StackPane imageStackPane = new StackPane();
					BorderPane imageBorderPane = new BorderPane();
					imageBorderPane.setPadding(new Insets(2));
					imageBorderPane.setBorder(new Border(new BorderStroke(Color.WHITE, 
							BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1, 1, 1, 1))));
					imageStackPane.setMinSize(800, 100);
					imageStackPane.setMaxSize(800, 100);
					imageBorderPane.setMinSize(800, 100);
					imageBorderPane.setMaxSize(800, 100);

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
						if(! info.errorcode.equalsIgnoreCase("")){ // A RiotApiException was thrown
							handleRiotException(APIException.Status.valueOf(info.errorcode), summoner);
						}
					}catch(NullPointerException e){

					}
					// Handling champion info and picture
					if(champ.equalsIgnoreCase("") || info == null || ! info.errorcode.equalsIgnoreCase("")|| summoner.equalsIgnoreCase("") || summoner == null){ // champ or summoner empty or not found
						imageStackPane.setId("empty");
					}else{
						totalChampions++;
						imageStackPane.setId("image-box");
						Image champsplash = data.getChampSplashArt(champ);
						ImageView champSplashView = new ImageView(champsplash);
						champSplashView.setId("splash-" + i);
						splashViews.add(champSplashView);
						champSplashView.resize(800, 100);
						champSplashView.setPreserveRatio(true);
						champSplashView.setOpacity(.7);

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
						masteryButton.setAlignment(Pos.TOP_CENTER);
						masteryButton.setMaxHeight(100);
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
						Label masteryPoints = new Label(NumberFormat.getNumberInstance(Locale.US).format(info.champmasterypoints) + " Points");
						masteryPoints.setId("info-label");
						masteryPoints.setTextAlignment(TextAlignment.CENTER);
						StackPane champMasteryBox = new StackPane();
						champMasteryBox.setMaxHeight(100);
						champMasteryBox.setMinWidth(125);
						champMasteryBox.setId("mastery-box");
						champMasteryBox.setAlignment(Pos.CENTER);
						champMasteryBox.getChildren().addAll(masteryButton, masteryPoints);
						champMasteryBox.setAlignment(masteryPoints, Pos.BOTTOM_CENTER);
						champMasteryBox.setAlignment(masteryButton, Pos.TOP_CENTER);
						imageBorderPane.setAlignment(champMasteryBox, Pos.CENTER_RIGHT);

						Label championGamesTotal = new Label(info.champgamestotal + " Games");
						championGamesTotal.setId("info-label");
						Label championGames = new Label("W " + info.champgameswon + "  /  " + info.champgameslost + " L");
						championGames.setId("info-label");
						championGames.setTextAlignment(TextAlignment.CENTER);
						double winlossratio = (double) info.champgameswon / (double) info.champgamestotal;
						if(Double.isNaN(winlossratio)){ // Divide by zero check
							winlossratio = 0;
						}
						if(info.champgameswon != 0 || info.champgameslost != 0){ // add winrate only if enough games
							champTotalWinRate+= winlossratio;
						}else{
							emptyWinLosses++;
						}
						ProgressBar winloss = new ProgressBar(winlossratio);
						Label winpercent = new Label(String.format("%.2f", winlossratio*100) + "%");
						winpercent.setId("info-label");
						// Game Stats Box
						VBox gameStatsBox = new VBox();
						gameStatsBox.setMinWidth(125);
						gameStatsBox.setId("game-stats-box");
						gameStatsBox.setAlignment(Pos.CENTER);
						gameStatsBox.getChildren().addAll(championGamesTotal, championGames, winloss, winpercent);

						/* Add BorderPane components */
						HBox champBox = new HBox();
						champBox.setMinWidth(250);
						champBox.setAlignment(Pos.CENTER_RIGHT);
						champBox.getChildren().addAll(gameStatsBox, champMasteryBox);
						BlurPane champBlurPane = new BlurPane();
						champBlurPane.getChildren().add(champBox);
						imageBorderPane.setAlignment(champBlurPane, Pos.CENTER_RIGHT);
						imageBorderPane.setRight(champBlurPane);

						imageStackPane.getChildren().addAll(champSplashView, imageBorderPane);
						final String championForURL = champ.replace("'", "").replace(" ", "").replace("Wukong", "MonkeyKing");

						imageBorderPane.setOnMouseEntered(evt->{
							FadeTransition st = new FadeTransition(Duration.millis(200), champSplashView);
							st.setAutoReverse(false);
							st.setFromValue(.7);
							st.setToValue(1);
							st.setCycleCount(1);
							st.play();

						});
						imageBorderPane.setOnMouseExited(evt->{
							FadeTransition st = new FadeTransition(Duration.millis(200), champSplashView);
							st.setAutoReverse(false);
							st.setFromValue(1);
							st.setToValue(.7);
							st.setCycleCount(1);
							st.play();

						});
						imageBorderPane.setOnMouseClicked(evt->{					
							FadeTransition st = new FadeTransition(Duration.millis(100), champSplashView);
							st.setAutoReverse(true);
							st.setFromValue(1);
							st.setToValue(.7);
							st.setCycleCount(2);
							st.play();	
							URI u = null;
							try {
								u = new URI("https://www.champion.gg/champion/" +  URLEncoder.encode(championForURL, "UTF-8"));
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
					}

					// Handling general summoner info and pictures
					if(summoner.equalsIgnoreCase("") || summoner == null || info == null 
							|| info.errorcode.equalsIgnoreCase("NOT_FOUND") || info.errorcode.equalsIgnoreCase("INTERNAL_SERVER_ERROR")){ // no summoner
						if(info.errorcode.equalsIgnoreCase("NOT_FOUND")){ //summoner not found in riot's database
							summonerStatusView.setImage(new Image("/images/exclamation.png"));
						}
					}else{
						totalSummoners++;
						summonerStatusView.setImage(new Image("/images/green.png"));
						HBox summonerBox = new HBox();

						summonerBox.setPadding(new Insets(0));
						summonerBox.setId("summoner-box");
						summonerBox.setMinHeight(97);
						summonerBox.setMaxHeight(97);
						summonerBox.setMinWidth(250);
						String tierURL = "images/tier/";
						//System.out.println("RANKTIER:" + info.ranktier);
						if(info.ranktier.equalsIgnoreCase("master") || info.ranktier.equalsIgnoreCase("challenger") || info.ranktier.equalsIgnoreCase("provisional")){
							tierURL = tierURL  + info.ranktier.toLowerCase();
						}else{
							tierURL = tierURL + info.ranktier.toLowerCase() + "_" + info.rankdivision.toLowerCase();
						}
						if(info.ranktier != ""){
							totalTier+= data.tierToInt(info.ranktier);
						}
						if(info.rankdivision != ""){
							totalRank+= data.romanToInt(info.rankdivision);
						}
						totalLP += info.ranklp;
						//System.out.println(tierURL);
						Image image = new Image(tierURL + ".png");
						ImageView rankView = new ImageView(image);
						Button rankButton = new Button();
						rankButton.setAlignment(Pos.TOP_CENTER);
						rankButton.setMaxHeight(100);
						rankButton.setId(Integer.toString(i)); //marker for masterypoints.com image
						rankButton.setStyle("-fx-background-color:transparent; -fx-focus-color: transparent");
						rankButton.setGraphic(rankView);
						rankButton.setTooltip(new Tooltip(capitalize(summoner) + " op.gg"));
						Label rankinfo = new Label(info.rankdivision.substring(0, 1).toUpperCase() + info.rankdivision.substring(1)
						+ " " + capitalize(info.ranktier.substring(0,  4).toLowerCase()) + " " + info.ranklp + " LP");
						rankinfo.setId("rank-label");
						//Button link to opgg for each summoner
						String hoverURL = tierURL;
						rankButton.setOnMouseClicked(evt ->{
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
							rankButton.setGraphic(new ImageView(new Image(hoverURL + "_hover.png")));
						});
						rankButton.setOnMouseReleased(evt ->{
							rankButton.setGraphic(new ImageView(new Image(hoverURL + ".png")));
						});
						rankButton.setOnMouseEntered(evt ->{
							rankButton.setGraphic(new ImageView(new Image(hoverURL + "_hover.png")));
						});
						rankButton.setOnMouseExited(evt ->{
							rankButton.setGraphic(new ImageView(new Image(hoverURL + ".png")));
						});
						StackPane rankedPane = new StackPane();
						rankedPane.setMaxHeight(100);
						rankedPane.setAlignment(Pos.CENTER);
						rankedPane.getChildren().addAll(rankButton, rankinfo);
						//StackPane.setAlignment(opggButton, Pos.TOP_RIGHT);
						StackPane.setAlignment(rankinfo,  Pos.BOTTOM_CENTER);
						StackPane.setAlignment(rankButton,  Pos.CENTER);

						Label rankedGamesTotal = new Label(info.rankedtotallosses + info.rankedtotalwins + " Games");
						rankedGamesTotal.setId("info-label");
						Label rankedGames = new Label("W " + info.rankedtotalwins + "  /  " + info.rankedtotallosses + " L");
						rankedGames.setId("info-label");
						rankedGames.setTextAlignment(TextAlignment.CENTER);
						double rankedwinlossratio = (double) info.rankedtotalwins / (double) (info.rankedtotalwins + info.rankedtotallosses);
						if(Double.isNaN(rankedwinlossratio)){ // Divide by zero check
							rankedwinlossratio = 0;
						}
						if(info.rankedtotalwins != 0 || info.rankedtotalwins != 0){ // add winrate only if enough games
							rankedTotalWinRate+= rankedwinlossratio;
						}else{
							rankedEmptyWinLosses++;
						}
						ProgressBar rankedwinloss = new ProgressBar(rankedwinlossratio);
						Label rankedwinpercent = new Label(String.format("%.2f", rankedwinlossratio*100) + "%");
						rankedwinpercent.setId("info-label");
						// Game Stats Box
						VBox rankedGameStatsBox = new VBox();
						rankedGameStatsBox.setMinWidth(125);
						rankedGameStatsBox.setMaxHeight(100);
						rankedGameStatsBox.setAlignment(Pos.CENTER);
						rankedGameStatsBox.getChildren().addAll(rankedGamesTotal, rankedGames, rankedwinloss, rankedwinpercent);
						summonerBox.getChildren().addAll(rankedPane, rankedGameStatsBox);
						Label summonerLabel = new Label(capitalize(summoner));
						summonerLabel.setId("summoner-label");
						imageBorderPane.setAlignment(summonerLabel, Pos.BOTTOM_CENTER);
						imageBorderPane.setCenter(summonerLabel);

						BlurPane summonerBlurPane = new BlurPane();
						summonerBlurPane.getChildren().add(summonerBox);
						imageBorderPane.setAlignment(summonerBlurPane, Pos.CENTER_LEFT);
						imageBorderPane.setLeft(summonerBlurPane);
					}
					rightpane.getChildren().add(imageStackPane);
				}
				/* Setting up bottom pane averages */
				if(totalSummoners != 0){

					double champAvgWinRate = champTotalWinRate / (totalChampions - emptyWinLosses);
					Label champAvgWinRateLabel = new Label("Average Champ Winrate: " + String.format("%.2f", champAvgWinRate*100) + "%   ");
					champAvgWinRateLabel.setId("info2-label");

					double rankedAvgWinRate = rankedTotalWinRate / (totalSummoners - rankedEmptyWinLosses);
					Label rankedAvgWinRateLabel = new Label("Average Ranked Winrate: " + String.format("%.2f", rankedAvgWinRate*100) + "%   ");
					rankedAvgWinRateLabel.setId("info2-label");
					int averageTier = totalTier / totalSummoners;
					int averageRank = totalRank / totalSummoners;
					int averageLP = totalLP / totalSummoners;
					Label averageRankLabel = new Label("Average Rank: " + data.intToTier(averageTier).toUpperCase() + " " + data.intToRoman(averageRank).toUpperCase() + " " + averageLP + " LP");
					averageRankLabel.setId("info2-label");
					bottomBox.getChildren().addAll(rankedAvgWinRateLabel, champAvgWinRateLabel, averageRankLabel);
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
						rightpane.getChildren().clear();
						bottomBox.getChildren().clear();
						masteryImages = new Image[5];
					});

					BorderPane.setMargin(leftpane, new Insets(20, 20, 0, 20));
					HBox hButtonBox = new HBox(5);
					hButtonBox.setAlignment(Pos.CENTER);
					Random rand = new Random();
					VBox vButtonBox = new VBox(5);
					vButtonBox.setAlignment(Pos.CENTER);
					vButtonBox.getChildren().addAll(hButtonBox, resetButton);
					hButtonBox.getChildren().addAll(importButton, fetchButton);
					leftpane.setAlignment(Pos.CENTER);
					leftpane.setPadding(new Insets(5));
					leftpane.getChildren().addAll(summonerlist, vButtonBox);
					leftpane.setVgrow(summonerlist, Priority.ALWAYS);
					mainpane.setLeft(leftpane);
					mainpane.setRight(rightpaneholder);
					mainpane.setBottom(bottompane);
					BackgroundImage background = new BackgroundImage(new Image("/images/background.jpg", 1000, 600,true,true),
							BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
							BackgroundSize.DEFAULT);
					mainpane.setBackground(new Background(background));
					mainpane.setBorder(new Border(new BorderStroke(Color.BLACK, 
							BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 2, 2, 2))));

					dragView.setImage(new Image("/images/cornerdrag.png"));
					mainpane.getChildren().add(dragView);
					dragView.setX(width - 17);
					dragView.setY(height - 17);
					dragView.setOnMouseEntered(new EventHandler<MouseEvent>() {
						public void handle(MouseEvent event) {
							mainpane.setCursor(Cursor.NW_RESIZE);
						}
					});
					dragView.setOnMouseExited(new EventHandler<MouseEvent>() {
						public void handle(MouseEvent event) {
							mainpane.setCursor(Cursor.DEFAULT);
						}
					});

					mainpane.setOnMousePressed(new EventHandler<MouseEvent>() {
						public void handle(MouseEvent event) {
							if (event.getX() > stage.getWidth() - 10
									&& event.getX() < stage.getWidth() + 10
									&& event.getY() > stage.getHeight() - 10
									&& event.getY() < stage.getHeight() + 10) {
								resizebottom = true;
								dx = stage.getWidth() - event.getX();
								dy = stage.getHeight() - event.getY();
							} else {
								resizebottom = false;
								xOffset = event.getSceneX();
								yOffset = event.getSceneY();
							}
						}
					});
					dragView.setOnMouseReleased(new EventHandler<MouseEvent>() {
						public void handle(MouseEvent event) {
							System.out.println("Width: " + width + " Height: " + height);
						}
					});


					mainpane.setOnMouseDragged(new EventHandler<MouseEvent>() {
						public void handle(MouseEvent event) {
							if (resizebottom == false) {
								stage.setX(event.getScreenX() - xOffset);
								stage.setY(event.getScreenY() - yOffset);
							} else {
								stage.setWidth(event.getX() + dx);
								width = event.getX() + dx;
								stage.setHeight(event.getY() + dy);
								height = event.getY() + dy;
								mainpane.setPrefSize(width,  height);
								dragView.setX(width - 17);
								dragView.setY(height - 17);
							}
						}
					});

					mainpane.setOnMouseReleased(new EventHandler<MouseEvent>() {
						public void handle(MouseEvent event) {
							mainpane.setMinWidth(width);
							mainpane.setMinHeight(height);
							mainpane.setPrefSize(width,  height);
							stage.setMinWidth(width);
							stage.setMinHeight(height);
							dragView.setX(width - 17);
							dragView.setY(height - 17);
							if(width < 1110){
								width = 1110;
								mainpane.setMinWidth(width);
								mainpane.setPrefWidth(width);
								stage.setMinWidth(width);
								dragView.setX(width - 17);
							}
							if(height < 660){
								height = 660;
								mainpane.setMinHeight(height);
								mainpane.setPrefHeight(height);
								stage.setMinHeight(height);
								dragView.setY(height - 17);
							}
						}
					});


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
		mainbody.setMinSize(700,  400);
		if(message != null){
			Label messageLabel = new Label(message);
			mainbody.setCenter(messageLabel);
			BorderPane.setAlignment(messageLabel, Pos.CENTER_LEFT);
		}

		dialogLayout.setBackground(new Background(new BackgroundImage(background, BackgroundRepeat.NO_REPEAT, 
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(700, 400, false, false, true, true))));

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

		if(header.equalsIgnoreCase("YOU NEED A MAP")){ //No internet connection
			dialog.setOnDialogClosed(e->{
				stage.close();
				System.exit(0);
			});
		}
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
		mainbody.setMinSize(width / 3.2,  height / 7.03);

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
		System.out.println("initializing league data..");
		RiotAPI.setRegion(Region.valueOf(region));
		try{
			data = new LeagueData(riotDirectory, ImageIO.read(this.getClass().getResource("/images/blankimg.jpg")));
		}catch(NullPointerException e){
			riotDirectoryDialog();
			e.printStackTrace();
		}catch(OriannaException e){
			createDialog("YOU NEED A MAP", "There's an I/O exception.\n"
					+ "Probably internet related.\n"
					+ "Check your connection.", 
					new Image("/images/ezreal.jpg"), null);
		}


		System.out.println("league data success");
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
			props.setProperty("Width", Double.toString(width));
			props.setProperty("Height", Double.toString(height));
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
		BorderPane mainbody = new BorderPane();

		Label headerLabel = new Label("Invalid Riot Directory");
		headerLabel.setId("header-label");;
		headerLabel.setAlignment(Pos.CENTER);
		HBox headerbox = new HBox();
		headerbox.getChildren().add(headerLabel);
		headerbox.setAlignment(Pos.CENTER);
		dialogLayout.setHeading(headerbox);

		Label messageLabel = new Label("The specified directory for Riot Games did not have "
				+ "the necessary files. Please select the Riot Games directory.");
		messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
		mainbody.setCenter(messageLabel);
		JFXButton selectDir = new JFXButton("Select Riot Directory");
		selectDir.setId("directory-button");
		selectDir.setOnAction(event ->{
			selectDir.setDisable(true);
			riotDirectory = getRiotDirectory();
			System.out.println("riot dir:" + riotDirectory);
			if(riotDirectory != null && !riotDirectory.equalsIgnoreCase("null")){
				messageLabel.setText("Please wait...");
				try {
					initializeLeagueData();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dialog.close();
			}
		});

		mainbody.setMinSize(width / 1.6,  height / 2.11);

		mainbody.setCenter(messageLabel);
		BorderPane.setAlignment(messageLabel, Pos.CENTER_LEFT);


		dialogLayout.setBackground(new Background(new BackgroundImage(new Image("/images/teemo.jpg"), BackgroundRepeat.NO_REPEAT, 
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(width / 1.7, height / 2.11, false, false, true, true))));

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
	 * @param status error.
	 */
	public void handleRiotException(Status status, String name){
		System.out.println(status.name());
		System.out.println(status);
		if(status == APIException.Status.UNKNOWN){
			createDialog("UNKNOWN ERROR", "Uh Oh.\n"
					+ "Riot's API may be unavailable.\n", 
					new Image("/images/ezreal.jpg"), null);
		}else if(status == APIException.Status.RATE_LIMIT_EXCEEDED){
			createDialog("RIOT PLS", "My API rate is limited.\n"
					+ "Try again soon.", 
					new Image("/images/teemo.jpg"), null);
		}else if(status == APIException.Status.INTERNAL_SERVER_ERROR){
			createDialog("RIOT PLS", "There was an internal server error.\n"
					+ "Try again soon.", 
					new Image("/images/teemo.jpg"), null);
		}else if(status == APIException.Status.SERVICE_UNAVAILABLE){ //603
			createDialog("RIOT PLS", "API request timed out.\n"
					+ "Riot's API servers may be having trouble.", 
					new Image("/images/teemo.jpg"), null);
		}else if(status == APIException.Status.NOT_FOUND){
			createDialog("POOR LOST SOULS", "The summoner " + name + " was not found.\n"
					+ "You may need to manually fix the summoner name.",
					new Image("/images/thresh.jpg"), null);
		}else if(status == APIException.Status.UNAUTHORIZED){
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
		}else if(status == APIException.Status.SERVICE_UNAVAILABLE){
			createDialog("SERVICE UNAVAILABLE", "Riot couldn't even.\n"
					+ "Just chill and try later.",
					new Image("/images/bard.jpg"), null);
		}else{
			createDialog("OK.", "I didn't handle this error.\n"
					+ "Error code is " + status.name()
					+ "\nGood Luck.",
					new Image("/images/rammus.jpg"), null);
		}
	}

	/** Create and display JFXDialogs for different Riot API error codes.
	 * @param status error.
	 */
	public void handleOriannaException(OriannaException e, String name){
		e.printStackTrace();
		System.out.println(e.getMessage());

		createDialog("YOU NEED A MAP", "There's an I/O exception.\n"
				+ "Probably internet related.\n"
				+ "Check your connection.", 
				new Image("/images/ezreal.jpg"), null);
	}



	/** Create toolbar window buttons */
	class WindowButtons extends HBox {
		public WindowButtons() {
			JFXComboBox<String>  regionComboBox = new JFXComboBox<String>();
			regionComboBox.setId("region-combobox");
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
			regionComboBox.valueProperty().setValue(region);
			regionComboBox.valueProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
					System.out.println(ov.getValue());
					RiotAPI.setRegion(Region.valueOf(ov.getValue().toString()));
					region = ov.getValue().toString();
					System.out.println(RiotAPI.getRealm().toString());
				}
			});
			RiotAPI.setRegion(Region.valueOf(region));

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
									+ "League of Legends © Riot Games, Inc.");
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
					tabPane.setMinHeight(600);
					tabPane.setMinWidth(1000);
					tabPane.setMaxWidth(1000);
					VBox aboutbox = new VBox();
					aboutbox.setAlignment(Pos.TOP_CENTER);
					ImageView banner = new ImageView(new Image("/images/banner.png"));
					aboutbox.setPadding(new Insets(10, 10, 10, 10));
					aboutbox.getChildren().addAll(banner, description, disclaimer, linkButton);

					aboutbox.setBackground(new Background(new BackgroundImage(new Image("/images/background2.jpg"), BackgroundRepeat.NO_REPEAT, 
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(800, 600, false, false, true, true))));

					Tab abouttab = new Tab("About");
					abouttab.setContent(aboutbox);
					Tab instructionstab = new Tab("Instructions");
					StackPane instructionspane = new StackPane();
					instructionspane.setBackground(new Background(new BackgroundImage(new Image("/images/ryze.png"), BackgroundRepeat.NO_REPEAT, 
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(800, 600, false, false, true, true))));
					VBox instructionsbox = new VBox(40);
					instructionsbox.setPadding(new Insets(height / 4.22, 10, 10, 10));
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
							+ "Press Import to pull the screenshot from your clipboard and analyze.");
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
							+ "Import will not overwrite existing fields, so clear before importing.");
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
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(800, 600, false, false, true, true))));

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
							BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, new BackgroundSize(800, 600, false, false, true, true))));

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