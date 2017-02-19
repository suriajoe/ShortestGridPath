public class Manhatten extends PathSearch{
    public Manhatten(Grid grid,int sourceX, int sourceY,int destX,int destY)
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
    	System.out.println("AStar Manhatten Heurisitc path length:" + pathLength);
        System.out.println("AStar Manhatten Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + blockCounter);
    }
    
    public double heuristic(Cell goal, Cell s)
    {
        double euclidean;		
        int eucX;						
        int eucY;
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		euclidean = Math.abs(eucX)+Math.abs(eucY);
		return euclidean;
    }
    
    public double seqHeuristic(Cell s,Cell goal)
    {
    	//s = grid.getCell(grid.getStartX(), grid.getStartY());
    	//goal = grid.getCell(grid.getEndX(), grid.getEndY());
        int eucX;						
        int eucY;
        double h = 0;
        
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		h = Math.abs(eucX)+Math.abs(eucY);
		
		return h;
    }
    public void noPath()
    {
    	System.out.println("Manhatten No Path Found");
    }
}
