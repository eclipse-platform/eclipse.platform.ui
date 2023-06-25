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
package org.eclipse.e4.tools.internal.persistence.util;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.internal.persistence.IWorkbenchState;
import org.eclipse.e4.tools.internal.persistence.impl.WorkbenchState;
import org.eclipse.e4.tools.persistence.PerspectivePersister;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IEditorPart;
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

	public static void persistCompatibilityEditors(MUIElement root) {
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

	public static List<MPart> getPerspectivePartsWithState(final MPerspective perspective) {
		List<MPlaceholder> phList = CommonUtil.getEModelService().findElements(perspective, null, MPlaceholder.class,
				null);
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

	/**
	 * Retrieve the EPartService from the EclipseContext.
	 *
	 * @return The EPartService
	 */
	public static EPartService getEPartService() {
		return CommonUtil.getEclipseContext().get(EPartService.class);
	}

	/**
	 * Retrieve the MPerspective from a MWindow based on the perspectiveId.
	 *
	 * @param window        The MWindow to search
	 * @param perspectiveId The perspective id to search for
	 *
	 * @return The MPerspective
	 */
	public static MPerspective getMainPerspectiveFromWindow(final MWindow window, final String perspectiveId) {

		final List<MPerspective> findElements = CommonUtil.getEModelService().findElements(window, MPerspective.class,
				EModelService.ANYWHERE, new Selector() {

					@Override
					public boolean select(final MApplicationElement element) {
						return Objects.equals(element.getElementId(), perspectiveId);
					}
				});

		if (findElements == null || findElements.isEmpty()) {
			throw new IllegalStateException("No perspective found with id " + perspectiveId); //$NON-NLS-1$
		} else if (findElements.size() > 1) {
			throw new IllegalStateException("Too many perspectives found"); //$NON-NLS-1$
		}
		return findElements.get(0);
	}

	/**
	 * Creates the persistable workbench state for an e4 perspective.
	 *
	 * @param window      the current application window. This is used to persist
	 *                    the current trim bars
	 * @param perspective The perspective to persist
	 * @return The created {@link WorkbenchState}
	 */
	public static IWorkbenchState createWorkbenchState(final MWindow window, final MPerspective perspective) {
		if (window == null) {
			throw new NullPointerException("Window must not be null"); //$NON-NLS-1$
		}
		IWorkbenchState workbenchState = PerspectivePersister.convertPerspective(perspective);

		// Add left and right trimbars that might contain MinMax controls
		if (window instanceof MTrimmedWindow) {
			MTrimmedWindow tw = (MTrimmedWindow) window;

			// Get side trim bars, clone them, and add them to the workbench state
			tw.getTrimBars().stream().filter(CommonUtil::isSideBar)
					.map(t -> (MTrimBar) CommonUtil.getEModelService().cloneElement(t, null))
					.forEach(workbenchState.getTrimBars()::add);
		}

		return workbenchState;
	}

	/**
	 * Retrieve the IEditorPart from a MPart wrapped in an Optional.
	 *
	 * @param part The MPart to get the IEditorPart from
	 * @return The optional IEditorPart
	 */
	public static Optional<IEditorPart> getOptionalEditorPart(final MPart part) {
		if (!CompatibilityEditor.class.isInstance(part.getObject())) {
			return Optional.empty();
		}
		CompatibilityEditor ce = CompatibilityEditor.class.cast(part.getObject());
		if (ce == null) {
			return Optional.empty();
		}
		IEditorPart editorPart = ce.getEditor();
		return Optional.ofNullable(editorPart);
	}

	/**
	 * Returns whether the given {@link MTrimBar} is a side trim bar.
	 *
	 * @param trimBar The MTrimBar to check
	 * @return true if the {@link MTrimBar} is a side trim bar (left or right).
	 */
	public static boolean isSideBar(MTrimBar trimBar) {
		return trimBar.getSide() == SideValue.LEFT || trimBar.getSide() == SideValue.RIGHT;
	}
}
