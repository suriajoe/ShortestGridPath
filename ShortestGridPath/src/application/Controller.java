import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
import javafx.scene.image.Image;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller extends Pane
{

	@FXML private AnchorPane list;
	@FXML Button saveGrid, loadGrid, createGrid; 
	@FXML Label xInfo,yInfo,gInfo,fInfo,hInfo,timeInfo;
	
	private IntegerProperty xCoor;
	private IntegerProperty yCoor;	
	private IntegerProperty time;
	public static final Duration INDEFINITE = new Duration(Double.POSITIVE_INFINITY);
	
    int rows = 120;
    int columns = 160;
    int value = 1;
    int type = 1;
    
    double width = 1200;
    double height = 670;
    //Maybe make One Global Grid, or 6 Grid(1 for random, 5 for load from file)
    //use list.getChildren.remove(grid[i]); grid=null  to clear it
    Grid grid;
    int sourceX;
    int sourceY;
    int destX;
    int destY;
    int roughArrayX[] = new int[8];
    int roughArrayY[] = new int[8];
    
	public void start(Stage mainStage)
	{
		createGrid.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				if(!list.getChildren().isEmpty())
					flush();
				CreateGrid();
			}
		});
		
		saveGrid.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				if(!list.getChildren().isEmpty())
					writeFile();
			}
		});
		
		loadGrid.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				readFile(mainStage);
			}
		});
		
    }
	
	//Load Method to load file, and set up Gui 
	
	public void CreateGrid()
	{
		try{
	        // create grid
	        grid = new Grid(columns, rows, width, height);

	        MouseGestures mouse = new MouseGestures();
	        
	        xCoor = new SimpleIntegerProperty(0);
	        yCoor = new SimpleIntegerProperty(0);
	        time = new SimpleIntegerProperty(0);
	        xInfo.textProperty().bind(xCoor.asString("x:%s"));
	        yInfo.textProperty().bind(yCoor.asString("y:%s"));
	        timeInfo.textProperty().bind(time.asString("time:%s ms"));
	        
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
	        int rough1;					//will be set to random number(0 or 1) to create 50% rough terrain
	        
	        int[][] rarray = new int[120][160]; //rows = 120, cols = 160
	        int[][] blockedArray = new int[120][160];//placeholder used for highway
	        for(int i=0;i<120;i++)
	        {
	        	for(int j=0;j<160;j++)
	        	{
	        		blockedArray[i][j]=1;
	        	}
	        }
	        
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
	        			roughArrayX[roughCount] = roughCoordinateX;
	        			roughArrayY[roughCount] = roughCoordinateY;
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
	    			    	blockedArray[roughCoordinateY-j][roughCoordinateX-i] = 2;
	    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY-j).setValue(2);
	    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY-j).setType(2);
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
	    			    	blockedArray[roughCoordinateY+(j-15)][roughCoordinateX-i] = 2;
	    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY+(j-15)).setValue(2);
	    			    	grid.getCell(roughCoordinateX-i, roughCoordinateY+(j-15)).setType(2);
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
	    			    	blockedArray[roughCoordinateY-j][roughCoordinateX+(i-15)] = 2;
	    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY-j).setValue(2);
	    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY-j).setType(2);
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
	    			    	blockedArray[roughCoordinateY+(j-15)][roughCoordinateX+(i-15)] = 2;
	    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY+(j-15)).setValue(2);
	    			    	grid.getCell(roughCoordinateX+(i-15), roughCoordinateY+(j-15)).setType(2);
	    			    }
	    			  }
	    		    }
	    		   }
	    	     }
	        	
	        	roughCount++;
	        	roughIsCentered = true;
	        }
	        
	        //Highway path
	        HighwayConstructor highway = new HighwayConstructor(blockedArray);
	        blockedArray = highway.construct(blockedArray);    
	        for(int i=0;i<120;i++)
	        {
	        	for(int j=0;j<160;j++)
	        	{
	        		if(blockedArray[i][j] == 3)
	        		{
	        			grid.getCell(j,i).setType(3);
	        			grid.getCell(j,i).setValue(.25);
	        			grid.getCell(j,i).highwayColor();
	        		}
	        		else if(blockedArray[i][j] == 4)
	        		{
	        			grid.getCell(j,i).clean();
	        			grid.getCell(j,i).setType(4);
	        			grid.getCell(j,i).setValue(.50);
	        			grid.getCell(j,i).highwayColor();
	        		}
	        	}
	        }
	        
	        //Blocked Cells
	        int xRand;
	        int yRand;
	        int blockedCount = 0;
	        while(blockedCount < 3840)
	        {
	        	xRand = rand.nextInt(160);
	        	yRand = rand.nextInt(120);
	        	if(grid.getCell(xRand, yRand).getType() != 2 || grid.getCell(xRand, yRand).getType() != 0 
	        			|| grid.getCell(xRand, yRand).getType() != 3 || grid.getCell(xRand, yRand).getType() != 4)
	        	{
			    	grid.getCell(xRand, yRand).setValue(0);
			    	grid.getCell(xRand, yRand).setType(0);
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
	        		if(grid.getCell(startX, startY).getType() !=0 || grid.getCell(startX, startY).getType() !=5 
	        				|| grid.getCell(startX, startY).getType() !=3 || grid.getCell(startX, startY).getType() !=4)
	        		{
	        			if(grid.getCell(goalX, goalY).getType() != 0 || grid.getCell(goalX, goalY).getType() !=5
	        					|| grid.getCell(goalX, goalY).getType() !=3 || grid.getCell(goalX, goalY).getType() !=4)
	        			{
	    			    	grid.getCell(startX, startY).setType(5);
	    			    	grid.getCell(startX, startY).setValue(1);
	    			    	grid.getCell(goalX, goalY).setType(5);
	    			    	grid.getCell(startX, startY).setValue(1);
	            			mouse.startPoint(grid.getCell(startX, startY));
	                        mouse.goalPoint(grid.getCell(goalX, goalY));
	                        sourceX = startX;
	                        sourceY = startY;
	                        destX = goalX;
	                        destY = goalY;
	                        distance100 = false;
	                        list.getChildren().addAll(grid);
	        			}      			
	        		}
	        	}
	        }
	        writeFile();
		  }
		  catch(Exception e)
		  {
				e.printStackTrace();
		  }
	}
	
	//delete grid=null, use array of grid[] objects
	public void flush()
	{
		list.getChildren().remove(grid);
		grid = null;
	}
	
	public void writeFile()
	{
		String source = ""+sourceX + "," + sourceY;
		String dest = ""+destX + "," + destY;
		LinkedList<String> line = new LinkedList<String>();
		line.add(source); 
		line.add(dest);
		for(int i=0;i<8;i++)
		{
			String roughCoor = ""+roughArrayX[i]+","+roughArrayY[i];
			line.add(roughCoor);
		}
		
		for(int i=0;i<120;i++)
		{
			String rowStr="";
			
			for(int j=0;j<160;j++)
			{
				switch(grid.getCell(j, i).getType())
				{
				case(0):
				{
					rowStr = rowStr + 0 + " ";
					break;
				}
				case(1):
				{
					rowStr = rowStr + 1 + " ";
					break;
				}
				case(2):
				{
					rowStr = rowStr + 2 + " ";
					break;
				}
				case(3):
				{
					rowStr = rowStr + 'a' + " ";
					break;
				}
				case(4):
				{
					rowStr = rowStr + 'b' + " ";
					break;
				}
				case(5):
				{
					rowStr = rowStr + 's' + " ";
					break;
				}
				
				}			
			}
			line.add(rowStr);
		}
		
		Path file = Paths.get("grid.txt");
		try {
			Files.write(file, line, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Load .txt and create grid from those coordinates
	public void readFile(Stage mainStage)
	{
	  try{
    	FileChooser fileChooser = new FileChooser();
    	FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Txt files (*.txt)", "*.txt");
    	fileChooser.getExtensionFilters().add(extFilter);
    	File file = fileChooser.showOpenDialog(mainStage);
    	Path path = file.toPath();
    	List<String> line;
    	line = Files.readAllLines(path, Charset.forName("UTF-8"));
		
    	if(!line.isEmpty())
    	{
    	  //if successful and valid file, flush and create file
    	  if(!list.getChildren().isEmpty())
			flush();
	      
    	  // create grid
    	  grid = new Grid(columns, rows, width, height);

    	  MouseGestures mouse = new MouseGestures();
	        
    	  xCoor = new SimpleIntegerProperty(0);
    	  yCoor = new SimpleIntegerProperty(0);
    	  time = new SimpleIntegerProperty(0);
    	  xInfo.textProperty().bind(xCoor.asString("x:%s"));
    	  yInfo.textProperty().bind(yCoor.asString("y:%s"));
    	  timeInfo.textProperty().bind(time.asString("time:%s ms"));
	        
	      // fill grid with empty cells
	      for(int row = 0; row < rows; row++) 
	      {
	          for(int column = 0; column < columns; column++) 
	          {

	             Cell cell = new Cell(column, row, value, type);

	             mouse.color(cell,xCoor,yCoor);  

	             grid.add(cell, column, row);
	          }
	      }
	      
	      int index=0;
	      int sx=0;
	      int sy=0;
	      int destX;
	      int destY;
	      int hardArrayX[] = new int[8];
	      int hardArrayY[] = new int[8];
	      boolean skipWhiteLine = true;
	      String sourceStr;
	      String destStr;
	      String hardStr;
	      String temp = "";
	      int tempX = 0;
	      sourceStr = line.get(index);
	      index++;
	      destStr = line.get(index);
	      
	      for(int x=0;x<sourceStr.length();x++)
	      {
	    	  if(Character.isDigit(sourceStr.charAt(x)))
	    		  temp = temp + (sourceStr.charAt(x)-'0');
	    	  else
	    	  {
	    		  tempX = x;
	    		  sx = Integer.parseInt(temp);
	    		  x=sourceStr.length();
	    	  }
	      }
	      for(int y=tempX+1;y<sourceStr.length();y++)
	      {
	    	  if(Character.isDigit(sourceStr.charAt(y)))
	    		  temp = temp + (sourceStr.charAt(y)-'0');
	    	  else
	    	  {
	    		  sy = Integer.parseInt(temp);
	    		  y=sourceStr.length();
	    	  }
	      }
	      
	      /*
	      if(Character.isDigit(sourceStr.charAt(0)))
	    	 sourceX = sourceStr.charAt(0)-'0';	    	  
	      if(Character.isDigit(sourceStr.charAt(1)))
	    	 sourceY = sourceStr.charAt(1)-'0';
	      else if(sourceStr.length() <= 2)
	    	  if(Character.isDigit(sourceStr.charAt(2)))
	    		  sourceY = sourceStr.charAt(2)-'0';
	      
	      if(Character.isDigit(destStr.charAt(0)))
	    	 destX = destStr.charAt(0)-'0';	    	  
	      if(Character.isDigit(destStr.charAt(1)))
	    	 destY = destStr.charAt(1)-'0';
	      else if(destStr.length() <= 2)
	    	  if(Character.isDigit(destStr.charAt(2)))
	    		  destY = destStr.charAt(2)-'0';
	      */
	      
	      for(int i=0;i<8;i++)
	      {
	    	  hardStr=line.get(index);
	    	  index++;
		      if(Character.isDigit(hardStr.charAt(0)))
			     hardArrayX[i] = hardStr.charAt(0)-'0';	    	  
			  if(Character.isDigit(hardStr.charAt(1)))
				 hardArrayY[i] = hardStr.charAt(1)-'0';	    	  
			  else if(hardStr.length() <= 2)
			     if(Character.isDigit(hardStr.charAt(2)))
				     hardArrayY[i] = hardStr.charAt(0)-'0';	    	  
	      }
	      
	      grid.getCell(sx, sy).setValue(1);
	      grid.getCell(sx, sy).setType(5);
	      mouse.startPoint(grid.getCell(sx, sy));
	      //mouse.goalPoint(grid.getCell(sx, sy));
	      
          list.getChildren().addAll(grid);      
    	}
	   	        
	  }
	  catch(IOException i)
	  {
		i.printStackTrace();
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



