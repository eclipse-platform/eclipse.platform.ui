/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SelectionServiceImpl implements ESelectionService {

	static final String OUT_SELECTION = "output.selection"; //$NON-NLS-1$

	private ListenerList genericListeners = new ListenerList();
	private Map<String, ListenerList> targetedListeners = new HashMap<String, ListenerList>();
	private Set<IEclipseContext> tracked = new HashSet<IEclipseContext>();

	@Inject
	private IEclipseContext context;

	@Inject
	private EPartService partService;

	/**
	 * This is the specific implementation. TODO: generalize it
	 */
	@Inject
	@Named(EPartService.PART_SERVICE_ROOT)
	private MContext serviceRoot;

	@Inject
	private IEventBroker eventBroker;

	private EventHandler eventHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (element instanceof MPart) {
				MPart part = (MPart) element;
				IEclipseContext context = part.getContext();
				if (context != null && isInContainer(part)) {
					track(part);
				}
			}
		}
	};

	private MPart activePart;

	@PreDestroy
	void preDestroy() {
		genericListeners.clear();
		targetedListeners.clear();

		eventBroker.unsubscribe(eventHandler);
	}

	@PostConstruct
	void postConstruct() {
		eventBroker
				.subscribe(UIEvents.buildTopic(UIEvents.Context.TOPIC, UIEvents.Context.CONTEXT),
						eventHandler);

		for (MPart part : partService.getParts()) {
			track(part);
		}
	}

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) final MPart part) {
		if ((part != null) && (activePart != part)) {
			activePart = part;
			IEclipseContext partContext = part.getContext();
			// only notify listeners if the part actually posts selections
			if (partContext.containsKey(OUT_SELECTION)) {
				Object selection = partContext.get(OUT_SELECTION);
				notifyListeners(part, selection);
			}
			track(part);
		}
	}

	private boolean isInContainer(MPart part) {
		return isInContainer((MElementContainer<?>) serviceRoot, part);
	}

	private boolean isInContainer(MElementContainer<?> container, MPart part) {
		for (Object object : container.getChildren()) {
			if (object == part) {
				return true;
			} else if (object instanceof MElementContainer<?>) {
				if (isInContainer((MElementContainer<?>) object, part)) {
					return true;
				}
			}
		}

		return false;
	}

	private void notifyListeners(MPart part, Object selection) {
		context.set(IServiceConstants.ACTIVE_SELECTION, selection);

		for (Object listener : genericListeners.getListeners()) {
			((ISelectionListener) listener).selectionChanged(part, selection);
		}

		notifyTargetedListeners(part, selection);
	}

	private void notifyTargetedListeners(MPart part, Object selection) {
		String id = part.getElementId();
		if (id != null) {
			ListenerList listenerList = targetedListeners.get(id);
			if (listenerList != null) {
				for (Object listener : listenerList.getListeners()) {
					((ISelectionListener) listener).selectionChanged(part, selection);
				}
			}
		}
	}

	private void track(final MPart part) {
		IEclipseContext context = part.getContext();
		if (context != null && tracked.add(context)) {
			context.runAndTrack(new RunAndTrack() {
				private boolean initial = true;

				public boolean changed(IEclipseContext context) {
					Object selection = context.get(OUT_SELECTION);
					if (initial) {
						initial = false;
						if (selection == null) {
							return true;
						}
					}

					if (activePart == part) {
						notifyListeners(part, selection);
					} else {
						notifyTargetedListeners(part, selection);
					}
					return true;
				}
			});
		}
	}

	void internalSetSelection(Object selection) {
		if (selection != null) {
			context.set(IServiceConstants.ACTIVE_SELECTION, selection);
		} else {
			context.remove(IServiceConstants.ACTIVE_SELECTION);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.selection.ESelectionService#setSelection(java.lang.Object)
	 */
	public void setSelection(Object selection) {
		throw new UnsupportedOperationException("Cannot set the selection of a window"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.selection.ESelectionService#getSelection()
	 */
	public Object getSelection() {
		if (activePart == null) {
			return null;
		}

		IEclipseContext partContext = activePart.getContext();
		return partContext == null ? null : partContext.get(ESelectionService.SELECTION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.selection.ESelectionService#addSelectionListener(org.eclipse.e4.ui.selection
	 * .ISelectionListener)
	 */
	public void addSelectionListener(ISelectionListener listener) {
		genericListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.selection.ESelectionService#removeSelectionListener(org.eclipse.e4.ui.selection
	 * .ISelectionListener)
	 */
	public void removeSelectionListener(ISelectionListener listener) {
		// we may have been destroyed already, see bug 310113
		if (context != null) {
			genericListeners.remove(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.selection.ESelectionService#addSelectionListener(java.lang.String,
	 * org.eclipse.e4.ui.selection.ISelectionListener)
	 */
	public void addSelectionListener(String partId, ISelectionListener listener) {
		ListenerList listeners = targetedListeners.get(partId);
		if (listeners == null) {
			listeners = new ListenerList();
			targetedListeners.put(partId, listeners);
		}
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.selection.ESelectionService#removeSelectionListener(java.lang.String,
	 * org.eclipse.e4.ui.selection.ISelectionListener)
	 */
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		// we may have been destroyed already, see bug 310113
		if (context != null) {
			ListenerList listeners = targetedListeners.get(partId);
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.selection.ESelectionService#getSelection(java.lang.String)
	 */
	public Object getSelection(String partId) {
		MPart part = partService.findPart(partId);
		if (part == null) {
			return null;
		}

		IEclipseContext partContext = part.getContext();
		if (partContext == null) {
			return null;
		}
		return partContext.get(OUT_SELECTION);
	}

}
