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
package org.eclipse.e4.ui.workbench.persistence.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState;
import org.eclipse.e4.ui.workbench.internal.persistence.impl.WorkbenchState;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.persistence.common.CommonUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;

/**
 * A Util for the workbench conversion.
 *
 * @since 3.3
 *
 */
@SuppressWarnings("restriction")
public final class Util {

	private Util() {
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
		IWorkbenchState workbenchState = CommonUtil.doCreateWorkbenchState(perspective);

		// Add left and right trimbars that might contain MinMax controls
		if (window instanceof MTrimmedWindow) {
			MTrimmedWindow tw = (MTrimmedWindow) window;

			// Get side trim bars, clone them, and add them to the workbench state
			tw.getTrimBars().stream().filter(Util::isSideBar)
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
