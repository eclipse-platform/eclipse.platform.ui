package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;



/**
 * The standard text editor for file resources (<code>IFile</code>).
 * <p>
 * This editor has id <code>"com.ibm.eclipse.ui.DefaultTextEditor"</code>.
 * The editor's context menu has id <code>#TextEditorContext</code>.
 * The editor's ruler context menu has id <code>#TextRulerContext</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when the default 
 * editor is needed for a workbench window. This class was not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public class TextEditor extends AbstractTextEditor {
	
	/** The resource bundle for text editors */
	private ResourceBundle fResourceBundle;
	
	/**
	 * Creates a new text editor.
	 */
	public TextEditor() {
		super();
		initializeEditor();
	}
	/**
	 * The <code>TextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method asks the user for the workspace path
	 * of a file resource and saves the document there.
	 */
	public void doSaveAs() {
		
		Shell shell= getSite().getShell();
		
		SaveAsDialog dialog= new SaveAsDialog(shell);
		dialog.open();
		IPath path= dialog.getResult();
		
		if (path == null)
			return;
			
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(path);
		final IEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				getDocumentProvider().saveDocument(monitor, newInput, getDocumentProvider().getDocument(getEditorInput()), false);
			}
		};
		
		boolean success= false;
		try {
			
			getDocumentProvider().aboutToChange(newInput);
			new ProgressMonitorDialog(shell).run(false, true, op);
			success= true;
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			String title= getResourceString("Error.save_as.title", "Error.save_as.title");
			String msg= getResourceString("Error.save_as.message", "Error.save_as.message");
			MessageDialog.openError(shell, title, msg + x.getTargetException().getMessage());
		} finally {
			getDocumentProvider().changed(newInput);
			if (success)
				setInput(newInput);
		}
	}
	/** 
	 * Returns the editor's resource bundle.
	 *
	 * @return the editor's resource bundle
	 */
	private ResourceBundle getResourceBundle() {
		if (fResourceBundle == null)
			fResourceBundle= ResourceBundle.getBundle("org.eclipse.ui.editors.text.TextEditorResources");
		return fResourceBundle;
	}
	/**
	 * Convenience method for safely accessing resources.
	 */
	private String getResourceString(String key, String dfltValue) {
		try {
			if (fResourceBundle != null && key != null)
				return fResourceBundle.getString(key);
		} catch (MissingResourceException x) {
		}
		return dfltValue;
	}
	/**
	 * Initializes this editor.
	 */
	protected void initializeEditor() {
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#TextEditorContext");
		setRulerContextMenuId("#TextRulerContext");
		
		Plugin plugin= Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin= (AbstractUIPlugin) plugin;		
			setPreferenceStore(uiPlugin.getPreferenceStore());
		}
	}
	/**
	 * The <code>TextEditor</code> implementation of this 
	 * <code>IEditorPart</code> method returns <code>true</code>.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
}
