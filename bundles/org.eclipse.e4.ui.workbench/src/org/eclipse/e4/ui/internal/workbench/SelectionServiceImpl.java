/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;

public class SelectionServiceImpl implements ESelectionService {

	private IEclipseContext context;

	@Inject
	SelectionServiceImpl(IEclipseContext context) {
		this.context = context;
	}

	public void setSelection(Object selection) {
		context.set(SelectionAggregator.OUT_SELECTION, selection);
	}

	public void setPostSelection(Object selection) {
		context.set(SelectionAggregator.OUT_POST_SELECTION, selection);
	}

	public Object getSelection() {
		return getServiceAggregator().getSelection();
	}

	public Object getSelection(String partId) {
		return getServiceAggregator().getSelection(partId);
	}

	public void addSelectionListener(ISelectionListener listener) {
		getServiceAggregator().addSelectionListener(listener);
	}

	public void removeSelectionListener(ISelectionListener listener) {
		getServiceAggregator().removeSelectionListener(listener);
	}

	public void addSelectionListener(String partId, ISelectionListener listener) {
		getServiceAggregator().addSelectionListener(partId, listener);
	}

	public void removeSelectionListener(String partId, ISelectionListener listener) {
		getServiceAggregator().removeSelectionListener(partId, listener);
	}

	public void addPostSelectionListener(ISelectionListener listener) {
		getServiceAggregator().addPostSelectionListener(listener);
	}

	public void removePostSelectionListener(ISelectionListener listener) {
		getServiceAggregator().removePostSelectionListener(listener);
	}

	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		getServiceAggregator().addPostSelectionListener(partId, listener);
	}

	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		getServiceAggregator().removePostSelectionListener(partId, listener);
	}

	private SelectionAggregator getServiceAggregator() {
		SelectionAggregator aggregator = context.get(SelectionAggregator.class);
		if (aggregator != null)
			return aggregator;
		MApplication app = context.get(MApplication.class);
		if (app == null)
			return null;
		MWindow selectedWindow = app.getSelectedElement();
		return selectedWindow.getContext().get(SelectionAggregator.class);
	}
}
