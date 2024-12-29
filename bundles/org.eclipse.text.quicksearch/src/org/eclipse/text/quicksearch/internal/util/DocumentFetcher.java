/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Pivotal Inc - Adapted for use in quicksearch
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchActivator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Useful utilities (private methods) copied from org.eclipse.search.internal.core.text.TextSearchVisitor
 * and rearanged / massaged to be a more reusable utility class.
 * <p>
 * These utilities allow us to access the contents of dirty editors so we can search/read in them as though they
 * are already saved but without actually requiring the user to save them.
 *
 * @author Kris De Volder
 */
public class DocumentFetcher {

	private static final ITextFileBufferManager BUFFER_MANAGER = FileBuffers.getTextFileBufferManager();

	private Map<IFile, IDocument> dirtyEditors;

	//Simple cache remembers the last fetched file and document.
	private IFile lastFile = null;
	private IDocument lastDocument = null;
	private boolean disconnectLastFile = false;

	IDocumentProvider provider = new TextFileDocumentProvider();


	public DocumentFetcher() {
		if (PlatformUI.isWorkbenchRunning()) {
			dirtyEditors = evalNonFileBufferDocuments();
		} else {
			dirtyEditors = Collections.emptyMap();
		}
	}

	/**
	 * Obtains a {@link IDocument} containing the contents of a
	 * {@link IFile}. Two different scenarios are supported depending
	 * on whether or not the file is currently opened in a editor.
	 * <p>
	 * If the IFile is opened in an editor, then the document reflects
	 * the editor contents (including any not-yet saved edits).
	 * <p>
	 * If the file is not open, then the document just reflects the
	 * contents of the file.
	 *
	 * @return Document containing the contents of the file or editor buffer,
	 *    or null if the content can not be found (it exists neither as a editor
	 *    buffer nor corresponds to an existing file in the workspace.
	 */
	public IDocument getDocument(IFile file) {
		if (lastFile != null && lastFile.equals(file)) {
			return lastDocument;
		}
		disconnectLastFile();
		lastFile = file;
		lastDocument = dirtyEditors.get(file);
		if (lastDocument==null) {
			lastDocument = getOpenDocument(file);
			if (lastDocument==null) {
				lastDocument = getClosedDocument(file);
			}
		}
		return lastDocument;
	}

	private IDocument getOpenDocument(IFile file) {
		ITextFileBuffer textFileBuffer= BUFFER_MANAGER.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		if (textFileBuffer != null) {
			return textFileBuffer.getDocument();
		}
		return null;
	}

	private IDocument getClosedDocument(IFile file) {
		//No  in the manager yet. Try to create a temporary buffer - required for some extensions to work
		// (will get removed once not used anymore)
		IPath location = file.getFullPath(); //Must use workspace location, not fs location for API below.
		ITextFileBuffer buffer = null;
		try {
			BUFFER_MANAGER.connect(location, LocationKind.IFILE, new NullProgressMonitor());
			disconnectLastFile = true;
			buffer = BUFFER_MANAGER.getTextFileBuffer(location, LocationKind.IFILE);
			if (buffer!=null) {
				return buffer.getDocument();
			}
		} catch (Throwable e) {
			QuickSearchActivator.log(e);
		}
		return null;
	}

	/**
	 * @return returns a map from IFile to IDocument for all open, dirty editors.
	 */
	private Map<IFile, IDocument> evalNonFileBufferDocuments() {
		Map<IFile, IDocument> result= new HashMap<>();
		IWorkbench workbench= PlatformUI.getWorkbench();
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			for (IWorkbenchPage page : window.getPages()) {
				for (IEditorReference editorRef : page.getEditorReferences()) {
					IEditorPart ep= editorRef.getEditor(false);
					if (ep instanceof ITextEditor && ep.isDirty()) { // only dirty editors
						evaluateTextEditor(result, ep);
					}
				}
			}
		}
		return result;
	}

	private void evaluateTextEditor(Map<IFile, IDocument> result, IEditorPart ep) {
		IEditorInput input= ep.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput) input).getFile();
			if (!result.containsKey(file)) { // take the first editor found
				ITextFileBuffer textFileBuffer= BUFFER_MANAGER.getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
				if (textFileBuffer != null) {
					// file buffer has precedence
					result.put(file, textFileBuffer.getDocument());
				} else {
					// use document provider
					IDocument document= ((ITextEditor) ep).getDocumentProvider().getDocument(input);
					if (document != null) {
						result.put(file, document);
					}
				}
			}
		}
	}

	private void disconnectLastFile() {
		if (disconnectLastFile && lastFile != null) {
			disconnectLastFile = false;
			try {
				BUFFER_MANAGER.disconnect(lastFile.getFullPath(), LocationKind.IFILE, new NullProgressMonitor());
			} catch (CoreException e) {
				QuickSearchActivator.log(e);
			}
		}
	}

	public void destroy() {
		disconnectLastFile();
	}

}
