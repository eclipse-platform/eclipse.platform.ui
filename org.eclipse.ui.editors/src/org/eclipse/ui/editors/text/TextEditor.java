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
package org.eclipse.ui.editors.text;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;


/**
 * The standard text editor for file resources (<code>IFile</code>).
 * <p>
 * This editor has id <code>"org.eclipse.ui.DefaultTextEditor"</code>.
 * The editor's context menu has id <code>#TextEditorContext</code>.
 * The editor's ruler context menu has id <code>#TextRulerContext</code>.
 * </p>
 * <p>
 * The workbench will automatically instantiate this class when the default
 * editor is needed for a workbench window.
 * </p>
 */
public class TextEditor extends AbstractDecoratedTextEditor {

	/**
	 * The encoding support for the editor.
	 * @since 2.0
	 */
	protected DefaultEncodingSupport fEncodingSupport;


	/**
	 * Creates a new text editor.
	 */
	public TextEditor() {
		super();
		if (getSourceViewerConfiguration() == null) {
			// configuration not yet set by subclass
			setSourceViewerConfiguration(new TextSourceViewerConfiguration(getPreferenceStore()));
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * This method configures the editor but does not define a
	 * <code>SourceViewerConfiguration</code>. When only interested in
	 * providing a custom source viewer configuration, subclasses may extend
	 * this method.
	 */
	protected void initializeEditor() {
		setEditorContextMenuId("#TextEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#TextRulerContext"); //$NON-NLS-1$
		setHelpContextId(ITextEditorHelpContextIds.TEXT_EDITOR);
		setPreferenceStore(EditorsPlugin.getDefault().getPreferenceStore());
		configureInsertMode(SMART_INSERT, false);
		setInsertMode(INSERT);
	}

	/*
	 * @see IWorkbenchPart#dispose()
	 * @since 2.0
	 */
	public void dispose() {
		if (fEncodingSupport != null) {
			fEncodingSupport.dispose();
			fEncodingSupport= null;
		}

		super.dispose();
	}

	/**
	 * Installs the encoding support on the given text editor.
	 * <p>
 	 * Subclasses may override to install their own encoding
 	 * support or to disable the default encoding support.
 	 * </p>
	 * @since 2.1
	 */
	protected void installEncodingSupport() {
		fEncodingSupport= new DefaultEncodingSupport();
		fEncodingSupport.initialize(this);
	}

	/**
	 * The <code>TextEditor</code> implementation of this  <code>AbstractTextEditor</code>
	 * method asks the user for the workspace path of a file resource and saves the document there.
	 *
	 * @param progressMonitor the progress monitor to be used
	 */
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell= getSite().getShell();
		IEditorInput input= getEditorInput();

		SaveAsDialog dialog= new SaveAsDialog(shell);

		IFile original= (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
		if (original != null)
			dialog.setOriginalFile(original);

		dialog.create();

		IDocumentProvider provider= getDocumentProvider();
		if (provider == null) {
			// editor has programmatically been  closed while the dialog was open
			return;
		}

		if (provider.isDeleted(input) && original != null) {
			String message= MessageFormat.format(TextEditorMessages.Editor_warning_save_delete, new Object[] { original.getName() });
			dialog.setErrorMessage(null);
			dialog.setMessage(message, IMessageProvider.WARNING);
		}

		if (dialog.open() == Window.CANCEL) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		IPath filePath= dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}

		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IFile file= workspace.getRoot().getFile(filePath);
		final IEditorInput newInput= new FileEditorInput(file);

		boolean success= false;
		try {

			provider.aboutToChange(newInput);
			provider.saveDocument(progressMonitor, newInput, provider.getDocument(input), true);
			success= true;

		} catch (CoreException x) {
			IStatus status= x.getStatus();
			if (status == null || status.getSeverity() != IStatus.CANCEL) {
				String title= TextEditorMessages.Editor_error_save_title;
				String msg= MessageFormat.format(TextEditorMessages.Editor_error_save_message, new Object[] { x.getMessage() });

				if (status != null) {
					switch (status.getSeverity()) {
						case IStatus.INFO:
							MessageDialog.openInformation(shell, title, msg);
						break;
						case IStatus.WARNING:
							MessageDialog.openWarning(shell, title, msg);
						break;
						default:
							MessageDialog.openError(shell, title, msg);
					}
				} else {
					MessageDialog.openError(shell, title, msg);
				}
			}
		} finally {
			provider.changed(newInput);
			if (success)
				setInput(newInput);
		}

		if (progressMonitor != null)
			progressMonitor.setCanceled(!success);
	}

	/*
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/*
	 * @see AbstractTextEditor#createActions()
	 * @since 2.0
	 */
	protected void createActions() {
		installEncodingSupport();
		super.createActions();
	}

	/*
	 * @see StatusTextEditor#getStatusHeader(IStatus)
	 * @since 2.0
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
	 * @since 2.0
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
	 * @since 2.0
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
	 * @since 2.0
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	/*
	 * @see IAdaptable#getAdapter(java.lang.Class)
	 * @since 2.0
	 */
	public Object getAdapter(Class adapter) {
		if (IEncodingSupport.class.equals(adapter))
			return fEncodingSupport;
		return super.getAdapter(adapter);
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#updatePropertyDependentActions()
	 * @since 2.0
	 */
	protected void updatePropertyDependentActions() {
		super.updatePropertyDependentActions();
		if (fEncodingSupport != null)
			fEncodingSupport.reset();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 * @since 3.0
	 */
	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, ITextEditorActionConstants.SHIFT_LEFT);
	}
}
