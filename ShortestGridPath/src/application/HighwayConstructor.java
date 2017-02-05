import java.util.Random;

public class HighwayConstructor {
	int i;
	int p1;
	int p2;
	int dir;
	/* dir: direction of the highway's expansion
	 * dir 0 = down
	 * dir 1 = up
	 * dir 2 = right
	 * dir 3 = left
	 */
	//Stack path;
	boolean found;
	boolean rejected;
	boolean started;
	int attempts;
	Stack[] path;
	
	public HighwayConstructor(int[][] array){
		p1 = 0;
		p2 = 0;
		dir = 0;
		found = false;
		rejected = false;
		path = new Stack[4];
		started = false;
		attempts = 0;
	}
	
	public int[][] construct(int[][] array){//calls all the methods written below to make the highway
		for(i=0; i<4; i++){
			if (attempts > 10){
				for(i=0; i<4; i++){
					rejectPath(array);
				}
				i=-1;
				continue;
			}
			path[i] = new Stack();
			found = false;
			do{
				array = start(array);
			}while(rejected);
			while(!found){
				array = straightPath(array);
				changeDirection();
				if(rejected){
					break;
				}
				if(p1>=array.length-1||p1<=0 || p2>=array[0].length-1||p2<=0){
					found = true;
				}
			}
			if (found && path[i].getSize() < 100){//found a boundary but it ain't long enough
				rejectPath(array);
			}
			if(found){
				attempts = 0;
			}
			//path[i].clear();
			if(rejected){
				i--;
				rejected = false;
			}
		}
		
		return array;
	}
	
	private int[][] start(int[][] array){
		Random rand = new Random();
		p1=0;
		p2=0;
		//insertion of highways, codes 3 and 4
		//need a loop here
		//p1 = row position, p2 = column position
		dir = rand.nextInt(4);
		switch(dir){//determines start position of highway
			case(0):{//start on top row, direction 0 = down
				//top row
				p2 = rand.nextInt(array[0].length-2)+1;//random column
				if(array[p1][p2] == 3 || array[p1][p2] == 4){
					rejected = true;
					return array;
				}
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path[i].push(new Node(p1,p2,null));
				break;
			}
			case(1):{//start bottom row, direction 1 = up
				p1 = array.length-1;//bottom row
				p2 = rand.nextInt(array[0].length-2)+1;
				if(array[p1][p2] == 3 || array[p1][p2] == 4){
					rejected = true;
					return array;
				}
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path[i].push(new Node(p1,p2,null));
				break;
			}
			case(2):{//start on left column, direction 2 = right
				p1 = rand.nextInt(array.length-2)+1;
				if(array[p1][p2] == 3 || array[p1][p2] == 4){
					rejected = true;
					return array;
				}
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path[i].push(new Node(p1,p2,null));
				break;
			}
			case(3):{//start on right column, direction 3 = left
				p2 = array[0].length-1;
				p1 = rand.nextInt(array.length-2)+1;
				if(array[p1][p2] == 3 || array[p1][p2] == 4){
					rejected = true;
					return array;
				}
				if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				}else{
					array[p1][p2] = 4;
				}
				path[i].push(new Node(p1,p2,null));
				break;
			}
		}
		rejected = false;
		started = true;
		return array;
	}
		
	
	private void changeDirection(){
		Random rand = new Random();
		int val = rand.nextInt(100);
		if(val<60){//continue
			return;
		}else if(val>80){//right turn
			switch(dir){
				case(0): dir = 3; break;
				case(1): dir = 2; break;
				case(2): dir = 0; break;
				case(3): dir = 1; break;
			}
		}else{//left turn
			switch(dir){
			case(0): dir = 2; break;
			case(1): dir = 3; break;
			case(2): dir = 1; break;
			case(3): dir = 0; break;
			}
		}
	}
	
	
	private int[][] rejectPath(int[][] array){
		rejected = true;
		attempts++;
		while(!path[i].isEmpty()){
			Node n = path[i].pop();
			if(array[n.p1][n.p2] == 3){
				array[n.p1][n.p2] = 1;
			}else{
				array[n.p1][n.p2] = 2;
			}
		}
		return array;
	}
	
	private int[][] straightPath(int[][] array){
		for(int x = 0; x < 20; x++){//change constant depending on how many tiles u wanna go
			if (started){
				continue;
			}
			switch(dir){
				case(0):{
					//check for boundary
					p1++;
					if(p1>array.length-1||p1<0 || p2>array[0].length-1||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){//check if the tile is already a highway
						rejectPath(array);
						break;
					}
					
					if (array[p1][p2] == 1){//convert to highway and push to stack
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path[i].push(new Node(p1,p2,null));
					break;
				}
				case(1):{
					p1--;
					if(p1>array.length-1||p1<0 || p2>array[0].length-1||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(array);
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path[i].push(new Node(p1,p2,null));
					break;
				}
				case(2):{
					p2++;
					if(p1>array.length-1||p1<0 || p2>array[0].length-1||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(array);
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path[i].push(new Node(p1,p2,null));
					break;
				}
				case(3):{
					p2--;
					if(p1>array.length-1||p1<0 || p2>array[0].length-1||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(array);
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
					}else{
						array[p1][p2] = 4;
					}
					path[i].push(new Node(p1,p2,null));
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
