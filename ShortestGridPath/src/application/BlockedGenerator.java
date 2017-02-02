import java.util.Random;

public class BlockedGenerator {
	int rem;
	
	
	public BlockedGenerator(int total){
		rem = total/5;
	}
	
	int [][] generate(int[][] arr){
		Random rand = new Random();
		int val1; int val2;
		while (rem>0){
			val1 = rand.nextInt(arr.length);
			val2 = rand.nextInt(arr[0].length);
			if (arr[val1][val2] == 3 || arr[val1][val2] == 4){
				continue;
			}else{
				arr[val1][val2] = 0;
			}
		}
		
		
		return arr;
	}
}
