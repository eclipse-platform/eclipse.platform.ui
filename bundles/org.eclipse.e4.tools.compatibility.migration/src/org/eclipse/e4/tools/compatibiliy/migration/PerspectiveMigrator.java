/*******************************************************************************
 * Copyright (c) 2022 EclipseSource GmbH and others.
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
package org.eclipse.e4.tools.compatibiliy.migration;

import java.util.List;

import org.eclipse.e4.tools.internal.compatibiliy.migration.E4MigrationTool;
import org.eclipse.e4.tools.internal.compatibiliy.migration.WorkbenchUtil;
import org.eclipse.e4.tools.internal.persistence.IWorkbenchState;
import org.eclipse.e4.tools.persistence.PerspectivePersister;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * The PerspectiveMigrator utility allows to convert 
 * and apply 3.x and e4 perspectives.
 *
 * @since 1.0
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
		return E4MigrationTool.convert(iMemento);
	}

	/**
	 * Extract the WorkbenchState from the MApplication. This state can be used to
	 * restore the workbench.
	 *
	 * @param application The MApplication to extract the state from
	 * @return The extracted WorkbenchState
	 */
	public static IWorkbenchState convertToWorkbenchState(final MApplication application) {
		EModelService modelService = WorkbenchUtil.getEModelService();
		MPerspective activePerspective = modelService.getActivePerspective(WorkbenchUtil.getCurrentMainWindow());
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

		return PerspectivePersister.convertPerspective(mPerspective);
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

}
