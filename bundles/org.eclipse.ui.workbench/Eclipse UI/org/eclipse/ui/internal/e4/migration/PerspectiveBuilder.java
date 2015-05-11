/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.PerspectiveTagger;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayoutUtils;
import org.eclipse.ui.internal.e4.migration.InfoReader.PageReader;
import org.eclipse.ui.internal.e4.migration.InfoReader.PartState;
import org.eclipse.ui.internal.e4.migration.PerspectiveReader.DetachedWindowReader;
import org.eclipse.ui.internal.e4.migration.PerspectiveReader.ViewLayoutReader;
import org.eclipse.ui.internal.registry.StickyViewDescriptor;

public class PerspectiveBuilder {

	static final String ORIGINAL_ID = "originalId"; //$NON-NLS-1$

	static final String BASE_PERSPECTIVE_ID = "basePerspectiveId"; //$NON-NLS-1$

	private static final String ID_EDITOR_AREA = IPageLayout.ID_EDITOR_AREA;

	@Inject
	private PerspectiveReader perspReader;

	@Inject
	private EModelService modelService;

	private MPerspective perspective;

	private List<String> tags;

	private List<String> renderedViews;

	private Map<String, MPlaceholder> viewPlaceholders = new HashMap<>();

	private Map<String, ViewLayoutReader> viewLayouts;

	private MPlaceholder editorAreaPlaceholder;

	private ModeledPageLayoutUtils layoutUtils;

	@PostConstruct
	private void postConstruct() {
		layoutUtils = new ModeledPageLayoutUtils(modelService);
	}

	public MPerspective createPerspective() {
		create();
		tags = perspective.getTags();
		populate();
		return perspective;
	}

	private void create() {
		perspective = modelService.createModelElement(MPerspective.class);
		perspective.setElementId(perspReader.getId());
		String label = perspReader.getLabel();
		perspective.setLabel(label);
		perspective.setTooltip(label);
		if (perspReader.isCustom()) {
			perspective.getTransientData().put(BASE_PERSPECTIVE_ID, perspReader.getBasicPerspectiveId());
			perspective.getTransientData().put(ORIGINAL_ID, perspReader.getOriginalId());
		}
	}

	private void populate() {
		addActionSetTags();
		addPerspectiveShortcutTags();
		addNewWizardTags();
		addShowViewTags();
		addHiddenItems();

		for (InfoReader info : perspReader.getInfos()) {
			if (info.isEditorArea()) {
				addEditorArea(info);
			} else if (info.isFolder()) {
				MPartStack stack = addPartStack(info);
				populatePartStack(stack, info);
			}
		}

		setZoomState();
		addDetachedWindows();
		hideEmptyStacks();
		hideUrenderableSashes();
		hideInvisibleSashes();
		processStandaloneViews();
		correctSelectedElements();
		addTrimBars();
		PerspectiveTagger.tagPerspective(perspective, modelService);
	}

	private void processStandaloneViews() {
		Map<String, ViewLayoutReader> viewLayouts = perspReader.getViewLayouts();
		for (String viewId : viewLayouts.keySet()) {
			MPlaceholder placeholder = viewPlaceholders.get(viewId);
			if (placeholder == null) {
				continue;
			}
			if (viewLayouts.get(viewId).isStandalone()) {
				MElementContainer<MUIElement> parent = placeholder.getParent();
				placeholder.setContainerData(parent.getContainerData());
				parent.getChildren().remove(placeholder);
				MElementContainer<MUIElement> grandParent = parent.getParent();
				int location = grandParent.getChildren().indexOf(parent);
				grandParent.getChildren().add(location, placeholder);
				grandParent.getChildren().remove(parent);
			}
		}
	}

	private void addDetachedWindows() {
		for (DetachedWindowReader detachedWindowReader : perspReader.getDetachedWindows()) {
			MTrimmedWindow detachedWindow = modelService.createModelElement(MTrimmedWindow.class);
			Rectangle bounds = detachedWindowReader.getBounds();
			detachedWindow.setX(bounds.x);
			detachedWindow.setY(bounds.y);
			detachedWindow.setWidth(bounds.width);
			detachedWindow.setHeight(bounds.height);
			MPartStack stack = modelService.createModelElement(MPartStack.class);
			populatePartStack(stack, detachedWindowReader);
			detachedWindow.getChildren().add(stack);
			perspective.getWindows().add(detachedWindow);
		}
	}

