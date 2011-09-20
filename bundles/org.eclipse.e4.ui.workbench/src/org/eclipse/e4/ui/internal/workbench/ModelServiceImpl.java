/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

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
			Class<? extends T> type, List<String> tagsToMatch, List<T> elements, int searchFlags) {
		Assert.isLegal(searchRoot != null);
		if (searchFlags == 0)
			return;

		// are *we* a match ?
		if (match(searchRoot, id, type, tagsToMatch)) {
			if (!elements.contains((T) searchRoot))
				elements.add((T) searchRoot);
		}

		// Check regular containers
		if (searchRoot instanceof MElementContainer<?>) {
			if (searchRoot instanceof MPerspectiveStack) {
				if ((searchFlags & IN_ANY_PERSPECTIVE) != 0) {
					// Search *all* the perspectives
					MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;
					List<MUIElement> children = container.getChildren();
					for (MUIElement child : children) {
						findElementsRecursive(child, id, type, tagsToMatch, elements, searchFlags);
					}
				} else if ((searchFlags & IN_ACTIVE_PERSPECTIVE) != 0) {
					// Only search the currently active perspective, if any
					MPerspective active = ((MPerspectiveStack) searchRoot).getSelectedElement();
					if (active != null) {
						findElementsRecursive(active, id, type, tagsToMatch, elements, searchFlags);
					}
				} else if ((searchFlags & IN_SHARED_AREA) != 0) {
					// Only recurse through the shared areas
					List<MArea> areas = findElements(searchRoot, null, MArea.class, null);
					for (MArea area : areas) {
						findElementsRecursive(area, id, type, tagsToMatch, elements, searchFlags);
					}
				}
			} else {
				MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;
				List<MUIElement> children = container.getChildren();
				for (MUIElement child : children) {
					findElementsRecursive(child, id, type, tagsToMatch, elements, searchFlags);
				}
			}
		}

		// Search Trim
		if (searchRoot instanceof MTrimmedWindow && (searchFlags & IN_TRIM) != 0) {
			MTrimmedWindow tw = (MTrimmedWindow) searchRoot;
			List<MTrimBar> bars = tw.getTrimBars();
			for (MTrimBar bar : bars) {
				findElementsRecursive(bar, id, type, tagsToMatch, elements, searchFlags);
			}
		}

		// Search Detached Windows
		if (searchRoot instanceof MWindow) {
			MWindow window = (MWindow) searchRoot;
			for (MWindow dw : window.getWindows()) {
				findElementsRecursive(dw, id, type, tagsToMatch, elements, searchFlags);
			}
		}
		if (searchRoot instanceof MPerspective) {
			MPerspective persp = (MPerspective) searchRoot;
			for (MWindow dw : persp.getWindows()) {
				findElementsRecursive(dw, id, type, tagsToMatch, elements, searchFlags);
			}
		}
		// Search shared elements
		if (searchRoot instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) searchRoot;

			// Don't search in shared areas unless the flag is set
			if (!(ph.getRef() instanceof MArea) || (searchFlags & IN_SHARED_AREA) != 0) {
				findElementsRecursive(ph.getRef(), id, type, tagsToMatch, elements, searchFlags);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.EModelService#findElements(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, java.lang.String, java.lang.Class, java.util.List)
	 */
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch) {
		List<T> elements = new ArrayList<T>();
		findElementsRecursive(searchRoot, id, clazz, tagsToMatch, elements, ANYWHERE);
		return elements;
	}

	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch, int searchFlags) {
		List<T> elements = new ArrayList<T>();
		findElementsRecursive(searchRoot, id, clazz, tagsToMatch, elements, searchFlags);
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#findPerspectiveElements(org.eclipse.e4
	 * .ui.model.application.ui.MUIElement, java.lang.String, java.lang.Class, java.util.List,
	 * boolean)
	 */
	public <T> List<T> findPerspectiveElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch) {
		List<T> elements = new ArrayList<T>();
		findElementsRecursive(searchRoot, id, clazz, tagsToMatch, elements, PRESENTATION);
		return elements;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.EModelService#find(java.lang.String,
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
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#countRenderableChildren(org.eclipse.e4
	 * .ui.model.application.ui.MUIElement)
	 */
	public int countRenderableChildren(MUIElement element) {
		if (!(element instanceof MElementContainer<?>))
			return 0;

		MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) element;
		int count = 0;
		List<MUIElement> kids = container.getChildren();
		for (MUIElement kid : kids) {
			if (kid.isToBeRendered())
				count++;
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#getContainingContext(org.eclipse.e4.ui
	 * .model .application.MUIElement)
	 */
	public IEclipseContext getContainingContext(MUIElement element) {
		return ModelUtils.getContainingContext(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.EModelService#cloneElement(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement, java.lang.String)
	 */
	public MUIElement cloneElement(MUIElement element, MSnippetContainer snippetContainer) {
		EObject eObj = (EObject) element;
		MUIElement clone = (MUIElement) EcoreUtil.copy(eObj);

		if (snippetContainer != null) {
			MUIElement snippet = findSnippet(snippetContainer, element.getElementId());
			if (snippet != null)
				snippetContainer.getSnippets().remove(snippet);
			snippetContainer.getSnippets().add(clone);
		}
		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.EModelService#cloneSnippet(org.eclipse.e4.ui.model.
	 * application.MApplication, java.lang.String)
	 */
	public MUIElement cloneSnippet(MSnippetContainer snippetContainer, String snippetId) {
		if (snippetContainer == null || snippetId == null || snippetId.length() == 0)
			return null;

		MApplicationElement elementToClone = null;
		for (MApplicationElement snippet : snippetContainer.getSnippets()) {
			if (snippetId.equals(snippet.getElementId())) {
				elementToClone = snippet;
				break;
			}
		}
		if (elementToClone == null)
			return null;

		EObject eObj = (EObject) elementToClone;
		MUIElement element = (MUIElement) EcoreUtil.copy(eObj);
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.EModelService#findSnippet(org.eclipse.e4.ui.model.
	 * application.ui.MSnippetContainer, java.lang.String)
	 */
	public MUIElement findSnippet(MSnippetContainer snippetContainer, String id) {
		if (snippetContainer == null || id == null || id.length() == 0)
			return null;

		List<MUIElement> snippets = snippetContainer.getSnippets();
		for (MUIElement snippet : snippets) {
			if (id.equals(snippet.getElementId()))
				return snippet;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#bringToTop(org.eclipse.e4.ui.model.application
	 * .ui.MUIElement)
	 */
	public void bringToTop(MUIElement element) {
		if (element instanceof MApplication)
			return;

		MWindow window = getTopLevelWindowFor(element);
		if (window == element) {
			if (!element.isToBeRendered()) {
				element.setToBeRendered(true);
			}

			window.getParent().setSelectedElement(window);
		} else {
			showElementInWindow(window, element);
		}
	}

	private void showElementInWindow(MWindow window, MUIElement element) {
		if (element instanceof MPartStack && !element.isVisible()) {
			String trimId = element.getElementId() + "(minimized)"; //$NON-NLS-1$
			MPerspective persp = getPerspectiveFor(element);
			if (persp != null)
				trimId = element.getElementId() + '(' + persp.getElementId() + ')';
			MToolControl trimCtrl = (MToolControl) find(trimId, window);
			if (trimCtrl != null && trimCtrl.getObject() != null) {
				IEclipseContext ctxt = EclipseContextFactory.create();
				ctxt.set("show", true); //$NON-NLS-1$
				ContextInjectionFactory.invoke(trimCtrl.getObject(), Execute.class, ctxt);
				ctxt.dispose();
			}
		}

		MUIElement parent = element.getParent();
		if (parent == null) {
			MPlaceholder ph = findPlaceholderFor(window, element);
			if (ph != null) {
				element = ph;
				parent = element.getParent();
			}
		}

		if (parent == null && element instanceof MWindow) {
			// no parent but is a window, could be a detached window then
			parent = (MUIElement) ((EObject) element).eContainer();
			if (parent != null) {
				// Force the element to be rendered
				if (!element.isToBeRendered()) {
					element.setToBeRendered(true);
				}

				if (window != parent) {
					showElementInWindow(window, parent);
				}
			}
		} else if (parent != null) {
			// Force the element to be rendered
			if (!element.isToBeRendered())
				element.setToBeRendered(true);

			((MElementContainer<MUIElement>) parent).setSelectedElement(element);
			if (window != parent) {
				showElementInWindow(window, parent);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#findPlaceholderFor(org.eclipse.e4.ui.model
	 * .application.ui.basic.MWindow, org.eclipse.e4.ui.model.application.ui.MUIElement)
	 */
	public MPlaceholder findPlaceholderFor(MWindow window, MUIElement element) {
		List<MPlaceholder> phList = findPerspectiveElements(window, null, MPlaceholder.class, null);
		List<MPlaceholder> elementRefs = new ArrayList<MPlaceholder>();
		for (MPlaceholder ph : phList) {
			if (ph.getRef() == element)
				elementRefs.add(ph);
		}

		if (elementRefs.size() == 0)
			return null;

		if (elementRefs.size() == 1)
			return elementRefs.get(0);

		// If there is more than one placeholder then return the one in the shared area
		for (MPlaceholder refPh : elementRefs) {
			int loc = getElementLocation(refPh);
			if ((loc & IN_SHARED_AREA) != 0)
				return refPh;
		}

		// Just return the first one
		return elementRefs.get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MElementContainer)
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent) {
		move(element, newParent, -1, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
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
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
	 * MUIElement, org.eclipse.e4.ui.model.application.MElementContainer, int)
	 */
	public void move(MUIElement element, MElementContainer<MUIElement> newParent, int index) {
		move(element, newParent, index, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#move(org.eclipse.e4.ui.model.application.
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
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#swap(org.eclipse.e4.ui.model.application.
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
		// To raise the granularity assume 100% == 10,000
		int adjustedPct = ratio * 100;
		toInsert.setContainerData(Integer.toString(adjustedPct));
		relTo.setContainerData(Integer.toString(10000 - adjustedPct));

		// add the new sash at the same location
		curParent.getChildren().add(index, newSash);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#insert(org.eclipse.e4.ui.model.application
	 * .MPartSashContainerElement, org.eclipse.e4.ui.model.application.MPartSashContainerElement,
	 * int, int)
	 */
	public void insert(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			int where, int ratio) {
		if (toInsert == null || relTo == null)
			return;

		// Ensure the ratio is sane
		if (ratio == 0)
			ratio = 1000;
		if (ratio > 10000)
			ratio = 9000;

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

		if (insertRoot instanceof MWindow || insertRoot instanceof MArea) {
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
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#detach(org.eclipse.e4.ui.model.application
	 * .MPartSashContainerElement)
	 */
	public void detach(MPartSashContainerElement element, int x, int y, int width, int height) {
		// Determine the correct parent for the new window
		MUIElement curParent = element.getParent();
		MUIElement current = element;
		MWindow window = getTopLevelWindowFor(element);
		while (!(curParent instanceof MPerspective) && !(curParent instanceof MWindow)) {
			if (curParent == null) {
				// no parent, maybe we're being represented by a placeholder
				current = findPlaceholderFor(window, current);
				if (current == null) {
					return; // log??
				}

				curParent = current.getParent();
				if (curParent == null) {
					return; // log??
				}
			}
			current = curParent;
			curParent = current.getParent();
		}

		MTrimmedWindow newWindow = BasicFactoryImpl.INSTANCE.createTrimmedWindow();

		// HACK! should either be args or should be computed from the control being detached
		newWindow.setX(x);
		newWindow.setY(y);
		newWindow.setWidth(width);
		newWindow.setHeight(height);

		element.getParent().getChildren().remove(element);
		MWindowElement uiRoot = wrapElementForWindow(element);
		newWindow.getChildren().add(uiRoot);

		if (curParent instanceof MPerspective) {
			MPerspective persp = (MPerspective) curParent;
			persp.getWindows().add(newWindow);
		} else if (curParent instanceof MWindow) {
			((MWindow) curParent).getWindows().add(newWindow);
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private MWindowElement wrapElementForWindow(MPartSashContainerElement element) {
		if (element instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) element).getRef();
			if (ref instanceof MPart) {
				MPartStack newPS = MBasicFactory.INSTANCE.createPartStack();
				newPS.getChildren().add((MPlaceholder) element);
				return newPS;
			}
		} else if (element instanceof MPart) {
			MPartStack newPS = MBasicFactory.INSTANCE.createPartStack();
			newPS.getChildren().add((MPart) element);
			return newPS;
		} else if (element instanceof MWindowElement) {
			return (MWindowElement) element;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#getTrim(org.eclipse.e4.ui.model.application
	 * .ui.basic.MTrimmedWindow, org.eclipse.e4.ui.model.application.ui.SideValue)
	 */
	public MTrimBar getTrim(MTrimmedWindow window, SideValue sv) {
		List<MTrimBar> bars = window.getTrimBars();
		for (MTrimBar bar : bars) {
			if (bar.getSide() == sv)
				return bar;
		}

		// Didn't find a trim bar for the side, make one
		MTrimBar newBar = BasicFactoryImpl.eINSTANCE.createTrimBar();
		newBar.setSide(sv);
		window.getTrimBars().add(newBar);
		return newBar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#getTopLevelWindowFor(org.eclipse.e4.ui
	 * .model .application.ui.MUIElement)
	 */
	public MWindow getTopLevelWindowFor(MUIElement element) {
		EObject eObj = (EObject) element;
		while (eObj != null && !(eObj.eContainer() instanceof MApplication))
			eObj = (EObject) eObj.eContainer();

		if (eObj instanceof MWindow)
			return (MWindow) eObj;

		return null; // Ooops!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#getPerspectiveFor(org.eclipse.e4.ui.model
	 * .application.ui.MUIElement)
	 */
	public MPerspective getPerspectiveFor(MUIElement element) {

		while (true) {
			// if we have a placeholder, reassign ourselves
			MPlaceholder placeholder = element.getCurSharedRef();
			if (placeholder != null) {
				element = placeholder;
			}
			EObject container = ((EObject) element).eContainer();
			if (container == null || container instanceof MApplication) {
				// climbed to the top and found nothing, return null
				return null;
			} else if (container instanceof MPerspectiveStack) {
				// parent is a perspective stack, we ourselves should be a perspective
				return (MPerspective) element;
			}

			// climb up
			element = (MUIElement) container;
		}
	}

	public void resetPerspectiveModel(MPerspective persp, MWindow window) {
		resetPerspectiveModel(persp, window, true);
	}

	private void resetPerspectiveModel(MPerspective persp, MWindow window,
			boolean removeSharedPlaceholders) {
		if (persp == null)
			return;

		// close and remove any detached windows
		for (MWindow win : persp.getWindows()) {
			win.setToBeRendered(false);
		}
		persp.getWindows().clear();

		if (removeSharedPlaceholders) {
			// Remove any views (Placeholders) from the shared area
			EPartService ps = window.getContext().get(EPartService.class);
			List<MArea> areas = findElements(window, null, MArea.class, null);
			if (areas.size() == 1) {
				MArea area = areas.get(0);

				// Strip out the placeholders in visible stacks
				List<MPlaceholder> phList = findElements(area, null, MPlaceholder.class, null);
				for (MPlaceholder ph : phList) {
					ps.hidePart((MPart) ph.getRef());
					ph.getParent().getChildren().remove(ph);
				}

				// Prevent shared stacks ids from clashing with the ones in the perspective
				List<MPartStack> stacks = findElements(area, null, MPartStack.class, null);
				for (MPartStack stack : stacks) {
					String generatedId = "PartStack@" + Integer.toHexString(stack.hashCode()); //$NON-NLS-1$
					stack.setElementId(generatedId);
				}
			}
		}

		// Remove any minimized stacks for this perspective
		List<MTrimBar> bars = findElements(window, null, MTrimBar.class, null);
		List<MToolControl> toRemove = new ArrayList<MToolControl>();
		for (MTrimBar bar : bars) {
			for (MUIElement barKid : bar.getChildren()) {
				if (!(barKid instanceof MToolControl))
					continue;
				String id = barKid.getElementId();
				if (id != null && id.contains(persp.getElementId())) {
					toRemove.add((MToolControl) barKid);
				}
			}
		}

		IEclipseContext ctxt = EclipseContextFactory.create();
		ctxt.set("show", false); //$NON-NLS-1$
		for (MToolControl toolControl : toRemove) {
			// Close any open fast view
			if (toolControl.getObject() != null
					&& toolControl.getObject().getClass().getName().contains("TrimStack")) { //$NON-NLS-1$
				ContextInjectionFactory.invoke(toolControl.getObject(), Execute.class, ctxt);
			}

			toolControl.setToBeRendered(false);
			toolControl.getParent().getChildren().remove(toolControl);
		}
		ctxt.dispose();
	}

	public void removePerspectiveModel(MPerspective persp, MWindow window) {
		// pick a new perspective to become active (if any)
		MUIElement psElement = persp.getParent();
		MPerspectiveStack ps = (MPerspectiveStack) psElement;
		boolean foundNewSelection = false;
		if (ps.getSelectedElement() == persp) {
			for (MPerspective p : ps.getChildren()) {
				if (p != persp && p.isToBeRendered()) {
					ps.setSelectedElement(p);
					foundNewSelection = true;
					break;
				}
			}

			if (!foundNewSelection) {
				ps.setSelectedElement(null);
			}
		}

		// Remove transient elements (minimized stacks, detached windows)
		resetPerspectiveModel(persp, window, false);

		// unrender the perspective and remove it
		persp.setToBeRendered(false);
		ps.getChildren().remove(persp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#getActivePerspective(org.eclipse.e4.ui
	 * .model.application.ui.basic.MWindow)
	 */
	public MPerspective getActivePerspective(MWindow window) {
		List<MPerspectiveStack> pStacks = findElements(window, null, MPerspectiveStack.class, null);
		if (pStacks.size() == 1)
			return pStacks.get(0).getSelectedElement();

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#toBeRenderedCount(org.eclipse.e4.ui.model
	 * .application.ui.MElementContainer)
	 */
	public int toBeRenderedCount(MElementContainer<?> container) {
		int count = 0;
		for (MUIElement child : container.getChildren()) {
			if (child.isToBeRendered()) {
				count++;
			}
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.workbench.modeling.EModelService#getContainer(org.eclipse.e4.ui.model.
	 * application.ui.MUIElement)
	 */
	public MUIElement getContainer(MUIElement element) {
		if (element == null)
			return null;

		return (MUIElement) ((EObject) element).eContainer();
	}

	public int getElementLocation(MUIElement element) {
		if (element == null)
			return NOT_IN_UI;

		MUIElement curElement = element;
		while (curElement != null) {
			MUIElement parent = curElement.getParent();
			if (parent instanceof MPerspective) {
				MElementContainer<MUIElement> perspectiveParent = parent.getParent();
				if (perspectiveParent == null)
					return NOT_IN_UI;
				else if (perspectiveParent.getSelectedElement() == parent)
					return IN_ACTIVE_PERSPECTIVE;
				else
					return IN_ANY_PERSPECTIVE;
			} else if (parent instanceof MApplication) {
				return OUTSIDE_PERSPECTIVE;
			} else if (parent instanceof MTrimBar) {
				return IN_TRIM;
			} else if (parent == null) {
				EObject container = ((EObject) curElement).eContainer();

				// DW tests
				if (container instanceof MWindow) {
					MWindow containerWin = (MWindow) container;
					if (containerWin.getSharedElements().contains(curElement))
						return IN_SHARED_AREA;

					EObject containerParent = container.eContainer();
					if (containerParent instanceof MPerspective) {
						MElementContainer<MUIElement> perspectiveParent = ((MPerspective) containerParent)
								.getParent();
						if (perspectiveParent == null)
							return NOT_IN_UI;
						int location = IN_ANY_PERSPECTIVE;
						if (perspectiveParent.getSelectedElement() == containerParent)
							location |= IN_ACTIVE_PERSPECTIVE;
						return location;
					} else if (containerParent instanceof MWindow)
						return OUTSIDE_PERSPECTIVE;
					else
						return NOT_IN_UI;
				}
			}
			curElement = parent;
		}

		return NOT_IN_UI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#removeLocalPlaceholders(org.eclipse.e4
	 * .ui.model.application.ui.basic.MWindow,
	 * org.eclipse.e4.ui.model.application.ui.advanced.MPerspective)
	 */
	public void hideLocalPlaceholders(MWindow window, MPerspective perspective) {
		List<MPlaceholder> globals = findElements(window, null, MPlaceholder.class, null,
				OUTSIDE_PERSPECTIVE | IN_SHARED_AREA);

		// Iterate across the perspective(s) removing any 'local' placeholders
		List<MPerspective> persps = new ArrayList<MPerspective>();
		if (perspective != null)
			persps.add(perspective);
		else
			persps = findElements(window, null, MPerspective.class, null);

		for (MPerspective persp : persps) {
			List<MPlaceholder> locals = findElements(persp, null, MPlaceholder.class, null,
					IN_ANY_PERSPECTIVE);
			for (MPlaceholder local : locals) {
				for (MPlaceholder global : globals) {
					if (global.getRef() == local.getRef()) {
						local.setToBeRendered(false);
						MElementContainer<MUIElement> localParent = local.getParent();
						setStackVisibility(localParent);
					}
				}
			}
		}
	}

	/**
	 * @param parent
	 */
	private void setStackVisibility(MElementContainer<MUIElement> parent) {
		for (MUIElement child : parent.getChildren()) {
			if (child.isToBeRendered() && child.isVisible()) {
				parent.setToBeRendered(true);
				return;
			}
		}
		parent.setToBeRendered(false);
		// continue modifying the visibility as the parent's parent may also
		// need to be hidden from the user
		setStackVisibility(parent.getParent());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.modeling.EModelService#isLastEditorStack(org.eclipse.e4.ui.model
	 * .application.ui.MUIElement)
	 */
	public boolean isLastEditorStack(MUIElement stack) {
		if (!(stack instanceof MPartStack))
			return false;

		// is it in the shared area?
		MUIElement parent = stack.getParent();
		while (parent != null && !(parent instanceof MArea))
			parent = parent.getParent();
		if (parent == null)
			return false;

		// OK, it's in the area, is it the last TBR one ?
		MArea area = (MArea) parent;
		List<MPartStack> stacks = findElements(area, null, MPartStack.class, null);
		int count = 0;
		for (MPartStack aStack : stacks) {
			if (aStack.isToBeRendered())
				count++;
		}
		return count < 2 && stack.isToBeRendered();
	}
}
