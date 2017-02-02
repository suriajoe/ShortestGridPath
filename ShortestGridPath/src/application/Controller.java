import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller extends Pane
{

	@FXML private AnchorPane list;
	@FXML Button points, rough, block, highway; 
	@FXML Label xInfo,yInfo,timeInfo;
	
	private IntegerProperty xCoor;
	private IntegerProperty yCoor;	
	private IntegerProperty time;
	public static final Duration INDEFINITE = new Duration(Double.POSITIVE_INFINITY);
	
    int rows = 120;
    int columns = 160;
    int value = 1;
    int type = 0;
    
    double width = 1200;
    double height = 670;
    
	public void start(Stage mainStage)
	{
		try{
        // create grid
        Grid grid = new Grid(columns, rows, width, height);

        MouseGestures mouse = new MouseGestures();
        
        xCoor = new SimpleIntegerProperty(0);
        yCoor = new SimpleIntegerProperty(0);
        time = new SimpleIntegerProperty(0);
        xInfo.textProperty().bind(xCoor.asString());
        yInfo.textProperty().bind(yCoor.asString());
        timeInfo.textProperty().bind(time.asString("%s ms"));
        
        // fill grid with empty cells
        for (int row = 0; row < rows; row++) 
        {
            for (int column = 0; column < columns; column++) 
            {

                Cell cell = new Cell(column, row, value, type);

                mouse.color(cell,xCoor,yCoor);  

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
        int roughCoordinateX = 15;
        int roughCoordinateY = 15;
        int rough1;
        
        int[][] rarray = new int[120][160];
        int[][] blockedArray = new int[120][160];  //delete since Cell object contains value,type
        
        while(roughCount < 8)
        {
        	/*
        	 *  Create coordinate Pair, if occupied 
        	 *  select new pair
        	 */
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
        	
            //32 because the center coordinate is colored twice
    	    for(int i=0;i<32;i++)
    	    {
    		  for(int j=0;j<32;j++)
    		  { 		   	
    		    rough1 = rand.nextInt(2);  // random generator
    		    if(i<15 && j<15)
    		    { 
    			  if(rough1 == 0)
    			  {
    			    mouse.roughColor(grid.roughCell(roughCoordinateX-i,roughCoordinateY-j));
    			    if((roughCoordinateX-i) >= 0 && (roughCoordinateY-j) >= 0 && (roughCoordinateX-i) < 160 && (roughCoordinateY-j) < 160)
    			    {
    			    	blockedArray[roughCoordinateY-j][roughCoordinateX-i] = 1;
    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY-j).setValue(2);
    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY-j).setType(1);
    			    }
    			  }
    		    }
    		    else if(i<15 && j>=15)
    		    {
    			  if(rough1 == 0)
    			  {
    			    mouse.roughColor(grid.roughCell(roughCoordinateX-i,roughCoordinateY+(j-15)));
    			    if((roughCoordinateX-i)>=0 && (roughCoordinateY+(j-15)) >=0 && ((roughCoordinateX-i) < 160) && (roughCoordinateY+(j-15) < 120))
    			    {
    			    	blockedArray[roughCoordinateY+(j-15)][roughCoordinateX-i] = 1;
    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY+(j-15)).setValue(2);
    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY+(j-15)).setType(1);
    			    }
    			  }
    		    }		  
    		    else if(i>=15 && j<15)
    		    {
    			  if(rough1 == 0)
    			  {
    			    mouse.roughColor(grid.roughCell(roughCoordinateX+(i-15),roughCoordinateY-j));
    			    if((roughCoordinateX+(i-15))>=0 && (roughCoordinateY-j) >=0 && ((roughCoordinateX+(i-15)) < 160) && (roughCoordinateY-j) < 120)
    			    {
    			    	blockedArray[roughCoordinateY-j][roughCoordinateX+(i-15)] = 1;
    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY-j).setValue(2);
    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY-j).setType(1);
    			    }
    			  }
    		    }  
    		    else
    		    {
    			  if(rough1 == 0)
    			  {
    			    mouse.roughColor(grid.roughCell(roughCoordinateX+(i-15),roughCoordinateY+(j-15)));	 
    			    if((roughCoordinateX+(i-15))>=0 && (roughCoordinateY+(j-15)) >=0 && ((roughCoordinateX+(i-15)) < 160) && (roughCoordinateY+(j-15) < 120))
    			    {
    			    	blockedArray[roughCoordinateY+(j-15)][roughCoordinateX+(i-15)] = 1;
    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY+(j-15)).setValue(2);
    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY+(j-15)).setType(1);
    			    }
    			  }
    		    }
    		   }
    	     }
        	
        	roughCount++;
        	roughIsCentered = true;
        }
        
        //Highway path
        //HighwayConstructor highway = new HighwayConstructor(blockedArray);
        //highway.construct(blockedArray);
        
        //Blocked Cells
        int xRand;
        int yRand;
        int blockedCount = 0;
        while(blockedCount < 3840)
        {
        	xRand = rand.nextInt(160);
        	yRand = rand.nextInt(120);
        	if(grid.getCell(xRand, yRand).getType() != 1 || grid.getCell(xRand, yRand).getType() != 2)
        	{
		    	grid.getCell(xRand, yRand).setValue(0);
		    	grid.getCell(xRand, yRand).setType(2);
        		grid.getCell(xRand, yRand).highlight();
        		blockedCount++;
        	}
        }
        
        //USE GUI BUTTON FOR MOUSE LISTENER
        
        //Select random start and goal points   
        boolean distance100 = true;
        while(distance100)
        {
        	int startX;
        	int startY;
        	int goalX;
        	int goalY;
        	int maxX = 159;
        	int minX = 139;
        	int maxY = 119;
        	int minY = 99;
        	
        	int topTwenty = rand.nextInt(2);
        	int leftTwenty = rand.nextInt(2);
        	int goalTop = rand.nextInt(2);
        	int goalLeft = rand.nextInt(2);
        	
        	if(topTwenty == 1)
        		startX = rand.nextInt(20);
        	else
        		startX = rand.nextInt((maxX-minX)+1) + minX;
        	
        	if(leftTwenty == 1)
        		startY = rand.nextInt(20);
        	else
        		startY = rand.nextInt((maxY-minY)+1) + minY;
        	
        	if(goalTop == 1)
        		goalX = rand.nextInt(20);
        	else
        		goalX = rand.nextInt((maxX-minX)+1) + minX;
        	
        	if(goalLeft == 1)
        		goalY = rand.nextInt(20);
        	else
        		goalY = rand.nextInt((maxY-minY)+1) + minY;
        	
        	
        	double euclidean;
        	int xDis = (startX+1)-(goalX+1);
        	int yDis = (startY+1)-(goalY+1);
        	euclidean = Math.sqrt(Math.pow(xDis, 2)+Math.pow(yDis, 2));
        	
        	//If distance is less than 100 select new points
        	if(euclidean >= 100)
        	{
        		//X = col, Y = row
        		if(grid.getCell(startX, startY).getType() !=2 || grid.getCell(startX, startY).getType() !=3)
        		{
        			if(grid.getCell(goalX, goalY).getType() != 2 || grid.getCell(goalX, goalY).getType() !=3)
        			{
    			    	grid.getCell(startX, startY).setType(3);
    			    	grid.getCell(goalX, goalY).setType(3);
            			mouse.startPoint(grid.getCell(startX, startY));
                        mouse.goalPoint(grid.getCell(goalX, goalY));
                        distance100 = false;
                        list.getChildren().addAll(grid);
        			}      			
        		}
        	}
        }
	  }
	  catch(Exception e)
	  {
			e.printStackTrace();
	  }
    }
	
	/*Create a thread that sets a timer when path starts, stop timer when goal reached
	long tStart = System.currentTimeMillis();
	public void time() throws InterruptedException
	{
	   Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1), ae->doSomething()));
	
	   timeline.setCycleCount(Animation.INDEFINITE);
	   timeline.play();
	}
	
	int count;
	public void doSomething()
	{
		time.setValue(System.currentTimeMillis()-tStart);
	}
	*/
}



