public class OppositeAStar extends PathSearch{
    public OppositeAStar(Grid grid,int sourceX, int sourceY,int destX,int destY)
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
    	System.out.println("Opposite AStar path length:" + pathLength);
        System.out.println("Opposite AStar Euclidean Heuristic cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + blockCounter);
    }
    
    public double heuristic(Cell goal, Cell s)
    {
        double euclidean;		
        int eucX;						
        int eucY;
		eucX=(goal.getColumn()+1)-(s.getColumn()+1);
		eucY=(goal.getRow()+1)-(s.getRow()+1);
		euclidean = Math.sqrt(Math.pow(eucX,2)+Math.pow(eucY, 2));
		return euclidean;
    }
    public void noPath()
    {
    	System.out.println("Opposite AStar No Path Found");
    }
}