	private void addTrimBars() {
		Map<String, Integer> fastViewBars = perspReader.getFastViewBars();
		if (fastViewBars.size() == 0) {
			return;
		}

		int topCounter = 0;
		int bottomCounter = 0;
		int rightCounter = 0;
		int leftCounter = 0;
		StringBuilder sb = new StringBuilder();

		for (InfoReader folder : perspReader.getInfos()) {
			String folderId = folder.getId();
			if (!fastViewBars.containsKey(folderId)) {
				continue;
			}

			sb.append(folderId).append(' ');

			Integer side = fastViewBars.get(folderId);
			if (side == null) {
				side = SWT.LEFT;
			}

			switch (side) {
			case SWT.TOP:
				sb.append(SideValue.TOP_VALUE).append(' ').append(topCounter++);
				break;
			case SWT.BOTTOM:
				sb.append(SideValue.BOTTOM_VALUE).append(' ').append(bottomCounter++);
				break;
			case SWT.RIGHT:
				sb.append(SideValue.RIGHT_VALUE).append(' ').append(rightCounter++);
				break;
			default:
				sb.append(SideValue.LEFT_VALUE).append(' ').append(leftCounter++);
				break;
			}

			sb.append('#');
		}

		perspective.getPersistedState().put("trims", sb.toString()); //$NON-NLS-1$
	}

	private void hideEmptyStacks() {
		for (MPartStack stack : modelService.findElements(perspective, null, MPartStack.class, null)) {
			if (ID_EDITOR_AREA.equals(stack.getElementId()) || ID_EDITOR_AREA.equals(stack.getParent().getElementId())) {
				continue;
			}
			if (!hasRenderableContent(stack)) {
				stack.setToBeRendered(false);
			}
		}
	}

	private void setZoomState() {
		List<MPartStack> stacks = modelService.findElements(perspective, null, MPartStack.class, null);
		boolean isAnythingMaximized = isMaximized(editorAreaPlaceholder) || isAnyMaximized(stacks);
		if (isAnythingMaximized) {
			markMinimizedByZoom(editorAreaPlaceholder);
			for (MPartStack stack : stacks) {
				markMinimizedByZoom(stack);
			}
		}
	}

	private void markMinimizedByZoom(MUIElement element) {
		List<String> tags = element.getTags();
		if (tags.contains(IPresentationEngine.MINIMIZED)) {
			tags.add(IPresentationEngine.MINIMIZED_BY_ZOOM);
		}
	}

	private boolean isAnyMaximized(List<MPartStack> stacks) {
		for (MPartStack stack : stacks) {
			if (isMaximized(stack)) {
				return true;
			}
		}
		return false;
	}

	private boolean isMaximized(MUIElement element) {
		return element.getTags().contains(IPresentationEngine.MAXIMIZED);
	}

	private void hideUrenderableSashes() {
		for (MPartSashContainer sash : modelService.findElements(perspective, null, MPartSashContainer.class, null)) {
			hideUnrenderableSash(sash);
		}
	}

	private void hideInvisibleSashes() {
		for (MPartSashContainer sash : modelService.findElements(perspective, null, MPartSashContainer.class, null)) {
			hideInvisibleSash(sash);
		}
	}

	private void hideInvisibleSash(MElementContainer<?> container) {
		if ((container instanceof MPartSashContainer) && container != perspective) {
			if (!hasVisibleContent((MPartSashContainer) container)) {
				container.setVisible(false);
				hideInvisibleSash(container.getParent());
			}
		}
	}

	private boolean hasVisibleContent(MPartSashContainer sash) {
		for (MPartSashContainerElement child : sash.getChildren()) {
			if (child.isVisible()) {
				return true;
			}
		}
		return false;
	}

	private void hideUnrenderableSash(MElementContainer<?> container) {
		if ((container instanceof MPartSashContainer) && container != perspective) {
			if (modelService.countRenderableChildren(container) == 0) {
				container.setToBeRendered(false);
				hideUnrenderableSash(container.getParent());
			}
		}
	}

	private boolean hasRenderableContent(MPartStack stack) {
		for (MStackElement child : stack.getChildren()) {
			if (child.isVisible() && child.isToBeRendered()) {
				return true;
			}
		}
		return false;
	}

