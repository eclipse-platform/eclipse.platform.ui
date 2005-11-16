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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;

/**
 * The MarkerResolutionDialog is the dialog used to select a marker resolution.
 * 
 * @since 3.2
 * 
 */
public class MarkerResolutionDialog extends TitleAreaDialog {

	private Collection markers = new ArrayList();

	private IMarkerResolution[] resolutions;

	private CheckboxTableViewer markersTable;

	private ListViewer resolutionsList;

	private ProgressMonitorPart progressPart;

	/**
	 * Create a new instance of the receiver with the given resolutions.
	 * 
	 * @param shell
	 * @param marker the marker to show
	 * @param newResolutions
	 */
	public MarkerResolutionDialog(Shell shell,IMarker marker,
			IMarkerResolution[] newResolutions) {
		super(shell);
		markers.add(marker);
		resolutions = newResolutions;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MarkerMessages.resolveMarkerAction_dialogTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite) super.createDialogArea(parent);

		control.setLayout(new GridLayout());
		initializeDialogUnits(control);

		Label title = new Label(control, SWT.NONE);
		title.setText(MarkerMessages.MarkerResolutionPage_Problems_List_Title);

		markersTable = CheckboxTableViewer.newCheckList(control, SWT.BORDER
				| SWT.V_SCROLL);
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableData.heightHint = convertHeightInCharsToPixels(10);
		markersTable.getControl().setLayoutData(tableData);

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionPage_Resolutions_List_Title);

		resolutionsList = new ListViewer(control, SWT.BORDER | SWT.SINGLE);
		GridData listData = new GridData(SWT.FILL, SWT.NONE, true, false);
		listData.heightHint = convertHeightInCharsToPixels(10);
		resolutionsList.getControl().setLayoutData(listData);

		markersTable.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return markers.toArray();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}
		});

		markersTable.setLabelProvider(new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return Util.getProperty(IMarker.MESSAGE, ((IMarker) element));
			}

			public Image getImage(Object element) {
				return Util.getImage(((IMarker) element).getAttribute(
						IMarker.SEVERITY, -1));
			}
		});

		markersTable.setInput(this);

		resolutionsList.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return resolutions;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}
		});

		resolutionsList.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((IMarkerResolution) element).getLabel();
			}
		});

		resolutionsList
				.addSelectionChangedListener(new ISelectionChangedListener() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
					 */
					public void selectionChanged(SelectionChangedEvent event) {
						setComplete(!event.getSelection().isEmpty());

					}
				});

		resolutionsList.setInput(this);
		
		progressPart = new ProgressMonitorPart(control,new GridLayout());
		progressPart.setLayoutData(new GridData(SWT.FILL,SWT.NONE,true,false));

		Dialog.applyDialogFont(control);

		markersTable.setAllChecked(true);
		setMessage(MarkerMessages.MarkerResolutionPage_Description);
		return control;

	}

	/**
	 * Set the dialog to be complete.
	 * 
	 * @param complete
	 */
	protected void setComplete(boolean complete) {
		getButton(IDialogConstants.OK_ID).setEnabled(complete);

	}

	/**
	 * Return all of the resolutions to choose from in the receiver.
	 * 
	 * @return IMarkerResolution[]
	 */
	public IMarkerResolution[] getResolutions() {
		return resolutions;
	}

	/**
	 * Return the choice whose label matches allChoices.
	 * 
	 * @param resolution
	 * @param allChoices
	 * @return IMarkerResolution or <code>null</code> if it cannot be found
	 */
	private IMarkerResolution getResolutionMatching(
			IMarkerResolution resolution, IMarkerResolution[] allChoices) {
		Comparator resolutionComparator = MarkerResolutionWizard
				.getResolutionComparator();
		for (int i = 0; i < allChoices.length; i++) {
			if (resolutionComparator.compare(allChoices[i], resolution) == 0)
				return allChoices[i];
		}
		return null;
	}

	/**
	 * Return the description of the element.
	 * 
	 * @param element
	 * @return String
	 */
	String getDescription(IMarker element) {
		return Util.getProperty(IMarker.MESSAGE, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	public void create() {
		super.create();
		setTitle(MarkerMessages.MarkerResolutionPage_Title);
		// If there is only one select it
		if (resolutionsList.getList().getItemCount() == 1) {
			resolutionsList.getList().select(0);
			setComplete(true);
		} else
			setComplete(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		ISelection selected = resolutionsList.getSelection();
		if (!(selected instanceof IStructuredSelection))
			return;

		Object[] checked = markersTable.getCheckedElements();
		IMarkerResolution resolution = (IMarkerResolution) ((IStructuredSelection) selected)
				.getFirstElement();
		progressPart.beginTask(NLS.bind(
				MarkerMessages.MarkerResolutionPage_Fixing, resolution
						.getLabel()), checked.length + 1);
		progressPart.worked(1);

		for (int i = 0; i < checked.length; i++) {
			IMarker marker = (IMarker) checked[i];
			IMarkerResolution[] newResolutions = IDE
					.getMarkerHelpRegistry().getResolutions(marker);

			if (newResolutions.length == 0) {
				MessageDialog
						.openInformation(
								getShell(),
								MarkerMessages.MarkerResolutionPage_CannotFixTitle,
								NLS
										.bind(
												MarkerMessages.MarkerResolutionPage_NoResolutionsMessage,
												getDescription(marker)));
				return;
			}

			IMarkerResolution matching = getResolutionMatching(
					resolution, newResolutions);
			if (matching == null) {
				MessageDialog
						.openInformation(
								getShell(),
								MarkerMessages.MarkerResolutionPage_CannotFixTitle,
								NLS
										.bind(
												MarkerMessages.MarkerResolutionPage_NoMatchMessage,
												getDescription(marker)));
				return;
			}
			matching.run(marker);
			progressPart.worked(1);

		}
		progressPart.done();
		super.okPressed();	
	}

}
