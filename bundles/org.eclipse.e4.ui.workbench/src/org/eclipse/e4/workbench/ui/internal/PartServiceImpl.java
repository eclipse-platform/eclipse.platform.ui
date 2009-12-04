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
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.modeling.EPartService;

public class PartServiceImpl implements EPartService {

	/**
	 * This is the specific implementation. TODO: generalize it
	 */
	@Inject
	private MWindow windowContainer;

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
		return findPart(windowContainer, id);
	}

	public Collection<MPart> getParts() {
		return getParts(new ArrayList<MPart>(), windowContainer);
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
		return isInContainer(windowContainer, part);
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
}
