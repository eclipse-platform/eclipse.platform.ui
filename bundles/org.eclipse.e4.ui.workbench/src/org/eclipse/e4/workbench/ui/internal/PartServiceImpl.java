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
package org.eclipse.e4.workbench.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.injector.IObjectProvider;
import org.eclipse.e4.core.services.internal.context.ObjectProviderContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.IPartListener;
import org.eclipse.e4.workbench.modeling.ISaveHandler;
import org.eclipse.e4.workbench.modeling.ISaveHandler.Save;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PartServiceImpl implements EPartService {

	public static void addListener(IEventBroker broker) {
		EventHandler windowHandler = new EventHandler() {
			public void handleEvent(Event event) {
				Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (element instanceof MWindow) {
					MContext contextAware = (MContext) element;
					IEclipseContext context = contextAware.getContext();
					if (context != null) {
						context.set(EPartService.PART_SERVICE_ROOT, element);
					}
				}
			}
		};
		broker.subscribe(UIEvents.buildTopic(UIEvents.Context.TOPIC, UIEvents.Context.CONTEXT),
				windowHandler);
	}

	private EventHandler selectedHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object oldSelected = event.getProperty(UIEvents.EventTags.OLD_VALUE);
			Object selected = event.getProperty(UIEvents.EventTags.NEW_VALUE);

			MPart oldSelectedPart = oldSelected instanceof MPart ? (MPart) oldSelected : null;
			MPart selectedPart = selected instanceof MPart ? (MPart) selected : null;

			if (oldSelectedPart != null) {
				firePartHidden(oldSelectedPart);
			}

			if (selectedPart != null && selectedPart.isToBeRendered()) {
				firePartVisible(selectedPart);
				firePartBroughtToTop(selectedPart);
			}
		}
	};

	@Inject
	private MApplication application;

	/**
	 * This is the specific implementation. TODO: generalize it
	 */
	@Inject
	@Named(EPartService.PART_SERVICE_ROOT)
	@Optional
	// technically this should be a mandatory parameter
	private MElementContainer<MUIElement> rootContainer;

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

	private MPart activePart;

	private MPart lastActivePart;

	private ListenerList listeners = new ListenerList();

	private boolean constructed = false;

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) MPart p) {
		lastActivePart = activePart;
		activePart = p;

		if (constructed) {
			if (lastActivePart != null && lastActivePart != activePart) {
				firePartDeactivated(lastActivePart);
			}

			if (activePart != null) {
				firePartActivated(activePart);
			}
		}
	}

	@PostConstruct
	void postConstruct() {
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), selectedHandler);
		constructed = true;

		if (rootContainer == null) {
			// couldn't find one, we'll just track the application then, it is
			// questionable why someone would ask the application for the part
			// service though
			application.getContext().runAndTrack(new IRunAndTrack() {
				public boolean notify(ContextChangeEvent event) {

					IObjectProvider provider = event.getContext();
					IEclipseContext eventsContext = ((ObjectProviderContext) provider).getContext();

					IEclipseContext childContext = (IEclipseContext) eventsContext
							.getLocal(IContextConstants.ACTIVE_CHILD);
					if (childContext != null) {
						rootContainer = (MElementContainer<MUIElement>) childContext
								.get(MWindow.class.getName());
					}
					return true;
				}
			}, null);
		}
	}

	@PreDestroy
	void preDestroy() {
		constructed = false;
		eventBroker.unsubscribe(selectedHandler);
	}

	private void firePartActivated(MPart part) {
		for (Object listener : listeners.getListeners()) {
			((IPartListener) listener).partActivated(part);
		}
	}

	private void firePartDeactivated(MPart part) {
		for (Object listener : listeners.getListeners()) {
			((IPartListener) listener).partDeactivated(part);
		}
	}

	private void firePartHidden(MPart part) {
		for (Object listener : listeners.getListeners()) {
			((IPartListener) listener).partHidden(part);
		}
	}

	private void firePartVisible(MPart part) {
		for (Object listener : listeners.getListeners()) {
			((IPartListener) listener).partVisible(part);
		}
	}

	private void firePartBroughtToTop(MPart part) {
		for (Object listener : listeners.getListeners()) {
			((IPartListener) listener).partBroughtToTop(part);
		}
	}

	public void addPartListener(IPartListener listener) {
		listeners.add(listener);
	}

	public void removePartListener(IPartListener listener) {
		listeners.remove(listener);
	}

	private MContext getParentWithContext(MUIElement part) {
		MElementContainer<MUIElement> parent = part.getParent();
		while (parent != null) {
			if (parent instanceof MContext) {
				if (((MContext) parent).getContext() != null)
					return (MContext) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	public void bringToTop(MPart part) {
		if (isInContainer(part)) {
			part.setToBeRendered(true);
			internalBringToTop(part);
		}
	}

	private void internalBringToTop(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		MPart oldSelectedElement = (MPart) parent.getSelectedElement();
		if (oldSelectedElement != part) {
			parent.setSelectedElement(part);
			internalFixContext(part, oldSelectedElement);
		}
	}

	private void internalFixContext(MPart part, MPart oldSelectedElement) {
		MContext parentPart = oldSelectedElement == null ? null
				: getParentWithContext(oldSelectedElement);
		if (parentPart == null) {
			return;
		}
		IEclipseContext parentContext = parentPart.getContext();
		IEclipseContext oldContext = oldSelectedElement.getContext();
		Object child = parentContext.get(IContextConstants.ACTIVE_CHILD);
		if (child == oldContext) {
			parentContext.set(IContextConstants.ACTIVE_CHILD, part == null ? null : part
					.getContext());
		}
	}

	private MElementContainer<MUIElement> getActivePerspective() {
		if (rootContainer.getChildren().size() > 0
				&& rootContainer.getChildren().get(0) instanceof MPerspectiveStack) {
			// HACK!! find the perspective stack, should use an id ...
			MElementContainer<MUIElement> perspStack = (MElementContainer<MUIElement>) rootContainer
					.getChildren().get(0);
			return (MElementContainer<MUIElement>) perspStack.getSelectedElement();
		}
		return null;
	}

	public MPart findPart(String id) {
		MUIElement searchRoot = rootContainer;

		// If the model is using perspectives then re-direct the search to the
		// currently active perspective
		if (getActivePerspective() != null) {
			searchRoot = getActivePerspective();
		}

		MApplicationElement element = modelService.find(id, searchRoot);
		return element instanceof MPart ? (MPart) element : null;
	}

	public Collection<MPart> getParts() {
		return modelService.findElements(rootContainer, null, MPart.class, null);
	}

	public boolean isPartVisible(MPart part) {
		if (isInContainer(part)) {
			MElementContainer<?> parent = part.getParent();
			if (parent instanceof MPartStack) {
				return parent.getSelectedElement() == part;
			}

			return part.isVisible();
		}
		return false;
	}

	private boolean isInContainer(MPart part) {
		return isInContainer(rootContainer, part);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EPartService#activate(org.eclipse.e4.ui.model.application
	 * .MPart)
	 */
	public void activate(MPart part) {
		if (!isInContainer(part)) {
			return;
		}
		IEclipseContext curContext = part.getContext();
		MContext pwc = getParentWithContext(part);
		MUIElement curElement = part;
		while (pwc != null) {
			// Ensure that the UI model has the part 'on top'
			while (curElement != pwc) {
				MElementContainer<MUIElement> parent = curElement.getParent();
				curElement.setToBeRendered(true);
				if (parent.getSelectedElement() != curElement) {
					parent.setSelectedElement(curElement);
				}
				curElement = parent;
			}

			if (curContext == null) {
				curContext = part.getContext();
			}

			IEclipseContext parentContext = pwc.getContext();
			if (parentContext != null) {
				parentContext.set(IContextConstants.ACTIVE_CHILD, curContext);
				curContext = parentContext;
			}

			pwc = getParentWithContext((MUIElement) pwc);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EPartService#getActivePart()
	 */
	public MPart getActivePart() {
		return activePart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EPartService#deactivate(org.eclipse.e4.ui.model.application
	 * .MPart)
	 */
	public void deactivate(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		MPart oldSelectedElement = (MPart) parent.getSelectedElement();
		if (oldSelectedElement == part) {
			parent.setSelectedElement(null);
			internalFixContext(null, oldSelectedElement);
		}
	}

	private MPartDescriptor findDescriptor(String id) {
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getId().equals(id)) {
				return descriptor;
			}
		}
		return null;
	}

	private MPart createPart(MPartDescriptor descriptor) {
		return descriptor == null ? null : (MPart) EcoreUtil.copy((EObject) descriptor);
	}

	public MPart createPart(String id) {
		MPartDescriptor descriptor = findDescriptor(id);
		return createPart(descriptor);
	}

	private MPart addPart(MPart providedPart, MPart localPart) {
		if (providedPart == localPart && isInContainer(providedPart)) {
			return providedPart;
		}

		MPartDescriptor descriptor = findDescriptor(providedPart.getId());
		if (descriptor == null) {
			if (providedPart != localPart) {
				MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
				stack.getChildren().add(providedPart);
				rootContainer.getChildren().add(stack);
			}
		} else {
			if (providedPart != localPart && !descriptor.isAllowMultiple()) {
				return localPart;
			}

			String category = descriptor.getCategory();
			MUIElement container = modelService.find(category, rootContainer);
			if (container instanceof MElementContainer<?>) {
				((MElementContainer<MPart>) container).getChildren().add(providedPart);
			} else {
				MElementContainer<?> lastContainer = getLastContainer();
				((List) lastContainer.getChildren()).add(providedPart);

				String id = lastContainer.getId();
				if (id == null || id.length() == 0) {
					lastContainer.setId(category);
				}
			}
		}
		return providedPart;
	}

	private MElementContainer<?> getLastContainer() {
		MElementContainer<MUIElement> searchRoot = rootContainer;
		if (getActivePerspective() != null) {
			searchRoot = getActivePerspective();
			rootContainer = searchRoot;
		}

		List<MUIElement> children = searchRoot.getChildren();
		if (children.size() == 0) {
			MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
			searchRoot.getChildren().add(stack);
			return stack;
		}

		MElementContainer<?> lastContainer = getLastContainer(rootContainer, children);
		if (lastContainer == null) {
			MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
			searchRoot.getChildren().add(stack);
			return stack;
		}
		return lastContainer;
	}

	private MElementContainer<?> getLastContainer(MElementContainer<?> container, List<?> children) {
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

	private MPart showExistingPart(PartState partState, MPart providedPart, MPart localPart) {
		MPart part = addPart(providedPart, localPart);
		switch (partState) {
		case ACTIVATE:
			activate(part);
			return part;
		case VISIBLE:
			MPart activePart = getActivePart();
			if (activePart == part) {
				part.setToBeRendered(true);
			} else {
				if (activePart.getParent() == part.getParent()) {
					part.setToBeRendered(true);
					engine.createGui(part);
				} else {
					bringToTop(part);
				}
			}
			return part;
		case CREATE:
			part.setToBeRendered(true);
			engine.createGui(part);
			return part;
		}
		return part;
	}

	private MPart showNewPart(MPart part, PartState partState) {
		part = addPart(part, part);

		MPart activePart = getActivePart();
		if (activePart == null) {
			activate(part);
			return part;
		}

		switch (partState) {
		case ACTIVATE:
			activate(part);
			return part;
		case VISIBLE:
			if (activePart.getParent() != part.getParent()) {
				bringToTop(part);
			}
		}
		return part;
	}

	public MPart showPart(String id, PartState partState) {
		Assert.isNotNull(id);
		Assert.isNotNull(partState);

		MPart part = findPart(id);
		if (part != null) {
			return showPart(part, partState);
		}

		MPartDescriptor descriptor = findDescriptor(id);
		part = createPart(descriptor);
		if (part == null) {
			return null;
		}

		return showNewPart(part, partState);
	}

	public MPart showPart(MPart part, PartState partState) {
		Assert.isNotNull(part);
		Assert.isNotNull(partState);

		MPart localPart = findPart(part.getId());
		if (localPart != null) {
			return showExistingPart(partState, part, localPart);
		}
		return showNewPart(part, partState);
	}

	public void hidePart(MPart part) {
		if (isInContainer(part)) {
			part.setToBeRendered(false);

			if (part.getTags().contains(REMOVE_ON_HIDE_TAG)) {
				MElementContainer<MUIElement> parent = part.getParent();
				EList<MUIElement> children = parent.getChildren();
				children.remove(part);

				// FIXME: should be based on activation list
				if (parent.getSelectedElement() == part && !children.isEmpty()) {
					parent.setSelectedElement(children.get(0));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EPartService#getDirtyParts()
	 */
	public Collection<MPart> getDirtyParts() {
		List<MPart> dirtyParts = new ArrayList<MPart>();
		for (MPart part : getParts()) {
			if (part.isDirty()) {
				dirtyParts.add(part);
			}
		}
		return dirtyParts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EPartService#save(org.eclipse.e4.ui.model.application.
	 * MSaveablePart, boolean)
	 */
	public boolean savePart(MPart part, boolean confirm) {
		if (!part.isDirty()) {
			return true;
		}

		if (confirm && saveHandler != null) {
			switch (saveHandler.promptToSave(part)) {
			case NO:
				return true;
			case CANCEL:
				return false;
			}
		}

		Object client = part.getObject();
		try {
			ContextInjectionFactory.invoke(client, "doSave", part.getContext()); //$NON-NLS-1$
		} catch (InvocationTargetException e) {
			logger.error(e.getCause());
			return false;
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			Throwable throwable = status.getException();
			if (throwable == null) {
				logger.error(status.getMessage());
			} else {
				logger.error(throwable);
			}
			return false;
		}
		return true;
	}

	public boolean saveAll(boolean confirm) {
		Collection<MPart> dirtyParts = getDirtyParts();
		if (dirtyParts.isEmpty()) {
			return true;
		}

		if (confirm) {
			List<MPart> dirtyPartsList = Collections.unmodifiableList(new ArrayList<MPart>(
					dirtyParts));
			Save[] decisions = saveHandler.promptToSave(dirtyPartsList);
			for (Save decision : decisions) {
				if (decision == Save.CANCEL) {
					return false;
				}
			}

			boolean success = true;
			for (int i = 0; i < decisions.length; i++) {
				if (decisions[i] == Save.YES) {
					if (!savePart(dirtyPartsList.get(i), false)) {
						return false;
					}
				}
			}
			return success;
		}

		boolean success = true;
		for (MPart dirtyPart : dirtyParts) {
			if (!savePart(dirtyPart, false)) {
				success = false;
			}
		}
		return success;
	}
}
