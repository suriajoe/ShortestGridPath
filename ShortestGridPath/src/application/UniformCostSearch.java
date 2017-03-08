
public class UniformCostSearch extends PathSearch{
    public UniformCostSearch(Grid grid,int sourceX, int sourceY,int destX,int destY)
    {
    	super(grid, sourceX, sourceY, destX, destY);
    }
    
    public void pathSearch()
    {
    	boolean uniform = true;
    	int highwayH = 1;
    	double weight = 1;
        startSearch(uniform,highwayH,weight);
    }
    
    public void totalCost()
    {
    	printPath(getTotalCost(),getCellCost(),getBlockCounter(),getPathLength());
    }
    
    public void printPath(double totalCost,double cellTotal,int blockCounter,int pathLength)
    {
    	System.out.println("Uniform path length:" + pathLength);
        System.out.println("Uniform Search cell cost total: " + cellTotal);
        System.out.println("Number of cells traversed: " + blockCounter);
    }
    
    public double heuristic(Cell goal, Cell s)
    {
    	return 0;
    }
    public void noPath()
    {
    	System.out.println("Uniform Cost Search No Path Found");
    }
}
