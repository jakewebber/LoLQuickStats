package application;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;

/**
 * Sets up the JavaFX stage, fxml and css resources. 
 * @author Jacob Webber */
public class LoLQuickStats extends Application {

	@Override
	public void start(Stage theStage) throws Exception {
		Stage stage = theStage;
		stage.setWidth(1280);
		stage.setHeight(800);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.getIcons().add(new Image("/images/LolQuickStats.png"));
		theStage.setTitle("LoL QuickStats");

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuickStats.fxml"));		
		Parent root = (Parent)loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/summonerlistview.css").toExternalForm());
		scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/buttonstyles.css").toExternalForm());
		scene.getStylesheets().add(LoLQuickStats.class.getResource("/css/dialogstyles.css").toExternalForm());

		QuickStatsController controller = (QuickStatsController)loader.getController();
		controller.setStage(stage, scene);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}