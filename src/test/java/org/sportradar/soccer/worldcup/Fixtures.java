package org.sportradar.soccer.worldcup;

import java.time.Instant;

class Fixtures {

  public static final Instant INSTANT_EPOCH = Instant.EPOCH;
  public static final String TEAM_A = "team_a";
  public static final String TEAM_B = "team_b";
  public static final String TEAM_C = "team_c";
  public static final String TEAM_D = "team_d";
  public static final String TEAM_BLANK = "  ";

  public static Match havingMatch() {
    return havingMatch(TEAM_A, TEAM_B);
  }

  public static Match havingMatch(String homeTeam, String awayTeam) {
    return new Match(homeTeam, awayTeam, INSTANT_EPOCH);
  }

  public static Match havingMatch(String homeTeam, String awayTeam, int homeScore, int awayScore) {
    return havingMatch(homeTeam, awayTeam).updateScore(homeScore, awayScore);
  }
}
