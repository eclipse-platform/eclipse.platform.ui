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
package org.eclipse.debug.ui.console;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * A hyperlink that opens a file in a text editor and selects a range of text.
 * <p>
 * This class is not intended to be subclassed; clients may instantiate this
 * class.
 * </p>
 * @since 2.1
 */
public class FileLink implements IConsoleHyperlink {

	private IFile fFile;
	private int fFileOffset;
	private int fFileLength;
	private int fFileLineNumber;
	private String fEditorId;
	
	/**
	 * Constructs a hyperlink to the specified file.
	 * 
	 * @param file the file to open when activated
	 * @param editorId the identifier of the editor to open the file in, or
	 * <code>null</code> if the default editor should be used
	 * @param fileOffset the offset in the file to select when activated, or -1
	 * @param fileLength the length of text to select in the file when activated
	 * or -1
	 * @param fileLineNumber the line number to select in the file when
	 * activated, or -1
	 */
	public FileLink(IFile file, String editorId, int fileOffset, int fileLength, int fileLineNumber) {
		fFile = file;
		fFileOffset = fileOffset;
		fFileLength = fileLength;
		fFileLineNumber = fileLineNumber;
		fEditorId = editorId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkActivated()
	 */
	public void linkActivated() {
		IWorkbenchWindow window = DebugUIPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				try {
					IEditorPart editorPart = page.openEditor(new FileEditorInput(fFile), getEditorId() , false);
					if (fFileLineNumber > 0 && editorPart instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor)editorPart;
						IEditorInput input = editorPart.getEditorInput();
						if (fFileOffset < 0) {
							IDocumentProvider provider = textEditor.getDocumentProvider();
							try {
								provider.connect(input);
							} catch (CoreException e) {
								// unable to link
								DebugUIPlugin.log(e);
								return;
							}
							IDocument document = provider.getDocument(input);
							try {
								fFileOffset = document.getLineOffset(fFileLineNumber - 1);
								fFileLength = document.getLineLength(fFileLineNumber - 1);
							} catch (BadLocationException e) {
								// unable to link
								DebugUIPlugin.log(e);
							}
							provider.disconnect(input);
						}
						if (fFileOffset >= 0 && fFileLength >=0) {
							textEditor.selectAndReveal(fFileOffset, fFileLength);
						}
					}
				} catch (PartInitException e) {
					DebugUIPlugin.log(e);
				}	
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkEntered()
	 */
	public void linkEntered() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkExited()
	 */
	public void linkExited() {
	}
	
	private String getEditorId() {
		if (fEditorId == null) {
			IWorkbench workbench= DebugUIPlugin.getDefault().getWorkbench();
			// If there is a registered editor for the file use it.
			IEditorDescriptor desc =  workbench.getEditorRegistry().getDefaultEditor(fFile.getName());
			if (desc == null) {
				//default editor
				desc= workbench.getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
			}
			fEditorId= desc.getId();
		}
		return fEditorId;
	}
}