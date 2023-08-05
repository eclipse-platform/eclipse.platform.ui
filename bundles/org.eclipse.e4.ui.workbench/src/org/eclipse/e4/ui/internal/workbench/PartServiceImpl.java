/*******************************************************************************
 * Copyright (c) 2009, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel (Lars.Vogel@vogella.com) - Bug 416082,  472654, 395825
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 450411, 486876, 461063
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 463962
 *     Christoph Läubrich - Bug 573537
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * A window-based {@link EPartService}.
 */
public class PartServiceImpl implements EPartService {

	/**
	 * The part activation time of a part is stored in it's transient data.
	 */
	public static final String PART_ACTIVATION_TIME = "partActivationTime"; //$NON-NLS-1$

	private EventHandler selectedHandler = event -> {
		// no need to do anything if we have no listeners
		if (!this.listeners.isEmpty()) {
			Object oldSelected = event.getProperty(UIEvents.EventTags.OLD_VALUE);
			if (oldSelected instanceof MPlaceholder) {
				oldSelected = ((MPlaceholder) oldSelected).getRef();
			}

			MPlaceholder placeholder = null;
			Object selected = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (selected instanceof MPlaceholder) {
				placeholder = (MPlaceholder) selected;
				selected = placeholder.getRef();
			}

			MPart oldSelectedPart = oldSelected instanceof MPart ? (MPart) oldSelected : null;
			MPart selectedPart = selected instanceof MPart ? (MPart) selected : null;

			if (oldSelectedPart != null && getParts().contains(selectedPart)) {
				firePartHidden(oldSelectedPart);
			}

			if (selectedPart != null && selectedPart.isToBeRendered()
					&& getParts().contains(selectedPart)) {
				// ask the renderer to create this part
				if (placeholder == null) {
					if (selectedPart.getParent().getRenderer() != null) {
						this.engine.createGui(selectedPart);
						firePartVisible(selectedPart);
						firePartBroughtToTop(selectedPart);
					}
				} else if (placeholder.getParent().getRenderer() != null) {
					this.engine.createGui(placeholder);
					firePartVisible(selectedPart);
					firePartBroughtToTop(selectedPart);
				}
			}
		}
	};

