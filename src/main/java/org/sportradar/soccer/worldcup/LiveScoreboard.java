package org.sportradar.soccer.worldcup;

import java.time.Clock;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * LiveScoreboard provides functionalities of real time soccer scoreboard. With functions of
 * starting new matches, updating the scores and finishing matches.
 *
 * <p>It also has functionality providing current summary of ongoing matches in desired order.
 *
 * <p>How to use it:
 *
 * <pre>
 *     LiveScoreboard board = LiveScoreboard.getInstance(); //provides new instance
 *
 *     board.startMatch("TEAM_A", "TEAM_B"); //starts new match
 *     board.startMatch("TEAM_C", "TEAM_D");
 *
 *     board.updateScore("TEAM_A", 1, "TEAM_B", 2); //update score of the match
 *
 *     board.finishMatch("TEAM_A", "TEAM_B");
 *
 *     List<Summary.Score> summary = board.getSummary().getScores() //provides current list of scores
 *
 * </pre>
 *
 * @version 1.0
 * @since 1.0
 * @author krzysztofkoper
 */
public class LiveScoreboard {
  private final Clock clock;

  private final InMemoryMatchRepository inMemoryRepository;

  public static LiveScoreboard getInstance() {
    return new LiveScoreboard(Clock.systemUTC(), new InMemoryMatchRepository());
  }

  private LiveScoreboard(Clock clock, InMemoryMatchRepository inMemoryMatchRepository) {
    this.clock = clock;
    this.inMemoryRepository = inMemoryMatchRepository;
  }

  /**
   * Starting new match at this particular moment and adds it to the scoreboard.
   *
   * @throws IllegalArgumentException - when null parameters are passed
   * @throws IllegalStateException - when provided team is already part of other match on the
   *     scoreboard
   * @param homeTeam - home team name
   * @param awayTeam - away team name
   */
  public void startMatch(final String homeTeam, final String awayTeam) {
    checkTeamNames(homeTeam, awayTeam);
    checkIfTeamAlreadyInAMatch(homeTeam);
    checkIfTeamAlreadyInAMatch(awayTeam);
    inMemoryRepository.save(Match.of(homeTeam, awayTeam, clock.instant()));
  }

  /**
   * Finishes match by removing it from the board.
   *
   * <p>Note: If there is no match on the board for given teams method will not report any errors,
   * operation would be successful
   *
   * @throws IllegalArgumentException - when null parameters are passed
   * @param homeTeam - home team name
   * @param awayTeam - away team name
   */
  public void finishMatch(final String homeTeam, final String awayTeam) {
    checkTeamNames(homeTeam, awayTeam);
    inMemoryRepository.deleteByHomeTeamAndAwayTeam(homeTeam, awayTeam);
  }

  /**
   * Updates score for a match on the board.
   *
   * @throws IllegalArgumentException - when null parameters are passed
   * @throws IllegalArgumentException - when provided scores are negative values
   * @throws IllegalStateException - when match does not exist on the scoreboard
   * @param homeTeam - home team name
   * @param awayTeam - away team name
   */
  public void updateScore(
      final String homeTeam, final int homeScore, final String awayTeam, final int awayScore) {
    checkTeamNames(homeTeam, awayTeam);
    inMemoryRepository
        .findByHomeTeamAndAwayTeam(homeTeam, awayTeam)
        .ifPresentOrElse(
            match -> inMemoryRepository.save(match.updateScore(homeScore, awayScore)),
            () -> {
              throw new IllegalStateException(
                  String.format(
                      "There is no match on the scoreboard for home team: %s and away team: %s",
                      homeTeam, awayTeam));
            });
  }

  /**
   * Returns actual summary of the current scores that are on the board.
   *
   * <p>Scores are ordered by match total score in descending way. The matches with the same total
   * score are ordered by the most recently started matches in the scoreboard.
   *
   * @return Summary
   */
  public Summary getSummary() {
    return Summary.from(inMemoryRepository.findAllOrderedByTotalScoreAndStartingTime());
  }

  private static void checkTeamNames(final String homeTeam, final String awayTeam) {
    if (Objects.isNull(homeTeam) || Objects.isNull(awayTeam)) {
      throw new IllegalArgumentException(
          String.format(
              "Provided team names cannot be null, provided home team: %s away team: %s",
              homeTeam, awayTeam));
    }
  }

  private void checkIfTeamAlreadyInAMatch(final String teamName) {
    if (inMemoryRepository.existsForATeam(teamName)) {
      throw new IllegalStateException(
          String.format(
              "There is already ongoing match for a team on the scoreboard: %s", teamName));
    }
  }

  private static class InMemoryMatchRepository {
    private List<Match> liveMatchesInOrder = Collections.emptyList();
    private Set<String> liveTeams = Collections.emptySet();
    private final Map<String, Match> keyToMatchMap = Collections.synchronizedMap(new HashMap<>());

    private static final Comparator<Match> matchComparator =
        Collections.reverseOrder(
            (match1, match2) -> {
              int totalScoreComparison = match1.getTotalScore().compareTo(match2.getTotalScore());
              if (totalScoreComparison == 0) {
                return match1.getStartingTime().compareTo(match2.getStartingTime());
              }
              return totalScoreComparison;
            });

    void save(final Match match) {
      keyToMatchMap.put(generateKey(match), match);
      reloadLiveData();
    }

    void deleteByHomeTeamAndAwayTeam(final String homeTeam, final String awayTeam) {
      keyToMatchMap.remove(generateKey(homeTeam, awayTeam));
      reloadLiveData();
    }

    Optional<Match> findByHomeTeamAndAwayTeam(final String homeTeam, final String awayTeam) {
      return Optional.ofNullable(keyToMatchMap.get(generateKey(homeTeam, awayTeam)));
    }

    boolean existsForATeam(final String teamName) {
      return liveTeams.contains(teamName);
    }

    List<Match> findAllOrderedByTotalScoreAndStartingTime() {
      return liveMatchesInOrder;
    }

    private synchronized void reloadLiveData() {
      liveMatchesInOrder = keyToMatchMap.values().stream().sorted(matchComparator).toList();
      liveTeams =
          liveMatchesInOrder.stream()
              .flatMap(match -> Stream.of(match.getHomeTeam(), match.getAwayTeam()))
              .collect(Collectors.toUnmodifiableSet());
    }

    private static String generateKey(final Match match) {
      return generateKey(match.getHomeTeam(), match.getAwayTeam());
    }

    private static String generateKey(final String homeTeam, final String awayTeam) {
      return homeTeam + "-" + awayTeam;
    }
  }
}
