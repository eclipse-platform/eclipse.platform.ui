package org.eclipse.ui.editors.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ui.texteditor.ConvertLineDelimitersAction;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.StatusTextEditor;



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
public class TextEditor extends StatusTextEditor {
	
	private DefaultEncodingSupport fEncodingSupport;
		
	/**
	 * Creates a new text editor.
	 */
	public TextEditor() {
		super();
		
		setRangeIndicator(new DefaultRangeIndicator());
		setEditorContextMenuId("#TextEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#TextRulerContext"); //$NON-NLS-1$
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		
		Plugin plugin= Platform.getPlugin(PlatformUI.PLUGIN_ID);
		if (plugin instanceof AbstractUIPlugin) {
			AbstractUIPlugin uiPlugin= (AbstractUIPlugin) plugin;		
			setPreferenceStore(uiPlugin.getPreferenceStore());
		}
	}
	
	/*
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (fEncodingSupport != null) {
				fEncodingSupport.dispose();
				fEncodingSupport= null;
		}
		super.dispose();
	}
	
	/**
	 * The <code>TextEditor</code> implementation of this 
	 * <code>AbstractTextEditor</code> method asks the user for the workspace path
	 * of a file resource and saves the document there.
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		
		Shell shell= getSite().getShell();
		
		SaveAsDialog dialog= new SaveAsDialog(shell);
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput)
			dialog.setOriginalFile(((IFileEditorInput) input).getFile());
		dialog.open();
		IPath path= dialog.getResult();
		
		if (path == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(path);
		final IEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				getDocumentProvider().saveDocument(monitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
			}
		};
		
		boolean success= false;
		try {
			
			getDocumentProvider().aboutToChange(newInput);
			new ProgressMonitorDialog(shell).run(false, true, op);
			success= true;
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			String title= TextEditorMessages.getString("Editor.error.save.title"); //$NON-NLS-1$
			String msg= MessageFormat.format(TextEditorMessages.getString("Editor.error.save.message"), new Object[] { x.getTargetException().getMessage() }); //$NON-NLS-1$
			MessageDialog.openError(shell, title, msg);
		} finally {
			getDocumentProvider().changed(newInput);
			if (success)
				setInput(newInput);
		}
		
		if (progressMonitor != null)
			progressMonitor.setCanceled(!success);
	}
	
	/*
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/*
	 * @see AbstractTextEditor#createActions()
	 */
	protected void createActions() {
		super.createActions();

		ResourceAction action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToWindows.", this, "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_WINDOWS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_WINDOWS);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS, action);

		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToUNIX.", this, "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_UNIX);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_UNIX);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX, action);
		
		action= new ConvertLineDelimitersAction(TextEditorMessages.getResourceBundle(), "Editor.ConvertToMac.", this, "\r"); //$NON-NLS-1$ //$NON-NLS-2$
		action.setHelpContextId(IAbstractTextEditorHelpContextIds.CONVERT_LINE_DELIMITERS_TO_MAC);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONVERT_LINE_DELIMITERS_TO_MAC);
		setAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC, action);
		
//		markAsContentDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_WINDOWS, true);
//		markAsContentDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_UNIX, true);
//		markAsContentDependentAction(ITextEditorActionConstants.CONVERT_LINE_DELIMITERS_TO_MAC, true);
		
		fEncodingSupport= new DefaultEncodingSupport();
		fEncodingSupport.initialize(this);
	}
	
	/*
	 * @see StatusTextEditor#getStatusHeader(IStatus)
	 */
	protected String getStatusHeader(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusHeader(status);
			if (message != null)
				return message;
		}
		return super.getStatusHeader(status);
	}
	
	/*
	 * @see StatusTextEditor#getStatusBanner(IStatus)
	 */
	protected String getStatusBanner(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusBanner(status);
			if (message != null)
				return message;
		}
		return super.getStatusBanner(status);
	}
	
	/*
	 * @see StatusTextEditor#getStatusMessage(IStatus)
	 */
	protected String getStatusMessage(IStatus status) {
		if (fEncodingSupport != null) {
			String message= fEncodingSupport.getStatusMessage(status);
			if (message != null)
				return message;
		}
		return super.getStatusMessage(status);
	}
	
	/*
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}
	
	/*
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IEncodingSupport.class.equals(adapter))
			return fEncodingSupport;
		return super.getAdapter(adapter);
	}
	
	/*
	 * @see AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		
		super.editorContextMenuAboutToShow(menu);
		
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
		
		String label= TextEditorMessages.getString("Editor.AddMenu.label"); //$NON-NLS-1$
		MenuManager submenu= new MenuManager(label, ITextEditorActionConstants.GROUP_ADD);
		addAction(submenu, ITextEditorActionConstants.BOOKMARK);
		addAction(submenu, ITextEditorActionConstants.ADD_TASK);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_ADD, submenu);
	}
}
