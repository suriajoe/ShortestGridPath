import java.util.LinkedList;

import javafx.scene.layout.Pane;

public class Grid extends Pane 
{
    int startX;
    int startY;
    int endX;
    int endY;

    int rows;
    int columns;

    double width;
    double height;

    Cell[][] cells;
    LinkedList<Cell> pathIsColored = new LinkedList<Cell>();

    public Grid(int columns, int rows, double width, double height) 
    {

        this.columns = columns;
        this.rows = rows;
        this.width = width;
        this.height = height;

        cells = new Cell[rows][columns];
    }

    /**
     * Add cell to array and to the UI.
     */
    public void add(Cell cell, int column, int row) 
    {

        cells[row][column] = cell;

        
        double w = width / columns;
        double h = height / rows;
        double x = w * column;
        double y = h * row;

        cell.setLayoutX(x);
        cell.setLayoutY(y);
        cell.setPrefWidth(w);
        cell.setPrefHeight(h);
		
        getChildren().add(cell);

    }

    public Cell getCell(int column, int row) 
    {
    	if(column > this.columns || column < 0 || row > this.rows || row < 0)
    		return null;
    	
        return cells[row][column];
    }
    public Cell roughCell(int column, int row)
    {
    	if(column > 159 || row > 119)
    		return null;
    	else if(column < 0 || row < 0)
    		return null;
    	else 
    		return cells[row][column];
    }
    public void setStartX(int startX){
    	this.startX = startX;
    }
    public int getStartX()
    {
    	return this.startX;
    }
    public void setStartY(int startY){
    	this.startY = startY;
    }
    public int getStartY()
    {
    	return this.startY;
    }
    public void setEndX(int endX){
    	this.endX = endX;
    }
    public int getEndX()
    {
    	return this.endX;
    }
    public void setEndY(int endY){
    	this.endY = endY;
    }
    public int getEndY()
    {
    	return this.endY;
    }

     //Unhighlight all cells
    public void hoverUnhighlight() 
    {
        while(!pathIsColored.isEmpty())
        {
        	pathIsColored.getFirst().cell = null;
        	pathIsColored.removeFirst().hoverUnhighlight();
        }
        pathIsColored.clear();
        
        for( int row=0; row < rows; row++) 
        {
            for( int col=0; col < columns; col++) 
            {
        		if(cells[row][col].getType() == 3 || cells[row][col].getType() == 4)
        		{
        			cells[row][col].highwayColor();
        		}
                cells[row][col].setG(2147483647);
                cells[row][col].setH(0);
                cells[row][col].setFZero();
                if(cells[row][col].cell != null)
                	cells[row][col].cell = null;
            }
        }
    }
    
}
