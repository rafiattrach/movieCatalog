package network.net;

@SuppressWarnings("serial")
public class ParameterProblemException extends RuntimeException {

	/*
	 * Inform the user how to use the parameters in the URL in order to find matches
	 * for his/her request
	 */
	public ParameterProblemException() {
		super("The parameters are either spelled incorrectly or not in the right format!" + "\r\n" + " ***USAGE*** "
				+ "\r\n" + "\r\n"
				+ " \" /find?<Parameter>=<Value> \" where there has to be exactly 3 parameters and each parameter can be one of the following: "
				+ "\r\n" + " \" genre: String \" " + "\r\n" + " \" rating: int \" " + "\r\n"
				+ " \" movieStars: Set<String> \" " + "\r\n" + "Note: "
				+ " The movie star values can be simply concatinated with + between each other to represent a Set");
	}

}
