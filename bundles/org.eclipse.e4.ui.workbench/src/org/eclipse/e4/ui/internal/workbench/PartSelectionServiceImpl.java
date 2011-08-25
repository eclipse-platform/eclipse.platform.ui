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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;

public class PartSelectionServiceImpl implements ESelectionService {

	private ListenerList genericListeners = new ListenerList();

	private Map<String, ListenerList> targetedListeners = new HashMap<String, ListenerList>();

	private MPart part;

	private IEclipseContext context;

	private EPartService partService;

	@Inject
	PartSelectionServiceImpl(MPart part, IEclipseContext context, EPartService partService) {
		this.part = part;
		this.context = context;
		this.partService = partService;
	}

	@PreDestroy
	void dispose() {
		genericListeners.clear();
		targetedListeners.clear();
	}

	private SelectionServiceImpl getWorkbenchWindowService() {
		MWindow tmp = context.get(MWindow.class);
		if (tmp == null) {
			throw new IllegalStateException("No workbench window found for this part"); //$NON-NLS-1$
		}

		MWindow window = null;
		while (tmp != null) {
			window = tmp;
			tmp = tmp.getContext().getParent().get(MWindow.class);
		}
		return (SelectionServiceImpl) window.getContext().get(ESelectionService.class);
	}

	public void setSelection(Object selection) {
		if (selection != null) {
			context.set(SelectionServiceImpl.OUT_SELECTION, selection);

			if (partService.getActivePart() == part) {
				getWorkbenchWindowService().internalSetSelection(selection);
			}
		} else {
			context.remove(SelectionServiceImpl.OUT_SELECTION);

			if (partService.getActivePart() == part) {
				getWorkbenchWindowService().internalSetSelection(selection);
			}
		}
	}

	public Object getSelection() {
		return getWorkbenchWindowService().getSelection();
	}

	public Object getSelection(String partId) {
		return getWorkbenchWindowService().getSelection(partId);
	}

	public void addSelectionListener(ISelectionListener listener) {
		genericListeners.add(listener);
		getWorkbenchWindowService().addSelectionListener(listener);
	}

	public void removeSelectionListener(ISelectionListener listener) {
		genericListeners.remove(listener);
		getWorkbenchWindowService().removeSelectionListener(listener);
	}

	public void addSelectionListener(String partId, ISelectionListener listener) {
		ListenerList listeners = targetedListeners.get(partId);
		if (listeners == null) {
			listeners = new ListenerList();
			targetedListeners.put(partId, listeners);
		}
		listeners.add(listener);

		getWorkbenchWindowService().addSelectionListener(partId, listener);
	}

	public void removeSelectionListener(String partId, ISelectionListener listener) {
		ListenerList listeners = targetedListeners.get(partId);
		if (listeners != null) {
			listeners.remove(listener);
		}

		getWorkbenchWindowService().removeSelectionListener(partId, listener);
	}

}
