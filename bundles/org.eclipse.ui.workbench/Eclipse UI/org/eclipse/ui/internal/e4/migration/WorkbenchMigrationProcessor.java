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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.StickyViewDescriptor;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class WorkbenchMigrationProcessor {

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	@Inject
	private EModelService modelService;

	private IMemento workbenchMemento;

	private File legacyWorkbenchFile;

	private boolean migrated;

	private List<MWindow> defaultWindows;

	public boolean isLegacyWorkbenchDetected() {
		legacyWorkbenchFile = getLegacyWorkbenchFile();
		return legacyWorkbenchFile != null && legacyWorkbenchFile.exists();
	}

	public void migrate() {
		if (!isLegacyWorkbenchDetected()) {
			return;
		}

		workbenchMemento = loadMemento();
		if (workbenchMemento == null) {
			return;
		}

		defaultWindows = new ArrayList<>(application.getChildren());
		application.getChildren().clear();
		IEclipseContext builderContext = context.createChild();
		IModelBuilderFactory builderFactory = ContextInjectionFactory.make(
				ModelBuilderFactoryImpl.class, builderContext);
		builderContext.set(IModelBuilderFactory.class, builderFactory);
		ApplicationBuilder modelBuilder = builderFactory.createApplicationBuilder(new WorkbenchMementoReader(
				workbenchMemento));
		modelBuilder.createApplication();
		context.remove(E4Workbench.NO_SAVED_MODEL_FOUND);
		PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_INTRO, false);
		migrated = true;
	}

	private IMemento loadMemento() {
		BufferedReader reader = null;
		IMemento memento = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(legacyWorkbenchFile), "utf-8")); //$NON-NLS-1$
			memento = XMLMemento.createReadRoot(reader);
		} catch (IOException e) {
			WorkbenchPlugin.log("Failed to load " + legacyWorkbenchFile.getAbsolutePath(), e); //$NON-NLS-1$
		} catch (WorkbenchException e) {
			WorkbenchPlugin.log("Failed to load " + legacyWorkbenchFile.getAbsolutePath(), e); //$NON-NLS-1$
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					WorkbenchPlugin.log(e);
				}
			}
		}
		return memento;
	}

	private File getLegacyWorkbenchFile() {
		if (legacyWorkbenchFile == null) {
			IPath path = WorkbenchPlugin.getDefault().getDataLocation();
			if (path == null) {
				return null;
			}
			path = path.append(Workbench.DEFAULT_WORKBENCH_STATE_FILENAME);
			legacyWorkbenchFile = path.toFile();
		}
		return legacyWorkbenchFile;
	}

	public boolean isWorkbenchMigrated() {
		return migrated;
	}

	public void updatePartsAfterMigration(IPerspectiveRegistry perspectiveRegistry,
			IViewRegistry viewRegistry) {
		if (!migrated) {
			return;
		}

		for (MPartDescriptor desc : application.getDescriptors()) {
			for (MPart part : modelService.findElements(application, desc.getElementId(), MPart.class, null)) {
				if (part.getLabel() == null) {
					part.setLabel(desc.getLocalizedLabel());
				}
				if (part.getTooltip() == null) {
					part.setTooltip(desc.getLocalizedTooltip());
				}
				if (part.getIconURI() == null) {
					part.setIconURI(desc.getIconURI());
				}
			}
		}

		for (MPerspective persp : modelService.findElements(application, null, MPerspective.class, null)) {
			setPerspectiveIcon(perspectiveRegistry, persp);
		}

		for (MUIElement snippet : application.getSnippets()) {
			if (snippet instanceof MPerspective) {
				setPerspectiveIcon(perspectiveRegistry, (MPerspective) snippet);
			}
		}

		IStickyViewDescriptor[] stickyViews = viewRegistry.getStickyViews();
		for (MWindow window : application.getChildren()) {
			moveStickyViews(stickyViews, window);
		}

	}

	private void moveStickyViews(IStickyViewDescriptor[] stickyViews, MWindow window) {
		for (IStickyViewDescriptor stickyView : stickyViews) {
			removeStickyViewFromPerspectives(stickyView, window);
		}
		for (MPartStack stickyFolder : modelService.findElements(application,
				StickyViewDescriptor.STICKY_FOLDER_RIGHT, MPartStack.class, null)) {
			fillStickyFolder(stickyViews, stickyFolder);
		}
	}

	private void removeStickyViewFromPerspectives(IStickyViewDescriptor stickyView, MWindow window) {
		for (MPlaceholder placeholder : modelService.findElements(window, stickyView.getId(),
				MPlaceholder.class, null)) {
			MElementContainer<MUIElement> parent = placeholder.getParent();
			if (StickyViewDescriptor.STICKY_FOLDER_RIGHT.equals(parent.getElementId())) {
				continue;
			}
			placeholder.setToBeRendered(false);
			placeholder.setVisible(false);
			parent.getChildren().remove(placeholder);
			// remove empty container
			if (parent.getChildren().isEmpty()) {
				parent.getParent().getChildren().remove(parent);
			} else if (parent.getSelectedElement() == placeholder) {
				parent.setSelectedElement(null);
			}
		}
	}

	private void fillStickyFolder(IStickyViewDescriptor[] stickyViews, MPartStack stickyFolder) {
		for (IStickyViewDescriptor stickyView : stickyViews) {
			addPartToStickyFolder(stickyView.getId(), stickyFolder);
		}
	}

	private void setPerspectiveIcon(IPerspectiveRegistry perspectiveRegistry, MPerspective perspective) {
		String perspId = perspective.getElementId();
		if (perspective.getTransientData().containsKey(PerspectiveBuilder.ORIGINAL_ID)) {
			perspId = (String) perspective.getTransientData().get(PerspectiveBuilder.ORIGINAL_ID);
		}
		IPerspectiveDescriptor orgPerspDescr = perspectiveRegistry.findPerspectiveWithId(perspId);
		if (orgPerspDescr != null) {
			perspective.setIconURI(MenuHelper.getIconURI(orgPerspDescr.getImageDescriptor(), context));
		}
	}

	private MPlaceholder addPartToStickyFolder(String partId, MPartStack stickyFolder) {
		MPart part = null;
		MWindow window = modelService.getTopLevelWindowFor(stickyFolder);
		for (MUIElement element : window.getSharedElements()) {
			if (element.getElementId().equals(partId)) {
				part = (MPart) element;
				break;
			}
		}
		if (part == null) {
			part = modelService.createModelElement(MPart.class);
			part.setElementId(partId);
			part.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
			part.getTags().add(ViewRegistry.VIEW_TAG);
			window.getSharedElements().add(part);
		}
		MPlaceholder placeholder = null;
		placeholder = modelService.createModelElement(MPlaceholder.class);
		placeholder.setElementId(partId);
		placeholder.setRef(part);
		placeholder.setToBeRendered(false);
		part.setCurSharedRef(placeholder);
		stickyFolder.getChildren().add(placeholder);
		return placeholder;
	}

	public void restoreDefaultModel() {
		application.getTags().clear();
		application.getPersistedState().clear();
		application.getSnippets().clear();
		application.getDescriptors().clear();
		application.getChildren().clear();
		application.getChildren().addAll(defaultWindows);
	}

}
