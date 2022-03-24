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

import org.eclipse.jface.text.IRegion;

/**
 * Handler to change the current multi selection upwards. This might either mean
 * to extend the selection by adding a match above, or shrink the selection by
 * removing the last selection range. This depends on the selection a multi
 * caret/selection command was invoked with the first time -- that selection is
 * remembered as an "anchor" to which successive calls are related as reference
 * selection.<br>
 * If no word is selected, an implicit selection of the word under the cursor is
 * performed.
 */
public class MultiSelectionUpHandler extends AbstractMultiSelectionHandler {

	@Override
	public void execute() throws ExecutionException {
		if (nothingSelected()) {
			selectIdentifierUnderCaret();
		} else if (selectionIsBelowAnchorRegion()) {
			removeLastRegionFromSelection();
		} else {
			extendSelectionToPreviousMatch();
		}
	}

	private void extendSelectionToPreviousMatch() throws ExecutionException {
		if (allRegionsHaveSameText()) {
			IRegion[] regions = getSelectedRegions();
			IRegion nextMatch = findPreviousMatch(regions[0]);
			selectRegions(addRegion(regions, nextMatch));
		}
	}

	private void removeLastRegionFromSelection() {
		if (allRegionsHaveSameText()) {
			selectRegions(removeLastRegionButOne(getSelectedRegions()));
		}
	}
}
