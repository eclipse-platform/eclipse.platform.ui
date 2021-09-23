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
package org.eclipse.e4.ui.workbench.persistence.common;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.internal.persistence.IPartMemento;
import org.eclipse.e4.ui.workbench.internal.persistence.IPersistenceFactory;
import org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityView;

/**
 * A Util for the workbench conversion.
 *
 * @since 3.3
 *
 */
@SuppressWarnings({ "restriction" })
public final class CommonUtil {

	private CommonUtil() {
	}

	/**
	 * The key to use for storing the memento.
	 */
	public static String MEMENTO_KEY = "memento"; //$NON-NLS-1$
	/**
	 * Constant for the EDITOR_AREA.
	 */
	public static String EDITOR_AREA = "org.eclipse.ui.editorss"; //$NON-NLS-1$

	/**
	 * Retrieve the IEclipseContext from the Workbench.
	 *
	 * @return The IEclipseContext
	 */
	public static IEclipseContext getEclipseContext() {
		return Workbench.getInstance().getContext();
	}

	/**
	 * Retrieve the EModelService from the EclipseContext.
	 *
	 * @return The EModelService
	 */
	public static EModelService getEModelService() {
		return getEclipseContext().get(EModelService.class);
	}



	/**
	 * Retrieve the first MWindow from the MApplication from the EclipseContext.
	 *
	 * @return The MWindow
	 */
	public static MWindow getCurrentMainWindow() {
		MApplication mApplication = getEclipseContext().get(MApplication.class);
		List<MWindow> children = mApplication.getChildren();
		MWindow mWindow = children.get(0);
		return mWindow;
	}

	/**
	 * @param perspective The perspective to persist
	 * @return The created WorkbenchState
	 */
	public static IWorkbenchState doCreateWorkbenchState(final MPerspective perspective) {
		MSnippetContainer snippetContainer = createSnippetContainer();
		persistCompatibilityEditors(perspective);
		CommonUtil.getEModelService().cloneElement(perspective, snippetContainer);
		IWorkbenchState workbenchState = IPersistenceFactory.eINSTANCE.createWorkbenchState();
		workbenchState.setPerspective((MPerspective) snippetContainer.getSnippets().get(0));
		List<MPart> parts = getPerspectivePartsWithState(perspective);
		for (MPart part : parts) {
			IPartMemento partMemento = IPersistenceFactory.eINSTANCE.createPartMemento();
			partMemento.setPartId(part.getElementId());
			partMemento.setMemento(part.getPersistedState().get(MEMENTO_KEY));
			workbenchState.getViewSettings().add(partMemento);
		}

		// Editor area
		MPlaceholder editorAreaPlaceholder = (MPlaceholder) CommonUtil.getEModelService().find(EDITOR_AREA, perspective);
		if (editorAreaPlaceholder != null) {
			MArea editorArea = (MArea) editorAreaPlaceholder.getRef();
			persistCompatibilityEditors(editorArea);
			MArea clonedEditorArea = (MArea) CommonUtil.getEModelService().cloneElement(editorArea, null);
			workbenchState.setEditorArea(clonedEditorArea);
			// TODO copy editor area view state of non editors
		}

		return workbenchState;
	}

	private static void persistCompatibilityEditors(MUIElement root) {
		CommonUtil.getEModelService().findElements(root, CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class)
				.forEach(CommonUtil::persistCompatibilityEditor);
	}

	/**
	 * If the part represents a CompatibilityEditor, writes the editor's state to
	 * the MPart's persisted state.
	 */
	private static void persistCompatibilityEditor(MPart part) {
		if (part.getObject() instanceof CompatibilityEditor) {
			// Use reflection to call EditorReference's persist method.
			// It writes all relevant information of the editor (id, editor input, etc.) to
			// the MPart's persisted state
			// With this, the editor can be restored.
			// Reflection is necessary because EditorReference#persist is package protected.
			CompatibilityEditor editor = (CompatibilityEditor) part.getObject();
			EditorReference reference = (EditorReference) editor.getReference();
			try {
				Method persistMethod = reference.getClass().getDeclaredMethod("persist"); //$NON-NLS-1$
				persistMethod.setAccessible(true);
				persistMethod.invoke(reference);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new IllegalStateException("Cannot call EditorReference's persist method", e); //$NON-NLS-1$
			}
		}
	}

	private static List<MPart> getPerspectivePartsWithState(final MPerspective perspective) {
		List<MPlaceholder> phList = CommonUtil.getEModelService().findElements(perspective, null, MPlaceholder.class, null);
		List<MPart> result = new ArrayList<MPart>();
		for (MPlaceholder ph : phList) {
			MUIElement element = ph.getRef();
			if (!MPart.class.isInstance(element)) {
				continue;
			}
			MPart part = MPart.class.cast(element);

			persist(part);
			result.add(part);
		}
		return result;
	}

	private static void persist(final MPart part) {
		Optional<IViewPart> viewPart = getOptionalViewPart(part);
		if (viewPart.isPresent()) {
			XMLMemento root = XMLMemento.createWriteRoot("view"); //$NON-NLS-1$
			viewPart.get().saveState(root);
			StringWriter writer = new StringWriter();
			try {
				root.save(writer);
				part.getPersistedState().put(MEMENTO_KEY, writer.toString());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Retrieve the IViewPart from a MPart wrapped in an Optional.
	 *
	 * @param part The MPart to get the IViewPart from
	 * @return The optional IViewPart
	 */
	public static Optional<IViewPart> getOptionalViewPart(final MPart part) {
		if (!CompatibilityView.class.isInstance(part.getObject())) {
			return Optional.empty();
		}
		CompatibilityView cv = CompatibilityView.class.cast(part.getObject());
		if (cv == null) {
			return Optional.empty();
		}
		IViewPart viewPart = cv.getView();
		return Optional.ofNullable(viewPart);
	}

	/**
	 * Create a new MSnippetContainer.
	 *
	 * @return The MSnippetContainer
	 */
	public static MSnippetContainer createSnippetContainer() {
		return new MSnippetContainer() {

			private final List<MUIElement> list = new ArrayList<>();

			@Override
			public List<MUIElement> getSnippets() {
				return list;
			}
		};
	}
}
