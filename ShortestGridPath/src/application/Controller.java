import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
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
	@FXML Button saveGrid, loadGrid, createGrid, startPath, uniform,test; 
	@FXML Label xInfo,yInfo,gInfo,fInfo,hInfo,timeInfo;
	@FXML TextField aWeight;
	@FXML ChoiceBox<String> fiveMaps,tenPoints,heuristic;
	
	private IntegerProperty xCoor;
	private IntegerProperty yCoor;	
	private IntegerProperty time;
	private DoubleProperty gInt;
	private DoubleProperty fInt;	
	private DoubleProperty hInt;
	public static final Duration INDEFINITE = new Duration(Double.POSITIVE_INFINITY);
	PerformanceTest pTest = new PerformanceTest();
	
    static int rows = 120;
    static int columns = 160;
    static int value = 1;
    static int type = 1;
    
    double width = 1200;
    double height = 670;
    Grid grid;
    Grid fiveGrids[];
    public static int sourceX;
    public static int sourceY;
    public static int destX;
    public static int destY;
    int roughArrayX[] = new int[8];
    int roughArrayY[] = new int[8];
    
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
		test.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event)
			{
				testData();
			}
		});
		
	    fiveMaps.getItems().addAll("Map 1","Map 2","Map 3","Map 4","Map 5");
		fiveMaps.getSelectionModel().selectedIndexProperty().addListener(new
									 ChangeListener<Number>()
		{
				public void changed(ObservableValue ov, Number value, Number new_value) 
				{
					if(new_value.intValue() >= 0 && new_value.intValue() < 5)
					{
						if(!list.getChildren().isEmpty())
							flush();
						CreateGrid(10);
					}
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
		
	    heuristic.getItems().addAll("Manhatten","Blocked Cells","Agent Distance","Opposite","Sequential Heuristic A*","Integrated Heuristic A*");

		heuristic.getSelectionModel().selectedIndexProperty().addListener(new
				 ChangeListener<Number>()
		{
			public void changed(ObservableValue ov, Number value, Number new_value) 
			{
				if(new_value.intValue() == 0)
					manhattenDis();
				else if(new_value.intValue() == 1)
					blocked();
				else if(new_value.intValue() == 2)
					agentDis();
				else if(new_value.intValue() == 3)
					oppositeAStar();
				else if(new_value.intValue() == 4)
					sequentialHeuristic();
				else if(new_value.intValue() == 5)
					integratedHeuristic();
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
	        		if(grid.getCell(startX, startY).getType() !=0 || grid.getCell(startX, startY).getType() !=5 || grid.getCell(startX, startY).getType() != 6
	        				|| grid.getCell(startX, startY).getType() !=3 || grid.getCell(startX, startY).getType() !=4)
	        		{
	        			if(grid.getCell(goalX, goalY).getType() != 0 || grid.getCell(goalX, goalY).getType() !=5 || grid.getCell(goalX, goalY).getType() != 6
	        					|| grid.getCell(goalX, goalY).getType() !=3 || grid.getCell(goalX, goalY).getType() !=4)
	        			{
	    			    	grid.getCell(startX, startY).setType(5);
	    			    	grid.getCell(startX, startY).setValue(1);
	    			    	grid.setStartX(startX);
	    			    	grid.setStartY(startY);
	    			    	grid.getCell(goalX, goalY).setType(6);
	    			    	grid.getCell(goalX, goalY).setValue(1);
	    			    	grid.setEndX(goalX);
	    			    	grid.setEndY(goalY);
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
		list.getChildren().clear();
		grid = null;
		//System.gc();
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
					rowStr = rowStr + '1' + " ";
					break;
				}
				case(6):
				{
					rowStr = rowStr + '1' + " ";
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
	      grid.getCell(dx, dy).setType(6);
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
		
	public void tenPoints()
	{
        //grid.hoverUnhighlight();
		int startX = grid.getStartX();
		int startY = grid.getStartY();
		int finishX = grid.getEndX();
		int finishY = grid.getEndY();
        
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
        		if(grid.getCell(sX, sY).getType() !=0 || grid.getCell(sX, sY).getType() !=5 || grid.getCell(sX, sY).getType() != 6
        				|| grid.getCell(sX, sY).getType() !=3 || grid.getCell(sX, sY).getType() !=4 )
        		{
        			if(grid.getCell(goalX, goalY).getType() != 0 || grid.getCell(goalX, goalY).getType() !=5 || grid.getCell(sX, sY).getType() != 6
        					|| grid.getCell(goalX, goalY).getType() !=3 || grid.getCell(goalX, goalY).getType() !=4)
        			{
    			    	grid.getCell(sX, sY).setType(5);
    			    	grid.getCell(sX, sY).setValue(1);
    			    	grid.getCell(goalX, goalY).setType(6);
    			    	grid.getCell(goalX, goalY).setValue(1);
            			grid.getCell(sX, sY).start();
                        grid.setStartX(sX);
                        grid.setStartY(sY);
                        grid.getCell(goalX, goalY).goal();
                        grid.setEndX(goalX);
                        grid.setEndY(goalY);
                        sourceX = sX;						//update 
                        sourceY = sY;						//update
                        destX = goalX;						//update 
                        destY = goalY;						//update 
                        distance100 = false;
        			}      			
        		}
        	}
        }   
        //old start/end points convert to empty cells
        grid.getCell(startX, startY).setType(1);
        grid.getCell(startX, startY).setValue(1);
        grid.getCell(startX, startY).startClean();
        grid.getCell(finishX, finishY).setType(1);
        grid.getCell(finishX, finishY).setValue(1);
        grid.getCell(finishX, finishY).goalClean();
        grid.hoverUnhighlight();
	}
	//Delete
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
		long memTest;
		memTest = pTest.memUsage();
		long t;
		AStar star = new AStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		star.clean();
		t = System.currentTimeMillis();
		star.pathSearch();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		star.totalCost();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void uniform()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		UniformCostSearch uni = new UniformCostSearch(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		uni.clean();
		t = System.currentTimeMillis();
		uni.pathSearch();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		uni.totalCost();	
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void weighted(double d)
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		WeightedAStar weight = new WeightedAStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		weight.clean();
		t = System.currentTimeMillis();
		weight.pathSearch(d);
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		weight.totalCost();	
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void manhattenDis()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		Manhatten ny = new Manhatten(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		ny.clean();
		t = System.currentTimeMillis();
		ny.pathSearch();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		ny.totalCost();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void agentDis()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		AgentDistance star = new AgentDistance(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		star.clean();
		t = System.currentTimeMillis();
		star.pathSearch();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		star.totalCost();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void oppositeAStar()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		OppositeAStar star = new OppositeAStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		star.clean();
		t = System.currentTimeMillis();
		star.pathSearch();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		star.totalCost();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void blocked()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		BlockedCells star = new BlockedCells(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		star.clean();
		t = System.currentTimeMillis();
		star.pathSearch();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		star.totalCost();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void sequentialHeuristic()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		SequentialHeuristic sh = new SequentialHeuristic(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		sh.clean();
		t = System.currentTimeMillis();
		sh.mainSequence();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		sh.printPath();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	public void integratedHeuristic()
	{
		long memTest;
		memTest = pTest.memUsage();
		long t;
		IntegratedAStar ih = new IntegratedAStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		ih.clean();
		t = System.currentTimeMillis();
		ih.mainIntegrated();
		t = System.currentTimeMillis()-t;
		time.setValue(t);
		ih.printPath();
		memTest = memTest - pTest.memUsage();
		System.out.println("Time:" + t +"ms");
		System.out.println("Search memory used in bytes: " + memTest);
	}
	
	//Create 5 Maps, then 10 Points and test for each search algorithm. Then save results in a file
	public void testData()
	{
		int map = 0;
		int point = 0;
		int counter = 0;
		long memTest;
		long t;
		long[] timeDataAstar = new long[50];
		long[] timeDataUniform = new long[50];
		long[] timeDataWeighted1point5 = new long[50];
		long[] timeDataWeighted1point25 = new long[50];
		long[] timeDataManhatten = new long[50];
		long[] timeDataAgent = new long[50];
		long[] timeDataOpposite = new long[50];
		long[] timeDataBlock = new long[50];
		long[] timeDataSequential = new long[50];
		
		long[] memAstar = new long[50];
		long[] memUniform = new long[50];
		long[] memWeighted1point5 = new long[50];
		long[] memWeighted1point25 = new long[50];
		long[] memManhatten = new long[50];
		long[] memAgent = new long[50];
		long[] memOpposite = new long[50];
		long[] memBlock = new long[50];
		long[] memSequential = new long[50];

		int[] pathAstar = new int[50];
		int[] pathUniform = new int[50];
		int[] pathWeighted1point5 = new int[50];
		int[] pathWeighted1point25 = new int[50];
		int[] pathManhatten = new int[50];
		int[] pathAgent = new int[50];
		int[] pathOpposite = new int[50];
		int[] pathBlock = new int[50];
		int[] pathSequential = new int[50];

		int[] blockAstar = new int[50];
		int[] blockUniform = new int[50];
		int[] blockWeighted1point5 = new int[50];
		int[] blockWeighted1point25 = new int[50];
		int[] blockManhatten = new int[50];
		int[] blockAgent = new int[50];
		int[] blockOpposite = new int[50];
		int[] blockBlock = new int[50];
		int[] blockSequential = new int[50];
		
		while(map < 5)
		{
			flush();
			CreateGrid(10);
			point=0;
			while(point<10)
			{					
				//write gui map to new file, then run algo.
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
						case(6):
						{
							rowStr = rowStr + 'g' + " ";
							break;
						}
						
						}			
					}
					line.add(rowStr);
				}
				counter++;
				Path file = Paths.get("../../Test/grid"+counter+".txt");
				try {
					Files.write(file, line, Charset.forName("UTF-8"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				AStar star = new AStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				UniformCostSearch uni = new UniformCostSearch(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				WeightedAStar weightOne = new WeightedAStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				WeightedAStar weightTwo = new WeightedAStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				Manhatten ny = new Manhatten(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				AgentDistance agent = new AgentDistance(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				OppositeAStar oppo = new OppositeAStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				BlockedCells block = new BlockedCells(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				SequentialHeuristic sh = new SequentialHeuristic(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
				
				//AStar
				memTest = pTest.memUsage();
				star.clean();
				t = System.currentTimeMillis();
				star.pathSearch();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataAstar[counter-1] = t;
				memAstar[counter-1] = memTest;
				pathAstar[counter-1] = star.getPathLength();
				blockAstar[counter-1] = star.getBlockCounter();
				//Uniform
				memTest = pTest.memUsage();
				uni.clean();
				t = System.currentTimeMillis();
				uni.pathSearch();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataUniform[counter-1] = t;
				memUniform[counter-1] = memTest;
				pathUniform[counter-1] = uni.getPathLength();
				blockUniform[counter-1] = uni.getBlockCounter();
				//Weighted value = 1.5
				memTest = pTest.memUsage();
				weightOne.clean();
				t = System.currentTimeMillis();
				weightOne.pathSearch(1.5);
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataWeighted1point5[counter-1] = t;
				memWeighted1point5[counter-1] = memTest;
				pathWeighted1point5[counter-1] = weightOne.getPathLength();
				blockWeighted1point5[counter-1] = weightOne.getBlockCounter();
				//Weighted value = 1.25
				memTest = pTest.memUsage();
				weightTwo.clean();
				t = System.currentTimeMillis();
				weightTwo.pathSearch(1.25);
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataWeighted1point25[counter-1] = t;
				memWeighted1point25[counter-1] = memTest;
				pathWeighted1point25[counter-1] = weightTwo.getPathLength();
				blockWeighted1point25[counter-1] = weightTwo.getBlockCounter();
				//Manhatten
				memTest = pTest.memUsage();
				ny.clean();
				t = System.currentTimeMillis();
				ny.pathSearch();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataManhatten[counter-1] = t;
				memManhatten[counter-1] = memTest;
				pathManhatten[counter-1] = ny.getPathLength();
				blockManhatten[counter-1] = ny.getBlockCounter();
				//Agent Distance
				memTest = pTest.memUsage();
				agent.clean();
				t = System.currentTimeMillis();
				agent.pathSearch();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataAgent[counter-1] = t;
				memAgent[counter-1] = memTest;
				pathAgent[counter-1] = agent.getPathLength();
				blockAgent[counter-1] = agent.getBlockCounter();
				//Opposite Astar
				memTest = pTest.memUsage();
				oppo.clean();
				t = System.currentTimeMillis();
				oppo.pathSearch();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataOpposite[counter-1] = t;
				memOpposite[counter-1] = memTest;
				pathOpposite[counter-1] = oppo.getPathLength();
				blockOpposite[counter-1] = oppo.getBlockCounter();
				//Blocked Cells
				memTest = pTest.memUsage();
				block.clean();
				t = System.currentTimeMillis();
				block.pathSearch();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataBlock[counter-1] = t;
				memBlock[counter-1] = memTest;
				pathBlock[counter-1] = block.getPathLength();
				blockBlock[counter-1] = block.getBlockCounter();
				//Sequential Heuristic
				memTest = pTest.memUsage();
				sh.clean();
				t = System.currentTimeMillis();
				sh.mainSequence();
				t = System.currentTimeMillis()-t;
				memTest = memTest - pTest.memUsage();
				timeDataSequential[counter-1] = t;
				memSequential[counter-1] = memTest;
				pathSequential[counter-1] = sh.getPathLength();
				blockSequential[counter-1] = sh.getBlockCounter();
				
				point++;
				tenPoints();
			}
			map++;
		}
		
		LinkedList<String> line = new LinkedList<String>();
		LinkedList<String> avgLine = new LinkedList<String>();
		line.add("Raw Data for 5 Heuristics, Sequential Heuristic A* and Integrated Heuristic A*");
		avgLine.add("Averages for 5 Heuristics, Sequential Heurisitic A* and Integrated Heuristic A*");
		for(int i=1;i<=5;i++)
		{
			double[] time = new double[9];
			double[] mem = new double[9];
			double[] pathL = new double[9];
			double[] blockExpanded = new double[9];
			
			line.add("");
			line.add("Map:"+i);
			avgLine.add("");
			avgLine.add("Map:"+i);		
			for(int j=1;j<=10;j++)
			{
				line.add("");
				line.add("Start/Goal Points:"+j);
				line.add("AStar Euclidean:		time:"+timeDataAstar[(i*j)-1]+"ms"
							+" memory usage:"+memAstar[(i*j)-1]+"bytes"
							+" path length:" +pathAstar[(i*j)-1]
							+"	nodes expanded:"+blockAstar[(i*j)-1]);
				line.add("Uniform Cost Search:		time:"+timeDataUniform[(i*j)-1]+"ms"
						+" memory usage:"+memUniform[(i*j)-1]+"bytes"
						+" path length:" +pathUniform[(i*j)-1]
						+"	nodes expanded:"+blockUniform[(i*j)-1]);
				line.add("Weighted Astar Value(1.5):	time:"+timeDataWeighted1point5[(i*j)-1]+"ms"
						+" memory usage:"+memWeighted1point5[(i*j)-1]+"bytes"
						+" path length:" +pathWeighted1point5[(i*j)-1]
						+"	nodes expanded:"+blockWeighted1point5[(i*j)-1]);
				line.add("Weighted Astar Value(1.25):	time:"+timeDataWeighted1point25[(i*j)-1]+"ms"
						+" memory usage:"+memWeighted1point25[(i*j)-1]+"bytes"
						+" path length:" +pathWeighted1point25[(i*j)-1]
						+"	nodes expanded:"+blockWeighted1point25[(i*j)-1]);
				line.add("Astar Manhattan:		time:"+timeDataManhatten[(i*j)-1]+"ms"
						+" memory usage:"+memManhatten[(i*j)-1]+"bytes"
						+" path length:" +pathManhatten[(i*j)-1]
						+"	nodes expanded:"+blockManhatten[(i*j)-1]);
				line.add("Agent Distance:		time:"+timeDataAgent[(i*j)-1]+"ms"
						+" memory usage:"+memAgent[(i*j)-1]+"bytes"
						+" path length:" +pathAgent[(i*j)-1]
						+"	nodes expanded:"+blockAgent[(i*j)-1]);
				line.add("Backwards AStar:		time:"+timeDataOpposite[(i*j)-1]+"ms"
						+" memory usage:"+memOpposite[(i*j)-1]+"bytes"
						+" path length:" +pathOpposite[(i*j)-1]
						+"	nodes expanded:"+blockOpposite[(i*j)-1]);
				line.add("Number of Blocked Cells:	time:"+timeDataBlock[(i*j)-1]+"ms"
						+" memory usage:"+memBlock[(i*j)-1]+"bytes"
						+" path length:" +pathBlock[(i*j)-1]
						+"	nodes expanded:"+blockBlock[(i*j)-1]);
				line.add("Sequential Heuristic AStar:	time:"+timeDataSequential[(i*j)-1]+"ms"
						+" memory usage:"+memSequential[(i*j)-1]+"bytes"
						+" path length:" +pathSequential[(i*j)-1]
						+"	nodes expanded:"+blockSequential[(i*j)-1]);
				
				time[0]=time[0]+timeDataAstar[(i*j)-1];
				time[1]=time[1]+timeDataUniform[(i*j)-1];
				time[2]=time[2]+timeDataWeighted1point5[(i*j)-1];
				time[3]=time[3]+timeDataWeighted1point25[(i*j)-1];
				time[4]=time[4]+timeDataManhatten[(i*j)-1];
				time[5]=time[5]+timeDataAgent[(i*j)-1];
				time[6]=time[6]+timeDataOpposite[(i*j)-1];
				time[7]=time[7]+timeDataBlock[(i*j)-1];
				time[8]=time[8]+timeDataSequential[(i*j)-1];
				
				mem[0]=mem[0]+memAstar[(i*j)-1];
				mem[1]=mem[1]+memUniform[(i*j)-1];
				mem[2]=mem[2]+memWeighted1point5[(i*j)-1];
				mem[3]=mem[3]+memWeighted1point25[(i*j)-1];
				mem[4]=mem[4]+memManhatten[(i*j)-1];
				mem[5]=mem[5]+memAgent[(i*j)-1];
				mem[6]=mem[6]+memOpposite[(i*j)-1];
				mem[7]=mem[7]+memBlock[(i*j)-1];
				mem[8]=mem[8]+memSequential[(i*j)-1];
				
				pathL[0]=pathL[0]+pathAstar[(i*j)-1];
				pathL[1]=pathL[0]+pathUniform[(i*j)-1];
				pathL[2]=pathL[0]+pathWeighted1point5[(i*j)-1];
				pathL[3]=pathL[0]+pathWeighted1point25[(i*j)-1];
				pathL[4]=pathL[0]+pathManhatten[(i*j)-1];
				pathL[5]=pathL[0]+pathAgent[(i*j)-1];
				pathL[6]=pathL[0]+pathOpposite[(i*j)-1];
				pathL[7]=pathL[0]+pathBlock[(i*j)-1];
				pathL[8]=pathL[0]+pathSequential[(i*j)-1];

				blockExpanded[0]=blockExpanded[0]+blockAstar[(i*j)-1];
				blockExpanded[1]=blockExpanded[1]+blockUniform[(i*j)-1];
				blockExpanded[2]=blockExpanded[2]+blockWeighted1point5[(i*j)-1];
				blockExpanded[3]=blockExpanded[3]+blockWeighted1point25[(i*j)-1];
				blockExpanded[4]=blockExpanded[4]+blockManhatten[(i*j)-1];
				blockExpanded[5]=blockExpanded[5]+blockAgent[(i*j)-1];
				blockExpanded[6]=blockExpanded[6]+blockOpposite[(i*j)-1];
				blockExpanded[7]=blockExpanded[7]+blockBlock[(i*j)-1];
				blockExpanded[8]=blockExpanded[8]+blockSequential[(i*j)-1];
			}
			avgLine.add("AStar Euclidean: time Average:"+time[0]/10+"ms");
			avgLine.add("AStar Euclidean: memory usage Average:"+mem[0]/10+"bytes");
			avgLine.add("AStar Euclidean: path length Average:"+pathL[0]/10);
			avgLine.add("AStar Euclidean: nodes expanded Average:"+blockExpanded[0]/10);
			avgLine.add("");
			avgLine.add("Uniform Cost Search: time Average:"+time[1]/10+"ms");
			avgLine.add("Uniform Cost Search: memory usage Average:"+mem[1]/10+"bytes");
			avgLine.add("Uniform Cost Search: path length Average:"+pathL[1]/10);
			avgLine.add("Uniform Cost Search: nodes expanded Average:"+blockExpanded[1]/10);
			avgLine.add("");
			avgLine.add("Weighted Astar Value(1.5): time Average:"+time[2]/10+"ms");
			avgLine.add("Weighted Astar Value(1.5): memory usage Average:"+mem[2]/10+"bytes");
			avgLine.add("Weighted Astar Value(1.5): path length Average:"+pathL[2]/10);
			avgLine.add("Weighted Astar Value(1.5): nodes expanded Average:"+blockExpanded[2]/10);
			avgLine.add("");
			avgLine.add("Weighted Astar Value(1.25): time Average:"+time[3]/10+"ms");
			avgLine.add("Weighted Astar Value(1.25): memory usage Average:"+mem[3]/10+"bytes");
			avgLine.add("Weighted Astar Value(1.25): path length Average:"+pathL[3]/10);
			avgLine.add("Weighted Astar Value(1.25): nodes expanded Average:"+blockExpanded[3]/10);
			avgLine.add("");
			avgLine.add("AStar Manhattan: time Average:"+time[4]/10+"ms");
			avgLine.add("AStar Manhattan: memory usage Average:"+mem[4]/10+"bytes");
			avgLine.add("AStar Manhattan: path length Average:"+pathL[4]/10);
			avgLine.add("AStar Manhattan: nodes expanded Average:"+blockExpanded[4]/10);
			avgLine.add("");
			avgLine.add("Agent Distance: time Average:"+time[5]/10+"ms");
			avgLine.add("Agent Distance: memory usage Average:"+mem[5]/10+"bytes");
			avgLine.add("Agent Distance: path length Average:"+pathL[5]/10);
			avgLine.add("Agent Distance: nodes expanded Average:"+blockExpanded[5]/10);
			avgLine.add("");
			avgLine.add("Backwards AStar: time Average:"+time[6]/10+"ms");
			avgLine.add("Backwards AStar: memory usage Average:"+mem[6]/10+"bytes");
			avgLine.add("Backwards AStar: path length Average:"+pathL[6]/10);
			avgLine.add("Backwards AStar: nodes expanded Average:"+blockExpanded[6]/10);
			avgLine.add("");
			avgLine.add("Number of Block Cells: time Average:"+time[7]/10+"ms");
			avgLine.add("Number of Block Cells: memory usage Average:"+mem[7]/10+"bytes");
			avgLine.add("Number of Block Cells: path length Average:"+pathL[7]/10);
			avgLine.add("Number of Block Cells: nodes expanded Average:"+blockExpanded[7]/10);
			avgLine.add("");
			avgLine.add("Sequential Heuristic AStar: time Average:"+time[8]/10+"ms");
			avgLine.add("Sequential Heuristic AStar: memory usage Average:"+mem[8]/10+"bytes");
			avgLine.add("Sequential Heuristic AStar: path length Average:"+pathL[8]/10);
			avgLine.add("Sequential Heuristic AStar: nodes expanded Average:"+blockExpanded[8]/10);
		}
		
		Path file = Paths.get("../../Test/RawData.txt");
		Path doc = Paths.get("../../Test/AvgData.txt");
		try {
			Files.write(file, line, Charset.forName("UTF-8"));
			Files.write(doc, avgLine, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}



