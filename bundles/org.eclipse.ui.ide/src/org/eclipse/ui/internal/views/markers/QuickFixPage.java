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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.e4.ui.internal.workspace.markers.Translation;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionRelevance;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.Util;

/**
 * QuickFixPage is a page for the quick fixes of a marker.
 *
 * @since 3.4
 */
public class QuickFixPage extends WizardPage {

	private Map<IMarkerResolution, Collection<IMarker>> resolutions;

	private TableViewer resolutionsList;
	private CheckboxTableViewer markersTable;
	private IMarker[] selectedMarkers;
	private final Consumer<StructuredViewer> showMarkers;
	private final Consumer<Control> bindHelp;


	/**
	 * Create a new instance of the receiver.
	 *
	 * @param problemDescription the description of the problem being fixed
	 * @param selectedMarkers    the selected markers
	 * @param resolutions        {@link Map} with key of {@link IMarkerResolution}
	 *                           and value of {@link Collection} of {@link IMarker}
	 * @param showMarkers        the consumer to show markers
	 * @param bindHelp           the consumer to bind help system
	 */
	public QuickFixPage(String problemDescription, IMarker[] selectedMarkers, Map<IMarkerResolution, Collection<IMarker>> resolutions,
			Consumer<StructuredViewer> showMarkers, Consumer<Control> bindHelp) {
		super(problemDescription);
		this.selectedMarkers= selectedMarkers;
		this.resolutions = resolutions;
		this.showMarkers = showMarkers;
		this.bindHelp = bindHelp;
		setTitle(MarkerMessages.resolveMarkerAction_dialogTitle);
		setMessage(problemDescription);
	}

	@Override
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		// Create a new composite as there is the title bar seperator
		// to deal with
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(control);
		bindHelp.accept(control);

		FormLayout layout = new FormLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.spacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		control.setLayout(layout);

		Label resolutionsLabel = new Label(control, SWT.NONE);
		resolutionsLabel.setText(MarkerMessages.MarkerResolutionDialog_Resolutions_List_Title);
		resolutionsLabel.setLayoutData(new FormData());

		createResolutionsList(control);

		FormData listData = new FormData();
		listData.top = new FormAttachment(resolutionsLabel, 0);
		listData.left = new FormAttachment(0);
		listData.right = new FormAttachment(100, 0);
		listData.height = convertHeightInCharsToPixels(10);
		resolutionsList.getControl().setLayoutData(listData);

		Label title = new Label(control, SWT.NONE);
		title.setText(MarkerMessages.MarkerResolutionDialog_Problems_List_Title);
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

		resolutionsList.setSelection(new StructuredSelection(resolutionsList.getElementAt(0)));

		markersTable.setCheckedElements(selectedMarkers);

