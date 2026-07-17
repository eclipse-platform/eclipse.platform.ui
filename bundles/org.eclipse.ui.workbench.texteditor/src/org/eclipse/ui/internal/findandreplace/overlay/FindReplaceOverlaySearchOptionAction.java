/*******************************************************************************
 * Copyright (c) 2026 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.findandreplace.overlay;

import org.eclipse.ui.internal.findandreplace.IFindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.SearchOptions;

class FindReplaceOverlaySearchOptionAction extends FindReplaceOverlayAction {

	private final SearchOptions searchOption;

	private final IFindReplaceLogic findReplaceLogic;

	FindReplaceOverlaySearchOptionAction(SearchOptions searchOption, IFindReplaceLogic findReplaceLogic,
			String commandId) {
		super(() -> findReplaceLogic.toggle(searchOption), commandId);
		this.searchOption = searchOption;
		this.findReplaceLogic = findReplaceLogic;
	}

	SearchOptions getSearchOption() {
		return searchOption;
	}

	IFindReplaceLogic getFindReplaceLogic() {
		return findReplaceLogic;
	}

}
