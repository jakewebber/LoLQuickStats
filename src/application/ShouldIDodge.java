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
public class ShouldIDodge extends Application {

	@Override
	public void start(Stage theStage) throws Exception {
		Stage stage = theStage;
		stage.setWidth(1280);
		stage.setHeight(800);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.getIcons().add(new Image("/images/shouldidodge.png"));
		theStage.setTitle("Should I Dodge?");

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShouldIDodge.fxml"));		
		Parent root = (Parent)loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(ShouldIDodge.class.getResource("/css/summonerlistview.css").toExternalForm());
		scene.getStylesheets().add(ShouldIDodge.class.getResource("/css/buttonstyles.css").toExternalForm());
		scene.getStylesheets().add(ShouldIDodge.class.getResource("/css/dialogstyles.css").toExternalForm());

		ShouldIDodgeController controller = (ShouldIDodgeController)loader.getController();
		controller.setStage(stage, scene); // or what you want to do
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}