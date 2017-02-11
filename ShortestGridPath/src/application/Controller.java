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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
				aStar();
			}
		});
		
		uniform.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				uniform();
			}
		});
		
		aWeight.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				String str = aWeight.getText();
				double d = Double.parseDouble(str);
				weighted(d);
			}
		});
		
	    fiveMaps.getItems().addAll("Map 1","Map 2","Map 3","Map 4","Map 5");
		fiveMaps.getSelectionModel().selectedIndexProperty().addListener(new
									 ChangeListener<Number>()
		{
				public void changed(ObservableValue ov, Number value, Number new_value) 
				{
					if(new_value.intValue() >= 0 && new_value.intValue() < 5)
						CreateGrid(10);
				}
		});
	    
	    tenPoints.getItems().addAll("Point 1","Point 2","Point 3","Point 4","Point 5"
	    		,"Point 6","Point 7","Point 8","Point 9","Point 10");

		tenPoints.getSelectionModel().selectedIndexProperty().addListener(new
				 ChangeListener<Number>()
		{
			public void changed(ObservableValue ov, Number value, Number new_value) 
			{
				if(new_value.intValue() >= 0 && new_value.intValue() < 10)
					tenPoints();
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
				else if(new_value.intValue() == 1)
					blockedCells();
				else if(new_value.intValue() == 2)
					agentDistance();
				else if(new_value.intValue() == 3)
					opposite();
				
			}
		});
		
    }
	
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
	    		    rough1 = rand.nextInt(2);  // 50%
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
	    			    	grid.getCell(goalX, goalY).setValue(1);
	            			grid.getCell(startX, startY).start();
	                        grid.getCell(goalX, goalY).goal();
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
	      String sourceStr;
	      String destStr;
	      String tempSourceX = "";
	      String tempSourceY = "";
	      String tempDestX = "";
	      String tempDestY = "";

	      sourceStr = line.get(index);
	      index++;
	      destStr = line.get(index);
	      
	      for(int x=0;x<sourceStr.length();x++)
	      {
	    	  if(Character.isDigit(sourceStr.charAt(x)))
	    		  tempSourceX = tempSourceX + (sourceStr.charAt(x)-'0');
	    	  else
	    	  {
	    		  sx = Integer.parseInt(tempSourceX.trim());
	    		  x=sourceStr.length();
	    	  }
	      }
	      int comma = sourceStr.indexOf(",");
	      tempSourceY = sourceStr.substring(comma+1);
		  sy = Integer.parseInt(tempSourceY.trim());
		  
	      for(int x=0;x<destStr.length();x++)
	      {
	    	  if(Character.isDigit(destStr.charAt(x)))
	    		  tempDestX = tempDestX + (destStr.charAt(x)-'0');
	    	  else
	    	  {
	    		  dx = Integer.parseInt(tempDestX.trim());
	    		  x=destStr.length();
	    	  }
	      }
	      comma = destStr.indexOf(",");
	      tempDestY = destStr.substring(comma+1);
		  dy = Integer.parseInt(tempDestY.trim());
	      
		  index = 10;
		  char c;
	      for(int i=0; i<120;i++)
	      {
	    	  String str = line.get(index);
	    	  String trim;
    		  trim = str.replaceAll("\\s", "");
	    	  for(int j=0; j<160;j++)
	    	  {
	    		  c = trim.charAt(j);
	    		  if(c == '0')
	    		  {
	    			  grid.getCell(j, i).setValue(0);
	    			  grid.getCell(j, i).setType(0);
	    			  grid.getCell(j, i).highlight();
	    		  }
	    		  else if(c == '1')
	    		  {
	    			  grid.getCell(j, i).setValue(1);
	    			  grid.getCell(j, i).setType(1);
	    		  }
	    		  else if(c == '2')
	    		  {
	    			  grid.getCell(j, i).setValue(2);
	    			  grid.getCell(j, i).setType(2);
	    			  grid.getCell(j, i).brownReadFile();
	    		  }
	    		  else if(c == 'a')
	    		  {
	    			  grid.getCell(j, i).setValue(0.25);
	    			  grid.getCell(j, i).setType(3);
	    			  grid.getCell(j, i).highwayColor();
	    		  }
	    		  else if(c == 'b')
	    		  {
	    			  grid.getCell(j, i).setValue(0.5);
	    			  grid.getCell(j, i).setType(4);
	    			  grid.getCell(j, i).highwayColor();
	    		  }
	    	  }
	    	  index++;
	      }
	      
	      grid.getCell(sx, sy).setValue(1);
	      grid.getCell(sx, sy).setType(5);
	      grid.getCell(dx, dy).setValue(1);	
	      grid.getCell(dx, dy).setType(5);
	      grid.getCell(sx, sy).start();
	      grid.getCell(dx, dy).goal();
	      
	      sourceX = sx;
	      sourceY = sy;
	      destX = dx;
	      destY = dy;
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
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(1000,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int eucX;//euclidean dis
        int eucY;
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
        double euclideanDistance;				//heuristic
        
        grid.getCell(sourceX, sourceY).setG(0);
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
        s.setH(euclideanDistance);
        s.setF();
        parentList.add(grid.getCell(sourceX, sourceY));
        path.add(grid.getCell(sourceX, sourceY));
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        
        while(!path.isEmpty())
        {
        	s=path.poll();

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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList);          		
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
        	grid.pathIsColored.add(s);
        	while(goal.cell != null)
        	{	
        		grid.pathIsColored.add(goal.cell);
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
		//heuristicCost= heuristicCost/4;
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
        PriorityQueue<Cell> path = new PriorityQueue<Cell>(1000,comparator);
        LinkedList<Cell> parentList = new LinkedList<Cell>();
        
        grid.hoverUnhighlight();
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        timeCounter = 0;
        int currentX, currentY;//present cell
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        //heuristic
        grid.getCell(sourceX, sourceY).setG(0);
        s.setH(0);
        s.setF();
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
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+roughCostD,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertexUnifrom(s,sPrime,s.getF()+emptyRoughCostD,closed,path,parentList); 
                	else
                		updateVertexUnifrom(s,sPrime,s.getF()+costD,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
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
        	grid.pathIsColored.add(s);
        	while(goal.cell != null)
        	{	
        		grid.pathIsColored.add(goal.cell);
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
			//sPrime.setH(0);
			sPrime.setG(cost);
			sPrime.setF();
			fringe.add(sPrime);
			timeCounter++;
			sPrime.cell=s;			
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
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
		s.setH(euclideanDistance);
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
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertex(s,sPrime,s.getF()+costVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+roughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList); 
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
    			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
    			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
    			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance*weightValue,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,(euclideanDistance*weightValue)/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance*weightValue,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
        			eucX=(sPrime.getColumn()+1)-(goal.getColumn()+1);
        			eucY=(sPrime.getRow()+1)-(goal.getRow()+1);
        			euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance*weightValue,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance*weightValue,closed,path,parentList); 
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
        	grid.pathIsColored.add(s);
        	while(goal.cell != null)
        	{	
        		grid.pathIsColored.add(goal.cell);
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
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		euclideanDistance = Math.abs(eucX)+Math.abs(eucY);
		s.setH(euclideanDistance);
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList);          		
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
        	grid.pathIsColored.add(s);
        	while(goal.cell != null)
        	{	
        		grid.pathIsColored.add(goal.cell);
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
        int futureX;
        int futureY;
        int costVH = 1;
        double costD = Math.sqrt(2);
        int roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance = 0;			
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
            s.setH(euclideanDistance);
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
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList);          		
                	else
                		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
                	//pos 7
                    if(currentY-1>=0){                      
                    	sPrime = grid.getCell(currentX-1, currentY-1);
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
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
                	else
                		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
                }
                //pos 8
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX, currentY+1);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
                	else
                		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
                }
                //pos 6
                if(currentX+1<160){
                	sPrime = grid.getCell(currentX+1, currentY);
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 3 && sPrime.getType() == 3) 
                		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
                	else if(s.getType() == 4 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
                	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
                		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
                	else
                		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
                	//pos 9
                    if(currentY-1>=0){
                    	sPrime = grid.getCell(currentX+1, currentY-1);
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
            	grid.pathIsColored.add(s);
            	while(goal.cell != null)
            	{	
            		grid.pathIsColored.add(goal.cell);
            		goal.hoverHighlight();
            		totalCost = totalCost + goal.getF();
            		cellTotal = cellTotal + goal.getValue();
            		goal=goal.cell;		
            	}
            }
            
        System.out.println("Agent Distance Heuristic TotalCost: " + euclideanDistance);
        System.out.println("Agent Distance Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
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
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		euclideanDistance = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));        
		s.setH(euclideanDistance);
        parentList.add(s);
        path.add(s);
        boolean closed[][] = new boolean[120][160];
        boolean pathFound = false;
        //time start
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList);          		
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
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
        //time end
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	grid.pathIsColored.add(s);
        	while(goal.cell != null)
        	{	
        		grid.pathIsColored.add(goal.cell);
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;		
        	}
        }
        System.out.println("AStar Reverse Euclidean Heuristic TotalCost: " + totalCost);
        System.out.println("AStar Reverse Euclidean Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + timeCounter);
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
	}
	
	public void blockedCells()
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
        int futureX, futureY;
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
        
        if(s != goal)
        {
        	System.out.println("No Path Found");
        }
        currentX = s.getColumn();
        currentY = s.getRow();
        futureX = goal.column;
        futureY = goal.getRow();
         euclideanDistance = 0;
        for(int i=Math.min(futureX, currentX);i<=Math.max(futureX,currentX);i++)
        {
        	for(int j=Math.min(futureY, currentY);j<=Math.max(futureY, currentY);j++)
        	{
        		if(grid.getCell(i, j).getType()==0)
        			euclideanDistance++;
        	}
        }
        s.setH(euclideanDistance);
        while(!path.isEmpty())
        {
        	s=path.poll();
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

            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance,closed,path,parentList);          		
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
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
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+roughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getF()+emptyRoughCostVH,euclideanDistance,closed,path,parentList);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getF()+costVHWay,euclideanDistance/4,closed,path,parentList);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+roughVHWay,euclideanDistance/4,closed,path,parentList);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getF()+emptyRoughVHWay,euclideanDistance/4,closed,path,parentList); 
            	else
            		updateVertex(s,sPrime,s.getF()+costVH,euclideanDistance,closed,path,parentList); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
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
                 	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+roughCostD,euclideanDistance,closed,path,parentList);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getF()+emptyRoughCostD,euclideanDistance,closed,path,parentList);
                	else
                		updateVertex(s,sPrime,s.getF()+costD,euclideanDistance,closed,path,parentList); 
                }  
            }
        }
        
        double totalCost = 0;
        double cellTotal = 0;
        if(pathFound == true)
        {
        	while(goal.cell != null)
        	{	
            	grid.pathIsColored.add(s);
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
	
	public void tenPoints()
	{
        //grid.hoverUnhighlight();

        grid.getCell(sourceX, sourceY).setType(1);
        grid.getCell(sourceX, sourceY).setValue(1);
        grid.getCell(sourceX, sourceY).startClean();
        grid.getCell(destX, destY).setType(1);
        grid.getCell(destX, destY).setValue(1);
        grid.getCell(destX, destY).goalClean();
        
        Random rand = new Random();
        //Select random start and goal points   
        boolean distance100 = true;
        while(distance100)
        {
        	int sX;
        	int sY;
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
        		sX = rand.nextInt(20);
        	else
        		sX = rand.nextInt((maxX-minX)+1) + minX;
        	
        	if(leftTwenty == 1)
        		sY = rand.nextInt(20);
        	else
        		sY = rand.nextInt((maxY-minY)+1) + minY;
        	
        	if(goalTop == 1)
        		goalX = rand.nextInt(20);
        	else
        		goalX = rand.nextInt((maxX-minX)+1) + minX;
        	
        	if(goalLeft == 1)
        		goalY = rand.nextInt(20);
        	else
        		goalY = rand.nextInt((maxY-minY)+1) + minY;
        	
        	
        	double euclidean;
        	int xDis = (sX+1)-(goalX+1);
        	int yDis = (sY+1)-(goalY+1);
        	euclidean = Math.sqrt(Math.pow(xDis, 2)+Math.pow(yDis, 2));
        	
        	//If distance is less than 100 select new points
        	if(euclidean >= 100)
        	{
        		//X = col, Y = row
        		if(grid.getCell(sX, sY).getType() !=0 || grid.getCell(sX, sY).getType() !=5 
        				|| grid.getCell(sX, sY).getType() !=3 || grid.getCell(sX, sY).getType() !=4)
        		{
        			if(grid.getCell(goalX, goalY).getType() != 0 || grid.getCell(goalX, goalY).getType() !=5
        					|| grid.getCell(goalX, goalY).getType() !=3 || grid.getCell(goalX, goalY).getType() !=4)
        			{
    			    	grid.getCell(sX, sY).setType(5);
    			    	grid.getCell(sX, sY).setValue(1);
    			    	grid.getCell(goalX, goalY).setType(5);
    			    	grid.getCell(goalX, goalY).setValue(1);
            			grid.getCell(sX, sY).start();
                        grid.getCell(goalX, goalY).goal();
                        sourceX = sX;
                        sourceY = sY;
                        destX = goalX;
                        destY = goalY;
                        distance100 = false;
        			}      			
        		}
        	}
        }   
        grid.hoverUnhighlight();
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
	
	public void aStar()
	{
		AStar star = new AStar(grid,sourceX,sourceY,destX,destY);
		star.pathSearch();
		star.totalCost();
	}
	public void uniform()
	{
		UniformCostSearch uni = new UniformCostSearch(grid,sourceX,sourceY,destX,destY);
		uni.pathSearch();
		uni.totalCost();		
	}
	public void weighted(double d)
	{
		WeightedAStar weight = new WeightedAStar(grid,sourceX,sourceY,destX,destY);
		weight.pathSearch(d);
		weight.totalCost();		
	}
}



