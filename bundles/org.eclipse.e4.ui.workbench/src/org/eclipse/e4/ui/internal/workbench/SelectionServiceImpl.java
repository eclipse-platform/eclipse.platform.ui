/*******************************************************************************
 * Copyright (c) 2012, 2016 IBM Corporation and others.
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

	@Override
	public void setSelection(Object selection) {
		context.set(SelectionAggregator.OUT_SELECTION, selection);
	}

	@Override
	public void setPostSelection(Object selection) {
		context.set(SelectionAggregator.OUT_POST_SELECTION, selection);
	}

	@Override
	public Object getSelection() {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			return aggregator.getSelection();
		}
		return null;
	}

	@Override
	public Object getSelection(String partId) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			return aggregator.getSelection(partId);
		}
		return null;
	}

	@Override
	public void addSelectionListener(ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.addSelectionListener(listener);
		}
	}

	@Override
	public void removeSelectionListener(ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.removeSelectionListener(listener);
		}
	}

	@Override
	public void addSelectionListener(String partId, ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.addSelectionListener(partId, listener);
		}
	}

	@Override
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.removeSelectionListener(partId, listener);
		}
	}

	@Override
	public void addPostSelectionListener(ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.addPostSelectionListener(listener);
		}
	}

	@Override
	public void removePostSelectionListener(ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.removePostSelectionListener(listener);
		}
	}

	@Override
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.addPostSelectionListener(partId, listener);
		}
	}

	@Override
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		SelectionAggregator aggregator = getServiceAggregator();
		if (aggregator != null) {
			aggregator.removePostSelectionListener(partId, listener);
		}
	}

	private SelectionAggregator getServiceAggregator() {
		SelectionAggregator aggregator = context.get(SelectionAggregator.class);
		if (aggregator != null)
			return aggregator;
		MApplication app = context.get(MApplication.class);
		if (app == null)
			return null;
		MWindow selectedWindow = app.getSelectedElement();
		IEclipseContext windowContext = selectedWindow.getContext();
		if (windowContext == null)
			return null;
		return windowContext.get(SelectionAggregator.class);
	}
}
