/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
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

	private ViewerComparator resolutionsComparator;

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
		resolutionsComparator.sort(view.getViewer(), newResolutions);
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

		initializeDialogUnits(parent);

		setTitleImage(JFaceResources
				.getResources()
				.createImageWithDefault(
						IDEInternalWorkbenchImages
								.getImageDescriptor(IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG)));

		Composite mainArea = (Composite) super.createDialogArea(parent);

		// Create a new composite as there is the title bar seperator
		// to deal with
		Composite control = new Composite(mainArea, SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		FormLayout layout = new FormLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.spacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		control.setLayout(layout);

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionDialog_Resolutions_List_Title);

		resolutionsLabel.setLayoutData(new FormData());

		resolutionsList = new ListViewer(control, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL);
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

						WorkbenchMarkerResolution resolution = getSelectedWorkbenchResolution();
						if (resolution == null
								|| markerMap.containsKey(resolution))
							addMatching.setEnabled(false);
						else
							addMatching.setEnabled(true);
						markersTable.refresh();
					}
				});

		resolutionsList.setInput(this);

		resolutionsList.setComparator(resolutionsComparator);

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

		String message = NLS.bind(
				MarkerMessages.MarkerResolutionDialog_Description, Util
						.getProperty(IMarker.MESSAGE, originalMarker));
		if (message.length() > 50) {
			// Add a carriage return in the middle if we can
			int insertionIndex = chooseWhitespace(message);
			if (insertionIndex > 0) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(message.substring(0, insertionIndex));
				buffer.append("\n"); //$NON-NLS-1$
				buffer.append(message.substring(insertionIndex, message
						.length()));
				message = buffer.toString();
			}
		}

		setMessage(message);
		return mainArea;

	}

	/**
	 * Choose a good whitespace position for a page break. Start in the middle
	 * of the message.
	 * 
	 * @param message
	 * @return int -1 if there is no whitespace to choose.
	 */
	private int chooseWhitespace(String message) {

		for (int i = message.length() / 2; i < message.length(); i++) {
			if (Character.isWhitespace(message.charAt(i)))
				return i;
		}
		return -1;
	}

	/**
	 * Create the resolutions sorter.
	 */
	private void initializeResolutionsSorter() {
		resolutionsComparator = new ViewerComparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
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
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		buttonComposite.setLayout(layout);

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
		addMatching.setEnabled(true);
		addMatching.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent arg0) {

				WorkbenchMarkerResolution selected = getSelectedWorkbenchResolution();
				if (selected == null) {
					return;
				}

				if (addMatchingMarkers(selected)) {
					addMatching.setEnabled(false);
				}
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
				|| !(selection instanceof WorkbenchMarkerResolution)) {
			return null;
		}
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
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

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
	 * Create the table for the markers/
	 * 
	 * @param control
	 */
	private void createMarkerTable(Composite control) {
		markersTable = CheckboxTableViewer.newCheckList(control, SWT.BORDER
				| SWT.V_SCROLL);

		createTableColumns();

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
				if (selected == null) {
					return new Object[0];
				}

				if (markerMap.containsKey(selected)) {
					return ((Collection) markerMap.get(selected)).toArray();
				}
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

		markersTable.setLabelProvider(new ITableLabelProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
			 *      int)
			 */
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0)
					return Util.getImage(((IMarker) element).getAttribute(
							IMarker.SEVERITY, -1));
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
			 *      int)
			 */
			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 0)
					return Util.getResourceName((IMarker) element);
				int line = ((IMarker) element).getAttribute(
						IMarker.LINE_NUMBER, -1);
				if (line < 0) {
					return MarkerMessages.Unknown;
				}
				return NLS.bind(MarkerMessages.label_lineNumber, Integer
						.toString(line));
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void addListener(ILabelProviderListener listener) {
				// do nothing

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
			 */
			public void dispose() {
				// do nothing

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
			 *      java.lang.String)
			 */
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
			 */
			public void removeListener(ILabelProviderListener listener) {
				// do nothing

			}
		});

		markersTable.addCheckStateListener(new ICheckStateListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
			 */
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked() == true) {
					setComplete(true);
				} else {
					setComplete(markersTable.getCheckedElements().length > 0);
				}

			}
		});

		markersTable.setInput(this);
		markersTable.setAllChecked(true);
	}

	/**
	 * Create the table columns for the receiver.
	 */
	private void createTableColumns() {
		TableLayout layout = new TableLayout();

		Table table = markersTable.getTable();
		table.setLayout(layout);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		layout.addColumnData(new ColumnWeightData(70, true));
		TableColumn tc = new TableColumn(table, SWT.NONE, 0);
		tc
				.setText(MarkerMessages.MarkerResolutionDialog_Problems_List_Location);
		layout.addColumnData(new ColumnWeightData(30, true));
		tc = new TableColumn(table, SWT.NONE, 0);
		tc
				.setText(MarkerMessages.MarkerResolutionDialog_Problems_List_Resource);

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
		resolutionsList.getList().select(0);
		markersTable.refresh();
		markersTable.setAllChecked(true);
		setComplete(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		IMarkerResolution resolution = getSelectedResolution();
		if (resolution == null) {
			return;
		}

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
			if (!progressCancelled() && checked.length == 1) {

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
	
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    protected boolean isResizable() {
    	return true;
    }
}
