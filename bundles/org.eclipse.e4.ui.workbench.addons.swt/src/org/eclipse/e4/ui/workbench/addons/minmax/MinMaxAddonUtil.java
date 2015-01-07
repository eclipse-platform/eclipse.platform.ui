/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugen Neufeld (eneufeld@eclipsesource.com) - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.minmax;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * An utility class which provides methods to handle the cases for the special
 * area.
 */
// MinMaximizeableChildrenArea
public class MinMaxAddonUtil {

	private static final String ID_EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$
	private static String MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG = IPresentationEngine.MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG;

	/**
	 * Check whether the passed {@link MUIElement} is an MArea with a
	 * MinMaximizeableChildrenArea tag and multiple children that are visible
	 * and not minimized.
	 *
	 * @param element
	 *            The element to check.
	 * @return true if the element is a MArea with an
	 *         MinMaximizeableChildrenArea tag and multiple visible not
	 *         minimized children, false otherwise
	 */
	public static boolean isMinMaxChildrenAreaWithMultipleVisibleChildren(MUIElement element) {
		if (!element.getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG))
			return false;
		if (!(element instanceof MArea))
			return false;
		MArea area = (MArea) element;
		if (area.getChildren().isEmpty())
			return false;
		if (!hasMoreThenOneVisibleRenderableChild(area.getChildren().get(0)))
			return false;
		return true;
	}

	private static boolean hasMoreThenOneVisibleRenderableChild(MPartSashContainerElement elementToCheck) {
		if (elementToCheck instanceof MPartSashContainer) {
			int partsToRender = 0;
			for (MPartSashContainerElement part : ((MPartSashContainer) elementToCheck).getChildren()) {

				boolean hasMinimizeableChild = hasMoreThenOneVisibleRenderableChild(part);

				if (hasMinimizeableChild) {
					return true;
				}
				if (isVisible(part))
					partsToRender++;
			}
			if (partsToRender > 1)
				return true;
		}
		return false;
	}

	private static boolean isVisible(MUIElement part) {
		boolean visible = part.isToBeRendered() && part.isVisible();
		if (part instanceof MElementContainer && visible) {
			visible = false;
			for (Object element : ((MElementContainer<?>) part).getChildren()) {
				MUIElement innerElement = (MUIElement) element;
				visible |= isVisible(innerElement);
			}
		}
		return visible;
	}

	/**
	 * Remove all partstacks that are inside a special area if the element that
	 * is maximized is a special area.
	 *
	 * @param modelService
	 *            The {@link EModelService} to use
	 * @param element
	 *            The {@link MUIElement} getting maximized
	 * @param curMax
	 *            The list of elements to restore
	 */
	// TODO needed, doesn't this exclude itself with getElementsToRestore?
	public static void ignoreChildrenOfMinMaxChildrenArea(EModelService modelService, MUIElement element,
			List<MUIElement> curMax) {
		if (element instanceof MPlaceholder
				&& ((MPlaceholder) element).getRef().getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG)) {
			Set<MUIElement> toRemove = new LinkedHashSet<MUIElement>();
			for (MUIElement maxElement : curMax) {
				if (modelService.find(maxElement.getElementId(), element) != null) {
					toRemove.add(maxElement);
				}
			}
			curMax.removeAll(toRemove);
		}
	}

	/**
	 * Check for {@link MPartStack MPartStacks} that are inside a special area
	 * and exclude them from being restored as they have to be handled
	 * specially.
	 *
	 * @param modelService
	 *            The {@link EModelService} to use
	 * @param element
	 *            The {@link MUIElement} being restored
	 * @param win
	 *            The {@link MWindow} the {@link MUIElement} is part of
	 * @param persp
	 *            The {@link MPerspective} the {@link MUIElement} is part of
	 * @param elementsToRestore
	 *            The list of elements to restore
	 */
	public static void addChildrenOfMinMaxChildrenAreaToRestoreList(EModelService modelService, MUIElement element,
			MWindow win, MPerspective persp, List<MUIElement> elementsToRestore) {
		List<MPlaceholder> areas = modelService.findElements(persp == null ? win : persp, ID_EDITOR_AREA,
				MPlaceholder.class, null, EModelService.PRESENTATION);

		for (MPlaceholder placeholder : areas) {
			if (placeholder == element)
				continue;
			if (win != getWindowFor(placeholder))
				continue;
			if (!placeholder.getRef().getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG))
				continue;
			List<MPartStack> partStacks = modelService.findElements(placeholder, null, MPartStack.class,
					null);
			if (partStacks.contains(element))
				continue;
			for (MPartStack partStack : partStacks) {
				elementsToRestore.remove(partStack);
			}
		}
	}

	/**
	 * Restore the inner stack of an area and adjust the buttons of the restored
	 * stack.
	 *
	 * @param minMaxAddon
	 *            The MinMaxAddon calling this
	 * @param element
	 *            The MUIElement being restored.
	 * @param maximizeTag
	 *            The List of tags to search for
	 */
	public static void restoreStacksOfMinMaxChildrenArea(final MinMaxAddon minMaxAddon, MUIElement element,
			List<String> maximizeTag) {
		if (element instanceof MPartStack) {
			MArea area = getAreaFor((MPartStack) element);
			if (area != null && area.getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG)) {
				final List<MPartStack> maximizedAreaChildren = minMaxAddon.modelService.findElements(area, null,
						MPartStack.class,
						maximizeTag);
				minMaxAddon.executeWithIgnoredTagChanges(new Runnable() {

					@Override
					public void run() {
						for (MPartStack partStack : maximizedAreaChildren) {
							partStack.getTags().remove(IPresentationEngine.MAXIMIZED);
							minMaxAddon.adjustCTFButtons(partStack);
						}
					}
				});
			}
		}
	}

	/**
	 * Maximize the area if the inner part stack is maximized and adjust the
	 * buttons of the maximized area.
	 *
	 * @param minMaxAddon
	 *            The MinMaxAddon calling this
	 * @param element
	 *            The MUIElement being restored.
	 */
	public static void maximizeMinMaxChildrenArea(final MinMaxAddon minMaxAddon, final MUIElement element) {
		if (element instanceof MPartStack) {
			MArea area = getAreaFor((MPartStack) element);
			if (area != null && area.getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG)) {
				final MPlaceholder placeholder = area.getCurSharedRef();
				minMaxAddon.executeWithIgnoredTagChanges(new Runnable() {

					@Override
					public void run() {
						placeholder.getTags().add(IPresentationEngine.MAXIMIZED);
					}
				});
				minMaxAddon.adjustCTFButtons(placeholder);
			}
		}
	}

	/**
	 * Check whether a special table is one of the minimized elements. If so do
	 * not minimized any contents of such a table.
	 *
	 * @param modelService
	 *            The {@link EModelService} to use
	 * @param element
	 *            The {@link MUIElement} being minimized
	 * @param win
	 *            The {@link MWindow} the minimized element is part of
	 * @param persp
	 *            The {@link MPerspective} the minimized element is part of
	 * @param elementsToMinimize
	 *            The list of elements to minimize
	 */
	public static void handleMinimizeOfMinMaxChildrenArea(EModelService modelService, MUIElement element, MWindow win,
			MPerspective persp, List<MUIElement> elementsToMinimize) {
		// if an area is minimized, exclude the children from being minimized
		List<MPlaceholder> areas = modelService.findElements(persp == null ? win : persp, ID_EDITOR_AREA,
				MPlaceholder.class, null, EModelService.ANYWHERE);
		boolean foundRelevantArea = false;
		for (MPlaceholder placeholder : areas) {
			if (placeholder == element)
				continue;
			if (win != getWindowFor(placeholder))
				continue;
			if (modelService.find(element.getElementId(), placeholder) == null)
				continue;
			if (placeholder.getRef().getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG))
				foundRelevantArea = true;
			List<MPartStack> partStacks = modelService.findElements(placeholder, null, MPartStack.class,
					null);
			for (MPartStack partStack : partStacks) {
				if (partStack == element)
					continue;
				elementsToMinimize.add(partStack);
			}
		}
		if (foundRelevantArea) {
			List<MUIElement> elementsToRemove = new ArrayList<MUIElement>();
			for (MUIElement element2 : elementsToMinimize) {
				List<Object> findElements = modelService.findElements(element2, element.getElementId(),
						null, null);
				if (findElements != null && findElements.size() != 0)
					elementsToRemove.add(element2);
			}
			elementsToMinimize.removeAll(elementsToRemove);
		}
		// allow Area Children to be maximizable
	}

	/**
	 * Unzoom the special area if a child of it is unzoomed.
	 *
	 * @param minMaxAddon
	 *            The {@link MinMaxAddon} calling
	 * @param element
	 *            The {@link MUIElement} being unzoomed
	 */
	public static void unzoomStackOfMinMaxChildrenArea(final MinMaxAddon minMaxAddon, final MUIElement element) {
		if (element instanceof MPartStack) {
			MArea area = getAreaFor((MPartStack) element);
			if (area != null && area.getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG)) {
				final MPlaceholder placeholder = area.getCurSharedRef();
				minMaxAddon.executeWithIgnoredTagChanges(new Runnable() {

					@Override
					public void run() {
						placeholder.getTags().remove(IPresentationEngine.MAXIMIZED);
					}
				});
				minMaxAddon.adjustCTFButtons(placeholder);
			}
		}
	}

	/**
	 * Check whether a {@link MUIElement} is a child of a special area.
	 *
	 * @param element
	 *            The {@link MUIElement} to check
	 * @return true if the element is part of a special area, false otherwise
	 */
	public static boolean isPartOfMinMaxChildrenArea(MUIElement element) {
		if (element instanceof MPartStack) {
			MArea area = getAreaFor((MPartStack) element);
			if (area != null && area.getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG))
				return true;
		}
		return false;
	}

	/**
	 * Return the first {@link MArea} that is the parent of the given
	 * {@link MPartStack}.
	 *
	 * @param stack
	 *            The {@link MPartStack} to find the container {@link MArea} for
	 * @return The container MArea or null if this stack is not part of an area
	 */
	public static MArea getAreaFor(MPartStack stack) {
		MUIElement parent = stack.getParent();
		while (parent != null) {
			if (parent instanceof MArea)
				return (MArea) parent;
			parent = parent.getParent();
		}
		return null;
	}

	/**
	 * Return the MWindow containing this element (if any). This may either be a
	 * 'top level' window -or- a detached window. This allows the min.max code
	 * to only affect elements in the window containing the element.
	 *
	 * @param element
	 *            The element to check
	 *
	 * @return the window containing the element.
	 */
	public static MWindow getWindowFor(MUIElement element) {
		MUIElement parent = element.getParent();

		// We rely here on the fact that a DW's 'getParent' will return
		// null since it's not in the 'children' hierarchy
		while (parent != null && !(parent instanceof MWindow)) {
			if (parent.getTags().contains(MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG) && parent instanceof MArea)
				parent = ((MArea) parent).getCurSharedRef();
			else
				parent = parent.getParent();
		}

		// A detached window will end up with getParent() == null
		return (MWindow) parent;
	}
}
