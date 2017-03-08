public class AStar extends PathSearch{
	
    public AStar(Grid grid,int sourceX, int sourceY,int destX,int destY)
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
    	System.out.println("AStar Euclidean Heuristic path length:" + pathLength);
        System.out.println("AStar Euclidean Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + blockCounter);
    }
    
    public double heuristic(Cell goal, Cell s)
    {
        double euclidean;		
        int eucX;						
        int eucY;
		eucX=(s.getColumn()+1)-(goal.getColumn()+1);
		eucY=(s.getRow()+1)-(goal.getRow()+1);
		euclidean = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
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
		h = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
		
		return h;
    }
    
    /*diagonal distance approximation
    public double heuristic(Cell goal, Cell s)
    {
    	double euclidean;
    	int eucX;
    	int eucY;
    	double diagCost = 1.41421356;
    	eucX = Math.abs((s.getColumn()+1)-(goal.getColumn()+1));
    	eucY = Math.abs((s.getRow()+1)-(goal.getRow()+1));
    	euclidean = (eucX+eucY)+(diagCost-2*1)*Math.min(eucX, eucY);
    	return euclidean;

    }
    */
    public void noPath()
    {
    	System.out.println("AStar No Path Found");
    }
}
