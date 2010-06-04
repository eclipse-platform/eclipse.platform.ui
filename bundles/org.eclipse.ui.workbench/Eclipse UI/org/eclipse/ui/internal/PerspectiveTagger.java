/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		if (id.equals("org.eclipse.jdt.ui.JavaPerspective")) { //$NON-NLS-1$
			tagJavaPerspective(perspective, modelService);
		} else if (id.equals("org.eclipse.team.cvs.ui.cvsPerspective")) { //$NON-NLS-1$
			tagCVSPerspective(perspective, modelService);
		} else if (id.equals("org.eclipse.team.ui.TeamSynchronizingPerspective")) { //$NON-NLS-1$
			tagTeamPerspective(perspective, modelService);
		} else if (id.equals("org.eclipse.debug.ui.DebugPerspective")) { //$NON-NLS-1$
			tagDebugPerspective(perspective, modelService);
		} else if (id.equals("org.eclipse.ui.resourcePerspective")) { //$NON-NLS-1$
			tagResourcePerspective(perspective, modelService);
		} else if (id.equals("org.eclipse.pde.ui.PDEPerspective")) { //$NON-NLS-1$
			tagPluginDevelopmentPerspective(perspective, modelService);
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
		MUIElement element = modelService.find(
				"org.eclipse.debug.internal.ui.NavigatorFolderView", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.primaryNavigationStack"); //$NON-NLS-1$
		}

		element = modelService.find(
				"org.eclipse.debug.internal.ui.ConsoleFolderView", perspective); //$NON-NLS-1$
		if (element != null) {
			element.getTags().add("org.eclipse.e4.secondaryDataStack"); //$NON-NLS-1$
		}

		element = modelService.find(
				"org.eclipse.debug.internal.ui.OutlineFolderView", perspective); //$NON-NLS-1$
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

	static void tagPluginDevelopmentPerspective(MPerspective perspective,
			EModelService modelService) {
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