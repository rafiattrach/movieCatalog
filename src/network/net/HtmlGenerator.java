package network.net;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * A tool to generate simple HTML pages for Movie Catalog based on templates.
 */
public final class HtmlGenerator {

	static Path findTemplatePath = Path.of("templates", "search.html");
	static Path profileTemplatePath = Path.of("templates", "profile.html");

	private static final String TEMPL_RATING = "%rating";
	private static final String TEMPL_GENRE = "%genre";
	private static final String TEMPL_TABLE = "%table";
	private static final String TEMPL_MOVIE_STARS = "%movieStars";
	private static final String TEMPL_DURATION_IN_MINUTES = "%durationInMinutes";
	private static final String TEMPL_NAME = "%name";
	private static final String TEMPL_RELEASE_YEAR = "%releaseYear";
	private static final String TEMPL_DESCRIPTION = "%description";

	private static final String HTML_TABLE_START = "<table border=\"1px solid black\">"
			+ "<tr><td><b>Name</b></td><td><b>Rating</b></td><td><b>Genre</b></td></tr>";
	private static final String HTML_TABLE_ROW = "<tr><td><a href=\"movie/%s\">%s</a></td><td>%s</td><td>%s</td></tr>";
	private static final String HTML_TABLE_END = "</table>";
	private static final String HTML_TABLE_NO_RESULTS = "<p>No results found :(</p>";

	private final TemplateProcessor profileTemplate;
	private final TemplateProcessor findTemplate;

	public HtmlGenerator() throws IOException {
		this.profileTemplate = new TemplateProcessor(profileTemplatePath);
		this.findTemplate = new TemplateProcessor(findTemplatePath);
	}

	/**
	 * Generates a profile page for the supplied Movie using the profileTemplate
	 */
	public String generateProfilePage(Movie movie) {
		return profileTemplate.replace(Map.of(TEMPL_NAME, movie.getName(), TEMPL_GENRE, movie.getGenre(), TEMPL_RATING,
				String.valueOf(movie.getRating()), TEMPL_DURATION_IN_MINUTES,
				String.valueOf(movie.getDurationInMinutes()), TEMPL_RELEASE_YEAR,
				String.valueOf(movie.getReleaseYear()), TEMPL_MOVIE_STARS,
				movie.getMovieStars().stream().collect(Collectors.joining(", ")), TEMPL_DESCRIPTION,
				movie.getDescription()));
	}

	/**
	 * Generates the find page for the supplied movie-matches using the
	 * findTemplate.
	 * <p>
	 * The request passed to the method is used to fill the form fields with the
	 * user's input values
	 */
	public String generateFindPage(SearchRequest request, List<Movie> results) {
		String table;
		if (results.isEmpty()) {
			table = HTML_TABLE_NO_RESULTS;
		} else {
			table = results
					.stream().map(movie -> String.format(HTML_TABLE_ROW, movie.getId(), movie.getName(),
							movie.getGenre(), movie.getRating()))
					.collect(Collectors.joining("", HTML_TABLE_START, HTML_TABLE_END));
		}
		return findTemplate.replace(Map.of(TEMPL_RATING, String.valueOf(request.getRating()), TEMPL_GENRE,
				request.getGenre(), TEMPL_MOVIE_STARS,
				request.getMovieStars().stream().collect(Collectors.joining(" ")), TEMPL_TABLE, table));
	}

	/**
	 * Generates the start page which is the find page without any preset inputs and
	 * no table with matching movies
	 */
	public String generateStartPage() {
		return findTemplate.replace(Map.of(TEMPL_RATING, "", TEMPL_GENRE, "", TEMPL_MOVIE_STARS, "", TEMPL_TABLE, ""));

	}
}
