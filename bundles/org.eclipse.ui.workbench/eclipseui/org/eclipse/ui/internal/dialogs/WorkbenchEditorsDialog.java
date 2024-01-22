/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.SaveablesList;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.layout.CellData;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.osgi.framework.FrameworkUtil;

/**
 * Implements a dialog showing all opened editors in the workbench and the
 * recent closed editors
 */
public class WorkbenchEditorsDialog extends SelectionDialog {

	private IWorkbenchWindow window;

	private Table editorsTable;

	private Button saveSelected;

	private Button closeSelected;

	private Button selectClean;

	private Button invertSelection;

	private Button allSelection;

	private boolean showAllPersp = false;

	private int sortColumn;

	private List<Adapter> elements = new ArrayList<>();

	private HashMap<ImageDescriptor, Image> disabledImageCache = new HashMap<>(11);

	private boolean reverse = false;

	private Collator collator = Collator.getInstance();

	private Rectangle bounds;

	private int columnsWidth[];

	private static final String SORT = "sort"; //$NON-NLS-1$

	private static final String ALLPERSP = "allPersp"; //$NON-NLS-1$

	private static final String BOUNDS = "bounds"; //$NON-NLS-1$

	private static final String COLUMNS = "columns"; //$NON-NLS-1$

	private SelectionListener headerListener = widgetSelectedAdapter(e -> {
		TableColumn column = (TableColumn) e.widget;
		int index = editorsTable.indexOf(column);
		if (index == sortColumn) {
			reverse = !reverse;
		} else {
			sortColumn = index;
		}
		editorsTable.setSortDirection(reverse ? SWT.DOWN : SWT.UP);
		editorsTable.setSortColumn(column);
		updateItems();
	});