	private void correctSelectedElements() {
		List<MPartSashContainerElement> perspChildren = perspective.getChildren();
		if (perspective.getSelectedElement() == null && !perspChildren.isEmpty()) {
			for (MPartSashContainerElement child : perspChildren) {
				if (child.isToBeRendered()) {
					perspective.setSelectedElement(child);
					break;
				}
			}
		}

		for (MPartSashContainerElement child : perspChildren) {
			correctSelectedElements(child);
		}
	}

	private void correctSelectedElements(MUIElement element) {
		if (!(element instanceof MPartSashContainer || element instanceof MPartStack)) {
			return;
		}
		@SuppressWarnings("unchecked")
		MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) element;
		List<MUIElement> children = container.getChildren();
		if (container.getSelectedElement() == null && !children.isEmpty()) {
			MUIElement firstRenderableElement = getFirstRenderableElement(children);
			if (firstRenderableElement != null) {
				container.setSelectedElement(firstRenderableElement);
			}
		}
		for (MUIElement child : children) {
			correctSelectedElements(child);
		}
	}

	private MUIElement getFirstRenderableElement(List<MUIElement> elements) {
		for (MUIElement element : elements) {
			if (element.isToBeRendered()) {
				return element;
			}
		}
		return null;
	}

	private void addToPerspective(MPartSashContainerElement element, InfoReader info) {
		if (info.isRelativelyPositioned()) {
			insert(element, info);
		} else {
			perspective.getChildren().add(element);
		}
	}

	private void addEditorArea(InfoReader info) {
		editorAreaPlaceholder = modelService.createModelElement(MPlaceholder.class);
		editorAreaPlaceholder.setElementId(ID_EDITOR_AREA);
		editorAreaPlaceholder.setToBeRendered(perspReader.isEditorAreaVisible());
		setPartState(editorAreaPlaceholder, perspReader.getEditorAreaState());
		addToPerspective(editorAreaPlaceholder, info);
	}

	private MPartStack addPartStack(InfoReader info) {
		MPartStack stack = createPartStack(info);
		if (info.isRelativelyPositioned()) {
			String refElementId = info.getRelative();
			MUIElement refElement = modelService.find(refElementId, perspective);
			MElementContainer<?> parent = refElement.getParent();
			// don't put a stack in another stack
			if (parent instanceof MPartStack) {
				refElement = parent;
			}

			insert(stack, refElement, info);
		} else {
			perspective.getChildren().add(stack);
		}
		setPartState(stack, info.getState());
		return stack;
	}

	private void setPartState(MUIElement element, PartState state) {
		List<String> tags = element.getTags();
		switch (state) {
		case MINIMIZED:
			tags.add(IPresentationEngine.MINIMIZED);
			element.setVisible(false);
			break;
		case MAXIMIZED:
			tags.add(IPresentationEngine.MAXIMIZED);
			break;
		default:
			break;
		}
	}

	private void insert(MUIElement element, MUIElement refElement, InfoReader info) {
		layoutUtils.insert(element, refElement, layoutUtils.plRelToSwt(info.getRelationship()), info.getRatio());
	}

	private void insert(MUIElement element, InfoReader info) {
		insert(element, modelService.find(info.getRelative(), perspective), info);
	}

	private MPartStack createPartStack(InfoReader info) {
		String stackId = info.getId();
		if (stackId != null) {
			if (stackId.equals(StickyViewDescriptor.STICKY_FOLDER_RIGHT)) {
				stackId = "legacyStickyFolderRight"; //$NON-NLS-1$
			}
		}
		return layoutUtils.createStack(stackId, true);
	}

	private void populatePartStack(MPartStack stack, InfoReader info) {
		for (PageReader page : info.getPages()) {
			addPlaceholderToStack(stack, page.getId());
		}
		MStackElement selectedElement = (MStackElement) modelService.find(info.getActivePageId(), stack);
		if (selectedElement != null) {
			selectedElement.setToBeRendered(true);
			selectedElement.setVisible(true);
		}
		stack.setSelectedElement(selectedElement);

		// restore order of views in the stacks
		List<MStackElement> renderedViews = getRenderedViews(stack);
		if (renderedViews.size() < 2) {
			return;
		}

		int[] partOrder = info.getPartOrder();
		List<MStackElement> stackChildren = stack.getChildren();
		// unexpected situation - don't order
		if (partOrder == null || partOrder.length != renderedViews.size()) {
			return;
		}
		List<MStackElement> originalOrder = new ArrayList<>(renderedViews);
		stackChildren.clear();
		for (int i = 0; i < partOrder.length; i++) {
			stackChildren.add(originalOrder.get(partOrder[i]));
		}
		originalOrder.removeAll(stackChildren);
		stackChildren.addAll(originalOrder);
	}

	private List<MStackElement> getRenderedViews(MPartStack stack) {
		List<MStackElement> renderedViews = new ArrayList<>();
		for (MStackElement element : stack.getChildren()) {
			if (element.isToBeRendered()) {
				renderedViews.add(element);
			}
		}
		return renderedViews;
	}

	private void populatePartStack(MPartStack stack, DetachedWindowReader info) {
		for (PageReader page : info.getPages()) {
			addPlaceholderToStack(stack, page.getId());
		}
		stack.setSelectedElement((MStackElement) modelService.find(info.getActivePageId(), stack));
	}

	private void addPlaceholderToStack(MPartStack stack, String partId) {
		MPlaceholder placeholder = modelService.createModelElement(MPlaceholder.class);
		placeholder.setElementId(partId);
		if (!isToBeRendered(placeholder)) {
			placeholder.setToBeRendered(false);
		}
		addLayoutTagsToPlaceholder(placeholder, partId);
		stack.getChildren().add(placeholder);
		viewPlaceholders.put(partId, placeholder);
	}

	private void addLayoutTagsToPlaceholder(MPlaceholder placeholder, String partId) {
		ViewLayoutReader viewLayout = getViewLayout(partId);
		if (viewLayout == null) {
			return;
		}
		List<String> tags = placeholder.getTags();
		if (!viewLayout.isCloseable()) {
			tags.add(IPresentationEngine.NO_CLOSE);
		}
		if (viewLayout.isStandalone()) {
			tags.add(IPresentationEngine.STANDALONE);
		}
	}

	private boolean isToBeRendered(MPlaceholder placeholder) {
		if (renderedViews == null) {
			renderedViews = perspReader.getRenderedViewIds();
		}
		return renderedViews.contains(placeholder.getElementId());
	}

	private void addPerspectiveShortcutTags() {
		for (String shortcutId : perspReader.getPerspectiveShortcutIds()) {
			tags.add(ModeledPageLayout.PERSP_SHORTCUT_TAG + shortcutId);
		}
	}

	private void addActionSetTags() {
		for (String actionSetId : perspReader.getActionSetIds()) {
			tags.add(ModeledPageLayout.ACTION_SET_TAG + actionSetId);
		}
	}

	private void addNewWizardTags() {
		for (String actionId : perspReader.getNewWizardActionIds()) {
			tags.add(ModeledPageLayout.NEW_WIZARD_TAG + actionId);
		}
	}

	private void addShowViewTags() {
		for (String actionId : perspReader.getShowViewActionIds()) {
			tags.add(ModeledPageLayout.SHOW_VIEW_TAG + actionId);
		}
	}

	private void addHiddenItems() {
		String comma = ","; //$NON-NLS-1$
		StringBuilder persistedValue = new StringBuilder();
		for (String elementId : perspReader.getHiddenMenuItemIds()) {
			persistedValue.append(ModeledPageLayout.HIDDEN_MENU_PREFIX);
			persistedValue.append(elementId).append(comma);
		}
		for (String elementId : perspReader.getHiddenToolbarItemIds()) {
			persistedValue.append(ModeledPageLayout.HIDDEN_TOOLBAR_PREFIX);
			persistedValue.append(elementId).append(comma);
		}
		perspective.getPersistedState().put(ModeledPageLayout.HIDDEN_ITEMS_KEY, persistedValue.toString());
	}

	private ViewLayoutReader getViewLayout(String viewId) {
		if (viewLayouts == null) {
			viewLayouts = perspReader.getViewLayouts();
		}
		return viewLayouts.get(viewId);
	}

	Collection<MPlaceholder> getPlaceholders() {
		return viewPlaceholders.values();
	}

	MPlaceholder getEditorAreaPlaceholder() {
		return editorAreaPlaceholder;
	}

}
