/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;

public class ViewSite extends WorkbenchPartSite implements IViewSite {

	private IActionBars actionBars;

	ViewSite(MPart model, IWorkbenchPart part, IConfigurationElement element) {
		super(model, part, element);
		actionBars = new ActionBars(model);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewSite#getActionBars()
	 */
	public IActionBars getActionBars() {
		return actionBars;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewSite#getSecondaryId()
	 */
	public String getSecondaryId() {
		// FIXME compat getSecondaryId
		throw new UnsupportedOperationException();
	}

}
