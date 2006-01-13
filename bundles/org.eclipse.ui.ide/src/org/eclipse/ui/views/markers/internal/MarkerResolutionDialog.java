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
import java.util.Hashtable;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;

/**
 * The MarkerResolutionDialog is the dialog used to select a marker resolution.
 * 
 * @since 3.2
 * 
 */
public class MarkerResolutionDialog extends TitleAreaDialog {

	private IMarker originalMarker;

	private IMarkerResolution[] resolutions;

	private CheckboxTableViewer markersTable;

	private ListViewer resolutionsList;

	private ProgressMonitorPart progressPart;

	private MarkerView markerView;

	private ViewerSorter resolutionsSorter;

	private boolean calculatingResolutions;

	private boolean progressCancelled = false;

	private Button addMatching;

	private Hashtable markerMap = new Hashtable(0);

	/**
	 * Create a new instance of the receiver with the given resolutions.
	 * 
	 * @param shell
	 * @param marker
	 *            the marker to show
	 * @param newResolutions
	 * @param view
	 *            the viewer that is showing these errors
	 */
	public MarkerResolutionDialog(Shell shell, IMarker marker,
			IMarkerResolution[] newResolutions, MarkerView view) {
		super(shell);
		initializeResolutionsSorter();
		resolutionsSorter.sort(view.getViewer(), newResolutions);
		resolutions = newResolutions;
		originalMarker = marker;
		markerView = view;
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

		// Create a new composite as there is the title bar seperator
		// to deal with
		Composite control = new Composite(mainArea, SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		FormLayout layout = new FormLayout();
		layout.marginLeft = IDialogConstants.BUTTON_MARGIN;
		layout.marginTop = IDialogConstants.BUTTON_MARGIN;
		layout.marginRight = IDialogConstants.BUTTON_MARGIN;
		layout.marginBottom = IDialogConstants.BUTTON_MARGIN;
		layout.spacing = IDialogConstants.BUTTON_MARGIN;
		control.setLayout(layout);

		initializeDialogUnits(control);

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionDialog_Resolutions_List_Title);

		FormData resolutionsLabelData = new FormData();
		resolutionsLabelData.top = new FormAttachment(0);
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
						addMatching
								.setEnabled(getSelectedWorkbenchResolution() != null);
						markersTable.refresh();
					}
				});

		resolutionsList.setInput(this);

		resolutionsList.setSorter(resolutionsSorter);

		FormData listData = new FormData();
		listData.top = new FormAttachment(resolutionsLabel, 0);
		listData.left = new FormAttachment(0);
		listData.right = new FormAttachment(100, 0);
		listData.height = convertHeightInCharsToPixels(10);
		resolutionsList.getControl().setLayoutData(listData);

		Label title = new Label(control, SWT.NONE);
		title
				.setText(MarkerMessages.MarkerResolutionDialog_Problems_List_Title);
		FormData labelData = new FormData();
		labelData.top = new FormAttachment(resolutionsList.getControl(), 0);
		labelData.left = new FormAttachment(0);
		title.setLayoutData(labelData);

		Composite buttons = createTableButtons(control);
		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(title, 0);
		buttonData.right = new FormAttachment(100);
		buttonData.height = convertHeightInCharsToPixels(10);
		buttons.setLayoutData(buttonData);

		createMarkerTable(control);

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(buttons, 0, SWT.TOP);
		tableData.left = new FormAttachment(0);
		tableData.right = new FormAttachment(buttons, 0);
		tableData.height = convertHeightInCharsToPixels(10);
		markersTable.getControl().setLayoutData(tableData);

		progressPart = new ProgressMonitorPart(control, new GridLayout());

		FormData progressData = new FormData();
		progressData.top = new FormAttachment(markersTable.getControl(), 0);
		progressData.left = new FormAttachment(0);
		progressData.right = new FormAttachment(100, 0);
		progressPart.setLayoutData(progressData);

		Dialog.applyDialogFont(control);
		markerView.getTree();

		setMessage(NLS.bind(MarkerMessages.MarkerResolutionDialog_Description,
				Util.getProperty(IMarker.MESSAGE, originalMarker)));
		return mainArea;

	}

	/**
	 * Create the resolutions sorter.
	 */
	private void initializeResolutionsSorter() {
		resolutionsSorter = new ViewerSorter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((IMarkerResolution) e1).getLabel().compareTo(
						((IMarkerResolution) e1).getLabel());
			}
		};
	}

	/**
	 * Create the buttons for the table.
	 * 
	 * @param control
	 * @return Composite
	 */
	private Composite createTableButtons(Composite control) {

		Composite buttonComposite = new Composite(control, SWT.NONE);
		buttonComposite.setLayout(new GridLayout());

		Button selectAll = new Button(buttonComposite, SWT.PUSH);
		selectAll.setText(MarkerMessages.selectAllAction_title);
		selectAll.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

		selectAll.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {
				markersTable.setAllChecked(true);
				setComplete(!resolutionsList.getSelection().isEmpty());
			}
		});

		Button deselectAll = new Button(buttonComposite, SWT.PUSH);
		deselectAll.setText(MarkerMessages.filtersDialog_deselectAll);
		deselectAll
				.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

		deselectAll.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {
				markersTable.setAllChecked(false);
				setComplete(false);
			}
		});

		addMatching = new Button(buttonComposite, SWT.PUSH);
		addMatching.setText(MarkerMessages.MarkerResolutionDialog_AddOthers);
		addMatching
				.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		addMatching.setEnabled(getMatchingButtonEnablement());
		addMatching.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {

				WorkbenchMarkerResolution selected = getSelectedWorkbenchResolution();
				if (selected == null)
					return;

				if (addMatchingMarkers(selected))
					addMatching.setEnabled(false);
			}
		});

		return buttonComposite;
	}

	/**
	 * Return the single selected WorkbenchMarkerResolution if there is one.
	 * 
	 * @return WorkbenchMarkerResolution or <code>null</code> if there is no
	 *         selection or the selection is not a WorkbenchMarkerResolution.
	 */
	private WorkbenchMarkerResolution getSelectedWorkbenchResolution() {
		Object selection = getSelectedResolution();
		if (selection == null
				|| !(selection instanceof WorkbenchMarkerResolution))
			return null;
		return (WorkbenchMarkerResolution) selection;

	}

	/**
	 * Return the marker resolution that is currenly selected/
	 * 
	 * @return IMarkerResolution or <code>null</code> if there is no
	 *         selection.
	 */
	private IMarkerResolution getSelectedResolution() {
		ISelection selection = resolutionsList.getSelection();
		if (!(selection instanceof IStructuredSelection))
			return null;

		Object first = ((IStructuredSelection) selection).getFirstElement();

		return (IMarkerResolution) first;

	}

	/**
	 * Add all of the markers that have resolutions compatible with the
	 * receiver.
	 * 
	 * @return boolean <code>true</code> if the operation completed.
	 */
	protected boolean addMatchingMarkers(
			final WorkbenchMarkerResolution resolution) {

		calculatingResolutions = true;
		progressPart.beginTask(
				MarkerMessages.MarkerResolutionDialog_CalculatingTask, 100);

		progressPart.worked(10);
		if (progressCancelled()) {
			calculatingResolutions = false;
			return false;
		}

		progressPart.subTask(NLS.bind(
				MarkerMessages.MarkerResolutionDialog_WorkingSubTask,
				resolution.getLabel()));

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				IMarker[] others = resolution.findOtherMarkers(markerView
						.getCurrentMarkers().getIMarkers());

				Collection currentMarkers = new ArrayList();
				currentMarkers.add(originalMarker);

				for (int i = 0; i < others.length; i++) {
					currentMarkers.add(others[i]);
				}

				markerMap.put(resolution, currentMarkers);

				progressPart.worked(90);
				progressPart.done();
				progressCancelled = false;
				calculatingResolutions = false;
				markersTable.refresh();

			}
		});

		return true;
	}

	/**
	 * Spin the event loop and see if the cancel button was pressed. If it was
	 * then clear the flags and return <code>true</code>.
	 * 
	 * @return boolean
	 */
	private boolean progressCancelled() {
		getShell().getDisplay().readAndDispatch();
		if (progressCancelled) {
			progressCancelled = false;
			calculatingResolutions = false;
			progressPart.done();
			return true;
		}
		return false;
	}

	/**
	 * Return whether or not the add button should be enabled.
	 * 
	 * @return boolean
	 */
	private boolean getMatchingButtonEnablement() {

		return getSelectedResolution() != null;
	}

	/**
	 * Create the table for the markers/
	 * 
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
				IMarkerResolution selected = getSelectedResolution();
				if (selected == null)
					return new Object[0];

				if (markerMap.containsKey(selected))
					return ((Collection) markerMap.get(selected)).toArray();
				return new IMarker[] { originalMarker };
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
				return Util.getResourceName((IMarker) element);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	public void create() {
		super.create();
		setTitle(MarkerMessages.MarkerResolutionDialog_Title);
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
		IMarkerResolution resolution = getSelectedResolution();
		if (resolution == null)
			return;

		Object[] checked = markersTable.getCheckedElements();

		progressPart.beginTask(MarkerMessages.MarkerResolutionDialog_Fixing,
				checked.length + 1);
		progressPart.worked(1);

		calculatingResolutions = true;

		if (resolution instanceof WorkbenchMarkerResolution) {

			IMarker[] markers = new IMarker[checked.length];
			System.arraycopy(checked, 0, markers, 0, checked.length);
			((WorkbenchMarkerResolution) resolution).run(markers,
					new SubProgressMonitor(progressPart, checked.length));
		} else {

			// Allow paint events and wake up the button
			getShell().getDisplay().readAndDispatch();
			if (!progressCancelled()) {

				// There will only be one
				IMarker marker = (IMarker) checked[0];

				progressPart.subTask(Util.getProperty(IMarker.MESSAGE, marker));
				resolution.run(marker);
				progressPart.worked(1);
			}

		}

		calculatingResolutions = false;
		progressPart.done();
		progressCancelled = false;
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		if (calculatingResolutions) {
			progressCancelled = true;
			progressPart.setCanceled(true);
			return;
		}
		super.cancelPressed();
	}

}
