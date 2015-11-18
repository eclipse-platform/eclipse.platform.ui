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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.TrimBarLayout;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayoutUtils;
import org.eclipse.ui.internal.e4.migration.WindowReader.EditorReader;
import org.eclipse.ui.internal.e4.migration.WindowReader.ViewReader;
import org.eclipse.ui.internal.registry.StickyViewDescriptor;
import org.eclipse.ui.internal.registry.ViewRegistry;

public class WindowBuilder {

	private MWindow window;

	private List<MUIElement> sharedElements;

	private MPartSashContainer mainSash;

	@Inject
	private WindowReader windowReader;

	@Inject
	private EModelService modelService;

	@Inject
	private IModelBuilderFactory factory;

	private ModeledPageLayoutUtils layoutUtils;

	private MArea editorArea;

	@PostConstruct
	private void postConstruct() {
		layoutUtils = new ModeledPageLayoutUtils(modelService);
	}

	MWindow createWindow() {
		create();
		populate();
		return window;
	}

	boolean isSelected() {
		return windowReader.isSelected();
	}

	boolean isWelcomePageOpen() {
		return windowReader.isWelcomePageOpen();
	}

	private void create() {
		window = modelService.createModelElement(MTrimmedWindow.class);

		Rectangle bounds = windowReader.getBounds();
		window.setX(bounds.x);
		window.setY(bounds.y);
		window.setWidth(bounds.width);
		window.setHeight(bounds.height);
		window.getTags().add("topLevel"); //$NON-NLS-1$

		String coolbarVisible = Boolean.TRUE.toString();
		if (!windowReader.isCoolbarVisible() && !isWelcomePageOpen()) {
			coolbarVisible = Boolean.FALSE.toString();
		}
		window.getPersistedState().put(IPreferenceConstants.COOLBAR_VISIBLE, coolbarVisible);
		window.getPersistedState().put(IPreferenceConstants.PERSPECTIVEBAR_VISIBLE, coolbarVisible);

		// necessary to force correct order of window initial
		// rendering when there is no perspective
		if (windowReader.isStatusBarVisible()) {
			MTrimBar statusBar = modelService.getTrim((MTrimmedWindow) window, SideValue.BOTTOM);
			if (windowReader.hasStatusLine()) {
				MToolControl statusLine = modelService.createModelElement(MToolControl.class);
				statusLine.setElementId(WorkbenchWindow.STATUS_LINE_ID);
				statusLine.setContributionURI(WorkbenchWindow.TRIM_CONTRIBUTION_URI);
				statusLine.getTags().add(TrimBarLayout.SPACER);
				statusBar.getChildren().add(statusLine);
			}
		}

		sharedElements = window.getSharedElements();
	}

	private void populate() {
		addEditorArea();
		addEditors();
		addViews();

		mainSash = modelService.createModelElement(MPartSashContainer.class);
		mainSash.setHorizontal(true);

		MPerspectiveStack perspectiveStack = createPerspectiveStack();
		mainSash.getChildren().add(perspectiveStack);
		mainSash.setSelectedElement(perspectiveStack);

		window.getChildren().add(mainSash);
		window.setSelectedElement(mainSash);

		for (PerspectiveReader perspReader : windowReader.getPerspectiveReaders()) {
			PerspectiveBuilder builder = factory.createPerspectiveBuilder(perspReader);
			perspectiveStack.getChildren().add(builder.createPerspective());
			MPlaceholder eaPlaceholder = builder.getEditorAreaPlaceholder();
			if (eaPlaceholder != null) {
				eaPlaceholder.setRef(editorArea);
			}
			for (MPlaceholder viewPlaceholder : builder.getPlaceholders()) {
				String id = viewPlaceholder.getElementId();
				if (id != null) {
					viewPlaceholder.setRef(getSharedView(id));
				}
			}
		}

		String activePerspectiveId = windowReader.getActivePerspectiveId();
		if (activePerspectiveId != null) {
			for (MPerspective persp : perspectiveStack.getChildren()) {
				String id = persp.getElementId();
				String originalId = (String) persp.getTransientData().get(
						PerspectiveBuilder.ORIGINAL_ID);
				if (originalId != null) {
					id = originalId;
				}
				if (activePerspectiveId.equals(id)) {
					perspectiveStack.setSelectedElement(persp);
					break;
				}
			}
		}
		addStickyFolder();
	}