	/**
	 * Constructor for WorkbenchEditorsDialog.
	 *
	 * @param window the window
	 */
	public WorkbenchEditorsDialog(IWorkbenchWindow window) {
		super(window.getShell());
		this.window = window;
		setTitle(WorkbenchMessages.WorkbenchEditorsDialog_title);

		IDialogSettings s = getDialogSettings();
		if (s.get(ALLPERSP) == null) {
			sortColumn = 0;
		} else {
			showAllPersp = s.getBoolean(ALLPERSP);
			sortColumn = s.getInt(SORT);
			String[] array = s.getArray(BOUNDS);
			if (array != null) {
				bounds = new Rectangle(0, 0, 0, 0);
				bounds.x = Integer.parseInt(array[0]);
				bounds.y = Integer.parseInt(array[1]);
				bounds.width = Integer.parseInt(array[2]);
				bounds.height = Integer.parseInt(array[3]);
			}
			array = s.getArray(COLUMNS);
			if (array != null) {
				columnsWidth = new int[array.length];
				for (int i = 0; i < columnsWidth.length; i++) {
					columnsWidth[i] = Integer.parseInt(array[i]);
				}
			}
		}
		setShellStyle(getShellStyle() | SWT.SHEET);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IWorkbenchHelpContextIds.WORKBENCH_EDITORS_DIALOG);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// Typically we would use the parent's createButtonsForButtonBar.
		// However, we only want a Cancel button and not an OK button. The
		// OK button will be used later (in createDialogArea) to activate
		// the selected editor.
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		Button button = getButton(IDialogConstants.CANCEL_ID);
		if (button != null) {
			button.setText(WorkbenchMessages.WorkbenchEditorsDialog_close);
		}

	}

	/**
	 * Initialize the dialog bounds with the bounds saved from the settings.
	 */
	@Override
	protected void initializeBounds() {
		if (bounds != null) {
			getShell().setBounds(bounds);
		} else {
			super.initializeBounds();
		}
	}

	/**
	 * Creates the contents of this dialog, initializes the listener and the update
	 * thread.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		initializeDialogUnits(parent);

		Font font = parent.getFont();

		Composite dialogArea = new Composite(parent, SWT.NONE);
		CellLayout dialogAreaLayout = new CellLayout(1)
				.setMargins(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN),
						convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN))
				.setSpacing(convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING),
						convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING))
				.setRow(1, Row.growing());
		dialogArea.setLayout(dialogAreaLayout);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Label over the table
		Label l = new Label(dialogArea, SWT.NONE);
		l.setText(WorkbenchMessages.WorkbenchEditorsDialog_label);
		l.setFont(font);
		l.setLayoutData(new CellData().align(SWT.FILL, SWT.CENTER));
		// Table showing the editors name, full path and perspective
		editorsTable = new Table(dialogArea, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		editorsTable.setLinesVisible(true);
		editorsTable.setHeaderVisible(true);
		editorsTable.setFont(font);

		final int height = 16 * editorsTable.getItemHeight();
		final int width = (int) (2.5 * height);

		CellData tableData = new CellData().align(SWT.FILL, SWT.FILL).setHint(CellData.OVERRIDE, width, height);

		editorsTable.setLayoutData(tableData);
		editorsTable.setLayout(new Layout() {
			@Override
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				return new Point(width, height);
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				TableColumn c[] = editorsTable.getColumns();
				if (columnsWidth == null) {
					int w = editorsTable.getClientArea().width;
					c[0].setWidth(w * 1 / 3);
					c[1].setWidth(w - c[0].getWidth());
				} else {
					c[0].setWidth(columnsWidth[0]);
					c[1].setWidth(columnsWidth[1]);
				}
				editorsTable.setLayout(null);
			}
		});
		// Name column
		TableColumn tc = new TableColumn(editorsTable, SWT.NONE);
		tc.setResizable(true);
		tc.setText(WorkbenchMessages.WorkbenchEditorsDialog_name);
		tc.addSelectionListener(headerListener);
		// Full path column
		tc = new TableColumn(editorsTable, SWT.NONE);
		tc.setResizable(true);
		tc.setText(WorkbenchMessages.WorkbenchEditorsDialog_path);
		tc.addSelectionListener(headerListener);

		// A composite for selection option buttons
		Composite selectionButtons = new Composite(dialogArea, SWT.NULL);
		Label compLabel = new Label(selectionButtons, SWT.NULL);
		compLabel.setFont(font);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		selectionButtons.setLayout(layout);

		// Select clean editors button
		selectClean = new Button(selectionButtons, SWT.PUSH);
		selectClean.setText(WorkbenchMessages.WorkbenchEditorsDialog_selectClean);
		selectClean.addSelectionListener(widgetSelectedAdapter(e -> {
			editorsTable.setSelection(selectClean(editorsTable.getItems()));
			updateButtons();
		}));
		selectClean.setFont(font);
		setButtonLayoutData(selectClean);

		// Invert selection button
		invertSelection = new Button(selectionButtons, SWT.PUSH);
		invertSelection.setText(WorkbenchMessages.WorkbenchEditorsDialog_invertSelection);
		invertSelection.addSelectionListener(widgetSelectedAdapter(e -> {
			editorsTable.setSelection(invertedSelection(editorsTable.getItems(), editorsTable.getSelection()));
			updateButtons();
		}));
		invertSelection.setFont(font);
		setButtonLayoutData(invertSelection);

		// Select all button
		allSelection = new Button(selectionButtons, SWT.PUSH);
		allSelection.setText(WorkbenchMessages.WorkbenchEditorsDialog_allSelection);
		allSelection.addSelectionListener(widgetSelectedAdapter(e -> {
			editorsTable.setSelection(editorsTable.getItems());
			updateButtons();
		}));
		allSelection.setFont(font);
		setButtonLayoutData(allSelection);

		// A composite for selected editor action buttons
		Composite actionButtons = new Composite(dialogArea, SWT.NULL);
		Label actLabel = new Label(actionButtons, SWT.NULL);
		actLabel.setFont(font);
		GridLayout actLayout = new GridLayout();
		actLayout.numColumns = 4;
		actionButtons.setLayout(actLayout);

		// Activate selected editor button
		createButton(actionButtons, IDialogConstants.OK_ID, WorkbenchMessages.WorkbenchEditorsDialog_activate, true);

		// Close selected editors button
		closeSelected = new Button(actionButtons, SWT.PUSH);
		closeSelected.setText(WorkbenchMessages.WorkbenchEditorsDialog_closeSelected);
		closeSelected.addSelectionListener(widgetSelectedAdapter(e -> closeItems(editorsTable.getSelection())));
		closeSelected.setFont(font);
		setButtonLayoutData(closeSelected);

		// Save selected editors button
		saveSelected = new Button(actionButtons, SWT.PUSH);
		saveSelected.setText(WorkbenchMessages.WorkbenchEditorsDialog_saveSelected);
		saveSelected.addSelectionListener(widgetSelectedAdapter(e -> saveItems(editorsTable.getSelection())));
		saveSelected.setFont(font);
		setButtonLayoutData(saveSelected);

		// Show only active perspective button
		final Button showAllPerspButton = new Button(dialogArea, SWT.CHECK);
		showAllPerspButton.setText(WorkbenchMessages.WorkbenchEditorsDialog_showAllPersp);
		showAllPerspButton.setSelection(showAllPersp);
		showAllPerspButton.setFont(font);
		setButtonLayoutData(showAllPerspButton);
		showAllPerspButton.addSelectionListener(widgetSelectedAdapter(e -> {
			showAllPersp = showAllPerspButton.getSelection();
			updateItems();
		}));
		// Create the items and update buttons state
		updateItems();
		updateButtons();

		editorsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				okPressed();
			}
		});
		editorsTable.addDisposeListener(e -> {
			for (Iterator<Image> images2 = disabledImageCache.values().iterator(); images2.hasNext();) {
				Image i2 = images2.next();
				i2.dispose();
			}
		});
		editorsTable.setFocus();
		applyDialogFont(dialogArea);
		return dialogArea;
	}

	/**
	 * Updates the button state (enabled/disabled)
	 */
	private void updateButtons() {
		TableItem selectedItems[] = editorsTable.getSelection();
		boolean hasDirty = false;
		for (TableItem selectedItem : selectedItems) {
			Adapter editor = (Adapter) selectedItem.getData();
			if (editor.isDirty()) {
				hasDirty = true;
				break;
			}
		}
		saveSelected.setEnabled(hasDirty);

		TableItem allItems[] = editorsTable.getItems();
		boolean hasClean = false;
		for (TableItem tableItem : allItems) {
			Adapter editor = (Adapter) tableItem.getData();
			if (!editor.isDirty()) {
				hasClean = true;
				break;
			}
		}
		selectClean.setEnabled(hasClean);
		invertSelection.setEnabled(allItems.length > 0);
		closeSelected.setEnabled(selectedItems.length > 0);

		Button ok = getOkButton();
		if (ok != null) {
			ok.setEnabled(selectedItems.length == 1);
		}
	}

	/**
	 * Closes the specified editors
	 */
	private void closeItems(TableItem items[]) {
		if (items.length == 0) {
			return;
		}

		// collect all instantiated editors that have been selected
		List<IWorkbenchPart> selectedEditors = new ArrayList<>();
		for (TableItem item : items) {
			Adapter e = (Adapter) item.getData();
			if (e.editorRef != null) {
				IWorkbenchPart part = e.editorRef.getPart(false);
				if (part != null) {
					selectedEditors.add(part);
				}
			}
		}

		SaveablesList saveablesList = (SaveablesList) window.getService(ISaveablesLifecycleListener.class);
		// prompt for save
		if (saveablesList.preCloseParts(selectedEditors, true, this, window) != null) {
			// close all editors
			for (TableItem item : items) {
				Adapter e = (Adapter) item.getData();
				e.close();
			}
			// update the list
			updateItems();
		}
	}

	/**
	 * Saves the specified editors
	 */
	private void saveItems(TableItem items[]) {
		if (items.length == 0) {
			return;
		}
		ProgressMonitorDialog pmd = new ProgressMonitorJobsDialog(getShell());
		pmd.open();
		for (TableItem item : items) {
			Adapter editor = (Adapter) item.getData();
			editor.save(pmd.getProgressMonitor());
			updateItem(item, editor);
		}
		pmd.close();
		updateItems();
	}

	/**
	 * Returns all clean editors from items[];
	 */
	private TableItem[] selectClean(TableItem items[]) {
		if (items.length == 0) {
			return new TableItem[0];
		}
		ArrayList<TableItem> cleanItems = new ArrayList<>(items.length);
		for (TableItem item : items) {
			Adapter editor = (Adapter) item.getData();
			if (!editor.isDirty()) {
				cleanItems.add(item);
			}
		}
		TableItem result[] = new TableItem[cleanItems.size()];
		cleanItems.toArray(result);
		return result;
	}

	/**
	 * Returns all clean editors from items[];
	 */
	private TableItem[] invertedSelection(TableItem allItems[], TableItem selectedItems[]) {
		if (allItems.length == 0) {
			return allItems;
		}
		ArrayList<TableItem> invertedSelection = new ArrayList<>(allItems.length - selectedItems.length);
		outerLoop: for (TableItem allItem : allItems) {
			for (TableItem selectedItem : selectedItems) {
				if (allItem == selectedItem) {
					continue outerLoop;
				}
			}
			invertedSelection.add(allItem);
		}
		TableItem result[] = new TableItem[invertedSelection.size()];
		invertedSelection.toArray(result);
		return result;
	}

	/**
	 * Updates the specified item
	 */
	private void updateItem(TableItem item, Adapter editor) {
		item.setData(editor);
		item.setText(editor.getText());
		Image image = editor.getImage();
		if (image != null)
			item.setImage(0, image);
	}

	/**
	 * Adds all editors to elements
	 */
	private void updateEditors(IWorkbenchPage[] pages) {
		for (IWorkbenchPage page : pages) {
			for (IEditorReference editor : page.getEditorReferences()) {
				elements.add(new Adapter(editor));
			}
		}
	}

	/**
	 * Updates all items in the table
	 */
	private void updateItems() {
		// record what the user has selected
		TableItem[] selectedItems = editorsTable.getSelection();
		Adapter[] selectedAdapters = new Adapter[selectedItems.length];
		for (int i = 0; i < selectedItems.length; i++) {
			selectedAdapters[i] = (Adapter) selectedItems[i].getData();
		}

		// remove all the items
		editorsTable.removeAll();
		elements = new ArrayList<>();
		if (showAllPersp) {
			for (IWorkbenchWindow workbenchWindow : window.getWorkbench().getWorkbenchWindows()) {
				updateEditors(workbenchWindow.getPages());
			}
		} else {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				updateEditors(new IWorkbenchPage[] { page });
			}
		}

		// sort the items
		sort();

		List<TableItem> selection = new ArrayList<>(selectedItems.length);
		for (Iterator<Adapter> iterator = elements.iterator(); iterator.hasNext();) {
			Adapter e = iterator.next();
			TableItem item = new TableItem(editorsTable, SWT.NULL);
			updateItem(item, e);

			// try to match this item's editor to one that was previously selected
			for (Adapter selectedAdapter : selectedAdapters) {
				if (selectedAdapter.editorRef == e.editorRef) {
					selection.add(item);
				}
			}
		}

		// set the selection back to the table
		editorsTable.setSelection(selection.toArray(new TableItem[selection.size()]));

		// update the buttons, because the selection may have changed
		updateButtons();
	}

	/**
	 * Sorts all the editors according to the table header
	 */
	private void sort() {
		// Backward compatible. Table used to have 3 columns.
		if (sortColumn > (editorsTable.getColumnCount() - 1)) {
			sortColumn = 0;
		}
		Adapter a[] = new Adapter[elements.size()];
		elements.toArray(a);
		Arrays.sort(a);
		elements = Arrays.asList(a);
	}

	/**
	 * The user has selected a resource and the dialog is closing.
	 */
	@Override
	protected void okPressed() {
		TableItem items[] = editorsTable.getSelection();
		if (items.length != 1) {
			super.okPressed();
			return;
		}

		Adapter selection = (Adapter) items[0].getData();
		// It would be better to activate before closing the
		// dialog but it does not work when the editor is in other
		// window. Must investigate.
		super.okPressed();
		selection.activate();
	}

	@Override
	public boolean close() {
		saveDialogSettings();
		return super.close();
	}

	/**
	 * Saves the dialog settings.
	 */
	private void saveDialogSettings() {
		IDialogSettings s = getDialogSettings();
		s.put(ALLPERSP, showAllPersp);
		s.put(SORT, sortColumn);
		bounds = getShell().getBounds();
		String array[] = new String[4];
		array[0] = String.valueOf(bounds.x);
		array[1] = String.valueOf(bounds.y);
		array[2] = String.valueOf(bounds.width);
		array[3] = String.valueOf(bounds.height);
		s.put(BOUNDS, array);
		array = new String[editorsTable.getColumnCount()];
		for (int i = 0; i < array.length; i++) {
			array[i] = String.valueOf(editorsTable.getColumn(i).getWidth());
		}
		s.put(COLUMNS, array);
	}

	/**
	 * Return a dialog setting section for this dialog
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = PlatformUI
				.getDialogSettingsProvider(FrameworkUtil.getBundle(WorkbenchEditorsDialog.class)).getDialogSettings();
		IDialogSettings thisSettings = settings.getSection(getClass().getName());
		if (thisSettings == null) {
			thisSettings = settings.addNewSection(getClass().getName());
		}
		return thisSettings;
	}

	/**
	 * A helper inner class to adapt EditorHistoryItem and IEditorPart in the same
	 * type.
	 */
	private class Adapter implements Comparable<Adapter> {
		IEditorReference editorRef;

		IEditorInput input;

		IEditorDescriptor desc;

		String text[];

		Image image;

		Adapter(IEditorReference ref) {
			editorRef = ref;
		}

		boolean isDirty() {
			if (editorRef == null) {
				return false;
			}
			return editorRef.isDirty();
		}

		void close() {
			if (editorRef == null) {
				return;
			}
			WorkbenchPage p = (WorkbenchPage) editorRef.getPage();
			// already saved when the i
			p.closeEditor(editorRef, false);
		}

		void save(IProgressMonitor monitor) {
			if (editorRef == null) {
				return;
			}
			IEditorPart editor = (IEditorPart) editorRef.getPart(true);
			if (editor != null) {
				editor.doSave(monitor);
			}
		}

		String[] getText() {
			if (text != null) {
				return text;
			}
			text = new String[2];
			if (editorRef != null) {
				if (editorRef.isDirty()) {
					text[0] = "*" + editorRef.getTitle(); //$NON-NLS-1$
				} else {
					text[0] = editorRef.getTitle();
				}
				text[1] = editorRef.getTitleToolTip();
			} else {
				text[0] = input.getName();
				text[1] = input.getToolTipText();
			}
			if (text[0] != null) {
				text[0] = TextProcessor.process(text[0]);
			}
			if (text[1] != null) {
				text[1] = TextProcessor.process(text[1]);
			}
			return text;
		}

		Image getImage() {
			if (image != null) {
				return image;
			}
			if (editorRef != null) {
				image = editorRef.getTitleImage();
			} else {
				ImageDescriptor imageDesc = null;
				if (desc != null) {
					imageDesc = desc.getImageDescriptor();
				}
				if (imageDesc == null) {
					IEditorRegistry registry = WorkbenchPlugin.getDefault().getEditorRegistry();
					imageDesc = registry.getImageDescriptor(input.getName());
					// TODO: how can this honour content types? Guessing at the content type
					// perhaps?

					if (imageDesc == null) {
						// @issue what should be the default image?
						// image = registry.getDefaultEditor().getImageDescriptor();
					}
				}
				if (imageDesc != null) {
					image = disabledImageCache.get(imageDesc);
					if (image == null) {
						Image enabled = imageDesc.createImage();
						Image disabled = new Image(editorsTable.getDisplay(), enabled, SWT.IMAGE_DISABLE);
						enabled.dispose();
						disabledImageCache.put(imageDesc, disabled);
						image = disabled;
					}
				}
			}
			return image;
		}

		private void activate() {
			if (editorRef != null) {
				IEditorPart editor = editorRef.getEditor(true);
				WorkbenchPage p = (WorkbenchPage) editor.getEditorSite().getPage();
				Shell s = p.getWorkbenchWindow().getShell();
				if (s.getMinimized()) {
					s.setMinimized(false);
				}
				s.moveAbove(null);
				p.getWorkbenchWindow().setActivePage(p);
				p.activate(editor);
			} else {
				IWorkbenchPage p = window.getActivePage();
				if (p != null) {
					try {
						p.openEditor(input, desc.getId(), true);
					} catch (PartInitException e) {
					}
				}
			}
		}

		@Override
		public int compareTo(Adapter another) {
			Adapter adapter = another;
			int result = collator.compare(getText()[sortColumn], adapter.getText()[sortColumn]);
			if (result == 0) {
				int column = sortColumn == 0 ? 1 : 0;
				result = collator.compare(getText()[column], adapter.getText()[column]);
			}
			if (reverse) {
				return result * -1;
			}
			return result;
		}
	}
}
