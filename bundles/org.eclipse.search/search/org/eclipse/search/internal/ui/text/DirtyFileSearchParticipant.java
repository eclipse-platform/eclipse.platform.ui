/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.internal.core.text.DirtyFileProvider;

public class DirtyFileSearchParticipant implements DirtyFileProvider {

	@Override
	public Map<IFile, IDocument> dirtyFiles() {
		return evalNonFileBufferDocuments();
	}

	/**
	 * Returns a map from IFile to IDocument for all open, dirty editors. After
	 * creation this map is not modified, so returning a non-synchronized map is
	 * ok.
	 *
	 * @return a map from IFile to IDocument for all open, dirty editors
	 */
	private Map<IFile, IDocument> evalNonFileBufferDocuments() {
		Map<IFile, IDocument> result = new HashMap<>();
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorReference[] editorRefs = page.getEditorReferences();
				for (IEditorReference editorRef : editorRefs) {
					IEditorPart ep = editorRef.getEditor(false);
					if (ep instanceof ITextEditor && ep.isDirty()) { // only
																		// dirty
																		// editors
						evaluateTextEditor(result, ep);
					}
				}
			}
		}
		return result;
	}

	private void evaluateTextEditor(Map<IFile, IDocument> result, IEditorPart ep) {
		IEditorInput input = ep.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			if (!result.containsKey(file)) { // take the first editor found
				ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
				ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(file.getFullPath(),
						LocationKind.IFILE);
				if (textFileBuffer != null) {
					// file buffer has precedence
					result.put(file, textFileBuffer.getDocument());
				} else {
					// use document provider
					IDocument document = ((ITextEditor) ep).getDocumentProvider().getDocument(input);
					if (document != null) {
						result.put(file, document);
					}
				}
			}
		}
	}
}
