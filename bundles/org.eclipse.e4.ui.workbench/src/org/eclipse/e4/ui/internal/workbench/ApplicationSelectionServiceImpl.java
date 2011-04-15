/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;

public class ApplicationSelectionServiceImpl implements ESelectionService {

	private MApplication application;

	@Inject
	ApplicationSelectionServiceImpl(MApplication application) {
		this.application = application;
	}

	private ESelectionService getActiveWindowService() {
		IEclipseContext activeWindowContext = application.getContext().getActiveChild();
		if (activeWindowContext == null) {
			throw new IllegalStateException("Application does not have an active window"); //$NON-NLS-1$
		}

		ESelectionService activeWindowSelectionService = activeWindowContext
				.get(ESelectionService.class);
		if (activeWindowSelectionService == null) {
			throw new IllegalStateException("Active window context is invalid"); //$NON-NLS-1$
		}

		return activeWindowSelectionService;
	}

	public void setSelection(Object selection) {
		throw new UnsupportedOperationException("Cannot set the selection of an application"); //$NON-NLS-1$
	}

	public Object getSelection() {
		return getActiveWindowService().getSelection();
	}

	public Object getSelection(String partId) {
		throw new UnsupportedOperationException(
				"Cannot retrieve the selection of a given part from the application"); //$NON-NLS-1$
	}

	public void addSelectionListener(ISelectionListener listener) {
		throw new UnsupportedOperationException("Cannot add global listeners to the application"); //$NON-NLS-1$
	}

	public void removeSelectionListener(ISelectionListener listener) {
		throw new UnsupportedOperationException("Cannot remove global listeners to the application"); //$NON-NLS-1$
	}

	public void addSelectionListener(String partId, ISelectionListener listener) {
		throw new UnsupportedOperationException("Cannot add global listeners to the application"); //$NON-NLS-1$
	}

	public void removeSelectionListener(String partId, ISelectionListener listener) {
		throw new UnsupportedOperationException("Cannot remove global listeners to the application"); //$NON-NLS-1$
	}

}
