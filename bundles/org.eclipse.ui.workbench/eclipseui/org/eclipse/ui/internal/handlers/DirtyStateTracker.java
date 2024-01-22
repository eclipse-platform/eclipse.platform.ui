/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 372799
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.E4PartWrapper;
import org.eclipse.ui.internal.SaveableHelper;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.7
 */
public class DirtyStateTracker implements IPartListener, IWindowListener, IPropertyListener {

	private final IWorkbench workbench;

	public DirtyStateTracker() {
		workbench = Workbench.getInstance();
		workbench.addWindowListener(this);
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		register(window);
	}

	public void update() {
		IEvaluationService service = workbench.getService(IEvaluationService.class);
		service.requestEvaluation(ISources.ACTIVE_PART_NAME);
	}

	private void register(IWorkbenchWindow window) {
		if (window == null) {
			return;
		}
		window.getPartService().addPartListener(this);
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (SaveableHelper.isSaveable(part)) {
			part.addPropertyListener(this);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (SaveableHelper.isSaveable(part)) {
			part.removePropertyListener(this);
			update();
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (SaveableHelper.isSaveable(part)) {
			part.addPropertyListener(this);
		}
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		register(window);
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		register(window);
	}

	@Override
	public void propertyChanged(Object source, int propID) {
		if (SaveableHelper.isSaveable(source) && propID == ISaveablePart.PROP_DIRTY) {
			update();
		} else if (source instanceof E4PartWrapper && propID == ISaveablePart.PROP_DIRTY) {
			update();
		}
	}

}
