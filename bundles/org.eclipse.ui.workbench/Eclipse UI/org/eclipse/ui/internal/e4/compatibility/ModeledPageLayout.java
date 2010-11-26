/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

public class ModeledPageLayout implements IPageLayout {

	public static final String ACTION_SET_TAG = "persp.actionSet:"; //$NON-NLS-1$
	public static final String NEW_WIZARD_TAG = "persp.newWizSC:"; //$NON-NLS-1$
	public static final String PERSP_SHORTCUT_TAG = "persp.perspSC:"; //$NON-NLS-1$
	public static final String SHOW_IN_PART_TAG = "persp.showIn:"; //$NON-NLS-1$
	public static final String SHOW_VIEW_TAG = "persp.viewSC:"; //$NON-NLS-1$

	public static List<String> getIds(MPerspective model, String tagPrefix) {
		if (model == null) {
			return Collections.EMPTY_LIST;
		}
		ArrayList<String> result = new ArrayList<String>();
		for (String tag : model.getTags()) {
			if (tag.startsWith(tagPrefix)) {
				result.add(tag.substring(tagPrefix.length()));
			}
		}
		return result;
	}

	private MApplication application;
	private MWindow window;
	private EModelService modelService;

	EPartService partService;
	WorkbenchPage page;
	MPerspective perspModel;
	private IPerspectiveDescriptor descriptor;

	private MPlaceholder eaRef;

	private MPartStack editorStack;

	boolean createReferences;

	public ModeledPageLayout(MWindow window, EModelService modelService,
			EPartService partService,
			MPerspective perspModel, IPerspectiveDescriptor descriptor, WorkbenchPage page,
			boolean createReferences) {
		this.window = window;
		MUIElement winParent = window.getParent();
		this.application = (MApplication) winParent;
		this.modelService = modelService;
		this.partService = partService;
		this.page = page;
		// Create the editor area stack
		this.perspModel = perspModel;
		this.descriptor = descriptor;

		this.createReferences = createReferences;

		MArea sharedArea = null;
		List<MUIElement> sharedElements = window.getSharedElements();
		for (MUIElement element : sharedElements) {
			if (element.getElementId().equals(getEditorArea())) {
				sharedArea = (MArea) element;
				break;
			}
		}

		if (sharedArea == null) {
			sharedArea = AdvancedFactoryImpl.eINSTANCE.createArea();
			// sharedArea.setLabel("Editor Area"); //$NON-NLS-1$

			editorStack = BasicFactoryImpl.eINSTANCE.createPartStack();
			// temporary HACK for bug 303982
			editorStack.getTags().add("newtablook"); //$NON-NLS-1$
			editorStack.getTags().add("org.eclipse.e4.primaryDataStack"); //$NON-NLS-1$
			editorStack.getTags().add("EditorStack"); //$NON-NLS-1$
			editorStack.setElementId("org.eclipse.e4.primaryDataStack"); //$NON-NLS-1$
			sharedArea.getChildren().add(editorStack);
			sharedArea.setElementId(getEditorArea());

			window.getSharedElements().add(sharedArea);
		}

		eaRef = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		eaRef.setElementId(getEditorArea());
		eaRef.setRef(sharedArea);

		perspModel.getChildren().add(eaRef);

		ActionSetRegistry registry = application.getContext().get(ActionSetRegistry.class);
		for (IActionSetDescriptor actionSetDescriptor : registry.getActionSets()) {
			if (actionSetDescriptor.isInitiallyVisible()) {
				addActionSet(actionSetDescriptor.getId());
			}
		}
	}

	public MPerspective getModel() {
		return perspModel;
	}

	public void addActionSet(String actionSetId) {
		perspModel.getTags().add(ACTION_SET_TAG + actionSetId);
	}

	public void addFastView(String viewId) {
		E4Util.unsupported("addFastView: " + viewId); //$NON-NLS-1$
	}

	public void addFastView(String viewId, float ratio) {
		E4Util.unsupported("addFastView: " + viewId); //$NON-NLS-1$
	}

	public void addNewWizardShortcut(String id) {
		perspModel.getTags().add(NEW_WIZARD_TAG + id);
	}

	public void addPerspectiveShortcut(String id) {
		perspModel.getTags().add(PERSP_SHORTCUT_TAG + id);
	}

	public void addPlaceholder(String viewId, int relationship, float ratio,
			String refId) {
		insertView(viewId, relationship, ratio, refId, false, true);
	}

	public void addShowInPart(String id) {
		perspModel.getTags().add(SHOW_IN_PART_TAG + id);
	}

