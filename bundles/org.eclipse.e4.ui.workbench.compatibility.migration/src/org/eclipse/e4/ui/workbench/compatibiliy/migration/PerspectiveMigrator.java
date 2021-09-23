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
package org.eclipse.e4.ui.workbench.compatibiliy.migration;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.internal.persistence.IWorkbenchState;
import org.eclipse.e4.ui.workbench.internal.persistence.impl.WorkbenchState;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.persistence.PerspectivePersister;
import org.eclipse.e4.ui.workbench.persistence.common.CommonUtil;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * The PerspectiveMigrator implementation.
 *
 * @since 3.3
 *
 */
@SuppressWarnings("restriction")
public final class PerspectiveMigrator{

	private PerspectiveMigrator() {}
	
	/**
	 * Apply a 3.x workbench state to the running 4.x application.
	 *
	 * @param iMemento The 3.x workbench state to apply
	 */
	public static void apply3xWorkbenchState(final IMemento iMemento) {
		MApplication application = convertToMApplication(iMemento);
		IWorkbenchState workbenchState = convertToWorkbenchState(application);
		PerspectivePersister.restoreWorkbenchState(workbenchState);
	}

	/**
	 * Convert a serialized 3.x workbench to an e4 MApplication.
	 *
	 * @param iMemento The 3.x workbench memento
	 * @return The resulting MApplication
	 */
	public static MApplication convertToMApplication(final IMemento iMemento) {
		Converter converter = new Converter();
		return converter.convert(iMemento);
	}

	/**
	 * Extract the WorkbenchState from the MApplication. This state can be used to
	 * restore the workbench.
	 *
	 * @param application The MApplication to extract the state from
	 * @return The extracted WorkbenchState
	 */
	public static IWorkbenchState convertToWorkbenchState(final MApplication application) {
		EModelService modelService = CommonUtil.getEModelService();
		MPerspective activePerspective = modelService.getActivePerspective(CommonUtil.getCurrentMainWindow());
		List<MPerspective> findElements = modelService.findElements(application, null, MPerspective.class, null);

		MPerspective mPerspective = null;
		String elementId = activePerspective.getElementId();
		for (MPerspective perspective : findElements) {
			if (elementId.equals(perspective.getElementId())) {
				mPerspective = perspective;
			}
		}

		if (mPerspective == null) {
			System.err
					.println("No perspective with id " + elementId + " was found. Using first perspective to restore"); //$NON-NLS-1$ //$NON-NLS-2$
			mPerspective = findElements.get(0);
			mPerspective.setElementId(elementId);
		}

		return createLegacyWorkbenchState(mPerspective);
	}
	/**
	 * Check whether the provided memento contains a legacy (3.x) workbench.
	 *
	 * @param iMemento The serialized IMemento
	 * @return true if the memento contains a legacy workbench.
	 */
	public static boolean isLegacyWorkbench(IMemento iMemento) {
		try {
			return IWorkbenchConstants.TAG_WORKBENCH.equals(iMemento.getType());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return false;
	}

	/**
	 * Creates the persistable workbench state for a legacy (3x) perspective.
	 *
	 * @param perspective The MPerspective to create 3.x the WorkbenchState for
	 * @return The created {@link WorkbenchState}
	 */
	public static IWorkbenchState createLegacyWorkbenchState(final MPerspective perspective) {
		return CommonUtil.doCreateWorkbenchState(perspective);
	}
}
