import javafx.scene.layout.AnchorPane;

public class Cell extends AnchorPane 
{

    int column; //X coordinate
    int row;    //Y coordinate
    //int value;

    public Cell(int column, int row) 
    {

        this.column = column;
        this.row = row;
        
        getStyleClass().add("cell");

//      Label label = new Label(this.toString());
//
//      getChildren().add(label);

        setOpacity(0.9);
    }
    
    /*public Cell(int column, int row, value)
     {
     	this.column = column;
     	this.row = row;
     	this.value = value;
     	
     	getStyleClass().add("cell");
     	
     	setOpacity(0.9);
     */

    public void highlight() 
    {
        // ensure the style is only once in the style list
        getStyleClass().remove("cell-highlight");

        // add style
        getStyleClass().add("cell-highlight");
    }

    public void unhighlight() 
    {
        getStyleClass().remove("cell-highlight");
    }

    public void hoverHighlight() 
    {
        // ensure the style is only once in the style list
        getStyleClass().remove("cell-hover-highlight");

        // add style
        getStyleClass().add("cell-hover-highlight");
    }

    public void hoverUnhighlight() 
    {
        getStyleClass().remove("cell-hover-highlight");
    }
    
    public void clean()
    {
    	getStyleClass().remove("cell-rough");
    }
    public void start() 
    {
        getStyleClass().add("cell-start");
    }
    public void goal() 
    {
        getStyleClass().add("cell-goal");
    }
    public void brown()
    {
    	getStyleClass().add("cell-rough");
    }
    public boolean isOccupied()
    {
    	return getStyleClass().isEmpty();
    }

    public String toString() 
    {
        return this.column + "/" + this.row;
    }
}