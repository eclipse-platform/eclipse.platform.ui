/*******************************************************************************
 * Copyright (c) 2007, 2020 IBM Corporation and others.
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
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.internal.workspace.markers.Translation;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * QuickFixWizard is the wizard for quick fixes.
 *
 * @since 3.4
 */
class QuickFixWizard extends Wizard {

	private IMarker[] selectedMarkers;
	private Map<IMarkerResolution, Collection<IMarker>> resolutionMap;
	private String description;
	private final Consumer<StructuredViewer> showMarkers;
	private final Consumer<Control> bindHelp;
	private final Consumer<Throwable> reporter;
	private QuickFixPage quickFixPage;

	/**
	 * Create the wizard with the map of resolutions.
	 *
	 * @param description     the description of the problem
	 * @param selectedMarkers the markers that were selected
	 * @param resolutions     Map key {@link IMarkerResolution} value
	 *                        {@link IMarker} []
	 * @param showMarkers     the consumer to show markers
	 * @param bindHelp        the consumer to bind help system
	 * @param reporter        used to report failures during
	 *                        {@link Wizard#performFinish()} call
	 */
	public QuickFixWizard(String description, IMarker[] selectedMarkers,
			Map<IMarkerResolution, Collection<IMarker>> resolutions, Consumer<StructuredViewer> showMarkers,
			Consumer<Control> bindHelp,
			Consumer<Throwable> reporter) {
		Objects.requireNonNull(reporter);
		this.selectedMarkers= selectedMarkers;
		this.resolutionMap = resolutions;
		this.description = description;
		this.showMarkers = showMarkers;
		this.bindHelp = bindHelp;
		this.reporter = reporter;
		setDefaultPageImageDescriptor(IDEInternalWorkbenchImages
				.getImageDescriptor(IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG));
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		quickFixPage = new QuickFixPage(description, selectedMarkers, resolutionMap, showMarkers, bindHelp);
		addPage(quickFixPage);
	}

	@Override
	public boolean performFinish() {
		Optional<IMarkerResolution> resolution = quickFixPage.getSelectedMarkerResolution();
		if (!resolution.isPresent()) {
			return true; // for backward compatibility
		}
		final IMarker[] markers = quickFixPage.getCheckedMarkers();
		if (markers.length == 0) {
			return true; // for backward compatibility
		}
		IRunnableWithProgress finishRunnable = monitor -> processResolution(resolution.get(), markers, monitor);
		try {
			getContainer().run(false, true, finishRunnable);
		} catch (InvocationTargetException | InterruptedException e) {
			reporter.accept(e);
			return false;
		}
		return true;
	}

	/**
	 * Runs the resolution in UI thread. It should be in UI because a lot of
	 * resolutions expect to be executed in the UI thread at the moment. We can
	 * think about some interface to mark resolutions that are ready to be executed
	 * outside of the UI thread.
	 *
	 * @param resolution the resolution to run
	 * @param markers    the array of markers to resolve
	 * @param monitor    progress callback
	 */
	private void processResolution(final IMarkerResolution resolution, final IMarker[] markers,
			IProgressMonitor monitor) {
		monitor.beginTask(MarkerMessages.MarkerResolutionDialog_Fixing, markers.length);
		ensureRepaint();
		if (resolution instanceof WorkbenchMarkerResolution) {
			((WorkbenchMarkerResolution) resolution).run(markers, monitor);
		} else {
			Translation translation = new Translation();
			for (IMarker marker : markers) {
				ensureRepaint();
				if (monitor.isCanceled()) {
					return;
				}
				monitor.subTask(translation.message(marker).orElse("")); //$NON-NLS-1$
				resolution.run(marker);
				monitor.worked(1);
			}
		}
	}

	/**
	 * org.eclipse.jface.wizard.ProgressMonitorPart needs to take a breath
	 */
	private void ensureRepaint() {
		final Display display = getShell().getDisplay();
		boolean dispatch = true;
		while (dispatch) {
			dispatch = display.readAndDispatch();
		}
	}

}
