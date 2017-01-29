import java.util.Random;

public class HighwayConstructor {
	public int [][] array;
	int p1;
	int p2;
	int dir;
	/* dir: direction of the highway's expansion
	 * dir 0 = down
	 * dir 1 = up
	 * dir 2 = right
	 * dir 3 = left
	 */
	boolean found;
	boolean rejected;
	
	public HighwayConstructor(int[][] array){
		p1 = 0;
		p2 = 0;
		dir = 0;
		found = false;
		rejected = false;
	}
	
	public int[][] construct(int[][] array){//calls all the methods written below to make the highway
		Stack path = new Stack();
		for(int i=0; i<4; i++){
			found = false;
			array = start();
			while(!found){
				array = straightPath(path);
				changeDirection();
				if(rejected){
					break;
				}
			}
			if(rejected){
				i--;
				rejected = false;
			}
		}
		
		return array;
	}
	
	private int[][] start(){
		Random rand = new Random();
		int val; int val2;
		//insertion of highways, codes 3 and 4
		//need a loop here
		int p1=0; int p2=0;//p1 = row position, p2 = column position
		Stack path = new Stack();//the current path
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
		array = straightPath(path);
		return array;
	}
		/*
		 * 20% should be marked 0 (Impassable Terrain)
		 */
		
	
	private void changeDirection(){
		Random rand = new Random();
		int val = rand.nextInt(100);
		if(val<60){//continue
			return;
		}else if(val<80){//right turn
			switch(dir){
				case(0): dir = 3; break;
				case(1): dir = 2; break;
				case(2): dir = 0; break;
				case(3): dir = 1; break;
			}
		}else{//left turn
			
		}
	}
	
	
	private void rejectPath(Stack s){
		rejected = true;
		while(!s.isEmpty()){
			Node n = s.pop();
			if(this.array[n.p1][n.p2] == 3){
				this.array[n.p1][n.p2] = 1;
			}else{
				this.array[n.p1][n.p2] = 2;
			}
		}
	}
	
	private int[][] straightPath(Stack path){
		int p1=0; int p2=0;
		for(int x = 0; x < 1; x++){//change constant depending on how many tiles u wanna go
			switch(dir){
				case(0):{
					//check for boundary
					p1++;
					if (array[p1][p2] == 3 || array[p1][p2] == 4){//check if the tile is already a highway
						rejectPath(path);//implement this before testing
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						found = true;
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
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						found = true;
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
						break;
					}
					if(p1>array.length||p1<0 || p2>array[0].length||p2<0){//found a boundary
						found = true;
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
			if (found||rejected){//if a boundary has been hit
				break;
			}
		}
		return array;
	}
}
