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
    Node parent = null;
    Cell cell = null;

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
     	this.g = 2147483647;
     	
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

    public void setF()
    {
    	this.f = getG()+getH();
    }
    public void setFZero()
    {
    	this.f = 0;
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
    public void add(Node node)
    {
    	this.parent = node;
    }
    public void removeNode(Node node)
    {
    	this.parent = null;
    }
    public Node peek()
    {
    	return this.parent;
    }
    public void highlight() 
    {
        // ensure the style is only once in the style list
        getStyleClass().remove("cell-highlight");
        getStyleClass().remove("cell-highway");

        // add style
        getStyleClass().add("cell-highlight");
    }

    public void unhighlight() 
    {
        getStyleClass().remove("cell-highlight");
    }
    
    public void hover()
    {
        getStyleClass().remove("cell-hover");

        getStyleClass().add("cell-hover");
    }
    public void unhover()
    {
        getStyleClass().remove("cell-hover");

    }
    /*Path */
    public void hoverHighlight() 
    {
        getStyleClass().remove("cell-hover-highlight");
        getStyleClass().remove("cell-highway");

        getStyleClass().add("cell-hover-highlight");
    }
    /*Path */
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
    	clean();

        getStyleClass().add("cell-start");
    }
    public void startClean() 
    {
        getStyleClass().remove("cell-start");
    }
    public void goal() 
    {
    	clean();
        getStyleClass().add("cell-goal");
    }
    public void goalClean() 
    {
        getStyleClass().remove("cell-goal");
    }
    public void brown()
    {
    	getStyleClass().add("cell-rough");
    }
    public void brownReadFile()
    {
    	getStyleClass().remove("cell-highlight");
    	getStyleClass().add("cell-rough");
    }
    public boolean isOccupied()
    {
    	return getStyleClass().isEmpty();
    }
    public void highwayColor()
    {
        getStyleClass().remove("cell-highway");

    	getStyleClass().add("cell-highway");
    }

    public String toString() 
    {
        return this.column + "/" + this.row;
    }
}