/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
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
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.WorkbenchMarkerResolution;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * QuickFixPage is a page for the quick fixes of a marker.
 * 
 * @since 3.4
 * 
 */
public class QuickFixPage extends WizardPage {

	private Map resolutions;

	private TableViewer resolutionsList;
	private CheckboxTableViewer markersTable;
	private IWorkbenchPartSite site;
	private final IMarker[] selectedMarkers;


	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param problemDescription the description of the problem being fixed
	 * @param selectedMarkers the selected markers
	 * @param resolutions {@link Map} with key of {@link IMarkerResolution} and value of
	 *            {@link Collection} of {@link IMarker}
	 * @param site The IWorkbenchPartSite to show markers
	 */
	public QuickFixPage(String problemDescription, IMarker[] selectedMarkers, Map resolutions,
			IWorkbenchPartSite site) {
		super(problemDescription);
		this.selectedMarkers= selectedMarkers;
		this.resolutions = resolutions;
		this.site = site;
		setTitle(MarkerMessages.resolveMarkerAction_dialogTitle);
		setMessage(problemDescription);
	}

	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		// Create a new composite as there is the title bar seperator
		// to deal with
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(control);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(control,
				IWorkbenchHelpContextIds.PROBLEMS_VIEW);

		FormLayout layout = new FormLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.spacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		control.setLayout(layout);

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel
				.setText(MarkerMessages.MarkerResolutionDialog_Resolutions_List_Title);

		resolutionsLabel.setLayoutData(new FormData());

		createResolutionsList(control);

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

		createMarkerTable(control);

		Composite buttons = createTableButtons(control);
		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(title, 0);
		buttonData.right = new FormAttachment(100);
		buttonData.height = convertHeightInCharsToPixels(10);
		buttons.setLayoutData(buttonData);

		FormData tableData = new FormData();
		tableData.top = new FormAttachment(buttons, 0, SWT.TOP);
		tableData.left = new FormAttachment(0);
		tableData.bottom = new FormAttachment(100);
		tableData.right = new FormAttachment(buttons, 0);
		tableData.height = convertHeightInCharsToPixels(10);
		markersTable.getControl().setLayoutData(tableData);

		Dialog.applyDialogFont(control);

		resolutionsList.setSelection(new StructuredSelection(resolutionsList
				.getElementAt(0)));

		markersTable.setCheckedElements(selectedMarkers);
		
