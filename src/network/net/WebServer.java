package network.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;

public class WebServer extends Thread {

	private static int port = 8080;
	private static ServerSocket ss;
	private static MovieDatabase db;
	private HtmlGenerator gen;
	private static ExecutorService service;
	private static String newLine = "\r\n";
	/*
	 * Inform the user what paths are available and signal once the user inputs
	 * something invalid
	 */
	private String unkownPath = "The path is unknown!" + newLine + "The following paths are available:" + newLine
			+ newLine + "\" / \"" + " ---> to generate the start page" + newLine + "\" /find?<Parameters> \""
			+ " ---> to find your desired movie" + newLine + "\" /movie/<ID> \""
			+ " ---> to generate the profile of the desired movie" + newLine;

	public WebServer() {
		db = new MovieDatabase();
		service = Executors.newCachedThreadPool();
		try {
			gen = new HtmlGenerator();
		} catch (IOException e) {
			System.out.println("Problem occurred while trying to instantiate the HTML Generator object");
		}
	}

	public static void main(String[] args) {
		/*
		 * Start web server on a separate thread and react to user input on another
		 */
		WebServer server = new WebServer();
		server.start();
		System.out.println(newLine
				+ "The web server should be now accessible via the browser (under loaclhost or 127.0.0.1 under port 8080)!" + newLine);
		showHowToUseAddCommand();
		reactToUserInput();
	}

	private static void reactToUserInput() {

		Scanner scan = new Scanner(System.in);
		String clientInput = scan.nextLine();

		/*
		 * If the client input isn't shutdown keep responding to their commands
		 */
		while (!clientInput.equals("shutdown"))
			clientInput = reactToAddAndUnknownCommands(scan, clientInput);

		disconnect(scan);

	}

	private static String reactToAddAndUnknownCommands(Scanner scan, String clientInput) {
		/*
		 * Inform the user how to use add in case it's used incorrectly
		 */
		if (clientInput.startsWith("add")) {

			if (clientInput.split(",").length != 8) {
				showHowToUseAddCommand();
				System.err.println("Make sure you filled out all 8 fields");
				clientInput = scan.nextLine();
				return clientInput;
			}

			String[] blocks = clientInput.split(",");

			for (String b : blocks) {
				if (b.equals("")) {
					showHowToUseAddCommand();
					System.err.println("Fields can't be empty!!!");
					clientInput = scan.nextLine();
					return clientInput;
				}
			}

			for (int i = 0; i < blocks.length; i++)
				blocks[i] = blocks[i].trim();

			/*
			 * If the user uses add correctly transform that into a Movie first then add it
			 * to our database
			 */
			String[] movieStars = blocks[7].split(" ");
			Set<String> movieStarsAsSet = new HashSet<>();

			for (String stars : movieStars)
				movieStarsAsSet.add(stars);

			Movie newComer = null;
			Optional<Movie> twin;
			try {
				newComer = toMovieObject(blocks, movieStarsAsSet);
				twin = db.lookupById(newComer.getId());
			} catch (Exception e) {
				showHowToUseAddCommand();
				System.err.println("The world's earliest surviving motion-picture film was in 1888!" + newLine
						+ "Enter a value after that");
				clientInput = scan.nextLine();
				return clientInput;
			}
			if (twin.isPresent()) {
				// Inform if movie already exists
				System.out.println("The movie you're trying to add already exists!");

			} else {
				db.add(newComer);
				System.out.println("Desired movie has been added!");
			}
		} else {
			System.out.println("unknown command");
		}
		clientInput = scan.nextLine();
		return clientInput;
	}

	private static void disconnect(Scanner scan) {
		// shutdown server and thread pool
		try {
			System.out.println("closing sockets and scanner...");
			ss.close();
			scan.close();
			System.out.println("sockets and scanner successfully closed!");
		} catch (IOException e) {
			System.out.println("Something went wrong trying to shutdown the server...");
		}
		try {
			service.shutdown();
			service.awaitTermination(1, TimeUnit.SECONDS);
		} catch (@SuppressWarnings("unused") InterruptedException e) {
			System.err.println("Server shutdown was interrupted");
			Thread.currentThread().interrupt();
		}
		System.out.println("Server shut down successfully!");
	}

	private static Movie toMovieObject(String[] blocks, Set<String> movieStarsAsSet) {
		// in case letters are input where numbers are needed, inform the user
		Movie newComer = null;
		try {
			newComer = new Movie(Long.parseLong(blocks[0].substring(blocks[0].indexOf(' ') + 1)), blocks[1], blocks[2],
					blocks[3], Integer.parseInt(blocks[4]), Integer.parseInt(blocks[5]), Double.parseDouble(blocks[6]),
					movieStarsAsSet);
		} catch (NumberFormatException e) {
			showHowToUseAddCommand();
		}
		return newComer;
	}

	@Override
	public void run() {
		/*
		 * Start accepting new clients while server is running
		 */
		try {
			ss = new ServerSocket(port);

			while (true) {

				Socket client = ss.accept();

				assignTaskToExecutor(client);

			}

		} catch (IOException e) {
			System.out.println("Something went wrong trying to accept a new client...");
		}
	}

