/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.views.markers.MarkerViewHandler;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * QuickFixHandler is the command handler for the quick fix dialog.
 * 
 * @since 3.4
 * 
 */
public class QuickFixHandler extends MarkerViewHandler {

	private class QuickFixWizardDialog extends WizardDialog {

		/**
		 * @param parentShell
		 * @param newWizard
		 */
		public QuickFixWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
					| SWT.MODELESS | SWT.RESIZE | getDefaultOrientation());	
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ExtendedMarkersView view = getView(event);
		if (view == null)
			return this;

		final Map resolutionsMap = new LinkedHashMap();
		final IMarker[] selectedMarkers = view.getSelectedMarkers();
		final IMarker firstSelectedMarker = selectedMarkers[0];

		IRunnableWithProgress resolutionsRunnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor
						.beginTask(
								MarkerMessages.resolveMarkerAction_computationManyAction,
								100);

				IMarker[] allMarkers = view.getAllMarkers();
				monitor.worked(20);
				IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry().getResolutions(firstSelectedMarker);
				int progressCount = 80;
				if (resolutions.length > 1)
					progressCount= progressCount / resolutions.length;
				for (int i = 0; i < resolutions.length; i++) {
					IMarkerResolution markerResolution= resolutions[i];
					if (markerResolution instanceof WorkbenchMarkerResolution) {
						IMarker[] other = ((WorkbenchMarkerResolution)markerResolution).findOtherMarkers(allMarkers);
						if (containsAllButFirst(other, selectedMarkers)) {
							Collection markers = new ArrayList(other.length + 1);
							markers.add(firstSelectedMarker);
							markers.addAll(Arrays.asList(other));
							resolutionsMap.put(markerResolution, markers);
						}
					} else if (selectedMarkers.length == 1) {
						Collection markers = new ArrayList(1);
						markers.add(firstSelectedMarker);
						resolutionsMap.put(markerResolution, markers);
					}
					monitor.worked(progressCount);
				}
				monitor.done();
			}
		};

		Object service = view.getSite().getAdapter(
				IWorkbenchSiteProgressService.class);

		IRunnableContext context = new ProgressMonitorDialog(view.getSite()
				.getShell());

		try {
			if (service == null) {
				PlatformUI.getWorkbench().getProgressService().runInUI(context,
						resolutionsRunnable, null);
			} else {
				((IWorkbenchSiteProgressService) service).runInUI(context,
						resolutionsRunnable, null);
			}
		} catch (InvocationTargetException exception) {
			throw new ExecutionException(exception.getLocalizedMessage(),
					exception);
		} catch (InterruptedException exception) {

			throw new ExecutionException(exception.getLocalizedMessage(),
					exception);
		}

		String markerDescription= firstSelectedMarker.getAttribute(IMarker.MESSAGE,
				MarkerSupportInternalUtilities.EMPTY_STRING);
		if (resolutionsMap.isEmpty()) {
			if (selectedMarkers.length == 1) {
				MessageDialog
				.openInformation(
						view.getSite().getShell(),
						MarkerMessages.resolveMarkerAction_dialogTitle,
						NLS	.bind(MarkerMessages.MarkerResolutionDialog_NoResolutionsFound,
								new Object[] { markerDescription }));
			} else {
				MessageDialog
				.openInformation(
						view.getSite().getShell(),
						MarkerMessages.resolveMarkerAction_dialogTitle,
						MarkerMessages.MarkerResolutionDialog_NoResolutionsFoundForMultiSelection);
				
			}
		} else {

			String description = NLS.bind(
					MarkerMessages.MarkerResolutionDialog_Description,
					markerDescription);

			Wizard wizard= new QuickFixWizard(description, selectedMarkers, resolutionsMap, view
					.getSite());
			wizard.setWindowTitle(MarkerMessages.resolveMarkerAction_dialogTitle);
			WizardDialog dialog = new QuickFixWizardDialog(view.getSite()
					.getShell(), wizard);
			dialog.open();
		}
		return this;
	}

	/**
	 * Checks whether the given extent contains all all but the first element from the given array.
	 * 
	 * @param extent the array which should contain the elements
	 * @param members the elements to check
	 * @return <code>true</code> if all but the first element are inside the extent
	 * @since 3.7
	 */
	private static boolean containsAllButFirst(Object[] extent, Object[] members) {
		outer: for (int i= 1; i < members.length; i++) {
			for (int j= 0; j < extent.length; j++) {
				if (members[i] == extent[j])
					continue outer;
			}
			return false;
		}
		return true;
	}
}
