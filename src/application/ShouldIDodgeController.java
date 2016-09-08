package application;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
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
import javafx.scene.control.Tab;
import javafx.scene.effect.ColorAdjust;
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

/**
 * Controller for JavaFX components and GUI
 * @author Jacob Webber */
public class ShouldIDodgeController implements Initializable {
	@FXML 
	private BorderPane mainpane; 
	private Stage stage;
	private Scene scene;
	private LeagueData data;
	private ClipBoardImage clipboardimage = new ClipBoardImage();
	private java.awt.Image screenshot;
	private boolean dialogShowing = false;

	private static final RiotApi api = new RiotApi("KEY_HERE");



	@SuppressWarnings({ "rawtypes", "static-access", "unused", "unchecked" })
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		try { 
			//Initial API call for setting up League data variables
			try{
				api.setRegion(Region.NA);
				data = new LeagueData();
				System.out.println("data success");

			}catch(RiotApiException e){
				handleRiotException(e, null);
			}
			//SummonerInfo summoner1info = data.getSummonerData("Tunt Coaster", "Bard");
			BackgroundImage background = new BackgroundImage(new Image("/images/background.jpg",1280,800,false,true),
					BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
					BackgroundSize.DEFAULT);
			mainpane.setBackground(new Background(background));
			VBox rightpane = new VBox();
			rightpane.setMinWidth(600);
			VBox leftpane = new VBox();
			VBox bottompane = new VBox();
			VBox centerpane = new VBox();
			centerpane.setMinWidth(160);

			/* Creating new header toolbar */
			ToolBar toolBar = new ToolBar();
			int height = 25;
			toolBar.setPrefHeight(height);
			toolBar.setMinHeight(height);
			toolBar.setMaxHeight(height);
			HBox alignRight = new HBox();
			Pane pane = new Pane(alignRight);
			alignRight.setHgrow(pane, Priority.ALWAYS);
			Text title = new Text("League of Legends - Should I Dodge?");
			title.setStyle("-fx-font-family: arial;"
					+ "-fx-font-size: 22px;"
					+ "-fx-fill: linear-gradient(from 50% 50% to 100% 100%, repeat, goldenrod 0%, gold 50%, peru 100%);"
					+ "-fx-stroke-width: 1;");
			toolBar.getItems().addAll(title, pane, new WindowButtons());
			toolBar.setStyle("-fx-background-color: rgba(153, 255, 204, 0.2);");
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

			/* Creating left list */
			//Collections.sort(data.champNames);

			JFXListView<VBox> summonerlist = new JFXListView<VBox>();
			summonerlist.setMinWidth(300);
			summonerlist.setMinHeight(675);
			//summoner1
			VBox summoner1 = new VBox();
			JFXTextField summoner1field = new JFXTextField();   
			summoner1field.setPromptText("Summoner 1");
			JFXComboBox<String> champ1box = new JFXComboBox<String>();
			champ1box.getItems().addAll(data.champNames);
			champ1box.setPromptText("Pick a champion");

			new ComboBoxAutoComplete<String>(champ1box);
			summoner1.getChildren().addAll(summoner1field, champ1box);

			//summoner2
			VBox summoner2 = new VBox();
			JFXTextField summoner2field = new JFXTextField();   
			summoner2field.setPromptText("Summoner 2");   
			JFXComboBox<String> champ2box = new JFXComboBox<String>();
			champ2box.getItems().addAll(data.champNames);
			champ2box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ2box);
			summoner2.getChildren().addAll(summoner2field, champ2box);
			//summoner3
			VBox summoner3 = new VBox();
			JFXTextField summoner3field = new JFXTextField(); 
			summoner3field.setPromptText("Summoner 3");     
			JFXComboBox<String> champ3box = new JFXComboBox<String>();
			champ3box.getItems().addAll(data.champNames);
			champ3box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ3box);
			summoner3.getChildren().addAll(summoner3field, champ3box);
			//summoner4
			VBox summoner4 = new VBox();
			JFXTextField summoner4field = new JFXTextField();   
			summoner4field.setPromptText("Summoner 4"); 
			JFXComboBox<String> champ4box = new JFXComboBox<String>();
			champ4box.getItems().addAll(data.champNames);
			champ4box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ4box);
			summoner4.getChildren().addAll(summoner4field, champ4box);
			//summoner5
			VBox summoner5 = new VBox();
			JFXTextField summoner5field = new JFXTextField();   
			summoner5field.setPromptText("Summoner 5");     
			JFXComboBox<String> champ5box = new JFXComboBox<String>();
			champ5box.getItems().addAll(data.champNames);
			champ5box.setPromptText("Pick a champion");
			new ComboBoxAutoComplete<String>(champ5box);
			summoner5.getChildren().addAll(summoner5field, champ5box);

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
			JFXButton importbtn = new JFXButton("Import Clipboard");
			importbtn.setOnMouseClicked(event -> {
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
								System.out.println("champfield " + i + ":" + champFields.get(i).getValue());
								System.out.println("champion set: " + data.summonerChampNames.get(i));
								champFields.get(i).setValue((data.summonerChampNames.get(i)));
							}
						}
					}else{
						createDialog("NOPE", "There was no image found on the clipboard. \n"
								+ "Make sure that you take your screenshot after ShouldIDodge is opened.", 
								new Image("/images/teemo.png"));
					}

				} catch (IOException e) {
					createDialog("Error", "Error importing image. \n"
							+ "I blame you for this, what were you uploading?", 
							new Image("/images/teemo.png"));
				} catch (TesseractException e) {
					createDialog("Whoops", "Error with Tesseract OCR. \n"
							+ "That definitely shouldn't happen.", 
							new Image("/images/teemo.png"));
					e.printStackTrace();
				}
			});
			importbtn.setId("analyze-button");

			/* Button for updating summoner info and GUI */
			JFXButton fetchbtn = new JFXButton("Fetch Data");
			fetchbtn.setId("fetch-button");
			fetchbtn.setOnMouseClicked(event -> {
				rightpane.getChildren().clear(); //remove old images
				centerpane.getChildren().clear();
				// loop over summoners and champions for info. 

				for(int i = 0; i < 5; i++){
					System.out.println("..... " + i);
					String champ = "";
					if(champFields.get(i).getValue() != null){
						champ = champFields.get(i).getValue().toString();
					}
					String summoner = summonerFields.get(i).getText();	
					SummonerInfo info = new SummonerInfo();
					info = data.getSummonerData(summoner, champ);
					try{
						if(info.errorcode!= -1){ // A RiotApiException was thrown
							handleRiotException(new RiotApiException(info.errorcode), summoner);
						}
					}catch(NullPointerException e){

					}

					// Handling champion info and picture
					if(champ.equalsIgnoreCase("") || info == null){ //champ not found
						System.out.println("champ not found?");
						StackPane emptybox = new StackPane();
						emptybox.setMinHeight(136);
						emptybox.setMaxHeight(136);
						rightpane.getChildren().add(emptybox);
					}else{
						System.out.println("Champ: " + champFields.get(i).getValue().toString());
						StackPane imageStackPane = new StackPane();
						BorderPane imageBorderPane = new BorderPane();
						imageStackPane.setMinSize(600,  136);
						imageBorderPane.setMaxSize(600,  136);
						imageStackPane.setId("image-box");
						Image champsplash = data.getChampSplashArt(champ);
						ImageView champSplashView = new ImageView(champsplash);
						ColorAdjust darken = new ColorAdjust();
						//	darken.setBrightness(-0.3);
						//champSplashView.setEffect(darken);
						System.out.println("mstery level: " + info.champmasterylevel);
						String masteryURL = "images/mastery/level" + info.champmasterylevel + ".png";
						Image masteryImage = new Image(masteryURL);
						ImageView masteryView = new ImageView(masteryImage);
						StackPane.setAlignment(masteryView, Pos.CENTER_LEFT);
						Label masteryPoints = new Label(NumberFormat.getNumberInstance(Locale.US).format(info.champmasterypoints) + " Points");
						masteryPoints.setId("info-label");
						masteryPoints.setTextAlignment(TextAlignment.CENTER);
						VBox champMasteryBox = new VBox();
						champMasteryBox.setMinWidth(150);
						champMasteryBox.setId("mastery-box");
						champMasteryBox.setAlignment(Pos.CENTER);
						champMasteryBox.getChildren().addAll(masteryView, masteryPoints);
						imageBorderPane.setLeft(champMasteryBox);
						Label championGamesTotal = new Label(info.champgamestotal + " Games");
						championGamesTotal.setId("info-label");
						Label championGames = new Label("W " + info.champgameswon + "  /  " + info.champgameslost + " L");
						championGames.setId("info-label");
						championGames.setTextAlignment(TextAlignment.CENTER);
						double winlossratio = (double) info.champgameswon / (double) info.champgamestotal;
						ProgressBar winloss = new ProgressBar(winlossratio);
						Label winpercent = new Label(String.format("%.2f", winlossratio*100) + "%");
						winpercent.setId("info-label");

						VBox gameStatsBox = new VBox();
						gameStatsBox.setMinWidth(150);
						gameStatsBox.setId("game-stats-box");
						gameStatsBox.setAlignment(Pos.CENTER);
						gameStatsBox.getChildren().addAll(championGamesTotal, championGames, winloss, winpercent);
						imageBorderPane.setRight(gameStatsBox);
						imageStackPane.getChildren().addAll(champSplashView, imageBorderPane);
						rightpane.getChildren().add(imageStackPane);
					}
					// Handling general summoner info and pictures
					if(summoner.equalsIgnoreCase("") || summoner == null || info == null){ //summoner not found
						StackPane emptybox = new StackPane();
						emptybox.setMinHeight(142);
						centerpane.getChildren().add(emptybox);
					}else{
						System.out.println("Summoner: " + summonerFields.get(i).getText());
						StackPane summonerbox = new StackPane();
						summonerbox.setId("summoner-box");
						summonerbox.setMinHeight(142);

						String tierURL = "images/tier/";
						if(info.ranktier.equalsIgnoreCase("master") || info.ranktier.equalsIgnoreCase("challenger") || info.ranktier.equalsIgnoreCase("provisional")){
							tierURL = tierURL  + info.ranktier.toLowerCase() + ".png";
						}else{
							tierURL = tierURL + info.ranktier.toLowerCase() + "_" + info.rankdivision.toLowerCase() + ".png";
						}
						System.out.println(tierURL);
						Image image = new Image(tierURL);
						ImageView view = new ImageView(image);
						Label rankinfo = new Label(info.rankdivision.substring(0, 1).toUpperCase() + info.rankdivision.substring(1)
						+ " " + info.ranktier + " " + info.ranklp + " LP");
						//rankinfo.setTextFill(Color.WHITE);
						rankinfo.setId("rank-label");
						summonerbox.getChildren().addAll(view, rankinfo);
						StackPane.setAlignment(rankinfo,  Pos.BOTTOM_CENTER);
						centerpane.getChildren().add(summonerbox);
					}
				}
			});

			BorderPane.setMargin(leftpane, new Insets(25));
			HBox buttonbox = new HBox();
			buttonbox.setSpacing(5);
			Random rand = new Random();

			buttonbox.getChildren().addAll(importbtn, fetchbtn);
			leftpane.setAlignment(Pos.CENTER);
			leftpane.getChildren().addAll(summonerlist, buttonbox);
			mainpane.setLeft(leftpane);
			mainpane.setRight(rightpane);
			mainpane.setCenter(centerpane);

		} catch (Exception e) {
			System.out.println("Exception occurred in Initialize");
			e.printStackTrace();
		}
	}
	//end of initialize



	/** Pass the main stage and scene to the controller */
	public void setStage(Stage stage, Scene scene) {
		this.scene = scene;
		this.stage = stage;
	}

	/** Create a JFXDialog box in the current StackPane
	 * @param header text for dialog title
	 * @param message text for dialog body
	 * @param icon image for right side */
	public void createDialog(String header, String message, Image icon){
		if(dialogShowing){ // Don't display multiple dialogs
			return;
		}
		dialogShowing = true;
		JFXDialog dialog = new JFXDialog();
		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		Label headerLabel = new Label(header);
		headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
		HBox headerbox = new HBox();
		headerbox.setPadding(new Insets(1, 1, 1, 1));
		headerbox.getChildren().add(headerLabel);
		dialogLayout.setHeading(headerbox);

		BorderPane mainbody = new BorderPane();
		Label messageLabel = new Label(message);
		messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
		mainbody.setCenter(messageLabel);
		mainbody.setRight(new ImageView(icon));
		dialogLayout.setBody(mainbody);
		dialog.requestFocus();
		dialog.setContent(dialogLayout);
		dialog.setTransitionType(DialogTransition.CENTER);
		dialog.setStyle("-fx-background-color: rgba(0.0,0.0,0.0,.5);"
				+ "	-fx-padding: 0px 0px 0px 0px;");
		dialog.show((StackPane) mainpane.getParent());
		dialog.setOnDialogClosed(event ->{
			dialogShowing = false;
		});
	}



	/**
	 * Create and display JFXDialogs for different Riot API error codes.
	 * @param e error.
	 */
	public void handleRiotException(RiotApiException e, String name){

		System.out.println(e.getErrorCode());
		if(e.getErrorCode() == 601){
			createDialog("YOU need a map", "There's an I/O exception...\n"
					+ "Probably internet related.", 
					new Image("/images/ezreal.png"));
		}else if(e.getErrorCode() == 429){
			createDialog("Damnit Rito", "My Riot API rate is limited.\n"
					+ "Try again soon.", 
					new Image("/images/teemo.png"));
		}else if(e.getErrorCode() == 603){
			createDialog("Riot pls?", "API request timed out.\n"
					+ "Riot API may be having trouble.", 
					new Image("/images/teemo.png"));
		}else if(e.getErrorCode() == 404){
			createDialog("Poor Lost Souls", "The summoner " + name + " was not found.\n"
					+ "You may need to manually fix the summoner name.",
					new Image("/images/thresh.png"));
		}else if(e.getErrorCode() == 403){
			createDialog("Rito Pls", "Riot's API is probably down.\nTry again later.",
					new Image("/images/thresh.png"));
		}else if(e.getErrorCode() == 503){
			createDialog("Service Unavailable", "Riot couldn't even.\nTry again later.",
					new Image("/images/thresh.png"));
		}else{
			System.out.println("UH OH");
			createDialog("Good Luck", "I didn't handle this error.\n"
					+ "Error code is " + e.getErrorCode() + " " + e.getMessage(),
					new Image("/images/teemo.png"));
		}
	}

	/** Create toolbar window buttons */
	class WindowButtons extends HBox {
		public WindowButtons() {
			JFXComboBox<String>  region = new JFXComboBox<String>();
			region.setMaxHeight(20);
			region.getItems().addAll(
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
			region.valueProperty().setValue(api.getRegion().toString().toUpperCase());
			region.valueProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
					System.out.println(ov.getValue());
					api.setRegion(Region.valueOf(ov.getValue().toString()));
					System.out.println(api.getRegion().toString());
				}
			});
			api.setRegion(Region.NA);

			/* Create about button */
			JFXButton aboutBtn = new JFXButton("?");
			aboutBtn.setId("about-button");
			aboutBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					JFXDialog dialog = new JFXDialog();
					;
					Label description = new Label("Tired of the 4 man clown fiesta?\n\n"
							+ "Are you just praying that the instalock jungle teemo is secretly a 1-trick pony?\n\n"
							+ "ShouldIDodge will reveal all of these mysteries and more.\n\n"
							+ "Find out whether your team is a bunch of boosted animals just before loading in, with just a few clicks.");
					description.setPadding(new Insets(20));
					description.setStyle(" -fx-font: 20px arial;"
							+ "-fx-fill: white");
					Text instructions = new Text("\n1) Wait for all of your billyboosted teammates to select or hover their champion of choice.\n\n"
							+ "2) Use ctrl + printscreen to get a screenshot of your League of Legends lobby.\n\n"
							+ "3) Press the analyze button to pull the screenshot from your clipboard and analyze.\n\n"
							+ "4) Fix any errors in the name or champion parsing");
					instructions.setStyle(" -fx-font: 20px arial;"
							+ "-fx-fill: white");

					JFXTabPane tabPane = new JFXTabPane();
					VBox aboutbox = new VBox();
					aboutbox.setStyle("-fx-background-color: black");
					Image about = new Image("/images/about.png");
					aboutbox.getChildren().addAll(new ImageView(about));
					Tab abouttab = new Tab("About");
					abouttab.setContent(aboutbox);
					Tab instructionstab = new Tab("Instructions");
					VBox instructionsbox = new VBox();
					instructionsbox.setStyle("-fx-background-color: black");
					instructionsbox.getChildren().add(instructions);
					instructionstab.setContent(instructionsbox);
					tabPane.getTabs().addAll(abouttab, instructionstab);

					dialog.requestFocus();
					dialog.setContent(tabPane);
					dialog.setTransitionType(DialogTransition.CENTER);
					dialog.setStyle("-fx-background-color: rgba(0.0,0.0,0.0,.5);"
							+ "	-fx-padding: 0px 0px 0px 0px;");
					dialog.show((StackPane) mainpane.getParent());

				}
			});
			/* Create close button */
			JFXButton closeBtn = new JFXButton("X");
			closeBtn.setId("close-button");
			closeBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent actionEvent) {
					Platform.exit();
				}
			});
			//HBox alignRight = new HBox();
			Pane separator = new Pane();
			separator.setMinSize(15,  0);
			this.getChildren().addAll(region, aboutBtn, separator, closeBtn);
		}
	}

}

/** Records relative x and y coordinates. */
class Delta { double x, y; }  