	private void addEditors() {
		Map<MPartStack, InfoReader> stackToReader = new HashMap<>();

		// add stacks to shared area
		List<InfoReader> stackReaders = windowReader.getEditorStacks();
		if (stackReaders.isEmpty()) {
			// create default stack
			MPartStack editorStack = modelService.createModelElement(MPartStack.class);
			editorStack.getTags().add(ModeledPageLayout.EDITOR_STACK_TAG);
			editorArea.getChildren().add(editorStack);
		}

		for (InfoReader stackReader : stackReaders) {
			stackToReader.put(addEditorStack(stackReader), stackReader);
		}

		editorArea.setSelectedElement(editorArea.getChildren().get(0));

		for (EditorReader editorReader : windowReader.getEditors()) {
			MPart editor = modelService.createModelElement(MPart.class);
			editor.setElementId(CompatibilityEditor.MODEL_ELEMENT_ID);
			editor.setContributionURI(CompatibilityPart.COMPATIBILITY_EDITOR_URI);
			editor.setLabel(editorReader.getLabel());
			editor.getPersistedState().put(Workbench.MEMENTO_KEY,
					new MementoSerializer(editorReader.getMemento()).serialize());
			List<String> tags = editor.getTags();
			tags.add(Workbench.EDITOR_TAG);
			tags.add(EPartService.REMOVE_ON_HIDE_TAG);
			tags.add(editorReader.getType());
			MPartStack stack = (MPartStack) modelService.find(editorReader.getStackId(), editorArea);
			stack.getChildren().add(editor);
			if (editorReader.isSelected()) {
				stack.setSelectedElement(editor);
			}
		}

		// restore order of editors in stacks
		for (MPartStack editorStack : stackToReader.keySet()) {
			if (editorStack.getChildren().size() < 2) {
				continue;
			}
			InfoReader stackReader = stackToReader.get(editorStack);
			if (stackReader == null) {
				continue;
			}
			int[] partOrder = stackReader.getPartOrder();
			List<MStackElement> stackChildren = editorStack.getChildren();
			List<MStackElement> originalOrder = new ArrayList<>(stackChildren);
			MStackElement selectedElement = editorStack.getSelectedElement();
			stackChildren.clear();
			for (int i = 0; i < partOrder.length; i++) {
				stackChildren.add(originalOrder.get(partOrder[i]));
			}
			if (selectedElement != null) {
				editorStack.setSelectedElement(selectedElement);
			}
		}
	}

	private MPartStack addEditorStack(InfoReader info) {
		MPartStack stack = layoutUtils.createStack(info.getId(), true);
		if (info.isRelativelyPositioned()) {
			MUIElement refElement = modelService.find(info.getRelative(), editorArea);
			MElementContainer<?> parent = refElement.getParent();
			if (parent instanceof MPartStack) {
				refElement = parent;
			}
			layoutUtils.insert(stack, refElement, layoutUtils.plRelToSwt(info.getRelationship()), info.getRatio());
		} else {
			editorArea.getChildren().add(stack);
		}
		return stack;
	}

	private void addViews() {
		for (ViewReader viewReader : windowReader.getViews()) {
			sharedElements.add(createView(viewReader));
		}
	}

	private MPart createView(ViewReader viewReader) {
		MPart view = modelService.createModelElement(MPart.class);
		view.setElementId(viewReader.getId());
		view.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
		view.setLabel(viewReader.getLabel());
		view.getTags().add(ViewRegistry.VIEW_TAG);
		view.getPersistedState().put(Workbench.MEMENTO_KEY,
				new MementoSerializer(viewReader.getViewState()).serialize());
		return view;
	}

	private void addEditorArea() {
		editorArea = modelService.createModelElement(MArea.class);
		sharedElements.add(editorArea);
		editorArea.setElementId(IPageLayout.ID_EDITOR_AREA);
	}

	private MPerspectiveStack createPerspectiveStack() {
		MPerspectiveStack perspStack = modelService
				.createModelElement(MPerspectiveStack.class);
		perspStack.setElementId("PerspectiveStack"); //$NON-NLS-1$
		return perspStack;
	}

	private void addStickyFolder() {
		MPartStack stickyFolder = modelService.createModelElement(MPartStack.class);
		stickyFolder.setElementId(StickyViewDescriptor.STICKY_FOLDER_RIGHT);
		stickyFolder.setContainerData("2500"); //$NON-NLS-1$
		stickyFolder.setToBeRendered(false);
		mainSash.getChildren().add(stickyFolder);
	}

	private MPart getSharedView(String id) {
		MPart part = null;
		for (MUIElement element : sharedElements) {
			if (id.equals(element.getElementId()) && element instanceof MPart) {
				part = (MPart) element;
				break;
			}
		}
		if (part == null) {
			part = modelService.createModelElement(MPart.class);
			part.setElementId(id);
			part.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
			part.getTags().add(ViewRegistry.VIEW_TAG);
			sharedElements.add(part);
		}
		return part;
	}

}
