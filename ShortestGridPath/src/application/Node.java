
public class Node {
	public int p1;
	public int p2;
	public Node next;
	public Node(int p1, int p2, Node next) {
		this.p1 = p1;
		this.p2 = p2;
		this.next = next;
	}
	public Node(){
		this.next = null;
	}
	public String toString() {
		return "(" + p1 + ", " + p2 + ")";
	}
	public String toListString(){
		String s = "";
		boolean isFirst = true;
		Node ptr = this;
		while (ptr != null){
			if (isFirst){
				s = ptr.toString();
				isFirst = false;
				ptr = ptr.next;
			}
			s += "->" + ptr.toString();
			ptr = ptr.next;
		}
		return (s);
	}
}