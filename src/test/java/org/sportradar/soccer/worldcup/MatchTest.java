package org.sportradar.soccer.worldcup;

import static org.junit.jupiter.api.Assertions.*;
import static org.sportradar.soccer.worldcup.Fixtures.*;
import static org.sportradar.soccer.worldcup.Fixtures.INSTANT_EPOCH;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_A;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_BLANK;
import static org.sportradar.soccer.worldcup.Fixtures.havingMatch;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MatchTest {

  @Test
  @DisplayName("match cannot be created when both teams have the same name")
  void constructor_throwsException_whenBothTeamsHaveTheSameName() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> Match.of(TEAM_A, TEAM_A, INSTANT_EPOCH));

    String expectedMessage =
        String.format(
            "Team names cannot be the same, provided home team: %s , away team: %s",
            TEAM_A, TEAM_A);
    String actualMessage = exception.getMessage();
    assertEquals(actualMessage, expectedMessage);
  }

  @Test
  @DisplayName("match cannot be created when team have blank name")
  void constructor_throwsException_whenTeamHaveBlankName() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> Match.of(TEAM_BLANK, TEAM_BLANK, INSTANT_EPOCH));

    String expectedMessage = String.format("Team name cannot be blank, provided: %s", TEAM_BLANK);
    String actualMessage = exception.getMessage();
    assertEquals(actualMessage, expectedMessage);
  }

  @DisplayName("update score fails with exception when invalid score value is passed")
  @ParameterizedTest
  @MethodSource("provideInvalidScoreValues")
  void updateScore_throwsException_whenInvalidScoreValueIsPassed(
      int homeScore, int awayScore, String expectedMessage) {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> havingMatch().updateScore(homeScore, awayScore));

    String actualMessage = exception.getMessage();
    assertEquals(actualMessage, expectedMessage);
  }

  @DisplayName("update score succeeds when valid score value are passed")
  @ParameterizedTest
  @MethodSource("provideValidScoreValues")
  void updateScore_succeeds_whenValidScoreValueIsPassed(
      int homeScore, int awayScore, int totalScore) {
    // when
    Match match = havingMatch().updateScore(homeScore, awayScore);

    // then
    assertEquals(homeScore, match.getHomeScore());
    assertEquals(awayScore, match.getAwayScore());
    assertEquals(totalScore, match.getTotalScore());
  }

  private static Stream<Arguments> provideInvalidScoreValues() {
    String expectedExceptionMessage = "Team score cannot be negative number, provided: %d";
    return Stream.of(
        Arguments.of(-1, 5, String.format(expectedExceptionMessage, -1)),
        Arguments.of(0, -4, String.format(expectedExceptionMessage, -4)),
        Arguments.of(-4, -5, String.format(expectedExceptionMessage, -4)));
  }

  private static Stream<Arguments> provideValidScoreValues() {
    return Stream.of(Arguments.of(0, 0, 0), Arguments.of(0, 1, 1), Arguments.of(10, 5, 15));
  }
}
