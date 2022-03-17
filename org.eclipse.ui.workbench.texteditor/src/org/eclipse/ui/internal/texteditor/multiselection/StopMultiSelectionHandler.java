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

/**
 * Handler to stop multi-selection mode. If more than one selection is active,
 * all selections are revoked, and the caret is positioned at position of the
 * first caret.
 */
public class StopMultiSelectionHandler extends AbstractMultiSelectionHandler {

	@Override
	public void execute() throws ExecutionException {
		if (isMultiSelectionActive()) {
			stopMultiSelection();
		}
	}

	private void stopMultiSelection() throws ExecutionException {
		int caretOffset = getCaretOffset();
		selectRegion(offsetAsCaretRegion(caretOffset));
		setAnchorRegion(null);
	}
}
