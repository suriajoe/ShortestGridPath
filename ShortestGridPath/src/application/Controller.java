import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.scene.input.PickResult;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Controller extends Pane
{

	@FXML private AnchorPane list;
	@FXML Button points, rough, block, highway; 
	
    int rows = 160;
    int columns = 120;
    double width = 1200;
    double height = 700;
	public void start(Stage mainStage)
	{
		try{
        // create grid
        Grid grid = new Grid(columns, rows, width, height);

        MouseGestures mouse = new MouseGestures();

        // fill grid
        for (int row = 0; row < rows; row++) 
        {
            for (int column = 0; column < columns; column++) 
            {

                Cell cell = new Cell(column, row);

                mouse.color(cell);

                grid.add(cell, column, row);
            }
        }
        
        //list = new AnchorPane();
        list.getChildren().addAll(grid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
        
        
        //listener for scrollbar and DPad position

    } 
}



