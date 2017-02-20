import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class SequentialHeuristic {
	static int numOfHeuristics = 4;
	Comparator<Cell> keyComparator = new keyCompare();	
	LinkedList<LinkedList<Cell>> list = new LinkedList<LinkedList<Cell>>();
	LinkedList<PriorityQueue<Cell>> open = new LinkedList<PriorityQueue<Cell>>();
	//PriorityQueue<Cell>[] open = (PriorityQueue<Cell>[]) new PriorityQueue[numOfHeuristics];
    boolean closed[][][] = new boolean[numOfHeuristics][120][160];
	int w1 = 1;//>1
	int w2 = 1;//>1
	boolean pathFound[] = new boolean[numOfHeuristics];
    int blockCounter;
    double total;
    double cell;
    int pathLength;
	
    Grid grid;
    int sourceX;
    int sourceY;
    int destX;
    int destY;
	
	public SequentialHeuristic(Grid grid,int sourceX,int sourceY,int destX,int destY)
	{
    	this.grid = grid;
    	this.sourceX = sourceX;
    	this.sourceY = sourceY;
    	this.destX = destX;
    	this.destY = destY;
	}
	
	//return min key value, if queue is empty return infinity
	public double minKey(PriorityQueue<Cell> openKey,double hValue)
	{
		double k = 0;
		Cell s;
		if(openKey.peek() == null)//list is empty, key is infinite
		{
			k = 2147483647;
			k = k + 1;
		}
		else
		{
			s = openKey.peek();
			k = key(s,1,hValue);
			s.setKey(k);
			openKey.poll();
			openKey.add(s);
		}
		//openKey.add(s);
		return k;
	}
	
	public double key(Cell s, int i, double hValue)
	{
		//compute once instance of heuristic value, add with G to return key value
		if(hValue != -1)
			s.setH(hValue);
		return s.getG() + w1*s.getH();
	}
	
	public void expandState(Cell s, int i,Cell goal)
	{
		//0
		AStar star = new AStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		//1
		Manhatten ny = new Manhatten(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		//2
		AgentDistance agent = new AgentDistance(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		//3
		BlockedCells block = new BlockedCells(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		double hValue = 0;
		
		//search the cells neighbor
        int currentX = s.getColumn();
        int currentY = s.getRow();
		open.get(i).remove(s);
        Cell sPrime;
        
        //pos 4
        if(currentX-1>=0)
        {
        	sPrime = grid.getCell(currentX-1, currentY);
			if(sPrime.getValue() != 0 && s.getValue() != 0)
			{
				if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
				{
					sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
					sPrime.cell = s;
					if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
					{
						if(i == 0)//Astar heuristic
						{
							hValue = star.seqHeuristic(s,goal);
						}
						else if(i==1)
						{
							hValue = ny.seqHeuristic(s,goal);
						}
						else if(i==2)
						{
							hValue = agent.seqHeuristic(s,goal);
						}
						else if(i==3)
						{
							hValue = block.seqHeuristic(s, goal);
						}
						sPrime.key = key(sPrime,i,hValue);
						open.get(i).add(sPrime);
						blockCounter++;
					}
				}
			}
        	//pos 7
            if(currentY-1>=0){                      
            	sPrime = grid.getCell(currentX-1, currentY-1);
				if(sPrime.getValue() != 0 && s.getValue() != 0)
				{
					if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
					{
						sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
						sPrime.cell = s;
						if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
						{
							if(i == 0)//Astar heuristic
							{
								hValue = star.seqHeuristic(s,goal);
							}
							else if(i==1)
							{
								hValue = ny.seqHeuristic(s,goal);
							}
							else if(i==2)
							{
								hValue = agent.seqHeuristic(s,goal);
							}
							else if(i==3)
							{
								hValue = block.seqHeuristic(s, goal);
							}
							sPrime.key = key(sPrime,i,hValue);
							open.get(i).add(sPrime);
							blockCounter++;
						}
					}
				}
            }
            //pos 1
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX-1, currentY+1);
				if(sPrime.getValue() != 0 && s.getValue() != 0)
				{
					if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
					{
						sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
						sPrime.cell = s;
						if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
						{
							if(i == 0)//Astar heuristic
							{
								hValue = star.seqHeuristic(s,goal);
							}
							else if(i==1)
							{
								hValue = ny.seqHeuristic(s,goal);
							}
							else if(i==2)
							{
								hValue = agent.seqHeuristic(s,goal);
							}
							else if(i==3)
							{
								hValue = block.seqHeuristic(s, goal);
							}
							sPrime.key = key(sPrime,i,hValue);
							open.get(i).add(sPrime);
							blockCounter++;
						}
					}
				}
            }
        }
        //pos 2
        if(currentY-1>=0){
        	sPrime = grid.getCell(currentX, currentY-1);
			if(sPrime.getValue() != 0 && s.getValue() != 0)
			{
				if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
				{
					sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
					sPrime.cell = s;
					if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
					{
						if(i == 0)//Astar heuristic
						{
							hValue = star.seqHeuristic(s,goal);
						}
						else if(i==1)
						{
							hValue = ny.seqHeuristic(s,goal);
						}
						else if(i==2)
						{
							hValue = agent.seqHeuristic(s,goal);
						}
						else if(i==3)
						{
							hValue = block.seqHeuristic(s, goal);
						}
						sPrime.key = key(sPrime,i,hValue);
						open.get(i).add(sPrime);
						blockCounter++;
					}
				}
			}
        }
        //pos 8
        if(currentY+1<120){
        	sPrime = grid.getCell(currentX, currentY+1);
			if(sPrime.getValue() != 0 && s.getValue() != 0)
			{
				if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
				{
					sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
					sPrime.cell = s;
					if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
					{
						if(i == 0)//Astar heuristic
						{
							hValue = star.seqHeuristic(s,goal);
						}
						else if(i==1)
						{
							hValue = ny.seqHeuristic(s,goal);
						}
						else if(i==2)
						{
							hValue = agent.seqHeuristic(s,goal);
						}
						else if(i==3)
						{
							hValue = block.seqHeuristic(s, goal);
						}
						sPrime.key = key(sPrime,i,hValue);
						open.get(i).add(sPrime);
						blockCounter++;
					}
				}
			}
        }
        //pos 6
        if(currentX+1<160){
        	sPrime = grid.getCell(currentX+1, currentY);
			if(sPrime.getValue() != 0 && s.getValue() != 0)
			{
				if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
				{
					sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
					sPrime.cell = s;
					if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
					{
						if(i == 0)//Astar heuristic
						{
							hValue = star.seqHeuristic(s,goal);
						}
						else if(i==1)
						{
							hValue = ny.seqHeuristic(s,goal);
						}
						else if(i==2)
						{
							hValue = agent.seqHeuristic(s,goal);
						}
						else if(i==3)
						{
							hValue = block.seqHeuristic(s, goal);
						}
						sPrime.key = key(sPrime,i,hValue);
						open.get(i).add(sPrime);
						blockCounter++;
					}
				}
			}
        	//pos 9
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX+1, currentY-1);
				if(sPrime.getValue() != 0 && s.getValue() != 0)
				{
					if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
					{
						sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
						sPrime.cell = s;
						if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
						{
							if(i == 0)//Astar heuristic
							{
								hValue = star.seqHeuristic(s,goal);
							}
							else if(i==1)
							{
								hValue = ny.seqHeuristic(s,goal);
							}
							else if(i==2)
							{
								hValue = agent.seqHeuristic(s,goal);
							}
							else if(i==3)
							{
								hValue = block.seqHeuristic(s, goal);
							}
							sPrime.key = key(sPrime,i,hValue);
							open.get(i).add(sPrime);
							blockCounter++;
						}
					} 
				}
            }
            //pos 3
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX+1, currentY+1);
				if(sPrime.getValue() != 0 && s.getValue() != 0)
				{
					if(sPrime.getG() > (s.getG()+(s.getValue()+sPrime.getValue()))) 
					{
						sPrime.setG(s.getG()+(s.getValue()+sPrime.getValue()));
						sPrime.cell = s;
						if(closed[i][sPrime.getRow()][sPrime.getColumn()] == false)
						{
							if(i == 0)//Astar heuristic
							{
								hValue = star.seqHeuristic(s,goal);
							}
							else if(i==1)
							{
								hValue = ny.seqHeuristic(s,goal);
							}
							else if(i==2)
							{
								hValue = agent.seqHeuristic(s,goal);
							}
							else if(i==3)
							{
								hValue = block.seqHeuristic(s, goal);
							}
							sPrime.key = key(sPrime,i,hValue);
							open.get(i).add(sPrime);
							blockCounter++;
						}
					}
				}  
            }
        }
	}
	public void clean()
	{
        grid.hoverUnhighlight(); 	//cleans up gui
	}
	public void mainSequence()
	{
		//weightedValue = weight;
		int n = numOfHeuristics; //num of heuristics used
		Cell s = null;
		Cell goal[] = new Cell[numOfHeuristics];
		int heuristicNum = 0;
		double hValue = 0;
		boolean forloopFinished = false;
		//0
		AStar star = new AStar(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		//1
		Manhatten ny = new Manhatten(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		//2
		AgentDistance agent = new AgentDistance(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		//3
		BlockedCells block = new BlockedCells(grid,grid.getStartX(),grid.getStartY(),grid.getEndX(),grid.getEndY());
		
		for(int i=0;i<n;i++)
		{
			PriorityQueue<Cell> queue = new PriorityQueue<Cell>(19200,keyComparator);
			open.add(queue);
			//open[i] = null;
			s = grid.getCell(sourceX, sourceY);
			goal[i] = grid.getCell(destX, destY);	
			s.setG(0);
			if(i == 0)//Astar heuristic
			{
				hValue = star.seqHeuristic(s,goal[i]);
			}
			else if(i==1)
			{
				hValue = ny.seqHeuristic(s,goal[i]);
			}
			else if(i==2)
			{
				hValue = agent.seqHeuristic(s,goal[i]);
			}
			else if(i==3)
			{
				hValue = block.seqHeuristic(s, goal[i]);
			}
				
			s.setKey(key(s,i,hValue));//open minKey starts at zero
			open.get(i).add(s);
			pathFound[i] = false;
		}
		//hValueAdmissable = star.seqHeuristic(s, goal[0]);
		while(minKey(open.get(0),-1) <= 2147483647) //terminate when Open0.key >= infinity
		{
			for(int i=0;i<n;i++)
			{			
				if(i == 0)//Astar heuristic
				{
					hValue = star.seqHeuristic(s,goal[i]);
				}
				else if(i==1)
				{
					hValue = ny.seqHeuristic(s,goal[i]);
				}
				else if(i==2)
				{
					hValue = agent.seqHeuristic(s,goal[i]);
				}
				else if(i==3)
				{
					hValue = block.seqHeuristic(s, goal[i]);
				}
				
				if(minKey(open.get(i),-1) <= w2*minKey(open.get(0),-1))
				{
					if(goal[i].getG() <= minKey(open.get(i),-1))								//both condition are 2E9(infinite)
					{
						if(goal[i].getG() <= 2147483647)
						{
							heuristicNum = i;
							pathFound[i] = true;
							forloopFinished = true;
							break;
						}
					}
					else
					{
							s = open.get(i).poll();	//Top() 			
							expandState(s,i,goal[i]);
							closed[i][s.getRow()][s.getColumn()] = true;
					}
				  }
				  else
				  {
					 if(goal[0].getG() <= minKey(open.get(0),-1))
					 {
						if(goal[0].getG() < 2147483647)
						{
							heuristicNum = 0;
							pathFound[0] = true;
							forloopFinished = true;
							i=n;
						}
					 }
					 else
					 {
						s = open.get(0).poll(); //Top() 
						expandState(s,0,goal[0]);
						closed[0][s.getRow()][s.getColumn()] = true;
					 }
					}
				}
			if(forloopFinished == true)
			{
				break;
			}
		}
		if(pathFound[heuristicNum] == true)
			totalCost(s,goal[heuristicNum],sourceX,sourceY,destX,destY);
		else
			System.out.println("Sequential No Path Found");
	}
	
    public void totalCost(Cell s, Cell goal,int sourceX,int sourceY,int destX,int destY)
    {
    	//System.out.println("path found");
        double totalCost = 0;
        double cellTotal = 0;
        grid.pathIsColored.add(s);		//add intital cell
        pathLength = 0;
		pathLength++;
        while(goal.cell != null)
        {	
        	pathLength++;
        	grid.pathIsColored.add(goal.cell); //add remaning cells
        	goal.hoverHighlight();
        	totalCost = totalCost + goal.getF();
        	cellTotal = cellTotal + goal.getValue();
        	goal=goal.cell;		
        }
        	
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
        
        setTotalCost(totalCost);
        setCellCost(cellTotal);
        setPathLength(pathLength);
        setBlockCounter(blockCounter);
    	//printPath(getTotalCost(),getCellCost(),getBlockCounter(),getPathLength());
    }
    
    public void printPath()
    {
    	System.out.println("Sequential Heuristic path length:" + getPathLength());
        System.out.println("Sequential Heuristic cell cost total: " + getCellCost());
        System.out.println("Number of cells traversed: " + getBlockCounter());
    }
    public void setTotalCost(double totalCost)
    {
    	this.total = totalCost;
    }
    public double getTotalCost()
    {
    	return this.total;
    }
    public void setCellCost(double cellTotal)
    {
    	this.cell = cellTotal;
    }
    public double getCellCost()
    {
    	return this.cell;
    }
    public void setBlockCounter(int cellCounter)
    {
    	blockCounter = cellCounter;
    }
    public int getBlockCounter()
    {
    	return this.blockCounter;
    }
    public void setPathLength(int pathLength)
    {
    	this.pathLength = pathLength;
    }
    public int getPathLength()
    {
    	return this.pathLength;
    }
}
