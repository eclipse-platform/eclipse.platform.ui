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
package org.eclipse.ui.texteditor.templates;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.internal.texteditor.NLSUtility;

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
					return template.getContextTypeId();
				case 2:
					return template.getDescription();
				case 3:
					return template.isAutoInsertable() ? TextEditorTemplateMessages.TemplatePreferencePage_on : "";  //$NON-NLS-1$
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
	/** Format checkbox. This gets conditionally added. */
	private Button fFormatButton;
	/** The store for our templates. */
	private TemplateStore fTemplateStore;
	/** The context type registry. */
	private ContextTypeRegistry fContextTypeRegistry;


	/**
	 * Creates a new template preference page.
	 */
	protected TemplatePreferencePage() {
		super();

		setDescription(TextEditorTemplateMessages.TemplatePreferencePage_message);
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

        Composite tableComposite= new Composite(innerParent, SWT.NONE);
        GridData data= new GridData(GridData.FILL_BOTH);
        data.widthHint= 360;
        data.heightHint= convertHeightInCharsToPixels(10);
        tableComposite.setLayoutData(data);
        
        ColumnLayout columnLayout= new ColumnLayout();
        tableComposite.setLayout(columnLayout);
		Table table= new Table(tableComposite, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GC gc= new GC(tableComposite.getDisplay());
		gc.setFont(JFaceResources.getDialogFont());
		
		TableColumn column1= new TableColumn(table, SWT.NONE);
		column1.setText(TextEditorTemplateMessages.TemplatePreferencePage_column_name);
		int minWidth= computeMinimumColumnWidth(gc, TextEditorTemplateMessages.TemplatePreferencePage_column_name);
		columnLayout.addColumnData(new ColumnWeightData(2, minWidth, true));

		TableColumn column2= new TableColumn(table, SWT.NONE);
		column2.setText(TextEditorTemplateMessages.TemplatePreferencePage_column_context);
		minWidth= computeMinimumColumnWidth(gc, TextEditorTemplateMessages.TemplatePreferencePage_column_context);
		columnLayout.addColumnData(new ColumnWeightData(1, minWidth, true));

		TableColumn column3= new TableColumn(table, SWT.NONE);
		column3.setText(TextEditorTemplateMessages.TemplatePreferencePage_column_description);
		minWidth= computeMinimumColumnWidth(gc, TextEditorTemplateMessages.TemplatePreferencePage_column_description);
		columnLayout.addColumnData(new ColumnWeightData(3, minWidth, true));

		TableColumn column4= new TableColumn(table, SWT.NONE);
		column4.setAlignment(SWT.CENTER);
		column4.setText(TextEditorTemplateMessages.TemplatePreferencePage_column_autoinsert);
		minWidth= computeMinimumColumnWidth(gc, TextEditorTemplateMessages.TemplatePreferencePage_column_autoinsert);
		minWidth= Math.max(minWidth, computeMinimumColumnWidth(gc, TextEditorTemplateMessages.TemplatePreferencePage_on));
		columnLayout.addColumnData(new ColumnPixelData(minWidth, false, false));
		column4.setResizable(false);
		
		gc.dispose();

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
		fAddButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_new);
		fAddButton.setLayoutData(getButtonGridData(fAddButton));
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				add();
			}
		});

		fEditButton= new Button(buttons, SWT.PUSH);
		fEditButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_edit);
		fEditButton.setLayoutData(getButtonGridData(fEditButton));
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				edit();
			}
		});

		fRemoveButton= new Button(buttons, SWT.PUSH);
		fRemoveButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_remove);
		fRemoveButton.setLayoutData(getButtonGridData(fRemoveButton));
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				remove();
			}
		});

		createSeparator(buttons);

		fRestoreButton= new Button(buttons, SWT.PUSH);
		fRestoreButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_restore);
		fRestoreButton.setLayoutData(getButtonGridData(fRestoreButton));
		fRestoreButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				restoreDeleted();
			}
		});

		fRevertButton= new Button(buttons, SWT.PUSH);
		fRevertButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_revert);
		fRevertButton.setLayoutData(getButtonGridData(fRevertButton));
		fRevertButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				revert();
			}
		});

		createSeparator(buttons);

		fImportButton= new Button(buttons, SWT.PUSH);
		fImportButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_import);
		fImportButton.setLayoutData(getButtonGridData(fImportButton));
		fImportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				import_();
			}
		});

		fExportButton= new Button(buttons, SWT.PUSH);
		fExportButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_export);
		fExportButton.setLayoutData(getButtonGridData(fExportButton));
		fExportButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				export();
			}
		});

		fPatternViewer= doCreateViewer(parent);

		if (isShowFormatterSetting()) {
			fFormatButton= new Button(parent, SWT.CHECK);
			fFormatButton.setText(TextEditorTemplateMessages.TemplatePreferencePage_use_code_formatter);
	        GridData gd1= new GridData();
	        gd1.horizontalSpan= 2;
	        fFormatButton.setLayoutData(gd1);
	        fFormatButton.setSelection(getPreferenceStore().getBoolean(getFormatterPreferenceKey()));
		}

		fTableViewer.setInput(fTemplateStore);
		fTableViewer.setAllChecked(false);
		fTableViewer.setCheckedElements(getEnabledTemplates());

		updateButtons();
		Dialog.applyDialogFont(parent);
		innerParent.layout();
		
		return parent;
	}

	private int computeMinimumColumnWidth(GC gc, String string) {
		return gc.stringExtent(string).x + 10; // pad 10 to accomodate table header trimmings
	}

	/**
	 * Creates a separator between buttons
	 * @param parent
	 * @return a separator
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
		label.setText(TextEditorTemplateMessages.TemplatePreferencePage_preview);
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

		Iterator it= fContextTypeRegistry.contextTypes();
		if (it.hasNext()) {
			Template template= new Template("", "", ((TemplateContextType) it.next()).getId(), "", true);   //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			Template newTemplate= editTemplate(template, false, true);
			if (newTemplate != null) {
				TemplatePersistenceData data= new TemplatePersistenceData(newTemplate, true);
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
	 * @deprecated not called any longer as of 3.1 - use {@link #editTemplate(Template, boolean, boolean)}
	 */
	protected Dialog createTemplateEditDialog(Template template, boolean edit, boolean isNameModifiable) {
		return new EditTemplateDialog(getShell(), template, edit, isNameModifiable, fContextTypeRegistry);
	}

	/**
	 * Creates the edit dialog. Subclasses may override this method to provide a
	 * custom dialog.
	 *
	 * @param template the template being edited
	 * @param edit whether the dialog should be editable
	 * @param isNameModifiable whether the template name may be modified
	 * @return the created or modified template, or <code>null</code> if the edition failed
	 * @since 3.1
	 */
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, edit, isNameModifiable, fContextTypeRegistry);
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
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
		Template newTemplate= editTemplate(new Template(oldTemplate), true, true);
		if (newTemplate != null) {

			if (!newTemplate.getName().equals(oldTemplate.getName()) &&
				MessageDialog.openQuestion(getShell(),
				TextEditorTemplateMessages.TemplatePreferencePage_question_create_new_title,
				TextEditorTemplateMessages.TemplatePreferencePage_question_create_new_message))
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
		dialog.setText(TextEditorTemplateMessages.TemplatePreferencePage_import_title);
		dialog.setFilterExtensions(new String[] {TextEditorTemplateMessages.TemplatePreferencePage_import_extension});
		String path= dialog.open();

		if (path == null)
			return;
		
		IFileStore fileStore= EFS.getLocalFileSystem().getStore(new Path(path));

		try {
			TemplateReaderWriter reader= new TemplateReaderWriter();
			if (fileStore.fetchInfo().exists()) {
				InputStream input= new BufferedInputStream(fileStore.openInputStream(EFS.NONE, null));
				try {
					TemplatePersistenceData[] datas= reader.read(input, null);
					for (int i= 0; i < datas.length; i++) {
						TemplatePersistenceData data= datas[i];
						fTemplateStore.add(data);
					}
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException x) {
							// ignore
						}
					}
				}
			}

			fTableViewer.refresh();
			fTableViewer.setAllChecked(false);
			fTableViewer.setCheckedElements(getEnabledTemplates());

		} catch (CoreException e) {
			openReadErrorDialog();
		} catch (IOException e) {
			openReadErrorDialog();
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
		dialog.setText(NLSUtility.format(TextEditorTemplateMessages.TemplatePreferencePage_export_title, new Integer(templates.length)));
		dialog.setFilterExtensions(new String[] {TextEditorTemplateMessages.TemplatePreferencePage_export_extension});
		dialog.setFileName(TextEditorTemplateMessages.TemplatePreferencePage_export_filename);
		String path= dialog.open();

		if (path == null)
			return;

		IFileStore fileStore= EFS.getLocalFileSystem().getStore(new Path(path));
		IFileInfo fileInfo= fileStore.fetchInfo();

		if (new File(fileStore.toURI()).isHidden()) {
			String title= TextEditorTemplateMessages.TemplatePreferencePage_export_error_title;
			String message= NLSUtility.format(TextEditorTemplateMessages.TemplatePreferencePage_export_error_hidden, fileStore.toString());
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (fileInfo.exists() && fileInfo.getAttribute(EFS.ATTRIBUTE_READ_ONLY)) {
			String title= TextEditorTemplateMessages.TemplatePreferencePage_export_error_title;
			String message= NLSUtility.format(TextEditorTemplateMessages.TemplatePreferencePage_export_error_canNotWrite, fileStore.toString());
			MessageDialog.openError(getShell(), title, message);
			return;
		}

		if (!fileInfo.exists() || confirmOverwrite(fileStore)) {
			OutputStream output= null;
			try {
				output= new BufferedOutputStream(fileStore.openOutputStream(EFS.NONE, null));
				TemplateReaderWriter writer= new TemplateReaderWriter();
				writer.save(templates, output);
			} catch (CoreException e) {
				openWriteErrorDialog();
			} catch (IOException e) {
				openWriteErrorDialog();
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}
	}

	private boolean confirmOverwrite(IFileStore fileStore) {
		return MessageDialog.openQuestion(getShell(),
			TextEditorTemplateMessages.TemplatePreferencePage_export_exists_title,
			NLSUtility.format(TextEditorTemplateMessages.TemplatePreferencePage_export_exists_message, fileStore.toString()));
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
			setTitle(TextEditorTemplateMessages.TemplatePreferencePage_title);
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
			openWriteErrorDialog();
		}

		return super.performOk();
	}

	/**
	 * Returns the key to use for the formatter preference.
	 *
	 * @return the formatter preference key
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
			openReadErrorDialog();
			return false;
		}
		return super.performCancel();
	}

	private void openReadErrorDialog() {
		String title= TextEditorTemplateMessages.TemplatePreferencePage_error_read_title;
		String message= TextEditorTemplateMessages.TemplatePreferencePage_error_read_message;
		MessageDialog.openError(getShell(), title, message);
	}

	private void openWriteErrorDialog() {
		String title= TextEditorTemplateMessages.TemplatePreferencePage_error_write_title;
		String message= TextEditorTemplateMessages.TemplatePreferencePage_error_write_message;
		MessageDialog.openError(getShell(), title, message);
	}

	protected SourceViewer getViewer() {
		return fPatternViewer;
	}

	protected TableViewer getTableViewer() {
		return fTableViewer;
	}
}
