/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

/**
 * 
 * A record representing a sticky line containing the text to display, and line number. It serves as
 * an abstraction to represent sticky line for sticky scrolling.
 * 
 * @param text the text of the corresponding sticky line
 * @param lineNumber the specific line number of the sticky line
 */
public record StickyLine(String text, int lineNumber) {
}
