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

	private boolean showActivePersp;
	private boolean showHistory;
	private int sortColumn;
	private List elements = new ArrayList();
	private HashMap imageCache = new HashMap();
	private boolean reverse = false;
	private Collator collator = Collator.getInstance();
	private Rectangle bounds;
	
	private static final String SORT = "sort";
	private static final String HISTORY = "history";
	private static final String ACTIVEPERSP = "activePersp";
	private static final String BOUNDS = "bounds";
	
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
		if(s.get(ACTIVEPERSP) == null) {
			showActivePersp = false;
			showHistory = true;
			sortColumn = 0;			
		} else {
			showActivePersp = s.getBoolean(ACTIVEPERSP);
			showHistory = s.getBoolean(HISTORY);
			sortColumn = s.getInt(SORT);
			String[] b = s.getArray(BOUNDS);
			if(b != null) {
				bounds = new Rectangle(0,0,0,0);
				bounds.x = new Integer(b[0]).intValue();
				bounds.y = new Integer(b[1]).intValue();
				bounds.width = new Integer(b[2]).intValue();
				bounds.height = new Integer(b[3]).intValue();
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
		tableData.heightHint = 22 * editorsTable.getItemHeight();; 
		tableData.widthHint = (int)(1.5 * tableData.heightHint);
		
		editorsTable.setLayoutData(tableData);
		editorsTable.setLayout(new Layout() {
			protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache){
				return new Point(tableData.widthHint,tableData.heightHint);
			}
			protected void layout (Composite composite, boolean flushCache){
				TableColumn c[] = editorsTable.getColumns();
				int w = editorsTable.getBounds().width;
				c[0].setWidth(w * 2 / 8);
				c[1].setWidth(w * 3 / 8);
				c[2].setWidth(w - c[0].getWidth() - c[1].getWidth());
				editorsTable.setLayout(null);
				tableData.heightHint = 3 * editorsTable.getItemHeight();
				tableData.widthHint = (int)(1.5 * tableData.heightHint);
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
		//Perspective column		
		tc = new TableColumn(editorsTable,SWT.NONE);
		tc.setResizable(true);
		tc.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.perspective"));
		tc.addSelectionListener(headerListener);
		//A composite for save editors and close editors buttons
		Composite selectionButtons = new Composite(dialogArea,SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
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
		//Show history check box
		final Button showHistoryButton = new Button(dialogArea,SWT.CHECK);
		showHistoryButton.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.showHistory"));
		showHistoryButton.setSelection(showHistory);
		showHistoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showHistory = showHistoryButton.getSelection();
				updateItems();
			}
		});
		//Show only active perspective button
		final Button showActivePerspButton = new Button(dialogArea,SWT.CHECK);
		showActivePerspButton.setText(WorkbenchMessages.getString("WorkbenchEditorsDialog.showActivePerspButton"));
		showActivePerspButton.setSelection(showActivePersp);
		showActivePerspButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showActivePersp = showActivePerspButton.getSelection();
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
			}
		});
		editorsTable.setFocus();
		return dialogArea;
	}
	/*
	 * Update buttons stated (enabled/disabled)
	 */
	private void updateButtons() {
		TableItem items[] = editorsTable.getSelection();
		for (int i = 0; i < items.length; i ++) {
			Adapter editor = (Adapter)items[i].getData();
			if(editor.isDirty()) {
				saveSelected.setEnabled(true);
				break;
			}
			saveSelected.setEnabled(false);
		}
		for (int i = 0; i < items.length; i ++) {
			Adapter editor = (Adapter)items[i].getData();
			if(editor.isOpened()) {
				closeSelected.setEnabled(true);	
				break;
			}
		}
	}
	/*
	 * Close the specified editors
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
	/*
	 * Save the specified editors
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
	/*
	 * Update the specified item
	 */
	private void updateItem(TableItem item,Adapter editor) {
		item.setData(editor);
		item.setText(editor.getText());
		Image images[] = editor.getImage();
		item.setImage(0,images[0]);
		item.setImage(2,images[2]); 
	}
	/*
	 * Add all editors to elements
	 */
	private void updateEditors(IWorkbenchPage[] pages) {
		for (int j = 0; j < pages.length; j++) {
			IEditorPart editors[] = pages[j].getEditors();
			for (int k = 0; k < editors.length; k++) {
				elements.add(new Adapter(editors[k]));
			}
		}
	}
	/*
	 * Update all items in the table
	 */
	private void updateItems() {
		boolean showAllPerspectives = !showActivePersp;
		editorsTable.removeAll();
		elements = new ArrayList();
		if(showAllPerspectives) {
			IWorkbenchWindow windows[] = window.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++)
				updateEditors(windows[i].getPages());
		} else {
			IWorkbenchPage page = window.getActivePage();
			updateEditors(new IWorkbenchPage[]{page});
		}
		if(showHistory) {
			Workbench wb = (Workbench)window.getWorkbench();
			EditorHistory history = wb.getEditorHistory();
			EditorHistoryItem editors[] = history.getItems();
			for (int i = 0; i < editors.length; i ++)
				elements.add(new Adapter(editors[i].input,editors[i].desc));
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
	}
	/*
	 * Sort all the editors according to the table header
	 */
	private void sort() {
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
		if(items.length != 1)
			return;
		
		IDialogSettings s = getDialogSettings();
		s.put(ACTIVEPERSP,showActivePersp);
		s.put(HISTORY,showHistory);
		s.put(SORT,sortColumn);
		bounds = getShell().getBounds();
		String b[] = new String[4];
		b[0] = String.valueOf(bounds.x);
		b[1] = String.valueOf(bounds.y);
		b[2] = String.valueOf(bounds.width);
		b[3] = String.valueOf(bounds.height);
		s.put(BOUNDS,b);
				
		Adapter selection = (Adapter)items[0].getData();	
		//It would be better to activate before closing the
		//dialog but it does not work when the editor is in other
		//window. Must investigate.
		super.okPressed();
		selection.activate();
	}
	/*
	 * Return a dialog setting section for this dialog
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings thisSettings = settings.getSection(getClass().getName());
		if (thisSettings == null)
			thisSettings = settings.addNewSection(getClass().getName());
		return thisSettings;
	}
	/*
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
			text = new String[3];
			if(editor != null) {	
				if(editor.isDirty())
					text[0] = "*" + editor.getTitle();
				else
					text[0] = editor.getTitle();
				text[1] = editor.getTitleToolTip();
				text[2] = editor.getEditorSite().getPage().getLabel();
			} else {	
				text[0] = input.getName();
				text[1] = input.getToolTipText();
				text[2] = WorkbenchMessages.getString("WorkbenchEditorsDialog.closed");
			}
			return text;
		}
		Image[] getImage() {
			if(images != null)
				return images;
			images = new Image[3];
			if(editor != null) {
				images[0] = editor.getTitleImage();
				IPerspectiveDescriptor persp = editor.getEditorSite().getPage().getPerspective();
				ImageDescriptor image = persp.getImageDescriptor();
				if(image == null)
					image = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE);
				images[2] = (Image)imageCache.get(image);
				if(images[2] == null) {
					images[2] = image.createImage();
					imageCache.put(image,images[2]);
				}
			} else {
				ImageDescriptor image = null;
				if(desc != null)
					image = desc.getImageDescriptor();
				if(image == null)
					image = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE);
				images[2] = (Image)imageCache.get(image);
				if(images[2] == null) {
					images[2] = image.createImage();
					Image disabled = new Image(editorsTable.getDisplay(), images[2], SWT.IMAGE_DISABLE);
					images[2].dispose();
					images[2] = disabled;
					imageCache.put(image,images[2]);
				}
				images[0] = images[2];
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
			Adapter a = (Adapter)another;
			if(reverse)
				return collator.compare(getText()[sortColumn],a.getText()[sortColumn]);
			else
				return collator.compare(a.getText()[sortColumn],getText()[sortColumn]);
		}
	}
}