	private EventHandler minimizedPartHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (!(element instanceof MPartStack)) {
				return;
			}

			Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);

			boolean minimizedTagAdded = UIEvents.isADD(event)
					&& IPresentationEngine.MINIMIZED.equals(newValue);
			boolean minimizedTagRemoved = UIEvents.isREMOVE(event)
					&& IPresentationEngine.MINIMIZED.equals(oldValue);

			if (!(minimizedTagAdded || minimizedTagRemoved)) {
				return;
			}

			MPart part = toPart(((MPartStack) element).getSelectedElement());
			if (part != null && minimizedTagAdded) {
				firePartHidden(part);
			} else if (part != null) {
				firePartVisible(part);
			}
		}

		private MPart toPart(MStackElement stackElement) {
			if (stackElement != null) {
				return stackElement instanceof MPlaceholder ? (MPart) ((MPlaceholder) stackElement)
						.getRef() : (MPart) stackElement;
			}
			return null;
		}
	};

	private MApplication application;

	/**
	 * Might be null if this part service is created for the application
	 */
	private MWindow workbenchWindow;

	@Inject
	private IPresentationEngine engine;

	@Inject
	private EModelService modelService;

	@Inject
	private Logger logger;

	@Inject
	@Optional
	private ISaveHandler saveHandler;

	@Inject
	private IEventBroker eventBroker;

	// @Optional as the context service may not have been installed
	@Inject
	@Optional
	private EContextService contextService;

	@Inject
	@Optional
	private ContextManager contextManager;

	private PartActivationHistory partActivationHistory;

	private MPart activePart;

	private ListenerList<IPartListener> listeners = new ListenerList<>();

	private boolean constructed = false;

	@Inject
	public PartServiceImpl(MApplication application, @Optional MWindow window) {
		// no need to track changes:
		this.application = application;
		workbenchWindow = window;
	}

	private void log(String unidentifiedMessage, String identifiedMessage, String id, Exception e) {
		if (id == null || id.isEmpty()) {
			logger.error(e, unidentifiedMessage);
		} else {
			logger.error(e, NLS.bind(identifiedMessage, id));
		}
	}

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) MPart p) {
		if (activePart != p) {
			if (p != null) {
				MPerspective persp = modelService.getPerspectiveFor(p);
				boolean inCurrentPerspective = persp == null
						|| persp == persp.getParent().getSelectedElement();
				if (inCurrentPerspective) {
					activate(p, true, true);
				}
			} else {
				activate(p, true, true);
			}
		}
	}

	@PostConstruct
	void postConstruct() {
		eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT, selectedHandler);
		eventBroker.subscribe(UIEvents.ApplicationElement.TOPIC_TAGS, minimizedPartHandler);
		constructed = true;
		partActivationHistory = new PartActivationHistory(this, modelService);
		if (activePart != null) {
			partActivationHistory.prepend(activePart);
		}
	}

	@PreDestroy
	void preDestroy() {
		constructed = false;
		eventBroker.unsubscribe(selectedHandler);
		eventBroker.unsubscribe(minimizedPartHandler);
		partActivationHistory.clear();
	}

	private void firePartActivated(final MPart part) {
		for (final IPartListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partActivated(part);
				}

				@Override
				public void handleException(Throwable throwable) {
					logger.error(throwable, "An exception occurred while notifying part listeners"); //$NON-NLS-1$
				}
			});
		}
	}

	private void firePartDeactivated(final MPart part) {
		for (final IPartListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partDeactivated(part);
				}

				@Override
				public void handleException(Throwable throwable) {
					logger.error(throwable, "An exception occurred while notifying part listeners"); //$NON-NLS-1$
				}
			});
		}
	}

	private void firePartHidden(final MPart part) {
		for (final IPartListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partHidden(part);
				}

				@Override
				public void handleException(Throwable throwable) {
					logger.error(throwable, "An exception occurred while notifying part listeners"); //$NON-NLS-1$
				}
			});
		}
	}

	private void firePartVisible(final MPart part) {
		for (final IPartListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partVisible(part);
				}

				@Override
				public void handleException(Throwable throwable) {
					logger.error(throwable, "An exception occurred while notifying part listeners"); //$NON-NLS-1$
				}
			});
		}
	}

	private void firePartBroughtToTop(final MPart part) {
		for (final IPartListener listener : listeners) {
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partBroughtToTop(part);
				}

				@Override
				public void handleException(Throwable throwable) {
					logger.error(throwable, "An exception occurred while notifying part listeners"); //$NON-NLS-1$
				}
			});
		}
	}

	@Override
	public void addPartListener(IPartListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removePartListener(IPartListener listener) {
		listeners.remove(listener);
	}

	private MWindow getWindow() {
		if (workbenchWindow != null)
			return workbenchWindow;
		if (application.getSelectedElement() != null)
			return application.getSelectedElement();
		List<MWindow> windows = application.getChildren();
		if (!windows.isEmpty())
			return windows.get(0);
		return null;
	}

	private MContext getParentWithContext(MUIElement part) {
		MElementContainer<MUIElement> parent = part.getParent();
		MUIElement intermediate = parent;
		if (intermediate == null) {
			intermediate = part;
		} else {
			while (parent != null) {
				if (parent instanceof MContext) {
					if (((MContext) parent).getContext() != null)
						return (MContext) parent;
				}
				intermediate = parent;
				parent = parent.getParent();
			}
		}

		MPlaceholder placeholder = modelService.findPlaceholderFor(getWindow(), intermediate);
		parent = placeholder.getParent();
		while (parent != null) {
			if (parent instanceof MContext) {
				if (((MContext) parent).getContext() != null)
					return (MContext) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	@Override
	public void bringToTop(MPart part) {
		if (isInContainer(part)) {
			MUIElement currentElement = part;
			MElementContainer<MUIElement> parent = part.getParent();
			if (parent == null) {
				currentElement = modelService.findPlaceholderFor(getWindow(), part);
				parent = currentElement.getParent();
			}

			// If the part is in the same stack as the currently active part then activate it
			// instead
			MElementContainer<MUIElement> activeParent = activePart != null ? activePart
					.getParent() : null;
			if (activePart != null && activeParent == null) {
				MPlaceholder activePH = modelService.findPlaceholderFor(getWindow(), activePart);
				if (activePH != null) {
					activeParent = activePH.getParent();
				}
			}
			if (parent == activeParent && part != activePart) {
				activate(part);
				return;
			}

			MUIElement oldSelectedElement = parent.getSelectedElement();

			delegateBringToTop(part);

			// check to make sure that the currently selected element is actually valid
			if (oldSelectedElement != currentElement
					&& parent.getChildren().contains(oldSelectedElement)
					&& parent instanceof MGenericStack<?>) {
				if (oldSelectedElement instanceof MPlaceholder) {
					oldSelectedElement = ((MPlaceholder) oldSelectedElement).getRef();
				}
				internalFixContext(part, oldSelectedElement);
			}
		}
	}

	private IEclipseContext getSubContext(MUIElement element) {
		if (element instanceof MContext) {
			return ((MContext) element).getContext();
		} else if (element instanceof MElementContainer<?>) {
			Object selectedElement = ((MElementContainer<?>) element).getSelectedElement();
			if (selectedElement instanceof MContext) {
				return ((MContext) selectedElement).getContext();
			} else if (selectedElement instanceof MElementContainer<?>) {
				return getSubContext((MUIElement) selectedElement);
			}
		}
		return null;
	}

	private void internalFixContext(MPart part, MUIElement oldSelectedElement) {
		if (oldSelectedElement == null) {
			return;
		}

		MContext parentPart = getParentWithContext(oldSelectedElement);
		if (parentPart == null) {
			// technically this shouldn't happen as there should be an MWindow somewhere
			return;
		}
		IEclipseContext parentContext = parentPart.getContext();
		IEclipseContext oldContext = getSubContext(oldSelectedElement);
		Object child = parentContext.getActiveChild();
		if (child == null || oldContext == null || child == oldContext) {
			if (part == null) {
				// TBD this should not be necessary; deactivation is missing somewhere
				IEclipseContext currentActive = parentContext.getActiveChild();
				if (currentActive != null)
					currentActive.deactivate();
			} else
				part.getContext().activate();
		}
	}

	@Override
	public MPart findPart(String id) {
		List<MPart> parts = getParts(MPart.class, id);
		return parts.size() > 0 ? parts.get(0) : null;
	}

	private <T> List<T> getParts(Class<T> cls, String id) {
		return modelService.findElements(workbenchWindow, id, cls, null,
				EModelService.OUTSIDE_PERSPECTIVE | EModelService.IN_ACTIVE_PERSPECTIVE
						| EModelService.IN_SHARED_AREA);
	}

	@Override
	public Collection<MPart> getParts() {
		return getParts(MPart.class, null);
	}

	@Override
	public boolean isPartVisible(MPart part) {
		if (isInActivePerspective(part)) {
			MUIElement element = part;
			MElementContainer<?> parent = part.getParent();
			if (parent == null) {
				// might be a shared part
				element = part.getCurSharedRef();
				if (element == null) {
					return false;
				}

				parent = element.getParent();
				if (parent == null) {
					return false;
				}
			}

			if (parent instanceof MPartStack && parent.getSelectedElement() != element) {
				return false;
			}
			if (!element.isVisible()) {
				return false;
			}
			if (isMinimized(parent) || isMinimized(element)) {
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean isMinimized(MUIElement elt) {
		List<String> tags = elt.getTags();
		return tags.contains(IPresentationEngine.MINIMIZED) && !tags.contains(IPresentationEngine.ACTIVE);
	}

	private boolean isInActivePerspective(MUIElement element) {
		if (modelService.isHostedElement(element, getWindow()))
			return true;
		MPerspective persp = modelService.getPerspectiveFor(element);
		if (persp == null) {
			List<MUIElement> allPerspectiveElements = modelService.findElements(workbenchWindow, null, MUIElement.class,
					null, EModelService.PRESENTATION);
			return allPerspectiveElements.contains(element);
		}
		return persp == persp.getParent().getSelectedElement();
	}

	private boolean isInContainer(MUIElement element) {
		if (modelService.isHostedElement(element, getWindow()))
			return true;
		List<MUIElement> allPerspectiveElements = modelService.findElements(workbenchWindow, null,
				MUIElement.class, null, EModelService.PRESENTATION);
		return allPerspectiveElements.contains(element);
	}

	boolean isInContainer(MElementContainer<?> container, MUIElement element) {
		for (Object object : container.getChildren()) {
			if (object == element) {
				return true;
			} else if (object instanceof MElementContainer<?>) {
				if (isInContainer((MElementContainer<?>) object, element)) {
					return true;
				}
			} else if (object instanceof MPlaceholder) {
				MUIElement ref = ((MPlaceholder) object).getRef();
				if (ref == element) {
					return true;
				} else if (ref instanceof MElementContainer<?>) {
					if (isInContainer((MElementContainer<?>) ref, element)) {
						return true;
					}
				}
			} else if (object instanceof MPerspective) {
				MPerspective persp = (MPerspective) object;
				for (MWindow dw : persp.getWindows()) {
					if (isInContainer(dw, element))
						return true;
				}
			} else if (object instanceof MWindow) {
				MWindow win = (MWindow) object;
				for (MWindow dw : win.getWindows()) {
					if (isInContainer(dw, element))
						return true;
				}
			}
		}

		if (container instanceof MWindow) {
			MWindow win = (MWindow) container;
			for (MWindow dw : win.getWindows()) {
				if (isInContainer(dw, element))
					return true;
			}
		}

		if (container instanceof MPerspective) {
			MPerspective persp = (MPerspective) container;
			for (MWindow dw : persp.getWindows()) {
				if (isInContainer(dw, element))
					return true;
			}
		}

		return false;
	}

	MPlaceholder getLocalPlaceholder(MUIElement part) {
		return modelService.findPlaceholderFor(getWindow(), part);
	}

	@Override
	public boolean isPartOrPlaceholderInPerspective(String elementId, MPerspective perspective) {
		List<MPart> findElements = modelService.findElements(perspective, elementId, MPart.class);
		if (!findElements.isEmpty()) {
			MPart part = findElements.get(0);

			// if that is a shared part, check the placeholders
			if (workbenchWindow.getSharedElements().contains(part)) {
				List<MPlaceholder> placeholders = modelService.findElements(perspective, null, MPlaceholder.class,
						null);
				for (MPlaceholder mPlaceholder : placeholders) {
					if (mPlaceholder.getRef() == part && mPlaceholder.isVisible() && mPlaceholder.isToBeRendered()) {
						return true;
					}
				}
				return false;
			}
			// not a shared part
			return part.isVisible() && part.isToBeRendered();
		}
		return false;
	}

	@Override
	public void switchPerspective(MPerspective perspective) {
		Assert.isNotNull(perspective);
		MWindow window = getWindow();
		if (window != null && isInContainer(window, perspective)) {
			perspective.getParent().setSelectedElement(perspective);
			List<MPart> newPerspectiveParts = modelService.findElements(perspective, null,
					MPart.class, null);
			// if possible, keep the same active part across perspective switches
			IEclipseContext eclipseContext = perspective.getContext();
			if (newPerspectiveParts.contains(activePart)
					&& partActivationHistory.isValid(perspective, activePart)) {
				MPart target = activePart;
				IEclipseContext activeChild = activePart.getContext().getParent().getActiveChild();
				if (activeChild != null) {
					activeChild.deactivate();
				}
				if (target.getContext() != null && target.getContext().get(MPerspective.class) != null
						&& target.getContext().get(MPerspective.class).getContext() == eclipseContext) {
					target.getContext().activateBranch();
				} else if (eclipseContext != null) {
					eclipseContext.activate();
				}

				modelService.bringToTop(target);
				activate(target, true, false);
				UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_SWITCHED, perspective);
				return;
			}

			MPart newActivePart = eclipseContext != null ? eclipseContext.getActiveLeaf().get(MPart.class) : null;
			if (newActivePart == null) {
				// whatever part was previously active can no longer be found, find another one
				MPart candidate = partActivationHistory.getActivationCandidate(perspective);
				if (candidate != null) {
					modelService.bringToTop(candidate);
					activate(candidate, true, false);
					UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_SWITCHED, perspective);
					return;
				}
			}

			// there seems to be no parts in this perspective, just activate it as is then
			if (newActivePart == null) {
				modelService.bringToTop(perspective);
				if (eclipseContext != null) {
					eclipseContext.activate();
				}
			} else {
				if ((modelService.getElementLocation(newActivePart) & EModelService.IN_SHARED_AREA) != 0) {
					if (newActivePart.getParent() != null
							&& newActivePart.getParent().getSelectedElement() != newActivePart) {
						newActivePart = (MPart) newActivePart.getParent().getSelectedElement();
					}
				}
				activate(newActivePart, true, false);
				UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_SWITCHED, perspective);
			}
		}
	}

	@Override
	public java.util.Optional<MPerspective> switchPerspective(String perspectiveId) {
		List<MPerspective> result = modelService.findElements(getWindow(), perspectiveId, MPerspective.class, null);
		if (!result.isEmpty()) {
			MPerspective perspective = result.get(0);
			switchPerspective(perspective);
			return java.util.Optional.of(perspective);
		}
		logger.error("Perspective with ID " + perspectiveId + " not found in the current window."); //$NON-NLS-1$ //$NON-NLS-2$

		return java.util.Optional.empty();
	}

	@Override
	public void activate(MPart part) {
		activate(part, true);
	}

	@Override
	public void activate(MPart part, boolean requiresFocus) {
		activate(part, requiresFocus, true);
	}

	private void activate(MPart part, boolean requiresFocus, boolean activateBranch) {
		if (part == null) {
			if (constructed && activePart != null) {
				if (Policy.DEBUG_FOCUS) {
					Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Deactivated: " + activePart, null);//$NON-NLS-1$
				}
				firePartDeactivated(activePart);
			}
			activePart = part;
			return;
		}

		if (Policy.DEBUG_FOCUS) {
			Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Activating " + part, null);//$NON-NLS-1$
		}

		// Delegate activations to a CompositePart's inner part (if any)
		if (part instanceof MCompositePart) {
			if (part.getContext() != null) {
				IEclipseContext pContext = part.getContext();
				if (pContext.getActiveLeaf() != null) {
					MPart inner = pContext.getActiveLeaf().get(MPart.class);
					if (inner != null) {
						part = inner;
					}
				}
			}
		}

		// only activate parts that is under our control
		if (!isInContainer(part)) {
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Activation failed, part is not in container: " + part, null);//$NON-NLS-1$
			}
			return;
		}

		MWindow window = getWindow();
		IEclipseContext windowContext = window.getContext();
		// check if the active part has changed or if we are no longer the active window
		if (windowContext.getParent().getActiveChild() == windowContext && part == activePart) {
			// insert it in the beginning of the activation history, it may not have been inserted
			// pending when this service was instantiated
			partActivationHistory.prepend(part);
			UIEvents.publishEvent(UIEvents.UILifeCycle.ACTIVATE, part);
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Trying to activate already active part: " + part, null);//$NON-NLS-1$
			}
			return;
		}
		if (contextService != null) {
			contextService.deferUpdates(true);
		}
		if (contextManager != null) {
			contextManager.deferUpdates(true);
		}

		MPart lastActivePart = activePart;
		activePart = part;

		if (constructed && lastActivePart != null && lastActivePart != activePart) {
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Deactivated: " + lastActivePart, null);//$NON-NLS-1$
			}
			firePartDeactivated(lastActivePart);
		}

		try {
			// record any sibling into the activation history if necessary, this will allow it to be
			// reselected again in the future as it will be an activation candidate in the future,
			// this
			// prevents other unrendered elements from being selected arbitrarily which would cause
			// unwanted bundle activation
			recordStackActivation(part);

			delegateBringToTop(part);
			window.getParent().setSelectedElement(window);

			partActivationHistory.activate(part, activateBranch);

			if (requiresFocus) {
				IEclipseContext context = part.getContext();
				if (context != null) {
					IPresentationEngine pe = context.get(IPresentationEngine.class);
					pe.focusGui(part);
				}
			}

			// store the activation time to sort the parts in MRU order
			part.getTransientData().put(PART_ACTIVATION_TIME, Long.valueOf(System.currentTimeMillis()));

			firePartActivated(part);
			UIEvents.publishEvent(UIEvents.UILifeCycle.ACTIVATE, part);
		} finally {
			if (contextService != null) {
				contextService.deferUpdates(false);
			}
			if (contextManager != null) {
				contextManager.deferUpdates(false);
			}
			if (Policy.DEBUG_FOCUS) {
				Activator.trace(Policy.DEBUG_FOCUS_FLAG, "Activation done: " + part, null);//$NON-NLS-1$
			}
		}
	}

	private void delegateBringToTop(MPart part) {
		modelService.bringToTop(part);
		createElement(part);
	}

	/**
	 * Records the specified parent part's selected element in the activation history if the parent
	 * is a stack.
	 *
	 * @param part
	 *            the part whose parent's selected element should be checked for activation history
	 *            recording
	 */
	private void recordStackActivation(MPart part) {
		MElementContainer<? extends MUIElement> parent = part.getParent();
		if (parent instanceof MGenericStack) {
			recordSelectedActivation(parent);
		} else if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			if (placeholder != null) {
				parent = placeholder.getParent();
				if (parent instanceof MGenericStack) {
					recordSelectedActivation(parent);
				}
			}
		}
	}

	/**
	 * Records the specified parent 's selected element in the activation history.
	 *
	 * @param parent
	 *            the element whose selected element should be checked for activation history
	 *            recording
	 */
	private void recordSelectedActivation(MElementContainer<? extends MUIElement> parent) {
		MUIElement selectedElement = parent.getSelectedElement();
		if (selectedElement instanceof MPart) {
			partActivationHistory.append((MPart) selectedElement);
		} else if (selectedElement instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) selectedElement).getRef();
			if (ref instanceof MPart) {
				partActivationHistory.append((MPart) ref);
			}
		}
	}

	@Override
	public MPart getActivePart() {
		return activePart;
	}

	@Override
	public MPart createPart(String partDescriptorId) {
		MPartDescriptor descriptor = modelService.getPartDescriptor(partDescriptorId);
		return modelService.createPart(descriptor);
	}

	@Override
	public MPlaceholder createSharedPart(String id) {
		return createSharedPart(id, false);
	}

	@Override
	public MPlaceholder createSharedPart(String id, boolean force) {
		MWindow sharedWindow = getWindow();
		// Do we already have the part to share?
		MPart sharedPart = null;

		// check for existing parts if necessary
		boolean secondaryId = false;
		String descId = id;
		MElementContainer<MUIElement> sharedPlaceHolderParent = null;
		if (!force) {
			int colonIndex = id.indexOf(':');
			if (colonIndex >= 0) {
				String remId = ""; //$NON-NLS-1$
				try {
					remId = id.substring(colonIndex + 1);
				} catch (StringIndexOutOfBoundsException e) {
					// do nothing
				}
				if (!remId.trim().equals("*")) {//$NON-NLS-1$
					for (MUIElement element : sharedWindow.getSharedElements()) {
						if (element.getElementId().equals(descId)) {
							sharedPart = (MPart) element;
							MPlaceholder ph = sharedPart.getCurSharedRef();
							if (ph != null) {
								sharedPlaceHolderParent = ph.getParent();
							}
							break;
						}
					}
				}
				if (sharedPart == null) {
					secondaryId = true;
					descId = id.substring(0, colonIndex);
					descId += ":*"; //$NON-NLS-1$
				}
			}
			if (sharedPart == null) {
				for (MUIElement element : sharedWindow.getSharedElements()) {
					if (element.getElementId().equals(descId)) {
						sharedPart = (MPart) element;
						break;
					}
				}
			}
		}

		if (sharedPart == null || secondaryId) {
			MPartDescriptor descriptor = modelService.getPartDescriptor(id);
			sharedPart = modelService.createPart(descriptor);
			if (sharedPart == null) {
				return null;
			}
			MPart active = getActivePart();
			if (secondaryId && active != null) {
				MElementContainer<MUIElement> parent = active.getParent();
				MPlaceholder sharedRef = active.getCurSharedRef();
				if (sharedRef != null) {
					parent = sharedRef.getParent();
				}
				while (!(MPerspective.class.isInstance(parent) || MWindow.class.isInstance(parent))) {
					if (parent.getParent() == null) {
						break;
					}
					parent = parent.getParent();
				}

				List<MPlaceholder> phs = modelService.findElements(parent, descId, MPlaceholder.class);
				if (phs.size() == 1) {
					MPlaceholder ph = phs.get(0);
					sharedPlaceHolderParent = ph.getParent();
					sharedPart.setCloseable(ph.isCloseable());
					sharedPart.getTags().addAll(ph.getTags());
				}
			}

			// Replace the id to ensure that multi-instance parts work correctly
			sharedPart.setElementId(id);

			sharedWindow.getSharedElements().add(sharedPart);
		}

		return createSharedPart(sharedPart, sharedPlaceHolderParent);
	}

	private MPlaceholder createSharedPart(MPart sharedPart, MElementContainer<MUIElement> sharedPlaceHolderParent) {
		// Create and return a reference to the shared part
		MPlaceholder sharedPartRef = modelService.createModelElement(MPlaceholder.class);
		sharedPartRef.setElementId(sharedPart.getElementId());
		sharedPartRef.setRef(sharedPart);
		sharedPartRef.setCloseable(sharedPart.isCloseable());
		sharedPartRef.getTags().addAll(sharedPart.getTags());
		if (sharedPlaceHolderParent != null) {
			sharedPartRef.setParent(sharedPlaceHolderParent);
		}
		return sharedPartRef;
	}

	/**
	 * Adds a part to the current container if it isn't already in the container. The part may still
	 * be added to the container if the part supports having multiple copies of itself in a given
	 * container.
	 *
	 * @param providedPart
	 *            the part to add
	 * @param localPart
	 *            a part that shares attributes with <code>providedPart</code>, for example, it may
	 *            have been backed by the same part descriptor, this part may already be in the
	 *            current container
	 * @return a part that has been added to the current container, note that this may not
	 *         necessarily be <code>providedPart</code>
	 * @see MPartDescriptor#isAllowMultiple()
	 */
	private MPart addPart(MPart providedPart, MPart localPart) {
		MPartDescriptor descriptor = modelService.getPartDescriptor(providedPart.getElementId());
		if (descriptor == null) {
			// there is no part descriptor backing the provided part, just add it to the container
			// if it's not already there
			if (!isInContainer(providedPart)) {
				adjustPlaceholder(providedPart);
				addToLastContainer(null, providedPart);
			}
		} else {
			if (providedPart != localPart && !descriptor.isAllowMultiple()) {
				// multiple copies of this part are not allowed, just return the local one
				return localPart;
			}

			// already in the container, return as is
			if (isInContainer(providedPart)) {
				return providedPart;
			}

			// corrects this part's placeholder if necessary
			adjustPlaceholder(providedPart);

			String category = descriptor.getCategory();
			if (category == null) {
				// no category, just add it to the end
				addToLastContainer(null, providedPart);
			} else if ("org.eclipse.e4.primaryDataStack".equals(category)) { //$NON-NLS-1$
				MElementContainer<? extends MUIElement> container = getContainer();
				MUIElement area = modelService.find("org.eclipse.ui.editorss", container); //$NON-NLS-1$

				MPartStack activeStack = null;
				if (area instanceof MPlaceholder
						&& ((MPlaceholder) area).getRef() instanceof MArea) {
					// Find the currently 'active' stack in the area
					MArea a = (MArea) ((MPlaceholder) area).getRef();
					MUIElement curActive = a.getSelectedElement();
					while (curActive instanceof MElementContainer<?>) {
						if (curActive instanceof MPartStack) {
							activeStack = (MPartStack) curActive;
							break;
						}
						MElementContainer<?> curContainer = (MElementContainer<?>) curActive;
						curActive = curContainer.getSelectedElement();
					}
				}

				if (activeStack != null) {
					activeStack.getChildren().add(providedPart);
				} else {
					// Find the first visible stack in the area
					List<MPartStack> sharedStacks = modelService.findElements(area, null, MPartStack.class);
					if (sharedStacks.size() > 0) {
						for (MPartStack stack : sharedStacks) {
							if (stack.isToBeRendered()) {
								stack.getChildren().add(providedPart);
								break;
							}
						}
					} else {
						addToLastContainer(null, providedPart);
					}
				}
			} else {
				@SuppressWarnings("rawtypes")
				List<MElementContainer> containers = modelService.findElements(getContainer(),
						null, MElementContainer.class, Collections.singletonList(category),
						EModelService.PRESENTATION);
				if (containers.isEmpty()) {
					// couldn't find any containers with the specified tag, just add it to the
					// end
					addToLastContainer(category, providedPart);
				} else {
					// add the part to the container
					MElementContainer<MPartSashContainerElement> container = containers.get(0);
					MPlaceholder placeholder = providedPart.getCurSharedRef();
					if (placeholder == null) {
						container.getChildren().add(providedPart);
					} else {
						container.getChildren().add(placeholder);
					}
				}
			}
		}
		return providedPart;
	}

	private void adjustPlaceholder(MPart part) {
		if (isShared(part)) {
			MPlaceholder placeholder = part.getCurSharedRef();
			// if this part doesn't have any placeholders, we need to make one
			if (placeholder == null
			// alternatively, if it has one but it's not in the current container, then we
			// need to spawn another one as we don't want to reuse the same one and end up
			// shifting that placeholder to the current container during the add operation
					|| (placeholder.getParent() != null && !isInContainer(placeholder))) {
				placeholder = createSharedPart(part, null);
				part.setCurSharedRef(placeholder);
			}
		}
	}

	private boolean isShared(MPart part) {
		return getWindow().getSharedElements().contains(part);
	}

	private void addToLastContainer(String category, MPart part) {
		// OK, we haven't found an explicit placeholder;
		// If this is a multi-instance view see if there's a 'global' placeholder
		String partId = part.getElementId();
		int colonIndex = partId == null ? -1 : partId.indexOf(':');
		if (colonIndex >= 0) {
			String descId = part.getElementId().substring(0, colonIndex);
			descId += ":*"; //$NON-NLS-1$
			List<MPlaceholder> phList = modelService.findElements(workbenchWindow, descId,
					MPlaceholder.class, null, EModelService.PRESENTATION);
			if (phList.size() > 0) {
				MUIElement phParent = phList.get(0).getParent();
				if (phParent instanceof MPartStack) {
					MPartStack theStack = (MPartStack) phParent;
					int phIndex = theStack.getChildren().indexOf(phList.get(0));
					adjustPlaceholder(part);
					MPlaceholder placeholder = part.getCurSharedRef();
					if (placeholder == null) {
						theStack.getChildren().add(phIndex, part);
					} else {
						theStack.getChildren().add(phIndex, placeholder);
					}
					return;
				}
			}
		}

		@SuppressWarnings("unchecked")
		MElementContainer<MUIElement> lastContainer = (MElementContainer<MUIElement>) getLastContainer();
		MPlaceholder placeholder = part.getCurSharedRef();
		if (placeholder == null) {
			lastContainer.getChildren().add(part);
		} else {
			lastContainer.getChildren().add(placeholder);
		}

		if (category != null) {
			lastContainer.getTags().add(category);
		}
	}

	private MElementContainer<? extends MUIElement> getLastContainer() {
		MElementContainer<? extends MUIElement> searchRoot = getContainer();
		@SuppressWarnings("unchecked")
		List<MUIElement> children = (List<MUIElement>) searchRoot.getChildren();
		if (children.isEmpty()) {
			MPartStack stack = modelService.createModelElement(MPartStack.class);
			children.add(stack);
			return stack;
		}

		MElementContainer<?> lastContainer = getLastContainer(searchRoot, children);
		if (lastContainer instanceof MPartStack) {
			return lastContainer;
		}

		// No stacks found make one and add it
		MPartStack stack = modelService.createModelElement(MPartStack.class);
		stack.setElementId("CreatedByGetLastContainer"); //$NON-NLS-1$
		if (children.get(0) instanceof MPartSashContainer) {
			MPartSashContainer psc = (MPartSashContainer) children.get(0);
			psc.getChildren().add(stack);
		} else {
			// We need a sash so 'insert' the new stack
			modelService.insert(stack, (MPartSashContainerElement) children.get(0),
					EModelService.RIGHT_OF, 0.5f);
		}
		return stack;
	}

	private MElementContainer<? extends MUIElement> getLastContainer(MElementContainer<?> container, List<?> children) {
		if (children.isEmpty()) {
			return null;
		}

		for (int i = children.size() - 1; i > -1; i--) {
			Object muiElement = children.get(i);
			if (muiElement instanceof MElementContainer<?>) {
				MElementContainer<?> childContainer = (MElementContainer<?>) muiElement;
				MElementContainer<?> lastContainer = getLastContainer(childContainer,
						childContainer.getChildren());
				if (lastContainer != null) {
					return lastContainer;
				}
			}
		}
		return container;
	}

	/**
	 * Returns the parent container of the specified element. If one cannot be found, a check will
	 * be performed to see whether the element is being represented by a placeholder, if it is, the
	 * placeholder's parent will be returned, if any.
	 *
	 * @param element
	 *            the element to query
	 * @return the element's parent container, or the parent container of the specified element's
	 *         current placeholder, if it has one
	 */
	private MElementContainer<MUIElement> getParent(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		if (parent == null) {
			MPlaceholder placeholder = element.getCurSharedRef();
			if (placeholder == null) {
				@SuppressWarnings("unchecked")
				MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) getContainer();
				return findContainer(container, element);
			}
			return placeholder.getParent();
		}
		return parent;
	}

	private MElementContainer<MUIElement> findContainer(MElementContainer<MUIElement> container,
			MUIElement element) {
		for (MUIElement child : container.getChildren()) {
			if (child == element) {
				return container;
			} else if (child instanceof MPlaceholder) {
				MPlaceholder placeholder = (MPlaceholder) child;
				MUIElement ref = placeholder.getRef();
				if (ref == element) {
					return container;
				} else if (ref instanceof MElementContainer<?>) {
					@SuppressWarnings("unchecked")
					MElementContainer<MUIElement> ref2 = (MElementContainer<MUIElement>) ref;
					MElementContainer<MUIElement> match = findContainer(ref2, element);
					if (match != null) {
						return match;
					}
				}
			} else if (child instanceof MElementContainer<?>) {
				@SuppressWarnings("unchecked")
				MElementContainer<MUIElement> child2 = (MElementContainer<MUIElement>) child;
				MElementContainer<MUIElement> match = findContainer(child2, element);
				if (match != null) {
					return match;
				}
			}
		}
		return null;
	}

	private MUIElement getRemoveTarget(MPart part) {
		MPlaceholder placeholder = getLocalPlaceholder(part);
		return placeholder == null ? part : placeholder;
	}

	public MPart addPart(MPart part) {
		Assert.isNotNull(part);
		MPart localPart = findPart(part.getElementId());
		return addPart(part, localPart == null ? part : localPart);
	}

	@Override
	public MPart showPart(String id, PartState partState) {
		Assert.isNotNull(id);
		Assert.isNotNull(partState);

		MPart part = findPart(id);
		if (part == null) {
			MPartDescriptor descriptor = modelService.getPartDescriptor(id);
			part = modelService.createPart(descriptor);
			if (part == null) {
				return null;
			}
		}
		return showPart(part, partState);
	}

	@Override
	public MPart showPart(MPart part, PartState partState) {
		Assert.isNotNull(part);
		Assert.isNotNull(partState);

		MPart addedPart = addPart(part);
		MPlaceholder localPlaceholder = getLocalPlaceholder(addedPart);
		// correct the placeholder setting if necessary
		if (localPlaceholder != null && addedPart.getCurSharedRef() != localPlaceholder) {
			addedPart.setCurSharedRef(localPlaceholder);
		}

		switch (partState) {
		case ACTIVATE:
			activate(addedPart);
			return addedPart;
		case VISIBLE:
			MPart activePart = getActivePart();
			if (activePart == null
					|| (activePart != addedPart && getParent(activePart) == getParent(addedPart))) {
				delegateBringToTop(addedPart);
				activate(addedPart);
			} else {
				bringToTop(addedPart);
			}
			return addedPart;
		case CREATE:
			createElement(addedPart);
			return addedPart;
		}
		return addedPart;
	}

	private void createElement(MUIElement element) {
		if (modelService.isHostedElement(element, workbenchWindow)) {
			// assume the client has full control
			return;
		}

		MPlaceholder placeholder = element.getCurSharedRef();
		if (placeholder != null) {
			element.setToBeRendered(true);
			element = placeholder;
		}

		// render this element
		element.setToBeRendered(true);

		// render all of its parents
		MUIElement parentWindow = workbenchWindow;
		// determine the top parent that needs to be forcibly created
		MUIElement target = null;
		MElementContainer<MUIElement> parent = element.getParent();
		while (parent != null && parent != parentWindow) {
			parent.setToBeRendered(true);
			if (parent.getWidget() == null) {
				target = parent;
			}
			parent = parent.getParent();
		}
		if (target != null) {
			// force the element's parent hierarchy to be created
			engine.createGui(target);
		}
		// ask the engine to create the element
		if (element.getWidget() == null)
			engine.createGui(element);

		parent = element.getParent();
		if (parent != null && parent.getChildren().size() == 1) {
			// if we're the only child, set ourselves as the selected element
			parent.setSelectedElement(element);
		}
	}

	@Override
	public void requestActivation() {
		if (activePart == null) {
			MPart candidate = partActivationHistory.getActivationCandidate(getParts());
			if (candidate != null) {
				activate(candidate);
			}
		} else if (!partActivationHistory.isValid(activePart) || !getParts().contains(activePart)) {
			MPart candidate = partActivationHistory.getNextActivationCandidate(getParts(),
					activePart);
			if (candidate != null) {
				activate(candidate);
			}
		}
	}

	@Override
	public void hidePart(MPart part) {
		hidePart(part, false);
	}

	@Override
	public void hidePart(MPart part, boolean force) {
		// if part is not in a container, nothing to do
		if (!isInContainer(part)) {
			return;
		}

		MPlaceholder sharedRef = part.getCurSharedRef();
		MUIElement toBeRemoved = getRemoveTarget(part);
		MElementContainer<MUIElement> parent = getParent(toBeRemoved);
		List<MUIElement> children = parent.getChildren();

		// check if we're a placeholder but not actually the shared ref of the
		// part
		if (toBeRemoved != part && toBeRemoved instanceof MPlaceholder && sharedRef != toBeRemoved) {
			toBeRemoved.setToBeRendered(false);

			// if so, not much to do, remove ourselves if necessary but that's
			// it
			if (force || part.getTags().contains(REMOVE_ON_HIDE_TAG)) {
				parent.getChildren().remove(toBeRemoved);
			}
			return;
		}

		boolean isActiveChild = isActiveChild(part);
		MPart activationCandidate = null;
		// check if we're the active child
		if (isActiveChild) {
			// get the activation candidate if we are
			activationCandidate = partActivationHistory.getNextActivationCandidate(getParts(), part);
		}

		MPerspective thePersp = modelService.getPerspectiveFor(toBeRemoved);
		boolean needNewSel = thePersp == null || !thePersp.getTags().contains("PerspClosing"); //$NON-NLS-1$
		if (needNewSel) {
			if (parent.getSelectedElement() == toBeRemoved) {
				// if we're the selected element and we're going to be hidden,
				// need to select something else
				MUIElement candidate = partActivationHistory.getSiblingSelectionCandidate(part);
				candidate = candidate == null ? null
						: candidate.getCurSharedRef() == null ? candidate : candidate.getCurSharedRef();
				if (candidate != null && children.contains(candidate)) {
					parent.setSelectedElement(candidate);
				} else {
					for (MUIElement child : children) {
						if (child != toBeRemoved && child.isToBeRendered()) {
							parent.setSelectedElement(child);
							break;
						}
					}
				}
			}

			if (activationCandidate == null) {
				// nothing else to activate and we're the active child,
				// deactivate
				if (isActiveChild) {
					part.getContext().deactivate();
				}
			} else {
				// activate our candidate
				activate(activationCandidate);
			}
		}

		if (toBeRemoved != null) {
			toBeRemoved.setToBeRendered(false);
		} else {
			part.setToBeRendered(false);
		}

		if (parent.getSelectedElement() == toBeRemoved) {
			parent.setSelectedElement(null);
		}

		if (force || part.getTags().contains(REMOVE_ON_HIDE_TAG)) {
			children.remove(toBeRemoved);
		}
		// remove ourselves from the activation history also since we're being
		// hidden
		partActivationHistory.forget(getWindow(), part, toBeRemoved == part);
	}

	private boolean isActiveChild(MPart part) {
		IEclipseContext context = part.getContext();
		if (context == null) {
			return false;
		}
		IEclipseContext parent = context.getParent();
		return parent != null && parent.getActiveChild() == context;
	}

	@Override
	public Collection<MPart> getDirtyParts() {
		List<MPart> dirtyParts = new ArrayList<>();
		for (MPart part : getParts()) {
			if (part.isDirty()) {
				dirtyParts.add(part);
			}
		}
		return dirtyParts;
	}

	@Override
	public boolean savePart(MPart part, boolean confirm) {
		if (!part.isDirty()) {
			return true;
		}

		if (saveHandler != null) {
			return saveHandler.save(part, confirm);
		}

		Object client = part.getObject();
		try {
			ContextInjectionFactory.invoke(client, Persist.class, part.getContext());
		} catch (InjectionException e) {
			log("Failed to persist contents of part", "Failed to persist contents of part ({0})", //$NON-NLS-1$ //$NON-NLS-2$
					part.getElementId(), e);
			return false;
		} catch (RuntimeException e) {
			log("Failed to persist contents of part via DI", //$NON-NLS-1$
					"Failed to persist contents of part ({0}) via DI", part.getElementId(), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	@Override
	public boolean saveAll(boolean confirm) {
		Collection<MPart> dirtyParts = getDirtyParts();
		if (dirtyParts.isEmpty()) {
			return true;
		}
		if (saveHandler != null) {
			return saveHandler.saveParts(dirtyParts, confirm);
		}

		for (MPart dirtyPart : dirtyParts) {
			if (!savePart(dirtyPart, false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * "Container" here is: 1) a selected MPerspective, or, if none available 2) the MWindow for
	 * which this part service is created, or, if not available, 3) the MApplication.
	 */
	private MElementContainer<? extends MUIElement> getContainer() {
		MElementContainer<? extends MUIElement> outerContainer = (workbenchWindow != null) ? workbenchWindow
				: application;

		// see if we can narrow it down to the active perspective
		for (MElementContainer<? extends MUIElement> container = outerContainer; container != null;) {
			if (container instanceof MPerspective) {
				return container;
			}
			Object child = container.getSelectedElement();
			if (child == null) {
				break;
			}
			if (child instanceof MElementContainer<?>) {
				container = (MElementContainer<?>) child;
			} else {
				break;
			}
		}
		return outerContainer;
	}
}
