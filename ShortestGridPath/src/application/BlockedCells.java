public class BlockedCells extends PathSearch{
	boolean runOnce = true;

    public BlockedCells(Grid grid,int sourceX, int sourceY,int destX,int destY)
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
    	System.out.println("BlockedCells Heuristic path length:" + pathLength);
        System.out.println("BlockedCells Heuristic cell cost total: " + cellTotal);
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
        	for(int i=Math.min(futureX, currentY);i<=Math.max(futureX, currentX);i++)
        	{
        		for(int j=Math.min(futureY, currentY);j<=Math.max(futureY, currentY);j++)
        		{
        			if(grid.getCell(i, j).getType()==0)
        				distance++;
        		}
        	}
        	runOnce = false;
        }
        return distance;
    }
    public double seqHeuristic(Cell goal, Cell s)
    {
    	double h =0;
        int currentX = s.getColumn();
        int currentY = s.getRow();
        int futureX = s.getColumn();
        int futureY = s.getRow();
    	boolean run = true;
        if(run)
        {
        	for(int i=Math.min(futureX, currentY);i<=Math.max(futureX, currentX);i++)
        	{
        		for(int j=Math.min(futureY, currentY);j<=Math.max(futureY, currentY);j++)
        		{
        			if(grid.getCell(i, j).getType()==0)
        				h++;
        		}
        	}
        	runOnce = false;
        }
        return h;
    }
    public void noPath()
    {
    	System.out.println("Blocked Cells No Path Found");
    }
}
