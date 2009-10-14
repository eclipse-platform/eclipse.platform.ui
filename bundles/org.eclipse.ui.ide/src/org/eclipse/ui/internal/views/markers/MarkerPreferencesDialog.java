/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.preferences.ViewSettingsDialog;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkerPreferencesDialog is the dialog for showing marker preferences.
 * 
 * @since 3.4
 * 
 */
public class MarkerPreferencesDialog extends ViewSettingsDialog {

	private IntegerFieldEditor limitEditor;

	private Button enablementButton;

	private Composite editArea;

	private Label messageLabel;

	private ExtendedMarkersView extendedView;

	private ArrayList visible;

	private ArrayList hidden;

	private ListViewer visibleViewer;

	private ListViewer nonVisibleViewer;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param view -
	 *            the view this is being launched from
	 */
	public MarkerPreferencesDialog(ExtendedMarkersView view) {
		super(view.getSite().getShell());
		this.extendedView = view;

		Object[] visibleFields = view.getVisibleFields();
		Object[] hiddenFields = view.getHiddenFields();

		visible = new ArrayList();
		hidden = new ArrayList();

		for (int i = 0; i < visibleFields.length; i++) {
			visible.add(visibleFields[i]);
		}

		for (int i = 0; i < hiddenFields.length; i++) {
			hidden.add(hiddenFields[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(MarkerMessages.MarkerPreferences_DialogTitle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#getShellStyle()
	 */
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);

		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		enablementButton = new Button(dialogArea, SWT.CHECK);
		enablementButton.setText(MarkerMessages.MarkerPreferences_MarkerLimits);
		enablementButton.setSelection(checked);

		editArea = new Composite(dialogArea, SWT.NONE);
		editArea.setLayout(new GridLayout());
		GridData editData = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		editData.horizontalIndent = 10;
		editArea.setLayoutData(editData);

		limitEditor = new IntegerFieldEditor(
				"limit", MarkerMessages.MarkerPreferences_VisibleItems, editArea) { //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.preference.IntegerFieldEditor#checkState()
			 */
			protected boolean checkState() {
				boolean state = super.checkState();
				setValid(state, getErrorMessage());
				return state;
			}
		};
		limitEditor.setPreferenceStore(IDEWorkbenchPlugin.getDefault()
				.getPreferenceStore());
		limitEditor
				.setPreferenceName(IDEInternalPreferences.MARKER_LIMITS_VALUE);
		limitEditor.load();

		GridData checkedData = new GridData(SWT.FILL, SWT.NONE, true, false);
		checkedData.horizontalSpan = limitEditor.getNumberOfControls();
		enablementButton.setLayoutData(checkedData);

		enablementButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setLimitEditorEnablement(editArea, enablementButton
						.getSelection());
			}
		});

		setLimitEditorEnablement(editArea, checked);

		messageLabel = new Label(dialogArea, SWT.NONE);

		messageLabel.setBackground(JFaceColors.getErrorBackground(dialogArea
				.getDisplay()));
		messageLabel.setForeground(JFaceColors.getErrorText(dialogArea
				.getDisplay()));
		messageLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		createColumnsArea(dialogArea);

		applyDialogFont(dialogArea);
		return dialogArea;
	}

	/**
	 * Create an area for the selected columns
	 * 
	 * @param dialogArea
	 */
	private void createColumnsArea(Composite dialogArea) {

		initializeDialogUnits(dialogArea);
		Group columnsComposite = new Group(dialogArea, SWT.NONE);
		columnsComposite.setText(MarkerMessages.MarkerPreferences_ColumnGroupTitle);
		FormLayout layout = new FormLayout();
		columnsComposite.setLayout(layout);

		columnsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		Label visibleItemsLabel = new Label(columnsComposite, SWT.NONE);
		visibleItemsLabel.setText(MarkerMessages.MarkerPreferences_VisibleColumnsTitle);
		FormData visibleLabelData = new FormData();
		visibleLabelData.right = new FormAttachment(45, 0);
		visibleLabelData.left = new FormAttachment(
				IDialogConstants.BUTTON_MARGIN);
		visibleLabelData.top = new FormAttachment(0);
		visibleItemsLabel.setLayoutData(visibleLabelData);

		int rightMargin = IDialogConstants.BUTTON_MARGIN * -1;

		Label nonVisibleLabel = new Label(columnsComposite, SWT.NONE);
		nonVisibleLabel.setText(MarkerMessages.MarkerPreferences_HiddenColumnsTitle);
		FormData nonVisibleLabelData = new FormData();
		nonVisibleLabelData.right = new FormAttachment(100);
		nonVisibleLabelData.left = new FormAttachment(55, 0);
		nonVisibleLabelData.top = new FormAttachment(0);
		nonVisibleLabel.setLayoutData(nonVisibleLabelData);

		visibleViewer = new ListViewer(columnsComposite,
				SWT.BORDER);

		FormData visibleViewerData = new FormData();
		visibleViewerData.right = new FormAttachment(visibleItemsLabel, 0,
				SWT.RIGHT);
		visibleViewerData.left = new FormAttachment(visibleItemsLabel, 0,
				SWT.LEFT);
		visibleViewerData.top = new FormAttachment(visibleItemsLabel,
				IDialogConstants.BUTTON_MARGIN);
		visibleViewerData.bottom = new FormAttachment(100, rightMargin);
		visibleViewerData.height = convertHeightInCharsToPixels(15);
		visibleViewerData.width = convertWidthInCharsToPixels(25);

		visibleViewer.getControl().setLayoutData(visibleViewerData);

		visibleViewer.setContentProvider(new IStructuredContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return visible.toArray();
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

		visibleViewer.setLabelProvider(markerFieldLabelProvider());

		visibleViewer.setInput(this);

		nonVisibleViewer = new ListViewer(columnsComposite,
				SWT.BORDER);

		nonVisibleViewer.setLabelProvider(markerFieldLabelProvider());

		nonVisibleViewer.setContentProvider(new IStructuredContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return hidden.toArray();
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
		nonVisibleViewer.setInput(this);

		FormData nonVisibleViewerData = new FormData();
		nonVisibleViewerData.right = new FormAttachment(nonVisibleLabel, 0,
				SWT.RIGHT);
		nonVisibleViewerData.left = new FormAttachment(nonVisibleLabel, 0,
				SWT.LEFT);
		nonVisibleViewerData.top = new FormAttachment(nonVisibleLabel,
				IDialogConstants.BUTTON_MARGIN);
		nonVisibleViewerData.bottom = new FormAttachment(100, rightMargin);
		nonVisibleViewerData.height = convertHeightInCharsToPixels(15);
		nonVisibleViewerData.width = convertWidthInCharsToPixels(25);

		nonVisibleViewer.getControl().setLayoutData(nonVisibleViewerData);

		Button toNonVisibleButton = new Button(columnsComposite, SWT.PUSH);
		toNonVisibleButton
				.setText(getDefaultOrientation() == SWT.RIGHT_TO_LEFT ? MarkerMessages.MarkerPreferences_MoveLeft
						: MarkerMessages.MarkerPreferences_MoveRight);

		FormData toNonVisibleButtonData = new FormData();

		toNonVisibleButtonData.top = new FormAttachment(visibleViewer
				.getControl(), IDialogConstants.BUTTON_BAR_HEIGHT, SWT.TOP);
		toNonVisibleButtonData.left = new FormAttachment(visibleViewer
				.getControl(), IDialogConstants.BUTTON_MARGIN);
		toNonVisibleButtonData.right = new FormAttachment(nonVisibleViewer
				.getControl(), rightMargin);
		toNonVisibleButton.setLayoutData(toNonVisibleButtonData);

		toNonVisibleButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				List selection = ((IStructuredSelection) visibleViewer
						.getSelection()).toList();
				hidden.addAll(selection);
				visible.removeAll(selection);
				visibleViewer.refresh();
				nonVisibleViewer.refresh();
				setValid(
						visible.size() > 0,
						MarkerMessages.MarkerPreferences_AtLeastOneVisibleColumn);
			}
		});

