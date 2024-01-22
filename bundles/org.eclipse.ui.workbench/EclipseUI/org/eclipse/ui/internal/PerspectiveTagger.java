/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.e4.ui.workbench.modeling.EModelService;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;

public class PerspectiveTagger {
	/**
	 * Alters known 3.x perspective part folders into their e4 counterparts.
	 */
	public static void tagPerspective(MPerspective perspective, EModelService modelService) {
		String id = perspective.getElementId();
		if (id == null) {
			return;
		}

		// see bug 305557
		switch (id) {
		case "org.eclipse.jdt.ui.JavaPerspective": //$NON-NLS-1$
			tagJavaPerspective(perspective, modelService);
			break;
		case "org.eclipse.team.cvs.ui.cvsPerspective": //$NON-NLS-1$
			tagCVSPerspective(perspective, modelService);
			break;
		case "org.eclipse.team.ui.TeamSynchronizingPerspective": //$NON-NLS-1$
			tagTeamPerspective(perspective, modelService);
			break;
		case "org.eclipse.debug.ui.DebugPerspective": //$NON-NLS-1$
			tagDebugPerspective(perspective, modelService);
			break;
		case "org.eclipse.ui.resourcePerspective": //$NON-NLS-1$
			tagResourcePerspective(perspective, modelService);
			break;
		case "org.eclipse.pde.ui.PDEPerspective": //$NON-NLS-1$
			tagPluginDevelopmentPerspective(perspective, modelService);
			break;
		default:
			break;
		}
	}

	static void tagJavaPerspective(MPerspective perspective, EModelService modelService) {
		MUIElement element = modelService.find("left", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}

		element = modelService.find("bottom", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
		}

		element = modelService.find("right", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryNavigationStack"); //$NON-NLS-1$
		}
	}

	static void tagCVSPerspective(MPerspective perspective, EModelService modelService) {
		MUIElement element = modelService.find("top", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}
	}

	static void tagTeamPerspective(MPerspective perspective, EModelService modelService) {
		MUIElement element = modelService.find("top", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}

		element = modelService.find("top2", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
		}
	}

	static void tagDebugPerspective(MPerspective perspective, EModelService modelService) {
		MUIElement element = modelService.find("org.eclipse.debug.internal.ui.NavigatorFolderView", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}

		element = modelService.find("org.eclipse.debug.internal.ui.ConsoleFolderView", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
		}

		element = modelService.find("org.eclipse.debug.internal.ui.OutlineFolderView", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryNavigationStack"); //$NON-NLS-1$
		}
	}

	static void tagResourcePerspective(MPerspective perspective, EModelService modelService) {
		MUIElement element = modelService.find("topLeft", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}

		element = modelService.find("bottomRight", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
		}

		element = modelService.find("bottomLeft", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryNavigationStack"); //$NON-NLS-1$
		}
	}

	static void tagPluginDevelopmentPerspective(MPerspective perspective, EModelService modelService) {
		MUIElement element = modelService.find("topLeft", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}

		element = modelService.find("bottomRight", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
		}
	}
}