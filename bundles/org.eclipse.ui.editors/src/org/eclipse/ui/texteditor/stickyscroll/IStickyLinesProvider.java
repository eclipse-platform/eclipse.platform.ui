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
package org.eclipse.ui.texteditor.stickyscroll;

import java.util.List;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IEditorPart;

/**
 * A sticky lines provider calculates the sticky lines for a given source
 * viewer. The sticky lines will be displayed in the top area of the editor.
 * 
 * since 3.20
 */
public interface IStickyLinesProvider {

	/**
	 * Calculate the sticky lines for the source code of the given sourceViewer. Specific
	 * properties, such as the <code>tabWidht</code>, can be retrieved from the
	 * <code>properties</code>.
	 * 
	 * @param sourceViewer The source viewer containing the source code and gives access to the text
	 *            widget
	 * @param lineNumber The line number to calculate the sticky lines for
	 * @param properties Properties for additional information
	 * @return The list of sticky lines to show
	 */
	public List<IStickyLine> getStickyLines(ISourceViewer sourceViewer, int lineNumber, StickyLinesProperties properties);

	/**
	 * Additional properties and access in order to calculate the sticky lines.
	 * 
	 * @param tabWith The with of a tab
	 * @param editor The editor for which the sticky lines should be provided
	 */
	record StickyLinesProperties(int tabWith, IEditorPart editor) {
	}

}