		Button toVisibleButton = new Button(columnsComposite, SWT.PUSH);
		toVisibleButton
				.setText(getDefaultOrientation() == SWT.RIGHT_TO_LEFT ? MarkerMessages.MarkerPreferences_MoveRight
						: MarkerMessages.MarkerPreferences_MoveLeft);

		FormData toVisibleButtonData = new FormData();

		toVisibleButtonData.top = new FormAttachment(toNonVisibleButton,
				IDialogConstants.BUTTON_MARGIN);
		toVisibleButtonData.left = new FormAttachment(visibleViewer
				.getControl(), IDialogConstants.BUTTON_MARGIN);
		toVisibleButtonData.right = new FormAttachment(nonVisibleViewer
				.getControl(), rightMargin);
		toVisibleButton.setLayoutData(toVisibleButtonData);

		toVisibleButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				List selection = ((IStructuredSelection) nonVisibleViewer
						.getSelection()).toList();
				hidden.removeAll(selection);
				visible.addAll(selection);
				visibleViewer.refresh();
				nonVisibleViewer.refresh();
				setValid(
						visible.size() > 0,
						MarkerMessages.MarkerPreferences_AtLeastOneVisibleColumn);
			}
		});

	}

	/**
	 * Return a label provider for fields.
	 * @return LabelProvider
	 */
	private LabelProvider markerFieldLabelProvider() {
		return new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((MarkerField) element).getName();
			}
		};
	}

	/**
	 * Set the enabled state of the OK button by state.
	 * 
	 * @param state
	 */
	protected void setValid(boolean state, String errorMessage) {
		Button okButton = getButton(IDialogConstants.OK_ID);

		if (okButton == null)
			return;

		if (state)
			messageLabel.setText(MarkerSupportInternalUtilities.EMPTY_STRING);
		else
			messageLabel.setText(errorMessage);

		okButton.setEnabled(state);

	}

	/**
	 * Enable the limitEditor based on checked.
	 * 
	 * @param control
	 *            The parent of the editor
	 * @param checked
	 */
	private void setLimitEditorEnablement(Composite control, boolean checked) {
		limitEditor.setEnabled(checked, control);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		limitEditor.store();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
				IDEInternalPreferences.USE_MARKER_LIMITS,
				enablementButton.getSelection());
		IDEWorkbenchPlugin.getDefault().savePluginPreferences();

		extendedView.setVisibleFields(visible);

		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.preferences.ViewSettingsDialog#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		limitEditor.loadDefault();
		boolean checked = IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.getDefaultBoolean(IDEInternalPreferences.USE_MARKER_LIMITS);
		enablementButton.setSelection(checked);
		setLimitEditorEnablement(editArea, checked);

		Object[] visibleFields=extendedView.getBuilder().getGenerator().getInitialVisible();
		Object[] allFields=extendedView.getBuilder().getGenerator().getAllFields();
		visible.clear();
		hidden.clear();
		for (int i = 0; i < allFields.length; i++) {
			hidden.add(allFields[i]);
		}
		for (int i = 0; i < visibleFields.length; i++) {
			hidden.remove(visibleFields[i]);
			visible.add(visibleFields[i]);
		}
		visibleViewer.refresh();
		nonVisibleViewer.refresh();
	}

}
