/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IIdentifier;
import org.eclipse.ui.activities.IIdentifierListener;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.IdentifierEvent;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class ModeledPageLayout implements IPageLayout {

	public static final String ACTION_SET_TAG = "persp.actionSet:"; //$NON-NLS-1$
	public static final String NEW_WIZARD_TAG = "persp.newWizSC:"; //$NON-NLS-1$
	public static final String PERSP_SHORTCUT_TAG = "persp.perspSC:"; //$NON-NLS-1$
	public static final String SHOW_IN_PART_TAG = "persp.showIn:"; //$NON-NLS-1$
	public static final String SHOW_VIEW_TAG = "persp.viewSC:"; //$NON-NLS-1$
	public static final String HIDDEN_MENU_PREFIX = "persp.hideMenuSC:"; //$NON-NLS-1$
	public static final String HIDDEN_TOOLBAR_PREFIX = "persp.hideToolbarSC:"; //$NON-NLS-1$
	public static final String HIDDEN_ITEMS_KEY = "persp.hiddenItems"; //$NON-NLS-1$

	public static List<String> getIds(MPerspective model, String tagPrefix) {
		if (model == null) {
			return Collections.emptyList();
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
	// private MWindow window;
	private EModelService modelService;

	EPartService partService;
	WorkbenchPage page;
	MPerspective perspModel;
	private IPerspectiveDescriptor descriptor;

	private MPlaceholder eaRef;

	private MPartStack editorStack;

	boolean createReferences;

	private IViewRegistry viewRegistry;

	private class ViewActivator implements IIdentifierListener {
		private MUIElement element;

		public ViewActivator(MUIElement element) {
			this.element = element;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.activities.IIdentifierListener#identifierChanged(org
		 * .eclipse.ui.activities.IdentifierEvent)
		 */
		public void identifierChanged(IdentifierEvent identifierEvent) {
			IIdentifier identifier = identifierEvent.getIdentifier();

			// Not activated, do nothing
			if (!identifier.isEnabled())
				return;

			// stop listening for activations
			identifier.removeIdentifierListener(this);

			element.setToBeRendered(true);
		}
	}

	public ModeledPageLayout(MWindow window, EModelService modelService,
			EPartService partService,
			MPerspective perspModel, IPerspectiveDescriptor descriptor, WorkbenchPage page,
			boolean createReferences) {
		// this.window = window;
		MUIElement winParent = window.getParent();
		this.application = (MApplication) winParent;
		this.modelService = modelService;
		this.partService = partService;
		this.viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
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
		} else {
			List<MPartStack> stacks = modelService.findElements(sharedArea, null, MPartStack.class,
					null);
			if (!stacks.isEmpty()) {
				editorStack = stacks.get(0);
			}
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
		insertView(viewId, relationship, ratio, refId, true, false);
	}

	public void addStandaloneViewPlaceholder(String viewId, int relationship,
			float ratio, String refId, boolean showTitle) {
		insertView(viewId, relationship, ratio, refId, false,
				false);
	}

	public void addView(String viewId, int relationship, float ratio, String refId) {
		insertView(viewId, relationship, ratio, refId, true, true);
	}

	public void addView(String viewId, int relationship, float ratio, String refId,
			boolean minimized) {
		if (minimized) {
			E4Util.unsupported("addView: use of minimized for " + viewId + " ref " + refId); //$NON-NLS-1$ //$NON-NLS-2$
		}
		addView(viewId, relationship, ratio, refId);
	}

	protected boolean isViewFiltered(String viewID) {
		IViewDescriptor viewDescriptor = viewRegistry.find(viewID);
		if (viewDescriptor == null)
			return false;
		if (WorkbenchActivityHelper.restrictUseOf(viewDescriptor))
			return true;
		return WorkbenchActivityHelper.filterItem(viewDescriptor);
	}

	protected void addViewActivator(MUIElement element) {
		IPluginContribution contribution = (IPluginContribution) viewRegistry.find(element
				.getElementId());
		IWorkbenchActivitySupport support = PlatformUI.getWorkbench().getActivitySupport();
		IIdentifier identifier = support.getActivityManager().getIdentifier(
				WorkbenchActivityHelper.createUnifiedId(contribution));
		identifier.addIdentifierListener(new ViewActivator(element));
	}

	public IFolderLayout createFolder(String folderId, int relationship,
			float ratio, String refId) {
		MPartStack stack = insertStack(folderId, relationship, ratio, refId,
				false);
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
		EModelService ms = application.getContext().get(EModelService.class);
		MPartDescriptor partDesc = ms.getPartDescriptor(id);
		if (partDesc != null) {
			MPlaceholder ph = partService.createSharedPart(id);
			ph.setToBeRendered(visible);

			MPart part = (MPart) (ph.getRef());
			// as a shared part, this should be true, actual un/rendering
			// will be dependent on any placeholders that are referencing
			// this part
			part.setToBeRendered(true);

			// there should only be view references for 3.x views that are
			// visible to the end user, that is, the tab items are being
			// drawn
			if (visible
					&& createReferences
					&& CompatibilityPart.COMPATIBILITY_VIEW_URI.equals(partDesc
							.getContributionURI())) {
				page.createViewReferenceForPart(part, id);
			}
			return ph;
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

		// Hide views that are filtered by capabilities
		boolean isFiltered = isViewFiltered(viewId);

		MStackElement viewModel = createViewModel(application, viewId, visible && !isFiltered,
				page, partService,
				createReferences);
		if (viewModel != null) {
			if (withStack) {
				String stackId = viewId + "MStack"; // Default id...basically unusable //$NON-NLS-1$
				MPartStack stack = insertStack(stackId, relationship, ratio, refId, visible
						& !isFiltered);
				stack.getChildren().add(viewModel);
			} else {
				insert(viewModel, findRefModel(refId), plRelToSwt(relationship), ratio);
			}

		}

		if (isFiltered) {
			addViewActivator(viewModel);
		}
	}

	private MUIElement findRefModel(String refId) {
		MUIElement refModel = findElement(perspModel, refId);
		if (refModel instanceof MPart) {
			MUIElement parent = refModel.getParent();
			return parent instanceof MPartStack ? parent : refModel;
		} else if (refModel instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) refModel).getRef();
			if (ref instanceof MPart) {
				MUIElement parent = refModel.getParent();
				return parent instanceof MPartStack ? parent : refModel;
			}
		}
		return refModel;
	}

	private MUIElement getLastElement(MUIElement element) {
		if (element instanceof MElementContainer<?>) {
			MElementContainer<?> container = (MElementContainer<?>) element;
			List<?> children = container.getChildren();
			return children.isEmpty() ? container : getLastElement((MUIElement) children
					.get(children.size() - 1));
		}

		MUIElement parent = element.getParent();
		return parent == perspModel ? element : parent;
	}

	/**
	 * Returns the element that is the deepest and last element of the
	 * containers underneath the current perspective. If this element's parent
	 * is the perspective itself, the element will be returned. The perspective
	 * will only be returned if the perspective itself has no children.
	 * 
	 * @return the parent of the final element in the recursion chain of
	 *         children, or the element itself if its parent is the perspective,
	 *         or the perspective if the perspective itself has no children
	 */
	private MUIElement getLastElement() {
		List<MPartSashContainerElement> children = perspModel.getChildren();
		if (children.isEmpty()) {
			return perspModel;
		}
		return getLastElement(children.get(children.size() - 1));
	}

	private MPartStack insertStack(String stackId, int relationship,
			float ratio, String refId, boolean visible) {
		MUIElement refModel = findElement(perspModel, refId);
		if (refModel == null) {
			WorkbenchPlugin.log(NLS.bind(WorkbenchMessages.PageLayout_missingRefPart, refId));
			MPartStack stack = createStack(stackId, visible);
			insert(stack, getLastElement(), plRelToSwt(relationship), ratio);
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

		List<MUIElement> kids = parent.getChildren();
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

			if (relTo.isToBeRendered() || toInsert.isToBeRendered()) {
				// one of the items to be inserted should be rendered, render
				// all parent elements as well
				MUIElement parent = psc.getParent();
				while (parent != null && !(parent instanceof MPerspective)) {
					parent.setToBeRendered(true);
					parent = parent.getParent();
				}
				psc.setToBeRendered(true);
			} else {
				// no child elements need to be rendered, the parent part sash
				// container does not need to be rendered either then
				psc.setToBeRendered(false);
			}
			return;
		}
	}

	public static void insert(MUIElement toInsert, MUIElement relTo,
			int swtSide, float ratio) {
		int pct = (int) (ratio * 10000);
		insert(toInsert, relTo, swtSide, pct);
	}

	MUIElement findElement(MUIElement toSearch, String id) {
		List<Object> found = modelService.findElements(toSearch, id, null, null,
				EModelService.IN_ANY_PERSPECTIVE);
		if (found.size() > 0)
			return (MUIElement) found.get(0);

		MUIElement foundElement = modelService.find(id, toSearch);
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
		page.addHiddenItems(perspModel, HIDDEN_MENU_PREFIX + id);
	}

	public void addHiddenToolBarItemId(String id) {
		page.addHiddenItems(perspModel, HIDDEN_TOOLBAR_PREFIX + id);
	}

	public void removePlaceholder(String id) {
		MUIElement refModel = findElement(perspModel, id);
		if (!(refModel instanceof MPlaceholder)) {
			E4Util.unsupported("removePlaceholder: failed to find " + id + ": " + refModel); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		// placeholders in the shared area should be ignored
		if (modelService.getElementLocation(refModel) != EModelService.IN_SHARED_AREA) {
			MElementContainer<MUIElement> parent = refModel.getParent();
			if (parent != null) {
				parent.getChildren().remove(refModel);
			}
		}
	}

	public void stackView(String id, String refId, boolean visible) {
		MUIElement refModel = refId.equals(getEditorArea()) ? editorStack : findElement(perspModel,
				refId);
		if (refModel == null) {
			addView(id, LEFT, DEFAULT_VIEW_RATIO, refId);
			return;
		}

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
			MPartStack stack = (MPartStack) refModel;
			boolean wasEmpty = stack.getChildren().isEmpty();
			stack.getChildren().add(viewModel);
			if (wasEmpty && visible) {
				// the stack didn't originally have any children, set this as
				// the selected element
				stack.setSelectedElement(viewModel);
			}

			if (visible) {
				// ensure that the parent is being rendered, it may have been a
				// placeholder folder so its flag may actually be false
				refModel.setToBeRendered(true);
			}
		}
	}
}
