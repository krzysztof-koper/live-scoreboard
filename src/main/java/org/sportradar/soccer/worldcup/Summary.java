package org.sportradar.soccer.worldcup;

import java.util.List;
import java.util.Objects;

/**
 * LiveScoreboard Summary view class
 *
 * <p>It contains list of the current scores on the scoreboard
 *
 * @version 1.0
 * @since 1.0
 * @author krzysztofkoper
 */
public class Summary {
  private final List<Score> scores;

  private Summary(List<Score> scores) {
    this.scores = scores;
  }

  static Summary of(final Score... scores) {
    return new Summary(List.of(scores));
  }

  static Summary from(final List<Match> matches) {
    return new Summary(matches.stream().map(Score::from).toList());
  }

  static Summary from(final Match... matches) {
    return from(List.of(matches));
  }

  public List<Score> getScores() {
    return scores;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Summary summary = (Summary) o;
    return Objects.equals(scores, summary.scores);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scores);
  }

  public record Score(String homeTeam, int homeScore, String awayTeam, int awayScore) {
    static Score from(final Match match) {
      return new Score(
          match.getHomeTeam(), match.getHomeScore(), match.getAwayTeam(), match.getAwayScore());
    }
  }
}
