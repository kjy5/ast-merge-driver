package org.kjy5.utils;

/**
 * Enum for branches in a merge.
 *
 * <p>LEFT is for your branch, RIGHT is for the other branch, BASE is for the common ancestor, and
 * MERGED is for the merge result.
 */
public enum Branch {
  LEFT,
  RIGHT,
  BASE,
  MERGED
}
