import java.util.Random;

public class TerrainProbability_v2 {
	public void createGrid(){
		int[][] array = new int[10][10];
		for (int i=0; i<array.length; i++){
			for (int j=0; j<array[i].length; j++){
				array[i][j] = 1;//all tiles default marked 1 (normal terrain)
			}
			
		}
		/*
		 * 20% should be marked 0 (Impassable Terrain)
		 */
		Random rand = new Random();
		int val; int val2;
		//insertion of highways, codes 3 and 4
		//need a loop here
		int p1=0; int p2=0;//p1 = row position, p2 = column position
		Stack path = new Stack();//the current path
		int dir;//direction the highway is currently expanding
		dir = rand.nextInt(4);
		switch(dir){//determines start position of highway
			case(0):{//start on top row, direction 0 = down
				val2 = rand.nextInt(array[0].length);
				p1 = val2;
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path.push(new Node(p1,p2,null));
				break;
			}
			case(1):{//start bottom row, direction 1 = up
				p2 = array.length-1;
				val2 = rand.nextInt(array[0].length);
				p1 = val2;
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path.push(new Node(p1,p2,null));
				break;
			}
			case(2):{//start on left column, direction 2 = right
				val2 = rand.nextInt(array.length);
				p2 = val2;
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path.push(new Node(p1,p2,null));
				break;
			}
			case(3):{//start on right column, direction 3 = left
				p1 = array[0].length-1;
				val2 = rand.nextInt(array.length);
				p2 = val2;
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path.push(new Node(p1,p2,null));
				break;
			}
		}
		array = straightPath(array, dir, path);
		/* confirm using path.peekInfo to find the current location of the highway
		 * change values of p1 and p2
		 */
		//probability of direction
		val = rand.nextInt(100);
		if(val<60){//continue
			
		}else if(val<80){//right turn
			
		}else{//left turn
			
		}
			
	}
	
	public static void rejectPath(Stack s){
		
		//reconvert terrain types in cells
		
	}
	
	public static int[][] straightPath(int[][]array, int dir, Stack path){
		int p1=0; int p2=0;
		boolean found = false;
		for(int x = 0; x < 1; x++){//change constant depending on how many tiles u wanna go
			switch(dir){
				case(0):{
					//check for boundary
					p1++;
					if (array[p1][p2] == 3 || array[p1][p2] == 4){//check if the tile is already a highway
						rejectPath(path);//implement this before testing
						found = true;
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						break;
					}
					if (array[p1][p2] == 1){//convert to highway and push to stack
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					break;
				}
				case(1):{
					p1--;
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(path);
						found = true;
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path.push(new Node(p1,p2,null));
					break;
				}
				case(2):{
					p2++;
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(path);
						found = true;
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path.push(new Node(p1,p2,null));
					break;
				}
				case(3):{
					p2--;
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(path);
						found = true;
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path.push(new Node(p1,p2,null));
					break;
				}
			}
			if (found){//if a boundary has been hit
				break;
			}
		}
		return array;
	}
}
