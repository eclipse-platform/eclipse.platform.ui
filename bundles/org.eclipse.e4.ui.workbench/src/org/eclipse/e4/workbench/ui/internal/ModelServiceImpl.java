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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MGenericStack;
import org.eclipse.e4.ui.model.application.MGenericTile;
import org.eclipse.e4.ui.model.application.MPlaceholder;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 *
 */
public class ModelServiceImpl implements EModelService {

	/**
	 * Returns true if the given element matches the required search criteria. Any of the search
	 * criteria may be null, indicating not to check that field.
	 * 
	 * @param element
	 *            The element to test
	 * @param id
	 *            The id to match
	 * @param type
	 *            The model element type. This is the element's type (i.e. MPartStack)
	 * @param tagsToMatch
	 *            The tags to check. In order to be a match all the tags in this list must be
	 *            defined in the element's tags list.
	 * 
	 * @return True iff all non-null test fields match
	 */
	private boolean match(MUIElement element, String id, String type, List<String> tagsToMatch) {
		if (id != null && !id.equals(element.getId()))
			return false;

		EObject eObj = (EObject) element;
		String clsName = 'M' + eObj.eClass().getName();
		if (type != null && !type.equals(clsName))
			return false;

		if (tagsToMatch != null) {
			EList<String> elementTags = element.getTags();
			for (String tag : tagsToMatch) {
				if (!elementTags.contains(tag))
					return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EModelService#findAllElements(org.eclipse.e4.ui.model.
	 * application.MUIElement, java.lang.String, java.lang.String, java.util.List,
	 * java.util.Collection)
	 */
	public void findAllElements(MUIElement searchRoot, String id, String type,
			List<String> tagsToMatch, Collection<MUIElement> elements) {
		if (match(searchRoot, id, type, tagsToMatch)) {
			elements.add(searchRoot);
		}

		if (searchRoot instanceof MElementContainer<?>) {
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;
			EList<MUIElement> children = container.getChildren();
			for (MUIElement child : children) {
				findAllElements(child, id, type, tagsToMatch, elements);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EModelService#find(java.lang.String,
	 * org.eclipse.e4.ui.model.application.MElementContainer)
	 */
	public MUIElement find(String id, MUIElement searchRoot) {
		if (id == null || id.length() == 0)
			return null;

		List<MUIElement> elements = new ArrayList<MUIElement>();
		findAllElements(searchRoot, id, null, null, elements);
		if (elements.size() > 0)
			return elements.get(0);
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
		// Cache where we were
		MElementContainer<MUIElement> curParent = element.getParent();
		int curIndex = curParent.getChildren().indexOf(element);

		// Move the model element
		newParent.getChildren().add(index, element);

		if (leavePlaceholder) {
			MPlaceholder ph = MApplicationFactory.eINSTANCE.createPlaceholder();
			ph.setRef(element);
			curParent.getChildren().add(curIndex, ph);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#swap(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MPlaceholder)
	 */
	public void swap(MPlaceholder placeholder) {
		MUIElement element = placeholder.getRef();

		MElementContainer<MUIElement> elementParent = element.getParent();
		int elementIndex = elementParent.getChildren().indexOf(element);
		MElementContainer<MUIElement> phParent = placeholder.getParent();
		int phIndex = phParent.getChildren().indexOf(placeholder);

		// Remove the two elements from their respective parents
		elementParent.getChildren().remove(element);
		phParent.getChildren().remove(placeholder);

		// swap over the UIElement info
		boolean onTop = element.isOnTop();
		boolean vis = element.isVisible();
		boolean tbr = element.isToBeRendered();

		element.setOnTop(placeholder.isOnTop());
		element.setVisible(placeholder.isVisible());
		element.setToBeRendered(placeholder.isToBeRendered());

		placeholder.setOnTop(onTop);
		placeholder.setVisible(vis);
		placeholder.setToBeRendered(tbr);

		// Add the elements back into the new parents
		elementParent.getChildren().add(elementIndex, placeholder);
		phParent.getChildren().add(phIndex, element);

	}

	public void selectStackElement(MGenericStack<MUIElement> stack, MUIElement element) {
		assert (stack.getChildren().indexOf(element) >= 0);

		// First, get all the elements under the existing 'selected' element
		List<MUIElement> goingHidden = new ArrayList<MUIElement>();
		MUIElement curSel = stack.getSelectedElement();
		hideElementRecursive(curSel, goingHidden);

		// Now process any newly visible elements
		List<MUIElement> becomingVisible = new ArrayList<MUIElement>();
		showElementRecursive(element, becomingVisible);
	}

	private void hideElementRecursive(MUIElement element, List<MUIElement> goingHidden) {
		if (element.getWidget() == null)
			return;

		// Hide any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			element.setVisible(false);
		}

		goingHidden.add(element);

		if (element instanceof MGenericTile<?>) {
			MGenericTile<?> container = (MGenericTile<?>) element;
			for (MUIElement childElement : container.getChildren()) {
				hideElementRecursive(childElement, goingHidden);
			}
		} else if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being hidden
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			hideElementRecursive(curSel, goingHidden);
		}
	}

	private void showElementRecursive(MUIElement element, List<MUIElement> becomingVisible) {
		if (!element.isToBeRendered())
			return;

		if (element instanceof MPlaceholder) {
			swap((MPlaceholder) element);
			element = ((MPlaceholder) element).getRef();
		}

		// Show any floating windows
		if (element instanceof MWindow && element.getWidget() != null) {
			element.setVisible(true);
		}

		becomingVisible.add(element);

		if (element instanceof MGenericTile<?>) {
			MGenericTile<?> container = (MGenericTile<?>) element;
			for (MUIElement childElement : container.getChildren()) {
				showElementRecursive(childElement, becomingVisible);
			}
		} else if (element instanceof MGenericStack<?>) {
			// For stacks only the currently selected elements are being visible
			MGenericStack<?> container = (MGenericStack<?>) element;
			MUIElement curSel = container.getSelectedElement();
			showElementRecursive(curSel, becomingVisible);
		}
	}
}
