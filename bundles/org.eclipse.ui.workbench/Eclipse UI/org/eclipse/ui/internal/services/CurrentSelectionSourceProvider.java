/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.DetachedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.services.IServiceLocator;

/**
 * <p>
 * This listens to changes to the current selection, and propagates them through
 * the <code>ISourceProvider</code> framework (a common language in which
 * events are communicated to expression-based services).
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class CurrentSelectionSourceProvider extends
		AbstractSourceProvider implements INullSelectionListener {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ISources.ACTIVE_CURRENT_SELECTION_NAME };

	private final Listener shellListener = new Listener() {
		public void handleEvent(Event event) {
			if (!(event.widget instanceof Shell)) {
				return;
			}
			switch (event.type) {
			case SWT.Activate:
				IWorkbenchWindow window = null;
				if (event.widget.getData() instanceof WorkbenchWindow) {
					window = (IWorkbenchWindow) event.widget.getData();
				} else if (event.widget.getData() instanceof DetachedWindow) {
					window = ((DetachedWindow) event.widget.getData())
							.getWorkbenchPage().getWorkbenchWindow();
				}
				updateWindows(window);
				break;
			}
		}
	};

	/**
	 * The workbench on which this source provider is acting. This value is
	 * never <code>null</code>.
	 */
	private IWorkbench workbench;

	private IWorkbenchWindow lastWindow = null;

	public final void dispose() {
		workbench.getDisplay().removeFilter(SWT.Activate, shellListener);
	}

	public final Map getCurrentState() {
		final Map currentState = new TreeMap();
		final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window != null) {
			final ISelectionService service = window.getSelectionService();
			final ISelection selection = service.getSelection();
			currentState.put(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
		} else {
			currentState.put(ISources.ACTIVE_CURRENT_SELECTION_NAME, null);
		}
		return currentState;
	}

	public final String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	public final void selectionChanged(final IWorkbenchPart part,
			final ISelection selection) {
		if (DEBUG) {
			logDebuggingInfo("Selection changed to " + selection); //$NON-NLS-1$
		}

		fireSourceChanged(ISources.ACTIVE_CURRENT_SELECTION,
				ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
	}

	private final void updateWindows(IWorkbenchWindow newWindow) {
		if (lastWindow == newWindow) {
			return;
		}

		ISelection selection = null;
		if (lastWindow != null) {
			lastWindow.getSelectionService().removeSelectionListener(
					CurrentSelectionSourceProvider.this);
		}
		if (newWindow != null) {
			newWindow.getSelectionService().addSelectionListener(
					CurrentSelectionSourceProvider.this);
			selection = newWindow.getSelectionService().getSelection();
		}
		selectionChanged(null, selection);
		lastWindow = newWindow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.AbstractSourceProvider#initializeSource(org.eclipse.ui.services.IServiceLocator)
	 */
	public void initialize(IServiceLocator locator) {
		workbench = (IWorkbench) locator.getService(IWorkbench.class);
		workbench.getDisplay().addFilter(SWT.Activate, shellListener);
	}
}
