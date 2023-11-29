/*******************************************************************************
 * Copyright (c) 2005, 2020 IBM Corporation and others.
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
 *     Jan-Ove Weichel <ovi.weichel@gmail.com> - Bug 441573
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.MarkerViewHandler;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * QuickFixHandler is the command handler for the quick fix dialog.
 *
 * @since 3.4
 */
public class QuickFixHandler extends MarkerViewHandler {

	private static class QuickFixWizardDialog extends WizardDialog {

		public QuickFixWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
			setShellStyle(SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER
					| SWT.MODELESS | SWT.RESIZE | getDefaultOrientation());
		}

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ExtendedMarkersView view = getView(event);
		if (view == null)
			return this;

		final Map<IMarkerResolution, Collection<IMarker>> resolutionsMap = new LinkedHashMap<>();
		final IMarker[] selectedMarkers = view.getSelectedMarkers();
		final IMarker firstSelectedMarker = selectedMarkers[0];

		IRunnableWithProgress resolutionsRunnable = monitor -> {
			monitor.beginTask(MarkerMessages.resolveMarkerAction_computationManyAction, 100);

			IMarker[] allMarkers = view.getAllMarkers();
			monitor.worked(20);
			IMarkerResolution[] resolutions = IDE.getMarkerHelpRegistry().getResolutions(firstSelectedMarker);
			int progressCount = 80;
			if (resolutions.length > 1) {
				progressCount= progressCount / resolutions.length;
			}
			for (IMarkerResolution markerResolution : resolutions) {
				if (markerResolution instanceof WorkbenchMarkerResolution) {
					IMarker[] other = ((WorkbenchMarkerResolution)markerResolution).findOtherMarkers(allMarkers);
					if (containsAllButFirst(other, selectedMarkers)) {
						Collection<IMarker> markers1 = new LinkedHashSet<>(other.length + 1);
						// Duplicates will not be added due to set
						markers1.add(firstSelectedMarker);
						markers1.addAll(Arrays.asList(other));
						resolutionsMap.put(markerResolution, markers1);
					}
				} else if (selectedMarkers.length == 1) {
					Collection<IMarker> markers2 = new ArrayList<>(1);
					markers2.add(firstSelectedMarker);
					resolutionsMap.put(markerResolution, markers2);
				}
				monitor.worked(progressCount);
			}
			monitor.done();
		};

		IWorkbenchSiteProgressService service = Adapters.adapt(view.getSite(), IWorkbenchSiteProgressService.class);

		IRunnableContext context = new ProgressMonitorDialog(view.getSite().getShell());

		try {
			if (service == null) {
				PlatformUI.getWorkbench().getProgressService().runInUI(context, resolutionsRunnable, null);
			} else {
				service.runInUI(context, resolutionsRunnable, null);
			}
		} catch (InvocationTargetException | InterruptedException exception) {
			throw new ExecutionException(exception.getLocalizedMessage(), exception);
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
			Consumer<StructuredViewer> showMarkers = v -> new ShowMarkers(v, view.getSite());
			Consumer<Control> bindHelp = c -> PlatformUI.getWorkbench().getHelpSystem().setHelp(c,
					IWorkbenchHelpContextIds.PROBLEMS_VIEW);
			Consumer<Throwable> reporter = t -> StatusManager.getManager().handle(StatusUtil.newError(t));
			Wizard wizard = new QuickFixWizard(description, selectedMarkers, resolutionsMap, showMarkers, bindHelp,
					reporter);
			wizard.setWindowTitle(MarkerMessages.resolveMarkerAction_dialogTitle);
			WizardDialog dialog = new QuickFixWizardDialog(view.getSite().getShell(), wizard);
			dialog.open();
		}
		return this;
	}

	/**
	 * Checks whether the given extent contains all all but the first element from the given members
	 * array.
	 *
	 * @param extent the array which should contain the elements
	 * @param members the elements to check
	 * @return <code>true</code> if all but the first element are inside the extent
	 * @since 3.7
	 */
	private static boolean containsAllButFirst(Object[] extent, Object[] members) {
		outer: for (int i= 1; i < members.length; i++) {
			for (Object e : extent) {
				if (members[i] == e) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}
}
