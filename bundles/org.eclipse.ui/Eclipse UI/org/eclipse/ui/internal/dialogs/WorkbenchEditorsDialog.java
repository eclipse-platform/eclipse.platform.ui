package org.eclipse.ui.internal.dialogs;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.dialogs.*;

/**
 * Implements a dialog showing all opened editors in the workbench
 * and the recent closed editors
 */
public class WorkbenchEditorsDialog extends SelectionDialog {

	private WorkbenchWindow window;
	private Table editorsTable;
	private Button saveSelected;
	private Button closeSelected;
	private Button selectClean;
	private Button invertSelection;
	
	private boolean showAllPersp = false;
	private int sortColumn;
	private List elements = new ArrayList();
	private HashMap imageCache = new HashMap(11);
	private HashMap disabledImageCache = new HashMap(11);
	private boolean reverse = false;
	private Collator collator = Collator.getInstance();
	private Rectangle bounds;
	private int columnsWidth[];
	
	private static final String SORT = "sort";
	private static final String HISTORY = "history";
	private static final String ALLPERSP = "allPersp";
	private static final String BOUNDS = "bounds";
	private static final String COLUMNS = "columns";
	
	private SelectionListener headerListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			int index = editorsTable.indexOf((TableColumn) e.widget);
			if(index == sortColumn)
				reverse = !reverse;
			else
				sortColumn = index;
			updateItems();
		}
	};
	
	/**
	 * Constructor for WorkbenchEditorsDialog.
	 */
	public WorkbenchEditorsDialog(WorkbenchWindow window) {
		super(window.getShell());
		this.window = window;
		setTitle(WorkbenchMessages.getString("WorkbenchEditorsDialog.title")); //$NON-NLS-1$
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		IDialogSettings s = getDialogSettings();
		if(s.get(ALLPERSP) == null) {
			sortColumn = 0;			
		} else {
			showAllPersp = s.getBoolean(ALLPERSP);
			sortColumn = s.getInt(SORT);
			String[] array = s.getArray(BOUNDS);
			if(array != null) {
				bounds = new Rectangle(0,0,0,0);
				bounds.x = new Integer(array[0]).intValue();
				bounds.y = new Integer(array[1]).intValue();
				bounds.width = new Integer(array[2]).intValue();
				bounds.height = new Integer(array[3]).intValue();
			}
			array = s.getArray(COLUMNS);
			if(array != null) {
				columnsWidth = new int[array.length];
				for (int i = 0; i < columnsWidth.length; i++)
					columnsWidth[i] = new Integer(array[i]).intValue();
			}
		}
	}
	
	/**
	 * Initialize the dialog bounds with the bounds saved
	 * from the settings.
	 */
	protected void initializeBounds() {
		if(bounds != null) {
			getShell().setBounds(bounds);		
		} else {
			super.initializeBounds();
		}
	}
	
	/**
	 * Creates the contents of this dialog, initializes the
	 * listener and the update thread.
	 */
	protected Control createDialogArea(Composite parent) {

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		//Label over the table
		Label l = new Label(dialogArea, SWT.NONE);
		l.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.label")); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		l.setLayoutData(data);
		//Table showing the editors name, full path and perspective
		editorsTable = new Table(dialogArea, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		editorsTable.setLinesVisible(true);
		editorsTable.setHeaderVisible(true);
		
		final GridData tableData = new GridData(GridData.FILL_BOTH);
		tableData.heightHint = 16 * editorsTable.getItemHeight();
		tableData.widthHint = (int) (2.5 * tableData.heightHint);
		
		editorsTable.setLayoutData(tableData);
		editorsTable.setLayout(new Layout() {
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache){
				return new Point(tableData.widthHint, tableData.heightHint);
			}
			protected void layout(Composite composite, boolean flushCache){
				TableColumn c[] = editorsTable.getColumns();
				if(columnsWidth == null) {
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
		//Name column
		TableColumn tc = new TableColumn(editorsTable,SWT.NONE);
		tc.setResizable(true);
		tc.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.name"));
		tc.addSelectionListener(headerListener);
		//Full path column
		tc = new TableColumn(editorsTable,SWT.NONE);
		tc.setResizable(true);
		tc.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.path"));
		tc.addSelectionListener(headerListener);
		//A composite for save editors and close editors buttons
		Composite selectionButtons = new Composite(dialogArea,SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		selectionButtons.setLayout(layout);
		//Close editors button
		closeSelected = new Button(selectionButtons,SWT.PUSH);
		closeSelected.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.closeSelected"));
		closeSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				closeItems(editorsTable.getSelection());
			}
		});
		//Save editors button
		saveSelected = new Button(selectionButtons,SWT.PUSH);
		saveSelected.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.saveSelected"));
		saveSelected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveItems(editorsTable.getItems(),null);
			}
		});
		//Select clean editors button
		selectClean = new Button(selectionButtons,SWT.PUSH);
		selectClean.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.selectClean"));
		selectClean.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editorsTable.setSelection(selectClean(editorsTable.getItems()));
				updateButtons();
			}
		});
		//Invert selection button
		invertSelection = new Button(selectionButtons,SWT.PUSH);
		invertSelection.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.invertSelection"));
		invertSelection.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editorsTable.setSelection(invertedSelection(editorsTable.getItems(),editorsTable.getSelection()));
				updateButtons();
			}
		});
		
		//Show only active perspective button
		final Button showAllPerspButton = new Button(dialogArea,SWT.CHECK);
		showAllPerspButton.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.showAllPersp"));
		showAllPerspButton.setSelection(showAllPersp);
		showAllPerspButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showAllPersp = showAllPerspButton.getSelection();
				updateItems();
			}
		});		
		//Create the items and update buttons state
		updateItems();
		updateButtons();
		
		editorsTable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				okPressed();
			}
		});
		editorsTable.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				for (Iterator images = imageCache.values().iterator(); images.hasNext();) {
					Image i = (Image)images.next();
					i.dispose();
				}
				for (Iterator images = disabledImageCache.values().iterator(); images.hasNext();) {
					Image i = (Image)images.next();
					i.dispose();
				}
			}
		});
		editorsTable.setFocus();
		return dialogArea;
	}
	/**
	 * Updates the button state (enabled/disabled)
	 */
	private void updateButtons() {
		TableItem selectedItems[] = editorsTable.getSelection();
		boolean hasDirty = false;
		for (int i = 0; i < selectedItems.length; i ++) {
			Adapter editor = (Adapter)selectedItems[i].getData();
			if(editor.isDirty()) {
				hasDirty = true;
				break;
			}
		}
		saveSelected.setEnabled(hasDirty);
		
		TableItem allItems[] = editorsTable.getItems();
		boolean hasClean = false;
		for (int i = 0; i < allItems.length; i ++) {
			Adapter editor = (Adapter)allItems[i].getData();
			if(!editor.isDirty()) {
				hasClean = true;
				break;
			}
		}
		selectClean.setEnabled(hasClean);
		invertSelection.setEnabled(allItems.length > 0);		
		closeSelected.setEnabled(selectedItems.length > 0);
		
		Button ok = getOkButton();
		if (ok != null)
			ok.setEnabled(selectedItems.length == 1);
	}
	/**
	 * Closes the specified editors
	 */
	private void closeItems(TableItem items[]) {
		if(items.length == 0)
			return;
		for (int i = 0; i < items.length; i++) {
			Adapter e = (Adapter)items[i].getData();
			e.close();
		}
		updateItems();
	}
	/**
	 * Saves the specified editors
	 */
	private void saveItems(TableItem items[],IProgressMonitor monitor) {
		if(items.length == 0)
			return;
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(getShell());
		pmd.open();
		for (int i = 0; i < items.length; i++) {
			Adapter editor = (Adapter)items[i].getData();
			editor.save(pmd.getProgressMonitor());
			updateItem(items[i],editor);
		}
		pmd.close();
		updateItems();
	}
	/**
	 * Returns all clean editors from items[];
	 */
	private TableItem[] selectClean(TableItem items[]) {
		if(items.length == 0)
			return new TableItem[0];
		ArrayList cleanItems = new ArrayList(items.length);
		for (int i = 0; i < items.length; i++) {
			Adapter editor = (Adapter)items[i].getData();
			if(!editor.isDirty())
				cleanItems.add(items[i]);
		}
		TableItem result[] = new TableItem[cleanItems.size()];
		cleanItems.toArray(result);
		return result;
	}
	/**
	 * Returns all clean editors from items[];
	 */
	private TableItem[] invertedSelection(TableItem allItems[],TableItem selectedItems[]) {
		if(allItems.length == 0)
			return allItems;
		ArrayList invertedSelection = new ArrayList(allItems.length - selectedItems.length);
		outerLoop: for (int i = 0; i < allItems.length; i++) {
			for (int j = 0; j < selectedItems.length; j++) {
				if(allItems[i] == selectedItems[j])
					continue outerLoop;
			}
			invertedSelection.add(allItems[i]);
		}
		TableItem result[] = new TableItem[invertedSelection.size()];
		invertedSelection.toArray(result);
		return result;
	}		
	/**
	 * Updates the specified item
	 */
	private void updateItem(TableItem item,Adapter editor) {
		item.setData(editor);
		item.setText(editor.getText());
		Image images[] = editor.getImage();
		for (int i = 0; i < images.length; i++) {
			if (images[i] != null)
				item.setImage(i, images[i]);
		}
	}
	/**
	 * Adds all editors to elements
	 */
	private void updateEditors(IWorkbenchPage[] pages) {
		for (int j = 0; j < pages.length; j++) {
			IEditorPart editors[] = pages[j].getEditors();
			for (int k = 0; k < editors.length; k++) {
				elements.add(new Adapter(editors[k]));
			}
		}
	}
	/**
	 * Updates all items in the table
	 */
	private void updateItems() {
		editorsTable.removeAll();
		elements = new ArrayList();
		if(showAllPersp) {
			IWorkbenchWindow windows[] = window.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++)
				updateEditors(windows[i].getPages());
		} else {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				updateEditors(new IWorkbenchPage[]{page});
			}
		}
		sort();
		Object selection = null;
		if(window.getActivePage() != null)
			selection = window.getActivePage().getActiveEditor();
		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			Adapter e = (Adapter) iterator.next();
			TableItem item = new TableItem(editorsTable,SWT.NULL);
			updateItem(item,e);
			if((selection != null) && (selection == e.editor))
				editorsTable.setSelection(new TableItem[]{item});
		}
		// update the buttons, because the selection may have changed
		updateButtons();
	}
	/**
	 * Sorts all the editors according to the table header
	 */
	private void sort() {
		//Backward compatible. Table used to have 3 columns.
		if(sortColumn > (editorsTable.getColumnCount() - 1))
			sortColumn = 0;
		Adapter a[] = new Adapter[elements.size()];
		elements.toArray(a);
		Arrays.sort(a);
		elements = Arrays.asList(a);
	}
	/**
 	 * The user has selected a resource and the dialog is closing.
     */
	protected void okPressed() {
		TableItem items[] = editorsTable.getSelection();
		if(items.length != 1) {
			super.okPressed();
			return;
		}
		
		saveDialogSettings();
						
		Adapter selection = (Adapter)items[0].getData();	
		//It would be better to activate before closing the
		//dialog but it does not work when the editor is in other
		//window. Must investigate.
		super.okPressed();
		selection.activate();
	}
	/**
	 * Saves the dialog settings.
	 */
	private void saveDialogSettings() {
		IDialogSettings s = getDialogSettings();
		s.put(ALLPERSP,showAllPersp);
		s.put(SORT,sortColumn);
		bounds = getShell().getBounds();
		String array[] = new String[4];
		array[0] = String.valueOf(bounds.x);
		array[1] = String.valueOf(bounds.y);
		array[2] = String.valueOf(bounds.width);
		array[3] = String.valueOf(bounds.height);
		s.put(BOUNDS,array);
		array = new String[editorsTable.getColumnCount()];
		for (int i = 0; i < array.length; i++)
			array[i] = String.valueOf(editorsTable.getColumn(i).getWidth());
		s.put(COLUMNS,array);
	}

	/**
	 * Return a dialog setting section for this dialog
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings thisSettings = settings.getSection(getClass().getName());
		if (thisSettings == null)
			thisSettings = settings.addNewSection(getClass().getName());
		return thisSettings;
	}
	/**
	 * A helper inner class to adapt EditorHistoryItem and IEditorPart
	 * in the same type.
	 */
	private class Adapter implements Comparable {
		IEditorPart editor;
		IEditorInput input;
		IEditorDescriptor desc;
		String text[];
		Image images[];
		Adapter(IEditorPart part) {
			editor = part;
		}
		Adapter(IEditorInput input,IEditorDescriptor desc) {
			this.input = input;
			this.desc = desc;
		}
		boolean isDirty() {
			if(editor == null)
				return false;
			return editor.isDirty();
		}
		boolean isOpened() {
			return editor != null;
		}
		void close() {
			if(editor == null)
				return;
			WorkbenchPage p = (WorkbenchPage)editor.getEditorSite().getPage();
			p.closeEditor(editor,true);
		}
		void save(IProgressMonitor monitor) {
			if(editor == null)
				return;
			editor.doSave(monitor);
		}
		String[] getText() {
			if(text != null)
				return text;
			text = new String[2];
			if(editor != null) {	
				if(editor.isDirty())
					text[0] = "*" + editor.getTitle();
				else
					text[0] = editor.getTitle();
				text[1] = editor.getTitleToolTip();
			} else {	
				text[0] = input.getName();
				text[1] = input.getToolTipText();
			}
			return text;
		}
		Image[] getImage() {
			if(images != null)
				return images;
			images = new Image[2];
			if(editor != null) {
				images[0] = editor.getTitleImage();
				IPerspectiveDescriptor persp = editor.getEditorSite().getPage().getPerspective();
				ImageDescriptor image = persp.getImageDescriptor();
				if(image == null)
					image = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE);
			} else {
				ImageDescriptor image = null;
				if(desc != null)
					image = desc.getImageDescriptor();
				if(image == null) {
					IEditorRegistry registry = WorkbenchPlugin.getDefault().getEditorRegistry();
					image = registry.getImageDescriptor(input.getName());
					if (image == null) {
						image = registry.getDefaultEditor().getImageDescriptor();
					}
				}
				if (image != null) {
					images[0] = (Image)disabledImageCache.get(image);
					if(images[0] == null) {
						Image enabled = image.createImage();
						Image disabled = new Image(editorsTable.getDisplay(), enabled, SWT.IMAGE_DISABLE);
						enabled.dispose();
						disabledImageCache.put(image, disabled);
						images[0] = disabled;
					}
				}
			}
			return images;
		}
	
		private void activate(){
			if(editor != null) {
				WorkbenchPage p = (WorkbenchPage)editor.getEditorSite().getPage();
				Shell s = p.getWorkbenchWindow().getShell();
				if(s.getMinimized())
					s.setMinimized(false);
				s.moveAbove(null);
				p.getWorkbenchWindow().setActivePage(p);
				p.activate(editor);
			} else {
				IWorkbenchPage p = window.getActivePage();
				try {
					if(desc != null)
						p.openEditor(input,desc.getId(),true);
					else if(input instanceof IFileEditorInput)
						p.openEditor(((IFileEditorInput)input).getFile());
				} catch (PartInitException e) {
				}
			}
		}
		public int compareTo(Object another) {
			Adapter adapter = (Adapter)another;
			int result = collator.compare(getText()[sortColumn],adapter.getText()[sortColumn]);
			if(result == 0) {
				int column = sortColumn == 0 ? 1 : 0;
				result = collator.compare(getText()[column],adapter.getText()[column]);
			}
			if(reverse)
				return result * -1;
			return result;
		}
	}
}