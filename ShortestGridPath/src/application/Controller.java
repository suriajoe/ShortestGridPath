import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller extends Pane
{

	@FXML private AnchorPane list;
	@FXML Button saveGrid, loadGrid, createGrid, startPath, uniform; 
	@FXML Label xInfo,yInfo,gInfo,fInfo,hInfo,timeInfo;
	@FXML TextField aWeight;
	@FXML ChoiceBox<String> fiveMaps,tenPoints,heuristic;
	//Name="tenpoints" IsEditable="True" IsReadOnly="True" Text="10 Points"
	
	private IntegerProperty xCoor;
	private IntegerProperty yCoor;	
	private IntegerProperty time;
	private DoubleProperty gInt;
	private DoubleProperty fInt;	
	private DoubleProperty hInt;
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
    Grid fiveGrids[];
    int sourceX;
    int sourceY;
    int destX;
    int destY;
    int roughArrayX[] = new int[8];
    int roughArrayY[] = new int[8];
    int timeCounter = 0;
    
	public void start(Stage mainStage)
	{
		createGrid.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event){
				if(!list.getChildren().isEmpty())
					flush();
				CreateGrid(10);
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
		
		startPath.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				//if(grid != null)
				//	cleanHighway();
				path();
			}
		});
		
		uniform.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				//if(grid != null)
				//	cleanHighway();
				uniformCost();
			}
		});
		
		aWeight.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				String str = aWeight.getText();
				double d = Double.parseDouble(str);
				//if(grid != null)
					//cleanHighway();
				pathWeighted(d);
			}
		});
		
	    fiveMaps.getItems().addAll("Map 1","Map 2","Map 3","Map 4","Map 5");
		fiveMaps.getSelectionModel().selectedIndexProperty().addListener(new
									 ChangeListener<Number>()
		{
				public void changed(ObservableValue ov, Number value, Number new_value) 
				{
					//CreateGrid(new_value.intValue());
				}
		});
	    
	    tenPoints.getItems().addAll("Point 1","Point 2","Point 3","Point 4","Point 5"
	    		,"Point 6","Point 7","Point 8","Point 9","Point 10");

		tenPoints.getSelectionModel().selectedIndexProperty().addListener(new
				 ChangeListener<Number>()
		{
			public void changed(ObservableValue ov, Number value, Number new_value) 
			{
				CreateGrid(10);
				tenPoints(new_value.intValue());
			}
		});
		
	    heuristic.getItems().addAll("Manhatten","Blocked Cells","Agent Distance","Opposite");

		heuristic.getSelectionModel().selectedIndexProperty().addListener(new
				 ChangeListener<Number>()
		{
			public void changed(ObservableValue ov, Number value, Number new_value) 
			{
				if(new_value.intValue() == 0)
					manhatten();
				else if(new_value.intValue() == 2)
					agentDistance();
				else if(new_value.intValue() == 3)
					opposite();
				
				//CreateGrid(10);
				//tenPoints(new_value.intValue());
			}
		});
		
    }
	
	//Load Method to load file, and set up Gui 
	
	public void CreateGrid(int gridNum)
	{
		try{
			if(gridNum == 10)
				grid = new Grid(columns, rows, width, height);
			else
				fiveGrids[gridNum] = new Grid(columns,rows,width,height);

	        MouseGestures mouse = new MouseGestures();
	        
	        xCoor = new SimpleIntegerProperty(0);
	        yCoor = new SimpleIntegerProperty(0);
	        time = new SimpleIntegerProperty(0);
	        fInt = new SimpleDoubleProperty(0);
	        gInt = new SimpleDoubleProperty(0);
	        hInt = new SimpleDoubleProperty(0);
	        fInfo.textProperty().bind(fInt.asString("f:%s"));
	        gInfo.textProperty().bind(gInt.asString("g:%s"));
	        hInfo.textProperty().bind(hInt.asString("h:%s"));
	        xInfo.textProperty().bind(xCoor.asString("x:%s"));
	        yInfo.textProperty().bind(yCoor.asString("y:%s"));
	        timeInfo.textProperty().bind(time.asString("time:%s ms"));
	        
	        // fill grid with empty cells
	        for (int row = 0; row < rows; row++) 
	        {
	            for (int column = 0; column < columns; column++) 
	            {

	                Cell cell = new Cell(column, row, value, type);

	                mouse.color(cell,xCoor,yCoor,fInt,gInt,hInt);  

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
	        			grid.getCell(j,i).clean();
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
		int count = 0;
		//while(count<5)
			//list.getChildren().remove(fiveGrids[5]);
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
	        fInt = new SimpleDoubleProperty(0);
	        gInt = new SimpleDoubleProperty(0);
	        hInt = new SimpleDoubleProperty(0);
	        fInfo.textProperty().bind(fInt.asString("f:%s"));
	        gInfo.textProperty().bind(gInt.asString("g:%s"));
	        hInfo.textProperty().bind(hInt.asString("h:%s"));
	        xInfo.textProperty().bind(xCoor.asString("x:%s"));
	        yInfo.textProperty().bind(yCoor.asString("y:%s"));
	        timeInfo.textProperty().bind(time.asString("time:%s ms"));
	        
	      // fill grid with empty cells
	      for(int row = 0; row < rows; row++) 
	      {
	          for(int column = 0; column < columns; column++) 
	          {

	             Cell cell = new Cell(column, row, value, type);

	                mouse.color(cell,xCoor,yCoor,fInt,gInt,hInt);  

	             grid.add(cell, column, row);
	          }
	      }
	      
	      int index=0;
	      int sx=0;
	      int sy=0;
	      int dx=0;
	      int dy=0;
	      int destX;
	      int destY;
	      int hardArrayX[] = new int[8];
	      int hardArrayY[] = new int[8];
	      boolean skipWhiteLine = true;
	      String sourceStr;
	      String destStr;
	      String hardStr;
	      String temp = "";
	      String tempT = "";
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
	    		  sx = Integer.parseInt(temp.trim());
	    		  x=sourceStr.length();
	    	  }
	      }
	      int comma = sourceStr.indexOf(",");
	      tempT = sourceStr.substring(comma+1);
		  sy = Integer.parseInt(tempT.trim());
		  
	      for(int x=0;x<destStr.length();x++)
	      {
	    	  if(Character.isDigit(destStr.charAt(x)))
	    		  temp = temp + (destStr.charAt(x)-'0');
	    	  else
	    	  {
	    		  tempX = x;
	    		  dx = Integer.parseInt(temp.trim());
	    		  x=sourceStr.length();
	    	  }
	      }
	      comma = sourceStr.indexOf(",");
	      temp = sourceStr.substring(comma+1);
		  dy = Integer.parseInt(temp.trim());

		  
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
	      grid.getCell(dx, dy).setValue(1);	
	      grid.getCell(dx, dy).setType(5);
	      mouse.startPoint(grid.getCell(sx, sy));
	      mouse.goalPoint(grid.getCell(dx, dy));
	      
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
	
	public void path()
	{      
		Comparator<Cell> comparator = new CellCompare();
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(200,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
        int heuristicCost = 0;//HeuristicCost
        int finalCost = 0; //g+h
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance;			
        //heuristic
        grid.getCell(sourceX, sourceY).setG(0);
        parentList.add(grid.getCell(sourceX, sourceY));
        path.add(grid.getCell(sourceX, sourceY));
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
        while(!path.isEmpty())
        {
        	s=path.poll();
        	//parentList.poll();
        	//s.hoverUnhighlight();
        	if(s == goal)
        	{
            	s.hoverUnhighlight();
        		System.out.println("path found");
        		pathFound = true;
        		break;
        	}
        	closed[s.getRow()][s.getColumn()] = true;
            currentX = s.getColumn();
            currentY = s.getRow();
            
            //pos 4
            if(currentX-1>=0){
            	sPrime = grid.getCell(currentX-1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList);          		
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }  
            }
        }
        if(s != goal)
        {
        	System.out.println("No Path Found");
        }
        
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	while(goal.cell != null)
        	{	
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;		
        	}
        }
        System.out.println("AStar Euclidean Heuristic TotalCost: " + totalCost);
        System.out.println("AStar Euclidean Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
	}

	public void updateVertex(Cell s, Cell sPrime, double cost, double heuristicCost,boolean[][] closed
							,PriorityQueue<Cell> fringe,LinkedList<Cell> parent)
	{
		//closed[120][160]
		//cost = s.getF+cellCost
		
		if(sPrime == null || sPrime.getType() == 0 || closed[sPrime.getRow()][sPrime.getColumn()]== true)
			return;
		double fCost = heuristicCost+cost;

		
		if(!fringe.contains(sPrime) || sPrime.getF()>fCost)
		{
			if(fringe.contains(sPrime))
			{
				fringe.remove(sPrime);
			}
			sPrime.setH(heuristicCost);
			sPrime.setG(cost);
			sPrime.setF();
			fringe.add(sPrime);
			timeCounter++;
			sPrime.cell=s;			
			//sPrime.hoverHighlight();
			closed[sPrime.getRow()][sPrime.getColumn()] = true;
		}
		
	}
	
	public void uniformCost()
	{      
		Comparator<Cell> comparator = new CellCompare();
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(200,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
        int heuristicCost = 0;//HeuristicCost
        int finalCost = 0; //g+h
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance;			
        //heuristic
        grid.getCell(sourceX, sourceY).setG(0);
        parentList.add(grid.getCell(sourceX, sourceY));
        path.add(grid.getCell(sourceX, sourceY));
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
        while(!path.isEmpty())
        {
        	s=path.poll();
        	//parentList.poll();
        	//s.hoverUnhighlight();
        	if(s == goal)
        	{
            	s.hoverUnhighlight();
        		System.out.println("path found");
        		pathFound = true;
        		break;
        	}
        	closed[s.getRow()][s.getColumn()] = true;
            currentX = s.getColumn();
            currentY = s.getRow();
            
            //pos 4
            if(currentX-1>=0){
            	sPrime = grid.getCell(currentX-1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughCostVH,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostVH,closed,path,parentList);
            	else
            		updateVertexUnifrom(s,sPrime,s.getF()+costVH,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+roughCostD,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostD,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertexUnifrom(s,sPrime,s.getF()+costVHWay,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertexUnifrom(s,sPrime,s.getF()+roughVHWay,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughVHWay,closed,path,parentList); 
                	else
                		updateVertexUnifrom(s,sPrime,s.getF()+costD,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+roughCostD,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostD,closed,path,parentList);
                	else
                		updateVertexUnifrom(s,sPrime,s.getF()+costD,closed,path,parentList);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughCostVH,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostVH,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertexUnifrom(s,sPrime,s.getF()+costVHWay,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughVHWay,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughVHWay,closed,path,parentList); 
            	else
            		updateVertexUnifrom(s,sPrime,s.getF()+costVH,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughCostVH,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostVH,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertexUnifrom(s,sPrime,s.getF()+costVHWay,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughVHWay,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughVHWay,closed,path,parentList); 
            	else
            		updateVertexUnifrom(s,sPrime,s.getF()+costVH,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughCostVH,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostVH,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertexUnifrom(s,sPrime,s.getF()+costVHWay,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertexUnifrom(s,sPrime,s.getF()+roughVHWay,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughVHWay,closed,path,parentList); 
            	else
            		updateVertexUnifrom(s,sPrime,s.getF()+costVH,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+roughCostD,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostD,closed,path,parentList);
                	else
                		updateVertexUnifrom(s,sPrime,s.getF()+costD,closed,path,parentList); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+roughCostD,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostD,closed,path,parentList);
                	else
                		updateVertexUnifrom(s,sPrime,s.getF()+costD,closed,path,parentList); 
                }  
            }
        }
        if(s != goal)
        {
        	System.out.println("No Path Found");
        }
        
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	while(goal.cell != null)
        	{	
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;		
        	}
        }
        System.out.println("Uniform Search TotalCost: " + totalCost);
        System.out.println("Uniform Search cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
        
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
	}

	public void updateVertexUnifrom(Cell s, Cell sPrime, double cost,boolean[][] closed
							,PriorityQueue<Cell> fringe,LinkedList<Cell> parent)
	{
		//closed[120][160]
		//cost = s.getF+cellCost
		
		if(sPrime == null || sPrime.getType() == 0 || closed[sPrime.getRow()][sPrime.getColumn()]== true)
			return;
		double fCost = cost;

		
		if(!fringe.contains(sPrime) || sPrime.getF()>fCost)
		{
			if(fringe.contains(sPrime))
			{
				fringe.remove(sPrime);
			}
			sPrime.setH(0);
			sPrime.setG(cost);
			sPrime.setF();
			fringe.add(sPrime);
			timeCounter++;
			sPrime.cell=s;			
			//sPrime.hoverHighlight();
			closed[sPrime.getRow()][sPrime.getColumn()] = true;
		}
		
	}
	public void pathWeighted(double weightValue)
	{      
		Comparator<Cell> comparator = new CellCompare();
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(200,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
        int heuristicCost = 0;//HeuristicCost
        int finalCost = 0; //g+h
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance;			
        //heuristic
        grid.getCell(sourceX, sourceY).setG(0);
        parentList.add(grid.getCell(sourceX, sourceY));
        path.add(grid.getCell(sourceX, sourceY));
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
        while(!path.isEmpty())
        {
        	s=path.poll();
        	//parentList.poll();
        	//s.hoverUnhighlight();
        	if(s == goal)
        	{
            	s.hoverUnhighlight();
        		System.out.println("path found");
        		pathFound = true;
        		break;
        	}
        	closed[s.getRow()][s.getColumn()] = true;
            currentX = s.getColumn();
            currentY = s.getRow();
            
            //pos 4
            if(currentX-1>=0){
            	sPrime = grid.getCell(currentX-1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else
            		updateVertexWeighted(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertexWeighted(s,sPrime,s.getF()+costVHWay,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertexWeighted(s,sPrime,s.getF()+roughVHWay,euclideanDistance*weightValue,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance*weightValue,closed,path,parentList); 
                	else
                		updateVertexWeighted(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else
                		updateVertexWeighted(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertexWeighted(s,sPrime,s.getF()+costVHWay,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertexWeighted(s,sPrime,s.getF()+roughVHWay,euclideanDistance*weightValue,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance*weightValue,closed,path,parentList); 
            	else
            		updateVertexWeighted(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertexWeighted(s,sPrime,s.getF()+costVHWay,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertexWeighted(s,sPrime,s.getF()+roughVHWay,euclideanDistance*weightValue,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance*weightValue,closed,path,parentList); 
            	else
            		updateVertexWeighted(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertexWeighted(s,sPrime,s.getF()+costVHWay,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertexWeighted(s,sPrime,s.getF()+roughVHWay,euclideanDistance*weightValue,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance*weightValue,closed,path,parentList); 
            	else
            		updateVertexWeighted(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else
                		updateVertexWeighted(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexWeighted(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else
                		updateVertexWeighted(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList); 
                }  
            }
        }
        if(s != goal)
        {
        	System.out.println("No Path Found");
        }
        
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	while(goal.cell != null)
        	{	
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;		
        	}
        }
        System.out.println("Weighted Euclidean TotalCost: " + totalCost);
        System.out.println("Weighted cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
	}

	public void updateVertexWeighted(Cell s, Cell sPrime, double cost, double heuristicCost,boolean[][] closed
							,PriorityQueue<Cell> fringe,LinkedList<Cell> parent)
	{
		//closed[120][160]
		//cost = s.getF+cellCost
		
		if(sPrime == null || sPrime.getType() == 0 || closed[sPrime.getRow()][sPrime.getColumn()]== true)
			return;
		double fCost = heuristicCost+cost;

		
		if(!fringe.contains(sPrime) || sPrime.getF()>fCost)
		{
			if(fringe.contains(sPrime))
			{
				fringe.remove(sPrime);
			}
			sPrime.setH(heuristicCost);
			sPrime.setG(cost);
			sPrime.setF();
			fringe.add(sPrime);
			timeCounter++;
			sPrime.cell=s;			
			//sPrime.hoverHighlight();
			closed[sPrime.getRow()][sPrime.getColumn()] = true;
		}
		
	}
	
	public void manhatten()
	{      
		Comparator<Cell> comparator = new CellCompare();
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(200,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
        int heuristicCost = 0;//HeuristicCost
        int finalCost = 0; //g+h
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance;		//Actually Manhatten distance	
        //heuristic
        grid.getCell(sourceX, sourceY).setG(0);
        parentList.add(grid.getCell(sourceX, sourceY));
        path.add(grid.getCell(sourceX, sourceY));
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
        while(!path.isEmpty())
        {
        	s=path.poll();
        	//parentList.poll();
        	//s.hoverUnhighlight();
        	if(s == goal)
        	{
            	s.hoverUnhighlight();
        		System.out.println("path found");
        		pathFound = true;
        		break;
        	}
        	closed[s.getRow()][s.getColumn()] = true;
            currentX = s.getColumn();
            currentY = s.getRow();
            
            //pos 4
            if(currentX-1>=0){
            	sPrime = grid.getCell(currentX-1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList);          		
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }  
            }
        }
        if(s != goal)
        {
        	System.out.println("No Path Found");
        }
        
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	while(goal.cell != null)
        	{	
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;		
        	}
        }
        System.out.println("AStar Manhattan Heuristic TotalCost: " + totalCost);
        System.out.println("AStar Manhattan Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
	}

	
	public void agentDistance()
	{      
		Comparator<Cell> comparator = new CellCompare();
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(200,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
        int futureX;
        int futureY;
        int heuristicCost = 0;//HeuristicCost
        int finalCost = 0; //g+h
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance = 0;		//Actually Manhatten distance	
        //heuristic
        grid.getCell(sourceX, sourceY).setG(0);
        parentList.add(grid.getCell(sourceX, sourceY));
        path.add(grid.getCell(sourceX, sourceY));
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
            currentX = s.getColumn();
            currentY = s.getRow();
            futureX = goal.getColumn();
            futureY = goal.getRow();
            
            while(currentX != futureX && currentY != futureY)
            {            		
            	grid.getCell(currentX, currentY).hoverHighlight();

            	euclideanDistance = Math.sqrt(2);
            	if(currentX > futureX)
            		currentX--;
            	else
            		currentX++;
            	
            	if(currentY > futureY)
            		currentY--;
            	else
            		currentY++;

            }

        System.out.println("Agent Distance Heuristic TotalCost: " + euclideanDistance);
        //System.out.println("AStar Euclidean Heuristic cell cost total: " + cellTotal);
        //System.out.println("Number of cells traversed: " + timeCounter);
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
	}
	public void opposite()
	{      
		Comparator<Cell> comparator = new CellCompare();
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(200,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell s = grid.getCell(destX, destY);
        Cell goal = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
        int heuristicCost = 0;//HeuristicCost
        int finalCost = 0; //g+h
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance;			
        //heuristic
        s.setG(0);
        parentList.add(s);
        path.add(s);
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
        while(!path.isEmpty())
        {
        	s=path.poll();
        	//parentList.poll();
        	//s.hoverUnhighlight();
        	if(s == goal)
        	{
            	s.hoverUnhighlight();
        		System.out.println("path found");
        		pathFound = true;
        		break;
        	}
        	closed[s.getRow()][s.getColumn()] = true;
            currentX = s.getColumn();
            currentY = s.getRow();
            
            //pos 4
            if(currentX-1>=0){
            	sPrime = grid.getCell(currentX-1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList);          		
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }  
            }
        }
        if(s != goal)
        {
        	System.out.println("No Path Found");
        }
        
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	while(goal.cell != null)
        	{	
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;		
        	}
        }
        System.out.println("AStar Euclidean Heuristic TotalCost: " + totalCost);
        System.out.println("AStar Euclidean Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
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
	
	public void tenPoints(int pointCounter)
	{
		System.out.println(pointCounter);
	}
	
	public void cleanHighway()
	{
        for(int i=0;i<120;i++)
        {
        	for(int j=0;j<160;j++)
        	{
        		if(grid.getCell(j, i).getType() == 3 || grid.getCell(j, i).getType() == 4)
        		{
        			grid.getCell(j,i).highwayColor();
        		}
        	}
        }
	}
}



