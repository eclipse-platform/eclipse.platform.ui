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
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.EPartService;

public class PartServiceImpl implements EPartService {

	/**
	 * This is the specific implementation. TODO: generalize it
	 */
	@Inject
	private MWindow windowContainer;

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

	public void bringToTop(MPart part) {
		if (isInContainer(part)) {
			internalBringToTop(part);
		}
	}

	private void internalBringToTop(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		if (parent.getActiveChild() != part) {
			parent.setActiveChild(part);
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
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EPartService#getActivePart()
	 */
	public MPart getActivePart() {
		// TODO Auto-generated method stub
		return null;
	}
}
