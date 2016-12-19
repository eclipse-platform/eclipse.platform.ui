/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.E4PartWrapper;
import org.eclipse.ui.internal.WorkbenchPage;

public class SelectionService implements ISelectionChangedListener, ISelectionService {

	@Inject
	private IEclipseContext context;

	@Inject
	private MApplication application;

	private ESelectionService selectionService;

	@Inject
	@Optional
	@Named("org.eclipse.ui.IWorkbenchPage")
	private WorkbenchPage page;

	private IWorkbenchPart activePart;

	private ListenerList<ISelectionListener> listeners = new ListenerList<>();
	private ListenerList<ISelectionListener> postSelectionListeners = new ListenerList<>();
	private Map<String, Set<ISelectionListener>> targetedListeners = new HashMap<>();
	private Map<String, Set<ISelectionListener>> targetedPostSelectionListeners = new HashMap<>();

	private org.eclipse.e4.ui.workbench.modeling.ISelectionListener listener = (part, selection) -> handleSelectionChanged(part, selection, false);

	private org.eclipse.e4.ui.workbench.modeling.ISelectionListener targetedListener = (part, selection) -> handleSelectionChanged(part, selection, true);

	private org.eclipse.e4.ui.workbench.modeling.ISelectionListener postListener = (part, selection) -> handlePostSelectionChanged(part, selection, false);

	private org.eclipse.e4.ui.workbench.modeling.ISelectionListener targetedPostListener = (part, selection) -> handlePostSelectionChanged(part, selection, true);

