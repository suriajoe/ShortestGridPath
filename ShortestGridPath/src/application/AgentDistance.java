public class AgentDistance extends PathSearch{
    boolean runOnce = true;
    public AgentDistance(Grid grid,int sourceX, int sourceY,int destX,int destY)
    {
    	super(grid, sourceX, sourceY, destX, destY);
    }
    
    public void pathSearch()
    {
    	boolean uniform = false;
    	int highwayHeuristic = 4;
    	double weight = 1;
        startSearch(uniform,highwayHeuristic,weight);
    }
    
    public void totalCost()
    {
    	printPath(getTotalCost(),getCellCost(),getBlockCounter(),getPathLength());
    }
    
    public void printPath(double totalCost,double cellTotal,int blockCounter,int pathLength)
    {
    	System.out.println("Agent Distance Heuristic path length:" + pathLength);
        System.out.println("Agent Distance Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + blockCounter);
    }
    
    public double heuristic(Cell goal, Cell s)
    {
        double distance = 0;		
        int currentX = s.getColumn();
        int currentY = s.getRow();
        int futureX = s.getColumn();
        int futureY = s.getRow();
        if(runOnce)
        {
        	while(currentX != futureX && currentY != futureY)
        	{
        		distance = Math.sqrt(2);
        		if(currentX>futureX)
        			currentX--;
        		else
        			currentX++;
        		if(currentY>futureY)
        			currentY--;
        		else
        			currentY++;
        	}
        	runOnce = false;
        }
		return distance;
    }
    
    public double seqHeuristic(Cell s,Cell goal)
    {     
    	double h = 0;
    	boolean run = true;
        int currentX = s.getColumn();
        int currentY = s.getRow();
        int futureX = s.getColumn();
        int futureY = s.getRow();
        if(run)
        {
        	while(currentX != futureX && currentY != futureY)
        	{
        		h = Math.sqrt(2);
        		if(currentX>futureX)
        			currentX--;
        		else
        			currentX++;
        		if(currentY>futureY)
        			currentY--;
        		else
        			currentY++;
        	}
        	run = false;
        }
		
		return h;
    }
    public void noPath()
    {
    	System.out.println("Agent Distance No Path Found");
    }
}
