/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Hochstein (Freescale) - Bug 393703 - NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 ******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.quickaccess.QuickAccessElement;

class PreviousPicksProvider extends QuickAccessProvider {

	private List<QuickAccessElement> previousPicksList;

	PreviousPicksProvider(List<QuickAccessElement> previousPicksList) {
		this.previousPicksList = previousPicksList;
	}

	@Override
	public QuickAccessElement getElementForId(String id) {
		return null;
	}

	@Override
	public QuickAccessElement[] getElements() {
		// If the list is being restored, it may contain null elements
		return previousPicksList.stream().filter(Objects::nonNull).collect(Collectors.toList())
				.toArray(new QuickAccessElement[0]);
	}

	@Override
	public QuickAccessElement[] getElementsSorted() {
		return getElements();
	}

	@Override
	public String getId() {
		return "org.eclipse.ui.previousPicks"; //$NON-NLS-1$
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
	}

	@Override
	public String getName() {
		return QuickAccessMessages.QuickAccess_Previous;
	}

	@Override
	protected void doReset() {
		// operation not applicable for this provider
	}

	@Override
	public boolean isAlwaysPresent() {
		return true;
	}
}
