import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application 
{
	public void start(Stage primaryStage) 
			throws Exception {                
		FXMLLoader loader = new FXMLLoader();   
		loader.setLocation(
				getClass().getResource("AnchorGrid.fxml"));
		AnchorPane root = (AnchorPane)loader.load();

		Controller controller = 
				loader.getController();
		controller.start(primaryStage);

		Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Grid Path");
		primaryStage.show(); 
	}
    public static void main(String[] args) 
    {
        launch(args);
    }
}
