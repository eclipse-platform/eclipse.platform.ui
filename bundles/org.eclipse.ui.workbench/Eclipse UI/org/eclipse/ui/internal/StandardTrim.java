/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 471310
 ******************************************************************************/

package org.eclipse.ui.internal;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TaskBar;
import org.eclipse.swt.widgets.TaskItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.ProgressRegion;
import org.eclipse.ui.internal.progress.TaskBarProgressManager;
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
		IEclipseContext context = modelService.getContainingContext(toolControl);
		IEclipseContext child = context.createChild(ProgressRegion.class.getName());
		child.set(MToolControl.class, toolControl);
		child.set(Composite.class, parent);
		ContextInjectionFactory.make(ProgressRegion.class, child);

		if (parent.getDisplay() != null && parent.getDisplay().getSystemTaskBar() != null) {
			// only create the TaskBarProgressManager if there is a TaskBar that
			// the progress can be displayed on
			TaskItem taskItem = null;
			TaskBar systemTaskBar = parent.getDisplay().getSystemTaskBar();
			taskItem = systemTaskBar.getItem(parent.getShell());
			if (taskItem == null) {
				// try to get the application TaskItem
				taskItem = systemTaskBar.getItem(null);
			}

			if (taskItem != null) {
				// If there is a TaskItem, see if there is
				// TaskBarProgressManager already associated with it to make
				// sure that we don't duplicate the progress information
				String taskBarProgressManagerKey = TaskBarProgressManager.class.getName() + ".instance"; //$NON-NLS-1$
				Object data = taskItem.getData(taskBarProgressManagerKey);
				if (data == null || !(data instanceof TaskBarProgressManager)) {
					taskItem.setData(taskBarProgressManagerKey, new TaskBarProgressManager(taskItem));
				}
			}
		}
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
