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

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.DetachedWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.5
 * @author Prakash G.R.
 */
public class WorkbenchSourceProvider extends AbstractSourceProvider implements
		INullSelectionListener {

	private static final String STATUS_LINE_VIS = ISources.ACTIVE_WORKBENCH_WINDOW_NAME
			+ ".isStatusLineVisible"; //$NON-NLS-1$

	/**
	 * The names of the sources supported by this source provider.
	 */
	private static final String[] PROVIDED_SOURCE_NAMES = new String[] {
			ISources.ACTIVE_CURRENT_SELECTION_NAME,
			ISources.ACTIVE_EDITOR_ID_NAME, ISources.ACTIVE_EDITOR_NAME,
			ISources.ACTIVE_PART_ID_NAME, ISources.ACTIVE_PART_NAME,
			ISources.ACTIVE_SITE_NAME, ISources.SHOW_IN_SELECTION,
			ISources.SHOW_IN_INPUT, ISources.ACTIVE_SHELL_NAME,
			ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
			ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME,
			ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME,
			ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME,
			ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME,
			STATUS_LINE_VIS };

	private IWorkbench workbench;
	private IWorkbenchWindow lastWindow;
//	private IServiceLocator locator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.AbstractSourceProvider#initialize(org.eclipse.ui.services
	 * .IServiceLocator)
	 */
	public void initialize(IServiceLocator locator) {
//		this.locator = locator;
		super.initialize(locator);
		IWorkbenchLocationService wls = (IWorkbenchLocationService) locator
				.getService(IWorkbenchLocationService.class);
		workbench = wls.getWorkbench();
		workbench.addWindowListener(windowListener);
		lastWindow = workbench.getActiveWorkbenchWindow();
		display = workbench.getDisplay();
		display.addFilter(SWT.Activate, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#dispose()
	 */
	public void dispose() {
		if (lastWindow != null)
			lastWindow.getSelectionService().removeSelectionListener(this);
		workbench.removeWindowListener(windowListener);
		display.removeFilter(SWT.Activate, listener);
		hookListener(lastActiveWorkbenchWindow, null);
		lastActiveWorkbenchWindow = null;
		lastActiveWorkbenchWindowShell = null;
		lastActiveShell = null;
		lastWindow = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
	 */
	public String[] getProvidedSourceNames() {
		return PROVIDED_SOURCE_NAMES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
	 */
	public Map getCurrentState() {

		final Map currentState = new HashMap();

		updateActiveShell(currentState);
		updateActivePart(currentState);
		updateSelection(currentState);

		return currentState;
	}

	// Selection SourceProvider

	ISelection selection;

	private int updateSelection(final Map currentState) {
		int sources = 0;
		currentState.put(ISources.ACTIVE_CURRENT_SELECTION_NAME,
				IEvaluationContext.UNDEFINED_VARIABLE);
		Object object = currentState.get(ISources.ACTIVE_PART_NAME);
		if (object instanceof IWorkbenchPart) {
			IWorkbenchPart part = (IWorkbenchPart) object;
			if (part.getSite() != null
					&& part.getSite().getSelectionProvider() != null) {
				sources = ISources.ACTIVE_CURRENT_SELECTION;
				ISelection currentSelection = part.getSite()
						.getSelectionProvider()
						.getSelection();
				currentState.put(ISources.ACTIVE_CURRENT_SELECTION_NAME,
						currentSelection);
			}
		}
		return sources;
	}

	public final void selectionChanged(final IWorkbenchPart part,
			final ISelection newSelection) {

		if (Util.equals(selection, newSelection))
			return; // we have already handled the change

		selection = newSelection;

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
			lastWindow.getSelectionService().removeSelectionListener(this);
		}
		if (newWindow != null) {
			newWindow.getSelectionService().addSelectionListener(this);
			selection = newWindow.getSelectionService().getSelection();
		}
		selectionChanged(null, selection);
		lastWindow = newWindow;
	}

	// Active Part SourceProvider

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
		}

	};

	private IEditorInput lastEditorInput;
	
	public void handleCheck(Shell s) {
		if (s != lastActiveShell) {
			lastActiveShell = s;
			checkActivePart();
			IWorkbenchWindow window = null;
			if (s.getData() instanceof WorkbenchWindow) {
				window = (IWorkbenchWindow) s.getData();
			} else if (s.getData() instanceof DetachedWindow) {
				window = ((DetachedWindow) s.getData()).getWorkbenchPage()
						.getWorkbenchWindow();
			}
			updateWindows(window);

		}
	}

	public final void checkActivePart() {
		checkActivePart(false);
	}

	public final void checkActivePart(boolean updateShowInSelection) {
		Map currentState = new HashMap();
		updateActivePart(currentState, updateShowInSelection);

		int sources = 0;

		// Figure out what was changed.
		final Object newActivePart = currentState
				.get(ISources.ACTIVE_PART_NAME);
		if (!Util.equals(newActivePart, lastActivePart)) {
			sources |= ISources.ACTIVE_PART;
			if (newActivePart != IEvaluationContext.UNDEFINED_VARIABLE) {
				lastActivePart = (IWorkbenchPart) newActivePart;
			} else {
				lastActivePart = null;
			}
		}
		final Object newActivePartId = currentState
				.get(ISources.ACTIVE_PART_ID_NAME);
		if (!Util.equals(newActivePartId, lastActivePartId)) {
			sources |= ISources.ACTIVE_PART_ID;
			if (newActivePartId != IEvaluationContext.UNDEFINED_VARIABLE) {
				lastActivePartId = (String) newActivePartId;
			} else {
				lastActivePartId = null;
			}
		}
		final Object newActivePartSite = currentState
				.get(ISources.ACTIVE_SITE_NAME);
		if (!Util.equals(newActivePartSite, lastActivePartSite)) {
			sources |= ISources.ACTIVE_SITE;
			if (newActivePartSite != IEvaluationContext.UNDEFINED_VARIABLE) {
				lastActivePartSite = (IWorkbenchPartSite) newActivePartSite;
			} else {
				lastActivePartSite = null;
			}
		}
		final Object newShowInInput = currentState.get(ISources.SHOW_IN_INPUT);
		if (!Util.equals(newShowInInput, lastShowInInput)) {
			sources |= ISources.ACTIVE_SITE;
			lastShowInInput = newShowInInput;
		}
		if (currentState.get(ISources.SHOW_IN_SELECTION) != IEvaluationContext.UNDEFINED_VARIABLE) {
			sources |= ISources.ACTIVE_SITE;
		}
		Object newActiveEditor = currentState.get(ISources.ACTIVE_EDITOR_NAME);
		if (!Util.equals(newActiveEditor, lastActiveEditor)) {
			sources |= ISources.ACTIVE_EDITOR;
			newActiveEditor = (newActiveEditor == IEvaluationContext.UNDEFINED_VARIABLE ? null
					: newActiveEditor);
			hookListener(lastActiveEditor, (IEditorPart) newActiveEditor);
			lastActiveEditor = (IEditorPart) newActiveEditor;
		}
		Object newEditorInput = currentState.get(ISources.ACTIVE_EDITOR_INPUT_NAME);
		if (!Util.equals(newEditorInput, lastEditorInput)) {
			sources |= ISources.ACTIVE_EDITOR;
			if (newEditorInput != IEvaluationContext.UNDEFINED_VARIABLE) {
				lastEditorInput = (IEditorInput) newEditorInput;
			} else {
				lastEditorInput = null;
			}
		}
		final Object newActiveEditorId = currentState
				.get(ISources.ACTIVE_EDITOR_ID_NAME);
		if (!Util.equals(newActiveEditorId, lastActiveEditorId)) {
			sources |= ISources.ACTIVE_EDITOR_ID;
			if (newActiveEditorId != IEvaluationContext.UNDEFINED_VARIABLE) {
				lastActiveEditorId = (String) newActiveEditorId;
			} else {
				lastActiveEditorId = null;
			}
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
			sources |= updateSelection(currentState);
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

	private IWorkbenchWindow getActiveWindow() {
		final Shell newActiveShell = workbench.getDisplay().getActiveShell();
		final IContextService contextService = (IContextService) workbench
				.getService(IContextService.class);
		if (contextService != null) {
			final int shellType = contextService.getShellType(newActiveShell);
			if (shellType != IContextService.TYPE_DIALOG) {
				return workbench.getActiveWorkbenchWindow();
			}
		}
		return null;
	}

	private void updateActivePart(Map currentState) {
		updateActivePart(currentState, false);
	}

	private void updateActivePart(Map currentState, boolean updateShowInSelection) {
		currentState.put(ISources.ACTIVE_SITE_NAME,
				IEvaluationContext.UNDEFINED_VARIABLE);
		currentState.put(ISources.ACTIVE_PART_NAME,
				IEvaluationContext.UNDEFINED_VARIABLE);
		currentState.put(ISources.ACTIVE_PART_ID_NAME,
				IEvaluationContext.UNDEFINED_VARIABLE);
		currentState.put(ISources.ACTIVE_EDITOR_NAME,
				IEvaluationContext.UNDEFINED_VARIABLE);
		currentState.put(ISources.ACTIVE_EDITOR_ID_NAME,
				IEvaluationContext.UNDEFINED_VARIABLE);
		currentState.put(ISources.SHOW_IN_INPUT,
				IEvaluationContext.UNDEFINED_VARIABLE);
		currentState.put(ISources.SHOW_IN_SELECTION,
				IEvaluationContext.UNDEFINED_VARIABLE);

		final IWorkbenchWindow activeWorkbenchWindow = getActiveWindow();
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
						Object input = context.getInput();
						if (input != null) {
							currentState.put(ISources.SHOW_IN_INPUT, input);
						}
						if (updateShowInSelection) {
							ISelection selection = context.getSelection();
							if (selection != null) {
								currentState.put(ISources.SHOW_IN_SELECTION,
										selection);
							}
						}
					}
				}

				// Check the active editor part.
				final IEditorPart newActiveEditor = activeWorkbenchPage
						.getActiveEditor();
				currentState.put(ISources.ACTIVE_EDITOR_NAME, newActiveEditor);
				if (newActiveEditor != null) {
					currentState.put(ISources.ACTIVE_EDITOR_INPUT_NAME,
							newActiveEditor.getEditorInput());
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

	}

	// Active Part SourceProvider

	/**
	 * The display on which this provider is working.
	 */
	private Display display;

	/**
	 * The last shell seen as active by this provider. This value may be
	 * <code>null</code> if the last call to
	 * <code>Display.getActiveShell()</code> returned <code>null</code>.
	 */
	private Shell lastActiveShell = null;

	/**
	 * The last workbench window shell seen as active by this provider. This
	 * value may be <code>null</code> if the last call to
	 * <code>workbench.getActiveWorkbenchWindow()</code> returned
	 * <code>null</code>.
	 */
	private Shell lastActiveWorkbenchWindowShell = null;

	/**
	 * The last workbench window seen as active by this provider. This value may
	 * be null if the last call to
	 * <code>workbench.getActiveWorkbenchWindow()</code> returned
	 * <code>null</code>.
	 * 
	 * @since 3.3
	 */
	private WorkbenchWindow lastActiveWorkbenchWindow = null;

	/**
	 * The result of the last visibility check on the coolbar of the last active
	 * workbench window.
	 * 
	 * @since 3.3
	 */
	private Boolean lastCoolbarVisibility = Boolean.FALSE;

	/**
	 * The result of the last visibility check on the perspective bar of the
	 * last active workbench window.
	 * 
	 * @since 3.3
	 */
	private Boolean lastPerspectiveBarVisibility = Boolean.FALSE;

	/**
	 * The result of the last visibility check on the status line for the last
	 * workbench window.
	 * 
	 * @since 3.4
	 */
	private Boolean lastStatusLineVisibility = Boolean.FALSE;

	/**
	 * The last perspective id that was provided by this source.
	 * 
	 * @since 3.4
	 */
	private String lastPerspectiveId = null;

	/**
	 * The listener to individual window properties.
	 * 
	 * @since 3.3
	 */
	private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (WorkbenchWindow.PROP_COOLBAR_VISIBLE
					.equals(event.getProperty())) {
				Object newValue = event.getNewValue();
				if (newValue == null || !(newValue instanceof Boolean))
					return;
				if (!lastCoolbarVisibility.equals(newValue)) {
					fireSourceChanged(
							ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE,
							ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME,
							newValue);
					lastCoolbarVisibility = (Boolean) newValue;
				}
			} else if (WorkbenchWindow.PROP_PERSPECTIVEBAR_VISIBLE.equals(event
					.getProperty())) {
				Object newValue = event.getNewValue();
				if (newValue == null || !(newValue instanceof Boolean))
					return;
				if (!lastPerspectiveBarVisibility.equals(newValue)) {
					fireSourceChanged(
							ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE,
							ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME,
							newValue);
					lastPerspectiveBarVisibility = (Boolean) newValue;
				}
			} else if (WorkbenchWindow.PROP_STATUS_LINE_VISIBLE.equals(event
					.getProperty())) {
				Object newValue = event.getNewValue();
				if (newValue == null || !(newValue instanceof Boolean))
					return;
				if (!lastStatusLineVisibility.equals(newValue)) {
					fireSourceChanged(
							ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE,
							ISources.ACTIVE_WORKBENCH_WINDOW_NAME
									+ ".isStatusLineVisible", newValue); //$NON-NLS-1$
					lastStatusLineVisibility = (Boolean) newValue;
				}
			}
		}

	};

	IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page,
				IPerspectiveDescriptor perspective) {
			String id = perspective == null ? null : perspective.getId();
			if (Util.equals(lastPerspectiveId, id)) {
				return;
			}

			fireSourceChanged(ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE,
					ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME,
					id);

			lastPerspectiveId = id;
		}

		public void perspectiveChanged(IWorkbenchPage page,
				IPerspectiveDescriptor perspective, String changeId) {
		}
	};
	
	private IPropertyListener editorListener = new IPropertyListener() {
		public void propertyChanged(Object source, int propId) {
			if (propId == IEditorPart.PROP_INPUT) {
				handleInputChanged((IEditorPart) source);
			}
		}
	};


	/**
	 * The listener to shell activations on the display.
	 */
	private final Listener listener = new Listener() {
		/**
		 * Notifies all listeners that the source has changed.
		 */
		public final void handleEvent(final Event event) {
			if (!(event.widget instanceof Shell)) {
				if (DEBUG) {
					logDebuggingInfo("WSP: passOnEvent: " + event.widget); //$NON-NLS-1$
				}
				return;
			}

			if (DEBUG) {
				logDebuggingInfo("\tWSP:lastActiveShell: " + lastActiveShell); //$NON-NLS-1$
				logDebuggingInfo("\tWSP:lastActiveWorkbenchWindowShell" + lastActiveWorkbenchWindowShell); //$NON-NLS-1$
			}

			final Map currentState = getCurrentState();
			final Shell newActiveShell = (Shell) currentState
					.get(ISources.ACTIVE_SHELL_NAME);
			final WorkbenchWindow newActiveWorkbenchWindow = (WorkbenchWindow) currentState
					.get(ISources.ACTIVE_WORKBENCH_WINDOW_NAME);
			final Shell newActiveWorkbenchWindowShell = (Shell) currentState
					.get(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME);

			// dont update the coolbar/perspective bar visibility unless we're
			// processing a workbench window change
			final Boolean newCoolbarVisibility = newActiveWorkbenchWindow == null ? lastCoolbarVisibility
					: (newActiveWorkbenchWindow.getCoolBarVisible() ? Boolean.TRUE
							: Boolean.FALSE);
			final Boolean newPerspectiveBarVisibility = newActiveWorkbenchWindow == null ? lastPerspectiveBarVisibility
					: (newActiveWorkbenchWindow.getPerspectiveBarVisible() ? Boolean.TRUE
							: Boolean.FALSE);
			final Boolean newStatusLineVis = newActiveWorkbenchWindow == null ? lastStatusLineVisibility
					: (newActiveWorkbenchWindow.getStatusLineVisible() ? Boolean.TRUE
							: Boolean.FALSE);

			String perspectiveId = lastPerspectiveId;
			if (newActiveWorkbenchWindow != null) {
				IWorkbenchPage activePage = newActiveWorkbenchWindow
						.getActivePage();
				if (activePage != null) {
					IPerspectiveDescriptor perspective = activePage
							.getPerspective();
					if (perspective != null) {
						perspectiveId = perspective.getId();
					}
				}
			}

			// Figure out which variables have changed.
			final boolean shellChanged = newActiveShell != lastActiveShell;
			final boolean windowChanged = newActiveWorkbenchWindowShell != lastActiveWorkbenchWindowShell;
			final boolean coolbarChanged = newCoolbarVisibility != lastCoolbarVisibility;
			final boolean statusLineChanged = newStatusLineVis != lastStatusLineVisibility;

			final boolean perspectiveBarChanged = newPerspectiveBarVisibility != lastPerspectiveBarVisibility;
			final boolean perspectiveIdChanged = !Util.equals(
					lastPerspectiveId, perspectiveId);
			// Fire an event for those sources that have changed.
			if (shellChanged && windowChanged) {
				final Map sourceValuesByName = new HashMap(5);
				sourceValuesByName.put(ISources.ACTIVE_SHELL_NAME,
						newActiveShell);
				sourceValuesByName.put(ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
						newActiveWorkbenchWindow);
				sourceValuesByName.put(
						ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME,
						newActiveWorkbenchWindowShell);
				int sourceFlags = ISources.ACTIVE_SHELL
						| ISources.ACTIVE_WORKBENCH_WINDOW;

				if (coolbarChanged) {
					sourceValuesByName
							.put(
									ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME,
									newCoolbarVisibility);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}
				if (statusLineChanged) {
					sourceValuesByName.put(STATUS_LINE_VIS, newStatusLineVis);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}
				if (perspectiveBarChanged) {
					sourceValuesByName
							.put(
									ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME,
									newPerspectiveBarVisibility);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}
				if (perspectiveIdChanged) {
					sourceValuesByName
							.put(
									ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME,
									perspectiveId);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}

				if (DEBUG) {
					logDebuggingInfo("Active shell changed to " //$NON-NLS-1$
							+ newActiveShell);
					logDebuggingInfo("Active workbench window changed to " //$NON-NLS-1$
							+ newActiveWorkbenchWindow);
					logDebuggingInfo("Active workbench window shell changed to " //$NON-NLS-1$
							+ newActiveWorkbenchWindowShell);
					logDebuggingInfo("Active workbench window coolbar visibility " //$NON-NLS-1$
							+ newCoolbarVisibility);
					logDebuggingInfo("Active workbench window perspective bar visibility " //$NON-NLS-1$
							+ newPerspectiveBarVisibility);
					logDebuggingInfo("Active workbench window status line visibility " //$NON-NLS-1$
							+ newStatusLineVis);
				}

				fireSourceChanged(sourceFlags, sourceValuesByName);
				hookListener(lastActiveWorkbenchWindow,
						newActiveWorkbenchWindow);

			} else if (shellChanged) {
				if (DEBUG) {
					logDebuggingInfo("Active shell changed to " //$NON-NLS-1$
							+ newActiveShell);
				}
				fireSourceChanged(ISources.ACTIVE_SHELL,
						ISources.ACTIVE_SHELL_NAME, newActiveShell);
			} else if (windowChanged) {
				final Map sourceValuesByName = new HashMap(4);
				sourceValuesByName.put(ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
						newActiveWorkbenchWindow);
				sourceValuesByName.put(
						ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME,
						newActiveWorkbenchWindowShell);

				int sourceFlags = ISources.ACTIVE_SHELL
						| ISources.ACTIVE_WORKBENCH_WINDOW;

				if (coolbarChanged) {
					sourceValuesByName
							.put(
									ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME,
									newCoolbarVisibility);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}
				if (statusLineChanged) {
					sourceValuesByName.put(STATUS_LINE_VIS, newStatusLineVis);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}
				if (perspectiveBarChanged) {
					sourceValuesByName
							.put(
									ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME,
									newPerspectiveBarVisibility);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}
				if (perspectiveIdChanged) {
					sourceValuesByName
							.put(
									ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME,
									perspectiveId);
					sourceFlags |= ISources.ACTIVE_WORKBENCH_WINDOW_SUBORDINATE;
				}

				if (DEBUG) {
					logDebuggingInfo("Active workbench window changed to " //$NON-NLS-1$
							+ newActiveWorkbenchWindow);
					logDebuggingInfo("Active workbench window shell changed to " //$NON-NLS-1$
							+ newActiveWorkbenchWindowShell);
					logDebuggingInfo("Active workbench window coolbar visibility " //$NON-NLS-1$
							+ newCoolbarVisibility);
					logDebuggingInfo("Active workbench window perspective bar visibility " //$NON-NLS-1$
							+ newPerspectiveBarVisibility);
					logDebuggingInfo("Active workbench window status line visibility " //$NON-NLS-1$
							+ newStatusLineVis);
				}

				fireSourceChanged(sourceFlags, sourceValuesByName);
				hookListener(lastActiveWorkbenchWindow,
						newActiveWorkbenchWindow);
			}

			if (shellChanged || windowChanged) {
				checkOtherSources((Shell) event.widget);
			}

			// Update the member variables.
			lastActiveShell = newActiveShell;
			lastActiveWorkbenchWindowShell = newActiveWorkbenchWindowShell;
			lastActiveWorkbenchWindow = newActiveWorkbenchWindow;
			lastCoolbarVisibility = newCoolbarVisibility;
			lastStatusLineVisibility = newStatusLineVis;
			lastPerspectiveBarVisibility = newPerspectiveBarVisibility;
			lastPerspectiveId = perspectiveId;
		}
	};


	protected void checkOtherSources(Shell s) {
		handleCheck(s);
	}

	protected void handleInputChanged(IEditorPart editor) {
		IEditorInput newInput = editor.getEditorInput();
		if (!Util.equals(newInput, lastEditorInput)) {
			fireSourceChanged(ISources.ACTIVE_EDITOR,
					ISources.ACTIVE_EDITOR_INPUT_NAME,
					newInput == null ? IEvaluationContext.UNDEFINED_VARIABLE
							: newInput);
			lastEditorInput = newInput;
		}
	}

	private void hookListener(WorkbenchWindow lastActiveWorkbenchWindow,
			WorkbenchWindow newActiveWorkbenchWindow) {
		if (lastActiveWorkbenchWindow != null) {
			lastActiveWorkbenchWindow
					.removePropertyChangeListener(propertyListener);
			lastActiveWorkbenchWindow
					.removePerspectiveListener(perspectiveListener);
		}

		if (newActiveWorkbenchWindow != null) {
			newActiveWorkbenchWindow
					.addPropertyChangeListener(propertyListener);
			newActiveWorkbenchWindow
					.addPerspectiveListener(perspectiveListener);
		}
	}
	
	private void hookListener(IEditorPart lastActiveEditor,
			IEditorPart newActiveEditor) {
		if (lastActiveEditor!=null) {
			lastActiveEditor.removePropertyListener(editorListener);
		}
		if (newActiveEditor!=null) {
			newActiveEditor.addPropertyListener(editorListener);
		}
	}

	private void updateActiveShell(Map currentState) {

		final Shell newActiveShell = display.getActiveShell();
		currentState.put(ISources.ACTIVE_SHELL_NAME, newActiveShell);

		/*
		 * We will fallback to the workbench window, but only if a dialog is not
		 * open.
		 */
		final IContextService contextService = (IContextService) workbench
				.getService(IContextService.class);
		final int shellType = contextService.getShellType(newActiveShell);
		if (shellType == IContextService.TYPE_DIALOG)
			return;

		final WorkbenchWindow newActiveWorkbenchWindow = (WorkbenchWindow) workbench
				.getActiveWorkbenchWindow();
		final Shell newActiveWorkbenchWindowShell;
		if (newActiveWorkbenchWindow == null) {
			newActiveWorkbenchWindowShell = null;
		} else {
			newActiveWorkbenchWindowShell = newActiveWorkbenchWindow.getShell();
		}
		currentState.put(ISources.ACTIVE_WORKBENCH_WINDOW_NAME,
				newActiveWorkbenchWindow);
		currentState.put(ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME,
				newActiveWorkbenchWindowShell);

		final Boolean newCoolbarVisibility = newActiveWorkbenchWindow == null ? lastCoolbarVisibility
				: (newActiveWorkbenchWindow.getCoolBarVisible() ? Boolean.TRUE
						: Boolean.FALSE);
		final Boolean newPerspectiveBarVisibility = newActiveWorkbenchWindow == null ? lastPerspectiveBarVisibility
				: (newActiveWorkbenchWindow.getPerspectiveBarVisible() ? Boolean.TRUE
						: Boolean.FALSE);
		final Boolean newStatusLineVis = newActiveWorkbenchWindow == null ? lastStatusLineVisibility
				: (newActiveWorkbenchWindow.getStatusLineVisible() ? Boolean.TRUE
						: Boolean.FALSE);

		String perspectiveId = lastPerspectiveId;
		if (newActiveWorkbenchWindow != null) {
			IWorkbenchPage activePage = newActiveWorkbenchWindow
					.getActivePage();
			if (activePage != null) {
				IPerspectiveDescriptor perspective = activePage
						.getPerspective();
				if (perspective != null) {
					perspectiveId = perspective.getId();
				}
			}
		}

		currentState.put(
				ISources.ACTIVE_WORKBENCH_WINDOW_IS_COOLBAR_VISIBLE_NAME,
				newCoolbarVisibility);

		currentState
				.put(
						ISources.ACTIVE_WORKBENCH_WINDOW_IS_PERSPECTIVEBAR_VISIBLE_NAME,
						newPerspectiveBarVisibility);
		currentState.put(STATUS_LINE_VIS, newStatusLineVis);

		currentState.put(
				ISources.ACTIVE_WORKBENCH_WINDOW_ACTIVE_PERSPECTIVE_NAME,
				perspectiveId);

	}

}
