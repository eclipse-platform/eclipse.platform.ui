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

package org.eclipse.ui.internal.handlers;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.services.IEvaluationService;

/**
 * @since 3.7
 * 
 */
public class DirtyStateTracker implements IPartListener, IWindowListener,
		IPropertyListener {

	private final IWorkbench workbench;

	public DirtyStateTracker() {

		workbench = Workbench.getInstance();
		workbench.addWindowListener(this);
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		register(window);
	}

	public void update() {
		IEvaluationService service = (IEvaluationService) workbench
				.getService(IEvaluationService.class);
		service.requestEvaluation(ISources.ACTIVE_PART_NAME);
	}

	/**
	 * @param window
	 */
	private void register(IWorkbenchWindow window) {
		if (window == null)
			return;
		window.getPartService().addPartListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof ISaveablePart) {
			part.addPropertyListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart
	 * )
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof ISaveablePart) {
			part.removePropertyListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart
	 * )
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof ISaveablePart) {
			part.addPropertyListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.
	 * IWorkbenchWindow)
	 */
	public void windowActivated(IWorkbenchWindow window) {
		register(window);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.
	 * IWorkbenchWindow)
	 */
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow
	 * )
	 */
	public void windowClosed(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow
	 * )
	 */
	public void windowOpened(IWorkbenchWindow window) {
		register(window);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object,
	 * int)
	 */
	public void propertyChanged(Object source, int propID) {
		if (source instanceof ISaveablePart && propID == ISaveablePart.PROP_DIRTY) {
			update();
		}
	}

}
