/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPlaceholder;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.EList;

/**
 *
 */
public class ModelServiceImpl implements EModelService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EModelService#find(java.lang.String,
	 * org.eclipse.e4.ui.model.application.MElementContainer)
	 */
	public MUIElement find(String id, MUIElement searchRoot) {
		if (id == null || id.length() == 0)
			return null;

		if (id.equals(searchRoot.getId())) {
			return searchRoot;
		}

		if (searchRoot instanceof MElementContainer<?>) {
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;
			EList<MUIElement> children = container.getChildren();
			for (MUIElement child : children) {
				MUIElement foundElement = find(id, child);
				if (foundElement != null)
					return foundElement;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#getContainingContext(org.eclipse.e4.ui.model
	 * .application.MUIElement)
	 */
	public IEclipseContext getContainingContext(MUIElement element) {
		MElementContainer<MUIElement> curParent = element.getParent();
		while (curParent != null) {
			if (curParent instanceof MContext) {
				return ((MContext) curParent).getContext();
			}
			curParent = curParent.getParent();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MElementContainer)
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent) {
		move(element, newParent, -1, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MElementContainer, boolean)
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent,
			boolean leavePlaceholder) {
		move(element, newParent, -1, leavePlaceholder);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MElementContainer, int)
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index) {
		move(element, newParent, index, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MElementContainer, int, boolean)
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index,
			boolean leavePlaceholder) {
		// MElementContainer<MUIElement> curParent = element.getParent();
		// int curIndex = curParent.getChildren().indexOf(element);

		newParent.getChildren().add(index, element);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#swap(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MPlaceholder)
	 */
	public void swap(MUIElement element, MPlaceholder placeholder) {
		// TODO Auto-generated method stub

	}

}
