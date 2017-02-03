import java.util.Random;

public class HighwayConstructor {
	//public int [][] array;
	int p1;
	int p2;
	int pathLength = 0; //counter to 100
	int reverse1[] = new int[2000];
	int reverse2[] = new int[2000];
	int reverseCounter = 0;
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
			array = start(array);
			/*
			if(rejected || found) //the first 20 cells are not valid
			{
				clean(array);
				i=i-1;
				rejected = false;
			}*/
			while(!found){
				changeDirection();
				array = straightPath(path,array);
				if(rejected){
					break;
				}
				if(pathLength < 100 && found == true)
				{
					clean(array);
					i=i-1;
				}
			}
			if(rejected){
				i=i-1;
				rejected = false;
				clean(array);
			}
			
			pathLength=0;//reset in case we called rejectPath()
			reverseCounter=0;//reset in case we called rejectPath()
		}
		
		return array;
	}
	
	private int[][] start(int[][] array){
		Random rand = new Random();
		int val; int val2;
		boolean highwayOccupied = true;
		//insertion of highways, codes 3 and 4
		//need a loop here
		p1=0; p2=0;//p1 = row position, p2 = column position
		Stack path = new Stack();//the current path
		dir = rand.nextInt(4);
		while(highwayOccupied)//check if collides with highway if(yes) then start again
		{
		  p1 = 0;
		  p2 = 0;
		  switch(dir){//determines start position of highway
			case(0):{//start on top row, direction 0 = down
				val2 = rand.nextInt(array[0].length);//num of cols 160
				p2 = val2;
				if(array[p1][p2] != 3 || array[p1][p2] != 4){//check if collides with highway if(yes) then start again
				 if (array[p1][p2] == 1){ 
					array[p1][p2] = 3;
				 }else{
					array[p1][p2] = 4;
				 }
				 path.push(new Node(p1,p2,null));
				 highwayOccupied = false;
				 reverse1[reverseCounter]=p1;
				 reverse2[reverseCounter]=p2;
				 break;
				}
				else
					break;
			}
			case(1):{//start bottom row, direction 1 = up
				p1 = array.length-1;//num of rows 120
				val2 = rand.nextInt(array[0].length);
				p2 = val2;
				if(array[p1][p2] != 3 || array[p1][p2] != 4){//check if collides with highway if(yes) then start again
				 if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				 }else{
					array[p1][p2] = 4;
				 }
				 path.push(new Node(p1,p2,null));
				 highwayOccupied = false;
				 reverse1[reverseCounter]=p1;
				 reverse2[reverseCounter]=p2;
				 break;
				}
				else
					break;
			}
			case(2):{//start on left column, direction 2 = right
				val2 = rand.nextInt(array.length);//num of rows 120
				p1 = val2;
				if(array[p1][p2] != 3 || array[p1][p2] != 4){//check if collides with highway if(yes) then start again
				 if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				 }else{
					array[p1][p2] = 4;
				 }
				 path.push(new Node(p1,p2,null));
				 highwayOccupied = false;
				 reverse1[reverseCounter]=p1;
				 reverse2[reverseCounter]=p2;
				 break;
				}
				else
					break;
			}
			case(3):{//start on right column, direction 3 = left
				p2 = array[0].length-1;
				val2 = rand.nextInt(array.length);
				p1 = val2;
				if(array[p1][p2] != 3 || array[p1][p2] != 4){//check if collides with highway if(yes) then start again
				 if (array[p1][p2] == 1){
					array[p1][p2] = 3;
				 }else{
					array[p1][p2] = 4;
				 }
				 path.push(new Node(p1,p2,null));
				 highwayOccupied = false;
				 reverse1[reverseCounter]=p1;
				 reverse2[reverseCounter]=p2;
				 break;
				}
				else
					break;
			}
		  }
		}
		array = straightPath(path,array);
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
			switch(dir){
				case(0): dir = 2; break;
				case(1): dir = 3; break;
				case(2): dir = 1; break;
				case(3): dir = 0; break;
			}
		}
	}
	
	
	private void rejectPath(Stack s, int[][] array){
		rejected = true;
		while(!s.isEmpty()){
			Node n = s.pop();
			if(array[n.p1][n.p2] == 3){
				array[n.p1][n.p2] = 1;
			}else{
				array[n.p1][n.p2] = 2;
			}
		}
		//clean(array);
	}
	
	private int[][] straightPath(Stack path,int[][] array){
		//int p1=0; int p2=0;
		for(int x = 0; x < 20; x++){//change constant depending on how many tiles u wanna go
			switch(dir){
				case(0):{
					//check for boundary
					p1++;
					if(p1>=array.length||p1<0 || p2>=array[0].length||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){//check if the tile is already a highway
						rejectPath(path,array);//implement this before testing
						break;
					}
					if (array[p1][p2] == 1){//convert to highway and push to stack		
						array[p1][p2] = 3;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;
						
					}else{
						array[p1][p2] = 4;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;						
					}
					path.push(new Node(p1,p2,null));
					break;
				}
				case(1):{
					p1--;
					if(p1>=array.length||p1<0 || p2>=array[0].length||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(path,array);
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;						
					}else{
						array[p1][p2] = 4;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;
					}
					path.push(new Node(p1,p2,null));
					break;
				}
				case(2):{
					p2++;
					if(p1>=array.length||p1<0 || p2>=array[0].length||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(path,array);
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;
						
					}else{
						array[p1][p2] = 4;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;
					}
					path.push(new Node(p1,p2,null));
					break;
				}
				case(3):{
					p2--;
					if(p1>=array.length||p1<0 || p2>=array[0].length||p2<0){//found a boundary
						found = true;
						break;
					}
					if (array[p1][p2] == 3 || array[p1][p2] == 4){
						rejectPath(path,array);
						break;
					}
					if (array[p1][p2] == 1){
						array[p1][p2] = 3;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;
						
					}else{
						array[p1][p2] = 4;
						pathLength++;
						reverseCounter++;
						reverse1[reverseCounter]=p1;
						reverse2[reverseCounter]=p2;
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
	
	public void clean(int[][] array)
	{
		pathLength = 0;
		for(int i=0;i<reverseCounter;i++)
		{
			if(array[reverse1[i]][reverse2[i]] == 3)
			{
				array[reverse1[i]][reverse2[i]] = 1;
			}
			else
			{
				array[reverse1[i]][reverse2[i]] = 2;
			}
		}
		for(int j=0;j<2000;j++)
		{
			reverse1[j]=0;
			reverse2[j]=0;
		}
		reverseCounter = 0;
	}
}
