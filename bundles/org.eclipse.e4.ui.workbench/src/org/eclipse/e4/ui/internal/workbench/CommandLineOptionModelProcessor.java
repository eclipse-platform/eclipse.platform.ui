/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 486583
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Activates the perspectives specified via the content key
 * <code>E4Workbench.FORCED_PERSPECTIVE_ID</code>.
 *
 * Might evaluate more keys in the future
 */
public class CommandLineOptionModelProcessor {
	@Inject
	private IEclipseContext context;

	@Inject
	private MApplication application;

	@Inject
	private EModelService modelService;

	public void process() {
		selectForcedPerspective();
	}

	private void selectForcedPerspective() {
		String forcedPerspectiveId = (String) context.get(E4Workbench.FORCED_PERSPECTIVE_ID);
		if (forcedPerspectiveId == null) {
			return;
		}

		List<MPerspectiveStack> perspStackList = modelService.findElements(application, null,
				MPerspectiveStack.class, null);

		if (perspStackList.isEmpty()) {
			return;
		}

		MPerspectiveStack perspStack = perspStackList.get(0);
		MPerspective selected = perspStack.getSelectedElement();

		if (selected != null && selected.getElementId().equals(forcedPerspectiveId)) {
			return;
		}

		for (MPerspective persp : perspStack.getChildren()) {
			if (persp.getElementId().equals(forcedPerspectiveId)) {
				perspStack.setSelectedElement(persp);
				return;
			}
		}
	}
}
