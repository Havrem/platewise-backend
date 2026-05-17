package com.havrem.platewise.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LexoRankTest {

    @Test
    void between_bothNull_returnsMidpointOfAlphabet() {
        String r = LexoRank.between(null, null);
        assertThat(r).isEqualTo("n");
    }

    @Test
    void between_nullAndValue_returnsValueLessThanArgument() {
        String r = LexoRank.between(null, "n");
        assertThat(r.compareTo("n")).isNegative();
    }

    @Test
    void between_valueAndNull_returnsValueGreaterThanArgument() {
        String r = LexoRank.between("n", null);
        assertThat(r.compareTo("n")).isPositive();
    }

    @Test
    void between_twoSpacedRanks_returnsValueBetween() {
        String r = LexoRank.between("a", "c");
        assertThat(r.compareTo("a")).isPositive();
        assertThat(r.compareTo("c")).isNegative();
    }

    @Test
    void between_adjacentRanks_extendsString() {
        String r = LexoRank.between("a", "b");
        assertThat(r.compareTo("a")).isPositive();
        assertThat(r.compareTo("b")).isNegative();
        assertThat(r.length()).isGreaterThan(1);
    }

    @Test
    void between_deeplyAdjacentRanks_extendsFurther() {
        String r = LexoRank.between("aaa", "aab");
        assertThat(r.compareTo("aaa")).isPositive();
        assertThat(r.compareTo("aab")).isNegative();
    }

    @Test
    void between_repeatedSubdivision_keepsProducingValidRanks() {
        String low = "a";
        String high = "b";
        for (int i = 0; i < 50; i++) {
            String mid = LexoRank.between(low, high);
            assertThat(mid.compareTo(low)).isPositive();
            assertThat(mid.compareTo(high)).isNegative();
            high = mid;
        }
    }

    @Test
    void between_prevEqualsNext_throws() {
        assertThatThrownBy(() -> LexoRank.between("n", "n"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void between_prevGreaterThanNext_throws() {
        assertThatThrownBy(() -> LexoRank.between("z", "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
