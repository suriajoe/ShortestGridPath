import java.util.NoSuchElementException;

public class Stack {
	private Node top;
	private int size;
	public Stack (){
		top = null;
		size = 0;
	}
	
	public void push (Node item){
		item.next = top;
		top = item;
		size++;
	}
	
	public Node pop ()
	throws NoSuchElementException{
		if (isEmpty()){
			throw new NoSuchElementException();
		}
		Node tmp = top;
		top = top.next;
		size--;
		return tmp;
	}
	
	public String peekInfo(){
		try{
			return top.toString();
		}catch(NoSuchElementException e){
			return ("empty stack");
		}
	}
	
	public int getSize(){
		return size;
	}
	
	public boolean isEmpty(){
		if (size!=0){
			return false;
		}
		return true;
	}
	
	public Node clear(){
		top = null;
		size = 0;
		return top;
	}
	
}
