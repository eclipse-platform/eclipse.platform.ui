/*******************************************************************************
 * Copyright (c) 2022 Dirk Steinkamp
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Steinkamp <dirk.steinkamp@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.multiselection;

import org.eclipse.core.commands.ExecutionException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Handler to change the current set of multi carets/selections upwards. This
 * might either mean to add a caret/selection above by adding a new
 * caret/selection at the same line offset in the previous line, or reduce the
 * number of carets/selections by removing the last caret/selection range. This
 * depends on the selection a multi caret/selection command was invoked with the
 * first time -- that selection is remembered as an "anchor" to which successive
 * calls are related as reference selection.<br>
 */
public class MultiCaretUpHandler extends AbstractMultiSelectionHandler {

	@Override
	public void execute() throws ExecutionException {
		if (selectionIsBelowAnchorRegion()) {
			removeLastRegionFromSelection();
		} else {
			extendSelectionWithSamePositionInPreviousLine();
		}
	}

	private void extendSelectionWithSamePositionInPreviousLine() throws ExecutionException {
		IRegion[] regions = getSelectedRegions();
		if (regions == null || regions.length == 0) {
			return;
		}
		try {
			IRegion firstRegion = regions[0];
			int newOffset = offsetInPreviousLine(firstRegion.getOffset());
			IRegion previousLineRegion = createRegionIfValid(newOffset, firstRegion.getLength());
			selectRegions(addRegion(regions, previousLineRegion));
		} catch (BadLocationException e) {
			throw new ExecutionException("Internal error in extendSelectionWithSamePositionInPreviousLine", e);
		}
	}

	private void removeLastRegionFromSelection() {
			selectRegions(removeLastRegionButOne(getSelectedRegions()));
	}
}
