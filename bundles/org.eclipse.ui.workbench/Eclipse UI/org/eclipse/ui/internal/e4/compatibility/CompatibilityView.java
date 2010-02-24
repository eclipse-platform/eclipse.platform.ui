/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import javax.inject.Inject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

public class CompatibilityView extends CompatibilityPart {

	private ViewReference reference;

	@Inject
	CompatibilityView(MPart part, ViewReference ref) {
		super(part);
		reference = ref;
	}

	// FIXME: remove me when bug 299760 is fixed
	void doSave(@Optional IProgressMonitor monitor) {
		super.doSave(monitor);
	}

	public IViewPart getView() {
		return (IViewPart) getPart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#getReference()
	 */
	@Override
	public IWorkbenchPartReference getReference() {
		return reference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#createPartControl
	 * (org.eclipse.ui.IWorkbenchPart, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createPartControl(IWorkbenchPart legacyPart, Composite parent) {
		super.createPartControl(legacyPart, parent);

		// Construct the toolbar
		IToolBarManager tbm = ((ViewPart) legacyPart).getViewSite().getActionBars()
				.getToolBarManager();
		if (tbm.getItems().length > 0) {
			Control partCtrl = (Control) part.getWidget();
			partCtrl.setData("legacyTBM", tbm); //$NON-NLS-1$
			MToolBar toolbar = part.getToolbar();
			if (toolbar == null) {
				toolbar = MApplicationFactory.eINSTANCE.createToolBar();

				// HACK!! Identifies this to the TB renderer
				toolbar.getTags().add("LegacyTB"); //$NON-NLS-1$
				part.setToolbar(toolbar);
			}
		}
	}
}
