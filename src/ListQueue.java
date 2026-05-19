public class ListQueue extends LinkedList {

    public ListQueue() {
		super();
    }
	
	public boolean empty() {
		return isEmpty();
	}
	
	public void enqueue(Object item) {
		addToTail(item);
	}
	
	public Object dequeue() {
		return removeFromHead();
	}
	
} // class ListQueue