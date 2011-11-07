/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.e4.compatibility.ActionBars;

/**
 * A view container manages the services for a view.
 */
public class ViewSite extends PartSite implements IViewSite {
    
	public ViewSite(MPart model, IWorkbenchPart part, IWorkbenchPartReference ref,
			IConfigurationElement element) {
		super(model, part, ref, element);
		setActionBars(new ActionBars(((WorkbenchPage) getPage()).getActionBars(), serviceLocator,
				model));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewSite#getSecondaryId()
	 */
	public String getSecondaryId() {
		for (String tag : model.getTags()) {
			if (tag.startsWith(WorkbenchPage.SECONDARY_ID_HEADER)) {
				return tag.substring(WorkbenchPage.SECONDARY_ID_HEADER.length());
			}

		}
		return null;
	}

	@Override
	public void dispose() {
		getActionBars().getMenuManager().dispose();
		super.dispose();
	}
}