	public void addShowViewShortcut(String id) {
		perspModel.getTags().add(SHOW_VIEW_TAG + id);
	}

	public void addStandaloneView(String viewId, boolean showTitle,
			int relationship, float ratio, String refId) {
		insertView(viewId, relationship, ratio, refId, true,
				false);
	}

	public void addStandaloneViewPlaceholder(String viewId, int relationship,
			float ratio, String refId, boolean showTitle) {
		insertView(viewId, relationship, ratio, refId, false,
				false);
	}

	public void addView(String viewId, int relationship, float ratio,
			String refId) {
		insertView(viewId, relationship, ratio, refId, true, true);
	}

	public void addView(String viewId, int relationship, float ratio, String refId,
			boolean minimized) {
		if (minimized) {
			E4Util.unsupported("addView: use of minimized for " + viewId + " ref " + refId); //$NON-NLS-1$ //$NON-NLS-2$
		}
		insertView(viewId, relationship, ratio, refId, true, true);
	}

	public IFolderLayout createFolder(String folderId, int relationship,
			float ratio, String refId) {
		MPartStack stack = insertStack(folderId, relationship, ratio, refId,
				true);
		return new ModeledFolderLayout(this, application, stack);
	}

	public IPlaceholderFolderLayout createPlaceholderFolder(String folderId,
			int relationship, float ratio, String refId) {
		MPartStack Stack = insertStack(folderId, relationship, ratio, refId,
				false);
		return new ModeledPlaceholderFolderLayout(this, application, Stack);
	}

	public IPerspectiveDescriptor getDescriptor() {
		return descriptor;
	}

	public static String internalGetEditorArea() {
		return IPageLayout.ID_EDITOR_AREA;
	}

	public String getEditorArea() {
		return internalGetEditorArea();
	}

	public int getEditorReuseThreshold() {
		return -1;
	}

	public IPlaceholderFolderLayout getFolderForView(String id) {
		MPart view = findPart(perspModel, id);
		if (view == null)
			return null;

		MUIElement stack = view.getParent();
		if (stack == null || !(stack instanceof MPartStack))
			return null;

		return new ModeledPlaceholderFolderLayout(this, application, (MPartStack) stack);
	}

	public IViewLayout getViewLayout(String id) {
		MPart view = findPart(perspModel, id);
		if (view != null)
			return new ModeledViewLayout(view);

		MPlaceholder placeholder = findPlaceholder(perspModel, id);
		if (placeholder != null)
			return new ModeledViewLayout(placeholder);

		return null;
	}

	public boolean isEditorAreaVisible() {
		return true;
	}

	public boolean isFixed() {
		return false;
	}

	public void setEditorAreaVisible(boolean showEditorArea) {
		eaRef.setToBeRendered(showEditorArea);
	}

	public void setEditorReuseThreshold(int openEditors) {
		// ignored, no-op, same as 3.x implementation
	}

	public void setFixed(boolean isFixed) {
		// perspModel.setFixed(isFixed);
	}

	private static int plRelToSwt(int rel) {
		switch (rel) {
		case IPageLayout.BOTTOM:
			return SWT.BOTTOM;
		case IPageLayout.LEFT:
			return SWT.LEFT;
		case IPageLayout.RIGHT:
			return SWT.RIGHT;
		case IPageLayout.TOP:
			return SWT.TOP;
		default:
			return 0;
		}
	}

