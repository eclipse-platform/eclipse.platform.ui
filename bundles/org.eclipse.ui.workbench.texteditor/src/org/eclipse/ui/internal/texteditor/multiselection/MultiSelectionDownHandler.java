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
 * Handler to change the current multi selection downwards. This might either
 * mean to extend the selection by adding a match below, or shrink the selection
 * by removing the first selection range. This depends on the selection a multi
 * caret/selection command was invoked with the first time -- that selection is
 * remembered as an "anchor" to which successive calls are related as reference
 * selection.<br>
 * If no word is selected, an implicit selection of the word under the cursor is
 * performed.
 */
public class MultiSelectionDownHandler extends AbstractMultiSelectionHandler {

	@Override
	public void execute() throws ExecutionException {
		if (nothingSelected()) {
			selectIdentifierUnderCaret();
		} else if (selectionIsAboveAnchorRegion()) {
			removeFirstRegionFromSelection();
		} else {
			extendSelectionToNextMatch();
		}
	}

	private void extendSelectionToNextMatch() throws ExecutionException {
		if (allRegionsHaveSameText()) {
			IRegion[] regions = getSelectedRegions();
			IRegion nextMatch = findNextMatch(regions[regions.length - 1]);
			selectRegions(addRegion(regions, nextMatch));
		}
	}

	private void removeFirstRegionFromSelection() {
		if (allRegionsHaveSameText()) {
			selectRegions(removeFirstRegionButOne(getSelectedRegions()));
		}
	}
}
