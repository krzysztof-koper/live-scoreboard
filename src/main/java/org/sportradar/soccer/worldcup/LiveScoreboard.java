package org.sportradar.soccer.worldcup;

import java.time.Clock;

public class LiveScoreboard {
  private final Clock clock;

  public static LiveScoreboard getInstance() {
    return new LiveScoreboard(Clock.systemUTC());
  }

  private LiveScoreboard(Clock clock) {
    this.clock = clock;
  }

  public void startMatch(final String homeTeam, final String awayTeam) {}

  public void finishMatch(final String homeTeam, final String awayTeam) {}

  public void updateScore(
      final String homeTeam, final int homeScore, final String awayTeam, final int awayScore) {}

  public Summary getSummary() {
    return Summary.of();
  }
}