	public static MStackElement createViewModel(MApplication application, String id,
			boolean visible,
			WorkbenchPage page, EPartService partService, boolean createReferences) {
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getElementId().equals(id)) {
				MPlaceholder ph = partService.createSharedPart(id);
				ph.setToBeRendered(visible);

				MPart part = (MPart) (ph.getRef());
				// as a shared part, this should be true, actual un/rendering
				// will be dependent on any placeholders that are referencing
				// this part
				part.setToBeRendered(true);

				// there should only be view references for views that are
				// visible to the end user, that is, the tab items are being
				// drawn
				if (visible && createReferences) {
					page.createViewReferenceForPart(part, id);
				}
				return ph;
			}
		}
		return null;
	}

	public static MPartStack createStack(String id, boolean visible) {
		MPartStack newStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		// temporary HACK for bug 303982
		newStack.getTags().add("newtablook"); //$NON-NLS-1$
		newStack.setElementId(id);
		newStack.setToBeRendered(visible);
		return newStack;
	}

	private void insertView(String viewId, int relationship, float ratio,
			String refId, boolean visible, boolean withStack) {
		MUIElement existingView = findElement(perspModel, viewId);
		if (existingView instanceof MPlaceholder) {
			existingView.getParent().getChildren().remove(existingView);
		}

		MUIElement refModel = findElement(perspModel, refId);
		if (refModel instanceof MPart) {
			refModel = refModel.getParent();
		} else if (refModel instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) refModel).getRef();
			if (ref instanceof MPart) {
				refModel = refModel.getParent();
			}
		}

		MStackElement viewModel = createViewModel(application, viewId, visible, page, partService,
				createReferences);
		if (viewModel != null) {
			if (withStack) {
				String stackId = viewId + "MStack"; // Default id...basically unusable //$NON-NLS-1$
				MPartStack stack = insertStack(stackId, relationship, ratio, refId, visible);
				stack.getChildren().add(viewModel);
			} else {
				insert(viewModel, refModel, plRelToSwt(relationship), ratio);
			}
		}
	}

	private MUIElement getLastContainer(MUIElement element) {
		if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			List<?> children = container.getChildren();
			return children.isEmpty() ? container : getLastContainer((MUIElement) children
					.get(children.size() - 1));
		}
		return element.getParent();
	}

	private MElementContainer<?> getLastContainer() {
		List<MPartSashContainerElement> children = perspModel.getChildren();
		if (children.isEmpty()) {
			return perspModel;
		}
		MUIElement element = getLastContainer(children.get(children.size() - 1));
		return element instanceof MElementContainer ? (MElementContainer<?>) element : perspModel;
	}

	private MPartStack insertStack(String stackId, int relationship,
			float ratio, String refId, boolean visible) {
		MUIElement refModel = findElement(perspModel, refId);
		if (refModel == null) {
			WorkbenchPlugin.log(NLS.bind(WorkbenchMessages.PageLayout_missingRefPart, refId));
			MPartStack stack = createStack(stackId, visible);
			insert(stack, getLastContainer(), plRelToSwt(relationship), ratio);
			return stack;
		}
		// If the 'refModel' is -not- a stack then find one
		// This covers cases where the defining layout is adding
		// Views relative to other views and relying on the stacks
		// being automatically created.
		// if (!(refModel instanceof MPartStack)) {
		// while (refModel.getParent() != null) {
		// refModel = refModel.getParent();
		// if (refModel instanceof MPartStack)
		// break;
		// }
		// if (!(refModel instanceof MPartStack))
		// return null;
		// }

		MPartStack stack = createStack(stackId, visible);
		MElementContainer<?> parent = refModel.getParent();
		if (parent instanceof MPartStack) {
			// we don't want to put a stack in a stack
			refModel = parent;
		}
		insert(stack, refModel, plRelToSwt(relationship), ratio);

		return stack;
	}

	public static void replace(MUIElement relTo,
			MElementContainer<MUIElement> newParent) {
		if (relTo == null || newParent == null)
			return;

		MElementContainer<MUIElement> parent = relTo.getParent();
		if (parent == null)
			return;

		List kids = parent.getChildren();
		if (kids == null)
			return;

		kids.add(kids.indexOf(relTo), newParent);
		kids.remove(relTo);
	}

	public static void insertParent(MElementContainer<MUIElement> newParent,
			MUIElement relTo) {
		if (newParent == null || relTo == null)
			return;

		MPart curParent = (MPart) relTo.getParent();
		if (curParent != null) {
			replace(relTo, newParent);
		}

		// Move the child under the new parent
		newParent.getChildren().add(relTo);
	}

	public static void insert(MUIElement toInsert, MUIElement relTo,
			int swtSide, int ratio) {
		if (toInsert == null || relTo == null)
			return;

		MElementContainer<MUIElement> relParent = relTo.getParent();
		if (relParent != null) {
			List<MUIElement> children = relParent.getChildren();
			int index = children.indexOf(relTo);
			MPartSashContainer psc = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			psc.setContainerData(relTo.getContainerData());
			relParent.getChildren().add(index + 1, psc);

			switch (swtSide) {
			case SWT.LEFT:
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				psc.getChildren().add((MPartSashContainerElement) relTo);
				toInsert.setContainerData("" + ratio); //$NON-NLS-1$
				relTo.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(true);
				break;
			case SWT.RIGHT:
				psc.getChildren().add((MPartSashContainerElement) relTo);
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				relTo.setContainerData("" + ratio); //$NON-NLS-1$
				toInsert.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(true);
				break;
			case SWT.TOP:
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				psc.getChildren().add((MPartSashContainerElement) relTo);
				toInsert.setContainerData("" + ratio); //$NON-NLS-1$
				relTo.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(false);
				break;
			case SWT.BOTTOM:
				psc.getChildren().add((MPartSashContainerElement) relTo);
				psc.getChildren().add((MPartSashContainerElement) toInsert);
				relTo.setContainerData("" + ratio); //$NON-NLS-1$
				toInsert.setContainerData("" + (10000 - ratio)); //$NON-NLS-1$
				psc.setHorizontal(false);
				break;
			}

			return;
		}

		boolean isStack = true;

		// Create the new sash if we're going to need one
		MPartSashContainer newSash = null;
		if ((swtSide == SWT.TOP || swtSide == SWT.BOTTOM) && !isStack) {
			newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			String label = "Vertical Sash[" + toInsert.getElementId() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			newSash.setElementId(label);
			newSash.setHorizontal(false);
		} else if ((swtSide == SWT.LEFT || swtSide == SWT.RIGHT) && !isStack) {
			newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
			String label = "Horizontal Sash[" + toInsert.getElementId() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			newSash.setElementId(label);
			newSash.setHorizontal(true);
		}

		List parts;
		if (newSash == null && relParent != null) {
			parts = relParent.getChildren();
		} else {
			MUIElement vscElement = newSash;
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) vscElement;
			insertParent(container, relTo);
			parts = newSash.getChildren();

			// List<Integer> weights = newSash.getWeights();
			// weights.add(ratio);
			// weights.add(100 - ratio);
		}

		// Insert the part in the correct location
		int index = parts.indexOf(relTo);
		if (swtSide == SWT.BOTTOM || swtSide == SWT.RIGHT) {
			index++;

		}

		parts.add(index, toInsert);
	}

	public static void insert(MUIElement toInsert, MUIElement relTo,
			int swtSide, float ratio) {
		int pct = (int) (ratio * 10000);
		insert(toInsert, relTo, swtSide, pct);
	}

	MUIElement findElement(MUIElement toSearch, String id) {
		MUIElement foundElement = modelService.find(id, toSearch);
		if (foundElement == null) {
			// if not found in the current perspective model check outside the
			// perspective
			// and in the shared area to see if it's 'globally' in the
			// presentation
			List<Object> elements = modelService.findElements(window, id, null, null,
					EModelService.OUTSIDE_PERSPECTIVE | EModelService.IN_SHARED_AREA);
			if (elements.size() == 1)
				foundElement = (MUIElement) elements.get(0);
		}
		return foundElement;
	}

	private MPart findPart(MUIElement toSearch, String id) {
		MUIElement element = modelService.find(id, toSearch);
		return element instanceof MPart ? (MPart) element : null;
	}

	private MPlaceholder findPlaceholder(MUIElement toSearch, String id) {
		MUIElement element = modelService.find(id, toSearch);
		return element instanceof MPlaceholder ? (MPlaceholder) element : null;
	}

	public void addHiddenMenuItemId(String id) {
		E4Util.unsupported("addHiddenMenuItemId: " + id); //$NON-NLS-1$
	}

	public void addHiddenToolBarItemId(String id) {
		E4Util.unsupported("addHiddenToolBarItemId: " + id); //$NON-NLS-1$
	}

	public void removePlaceholder(String id) {
		MUIElement refModel = findElement(perspModel, id);
		if (!(refModel instanceof MPlaceholder)) {
			E4Util.unsupported("removePlaceholder: failed to find " + id + ": " + refModel); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		MElementContainer<MUIElement> parent = refModel.getParent();
		if (parent != null) {
			parent.getChildren().remove(refModel);
		}
	}

	public void stackView(String id, String refId, boolean visible) {
		MUIElement existingView = findElement(perspModel, id);
		if (existingView instanceof MPlaceholder) {
			existingView.getParent().getChildren().remove(existingView);
		}

		MUIElement refModel = findElement(perspModel, refId);
		if (refModel instanceof MPart || refModel instanceof MPlaceholder) {
			refModel = refModel.getParent();
		}
		if (!(refModel instanceof MPartStack)) {
			E4Util.unsupported("stackView: failed to find " + refId + " for " + id); //$NON-NLS-1$//$NON-NLS-2$
			return;
		}
		MStackElement viewModel = createViewModel(application, id, visible, page, partService,
				createReferences);
		if (viewModel != null) {
			((MPartStack) refModel).getChildren().add(viewModel);
			
			if (visible) {
				// ensure that the parent is being rendered, it may have been a
				// placeholder folder so its flag may actually be false
				refModel.setToBeRendered(true);
			}
		}
	}
}
