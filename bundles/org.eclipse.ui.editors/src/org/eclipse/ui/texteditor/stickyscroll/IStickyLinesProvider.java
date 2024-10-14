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

import org.eclipse.swt.custom.StyledText;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IEditorPart;

/**
 * A sticky lines provider calculates the sticky lines for a given source viewer. The sticky lines
 * will be displayed in the top area of the editor.
 * 
 * since 3.19
 */
public interface IStickyLinesProvider {

	/**
	 * Calculate the sticky lines for the source code of the given textWidget. Specific properties,
	 * such as the <code>tabWidht</code> can be retrieved from the <code>properties</code>. To get
	 * access to additional information, the source viewer can be used.
	 * 
	 * @param textWidget The text widget containing the source code
	 * @param lineNumber The line number to calculate the sticky lines for
	 * @param properties Properties for additional information
	 * @return The list of sticky lines to show
	 * 
	 * @see ISourceViewer#getTopIndex()
	 * @see ISourceViewer#getTextWidget()
	 * @see StyledText#getTopIndex()
	 */
	public List<IStickyLine> getStickyLines(StyledText textWidget, int lineNumber, StickyLinesProperties properties);

	/**
	 * Additional properties and access in order to calculate the sticky lines.
	 * 
	 * @param tabWith The with of a tab
	 * @param sourceViewer The sourceViewer to access additional information
	 */
	record StickyLinesProperties(int tabWith, ISourceViewer sourceViewer, IEditorPart editor) {
	}

}
