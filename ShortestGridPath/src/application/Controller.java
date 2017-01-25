import java.util.Random;

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
	
    int rows = 120;
    int columns = 160;
    
    double width = 1200;
    double height = 700;
	public void start(Stage mainStage)
	{
		try{
        // create grid
        Grid grid = new Grid(columns, rows, width, height);

        MouseGestures mouse = new MouseGestures();
 
        // fill grid with empty cells
        for (int row = 0; row < rows; row++) 
        {
            for (int column = 0; column < columns; column++) 
            {

                Cell cell = new Cell(column, row);

                mouse.color(cell);  //Used to color cell when cursor is hovering

                grid.add(cell, column, row);
            }
        }
        
        //If I addAll(grid) twice will i get conflicts
        //list.getChildren().addAll(grid);
        
        //list.getChildren().add(index, element);
        Random rand = new Random();
        
        /*
         * Add Hard to traverse cells
         * 8 separate 31x31 regions with 50% chance of hard terrain
         */
        boolean roughIsCentered = true;
        int roughCount = 0;
        int roughCoordinateX = 9;
        int roughCoordinateY = 9;
        int rough1;
        int rough2;
        int rough3;
        int rough4;
        
        int[][] rarray = new int[120][160];
        
        rough1 = roughCoordinateX;
        rough2 = roughCoordinateY;
    	for(int i=0;i<31;i++)
    	{
    	  for(int j=0;j<31;j++)
    	  {

    		    if(j<15 && i<15)
    		    {
    			  if(roughCoordinateX-i < 0 || roughCoordinateY-j < 0)
    		      {
    		      }  
    		      else
    		      {
    			    mouse.roughColor(grid.getCell(roughCoordinateX-i, roughCoordinateY-j));
    		      }
    		    }
    		    else
    		    {
    		    	if(roughCoordinateX-i < 0 && roughCoordinateY-j > 0)
    		    	{
    		    	  mouse.roughColor(grid.getCell(roughCoordinateX-i+1,roughCoordinateY-j));
    		    	}
    		    	else if(roughCoordinateX-i > 0 && roughCoordinateY-j > 0)
    		    	{
    		    		mouse.roughColor(grid.getCell(roughCoordinateX-i, roughCoordinateY-j+1));
    		    	}   		    	
    		    	else
    		    	{
    		    		mouse.roughColor(grid.getCell(roughCoordinateX+i, roughCoordinateY+j));
    		    	}
    		    	
    		    }
    		  }
    	}
    	
    	/*
    	for(int i=0;i<31;i++)
    	{
    		for(int j=0;j<31;j++)
    		{
    		    if(j<15 && i<15)
    		    {
    			  if(roughCoordinateX-i < 0 || roughCoordinateY-j < 0)
    		      {}  
    		      else
    			    mouse.roughColor(grid.getCell(roughCoordinateX-i, roughCoordinateY-j));
    		    }
    		    else if(j<15 && i)
    			
    		}
    	}
        */
    	
        /*
        while(roughCount < 8)
        {
        	/*
        	 *  Create coordinate Pair, if occupied 
        	 *  select new pair
        	 *
        	while(roughIsCentered)
        	{
        		roughCoordinateY = rand.nextInt(119);
        		roughCoordinateX = rand.nextInt(159);

        		if(rarray[roughCoordinateY][roughCoordinateX] != 1)
        		{
        			rarray[roughCoordinateY][roughCoordinateX] = 1;
        			roughIsCentered = false;		
        		}
        	}
        	
        	for(int i=0;i<31;i++)
        	{
        	  for(int j=0;j<31;j++)
        	  {
        		  if(roughCoordinateX-i < 0 || roughCoordinateY-j < 0)
        		  {}  
        		  else
        			  mouse.roughColor(grid.getCell(roughCoordinateX-i, roughCoordinateY-j));
        	  }
        	}
        	
        	roughCount++;
        	roughIsCentered = true;
        }
        */
        
        //USE GUI BUTTON FOR MOUSE LISTENER
        
        //Select random start and goal points   
        boolean distance100 = true;
        while(distance100)
        {
        	int startY = rand.nextInt(119);
        	int startX = rand.nextInt(159);
        	int goalY = rand.nextInt(119);
        	int goalX = rand.nextInt(159);
        	
        	//If distance is less than 100 select new points
        	if(Math.abs(startX-goalX) >= 100 && Math.abs(startY-goalY) >= 100)
        	{
        		//X = col, Y = row
                mouse.startPoint(grid.getCell(startX, startY));
                mouse.goalPoint(grid.getCell(goalX, goalY));
                //list.getChildren().removeAll(grid);
                list.getChildren().addAll(grid);
                distance100 = false;
        	}
        }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

    } 
}