		setPageComplete(markersTable.getCheckedElements().length > 0);
	}

	/**
	 * Create the table buttons for the receiver.
	 * 
	 * @param control
	 * @return {@link Composite}
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
				setPageComplete(!resolutionsList.getSelection().isEmpty());
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
				setPageComplete(false);
			}
		});

		return buttonComposite;
	}

	/**
	 * @param control
	 */
	private void createResolutionsList(Composite control) {
		resolutionsList= new TableViewer(control, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL);
		resolutionsList.setContentProvider(new IStructuredContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return resolutions.keySet().toArray();
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
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((IMarkerResolution) element).getLabel();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 * @since 3.7
			 */
			public Image getImage(Object element) {
				return element instanceof IMarkerResolution2 ? ((IMarkerResolution2)element).getImage() : null;
			}
		});

		resolutionsList.setInput(this);

		resolutionsList.setComparator(new ViewerComparator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((IMarkerResolution) e1).getLabel().compareTo(
						((IMarkerResolution)e2).getLabel());
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
						markersTable.refresh();
						setPageComplete(markersTable.getCheckedElements().length > 0);
					}
				});
	}

	/**
	 * Create the table that shows the markers.
	 * 
	 * @param control
	 */
	private void createMarkerTable(Composite control) {
		markersTable = CheckboxTableViewer.newCheckList(control, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE);

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

				if (resolutions.containsKey(selected)) {
					return ((Collection) resolutions.get(selected)).toArray();
				}
				return MarkerSupportInternalUtilities.EMPTY_MARKER_ARRAY;
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
				IMarker marker =(IMarker) element;
				if (columnIndex == 0)
					return Util.getResourceName(marker);
				
				// Is the location override set?
				String locationString = marker.getAttribute(IMarker.LOCATION,
						MarkerSupportInternalUtilities.EMPTY_STRING);
				if (locationString.length() > 0) {
					return locationString;
				}

				// No override so use line number
				int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
				String lineNumberString=null;
				if (lineNumber < 0)
					lineNumberString = MarkerMessages.Unknown;
				else
					lineNumberString = NLS.bind(MarkerMessages.label_lineNumber,
							Integer.toString(lineNumber));

				return lineNumberString;
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
					setPageComplete(true);
				} else {
					setPageComplete(markersTable.getCheckedElements().length > 0);
				}

			}
		});

		new OpenAndLinkWithEditorHelper(markersTable) {
			
			{ setLinkWithEditor(false); }

			protected void activate(ISelection selection) {
				open(selection, true);
			}

			/** Not supported*/
			protected void linkToEditor(ISelection selection) {
			}
			
			protected void open(ISelection selection, boolean activate) {
				if (selection.isEmpty())
					return;
				IMarker marker = (IMarker) ((IStructuredSelection) selection)
						.getFirstElement();
				if (marker != null && marker.getResource() instanceof IFile) {
					try {
						IDE.openEditor(site.getPage(), marker, activate);
					} catch (PartInitException e) {
						MarkerSupportInternalUtilities.showViewError(e);
					}
				}
			}
		};
		markersTable.setInput(this);
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
	 * Return the marker being edited.
	 * 
	 * @return IMarker or <code>null</code>
	 */
	public IMarker getSelectedMarker() {
		ISelection selection = markersTable.getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection struct = (IStructuredSelection) selection;
			if (struct.size() == 1)
				return (IMarker) struct.getFirstElement();
		}
		return null;
	}

	/**
	 * Finish has been pressed. Process the resolutions. monitor the monitor to
	 * report to.
	 */
	/**
	 * @param monitor
	 */
	/**
	 * @param monitor
	 */
	void performFinish(IProgressMonitor monitor) {

		final IMarkerResolution resolution = getSelectedResolution();
		if (resolution == null)
			return;

		final Object[] checked = markersTable.getCheckedElements();
		if (checked.length == 0)
			return;

		if (resolution instanceof WorkbenchMarkerResolution) {

			try {
				getWizard().getContainer().run(false, true,
						new IRunnableWithProgress() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
							 */
							public void run(IProgressMonitor monitor) {
								IMarker[] markers = new IMarker[checked.length];
								System.arraycopy(checked, 0, markers, 0,
										checked.length);
								((WorkbenchMarkerResolution) resolution).run(
										markers, monitor);
							}

						});
			} catch (InvocationTargetException e) {
				StatusManager.getManager().handle(
						MarkerSupportInternalUtilities.errorFor(e));
			} catch (InterruptedException e) {
				StatusManager.getManager().handle(
						MarkerSupportInternalUtilities.errorFor(e));
			}

		} else {

			try {
				getWizard().getContainer().run(false, true,
						new IRunnableWithProgress() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
							 */
							public void run(IProgressMonitor monitor) {
								monitor
										.beginTask(
												MarkerMessages.MarkerResolutionDialog_Fixing,
												checked.length);
								for (int i = 0; i < checked.length; i++) {
									// Allow paint events and wake up the button
									getShell().getDisplay().readAndDispatch();
									if (monitor.isCanceled())
										return;
									IMarker marker = (IMarker) checked[i];
									monitor.subTask(Util.getProperty(
											IMarker.MESSAGE, marker));
									resolution.run(marker);
									monitor.worked(1);
								}
							}

						});
			} catch (InvocationTargetException e) {
				StatusManager.getManager().handle(
						MarkerSupportInternalUtilities.errorFor(e));
			} catch (InterruptedException e) {
				StatusManager.getManager().handle(
						MarkerSupportInternalUtilities.errorFor(e));
			}

		}

	}

	/**
	 * Return the marker resolution that is currently selected/
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

}
