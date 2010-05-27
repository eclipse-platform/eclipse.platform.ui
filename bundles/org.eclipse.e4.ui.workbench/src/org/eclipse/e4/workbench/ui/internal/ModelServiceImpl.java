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
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;

/**
 *
 */
public class ModelServiceImpl implements EModelService {
	/**
	 * Determine if the element passes the matching test for all non-null parameters.
	 * 
	 * @param element
	 *            The element to test
	 * @param id
	 *            The Id
	 * @param clazz
	 *            The class that element must be an instance of
	 * @param tagsToMatch
	 *            The tags to check, <b>all</b> the specified rags must be in the element's tags
	 * @return <code>true</code> iff all the tests pass
	 */
	private boolean match(MUIElement element, String id, Class clazz, List<String> tagsToMatch) {
		if (id != null && !id.equals(element.getElementId()))
			return false;

		if (clazz != null && !(clazz.isInstance(element)))
			return false;

		if (tagsToMatch != null) {
			List<String> elementTags = element.getTags();
			for (String tag : tagsToMatch) {
				if (!elementTags.contains(tag))
					return false;
			}
		}

		return true;
	}

	private <T> void findElementsRecursive(MUIElement searchRoot, String id,
			Class<? extends T> type, List<String> tagsToMatch, List<T> elements) {
		// are *we* a match ?
		if (match(searchRoot, id, type, tagsToMatch)) {
			if (!elements.contains((T) searchRoot))
				elements.add((T) searchRoot);
		}

		// Check regular containers
		if (searchRoot instanceof MElementContainer<?>) {
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;
			List<MUIElement> children = container.getChildren();
			for (MUIElement child : children) {
				findElementsRecursive(child, id, type, tagsToMatch, elements);
			}
		}

		// Search Trim
		if (searchRoot instanceof MTrimmedWindow) {
			MTrimmedWindow tw = (MTrimmedWindow) searchRoot;
			for (MTrimBar bar : tw.getTrimBars()) {
				findElementsRecursive(bar, id, type, tagsToMatch, elements);
			}
		}

		// Search Detached Windows
		if (searchRoot instanceof MWindow) {
			MWindow window = (MWindow) searchRoot;
			for (MWindow dw : window.getWindows()) {
				findElementsRecursive(dw, id, type, tagsToMatch, elements);
			}
		}
		if (searchRoot instanceof MPerspective) {
			MPerspective persp = (MPerspective) searchRoot;
			for (MWindow dw : persp.getWindows()) {
				findElementsRecursive(dw, id, type, tagsToMatch, elements);
			}
		}
		// Search shared elements
		if (searchRoot instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) searchRoot;
			findElementsRecursive(ph.getRef(), id, type, tagsToMatch, elements);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.workbench.modeling.EModelService#getAllElements(org.eclipse.e4.ui.model.
	 * application.MUIElement, java.lang.String, java.lang.Class, java.util.List)
	 */
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch) {
		List<T> elements = new ArrayList<T>();
		findElementsRecursive(searchRoot, id, clazz, tagsToMatch, elements);
		return elements;
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

		List elements = findElements(searchRoot, id, null, null);
		if (elements.size() > 0)
			return (MUIElement) elements.get(0);
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
		MUIElement curParent = null;
		if (element.getCurSharedRef() != null)
			curParent = element.getCurSharedRef().getParent();
		else
			curParent = (MUIElement) ((EObject) element).eContainer();

		while (curParent != null) {
			if (curParent instanceof MContext) {
				return ((MContext) curParent).getContext();
			}

			if (curParent.getCurSharedRef() != null)
				curParent = curParent.getCurSharedRef().getParent();
			else
				curParent = (MUIElement) ((EObject) curParent).eContainer();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#bringToTop(org.eclipse.e4.ui.model.application
	 * .ui.basic.MWindow, org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	public void bringToTop(MWindow window, MUIElement element) {
		if (element instanceof MApplication)
			return;

		MUIElement parent = element.getParent();
		if (parent == null) {
			MPlaceholder ph = findPlaceholderFor(window, element);
			if (ph != null) {
				element = ph;
				parent = element.getParent();
			}
		}

		if (parent != null) {
			// Force the element to be rendered
			if (!element.isToBeRendered())
				element.setToBeRendered(true);

			((MElementContainer<MUIElement>) parent).setSelectedElement(element);
			bringToTop(window, parent);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#findPlaceholderFor(org.eclipse.e4.ui.model
	 * .application.ui.basic.MWindow, org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	public MPlaceholder findPlaceholderFor(MWindow window, MUIElement element) {
		List<MPerspectiveStack> psList = findElements(window, null, MPerspectiveStack.class, null);
		if (psList.size() != 1)
			return null;
		MPerspectiveStack pStack = psList.get(0);
		MPerspective persp = pStack.getSelectedElement();
		if (persp == null)
			return null;

		List<MPlaceholder> phList = findElements(persp, null, MPlaceholder.class, null);
		for (MPlaceholder ph : phList) {
			if (ph.getRef() == element)
				return ph;
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
			MPlaceholder ph = MAdvancedFactory.INSTANCE.createPlaceholder();
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

	private void combine(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			MPartSashContainer newSash, boolean newFirst, int ratio) {
		MElementContainer<MUIElement> curParent = relTo.getParent();
		int index = curParent.getChildren().indexOf(relTo);
		curParent.getChildren().remove(relTo);
		if (newFirst) {
			newSash.getChildren().add(toInsert);
			newSash.getChildren().add(relTo);
		} else {
			newSash.getChildren().add(relTo);
			newSash.getChildren().add(toInsert);
		}

		// Set up the container data before adding the new sash to the model
		toInsert.setContainerData(Integer.toString(ratio));
		relTo.setContainerData(Integer.toString(100 - ratio));

		// add the new sash at the same location
		curParent.getChildren().add(index, newSash);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#insert(org.eclipse.e4.ui.model.application
	 * .MPartSashContainerElement, org.eclipse.e4.ui.model.application.MPartSashContainerElement,
	 * int, int)
	 */
	public void insert(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			int where, int ratio) {
		if (toInsert == null || relTo == null)
			return;

		// Ensure the ratio is sane
		if (ratio == 0)
			ratio = 10;
		if (ratio > 100)
			ratio = 90;

		// determine insertion order
		boolean newFirst = where == ABOVE || where == LEFT_OF;

		// The only thing we can add sashes to is an MPartSashContainer or an MWindow so
		// find the correct place to start the insertion
		MUIElement insertRoot = relTo.getParent();
		while (insertRoot != null && !(insertRoot instanceof MWindow)
				&& !(insertRoot instanceof MPartSashContainer)) {
			relTo = (MPartSashContainerElement) insertRoot;
			insertRoot = insertRoot.getParent();
		}

		if (insertRoot instanceof MWindow) {
			// OK, we're certainly going to need a new sash
			MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			newSash.setHorizontal(where == LEFT_OF || where == RIGHT_OF);
			combine(toInsert, relTo, newSash, newFirst, ratio);
		} else if (insertRoot instanceof MGenericTile<?>) {
			MGenericTile<MUIElement> curTile = (MGenericTile<MUIElement>) insertRoot;

			// do we need a new sash or can we extend the existing one?
			if (curTile.isHorizontal() && (where == ABOVE || where == BELOW)) {
				MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
				newSash.setHorizontal(false);
				newSash.setContainerData(relTo.getContainerData());
				combine(toInsert, relTo, newSash, newFirst, ratio);
			} else if (!curTile.isHorizontal() && (where == LEFT_OF || where == RIGHT_OF)) {
				MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
				newSash.setHorizontal(true);
				newSash.setContainerData(relTo.getContainerData());
				combine(toInsert, relTo, newSash, newFirst, ratio);
			} else {
				// We just need to add to the existing sash
				int relToIndex = relTo.getParent().getChildren().indexOf(relTo);
				if (newFirst) {
					curTile.getChildren().add(relToIndex, toInsert);
				} else {
					curTile.getChildren().add(relToIndex + 1, toInsert);
				}

				// Adjust the sash weights by taking the ratio
				int relToWeight = 100;
				if (relTo.getContainerData() != null) {
					try {
						relToWeight = Integer.parseInt(relTo.getContainerData());
					} catch (NumberFormatException e) {
					}
				}
				int toInsertWeight = (int) ((ratio / 100.0) * relToWeight + 0.5);
				relToWeight = relToWeight - toInsertWeight;
				relTo.setContainerData(Integer.toString(relToWeight));
				toInsert.setContainerData(Integer.toString(toInsertWeight));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.workbench.modeling.EModelService#detach(org.eclipse.e4.ui.model.application
	 * .MPartSashContainerElement)
	 */
	public void detach(MPartSashContainerElement element) {
		// Determine the correct parent for the new window
		MUIElement curParent = element.getParent();
		while (curParent != null && !(curParent instanceof MPerspective)
				&& !(curParent instanceof MWindow))
			curParent = curParent.getParent();

		if (curParent == null)
			return; // log??

		MWindow newWindow = BasicFactoryImpl.INSTANCE.createWindow();

		// HACK! should either be args or should be computed from the control being detached
		newWindow.setX(100);
		newWindow.setY(100);
		newWindow.setWidth(400);
		newWindow.setHeight(250);

		element.getParent().getChildren().remove(element);
		MWindowElement uiRoot = wrapElementForWindow(element);
		newWindow.getChildren().add(uiRoot);

		MElementContainer<MUIElement> windowParent = (MElementContainer<MUIElement>) curParent;
		windowParent.getChildren().add(newWindow);
	}

	/**
	 * @param element
	 * @return
	 */
	private MWindowElement wrapElementForWindow(MPartSashContainerElement element) {
		if (element instanceof MWindowElement)
			return (MWindowElement) element;
		return null;
	}
}
