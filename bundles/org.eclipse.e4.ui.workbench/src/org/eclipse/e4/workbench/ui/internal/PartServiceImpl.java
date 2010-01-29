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
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.ISaveHandler;
import org.eclipse.e4.workbench.modeling.ISaveHandler.Save;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.UIEvents;
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

	@Inject
	private MApplication application;

	/**
	 * This is the specific implementation. TODO: generalize it
	 */
	@Inject
	@Named(EPartService.PART_SERVICE_ROOT)
	private MElementContainer<MUIElement> rootContainer;

	@Inject
	private IPresentationEngine engine;

	@Inject
	private EModelService modelService;

	@Inject
	private Logger logger;

	@Inject
	private ISaveHandler saveHandler;

	@Inject
	void setPart(@Optional @Named(IServiceConstants.ACTIVE_PART) MPart p) {
		activePart = p;
	}

	private MPart activePart;

	protected MContext getParentWithContext(MUIElement part) {
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

	protected IEclipseContext getContext(MPart part) {
		return part.getContext();
	}

	public void bringToTop(MPart part) {
		if (isInContainer(part)) {
			internalBringToTop(part);
		}
	}

	private void internalBringToTop(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		MPart oldActiveChild = (MPart) parent.getActiveChild();
		if (oldActiveChild != part) {
			parent.setActiveChild(part);
			internalFixContext(part, oldActiveChild);
		}
	}

	private void internalFixContext(MPart part, MPart oldActiveChild) {
		MContext parentPart = getParentWithContext(oldActiveChild);
		if (parentPart == null) {
			return;
		}
		IEclipseContext parentContext = parentPart.getContext();
		IEclipseContext oldContext = oldActiveChild.getContext();
		Object child = parentContext.get(IContextConstants.ACTIVE_CHILD);
		if (child == oldContext) {
			parentContext.set(IContextConstants.ACTIVE_CHILD, part == null ? null : part
					.getContext());
		}
	}

	public MPart findPart(String id) {
		MApplicationElement element = modelService.find(id, rootContainer);
		return element instanceof MPart ? (MPart) element : null;
	}

	public Collection<MPart> getParts() {
		return getParts(new ArrayList<MPart>(), rootContainer);
	}

	private Collection<MPart> getParts(Collection<MPart> parts,
			MElementContainer<?> elementContainer) {
		for (Object child : elementContainer.getChildren()) {
			if (child instanceof MPart) {
				parts.add((MPart) child);
			} else if (child instanceof MElementContainer<?>) {
				getParts(parts, (MElementContainer<?>) child);
			}
		}
		return parts;
	}

	public boolean isPartVisible(MPart part) {
		if (isInContainer(part)) {
			MElementContainer<MUIElement> parent = part.getParent();
			return parent.getActiveChild() == part;
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
		IEclipseContext curContext = getContext(part);
		MContext pwc = getParentWithContext(part);
		MUIElement curElement = part;
		while (pwc != null) {
			// Ensure that the UI model has the part 'on top'
			while (curElement != pwc) {
				MElementContainer<MUIElement> parent = curElement.getParent();
				if (parent.getActiveChild() != curElement) {
					parent.setActiveChild(curElement);
				}
				curElement = parent;
			}

			if (curContext == null) {
				curContext = getContext(part);
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
		MPart oldActiveChild = (MPart) parent.getActiveChild();
		if (oldActiveChild == part) {
			parent.setActiveChild(null);
			internalFixContext(null, oldActiveChild);
		}

	}

	public MPart showPart(String id) {
		return showPart(id, PartState.ACTIVATE);
	}

	public MPart showPart(String id, PartState partState) {
		Assert.isNotNull(id);

		MPart part = findPart(id);
		if (part != null) {
			switch (partState) {
			case ACTIVATE:
				activate(part);
				return part;
			case VISIBLE:
				MPart activePart = getActivePart();
				if (activePart != part) {
					if (activePart.getParent() == part.getParent()) {
						engine.createGui(part);
					} else {
						bringToTop(part);
					}
				}
				return part;
			case CREATE:
				engine.createGui(part);
				return part;
			}
			return part;
		}

		MPartDescriptor descriptorMatch = null;
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getId().equals(id)) {
				descriptorMatch = descriptor;
				break;
			}
		}

		if (descriptorMatch == null) {
			return null;
		}
		// 2) add a child under the parent
		// TBD only make a new one if multiple copies are allowed

		// TBD here we just copy descriptor for convenience; better would be to have an utility
		// to make a part based on the descriptor.
		// Create the part based on the descriptor
		// MPart part = MApplicationFactory.eINSTANCE.createPart();
		// part.setURI(descriptor.getURI());
		// part.setLabel(descriptor.getLabel());
		part = (MPart) EcoreUtil.copy((EObject) descriptorMatch);

		MApplicationElement container = modelService.find(descriptorMatch.getCategory(),
				rootContainer);
		if (container instanceof MElementContainer<?>) {
			((MElementContainer<MPart>) container).getChildren().add(part);
		} else { // wrap it in a stack
			MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
			stack.setId(descriptorMatch.getCategory());
			stack.getChildren().add(part);
			rootContainer.getChildren().add(stack);
		}

		MPart activePart = getActivePart();
		if (activePart == null) {
			activate(part);
			return part;
		}

		// 3) make it visible / active / re-layout
		switch (partState) {
		case ACTIVATE:
			activate(part);
			return part;
		case VISIBLE:
			if (activePart.getParent() != part.getParent()) {
				bringToTop(part);
			}
			return part;
		}
		return part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EPartService#getSaveableParts()
	 */
	public Collection<MSaveablePart> getSaveableParts() {
		List<MSaveablePart> saveableParts = new ArrayList<MSaveablePart>();
		for (MPart part : getParts()) {
			if (part instanceof MSaveablePart) {
				saveableParts.add((MSaveablePart) part);
			}
		}
		return saveableParts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EPartService#getDirtyParts()
	 */
	public Collection<MSaveablePart> getDirtyParts() {
		List<MSaveablePart> dirtyParts = new ArrayList<MSaveablePart>();
		for (MSaveablePart part : getSaveableParts()) {
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
	public boolean savePart(MSaveablePart part, boolean confirm) {
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
			logger.error(e.getStatus().getException());
			return false;
		}
		return true;
	}

	public boolean saveAll(boolean confirm) {
		Collection<MSaveablePart> dirtyParts = getDirtyParts();
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
					if (!savePart((MSaveablePart) dirtyPartsList.get(i), false)) {
						return false;
					}
				}
			}
			return success;
		}

		boolean success = true;
		for (MSaveablePart dirtyPart : dirtyParts) {
			if (!savePart(dirtyPart, false)) {
				success = false;
			}
		}
		return success;
	}
}
