import javafx.scene.layout.AnchorPane;

public class Cell extends AnchorPane 
{

    int column; //X coordinate
    int row;    //Y coordinate
    int value;  //0=blocked
    int type;   //0=empty,1=hard/rough,2=block,3=start/goal vertex,4=highway

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
    
    public Cell(int column, int row, int value, int type)
    {
     	this.column = column;
     	this.row = row;
     	this.value = value;
     	this.type = type;
     	
     	getStyleClass().add("cell");
     	
     	setOpacity(0.9);
    }
    
    public int getValue()
    {
    	return this.value;
    }
    
    public void setValue(int v)
    {
    	this.value = v;
    }
    
    public int getType()
    {
    	return this.value;
    }
    
    public void setType(int t)
    {
    	this.type = t;
    }
    
    public int getColumn()
    {
    	return this.column;
    }
    
    public void setColumn(int col)
    {
    	this.column = col;
    }
    
    public int getRow()
    {
    	return this.row;
    }

    public void setRow(int r)
    {
    	this.row = r;
    }
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
        getStyleClass().remove("cell-hover-highlight");

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