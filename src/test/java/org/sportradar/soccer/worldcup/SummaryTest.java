package org.sportradar.soccer.worldcup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_A;
import static org.sportradar.soccer.worldcup.Fixtures.TEAM_B;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SummaryTest {
  @Test
  @DisplayName("from method properly maps Match object into Summary object")
  void from_Succeeds_WhenProperMatchObjectIsPassed() {
    // given
    Match match = Fixtures.havingMatch(TEAM_A, TEAM_B, 10, 20);

    // when
    Summary actualSummary = Summary.from(match);
    Summary expectedSummary = Summary.of(new Summary.Score(TEAM_A, 10, TEAM_B, 20));

    // then
    assertEquals(actualSummary, expectedSummary);
  }
}
