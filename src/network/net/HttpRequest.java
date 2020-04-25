package network.net;

import java.util.HashMap;
import java.util.Map;

public final class HttpRequest {

	private HttpMethod method;
	private String path;
	private Map<String, String> parameters = new HashMap<String, String>();

	HttpMethod getMethod() {
		return method;
	}

	String getPath() {
		return path;
	}

	Map<String, String> getParameters() {
		return parameters;
	}

	public HttpRequest(String request) {
		/*
		 * Check for invalid request format types.
		 */
		checkRequestValidity(request);

		String[] blocks = request.split(" ");

		method = HttpMethod.valueOf(blocks[0]);

		// check if request is find which therefore has parameters
		boolean hasParameters = false;

		if (blocks[1].startsWith("/find?"))
			hasParameters = true;

		if (hasParameters) {
			/*
			 * Parse the values from the request and store them in the map which will later
			 * be used to find matches for a certain request
			 */
			path = blocks[1].substring(blocks[1].indexOf('/'), blocks[1].indexOf('?'));
			String[] paramsWithValue = blocks[1].substring(blocks[1].indexOf('?') + 1, blocks[1].length()).split("&");
			for (String p : paramsWithValue)
				parameters.put(p.substring(0, p.indexOf('=')), p.substring(p.indexOf('=') + 1, p.length()));

			if (!parameters.containsKey("genre") || !parameters.containsKey("rating")
					|| !parameters.containsKey("movieStars") || parameters.size() != 3)
				throw new ParameterProblemException();

		} else
			path = blocks[1].substring(blocks[1].indexOf('/'), blocks[1].length());

	}

	private void checkRequestValidity(String request) {
		if (request == null)
			throw new IllegalArgumentException("request can't be null!");

		// has to have 3 parts
		if (request.split(" ").length != 3)
			throw new IllegalArgumentException(
					"The request must have the following format: <MethodType> <Path> <HTTP version>");

		// has to be GET
		HttpMethod get = HttpMethod.valueOf(request.split(" ")[0]);
		if (!(get == HttpMethod.GET))
			throw new MethodNotAllowedException();

		// path is never empty, always at least a /
		if (!request.split(" ")[1].contains("/"))
			throw new IllegalArgumentException("The second block of the request is the path which is at minimum '/' ");

		// there's a slash after HTTP and before the version
		if (!request.split(" ")[2].contains("/"))
			throw new IllegalArgumentException("The third block must contain '/' ");

	}
}