		setPageComplete(markersTable.getCheckedElements().length > 0);
	}

	/**
	 * Create the table buttons for the receiver.
	 *
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
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				markersTable.setAllChecked(true);
				setPageComplete(!resolutionsList.getStructuredSelection().isEmpty());
			}
		});

		Button deselectAll = new Button(buttonComposite, SWT.PUSH);
		deselectAll.setText(MarkerMessages.filtersDialog_deselectAll);
		deselectAll
				.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

		deselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				markersTable.setAllChecked(false);
				setPageComplete(false);
			}
		});

		return buttonComposite;
	}

	private void createResolutionsList(Composite control) {
		resolutionsList = new TableViewer(control, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		resolutionsList.setContentProvider(ArrayContentProvider.getInstance());

		resolutionsList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IMarkerResolution) element).getLabel();
			}

			@Override
			public Image getImage(Object element) {
				return element instanceof IMarkerResolution2 ? ((IMarkerResolution2)element).getImage() : null;
			}
		});

		resolutionsList.setInput(resolutions.keySet().toArray());

		resolutionsList.setComparator(new ViewerComparator() {
			/**
			 * This comparator compares the resolutions based on the relevance of the
			 * resolutions. Any resolution that doesn't implement IMarkerResolutionRelevance
			 * will be deemed to have relevance 0 (default value for relevance). If both
			 * resolutions have the same relevance, then marker resolution label string will
			 * be used for comparing the resolutions.
			 *
			 * @see IMarkerResolutionRelevance#getRelevanceForResolution()
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				int relevanceMarker1 = getRelevance(e1);
				int relevanceMarker2 = getRelevance(e2);
				int c = Integer.compare(relevanceMarker2, relevanceMarker1);
				if (c != 0) {
					return c;
				}
				return getLabel(e1).compareTo(getLabel(e2));
			}

			private static String getLabel(Object object) {
				if (object instanceof IMarkerResolution resolution) {
					return resolution.getLabel();
				}
				return ""; //$NON-NLS-1$
			}

			private static int getRelevance(Object object) {
				if (object instanceof IMarkerResolutionRelevance relevance) {
					return relevance.getRelevanceForResolution();
				}
				return 0;
			}
		});

		resolutionsList
				.addSelectionChangedListener(event -> {
					markersTable.refresh();
					setPageComplete(markersTable.getCheckedElements().length > 0);
				});
	}

	/**
	 * Create the table that shows the markers.
	 */
	private void createMarkerTable(Composite control) {
		markersTable = CheckboxTableViewer.newCheckList(control, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);

		createTableColumns();

		markersTable.setContentProvider(new IStructuredContentProvider() {

			private final IMarker[] noMarkers = new IMarker[0];

			@Override
			public void dispose() {

			}

			@Override
			public Object[] getElements(Object inputElement) {
				Optional<IMarkerResolution> selected = getSelectedMarkerResolution();
				if (selected.isPresent()) {
					IMarkerResolution resolution = selected.get();
					if (resolutions.containsKey(resolution)) {
						return resolutions.get(resolution).toArray();
					}
				}
				return noMarkers;
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

			}
		});

		markersTable.setLabelProvider(new ITableLabelProvider() {

			private final Translation translation = new Translation();

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0)
					return Util.getImage(((IMarker) element).getAttribute(IMarker.SEVERITY, -1));
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				IMarker marker =(IMarker) element;
				if (columnIndex == 0) {
					return translation.name(marker).orElse(""); //$NON-NLS-1$
				}
				// Is the location override set?
				String locationString = marker.getAttribute(IMarker.LOCATION, MarkerItemDefaults.LOCATION_DEFAULT);
				if (!MarkerItemDefaults.LOCATION_DEFAULT.equals(locationString)) {
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

			@Override
			public void addListener(ILabelProviderListener listener) {
				// do nothing
			}

			@Override
			public void dispose() {
				// do nothing
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
				// do nothing
			}
		});

		markersTable.addCheckStateListener(event -> {
			if (event.getChecked()) {
				setPageComplete(true);
			} else {
				setPageComplete(markersTable.getCheckedElements().length > 0);
			}

		});
		showMarkers.accept(markersTable);
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
		tc.setText(MarkerMessages.MarkerResolutionDialog_Problems_List_Location);
		layout.addColumnData(new ColumnWeightData(30, true));
		tc = new TableColumn(table, SWT.NONE, 0);
		tc.setText(MarkerMessages.MarkerResolutionDialog_Problems_List_Resource);

	}

	/**
	 * Return the marker being edited.
	 *
	 * @return IMarker or <code>null</code>
	 */
	public IMarker getSelectedMarker() {
		IStructuredSelection selection = markersTable.getStructuredSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection struct = selection;
			if (struct.size() == 1)
				return (IMarker) struct.getFirstElement();
		}
		return null;
	}

	/**
	 * Returns the selected marker resolution.
	 *
	 * @return <code>java.util.Optional&lt;IMarkerResolution&gt;</code> or
	 *         {@link Optional#empty()}
	 */
	Optional<IMarkerResolution> getSelectedMarkerResolution() {
		return Optional.ofNullable((IMarkerResolution) resolutionsList.getStructuredSelection().getFirstElement());
	}

	/**
	 * Returns an array of {@link IMarker} checked in the viewer
	 *
	 * @return an array of checked markers
	 */
	IMarker[] getCheckedMarkers() {
		Object[] checked = markersTable.getCheckedElements();
		IMarker[] markers = new IMarker[checked.length];
		System.arraycopy(checked, 0, markers, 0, checked.length);
		return markers;
	}

}
