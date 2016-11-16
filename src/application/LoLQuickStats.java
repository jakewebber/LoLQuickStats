package application;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;

/**
 * Sets up the JavaFX stage, fxml and css resources. 
 * @author Jacob Webber */
public class LoLQuickStats extends Application {

	private Pane splashLayout;
	private static final int SPLASH_WIDTH = 911;
	private static final int SPLASH_HEIGHT = 200;

	@Override public void start(Stage theStage) throws Exception {
		Stage stage = theStage;
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		stage.setX((screenBounds.getWidth() - SPLASH_WIDTH) / 2); 
		stage.setY((screenBounds.getHeight() - SPLASH_HEIGHT) / 2);  
		stage.initStyle(StageStyle.UNDECORATED);
		stage.getIcons().add(new Image(this.getClass().getResourceAsStream("/images/LoLQuickStats.png")));
		theStage.setTitle("LoL Quick Stats");
		showSplash(theStage);
		theStage.show();

		final Thread thread = new Thread(){
			public void run(){	
				Platform.runLater(() -> {
					FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuickStats.fxml"));		
					Parent root = null;
					try {
						root = (Parent)loader.load();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Scene scene = new Scene(root);

					/* Loading CSS Resources */
					scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/summonerlistview.css").toExternalForm());
					scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/buttonstyles.css").toExternalForm());
					scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/dialogstyles.css").toExternalForm());
					scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/combobox.css").toExternalForm());

					Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
					
					System.out.println("width: "+ screenBounds.getWidth() + " height: " + screenBounds.getHeight());
					QuickStatsController controller = (QuickStatsController)loader.getController();
					controller.setStage(stage, scene);
					stage.setWidth(controller.width);
					stage.setHeight(controller.height);
					stage.setScene(scene);
					stage.setResizable(true);

					stage.show();
					
					stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2); 
					stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);  
				});
			}
		};
		thread.start();
	}

	/** Splash Stage displays before main stage */
	private void showSplash(Stage initStage){
		ImageView splash = new ImageView(new Image("/images/bannerloading.png"));
		splashLayout = new VBox();
		splashLayout.getChildren().addAll(splash);
		splashLayout.setStyle("-fx-background-color: black; -fx-border-width:5; -fx-border-color: linear-gradient(to bottom, black, derive(gold, 50%));");
		splashLayout.setEffect(new DropShadow());
		final Thread thread = new Thread(){
			public void run(){	
				Platform.runLater(() -> {
					Scene splashScene = new Scene(splashLayout);
					Screen.getPrimary().getBounds();
					initStage.setScene(splashScene);
					initStage.show();
				});
			}
		};
		thread.start();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
