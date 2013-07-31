/*******************************************************************************
 * Copyright (c) 2012, 2013 Sebastian Schmidt and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Schmidt - initial API and implementation
 *     IBM Corporation - bug fixing
 *******************************************************************************/
package org.eclipse.debug.internal.ui.importexport.breakpoints;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.core.BreakpointManager;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsViewer;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.ImportBreakpointsOperation;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8
 * @noextend This class is not intended to be subclassed by clients.
 */
public class WizardImportBreakpointsSelectionPage extends WizardPage {

	private EmbeddedBreakpointsViewer fTView;
	private boolean fIsVisible;

	protected WizardImportBreakpointsSelectionPage(String pageName) {
		super(pageName, ImportExportMessages.WizardImportBreakpointsSelectionPage_2, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		setDescription(ImportExportMessages.WizardImportBreakpointsSelectionPage_1);
		Composite composite = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		SWTFactory.createLabel(composite, ImportExportMessages.WizardExportBreakpointsPage_2, 1);
		BreakpointManager breakpointManager = new BreakpointManager();
		fTView = new EmbeddedBreakpointsViewer(composite, breakpointManager, null);
		BreakpointsViewer viewer = fTView.getViewer();
		viewer.setLabelProvider(new DecoratingLabelProvider((ILabelProvider) viewer.getLabelProvider(), new BreakpointsPathDecorator()));
		setControl(composite);
	}

	public List<IMarker> getSelectedMarkers() {
		if(!fIsVisible) {
			return null;
		}
		List<IMarker> markers = new ArrayList<IMarker>();
		List<IBreakpoint> breakpoints = fTView.getCheckedElements().toList();
		for(int i = 0; i < breakpoints.size(); i++) {
			markers.add(breakpoints.get(i).getMarker());
		}
		return markers;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			fIsVisible = true;
			try {
				updateBreakpointsPreviewList(fTView);
			} catch (Exception e) {
				setErrorMessage(e.getMessage());
			}
		} else {
			fIsVisible = false;
		}

		super.setVisible(visible);
	}

	private void updateBreakpointsPreviewList(final EmbeddedBreakpointsViewer currentTView) throws InvocationTargetException, InterruptedException {
		getContainer().run(false, true, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				WizardImportBreakpointsPage mainPage = (WizardImportBreakpointsPage) getWizard().getPage(
						ImportExportMessages.WizardImportBreakpoints_0);
				ImportBreakpointsOperation operation = new ImportBreakpointsOperation(mainPage.getFileNameField().getText()
						.trim(), mainPage.getAutoRemoveDuplicates(), false, false);
				operation.run(monitor);
				BreakpointContainer breakpointManager = new BreakpointContainer(null, null);
				IBreakpoint[] importedBreakpoints = operation.getImportedBreakpoints();
				for(int i = 0; i < importedBreakpoints.length; i++) {
					breakpointManager.addBreakpoint(importedBreakpoints[i], new ModelDelta(null, IModelDelta.ADDED));
				}
				currentTView.getViewer().setInput(breakpointManager);
				currentTView.getViewer().refresh();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	@Override
	public Image getImage() {
		return DebugUITools.getImage(IInternalDebugUIConstants.IMG_WIZBAN_IMPORT_BREAKPOINTS);
	}
}
