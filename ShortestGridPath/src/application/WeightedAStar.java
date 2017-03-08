
public class WeightedAStar extends PathSearch{
    public WeightedAStar(Grid grid,int sourceX, int sourceY,int destX,int destY)
    {
    	super(grid, sourceX, sourceY, destX, destY);
    }
    
    public void pathSearch(double weight)
    {
    	boolean uniform = false;
    	int highwayHeuristic = 4;
        startSearch(uniform,highwayHeuristic,weight);
    }
    
    public void totalCost()
    {
    	printPath(getTotalCost(),getCellCost(),getBlockCounter(),getPathLength());
    }
    
    public void printPath(double totalCost,double cellTotal,int blockCounter,int pathLength)
    {
        System.out.println("Weighted AStar path length: " + pathLength);
        System.out.println("Weighted cell cost total: " + cellTotal);
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
    public void noPath()
    {
    	System.out.println("Weight AStar No Path Found");
    }
}
