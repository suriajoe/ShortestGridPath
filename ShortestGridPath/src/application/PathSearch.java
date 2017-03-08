import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class PathSearch {
	Comparator<Cell> comparator = new CellCompare();
    PriorityQueue<Cell> path = new PriorityQueue<Cell>(1000,comparator);
    boolean closed[][] = new boolean[120][160];
    
    Grid grid;
    int sourceX;
    int sourceY;
    int destX;
    int destY;
    int blockCounter;
    int pathLen;
    double total;
    double cell;
    
    public PathSearch(Grid grid,int sourceX, int sourceY,int destX,int destY)
    {
    	this.grid = grid;
    	this.sourceX = sourceX;
    	this.sourceY = sourceY;
    	this.destX = destX;
    	this.destY = destY;
    }
    public void clean()
    {
        grid.hoverUnhighlight(); 	//cleans up gui
    }
    
    public void startSearch(boolean uni,int highwayH,double weight)
    {
        double costVH = 1;
        double costD = Math.sqrt(2);
        double roughCostVH = 2;
        double roughCostD = Math.sqrt(8);
        double emptyRoughCostVH = 1.5;
        double emptyRoughCostD = ((Math.sqrt(2)+Math.sqrt(8))/2);
        double costVHWay = 0.25;
        double roughVHWay = 0.5;
        double emptyRoughVHWay = 0.375;
        double euclideanDistance = 0;		//heuristic
        blockCounter = 0;
        double weightValue = weight;
        int highwayHeuristic = highwayH;
        boolean pathFound = false;
        boolean uniform = uni;
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        Cell sPrime;
        int currentX, currentY;//present cell   
        s.setG(0);
        s.setH(heuristic(goal,s));
        s.setF();
        path.add(s);			//priority queue
        
        while(!path.isEmpty())
        {
        	s=path.poll();

        	if(s == goal)
        	{    
            	s.hoverUnhighlight();
        		//System.out.println("path found");
        		pathFound = true;
        		break;
        	}
        	closed[s.getRow()][s.getColumn()] = true;
            currentX = s.getColumn();
            currentY = s.getRow();
            
            //pos 4
            if(currentX-1>=0){
            	sPrime = grid.getCell(currentX-1, currentY);
            	if(uniform == false)
            	{
            		euclideanDistance = heuristic(goal,s)*weightValue;
            	}
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+roughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+emptyRoughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getG()+costVHWay,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+roughVHWay,euclideanDistance/highwayHeuristic,closed,path);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+emptyRoughVHWay,euclideanDistance/highwayHeuristic,closed,path);          		
            	else
            		updateVertex(s,sPrime,s.getG()+costVH,euclideanDistance/highwayHeuristic,closed,path);
            	//pos 7
                if(currentY-1>=0){                      
                	sPrime = grid.getCell(currentX-1, currentY-1);
                	if(uniform == false)
                	{
                		euclideanDistance = heuristic(goal,s)*weightValue;
                	}
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+roughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+emptyRoughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else
                		updateVertex(s,sPrime,s.getG()+costD,euclideanDistance/highwayHeuristic,closed,path); 
                }
                //pos 1
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX-1, currentY+1);
                	if(uniform == false)
                	{
                		euclideanDistance = heuristic(goal,s)*weightValue;
                	}
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+roughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+emptyRoughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else
                		updateVertex(s,sPrime,s.getG()+costD,euclideanDistance/highwayHeuristic,closed,path);
                }
            } 
            //pos 2
            if(currentY-1>=0){
            	sPrime = grid.getCell(currentX, currentY-1);
            	if(uniform == false)
            	{
            		euclideanDistance = heuristic(goal,s)*weightValue;
            	}
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+roughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+emptyRoughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getG()+costVHWay,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+roughVHWay,euclideanDistance/highwayHeuristic,closed,path);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+emptyRoughVHWay,euclideanDistance/highwayHeuristic,closed,path); 
            	else
            		updateVertex(s,sPrime,s.getG()+costVH,euclideanDistance/highwayHeuristic,closed,path);
            }
            //pos 8
            if(currentY+1<120){
            	sPrime = grid.getCell(currentX, currentY+1);
            	if(uniform == false)
            	{
            		euclideanDistance = heuristic(goal,s)*weightValue;
            	}
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+roughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+emptyRoughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getG()+costVHWay,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+roughVHWay,euclideanDistance/highwayHeuristic,closed,path);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+emptyRoughVHWay,euclideanDistance/highwayHeuristic,closed,path); 
            	else
            		updateVertex(s,sPrime,s.getG()+costVH,euclideanDistance/highwayHeuristic,closed,path); 
            }
            //pos 6
            if(currentX+1<160){
            	sPrime = grid.getCell(currentX+1, currentY);
            	if(uniform == false)
            	{
            		euclideanDistance = heuristic(goal,s)*weightValue;
            	}
            	if(s.getType() == 2 && sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+roughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 2 || sPrime.getType() == 2)
            		updateVertex(s,sPrime,s.getG()+emptyRoughCostVH,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 3 && sPrime.getType() == 3) 
            		updateVertex(s,sPrime,s.getG()+costVHWay,euclideanDistance/highwayHeuristic,closed,path);
            	else if(s.getType() == 4 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+roughVHWay,euclideanDistance/highwayHeuristic,closed,path);          		
            	else if(s.getType() == 4 && sPrime.getType() == 3 || s.getType() == 3 && sPrime.getType() == 4)
            		updateVertex(s,sPrime,s.getG()+emptyRoughVHWay,euclideanDistance/highwayHeuristic,closed,path); 
            	else
            		updateVertex(s,sPrime,s.getG()+costVH,euclideanDistance/highwayHeuristic,closed,path); 
            	//pos 9
                if(currentY-1>=0){
                	sPrime = grid.getCell(currentX+1, currentY-1);
                	if(uniform == false)
                	{
                		euclideanDistance = heuristic(goal,s)*weightValue;
                	}
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+roughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+emptyRoughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else
                		updateVertex(s,sPrime,s.getG()+costD,euclideanDistance/highwayHeuristic,closed,path); 
                }
                //pos 3
                if(currentY+1<120){
                	sPrime = grid.getCell(currentX+1, currentY+1);
                	if(uniform == false)
                	{
                		euclideanDistance = heuristic(goal,s)*weightValue;
                	}
                	if(s.getType() == 2 && sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+roughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else if(s.getType() == 2 || sPrime.getType() == 2)
                		updateVertex(s,sPrime,s.getG()+emptyRoughCostD,euclideanDistance/highwayHeuristic,closed,path);
                	else
                		updateVertex(s,sPrime,s.getG()+costD,euclideanDistance/highwayHeuristic,closed,path); 
                }  
            }
        }
        if(s != goal)
        {
        	noPath();
        }
        setBlockCounter(blockCounter);
		totalCost(pathFound,sourceX,sourceY,destX,destY);
    }
    
    public void updateVertex(Cell s, Cell sPrime, double cost, double heuristicCost,boolean[][] closed,PriorityQueue<Cell> fringe)
    {
		//closed[120][160]
		//cost = g(s)+c(s,s')
		if(sPrime == null || sPrime.getType() == 0 || closed[sPrime.getRow()][sPrime.getColumn()]== true)
			return;
		//h(s)+g(s)+c(s,s')
		//double fCost = heuristicCost+cost;
		//g(s') > g(s)+c(s,s')
		if(!fringe.contains(sPrime) || sPrime.getG()>cost)
		{
			if(fringe.contains(sPrime))
			{
				fringe.remove(sPrime);
			}
			sPrime.setH(heuristicCost);
			sPrime.setG(cost);
			sPrime.setF();
			fringe.add(sPrime);
			blockCounter++;
			sPrime.cell=s;			
			closed[sPrime.getRow()][sPrime.getColumn()] = true;
		}
    }
    
    
    public void totalCost(boolean pathFound,int sourceX,int sourceY,int destX,int destY)
    {
        double totalCost = 0;
        double cellTotal = 0;
        int pathLength = 0;
        Cell goal = grid.getCell(destX, destY);
        Cell s = grid.getCell(sourceX, sourceY);
        if(pathFound == true)
        {
        	pathLength++;
        	//draw out the path
        	grid.pathIsColored.add(s);		//add initial cell
        	while(goal.cell != null)
        	{	
        		grid.pathIsColored.add(goal.cell); //add remaining cells
        		goal.hoverHighlight();
        		totalCost = totalCost + goal.getF();
        		cellTotal = cellTotal + goal.getValue();
        		goal=goal.cell;
        		pathLength++;
        	}
        }	
        grid.getCell(sourceX, sourceY).hoverUnhighlight();
        grid.getCell(destX, destY).hoverUnhighlight();
        
        setTotalCost(totalCost);
        setCellCost(cellTotal);
        setPathLength(pathLength);
    }
    
    public void setTotalCost(double totalCost)
    {
    	total = totalCost;
    }
    public double getTotalCost()
    {
    	return total;
    }
    public void setCellCost(double cellTotal)
    {
    	cell = cellTotal;
    }
    public double getCellCost()
    {
    	return cell;
    }
    public void setBlockCounter(int cellCounter)
    {
    	blockCounter = cellCounter;
    }
    public int getBlockCounter()
    {
    	return blockCounter;
    }
    public void setPathLength(int pathLength)
    {
    	pathLen = pathLength;
    }
    public int getPathLength()
    {
    	return pathLen;
    }
    
    public abstract void noPath();
    public abstract double heuristic(Cell goal, Cell s);
    
}
