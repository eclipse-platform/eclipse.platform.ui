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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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

	private MarkerView markerViewer;

	/**
	 * Create a new instance of the receiver with the given resolutions.
	 * 
	 * @param shell
	 * @param marker
	 *            the marker to show
	 * @param newResolutions
	 * @param viewer
	 *            the viewer that is showing these errors
	 */
	public MarkerResolutionDialog(Shell shell, IMarker marker,
			IMarkerResolution[] newResolutions, MarkerView viewer) {
		super(shell);
		markers.add(marker);
		resolutions = newResolutions;
		markerViewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		Composite mainArea = (Composite) super.createDialogArea(parent);
		
		//Create a new composite as there is the title bar seperator
		//to deal with
		Composite control = new Composite(mainArea,SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		FormLayout layout = new FormLayout();
		layout.marginLeft = IDialogConstants.BUTTON_MARGIN;
		layout.marginTop = IDialogConstants.BUTTON_MARGIN ;
		layout.marginRight = IDialogConstants.BUTTON_MARGIN ;
		layout.marginBottom = IDialogConstants.BUTTON_MARGIN ;
		layout.spacing = IDialogConstants.BUTTON_MARGIN;
		control.setLayout(layout);

		initializeDialogUnits(control);

		Label title = new Label(control, SWT.NONE);
		title.setText(MarkerMessages.MarkerResolutionPage_Problems_List_Title);
		FormData labelData = new FormData();
		labelData.top = new FormAttachment(0);
		labelData.left = new FormAttachment(0);
		title.setLayoutData(labelData);
		
		Composite buttons = createTableButtons(control);
		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(title,0);
		buttonData.right = new FormAttachment(100);
		buttonData.height = convertHeightInCharsToPixels(10);
		buttons.setLayoutData(buttonData);
		
		createMarkerTable(control);
		
		FormData tableData = new FormData();
		tableData.top = new FormAttachment(buttons,0,SWT.TOP);
		tableData.left = new FormAttachment(0);
		tableData.right = new FormAttachment(buttons,0);
		tableData.height = convertHeightInCharsToPixels(10);
		markersTable.getControl().setLayoutData(tableData);
		
		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionPage_Resolutions_List_Title);

		FormData resolutionsLabelData = new FormData();
		resolutionsLabelData.top = new FormAttachment(markersTable.getControl(),0);
		resolutionsLabelData.left = new FormAttachment(0);
		resolutionsLabel.setLayoutData(resolutionsLabelData);		
		
		resolutionsList = new ListViewer(control, SWT.BORDER | SWT.SINGLE);
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
		FormData listData = new FormData();
		listData.top = new FormAttachment(resolutionsLabel,0);
		listData.left = new FormAttachment(0);
		listData.right = new FormAttachment(100,0);
		listData.height = convertHeightInCharsToPixels(10);
		resolutionsList.getControl().setLayoutData(listData);

		progressPart = new ProgressMonitorPart(control, new GridLayout());
		
		FormData progressData = new FormData();
		progressData.top = new FormAttachment(resolutionsList.getControl(),0);
		progressData.left = new FormAttachment(0);
		progressData.right = new FormAttachment(100,0);
		progressPart
				.setLayoutData(progressData);

		Dialog.applyDialogFont(control);
		markerViewer.getTree();

		setMessage(MarkerMessages.MarkerResolutionPage_Description);
		return mainArea;

	}

	/**
	 * Create the buttons for the table.
	 * @param control
	 * @return Composite
	 */
	private Composite createTableButtons(Composite control) {
		
		Composite buttonComposite = new Composite(control,SWT.NONE);
		buttonComposite.setLayout(new GridLayout());
		
		Button selectAll = new Button(buttonComposite,SWT.PUSH);
		selectAll.setText(MarkerMessages.selectAllAction_title);
		selectAll.setLayoutData(new GridData(SWT.FILL,SWT.NONE,false,false));
		
		selectAll.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {
				markersTable.setAllChecked(true);
			}
		});
		
		Button deselectAll = new Button(buttonComposite,SWT.PUSH);
		deselectAll.setText(MarkerMessages.filtersDialog_deselectAll);
		deselectAll.setLayoutData(new GridData(SWT.FILL,SWT.NONE,false,false));
		
		deselectAll.addSelectionListener(new SelectionAdapter(){
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {
				markersTable.setAllChecked(false);
			}
		});
		
		Button addMatching = new Button(buttonComposite,SWT.PUSH);
		addMatching.setText(MarkerMessages.MarkerResolutionDialog_AddOthers);
		addMatching.setLayoutData(new GridData(SWT.FILL,SWT.NONE,false,false));
		addMatching.setEnabled(false);
		
		return buttonComposite;
	}

	/**
	 * Create the table for the markers/
	 * @param control
	 */
	private void createMarkerTable(Composite control) {
		markersTable = CheckboxTableViewer.newCheckList(control, SWT.BORDER
				| SWT.V_SCROLL);


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
		markersTable.setAllChecked(true);
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

	/*
	 * (non-Javadoc)
	 * 
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
			IMarkerResolution[] newResolutions = IDE.getMarkerHelpRegistry()
					.getResolutions(marker);

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

			IMarkerResolution matching = getResolutionMatching(resolution,
					newResolutions);
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
