package org.eclipse.ui.views.markers.internal;

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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;

/**
 * MarkerResolutionWizard is the wizard for selecting a series of markers with
 * the same resolutions.
 * 
 * @since 3.1
 * 
 */
public class MarkerResolutionWizard extends Wizard {

	private IMarker[] selectedMarkers;

	private IMarker[] otherMarkers;

	private ArrayList wizardPages;

	/**
	 * Create an instance of the receiver on markers. allMarkers can be used to
	 * determine the other matching markers.
	 * 
	 * @param markers
	 * @param allMarkers
	 */
	MarkerResolutionWizard(IMarker[] markers, IMarker[] allMarkers) {
		selectedMarkers = markers;
		otherMarkers = allMarkers;
		setWindowTitle(MarkerMessages.MarkerResolutionWizard_Title);
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {

		Iterator pageIterator = wizardPages.iterator();
		while (pageIterator.hasNext()) {
			addPage((WizardPage) pageIterator.next());
		}

	}

	/**
	 * Add the other markers to the pages.
	 * @param wizardPages
	 */
	public void determineOtherMarkers(Collection wizardPages) {
		// Add in the extra markers if there are pages for the,
		for (int i = 0; i < otherMarkers.length; i++) {
			IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry()
					.getResolutions(otherMarkers[i]);
			MarkerResolutionPage page = getPageFor(wizardPages, resolutions,
					false);
			if (page != null)
				page.addOtherMarker(otherMarkers[i]);
		}
	}

	/**
	 * Find the page that has resolutions in wizardPages. If it is not found and
	 * create is true create it and return. Otherwise return <code>null</code>.
	 * 
	 * @param wizardPages
	 * @param resolutions
	 * @param create
	 * @return MarkerResolutionPage or <code>null</code>.
	 */
	private MarkerResolutionPage getPageFor(Collection wizardPages,
			IMarkerResolution[] resolutions, boolean create) {

		Iterator currentPages = wizardPages.iterator();
		Arrays.sort(resolutions, getResolutionComparator());
		Comparator resolutionComparator = getResolutionComparator();

		while (currentPages.hasNext()) {
			MarkerResolutionPage page = (MarkerResolutionPage) currentPages
					.next();
			IMarkerResolution[] pageResolutions = page.getResolutions();
			if (pageResolutions.length != resolutions.length)
				continue;

			boolean matches = true;
			for (int i = 0; i < resolutions.length; i++) {
				if (resolutionComparator.compare(resolutions[i],
						pageResolutions[i]) != 0) {
					matches = false;
					break;
				}
			}
			if (matches)
				return page;
		}

		// Didn't find the page so make a new one if creating.
		if (create) {
			MarkerResolutionPage page = new MarkerResolutionPage(resolutions);
			wizardPages.add(page);
			return page;
		}
		return null;

	}

	/**
	 * Get a comparator for marker resolutions.
	 * 
	 * @return Comparator
	 */
	public static Comparator getResolutionComparator() {
		return new Comparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object arg0, Object arg1) {

				return ((IMarkerResolution) arg0).getLabel().compareTo(
						((IMarkerResolution) arg1).getLabel());
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		final IWizardPage[] pages = getPages();
		
		IRunnableWithProgress doneRunnable = new IRunnableWithProgress(){
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				monitor.beginTask(MarkerMessages.MarkerResolutionWizard_FixingTask, (pages.length + 1) * 100);
				monitor.worked(100);
				
				for (int i = 0; i < pages.length; i++) {
					MarkerResolutionPage page = (MarkerResolutionPage) pages[i];
					if (!monitor.isCanceled() && page.isPageComplete())
						page.getCompletionRunnable().run( new SubProgressMonitor(monitor,100,SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
					else
						monitor.worked(100);
				}
				monitor.done();
				
			}
		};
		
		try {
			getContainer().run(false, true, doneRunnable);
		} catch (InvocationTargetException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		
		return true;
	}

	/**
	 * Do a preprocess to figures out the pages.
	 * @param monitor The monitor to report to.
	 */
	public void determinePages(IProgressMonitor monitor) {
		wizardPages = new ArrayList();
		monitor.beginTask(
				MarkerMessages.MarkerResolutionWizard_CalculatingTask,
				selectedMarkers.length + 1);
		monitor.worked(1);

		// Create all of the pages for the selected markers
		for (int i = 0; i < selectedMarkers.length; i++) {
			monitor.subTask(NLS.bind(
					MarkerMessages.MarkerResolutionWizard_WorkingSubTask, Util
							.getProperty(IMarker.MESSAGE, selectedMarkers[i])));
			IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry()
					.getResolutions(selectedMarkers[i]);
			if (resolutions.length > 0) {
				MarkerResolutionPage page = getPageFor(wizardPages,
						resolutions, true);
				page.addMarker(selectedMarkers[i]);
			}
			monitor.worked(1);
		}

	}

}
