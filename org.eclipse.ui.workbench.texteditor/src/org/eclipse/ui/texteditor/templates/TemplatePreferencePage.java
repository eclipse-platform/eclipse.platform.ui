/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A template preference page allows configuration of the templates for an
 * editor. It provides controls for adding, removing and changing templates as
 * well as enablement, default management and an optional formatter preference.
 * <p>
 * Subclasses need to provide a {@link TemplateStore} and a
 * {@link ContextTypeRegistry} and should set the preference store. They may
 * optionally override {@link #isShowFormatterSetting()}.
 * </p>
 * 
 * @since 3.0
 */
public abstract class TemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Label provider for templates.
	 */
	private class TemplateLabelProvider extends LabelProvider implements ITableLabelProvider {
	
		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	
		/*
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			TemplatePersistenceData data = (TemplatePersistenceData) element;
			Template template= data.getTemplate();
			
			switch (columnIndex) {
				case 0:
					return template.getName();
				case 1:
					TemplateContextType type= fContextTypeRegistry.getContextType(template.getContextTypeId());
					if (type != null)
						return type.getName();
					else
						return template.getContextTypeId();
				case 2:
					return template.getDescription();
				default:
					return ""; //$NON-NLS-1$
			}
		}
	}


	/** Qualified key for formatter preference. */ 
	private static final String DEFAULT_FORMATTER_PREFERENCE_KEY= "org.eclipse.ui.texteditor.templates.preferences.format_templates"; //$NON-NLS-1$

	/** The table presenting the templates. */
	private CheckboxTableViewer fTableViewer;
	
	/* buttons */
	private Button fAddButton;
	private Button fEditButton;
	private Button fImportButton;
	private Button fExportButton;
	private Button fRemoveButton;
	private Button fRestoreButton;
	private Button fRevertButton;

	/** The viewer displays the pattern of selected template. */
	private SourceViewer fPatternViewer;
	/** Format checkbox. This gets conditionaly added. */
	private Button fFormatButton;
	/** The store for our templates. */
	private TemplateStore fTemplateStore;
	/** The context type registry. */
	private ContextTypeRegistry fContextTypeRegistry;

	
	/**
	 * Creates a new template preferenc page.
	 */
	protected TemplatePreferencePage() {
		super();
		
		setDescription(TextEditorTemplateMessages.getString("TemplatePreferencePage.message")); //$NON-NLS-1$
	}
	
	/**
	 * Returns the template store.
	 * 
	 * @return the template store
	 */
	public TemplateStore getTemplateStore() {
		return fTemplateStore;
	}
	
	/**
	 * Returns the context type registry.
	 * 
	 * @return the context type registry
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		return fContextTypeRegistry;
	}
	
	/**
	 * Sets the template store.
	 * 
	 * @param store the new template store
	 */
	public void setTemplateStore(TemplateStore store) {
		fTemplateStore= store;
	}
	
	/**
	 * Sets the context type registry.
	 * 
	 * @param registry the new context type registry
	 */
	public void setContextTypeRegistry(ContextTypeRegistry registry) {
		fContextTypeRegistry= registry;
	}
	
	/*
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite ancestor) {	
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		parent.setLayout(layout);				

        Composite innerParent= new Composite(parent, SWT.NONE);
        GridLayout innerLayout= new GridLayout();
        innerLayout.numColumns= 2;
        innerLayout.marginHeight= 0;
        innerLayout.marginWidth= 0;
        innerParent.setLayout(innerLayout);
        GridData gd= new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan= 2;
        innerParent.setLayoutData(gd);

		Table table= new Table(innerParent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(3);
		data.heightHint= convertHeightInCharsToPixels(10);
		table.setLayoutData(data);
				
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		

		TableLayout tableLayout= new TableLayout();
		table.setLayout(tableLayout);

		TableColumn column1= new TableColumn(table, SWT.NONE);		
		column1.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.column.name")); //$NON-NLS-1$

		TableColumn column2= new TableColumn(table, SWT.NONE);
		column2.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.column.context")); //$NON-NLS-1$
	
		TableColumn column3= new TableColumn(table, SWT.NONE);
		column3.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.column.description")); //$NON-NLS-1$
		
		fTableViewer= new CheckboxTableViewer(table);		
		fTableViewer.setLabelProvider(new TemplateLabelProvider());
		fTableViewer.setContentProvider(new TemplateContentProvider());

		fTableViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object object1, Object object2) {
				if ((object1 instanceof TemplatePersistenceData) && (object2 instanceof TemplatePersistenceData)) {
					Template left= ((TemplatePersistenceData) object1).getTemplate();
					Template right= ((TemplatePersistenceData) object2).getTemplate();
					int result= left.getName().compareToIgnoreCase(right.getName());
					if (result != 0)
						return result;
					return left.getDescription().compareToIgnoreCase(right.getDescription());
				}
				return super.compare(viewer, object1, object2);
			}
			
			public boolean isSorterProperty(Object element, String property) {
				return true;
			}
		});
		
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				edit();
			}
		});
		
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				selectionChanged1();
			}
		});

		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				TemplatePersistenceData d= (TemplatePersistenceData) event.getElement();
				d.setEnabled(event.getChecked());
			}
		});

		Composite buttons= new Composite(innerParent, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		buttons.setLayout(layout);
		
		fAddButton= new Button(buttons, SWT.PUSH);
		fAddButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.new")); //$NON-NLS-1$
		fAddButton.setLayoutData(getButtonGridData(fAddButton));
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				add();
			}
		});

		fEditButton= new Button(buttons, SWT.PUSH);
		fEditButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.edit")); //$NON-NLS-1$
		fEditButton.setLayoutData(getButtonGridData(fEditButton));
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				edit();
			}
		});

		fRemoveButton= new Button(buttons, SWT.PUSH);
		fRemoveButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.remove")); //$NON-NLS-1$
		fRemoveButton.setLayoutData(getButtonGridData(fRemoveButton));
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				remove();
			}
		});

		createSeparator(buttons);
				
		fRestoreButton= new Button(buttons, SWT.PUSH);
		fRestoreButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.restore")); //$NON-NLS-1$
		fRestoreButton.setLayoutData(getButtonGridData(fRestoreButton));
		fRestoreButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				restoreDeleted();
			}
		});

		fRevertButton= new Button(buttons, SWT.PUSH);
		fRevertButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.revert")); //$NON-NLS-1$
		fRevertButton.setLayoutData(getButtonGridData(fRevertButton));
		fRevertButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				revert();
			}
		});
		
		createSeparator(buttons);

		fImportButton= new Button(buttons, SWT.PUSH);
		fImportButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.import")); //$NON-NLS-1$
		fImportButton.setLayoutData(getButtonGridData(fImportButton));
		fImportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				import_();
			}
		});
		
		fExportButton= new Button(buttons, SWT.PUSH);
		fExportButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.export")); //$NON-NLS-1$
		fExportButton.setLayoutData(getButtonGridData(fExportButton));
		fExportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				export();
			}
		});

		fPatternViewer= doCreateViewer(parent);
		
		if (isShowFormatterSetting()) {
			fFormatButton= new Button(parent, SWT.CHECK);
			fFormatButton.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.use.code.formatter")); //$NON-NLS-1$
	        GridData gd1= new GridData();
	        gd1.horizontalSpan= 2;
	        fFormatButton.setLayoutData(gd1);
	        fFormatButton.setSelection(getPreferenceStore().getBoolean(getFormatterPreferenceKey()));
		}

		fTableViewer.setInput(fTemplateStore);
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());		

		updateButtons();
        configureTableResizing(innerParent, buttons, table, column1, column2, column3);
		
		Dialog.applyDialogFont(parent);		
		return parent;
	}
    
	/**
	 * Creates a separator between buttons
	 * @param parent
	 * @return
	 */
	private Label createSeparator(Composite parent) {
		Label separator= new Label(parent, SWT.NONE);
		separator.setVisible(false);
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.verticalAlignment= GridData.BEGINNING;
		gd.heightHint= 4;
		separator.setLayoutData(gd);
		return separator;
	}

	/**
	 * Returns whether the formatter preference checkbox should be shown.
	 * 
	 * @return <code>true</code> if the formatter preference checkbox should
	 *         be shown, <code>false</code> otherwise
	 */
	protected boolean isShowFormatterSetting() {
		return true;
	}

	/**
     * Correctly resizes the table so no phantom columns appear
     */
    private static void configureTableResizing(final Composite parent, final Composite buttons, final Table table, final TableColumn column1, final TableColumn column2, final TableColumn column3) {
        parent.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area= parent.getClientArea();
                Point preferredSize= table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width= area.width - 2 * table.getBorderWidth();
                if (preferredSize.y > area.height) {
                    // Subtract the scrollbar width from the total column width
                    // if a vertical scrollbar will be required
                    Point vBarSize = table.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }
                width -= buttons.getSize().x;
                Point oldSize= table.getSize();
                if (oldSize.x > width) {
                    // table is getting smaller so make the columns
                    // smaller first and then resize the table to
                    // match the client area width
                    column1.setWidth(width/4);
                    column2.setWidth(width/4);
                    column3.setWidth(width - (column1.getWidth() + column2.getWidth()));
                    table.setSize(width, area.height);
                } else {
                    // table is getting bigger so make the table
                    // bigger first and then make the columns wider
                    // to match the client area width
                    table.setSize(width, area.height);
                    column1.setWidth(width / 4);
                    column2.setWidth(width / 4);
                    column3.setWidth(width - (column1.getWidth() + column2.getWidth()));
                 }
            }
        });
    }
    
	
	private TemplatePersistenceData[] getEnabledTemplates() {
		List enabled= new ArrayList();
		TemplatePersistenceData[] datas= fTemplateStore.getTemplateData(false);
		for (int i= 0; i < datas.length; i++) {
			if (datas[i].isEnabled())
				enabled.add(datas[i]);
		}
		return (TemplatePersistenceData[]) enabled.toArray(new TemplatePersistenceData[enabled.size()]);
	}
	
	private SourceViewer doCreateViewer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.preview")); //$NON-NLS-1$
		GridData data= new GridData();
		data.horizontalSpan= 2;
		label.setLayoutData(data);
		
		SourceViewer viewer= createViewer(parent);
		viewer.setEditable(false);
	
		Control control= viewer.getControl();
		data= new GridData(GridData.FILL_BOTH);
		data.horizontalSpan= 2;
		data.heightHint= convertHeightInCharsToPixels(5);
		control.setLayoutData(data);
		
		return viewer;
	}
	
	/**
	 * Creates, configures and returns a source viewer to present the template
	 * pattern on the preference page. Clients may override to provide a custom
	 * source viewer featuring e.g. syntax coloring.
	 * 
	 * @param parent the parent control
	 * @return a configured source viewer
	 */
	protected SourceViewer createViewer(Composite parent) {
		SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		SourceViewerConfiguration configuration= new SourceViewerConfiguration();
		viewer.configure(configuration);
		IDocument document= new Document();
		viewer.setDocument(document);
		return viewer;
	}

	private static GridData getButtonGridData(Button button) {
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		// TODO replace SWTUtil
//		data.widthHint= SWTUtil.getButtonWidthHint(button);
//		data.heightHint= SWTUtil.getButtonHeightHint(button);
	
		return data;
	}
	
	private void selectionChanged1() {		
		updateViewerInput();
		
		updateButtons();
	}
	
	/**
	 * Updates the pattern viewer.
	 */
	protected void updateViewerInput() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		if (selection.size() == 1) {
			TemplatePersistenceData data= (TemplatePersistenceData) selection.getFirstElement();
			Template template= data.getTemplate();
			fPatternViewer.getDocument().set(template.getPattern());
		} else {		
			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}
	}

	/**
	 * Updates the buttons.
	 */
	protected void updateButtons() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();
		int selectionCount= selection.size();
		int itemCount= fTableViewer.getTable().getItemCount();
		boolean canRestore= fTemplateStore.getTemplateData(true).length != fTemplateStore.getTemplateData(false).length;
		boolean canRevert= false;
		for (Iterator it= selection.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isModified()) {
				canRevert= true;
				break;
			}
		}
		
		fEditButton.setEnabled(selectionCount == 1);
		fExportButton.setEnabled(selectionCount > 0);
		fRemoveButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
		fRestoreButton.setEnabled(canRestore);
		fRevertButton.setEnabled(canRevert);
	}
	
	private void add() {		
		
		Template template= new Template();
		Iterator it= fContextTypeRegistry.contextTypes();
		if (it.hasNext()) {
			template.setContextTypeId(((TemplateContextType) it.next()).getId());
			
			Dialog dialog= createTemplateEditDialog(template, false, true);
			if (dialog.open() == Window.OK) {
				TemplatePersistenceData data= new TemplatePersistenceData(template, true);
				fTemplateStore.add(data);
				fTableViewer.refresh();
				fTableViewer.setChecked(data, true);
				fTableViewer.setSelection(new StructuredSelection(data));			
			}
		}
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a 
	 * custom dialog.
	 * 
	 * @param template the template being edited
	 * @param edit whether the dialog should be editable
	 * @param isNameModifiable whether the template name may be modified
	 * @return an <code>EditTemplateDialog</code> which will be opened.
	 */
	protected Dialog createTemplateEditDialog(Template template, boolean edit, boolean isNameModifiable) {
		return new EditTemplateDialog(getShell(), template, edit, isNameModifiable, fContextTypeRegistry);
	}

	private void edit() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		Object[] objects= selection.toArray();		
		if ((objects == null) || (objects.length != 1))
			return;
		
		TemplatePersistenceData data= (TemplatePersistenceData) selection.getFirstElement();
		edit(data);
	}

	private void edit(TemplatePersistenceData data) {
		Template oldTemplate= data.getTemplate();
		Template newTemplate= new Template(oldTemplate);
		Dialog dialog= createTemplateEditDialog(newTemplate, true, true);
		if (dialog.open() == Window.OK) {

			if (!newTemplate.getName().equals(oldTemplate.getName()) &&
				MessageDialog.openQuestion(getShell(),
				TextEditorTemplateMessages.getString("TemplatePreferencePage.question.create.new.title"), //$NON-NLS-1$
				TextEditorTemplateMessages.getString("TemplatePreferencePage.question.create.new.message"))) //$NON-NLS-1$
			{
				data= new TemplatePersistenceData(newTemplate, true);
				fTemplateStore.add(data);
				fTableViewer.refresh();
			} else {
				data.setTemplate(newTemplate);
				fTableViewer.refresh(data);
			}
			selectionChanged1();
			fTableViewer.setChecked(data, data.isEnabled());
			fTableViewer.setSelection(new StructuredSelection(data));			
		}
	}
		
	private void import_() {
		FileDialog dialog= new FileDialog(getShell());
		dialog.setText(TextEditorTemplateMessages.getString("TemplatePreferencePage.import.title")); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] {TextEditorTemplateMessages.getString("TemplatePreferencePage.import.extension")}); //$NON-NLS-1$
		String path= dialog.open();
		
		if (path == null)
			return;
		
		try {
			TemplateReaderWriter reader= new TemplateReaderWriter();
			File file= new File(path);
			if (file.exists()) {
				InputStream input= new BufferedInputStream(new FileInputStream(file));
				TemplatePersistenceData[] datas= reader.read(input, null);
				for (int i= 0; i < datas.length; i++) {
					TemplatePersistenceData data= datas[i];
					fTemplateStore.add(data);
				}
			}
			
			fTableViewer.refresh();
			fTableViewer.setAllChecked(false);
			fTableViewer.setCheckedElements(getEnabledTemplates());

		} catch (FileNotFoundException e) {
			openReadErrorDialog(e);
		} catch (IOException e) {
			openReadErrorDialog(e);
		}
	}
	
	private void export() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();
		Object[] templates= selection.toArray();

		TemplatePersistenceData[] datas= new TemplatePersistenceData[templates.length];
		for (int i= 0; i != templates.length; i++)
			datas[i]= (TemplatePersistenceData) templates[i];
		
		export(datas);
	}
	
	private void export(TemplatePersistenceData[] templates) {
		FileDialog dialog= new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(TextEditorTemplateMessages.getFormattedString("TemplatePreferencePage.export.title", new Integer(templates.length))); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] {TextEditorTemplateMessages.getString("TemplatePreferencePage.export.extension")}); //$NON-NLS-1$
		dialog.setFileName(TextEditorTemplateMessages.getString("TemplatePreferencePage.export.filename")); //$NON-NLS-1$
		String path= dialog.open();
		
		if (path == null)
			return;
		
		File file= new File(path);		

		if (file.isHidden()) {
			String title= TextEditorTemplateMessages.getString("TemplatePreferencePage.export.error.title"); //$NON-NLS-1$ 
			String message= TextEditorTemplateMessages.getFormattedString("TemplatePreferencePage.export.error.hidden", file.getAbsolutePath()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
			return;
		}
		
		if (file.exists() && !file.canWrite()) {
			String title= TextEditorTemplateMessages.getString("TemplatePreferencePage.export.error.title"); //$NON-NLS-1$
			String message= TextEditorTemplateMessages.getFormattedString("TemplatePreferencePage.export.error.canNotWrite", file.getAbsolutePath()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (!file.exists() || confirmOverwrite(file)) {
			try {
				OutputStream output= new BufferedOutputStream(new FileOutputStream(file));
				TemplateReaderWriter writer= new TemplateReaderWriter();
				writer.save(templates, output);
			} catch (Exception e) {
				openWriteErrorDialog(e);
			}		
		}
	}

	private boolean confirmOverwrite(File file) {
		return MessageDialog.openQuestion(getShell(),
			TextEditorTemplateMessages.getString("TemplatePreferencePage.export.exists.title"), //$NON-NLS-1$
			TextEditorTemplateMessages.getFormattedString("TemplatePreferencePage.export.exists.message", file.getAbsolutePath())); //$NON-NLS-1$
	}
	
	private void remove() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		Iterator elements= selection.iterator();
		while (elements.hasNext()) {
			TemplatePersistenceData data= (TemplatePersistenceData) elements.next();
			fTemplateStore.delete(data);
		}

		fTableViewer.refresh();
	}
	
	private void restoreDeleted() {
		fTemplateStore.restoreDeleted();
		fTableViewer.refresh();
		fTableViewer.setCheckedElements(getEnabledTemplates());
		updateButtons();
	}
	
	private void revert() {
		IStructuredSelection selection= (IStructuredSelection) fTableViewer.getSelection();

		Iterator elements= selection.iterator();
		while (elements.hasNext()) {
			TemplatePersistenceData data= (TemplatePersistenceData) elements.next();
			data.revert();
		}

		fTableViewer.refresh();
		selectionChanged1();
		fTableViewer.setChecked(getEnabledTemplates(), true);
	}
	
	/*
	 * @see Control#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setTitle(TextEditorTemplateMessages.getString("TemplatePreferencePage.title")); //$NON-NLS-1$
	}
	
	/*
	 * @see PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		if (isShowFormatterSetting()) {
			IPreferenceStore prefs= getPreferenceStore();
			fFormatButton.setSelection(prefs.getDefaultBoolean(getFormatterPreferenceKey()));
		}

		fTemplateStore.restoreDefaults();
		
		// refresh
		fTableViewer.refresh();
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());		
	}

	/*
	 * @see PreferencePage#performOk()
	 */	
	public boolean performOk() {
		if (isShowFormatterSetting()) {
			IPreferenceStore prefs= getPreferenceStore();
			prefs.setValue(getFormatterPreferenceKey(), fFormatButton.getSelection());
		}
		
		try {
			fTemplateStore.save();
		} catch (IOException e) {
			openWriteErrorDialog(e);
		}

		return super.performOk();
	}	
	
	/**
	 * Returns the key to use for the formatter preference.
	 * @return
	 */
	protected String getFormatterPreferenceKey() {
		return DEFAULT_FORMATTER_PREFERENCE_KEY;
	}

	/*
	 * @see PreferencePage#performCancel()
	 */
	public boolean performCancel() {
		try {
			fTemplateStore.load();
		} catch (IOException e) {
			openReadErrorDialog(e);
			return false;
		}
		return super.performCancel();
	}
	
	private void openReadErrorDialog(Exception e) {
		String title= TextEditorTemplateMessages.getString("TemplatePreferencePage.error.read.title"); //$NON-NLS-1$
		String message= TextEditorTemplateMessages.getString("TemplatePreferencePage.error.read.message"); //$NON-NLS-1$
		MessageDialog.openError(getShell(), title, message);
	}
	
	private void openWriteErrorDialog(Exception e) {
		String title= TextEditorTemplateMessages.getString("TemplatePreferencePage.error.write.title"); //$NON-NLS-1$
		String message= TextEditorTemplateMessages.getString("TemplatePreferencePage.error.write.message"); //$NON-NLS-1$
		MessageDialog.openError(getShell(), title, message);		
	}
	
	protected SourceViewer getViewer() {
		return fPatternViewer;
	}
	
	protected TableViewer getTableViewer() {
		return fTableViewer;
	}
}
