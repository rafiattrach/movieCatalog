package network.net;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MovieDatabase {
	static Path dataFile = Path.of("db", "movies.csv");
	// "database" a.k.a List to store the movies
	ArrayList<Movie> movies = new ArrayList<>();
	// classic lock object to allow only one user to add at a time but for multiple
	// to read
	private RW lock = new RW();

	public MovieDatabase() {

		try (BufferedReader csvReader = Files.newBufferedReader(dataFile)) {
			String row;
			// add movie from file to "database"
			while ((row = csvReader.readLine()) != null)
				movies.add(Movie.parse(row));

		} catch (IOException e) {
			System.out.println("IOException while trying to read the " + dataFile);
		} catch (NumberFormatException e) {
			System.out.println("Number format exception, check if data has been input incorrectly!");
		}

	}

	List<Movie> findMatchesFor(SearchRequest request) {
		try {
			lock.startRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<Movie> result = new ArrayList<Movie>();

		for (Movie m : movies) {

			if ((m.getGenre().equals(request.getGenre()) || request.getGenre().equals(""))
					&& m.getRating() >= request.getRating()) {

				if (request.getMovieStars().contains("") || request.getMovieStars().contains("any"))
					result.add(m);
				else {
					Set<String> intersection = new HashSet<>(m.getMovieStars());
					intersection.retainAll(request.getMovieStars());
					// retain all filters out the common indices
					if (intersection.size() > 0)
						result.add(m);
				}
			}
		}

		lock.endRead();

		return result;

	}

	Optional<Movie> lookupById(long id) {

		try {
			lock.startRead();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Optional<Movie> lookedForMovie = Optional.empty();

		for (Movie p : movies) {
			if (p.getId() == id) {
				lookedForMovie = Optional.of(p);
				// if we found the movie no need to look anymore
				break;
			}
		}
		lock.endRead();

		return lookedForMovie;

	}

	boolean add(Movie newMovie) {
		try {
			lock.startWrite();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// check if ID already exists
		boolean uniqueId = true;
		for (Movie m : movies) {
			if (m.getId() == newMovie.getId())
				uniqueId = false;
		}
		if (!uniqueId) {
			lock.endWrite();
			return false;
		}

		try (FileWriter writer = new FileWriter(dataFile.toFile(), true)) {
			String csvRow = newMovie.toCsvRow();
			// if the file is empty don't skip a line
			if (dataFile.toFile().length() == 0)
				writer.write(csvRow);
			else
				// add a newline before typing to avoid merging two lines
				writer.write("\n" + csvRow);

		} catch (IOException e) {
			System.out.println("There was a problem writing to the csv file...Try again!");
			lock.endWrite();
			return false;
		}

		movies.add(newMovie);

		lock.endWrite();
		return true;

	}

}
