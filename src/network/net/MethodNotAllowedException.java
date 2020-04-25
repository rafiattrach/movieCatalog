package network.net;

@SuppressWarnings("serial")
public class MethodNotAllowedException extends RuntimeException {

	
	public MethodNotAllowedException() {
		super("Method type should be GET!");
	}
	
}
