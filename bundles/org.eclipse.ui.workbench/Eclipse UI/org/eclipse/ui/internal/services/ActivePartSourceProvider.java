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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Provides notifications when the active part changes.
 * 
 * @since 3.1
 */
public class ActivePartSourceProvider extends AbstractSourceProvider {

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] {
			ISources.ACTIVE_EDITOR_ID_NAME, ISources.ACTIVE_EDITOR_NAME,
			ISources.ACTIVE_PART_ID_NAME, ISources.ACTIVE_PART_NAME,
			ISources.ACTIVE_SITE_NAME, ISources.SHOW_IN_SELECTION,
			ISources.SHOW_IN_INPUT };

	/**
	 * The last active editor part seen as active by this provider. This value
	 * may be <code>null</code> if there is no currently active editor.
	 */
	private IEditorPart lastActiveEditor = null;

	/**
	 * The last active editor id seen as active by this provider. This value may
	 * be <code>null</code> if there is no currently active editor.
	 */
	private String lastActiveEditorId = null;

	/**
	 * The last active part seen as active by this provider. This value may be
	 * <code>null</code> if there is no currently active part.
	 */
	private IWorkbenchPart lastActivePart = null;

	/**
	 * The last active part id seen as active by this provider. This value may
	 * be <code>null</code> if there is no currently active part.
	 */
	private String lastActivePartId = null;

	/**
	 * The last active part site seen by this provider. This value may be
	 * <code>null</code> if there is no currently active site.
	 */
	private IWorkbenchPartSite lastActivePartSite = null;

	private Object lastShowInInput = null;
	private ISelection lastShowInSelection = null;

	private final IPartListener partListener = new IPartListener() {

		public final void partActivated(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partBroughtToTop(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partClosed(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partDeactivated(final IWorkbenchPart part) {
			checkActivePart();
		}

		public final void partOpened(final IWorkbenchPart part) {
			checkActivePart();
		}

	};

	private final IWindowListener windowListener = new IWindowListener() {

		public final void windowActivated(final IWorkbenchWindow window) {
			checkActivePart();
		}

		public final void windowClosed(final IWorkbenchWindow window) {
			if (window != null) {
				window.getPartService().removePartListener(partListener);
			}
			checkActivePart();
		}

		public final void windowDeactivated(final IWorkbenchWindow window) {
			checkActivePart();
		}

		public final void windowOpened(final IWorkbenchWindow window) {
			if (window != null) {
				window.getPartService().addPartListener(partListener);
			}
			checkActivePart();
		}

	};

	/**
	 * The workbench on which this source provider will act.
	 */
	private IWorkbench workbench;

	public final void checkActivePart() {
		final Map currentState = getCurrentState();
		int sources = 0;

		// Figure out what was changed.
		final Object newActivePart = currentState
				.get(ISources.ACTIVE_PART_NAME);
		if (!Util.equals(newActivePart, lastActivePart)) {
			sources |= ISources.ACTIVE_PART;
			lastActivePart = (IWorkbenchPart) newActivePart;
		}
		final Object newActivePartId = currentState
				.get(ISources.ACTIVE_PART_ID_NAME);
		if (!Util.equals(newActivePartId, lastActivePartId)) {
			sources |= ISources.ACTIVE_PART_ID;
			lastActivePartId = (String) newActivePartId;
		}
		final Object newActivePartSite = currentState
				.get(ISources.ACTIVE_SITE_NAME);
		if (!Util.equals(newActivePartSite, lastActivePartSite)) {
			sources |= ISources.ACTIVE_SITE;
			lastActivePartSite = (IWorkbenchPartSite) newActivePartSite;
		}
		final Object newShowInInput = currentState.get(ISources.SHOW_IN_INPUT);
		if (!Util.equals(newShowInInput, lastShowInInput)) {
			sources |= ISources.ACTIVE_SITE;
			lastShowInInput = newShowInInput;
		}
		final Object newShowInSelection = currentState
				.get(ISources.SHOW_IN_SELECTION);
		if (!Util.equals(newShowInSelection, lastShowInSelection)) {
			sources |= ISources.ACTIVE_SITE;
			lastShowInSelection = (ISelection) newShowInSelection;
		}
		final Object newActiveEditor = currentState
				.get(ISources.ACTIVE_EDITOR_NAME);
		if (!Util.equals(newActiveEditor, lastActiveEditor)) {
			sources |= ISources.ACTIVE_EDITOR;
			lastActiveEditor = (IEditorPart) newActiveEditor;
		}
		final Object newActiveEditorId = currentState
				.get(ISources.ACTIVE_EDITOR_ID_NAME);
		if (!Util.equals(newActiveEditorId, lastActiveEditorId)) {
			sources |= ISources.ACTIVE_EDITOR_ID;
			lastActiveEditorId = (String) newActiveEditorId;
		}

		// Fire the event, if something has changed.
		if (sources != 0) {
			if (DEBUG) {
				if ((sources & ISources.ACTIVE_PART) != 0) {
					logDebuggingInfo("Active part changed to " //$NON-NLS-1$
							+ lastActivePart);
				}
				if ((sources & ISources.ACTIVE_PART_ID) != 0) {
					logDebuggingInfo("Active part id changed to " //$NON-NLS-1$
							+ lastActivePartId);
				}
				if ((sources & ISources.ACTIVE_SITE) != 0) {
					logDebuggingInfo("Active site changed to " //$NON-NLS-1$
							+ lastActivePartSite);
				}
				if ((sources & ISources.ACTIVE_EDITOR) != 0) {
					logDebuggingInfo("Active editor changed to " //$NON-NLS-1$
							+ lastActiveEditor);
				}
				if ((sources & ISources.ACTIVE_EDITOR_ID) != 0) {
					logDebuggingInfo("Active editor id changed to " //$NON-NLS-1$
							+ lastActiveEditorId);
				}
			}
			fireSourceChanged(sources, currentState);
		}
	}

	private IShowInSource getShowInSource(IWorkbenchPart sourcePart) {
		return (IShowInSource) Util.getAdapter(sourcePart, IShowInSource.class);
	}

	private ShowInContext getContext(IWorkbenchPart sourcePart) {
		IShowInSource source = getShowInSource(sourcePart);
		if (source != null) {
			ShowInContext context = source.getShowInContext();
			if (context != null) {
				return context;
			}
		} else if (sourcePart instanceof IEditorPart) {
			Object input = ((IEditorPart) sourcePart).getEditorInput();
			ISelectionProvider sp = sourcePart.getSite().getSelectionProvider();
			ISelection sel = sp == null ? null : sp.getSelection();
			return new ShowInContext(input, sel);
		}
		return null;
	}

	public final void dispose() {
		workbench.removeWindowListener(windowListener);
	}

	public final Map getCurrentState() {
		final Map currentState = new HashMap(7);
		currentState.put(ISources.ACTIVE_SITE_NAME, null);
		currentState.put(ISources.ACTIVE_PART_NAME, null);
		currentState.put(ISources.ACTIVE_PART_ID_NAME, null);
		currentState.put(ISources.ACTIVE_EDITOR_NAME, null);
		currentState.put(ISources.ACTIVE_EDITOR_ID_NAME, null);
		currentState.put(ISources.SHOW_IN_INPUT, null);
		currentState.put(ISources.SHOW_IN_SELECTION, null);

		final IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			final IWorkbenchPage activeWorkbenchPage = activeWorkbenchWindow
					.getActivePage();
			if (activeWorkbenchPage != null) {
				// Check the active workbench part.
				final IWorkbenchPart newActivePart = activeWorkbenchPage
						.getActivePart();
				currentState.put(ISources.ACTIVE_PART_NAME, newActivePart);
				if (newActivePart != null) {
					final IWorkbenchPartSite activeWorkbenchPartSite = newActivePart
							.getSite();
					currentState.put(ISources.ACTIVE_SITE_NAME,
							activeWorkbenchPartSite);
					if (activeWorkbenchPartSite != null) {
						final String newActivePartId = activeWorkbenchPartSite
								.getId();
						currentState.put(ISources.ACTIVE_PART_ID_NAME,
								newActivePartId);
					}
					ShowInContext context = getContext(newActivePart);
					if (context != null) {
						currentState.put(ISources.SHOW_IN_INPUT, context
								.getInput());
						currentState.put(ISources.SHOW_IN_SELECTION, context
								.getSelection());
					}
				}

				// Check the active editor part.
				final IEditorPart newActiveEditor = activeWorkbenchPage
						.getActiveEditor();
				currentState.put(ISources.ACTIVE_EDITOR_NAME, newActiveEditor);
				if (newActiveEditor != null) {
					final IEditorSite activeEditorSite = newActiveEditor
							.getEditorSite();
					if (activeEditorSite != null) {
						final String newActiveEditorId = activeEditorSite
								.getId();
						currentState.put(ISources.ACTIVE_EDITOR_ID_NAME,
								newActiveEditorId);
					}
				}
			}
		}

		return currentState;
	}

	public final String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	public void initialize(IServiceLocator locator) {
		workbench = (IWorkbench) locator.getService(IWorkbench.class);
		workbench.addWindowListener(windowListener);
	}
}
