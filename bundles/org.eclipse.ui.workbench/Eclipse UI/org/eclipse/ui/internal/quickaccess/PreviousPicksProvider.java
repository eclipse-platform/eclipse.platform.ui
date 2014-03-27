/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Hochstein (Freescale) - Bug 393703 - NotHandledException selecting inactive command under 'Previous Choices' in Quick access
 ******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

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
		return previousPicksList.toArray(new QuickAccessElement[previousPicksList.size()]);
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