	private void handleSelectionChanged(MPart part, Object selection, boolean targeted) {
		selection = createCompatibilitySelection(selection);
		context.set(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);

		IEclipseContext applicationContext = application.getContext();
		if (applicationContext.getActiveChild() == context) {
			application.getContext().set(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
		}

		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
			if (targeted) {
				notifyListeners(workbenchPart, (ISelection) selection, part.getElementId(),
						targetedListeners);
			} else {
				notifyListeners(workbenchPart, (ISelection) selection, listeners);
			}
		} else if (client != null) {
			if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
				IWorkbenchPart workbenchPart = (IWorkbenchPart) part.getTransientData()
						.get(E4PartWrapper.E4_WRAPPER_KEY);
				if (targeted) {
					notifyListeners(workbenchPart, (ISelection) selection, part.getElementId(), targetedListeners);
				} else {
					notifyListeners(workbenchPart, (ISelection) selection, listeners);
				}
			}
		}
	}

	private void handlePostSelectionChanged(MPart part, Object selection, boolean targeted) {
		selection = createCompatibilitySelection(selection);

		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
			if (targeted) {
				notifyListeners(workbenchPart, (ISelection) selection, part.getElementId(),
						targetedPostSelectionListeners);
			} else {
				notifyListeners(workbenchPart, (ISelection) selection, postSelectionListeners);
			}
		} else if (client != null) {
			if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
				IWorkbenchPart workbenchPart = (IWorkbenchPart) part.getTransientData()
						.get(E4PartWrapper.E4_WRAPPER_KEY);
				if (targeted) {
					notifyListeners(workbenchPart, (ISelection) selection, part.getElementId(),
							targetedPostSelectionListeners);
				} else {
					notifyListeners(workbenchPart, (ISelection) selection, postSelectionListeners);
				}
			}
		}
	}

	private static ISelection createCompatibilitySelection(Object selection) {
		if (selection instanceof ISelection) {
			return (ISelection) selection;
		}
		return selection == null ? StructuredSelection.EMPTY : new StructuredSelection(
				selection);
	}

	/**
	 * Updates the selection of the workbench window with that of the active
	 * part's.
	 *
	 * @param activePart
	 *            the currently active part
	 */
	public void updateSelection(IWorkbenchPart activePart) {
		if (activePart != null) {
			ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				ISelection selection = selectionProvider.getSelection();
				context.set(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);

				IEclipseContext applicationContext = application.getContext();
				if (applicationContext.getActiveChild() == context) {
					application.getContext().set(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
				}
			}
		}
	}

	/**
	 * Notifies selection listeners about selection change caused by active part
	 * change.
	 *
	 * @param activePart
	 *            the currently active part
	 */
	public void notifyListeners(IWorkbenchPart activePart) {
		if (activePart != null) {
			ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				ISelection selection = selectionProvider.getSelection();

				notifyListeners(activePart, selection, listeners);
				notifyListeners(activePart, selection, activePart.getSite().getId(), targetedListeners);
				notifyListeners(activePart, selection, postSelectionListeners);
				notifyListeners(activePart, selection, activePart.getSite().getId(), targetedPostSelectionListeners);
			}
		}
	}

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) final MPart part) {
		activePart = null;
		if (part != null) {
			Object client = part.getObject();
			if (client instanceof CompatibilityPart) {
				IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
				activePart = workbenchPart;
			} else if (client != null) {
				if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
					activePart = (IWorkbenchPart) part.getTransientData().get(
							E4PartWrapper.E4_WRAPPER_KEY);
				}
			}
		}
	}

	@Inject
	void setSelectionService(@Optional ESelectionService selectionService) {
		if (this.selectionService != null) {
			this.selectionService.removeSelectionListener(listener);
			for (String partId : targetedListeners.keySet()) {
				this.selectionService.removeSelectionListener(partId, targetedListener);
			}

			this.selectionService.removePostSelectionListener(postListener);
			for (String partId : targetedPostSelectionListeners.keySet()) {
				this.selectionService.removePostSelectionListener(partId, targetedPostListener);
			}
		}

		if (selectionService != null) {
			selectionService.addSelectionListener(listener);
			for (String partId : targetedListeners.keySet()) {
				selectionService.addSelectionListener(partId, targetedListener);
			}

			selectionService.addPostSelectionListener(postListener);
			for (String partId : targetedPostSelectionListeners.keySet()) {
				selectionService.addPostSelectionListener(partId, targetedPostListener);
			}
			this.selectionService = selectionService;
		}
	 }

	@PreDestroy
	public void dispose() {
		setSelectionService(null);
		selectionService = null;
		listeners.clear();
		postSelectionListeners.clear();
		targetedListeners.clear();
		targetedPostSelectionListeners.clear();
	}

	private void notifyListeners(IWorkbenchPart workbenchPart, ISelection selection,
			ListenerList<ISelectionListener> listenerList) {
		for (ISelectionListener listener : listenerList) {
			if (selection != null || listener instanceof INullSelectionListener) {
				listener.selectionChanged(workbenchPart, selection);
			}
		}
	}

	private void notifyListeners(IWorkbenchPart workbenchPart, ISelection selection, String id,
			Map<String, Set<ISelectionListener>> listenerMap) {
		if (id != null) {
			Set<ISelectionListener> listeners = listenerMap.get(id);
			if (listeners != null) {
				for (ISelectionListener listener : listeners) {
					if (selection != null || listener instanceof INullSelectionListener) {
						listener.selectionChanged(workbenchPart, selection);
					}
				}
			}
		}
	}

	@Override
	public void addSelectionListener(ISelectionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void addSelectionListener(String partId, ISelectionListener listener) {
		Set<ISelectionListener> listeners = targetedListeners.get(partId);
		if (listeners == null) {
			listeners = new HashSet<>();
			targetedListeners.put(partId, listeners);
		}
		if (listeners.size() == 0 && selectionService != null) {
			selectionService.addSelectionListener(partId, this.targetedListener);
		}
		listeners.add(listener);
	}

	@Override
	public void addPostSelectionListener(ISelectionListener listener) {
		postSelectionListeners.add(listener);
	}

	@Override
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		Set<ISelectionListener> listeners = targetedPostSelectionListeners.get(partId);
		if (listeners == null) {
			listeners = new HashSet<>();
			targetedPostSelectionListeners.put(partId, listeners);
		}
		if (listeners.size() == 0 && selectionService != null) {
			selectionService.addPostSelectionListener(partId, targetedPostListener);
		}
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		if (activePart != null) {
			// get the selection from the active part
			ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
			return selectionProvider == null ? null : selectionProvider.getSelection();
		}

		Object selection = selectionService.getSelection();
		if (selection == null || selection instanceof ISelection) {
			return (ISelection) selection;
		}
		return new StructuredSelection(selection);
	}

	@Override
	public ISelection getSelection(String partId) {
		Object selection = selectionService.getSelection(partId);
		if (selection == null || selection instanceof ISelection) {
			return (ISelection) selection;
		}
		return new StructuredSelection(selection);
	}

	@Override
	public void removeSelectionListener(ISelectionListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		Set<ISelectionListener> listeners = targetedListeners.get(partId);
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.size() == 0 && selectionService != null) {
				selectionService.removeSelectionListener(partId, this.targetedListener);
			}
		}
	}

	@Override
	public void removePostSelectionListener(ISelectionListener listener) {
		postSelectionListeners.remove(listener);
	}

	@Override
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		Set<ISelectionListener> listeners = targetedPostSelectionListeners.get(partId);
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.size() == 0 && selectionService != null) {
				selectionService.removePostSelectionListener(partId, targetedPostListener);
			}
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent e) {
		MPart part = page.findPart(activePart);
		ESelectionService selectionService = (ESelectionService) part.getContext().get(
				ESelectionService.class.getName());
		selectionService.setSelection(e.getSelection());
	}

}
