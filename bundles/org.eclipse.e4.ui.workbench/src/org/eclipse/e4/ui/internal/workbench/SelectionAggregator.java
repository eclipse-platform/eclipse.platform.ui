/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.internal.contexts.IContextDisposalListener;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class SelectionAggregator {

	static final String OUT_SELECTION = "org.eclipse.ui.output.selection"; //$NON-NLS-1$
	static final String OUT_POST_SELECTION = "org.eclipse.ui.output.postSelection"; //$NON-NLS-1$

	private ListenerList genericListeners = new ListenerList();
	private ListenerList genericPostListeners = new ListenerList();
	private Map<String, ListenerList> targetedListeners = new HashMap<String, ListenerList>();
	private Map<String, ListenerList> targetedPostListeners = new HashMap<String, ListenerList>();
	private Set<IEclipseContext> tracked = new HashSet<IEclipseContext>();

	private EventHandler eventHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (element instanceof MPart) {
				MPart part = (MPart) element;

				String partId = part.getElementId();
				if (targetedListeners.containsKey(partId))
					track(part);
			}
		}
	};

	private MPart activePart;

	private IEclipseContext context;

	private EPartService partService;

	private IEventBroker eventBroker;

	private Logger logger;

	@Inject
	SelectionAggregator(IEclipseContext context, EPartService partService,
			IEventBroker eventBroker, Logger logger) {
		super();
		this.context = context;
		this.partService = partService;
		this.eventBroker = eventBroker;
		this.logger = logger;
	}

	@PreDestroy
	void preDestroy() {
		genericListeners.clear();
		genericPostListeners.clear();
		targetedListeners.clear();
		targetedPostListeners.clear();

		eventBroker.unsubscribe(eventHandler);
	}

	@PostConstruct
	void postConstruct() {
		eventBroker.subscribe(UIEvents.Context.TOPIC_CONTEXT, eventHandler);
	}

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) final MPart part) {
		if ((part != null) && (activePart != part)) {
			activePart = part;
			IEclipseContext partContext = part.getContext();
			if (partContext.containsKey(OUT_POST_SELECTION)) {
				Object selection = partContext.get(OUT_POST_SELECTION);
				context.set(IServiceConstants.ACTIVE_SELECTION, selection);
			} else if (partContext.containsKey(OUT_SELECTION)) {
				Object selection = partContext.get(OUT_SELECTION);
				context.set(IServiceConstants.ACTIVE_SELECTION, selection);
			}
			track(part);
		}
	}

	private void notifyListeners(final MPart part, final Object selection) {
		for (Object listener : genericListeners.getListeners()) {
			final ISelectionListener myListener = (ISelectionListener) listener;
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					myListener.selectionChanged(part, selection);
				}

				public void handleException(Throwable exception) {
					logger.error(exception);
				}
			});
		}
		notifyTargetedListeners(part, selection);
	}

	private void notifyTargetedListeners(final MPart part, final Object selection) {
		String id = part.getElementId();
		if (id != null) {
			ListenerList listenerList = targetedListeners.get(id);
			if (listenerList != null) {
				for (Object listener : listenerList.getListeners()) {
					final ISelectionListener myListener = (ISelectionListener) listener;
					SafeRunner.run(new ISafeRunnable() {
						public void run() throws Exception {
							myListener.selectionChanged(part, selection);
						}

						public void handleException(Throwable exception) {
							logger.error(exception);
						}
					});
				}
			}
		}
	}

	private void notifyPostListeners(final MPart part, final Object selection) {
		for (Object listener : genericPostListeners.getListeners()) {
			final ISelectionListener myListener = (ISelectionListener) listener;
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Exception {
					myListener.selectionChanged(part, selection);
				}

				public void handleException(Throwable exception) {
					logger.error(exception);
				}
			});
		}
		notifyTargetedPostListeners(part, selection);
	}

	private void notifyTargetedPostListeners(final MPart part, final Object selection) {
		String id = part.getElementId();
		if (id != null) {
			ListenerList listenerList = targetedPostListeners.get(id);
			if (listenerList != null) {
				for (Object listener : listenerList.getListeners()) {
					final ISelectionListener myListener = (ISelectionListener) listener;
					SafeRunner.run(new ISafeRunnable() {
						public void run() throws Exception {
							myListener.selectionChanged(part, selection);
						}

						public void handleException(Throwable exception) {
							logger.error(exception);
						}
					});
				}
			}
		}
	}

	private void track(final MPart part) {
		final IEclipseContext myContext = this.context;
		IEclipseContext context = part.getContext();
		if (context != null && tracked.add(context)) {
			if (context instanceof EclipseContext) {
				((EclipseContext) context).notifyOnDisposal(new IContextDisposalListener() {
					public void disposed(IEclipseContext context) {
						tracked.remove(context);
					}
				});
			}

			context.runAndTrack(new RunAndTrack() {
				private boolean initial = true;

				public boolean changed(IEclipseContext context) {
					final Object selection = context.get(OUT_SELECTION);
					if (initial) {
						initial = false;
						if (selection == null) {
							return true;
						}
					}

					if (activePart == part) {
						myContext.set(IServiceConstants.ACTIVE_SELECTION, selection);
						runExternalCode(new Runnable() {
							public void run() {
								notifyListeners(part, selection);
							}
						});
					} else {
						runExternalCode(new Runnable() {
							public void run() {
								notifyTargetedListeners(part, selection);
							}
						});
						// we don't need to keep tracking non-active parts unless
						// they have targeted listeners
						String partId = part.getElementId();
						boolean continueTracking = targetedListeners.containsKey(partId)
								|| targetedPostListeners.containsKey(partId);
						if (!continueTracking) {
							tracked.remove(part.getContext());
						}
						return continueTracking;
					}
					return true;
				}
			});
			context.runAndTrack(new RunAndTrack() {
				private boolean initial = true;

				public boolean changed(IEclipseContext context) {
					final Object postSelection = context.get(OUT_POST_SELECTION);
					if (initial) {
						initial = false;
						if (postSelection == null) {
							return true;
						}
					}

					if (activePart == part) {
						runExternalCode(new Runnable() {
							public void run() {
								notifyPostListeners(part, postSelection);
							}
						});
					} else {
						runExternalCode(new Runnable() {
							public void run() {
								notifyTargetedPostListeners(part, postSelection);
							}
						});
						// we don't need to keep tracking non-active parts unless
						// they have targeted listeners
						String partId = part.getElementId();
						boolean continueTracking = targetedListeners.containsKey(partId)
								|| targetedPostListeners.containsKey(partId);
						if (!continueTracking) {
							tracked.remove(part.getContext());
						}
						return continueTracking;
					}
					return true;
				}
			});
		}
	}

	public Object getSelection() {
		return context.get(IServiceConstants.ACTIVE_SELECTION);
	}

	public void addSelectionListener(ISelectionListener listener) {
		genericListeners.add(listener);
	}

	public void addPostSelectionListener(ISelectionListener listener) {
		genericPostListeners.add(listener);
	}

	public void removeSelectionListener(ISelectionListener listener) {
		// we may have been destroyed already, see bug 310113
		if (context != null) {
			genericListeners.remove(listener);
		}
	}

	public void removePostSelectionListener(ISelectionListener listener) {
		// we may have been destroyed already, see bug 310113
		if (context != null) {
			genericPostListeners.remove(listener);
		}
	}

	public void addSelectionListener(String partId, ISelectionListener listener) {
		ListenerList listeners = targetedListeners.get(partId);
		if (listeners == null) {
			listeners = new ListenerList();
			targetedListeners.put(partId, listeners);
		}
		listeners.add(listener);

		MPart part = partService.findPart(partId);
		if (part != null)
			track(part);
	}

	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		ListenerList listeners = targetedPostListeners.get(partId);
		if (listeners == null) {
			listeners = new ListenerList();
			targetedPostListeners.put(partId, listeners);
		}
		listeners.add(listener);

		MPart part = partService.findPart(partId);
		if (part != null)
			track(part);
	}

	public void removeSelectionListener(String partId, ISelectionListener listener) {
		// we may have been destroyed already, see bug 310113
		if (context != null) {
			ListenerList listeners = targetedListeners.get(partId);
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}

	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		// we may have been destroyed already, see bug 310113
		if (context != null) {
			ListenerList listeners = targetedPostListeners.get(partId);
			if (listeners != null) {
				listeners.remove(listener);
			}
		}
	}

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
