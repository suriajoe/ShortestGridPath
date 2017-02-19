import java.util.Comparator;

public class CellCompare implements Comparator<Cell>{
	
	@Override
    public int compare(Cell c1,Cell c2)
    {
		if(c1.getF() < c2.getF())
			return -1;
		if(c1.getF() > c2.getF())
			return 1;
		
		if(c1.getF() == c2.getF())
		{			
			if(c1.getH() < c2.getH())
				return -1;
			if(c1.getH() > c2.getH())
				return 1;
		}
		
		return 0;
    }
}
