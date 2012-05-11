/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.ProgressRegion;
import org.eclipse.ui.internal.util.PrefUtil;

/**
 * @since 3.5
 *
 */
public class StandardTrim {
	@Inject
	EModelService modelService;

	private StatusLineManager manager;

	@PostConstruct
	void createWidget(Composite parent, MToolControl toolControl) {
		if (toolControl.getElementId().equals("org.eclipse.ui.StatusLine")) { //$NON-NLS-1$
			createStatusLine(parent, toolControl);
		} else if (toolControl.getElementId().equals("org.eclipse.ui.HeapStatus")) { //$NON-NLS-1$
			createHeapStatus(parent, toolControl);
		} else if (toolControl.getElementId().equals("org.eclipse.ui.ProgressBar")) { //$NON-NLS-1$
			createProgressBar(parent, toolControl);
		}
	}

	@PreDestroy
	void destroy() {
		if (manager != null) {
			manager.dispose();
			manager = null;
		}
	}

	/**
	 * @param parent
	 * @param toolControl
	 */
	private void createProgressBar(Composite parent, MToolControl toolControl) {
		ProgressRegion progressRegion = new ProgressRegion();
		IEclipseContext context = modelService.getContainingContext(toolControl);
		WorkbenchWindow wbw = (WorkbenchWindow) context.get(IWorkbenchWindow.class);
		progressRegion.createContents(parent, wbw);
	}

	/**
	 * @param parent
	 * @param toolControl
	 */
	private void createHeapStatus(Composite parent, MToolControl toolControl) {
		new HeapStatus(parent, PrefUtil.getInternalPreferenceStore());
	}

	/**
	 * @param parent
	 * @param toolControl
	 */
	private void createStatusLine(Composite parent, MToolControl toolControl) {
		IEclipseContext context = modelService.getContainingContext(toolControl);
		WorkbenchWindow wbw = (WorkbenchWindow) context.get(IWorkbenchWindow.class);
		// wbw may be null if workspace is started with no open perspectives.
		if (wbw == null) {
			// Create one assuming there's no defined perspective
			Workbench wb = (Workbench) PlatformUI.getWorkbench();
			wb.createWorkbenchWindow(wb.getDefaultPageInput(), null,
					modelService.getTopLevelWindowFor(toolControl), false);
			wbw = (WorkbenchWindow) context.get(IWorkbenchWindow.class);
		}

		if (wbw != null) {
			Workbench wb = (Workbench) PlatformUI.getWorkbench();
			wb.createWorkbenchWindow(wb.getDefaultPageInput(), null,
					modelService.getTopLevelWindowFor(toolControl), false);
			wbw = (WorkbenchWindow) context.get(IWorkbenchWindow.class);

			manager = wbw.getStatusLineManager();
			manager.createControl(parent);
		}
	}
}
