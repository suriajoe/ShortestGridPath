import javafx.scene.layout.AnchorPane;

public class Cell extends AnchorPane 
{

    int column; //X coordinate
    int row;    //Y coordinate
    double value;  //0=blocked,1=empty,2=hard/rough,0.25=empty(highway),0.5=rough(highway)
    int type;      //0=blocked,1=empty,2=hard/rough,3=empty(highway),4=rough(highway),5=start/goal vertex
    double f; 
    double g;
    double h;

    public Cell(int column, int row) 
    {

        this.column = column;
        this.row = row;
        
        getStyleClass().add("cell");

        setOpacity(0.9);
    }
    
    public Cell(int column, int row, double value, int type)
    {
     	this.column = column;
     	this.row = row;
     	this.value = value;
     	this.type = type;
     	
     	getStyleClass().add("cell");
     	
     	setOpacity(0.9);
    }
    
    public double getValue()
    {
    	return this.value;
    }
    
    public void setValue(double v)
    {
    	this.value = v;
    }
    
    public int getType()
    {
    	return this.type;
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
    
    public double getF()
    {
    	return this.f;
    }

    public void setF(double f)
    {
    	this.f = f;
    }
    public double getG()
    {
    	return this.g;
    }

    public void setG(double g)
    {
    	this.g = g;
    }
    public double getH()
    {
    	return this.h;
    }

    public void setH(double h)
    {
    	this.h = h;
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
    public void highwayColor()
    {
    	getStyleClass().add("cell-highway");
    }

    public String toString() 
    {
        return this.column + "/" + this.row;
    }
}