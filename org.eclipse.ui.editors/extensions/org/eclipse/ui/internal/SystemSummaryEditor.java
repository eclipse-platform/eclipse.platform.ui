package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * The <code>SystemSummaryEditor</code> displays diagnostics information 
 * about the Eclipse platform in which it is running.
 */
public class SystemSummaryEditor extends AbstractTextEditor {
	/*
	 * The Editor ID
	 */
	static final String ID= WorkbenchPlugin.getDefault().getDescriptor().getUniqueIdentifier() + ".SystemSummaryEditor"; //$NON-NLS-1$
	
	/*
	 * The name of the dialog setting property used to store
	 * the last file the user selected to write to.
	 */
	public static final String LAST_FILE="last_file"; //$NON-NLS-1$
	
	
	/**
	 * Creates a new text editor.
	 */
	public SystemSummaryEditor() {
		super();
		setDocumentProvider(new SystemSummaryDocumentProvider());
	}
		
	/**
	 * Allow save so that "save as" is enabled.
	 * 
	 * @see AbstractTextEditor#isSaveAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/**
	 * Disallow editing.
	 * 
	 * @see AbstractTextEditor#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}
	
	/**
	 * Saves the contents of the editor to a file system file.
	 * 
	 * @see AbstractTextEditor#performSaveAs(IProgressMonitor)
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		String filename= getFilename();
		if (filename == null)
			return;
		String contents= getDocumentProvider().getDocument(getEditorInput()).get();
		
		Writer writer = null;
		try {
			writer = 
				new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, false), "UTF-8"));//$NON-NLS-1$
			writer.write(contents);
			writer.close();
			writer = null;
		} catch (IOException ioe) {
			MessageDialog.openError(
				getSite().getShell(),
				WorkbenchMessages.getString("SystemSummary.saveErrorTitle"),
				WorkbenchMessages.format("SystemSummary.saveErrorMessage", new Object[]{filename, ioe.getMessage() == null ? "" : ioe.getMessage()}));
		}
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				// silent
			}
		}
		
	}		

	/*
	 * Prompts the user to select a file
	 */
	private String getFilename() {
		String lastUsedPath= getDialogSettings().get(LAST_FILE);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog= new FileDialog(getEditorSite().getShell(), SWT.SINGLE);
		dialog.setText(WorkbenchMessages.getString("SystemSummary.dialogTitle")); //$NON-NLS-1$
		dialog.setFilterPath(lastUsedPath);
		String filename= dialog.open();
		if (filename != null)
			getDialogSettings().put(LAST_FILE, filename);
		return filename;
	}

	/*
	 * Answers the dialog settings for the DiagnosticsPrinterPlugin plugin
	 */	
	private IDialogSettings getDialogSettings() {
		return WorkbenchPlugin.getDefault().getDialogSettings();
	}
	
	/**
	 * Override the default implementation to only show 
	 * copy, select all, print, find and goto line.
	 * 
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_COPY));
		addAction(menu, ITextEditorActionConstants.COPY);
		addAction(menu, ITextEditorActionConstants.SELECT_ALL);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_PRINT));
		addAction(menu, ITextEditorActionConstants.PRINT);
		
		menu.add(new Separator(ITextEditorActionConstants.GROUP_FIND));
		addAction(menu, ITextEditorActionConstants.FIND);
		addAction(menu, ITextEditorActionConstants.GOTO_LINE);

		menu.add(new Separator(ITextEditorActionConstants.MB_ADDITIONS));
	}
	
	
	
	
}
