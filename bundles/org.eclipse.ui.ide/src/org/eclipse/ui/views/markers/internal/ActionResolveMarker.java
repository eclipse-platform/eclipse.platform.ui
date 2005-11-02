/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * This action displays a list of resolutions for the selected marker
 * 
 * @since 2.0
 */
public class ActionResolveMarker extends MarkerSelectionProviderAction {

	private MarkerView view;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param markerView
	 * @param provider
	 */
	public ActionResolveMarker(MarkerView markerView,
			ISelectionProvider provider) {
		super(provider, MarkerMessages.resolveMarkerAction_title);
		setEnabled(false);
		view = markerView;
	}

	/**
	 * Displays a list of resolutions and performs the selection.
	 */
	public void run() {

		final MarkerResolutionWizard[] wizard = new MarkerResolutionWizard[1];

		Job processingJob = new WorkbenchJob(
				MarkerMessages.ActionResolveMarker_CalculatingJob) {

			public IStatus runInUIThread(IProgressMonitor monitor) {

				IMarker[] markers = getSelectedMarkers();

				try {
					wizard[0] = new MarkerResolutionWizard(markers, MarkerList
							.compute(view.getMarkerTypes()));
					wizard[0].determinePages(monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}

		};

		processingJob.addJobChangeListener(new JobChangeAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {

				WorkbenchJob dialogJob = new WorkbenchJob(
						MarkerMessages.ActionResolveMarker_OpenWizardJob) {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
					 */
					public IStatus runInUIThread(IProgressMonitor monitor) {
						(new WizardDialog(view.getSite().getShell(), wizard[0]))
								.open();
						return Status.OK_STATUS;
					}
				};
				dialogJob.setSystem(true);
				dialogJob.schedule();

			}
		});

		processingJob.setUser(true);
		processingJob.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection selection) {
		
		if(Util.isSingleConcreteSelection(selection)){
			if(IDE.getMarkerHelpRegistry().hasResolutions(getSelectedMarker())){
				setEnabled(true);
				return;
			}
		}
		
		setEnabled(false);
	}
}
