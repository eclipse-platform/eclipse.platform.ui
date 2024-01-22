/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.e4.compatibility.ActionBars;

/**
 * A view container manages the services for a view.
 */
public class ViewSite extends PartSite implements IViewSite {

	public ViewSite(MPart model, IWorkbenchPart part, IWorkbenchPartReference ref, IConfigurationElement element) {
		super(model, part, ref, element);
		initializeDefaultServices();
	}

	private void initializeDefaultServices() {
		setActionBars(new ActionBars(((WorkbenchPage) getPage()).getActionBars(), serviceLocator, model));
		serviceLocator.registerService(IViewPart.class, getPart());
	}

	@Override
	public String getSecondaryId() {
		MPart part = getModel();

		int colonIndex = part.getElementId().indexOf(':');
		if (colonIndex == -1 || colonIndex == (part.getElementId().length() - 1))
			return null;

		return part.getElementId().substring(colonIndex + 1);
	}

	@Override
	public void dispose() {
		final IActionBars actionBars = getActionBars();
		if (actionBars instanceof SubActionBars) {
			((SubActionBars) actionBars).dispose();
		}
		super.dispose();
	}
}
