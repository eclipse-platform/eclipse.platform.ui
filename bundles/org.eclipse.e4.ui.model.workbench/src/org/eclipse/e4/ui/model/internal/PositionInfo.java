/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     René Brandstetter - Bug 411821 - [QuickAccess] Contribute SearchField
 *                                      through a fragment or other means
 ******************************************************************************/

package org.eclipse.e4.ui.model.internal;

/**
 * A holder class for the full information to position an element in a list.
 *
 * @author René Brandstetter
 */
public final class PositionInfo {
  /** The position type to use. */
  private final Position position;

  /**
   * The additional positioning information which can be used to position an
   * element relative to another element.
   */
  private final String positionReference;

  /**
   * The {@link PositionInfo} which represent an insert at the beginning of
   * the list.
   */
  public static final PositionInfo FIRST = new PositionInfo(Position.FIRST, null);

  /**
   * The {@link PositionInfo} which represent an insert at the end of the
   * list.
   */
  public static final PositionInfo LAST = new PositionInfo(Position.LAST, null);

  /**
   * Creates an instance of the PositionInfo.
   *
   * @param position
   *          the kind of the positioning
   * @param positionReference
   *          additional information which is need to position an element
   *          (e.g.: index, ID of another element)
   * @throws NullPointerException
   *           if the <code>position</code> is <code>null</code>
   */
  public PositionInfo(Position position, String positionReference) {
    if (position == null) {
      throw new NullPointerException("No position given!");
    }

    this.position = position;
    this.positionReference = positionReference;
  }

  /**
   * Returns the kind/type of positioning which should be used.
   *
   * @return the position
   */
  public Position getPosition() {
    return position;
  }

  /**
   * Returns additional information which is needed to place an element.
   *
   * @return the positionReference, or <code>null</code> if no additional information is given
   */
  public String getPositionReference() {
    return positionReference;
  }

  /**
   * Returns the additional information which is needed to place an element as
   * an int.
   *
   * @return the positionReference as an int
   * @throws NumberFormatException
   *           if the {@link #positionReference} can't be parsed to an int
   * @throws NullPointerException
   *           if the {@link #positionReference} is <code>null</code>
   */
  public int getPositionReferenceAsInteger() {
    return Integer.parseInt(positionReference);
  }

  /**
   * Creates a {@link PositionInfo} object out of the given positioning
   * string.
   *
   * <p>
   * <b>Examples for a positioning string:</b>
   * <ul>
   * <li><code>last</code> - place an element to the end of a list</li>
   * <li><code>first</code> - place an element to the beginning of a list</li>
   * <li><code>index:3</code> - place an element at the provided index 3 in a
   * list</li>
   * <li><code>before:org.eclipse.test.id</code> - place an element in a list
   * in front of the element with the ID "org.eclipse.test.id"</li>
   * <li><code>after:org.eclipse.test.id</code> - place an element in a list
   * after the element with the ID "org.eclipse.test.id"</li>
   * </ul>
   * </p>
   *
   * @param positionInfo
   *          the positioning string
   * @return a {@link PositionInfo} which holds all the data mentioned in the
   *         positioning string, or <code>null</code> if the positioning
   *         string doesn't hold a positioning information
   */
  public static PositionInfo parse(String positionInfo) {
    Position position = Position.find(positionInfo);
    if (position != null) {
      switch (position) {
        case FIRST:
          return PositionInfo.FIRST;

        case LAST:
          return PositionInfo.LAST;

        default:
          return new PositionInfo(position, positionInfo.substring(position.prefix.length()));
      }
    }

    return null;
  }

  @Override
  public String toString() {
    StringBuilder back = new StringBuilder(position.prefix);
    if (positionReference != null) {
      back.append(positionReference);
    }
    return back.toString();
  }
}