	private void assignTaskToExecutor(Socket client) throws IOException {
		// submit task to the thread pool via runnable object
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter writer = new PrintWriter(client.getOutputStream())) {

			Runnable task = () -> {
				String firstLine = "";
				try {
					firstLine = reader.readLine();
				} catch (IOException e) {
					System.out.println("Something went wrong trying to read the first line of request...");
				}

				HttpRequest request = null;
				request = checkRequestValidity(writer, firstLine, request);
				replyToRequest(writer, request);

			};
			submitToExecutor(task);

		}

		client.close();
	}

	private void replyToRequest(PrintWriter writer, HttpRequest request) {
		// react to request accordingly
		if (request.getPath().equals("/"))
			sendStartPage(writer);

		else if (request.getPath().startsWith("/find"))
			sendMatches(writer, request);

		else if (request.getPath().startsWith("/movie/"))
			sendMovie(writer, request);

		else
			// unknown path
			sendResponse(writer, HttpStatus.NOT_FOUND, unkownPath);

	}

	private void sendResponse(PrintWriter writer, HttpStatus status, String replyMessage) {
		HttpResponse response = new HttpResponse(status, replyMessage);
		writer.println(response.toString());
	}

	private HttpRequest checkRequestValidity(PrintWriter writer, String firstLine, HttpRequest request) {
		try {
			request = new HttpRequest(firstLine);
		}
		// inform user of misuse
		catch (MethodNotAllowedException e) {

			sendResponse(writer, HttpStatus.METHOD_NOT_ALLOWED, e.getMessage());

		} catch (IllegalArgumentException e) {

			sendResponse(writer, HttpStatus.FORBIDDEN, "The request should have the following format:" + newLine
					+ "<MethodType> <Path> <HTTP version>" + newLine);

		} catch (ParameterProblemException e) {

			sendResponse(writer, HttpStatus.FORBIDDEN, e.getMessage());

		}
		return request;
	}

	private void submitToExecutor(Runnable task) {
		Future<?> future = service.submit(task);

		try {
			future.get();
		} catch (InterruptedException e) {
			System.out.println("Task was interrupted...");
		} catch (ExecutionException e) {
			System.out.println("Task couldn't be executed...");
		}
	}

	private void sendMovie(PrintWriter writer, HttpRequest request) {

		String path = request.getPath();
		int slash = path.indexOf("/", path.indexOf("/") + 1);
		long movieID = 0L;
		try {
			movieID = Long.parseLong(path.substring(slash + 1, path.length()));
		} catch (NumberFormatException e) {
			sendResponse(writer, HttpStatus.FORBIDDEN,
					"There should be a number after the second slash" + "\r\n" + "Usage \" /movie/<long> \"");
			return;
		}
		Optional<Movie> potentialMovie = db.lookupById(movieID);
		if (potentialMovie.isPresent()) {
			String profilePage = gen.generateProfilePage(potentialMovie.get());
			sendResponse(writer, HttpStatus.OK, profilePage);
		} else {
			sendResponse(writer, HttpStatus.NOT_FOUND,
					"The movie you're trying to look for doesn't exist. Maybe soon though ;)");
		}
	}

	private void sendMatches(PrintWriter writer, HttpRequest request) {

		try {
			Map<String, String> parameters = request.getParameters();

			Set<String> movieStars = getMovieStarsFromURL(parameters);

			SearchRequest searchRequest = new SearchRequest(parameters.get("genre"),
					Double.parseDouble(parameters.get("rating")), movieStars);

			String findPage = gen.generateFindPage(searchRequest, db.findMatchesFor(searchRequest));
			sendResponse(writer, HttpStatus.OK, findPage);
		} catch (Exception e) {
			sendResponse(writer, HttpStatus.NOT_FOUND,
					"Please fill out the rating box at the very least. Rating can only be between 0 and 10");
		}
	}

	private Set<String> getMovieStarsFromURL(Map<String, String> parameters) {
		String movieStarsWithPlus = parameters.get("movieStars");
		Set<String> movieStars = new HashSet<>();
		// + is a special character so has to be escaped by \\
		for (String star : movieStarsWithPlus.split("\\+"))
			movieStars.add(star);
		return movieStars;
	}

	private void sendStartPage(PrintWriter writer) {
		String startPage = gen.generateStartPage();
		sendResponse(writer, HttpStatus.OK, startPage);
	}

	private static void showHowToUseAddCommand() {
		System.out.println(newLine
				+ "The two commands are 'add' and 'shutdown'! Use 'shutdown' to exit while 'add' is explained down below.."
				+ newLine);
		System.out.print("*** add USAGE ***" + newLine
				+ "add <ID>,<name>,<genre>,<description>,<release year>,<duration in minutes>,<rating>,<movie stars>"
				+ newLine + "Make sure to enter whole numbers for <release year> and <duration in minutes> and a "
				+ "decimal number for <rating>!" + newLine + newLine
				+ "The <> signs are only used to identify placeholders." + newLine + newLine);
	}
}
