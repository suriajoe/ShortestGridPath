import java.util.Comparator;

public class keyCompare implements Comparator<Cell>{
	@Override
    public int compare(Cell c1,Cell c2)
    {
		if(c1.getKey() < c2.getKey())
			return -1;
		if(c1.getKey() > c2.getKey())
			return 1;
		return 0;
    }
}
