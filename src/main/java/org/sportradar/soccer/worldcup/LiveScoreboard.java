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

  public void startMatch(final String homeTeam, final String awayTeam) {
    checkTeamNames(homeTeam, awayTeam);
    checkIfTeamAlreadyInAMatch(homeTeam);
    checkIfTeamAlreadyInAMatch(awayTeam);
    inMemoryRepository.save(Match.of(homeTeam, awayTeam, clock.instant()));
  }

  public void finishMatch(final String homeTeam, final String awayTeam) {
    checkTeamNames(homeTeam, awayTeam);
    inMemoryRepository.deleteByHomeTeamAndAwayTeam(homeTeam, awayTeam);
  }

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
