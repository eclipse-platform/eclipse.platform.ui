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
 * All the possible positioning values which can be used to contribute
 * elements into the wanted place of a list.
 *
 * @author René Brandstetter
 */
public enum Position {
  /** Add an element to the end of a list (absolute positioning). */
  LAST("last"),

  /** Add an element at the beginning of a list (absolute positioning). */
  FIRST("first"),

  /** Add an element before another named element (relative positioning). */
  BEFORE("before:"),

  /** Add an element after a named element (relative positioning). */
  AFTER("after:"),

  /** Add an element at a specific index (absolute positioning). */
  INDEX("index:");

  /** The prefix of the enum which is used in the positioning string. */
  final String prefix;

  private Position(String prefix) {
    assert prefix != null : "Prefix required!";
    this.prefix = prefix;
  }

  /**
   * Find the {@link Position} enum value used in the given positioning
   * string.
   *
   * @param positionInfo
   *          the positioning string (can be <code>null</code>, which would
   *          result in <code>null</code>)
   * @return the {@link Position} which is mentioned in the positioning
   *         string, or <code>null</code> if none can be found
   */
  public static final Position find(String positionInfo) {
    if (positionInfo == null || positionInfo.length() <= 0)
      return null;

    for (Position position : Position.values()) {
      if (positionInfo.startsWith(position.prefix))
        return position;
    }

    return null;
  }
}