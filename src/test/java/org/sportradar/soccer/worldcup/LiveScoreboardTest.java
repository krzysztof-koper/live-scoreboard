package org.sportradar.soccer.worldcup;

import static org.junit.jupiter.api.Assertions.*;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_A;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_B;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_C;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_D;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LiveScoreboardTest {
  private LiveScoreboard liveScoreboard;

  @BeforeEach
  void beforeEach() {
    liveScoreboard = LiveScoreboard.getInstance();
  }

  @Nested
  @DisplayName("start match")
  class StartMatch {
    @ParameterizedTest
    @DisplayName("fails with exception when null objects are passed as parameters")
    @MethodSource("provideNullTeamNames")
    void throwsException_whenNullParametersArePassed(
        String teamA, String teamB, String expectedMessage) {
      Exception exception =
          assertThrows(
              IllegalArgumentException.class, () -> liveScoreboard.startMatch(teamA, teamB));

      String actualMessage = exception.getMessage();
      assertEquals(actualMessage, expectedMessage);
    }

    @ParameterizedTest
    @DisplayName("fails with exception when team is already playing in another match")
    @MethodSource("provideTeamsAlreadyInAMatch")
    void throwsException_whenTeamIsAlreadyInAMatch(
        String homeTeamInAMatch,
        String awayTeamInAMatch,
        String teamA,
        String teamB,
        String expectedMessage) {

      liveScoreboard.startMatch(homeTeamInAMatch, awayTeamInAMatch);
      Exception exception =
          assertThrows(IllegalStateException.class, () -> liveScoreboard.startMatch(teamA, teamB));

      String actualMessage = exception.getMessage();
      assertEquals(actualMessage, expectedMessage);
    }

    private static Stream<Arguments> provideNullTeamNames() {
      return getNullTeamNames();
    }

    private static Stream<Arguments> provideTeamsAlreadyInAMatch() {
      String expectedExceptionMessage =
          "There is already ongoing match for a team on the scoreboard: %s";
      Stream<Arguments> argumentsStream =
          Stream.of(
              Arguments.of(
                  TEAM_A, TEAM_B, TEAM_A, TEAM_C, String.format(expectedExceptionMessage, TEAM_A)),
              Arguments.of(
                  TEAM_A, TEAM_B, TEAM_C, TEAM_B, String.format(expectedExceptionMessage, TEAM_B)));
      return argumentsStream;
    }
  }

  @Nested
  @DisplayName("finish match")
  class FinishMatch {

    @ParameterizedTest
    @DisplayName("fails with exception when null objects are passed as parameters")
    @MethodSource("provideNullTeamNames")
    void throwsException_whenNullParametersArePassed(
        String teamA, String teamB, String expectedMessage) {
      Exception exception =
          assertThrows(
              IllegalArgumentException.class, () -> liveScoreboard.finishMatch(teamA, teamB));

      String actualMessage = exception.getMessage();
      assertEquals(actualMessage, expectedMessage);
    }

    private static Stream<Arguments> provideNullTeamNames() {
      return getNullTeamNames();
    }
  }

  @Nested
  @DisplayName("update score")
  class UpdateScore {
    @ParameterizedTest
    @DisplayName("fails with exception when null objects are passed as parameters")
    @MethodSource("provideNullTeamNames")
    void throwsException_whenNullParametersArePassed(
        String teamA, String teamB, String expectedMessage) {
      Exception exception =
          assertThrows(
              IllegalArgumentException.class, () -> liveScoreboard.updateScore(teamA, 1, teamB, 1));

      String actualMessage = exception.getMessage();
      assertEquals(actualMessage, expectedMessage);
    }

    @Test
    @DisplayName("fails with exception when match is not on the scoreboard")
    void throwsException_whenMatchIsNotOnScoreboard() {
      Exception exception =
          assertThrows(
              IllegalStateException.class, () -> liveScoreboard.updateScore(TEAM_A, 1, TEAM_B, 1));

      String expectedMessage =
          String.format(
              "There is no match on the scoreboard for home team: %s and away team: %s",
              TEAM_A, TEAM_B);
      String actualMessage = exception.getMessage();
      assertEquals(actualMessage, expectedMessage);
    }

    private static Stream<Arguments> provideNullTeamNames() {
      return getNullTeamNames();
    }
  }

  @Nested
  @DisplayName("get summary")
  class GetSummary {

    @Test
    @DisplayName("provides expected summary for exemplary World Cup conditions")
    void succeeds_forExemplaryWorldCupConditions() {
      // given
      /*
       Following matches are started in the specified order and their scores respectively updated
        Mexico 0 - Canada 5
        Spain 10 - Brazil 2
        Germany 2 - France 2
        Uruguay 6 - Italy 6
        Argentina 3 - Australia 1
      */
      liveScoreboard.startMatch("Mexico", "Canada");
      liveScoreboard.startMatch("Spain", "Brazil");
      liveScoreboard.startMatch("Germany", "France");
      liveScoreboard.startMatch("Uruguay", "Italy");
      liveScoreboard.startMatch("Argentina", "Australia");

      liveScoreboard.updateScore("Spain", 10, "Brazil", 2);
      liveScoreboard.updateScore("Uruguay", 6, "Italy", 6);
      liveScoreboard.updateScore("Mexico", 0, "Canada", 5);
      liveScoreboard.updateScore("Argentina", 3, "Australia", 1);
      liveScoreboard.updateScore("Germany", 2, "France", 2);

      // when
      Summary actualSummary = liveScoreboard.getSummary();

      // then
      /*
        The summary should be as follows:
          1. Uruguay 6 - Italy 6
          2. Spain 10 - Brazil 2
          3. Mexico 0 - Canada 5
          4. Argentina 3 - Australia 1
          5. Germany 2 - France 2
      */
      Summary expectedSummary =
          Summary.of(
              new Summary.Score("Uruguay", 6, "Italy", 6),
              new Summary.Score("Spain", 10, "Brazil", 2),
              new Summary.Score("Mexico", 0, "Canada", 5),
              new Summary.Score("Argentina", 3, "Australia", 1),
              new Summary.Score("Germany", 2, "France", 2));
      assertEquals(expectedSummary, actualSummary);
    }

    @Test
    @DisplayName("provides empty summary when all matches were already finished")
    void providesEmptyList_whenAllMatchesWereFinished() {
      // given
      liveScoreboard.startMatch(TEAM_A, TEAM_B);
      liveScoreboard.updateScore(TEAM_A, 1, TEAM_B, 0);

      liveScoreboard.startMatch(TEAM_C, TEAM_D);
      liveScoreboard.updateScore(TEAM_C, 1, TEAM_D, 2);

      liveScoreboard.finishMatch(TEAM_A, TEAM_B);
      liveScoreboard.finishMatch(TEAM_C, TEAM_D);

      // when
      Summary actualSummary = liveScoreboard.getSummary();

      liveScoreboard.startMatch(TEAM_A, TEAM_B);

      // then
      assertTrue(actualSummary.getScores().isEmpty());
    }

    @Test
    @DisplayName("provides properly sorted summary when match was finished and stared again")
    void providesProperlyOrderList_whenMatchWasFinishedAndStartedAgain() {
      // given
      liveScoreboard.startMatch(TEAM_A, TEAM_B);
      liveScoreboard.updateScore(TEAM_A, 1, TEAM_B, 1);

      liveScoreboard.startMatch(TEAM_C, TEAM_D);
      liveScoreboard.updateScore(TEAM_C, 1, TEAM_D, 1);

      // finish match A,B
      liveScoreboard.finishMatch(TEAM_A, TEAM_B);
      // start match A,B again
      liveScoreboard.startMatch(TEAM_A, TEAM_B);
      liveScoreboard.updateScore(TEAM_A, 1, TEAM_B, 1);

      // when
      Summary actualSummary = liveScoreboard.getSummary();

      // then
      Summary expectedSummary =
          Summary.of(
              new Summary.Score(TEAM_A, 1, TEAM_B, 1), new Summary.Score(TEAM_C, 1, TEAM_D, 1));
      assertEquals(expectedSummary, actualSummary);
    }
  }

  private static Stream<Arguments> getNullTeamNames() {
    String expectedExceptionMessage =
        "Provided team names cannot be null, provided home team: %s away team: %s";
    return Stream.of(
        Arguments.of(null, null, String.format(expectedExceptionMessage, null, null)),
        Arguments.of(TEAM_A, null, String.format(expectedExceptionMessage, TEAM_A, null)),
        Arguments.of(null, TEAM_A, String.format(expectedExceptionMessage, null, TEAM_A)));
  }
}
