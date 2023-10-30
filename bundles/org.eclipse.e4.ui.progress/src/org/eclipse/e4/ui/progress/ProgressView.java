/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.progress;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.progress.internal.DetailedProgressViewer;
import org.eclipse.e4.ui.progress.internal.FinishedJobs;
import org.eclipse.e4.ui.progress.internal.ProgressManager;
import org.eclipse.e4.ui.progress.internal.ProgressManagerUtil;
import org.eclipse.e4.ui.progress.internal.ProgressViewUpdater;
import org.eclipse.e4.ui.progress.internal.ProgressViewerContentProvider;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * @noreference
 */
public class ProgressView {

	DetailedProgressViewer viewer;

	@Inject
	ESelectionService selectionService;

	ISelectionChangedListener selectionListener;

	@PostConstruct
	public void createPartControl(Composite parent, ProgressManager progressManager,
			IProgressService progressService, FinishedJobs finishedJobs,
			ProgressViewUpdater viewUpdater) {
		viewer = new DetailedProgressViewer(parent, SWT.MULTI,
				progressService, finishedJobs);
		viewer.setComparator(ProgressManagerUtil.getProgressViewerComparator());

		viewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

//		helpSystem.setHelp(parent, IWorkbenchHelpContextIds.RESPONSIVE_UI);

		ProgressViewerContentProvider provider = new ProgressViewerContentProvider(
				viewer, finishedJobs, viewUpdater, progressManager,  true, true);
		viewer.setContentProvider(provider);
		viewer.setInput(progressManager);

		selectionListener = event -> {
			if (selectionService != null)
				selectionService.setSelection(event.getSelection());
		};
		viewer.addSelectionChangedListener(selectionListener);
	}

	@Focus
	public void setFocus() {
		if (viewer != null) {
			viewer.setFocus();
		}
	}
}
