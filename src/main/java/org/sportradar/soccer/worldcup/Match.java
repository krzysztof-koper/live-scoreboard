package org.sportradar.soccer.worldcup;

import java.time.Instant;
import java.util.Objects;

class Match {
  private final String homeTeam;
  private final String awayTeam;
  private final Instant startingTime;
  private int homeScore;
  private int awayScore;

  static Match of(String homeTeam, String awayTeam, Instant startingTime) {
    return new Match(homeTeam, awayTeam, startingTime);
  }

  Match(String homeTeam, String awayTeam, Instant startingTime) {
    this.homeTeam = homeTeam;
    this.awayTeam = awayTeam;
    this.startingTime = startingTime;
  }

  String getHomeTeam() {
    return homeTeam;
  }

  String getAwayTeam() {
    return awayTeam;
  }

  Instant getStartingTime() {
    return startingTime;
  }

  int getHomeScore() {
    return homeScore;
  }

  int getAwayScore() {
    return awayScore;
  }

  Integer getTotalScore() {
    return homeScore + awayScore;
  }

  Match updateScore(final int homeScore, final int awayScore) {
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Match match = (Match) o;
    return Objects.equals(homeTeam, match.homeTeam)
        && Objects.equals(awayTeam, match.awayTeam)
        && Objects.equals(startingTime, match.startingTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(homeTeam, awayTeam, startingTime);
  }
}
