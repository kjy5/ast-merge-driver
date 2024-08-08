package org.kjy5.driver;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matchers;

/**
 * Three-way matching of GumTree Trees.
 *
 * @param baseToLeft the mapping from the base tree to the left tree
 * @param baseToRight the mapping from the base tree to the right tree
 * @param leftToRight the mapping from the left tree to the right tree
 */
public record Matching(
    MappingStore baseToLeft, MappingStore baseToRight, MappingStore leftToRight) {
  /**
   * Create a Matching object from a set of parsings.
   *
   * @param parsings the set of parsings to match
   * @return a new Matching object
   * @see org.kjy5.driver.Parsings
   */
  public static Matching from(Parsings parsings) {
    // TODO: Consider mapping from left/right to base to better follow usage direction later.
    Run.initMatchers();
    final var matcher = Matchers.getInstance().getMatcher();
    final var baseToLeft = matcher.match(parsings.baseTree(), parsings.leftTree());
    final var baseToRight = matcher.match(parsings.baseTree(), parsings.rightTree());
    final var leftToRight = matcher.match(parsings.leftTree(), parsings.rightTree());

    return new Matching(baseToLeft, baseToRight, leftToRight);
  }
}
