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
 * Handler to extend the current selection to all found matches in the document.
 * If nothing is selected, an implicit selection of the word under the cursor is
 * performed and the selection performed with this.
 */
public class AddAllMatchesToMultiSelectionHandler extends AbstractMultiSelectionHandler {

	@Override
	public void execute() throws ExecutionException {
		if (nothingSelected()) {
			selectIdentifierUnderCaret();
		}
		extendSelectionToAllMatches();
	}

	private void extendSelectionToAllMatches() throws ExecutionException {
		if (allRegionsHaveSameText()) {
			IRegion[] regions = getSelectedRegions();
			selectRegions(findAllMatches(regions[0]));
		}
	}
}
