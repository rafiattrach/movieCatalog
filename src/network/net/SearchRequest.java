package network.net;

import java.util.Objects;
import java.util.Set;

public final class SearchRequest {
	private final String genre;
	private final double rating;
	private final Set<String> movieStars;

	public SearchRequest(String genre, double rating, Set<String> movieStars) {

		if (rating < 0 || rating > 10)
			throw new IllegalArgumentException("Rating can only be between 0 and 10!");
		this.rating = rating;
		this.genre = Objects.requireNonNull(genre, "genre");
		this.movieStars = Objects.requireNonNull(movieStars, "hobbies");
	}

	public double getRating() {
		return rating;
	}

	public String getGenre() {
		return genre;
	}

	public Set<String> getMovieStars() {
		return movieStars;
	}

	@Override
	public int hashCode() {
		return Objects.hash(genre, movieStars, rating);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SearchRequest))
			return false;
		SearchRequest other = (SearchRequest) obj;
		return rating == other.rating && Objects.equals(genre, other.genre)
				&& Objects.equals(movieStars, other.movieStars);
	}

	@Override
	public String toString() {
		return String.format("SeachRequest [genre=%s, rating=%s, movie stars=%s]", genre, rating, movieStars);
	}

}
