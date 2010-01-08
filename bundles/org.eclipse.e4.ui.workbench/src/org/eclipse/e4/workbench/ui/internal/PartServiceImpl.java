/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.modeling.EPartService;
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
		return findPart(rootContainer, id);
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

	private MPart findPart(MElementContainer<?> container, String id) {
		for (Object object : container.getChildren()) {
			if (object instanceof MPart) {
				MPart part = (MPart) object;
				if (id.equals(part.getId())) {
					return part;
				}
			} else if (object instanceof MElementContainer<?>) {
				MPart part = findPart((MElementContainer<?>) object, id);
				if (part != null) {
					return part;
				}
			}
		}

		return null;
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
			IEclipseContext parentContext = pwc.getContext();
			if (parentContext != null) {
				parentContext.set(IContextConstants.ACTIVE_CHILD, curContext);
				curContext = parentContext;
			}

			// Ensure that the UI model has the part 'on top'
			while (curElement != pwc) {
				MElementContainer<MUIElement> parent = curElement.getParent();
				if (parent.getActiveChild() != curElement)
					parent.setActiveChild(curElement);
				curElement = parent;
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
		MPart part = null;
		for (MPart localPart : getParts()) {
			if (localPart.getId().equals(id)) {
				part = localPart;
			}
		}

		if (part != null) {
			activate(part);
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

		// Wrap it in a stack - TBD - always?
		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		stack.getChildren().add(part);

		rootContainer.getChildren().add(stack);
		// 3) make it visible / active / re-layout
		// XXX part service somehow tied to a container window. After that fixed code will be:
		// partService.activate((MPart) copy);
		// but for now:
		// IEclipseContext parentContext = ((MContext)parent).getContext();
		// EPartService parentPartService = (EPartService)
		// parentContext.get(EPartService.class.getName());
		// parentPartService.activate(part);
		activate(part);
		return part;
	}
}
