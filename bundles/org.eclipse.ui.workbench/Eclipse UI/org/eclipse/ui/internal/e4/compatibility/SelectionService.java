/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.IPostSelectionProvider;
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

	private ListenerList listeners = new ListenerList();
	private Map<String, Set<ISelectionListener>> targetedListeners = new HashMap<String, Set<ISelectionListener>>();

	private org.eclipse.e4.ui.workbench.modeling.ISelectionListener listener = new org.eclipse.e4.ui.workbench.modeling.ISelectionListener() {
		public void selectionChanged(MPart part, Object selection) {
			selection = createCompatibilitySelection(selection);
			context.set(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);

			IEclipseContext applicationContext = application.getContext();
			if (applicationContext.getActiveChild() == context) {
				application.getContext().set(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
			}

			Object client = part.getObject();
			if (client instanceof CompatibilityPart) {
				IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
				notifyListeners(part.getElementId(), workbenchPart, (ISelection) selection);
			}

			if (page != null) {
				page.updateShowInSources(part);
			}
		}
	};

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

				notifyListeners(activePart.getSite().getId(), activePart, selection);
			}
		}
	}

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) final MPart part) {
		if (activePart != null) {
			ISelectionProvider selectionProvider = activePart.getSite().getSelectionProvider();
			if (selectionProvider != null) {
				selectionProvider.removeSelectionChangedListener(this);

				if (selectionProvider instanceof IPostSelectionProvider) {
					((IPostSelectionProvider) selectionProvider)
							.removePostSelectionChangedListener(this);
				}
			}
			activePart = null;
		}

		if (part != null) {
			Object client = part.getObject();
			if (client instanceof CompatibilityPart) {
				IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();

				ISelectionProvider selectionProvider = workbenchPart.getSite()
						.getSelectionProvider();
				if (selectionProvider != null) {
					selectionProvider.addSelectionChangedListener(this);

					if (selectionProvider instanceof IPostSelectionProvider) {
						((IPostSelectionProvider) selectionProvider)
								.addPostSelectionChangedListener(this);
					}
				}

				activePart = workbenchPart;
			}
		}
	}

	@Inject
	void setSelectionService(@Optional ESelectionService selectionService) {
		if (this.selectionService != null) {
			this.selectionService.removeSelectionListener(listener);
		}

		if (selectionService != null) {
			selectionService.addSelectionListener(listener);
			this.selectionService = selectionService;
		}
	 }

	private void notifyListeners(String id, IWorkbenchPart workbenchPart, ISelection selection) {
		for (Object listener : listeners.getListeners()) {
			if (selection != null || listener instanceof INullSelectionListener) {
				((ISelectionListener) listener).selectionChanged(workbenchPart, selection);
			}
		}

		if (id != null) {
			Set<ISelectionListener> listeners = targetedListeners.get(id);
			if (listeners != null) {
				for (ISelectionListener listener : listeners) {
					if (selection != null || listener instanceof INullSelectionListener) {
						listener.selectionChanged(workbenchPart, selection);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#addSelectionListener(org.eclipse.ui.
	 * ISelectionListener)
	 */
	public void addSelectionListener(ISelectionListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#addSelectionListener(java.lang.String,
	 * org.eclipse.ui.ISelectionListener)
	 */
	public void addSelectionListener(String partId, ISelectionListener listener) {
		Set<ISelectionListener> listeners = targetedListeners.get(partId);
		if (listeners == null) {
			listeners = new HashSet<ISelectionListener>();
			targetedListeners.put(partId, listeners);
		}
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#addPostSelectionListener(org.eclipse
	 * .ui.ISelectionListener)
	 */
	public void addPostSelectionListener(ISelectionListener listener) {
		// TODO compat addPostSelectionListener
		addSelectionListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#addPostSelectionListener(java.lang.String
	 * , org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		// TODO compat addPostSelectionListener
		addSelectionListener(partId, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionService#getSelection()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISelectionService#getSelection(java.lang.String)
	 */
	public ISelection getSelection(String partId) {
		Object selection = selectionService.getSelection(partId);
		if (selection == null || selection instanceof ISelection) {
			return (ISelection) selection;
		}
		return new StructuredSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#removeSelectionListener(org.eclipse.
	 * ui.ISelectionListener)
	 */
	public void removeSelectionListener(ISelectionListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#removeSelectionListener(java.lang.String
	 * , org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		Set<ISelectionListener> listeners = targetedListeners.get(partId);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#removePostSelectionListener(org.eclipse
	 * .ui.ISelectionListener)
	 */
	public void removePostSelectionListener(ISelectionListener listener) {
		// TODO compat removePostSelectionListener
		removeSelectionListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ISelectionService#removePostSelectionListener(java.lang
	 * .String, org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		// TODO compat removePostSelectionListener
		removeSelectionListener(partId, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(
	 * org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent e) {
		MPart part = page.findPart(activePart);
		ESelectionService selectionService = (ESelectionService) part.getContext().get(
				ESelectionService.class.getName());
		selectionService.setSelection(e.getSelection());
	}

}
