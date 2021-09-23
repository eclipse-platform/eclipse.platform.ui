/*******************************************************************************
 * Copyright (c) 2021 EclipseSource GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     EclipseSource GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento;
import org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.persistence.common.CommonUtil;
import org.eclipse.e4.ui.workbench.persistence.impl.Util;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


/**
 * Persists and restores perspectives from a {@link IWorkbenchWindow}.
 */
public final class PerspectivePersister {

	private PerspectivePersister() {
	}

	/**
	 * Serialize a specific perspective from the running application.
	 *
	 * @param perspectiveId The id of the perspective to extract and serialize
	 * @return the serialized WorkbenchState
	 */
	public static String serializePerspectiveAndPartStates(final String perspectiveId) {
		final MWindow mWindow = CommonUtil.getCurrentMainWindow();
		final MPerspective perspective = Util.getMainPerspectiveFromWindow(mWindow, perspectiveId);
		final IWorkbenchState workbenchState = Util.createWorkbenchState(mWindow, perspective);
		final String xml = serialize(workbenchState);
		return xml;
	}

	private static String serialize(final EObject eObject) {
		final EObject copy = EcoreUtil.copy(eObject);
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Resource resource = resourceSet.createResource(URI.createURI("virtualUri")); //$NON-NLS-1$
		// changes containment, therefore we use a copy
		resource.getContents().add(copy);
		try (Writer writer = new StringWriter()) {
			final URIConverter.WriteableOutputStream uws = new URIConverter.WriteableOutputStream(writer, "UTF-8"); //$NON-NLS-1$
			resource.save(uws, null);
			final String xml = writer.toString().trim();
			writer.close();
			return xml;

		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Deserialize and restore the WorkbenchState.
	 *
	 * @param serializedState The serialized WorkbenchState
	 */
	public static void restoreWorkbenchState(final String serializedState) {
		final IWorkbenchState toBeMerged = deserialize(serializedState);
		restoreWorkbenchState(toBeMerged);
	}

	/**
	 * Restore the WorkbenchState.
	 *
	 * @param workbenchState The WorkbenchState to restore
	 */
	public static void restoreWorkbenchState(final IWorkbenchState workbenchState) {
		final MWindow currentWindow = CommonUtil.getCurrentMainWindow();
		final EPartService ePartService = Util.getEPartService();
		disposeCurrentParts(ePartService.getParts());
		MPerspective perspective = workbenchState.getPerspective();
		ePartService.switchPerspective(perspective.getElementId());

		Shell widget = (Shell) currentWindow.getWidget();
		// Turn of redrawing while we update the complete layout
		widget.setRedraw(false);

		try {
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			List<IViewPart> viewPartsToHide = new ArrayList<>();
			List<IViewReference> viewReferencesToHide = new ArrayList<>();
			List<MPart> parts = new ArrayList<>();

			for (final IPartMemento viewSetting : workbenchState.getViewSettings()) {
				final List<MPart> elements = CommonUtil.getEModelService().findElements(currentWindow,
						viewSetting.getPartId(), MPart.class, null);
				if (elements.size() < 1) {
					System.err.println("Part ID has not been found. View Settings cannot be restored for part: " //$NON-NLS-1$
							+ viewSetting.getPartId());
					continue;
				}

				final MPart part = elements.get(0);
				parts.add(part);

				final Optional<IViewPart> viewPart = CommonUtil.getOptionalViewPart(part);
				if (viewPart.isPresent()) {
					viewPartsToHide.add(viewPart.get());
				} else {
					// If we get no part this might mean the view has not been activated yet
					IViewReference viewRef = (IViewReference) part.getTransientData()
							.get(IWorkbenchPartReference.class.getName());
					if (viewRef != null) {
						// Make sure we use not an old reference object
						IViewReference resolved = activePage.findViewReference(viewRef.getId());
						// Activate forcefully otherwise we are unable to close the view and have to
						// deal with
						// WidgetDisposedExceptions
						if (resolved == null) {
							try {
								IViewPart view = activePage.showView(viewRef.getId());
								if (view != null) {
									viewPartsToHide.add(view);
								}
							} catch (PartInitException e) {
								System.err.println("Failed to initialize part"); //$NON-NLS-1$
								e.printStackTrace();
							}
						} else {
							viewReferencesToHide.add(resolved);
						}
					}
				}
			}

			// First hide references to not forcefully activate them
			viewReferencesToHide.forEach(activePage::hideView);
			viewPartsToHide.forEach(activePage::hideView);

			// Bug in e4/JFace/Compat-Layer disposing Images in
			// org.eclipse.ui.part.WorkbenchPart but they are kept in
			// JFaceResources.getImageRegistry().get(String)
			for (MPart p : parts) {
				if (p.getIconURI() != null) {
					Image image = JFaceResources.getImageRegistry().get(p.getIconURI());
					if (image != null && image.isDisposed()) {
						JFaceResources.getImageRegistry().remove(p.getIconURI());
					}
				}
			}

			for (final IPartMemento viewSetting : workbenchState.getViewSettings()) {
				final List<MPart> elements = CommonUtil.getEModelService().findElements(currentWindow,
						viewSetting.getPartId(), MPart.class, null);
				if (elements.size() < 1) {
					System.err.println("Part ID has not been found. View Settings cannot be restored for part: " //$NON-NLS-1$
							+ viewSetting.getPartId());
					continue;
				}

				final MPart part = elements.get(0);
				parts.add(part);

				part.getPersistedState().put(CommonUtil.MEMENTO_KEY, viewSetting.getMemento());
			}

			replaceExistingPerspective(currentWindow, workbenchState.getPerspective(),
					workbenchState.getPerspective().getElementId());

			if (workbenchState.getEditorArea() != null) {
				replaceEditorAreaContents(currentWindow, workbenchState.getEditorArea());
			}

			// Replace side trim bars to ensure persisted minimized part stacks don't get
			// lost.
			// This is necessary because a MToolControl to restore a part stack (and its
			// child parts) is only created the first time a part stack is minimized.
			// If a part stack was never minimized in the current workspace but is minimized
			// in the persisted workbench, there are no min max controls without restoring
			// the side bars.
			if (currentWindow instanceof MTrimmedWindow) {
				replaceSideTrimBars((MTrimmedWindow) currentWindow, workbenchState.getTrimBars());
			}

			// Re-minimize part stacks to trigger min-max addon
			MPerspective appliedPerspective = Util.getMainPerspectiveFromWindow(currentWindow,
					workbenchState.getPerspective().getElementId());
			List<MPartStack> stacks = CommonUtil.getEModelService().findElements(appliedPerspective, null, MPartStack.class);
			stacks.stream().filter(s -> s.getTags().contains(IPresentationEngine.MINIMIZED)).forEach(s -> {
				s.getTags().remove(IPresentationEngine.MINIMIZED);
				s.getTags().add(IPresentationEngine.MINIMIZED);
			});
		} finally {
			widget.setRedraw(true);
		}
	}

	private static void disposeCurrentParts(Collection<MPart> parts) {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		List<IViewPart> viewPartsToHide = new LinkedList<>();
		List<IEditorPart> editorsToHide = new LinkedList<>();
		parts.forEach(part -> {
			Optional<IViewPart> viewPart = CommonUtil.getOptionalViewPart(part);
			if (viewPart.isPresent()) {
				viewPartsToHide.add(viewPart.get());
				return;
			}
			Optional<IEditorPart> editorPart = Util.getOptionalEditorPart(part);
			if (editorPart.isPresent()) {
				editorsToHide.add(editorPart.get());
				return;
			}

			// the editor/view was not activated yet
			// we need to be careful that this does not get activated by a neighbor being
			// disposed and this is getting focus -> hide it now
			Util.getEPartService().hidePart(part);
		});
		viewPartsToHide.forEach(activePage::hideView);
		editorsToHide.forEach(e -> activePage.closeEditor(e, true));
	}

	/**
	 * Replaces side trim bars (left and right) in the given window. If a trim bar
	 * of a side is given, add it to the window. If a trim bar of the same side
	 * already exists in the window, replace its children with the persisted one's
	 * children.
	 *
	 * @param window   The window whose side trim bars to replace
	 * @param trimBars The list of replacement trim bars.
	 */
	private static void replaceSideTrimBars(MTrimmedWindow window, List<MTrimBar> trimBars) {
		for (MTrimBar trimBar : trimBars) {
			if (!Util.isSideBar(trimBar)) {
				continue;
			}
			MTrimBar clonedTrimBar = (MTrimBar) CommonUtil.getEModelService().cloneElement(trimBar, null);
			SideValue side = trimBar.getSide();

			// If a trim bar of the current side exists, replace its children with the
			// persisted trim bar's children. This is done because deleting a trim bar logs
			// a NPE in the error log.
			// If no trim bar of the current side exists, insert the persisted one.
			Optional<MTrimBar> existingTrimOptional = window.getTrimBars().stream().filter(t -> t.getSide() == side)
					.findFirst();
			if (existingTrimOptional.isPresent()) {
				final MTrimBar existingTrim = existingTrimOptional.get();
				List<MTrimElement> children = new LinkedList<>(existingTrim.getChildren());
				children.forEach(CommonUtil.getEModelService()::deleteModelElement);

				// remove trim elements that are not part of current perspective anymore
				// leads to a NPE otherwise
				List<MTrimElement> trimElementsToAdd = clonedTrimBar.getChildren().stream()//
						.filter(te -> isTrimPartOfPerspective(te, window))//
						.collect(Collectors.toList());
				existingTrim.getChildren().addAll(trimElementsToAdd);
				existingTrim.getTags().clear();
				existingTrim.getTags().addAll(trimBar.getTags());
				existingTrim.setVisible(trimBar.isVisible());
				existingTrim.setToBeRendered(trimBar.isToBeRendered());
			} else {
				window.getTrimBars().add(clonedTrimBar);
			}
		}
	}

	@SuppressWarnings("restriction")
	private static boolean isTrimPartOfPerspective(MTrimElement te, MTrimmedWindow window) {
		EModelService eModelService = CommonUtil.getEModelService();
		MPerspective activePerspective = eModelService.getActivePerspective(window);
		Map<org.eclipse.e4.ui.workbench.addons.minmax.TrimStackIdHelper.TrimStackIdPart, String> parsedIds = org.eclipse.e4.ui.workbench.addons.minmax.TrimStackIdHelper
				.parseTrimStackId(te.getElementId());
		String stackId = parsedIds
				.get(org.eclipse.e4.ui.workbench.addons.minmax.TrimStackIdHelper.TrimStackIdPart.ELEMENT_ID);
		MUIElement foundElement = eModelService.find(stackId, activePerspective);
		return foundElement != null;
	}

	private static IWorkbenchState deserialize(final String serializedState) {
		final ResourceSet resourceSet = new ResourceSetImpl();
		final Resource resource = resourceSet.createResource(URI.createURI("virtualUri")); //$NON-NLS-1$

		try (Reader reader = new StringReader(serializedState)) {
			final URIConverter.ReadableInputStream is = new URIConverter.ReadableInputStream(reader, "UTF-8"); //$NON-NLS-1$
			resource.load(is, null);
			final EObject eObject = resource.getContents().get(0);
			if (!(eObject instanceof IWorkbenchState)) {
				throw new IllegalStateException("Corrupted Perspective File"); //$NON-NLS-1$
			}
			return (IWorkbenchState) eObject;
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void replaceExistingPerspective(final MWindow currentWindow, final MPerspective toBeMerged,
			final String perspectiveName) {
		final MPerspective currentPerspective = Util.getMainPerspectiveFromWindow(currentWindow, perspectiveName);
		final Control control = (Control) currentPerspective.getWidget();

		// If it exists, don't render editor area before removing the old perspective
		// because otherwise the AreaRenderer throws an exception if the editor area was
		// split.
		MUIElement editorArea = CommonUtil.getEModelService().find(CommonUtil.EDITOR_AREA, currentPerspective);
		if (editorArea != null) {
			editorArea.setToBeRendered(false);
		}

		final MPerspectiveStack mPerspectiveStack = (MPerspectiveStack) deletePerspective(currentPerspective);
		control.dispose();
		final MPerspective clonedPerspective = addPerspective(toBeMerged, mPerspectiveStack, currentWindow);

		// Remember the active element
		final MUIElement activeElement = getActiveElement(clonedPerspective);

		// Show the UI
		final EPartService ePartService = Util.getEPartService();
		ePartService.switchPerspective(clonedPerspective);

		// Reset the activation, which might be adjusted by the part activation history
		if (activeElement instanceof MPart) {
			ePartService.activate((MPart) activeElement, true);
		}
	}

	private static void replaceEditorAreaContents(final MWindow currentWindow, final MArea toBeMerged) {
		List<MArea> editorAreaList = CommonUtil.getEModelService().findElements(currentWindow, CommonUtil.EDITOR_AREA, MArea.class,
				null, EModelService.OUTSIDE_PERSPECTIVE | EModelService.IN_SHARED_AREA);
		final MSnippetContainer snippetContainer = CommonUtil.createSnippetContainer();
		snippetContainer.getSnippets().add(toBeMerged);
		final MArea cloneSnippet = (MArea) CommonUtil.getEModelService().cloneSnippet(snippetContainer,
				toBeMerged.getElementId(), currentWindow);

		// Replace children of existing editor area: If there is a placeholder for an
		// editor area, there is also a shared element.
		MArea editorArea = editorAreaList.get(0);

		while (editorArea.getChildren().size() > 0) {
			Object widget = editorArea.getChildren().get(0).getWidget();
			EcoreUtil.delete((EObject) editorArea.getChildren().get(0));
			// Dispose widgets of removed children to avoid lingering "zombie" widgets
			if (widget instanceof Control) {
				((Control) widget).dispose();
			}
		}
		editorArea.getChildren().addAll(cloneSnippet.getChildren());

		// Activate all editors once to avoid problems with closing uninitialized
		// editors later on.
		activateEditors(editorArea);
	}

	/**
	 * Activates all editors in the given editor area once to initialize them.
	 *
	 * @param editorArea
	 */
	private static void activateEditors(MArea editorArea) {
		final EPartService partService = Util.getEPartService();

		// Remember selection of all part stacks because activating a part selects it in
		// the part stack.
		// We want to restore the original selection after the activations are
		// completed.
		final List<MPartStack> partStacks = CommonUtil.getEModelService().findElements(editorArea, null, MPartStack.class);
		final Map<MPartStack, MStackElement> stackSelections = new LinkedHashMap<>();
		for (MPartStack partStack : partStacks) {
			stackSelections.put(partStack, partStack.getSelectedElement());
		}

		// Activate parts without the need to get focus
		final List<MPart> editorParts = CommonUtil.getEModelService().findElements(editorArea, null, MPart.class);
		for (MPart editor : editorParts) {
			partService.activate(editor, false);
		}

		// Restore original part stack selections
		for (MPartStack partStack : stackSelections.keySet()) {
			partStack.setSelectedElement(stackSelections.get(partStack));
		}
	}

	private static MUIElement getActiveElement(final MPerspective clonedPerspective) {
		MUIElement activeElement = clonedPerspective;
		// search the active Element
		while (activeElement instanceof MElementContainer<?>) {
			final MUIElement selectedElement = ((MElementContainer<?>) activeElement).getSelectedElement();
			if (selectedElement == null) {
				break;
			}
			activeElement = selectedElement;
		}

		if (activeElement instanceof MPlaceholder) {
			activeElement = ((MPlaceholder) activeElement).getRef();
		}
		return activeElement;
	}

	private static EObject deletePerspective(final MPerspective currentPerspective) {
		final EObject eContainer = ((EObject) currentPerspective).eContainer();
		EcoreUtil.delete((EObject) currentPerspective);
		return eContainer;
	}

	private static MPerspective addPerspective(final MPerspective perspective2BeMerged,
			final MPerspectiveStack mPerspectiveStack, final MWindow currentWindow) {
		final MSnippetContainer snippetContainer = CommonUtil.createSnippetContainer();
		snippetContainer.getSnippets().add(perspective2BeMerged);
		final MUIElement cloneSnippet = CommonUtil.getEModelService().cloneSnippet(snippetContainer,
				perspective2BeMerged.getElementId(), currentWindow);
		mPerspectiveStack.getChildren().add((MPerspective) cloneSnippet);
		return (MPerspective) cloneSnippet;
	}

}
