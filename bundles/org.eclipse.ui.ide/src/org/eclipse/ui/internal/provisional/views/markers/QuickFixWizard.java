/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * QuickFixWizard is the wizard for quick fixes.
 * 
 * @since 3.4
 * 
 */
class QuickFixWizard extends Wizard {

	private Map resolutionMap;

	/**
	 * Create the wizard with the map of resolutions.
	 * 
	 * @param resolutions
	 *            Map key {@link IMarker} value {@link IMarkerResolution} []
	 * @param page
	 *            The page to display the problem in.
	 */
	public QuickFixWizard(Map resolutions) {
		super();
		this.resolutionMap = resolutions;
		setDefaultPageImageDescriptor(IDEInternalWorkbenchImages
				.getImageDescriptor(IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG));
		setNeedsProgressMonitor(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		Iterator markers = resolutionMap.keySet().iterator();
		while (markers.hasNext()) {
			IMarker next = (IMarker) markers.next();
			IMarkerResolution[] resolutions = (IMarkerResolution[]) resolutionMap
					.get(next);
			addPage(new QuickFixPage(next, resolutions));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		IRunnableWithProgress finishRunnable = new IRunnableWithProgress() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void run(IProgressMonitor monitor)
				 {
				IWizardPage[] pages = getPages();
				monitor.beginTask(MarkerMessages.MarkerResolutionDialog_Fixing,
						(10 * pages.length) + 1);
				monitor.worked(1);
				for (int i = 0; i < pages.length; i++) {
					//Allow for cancel event processing
					getShell().getDisplay().readAndDispatch();
					if(monitor.isCanceled())
						return;
					QuickFixPage wizardPage = (QuickFixPage) pages[i];
					wizardPage.performFinish(new SubProgressMonitor(monitor,10));
					monitor.worked(1);
				}
				monitor.done();

			}
		};

		try {
			getContainer().run(false, true, finishRunnable);
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e));
			return false;
		} catch (InterruptedException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e));
			return false;
		}

		return true;
	}

}
