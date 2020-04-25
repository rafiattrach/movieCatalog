package network.net;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Movie {
	private final long id;
	private final String name;
	private final String genre;
	private final String description;
	private final int releaseYear;
	private final int durationInMinutes;
	private final double rating;
	private final Set<String> movieStars;

	public Movie(long id, String name, String genre, String description, int releaseYear, int durationInMinutes,
			double rating, Set<String> movieStars) {
		if (releaseYear < 1888)
			throw new IllegalArgumentException(
					"The world's earliest surviving motion-picture film was in 1888!/n Enter a value after that");

		if (rating < 0 || rating > 10)
			throw new IllegalArgumentException("Rating can only be between 0 and 10!");

		if (durationInMinutes < 0)
			throw new IllegalArgumentException("Duration of a movie in minutes can't be negative!");

		this.id = id;
		this.name = Objects.requireNonNull(name, "name");
		this.genre = Objects.requireNonNull(genre, "genre");
		this.durationInMinutes = durationInMinutes;
		this.rating = rating;
		this.releaseYear = releaseYear;
		this.movieStars = Objects.requireNonNull(movieStars, "hobbies");
		this.description = Objects.requireNonNull(description, "aboutMe");
	}

	public int getDurationInMinutes() {
		return durationInMinutes;
	}

	public double getRating() {
		return rating;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getGenre() {
		return genre;
	}

	public int getReleaseYear() {
		return releaseYear;
	}

	public Set<String> getMovieStars() {
		return movieStars;
	}

	public String getDescription() {
		return description;
	}

	public String toCsvRow() {
		return String.format("%s,%s,%s,%s,%s,%s,%s,%s", id, name, genre, description, releaseYear, durationInMinutes,
				rating, movieStars.stream().collect(Collectors.joining(" ")));
	}

	public static Movie parse(String csvRow) {
		String[] parts = csvRow.split(",", 8);
		return new Movie(Long.parseLong(parts[0]), parts[1], parts[2], parts[3], Integer.parseInt(parts[4]),
				Integer.parseInt(parts[5]), Double.parseDouble(parts[6]), Set.of(parts[7].split(" ", -1)));
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Movie))
			return false;
		Movie other = (Movie) obj;
		return id == other.id;
	}

	@Override
	public String toString() {
		return "Movie: " + name + "/n" + "Rating: " + rating + "/n" + "Genre: " + genre;
	}
}
