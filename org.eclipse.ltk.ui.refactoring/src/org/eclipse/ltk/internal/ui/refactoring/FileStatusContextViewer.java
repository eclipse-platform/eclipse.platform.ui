/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.ui.refactoring.TextStatusContextViewer;


public class FileStatusContextViewer extends TextStatusContextViewer {

	public void createControl(Composite parent) {
		super.createControl(parent);
		getSourceViewer().configure(new SourceViewerConfiguration());
	}

	public void setInput(RefactoringStatusContext context) {
		FileStatusContext fc= (FileStatusContext)context;
		IFile file= fc.getFile();
		updateTitle(file);
		IDocument document= getDocument(file);
		IRegion region= fc.getTextRegion();
		if (region != null && document.getLength() >= region.getOffset() + region.getLength())
			setInput(document, region);
		else {
			setInput(document, new Region(0, 0));
		}
	}

	protected SourceViewer createSourceViewer(Composite parent) {
	    return new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
	}

	private IDocument getDocument(IFile file) {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		try {
			try {
				manager.connect(path, LocationKind.IFILE, new NullProgressMonitor());
				ITextFileBuffer buffer = manager.getTextFileBuffer(path, LocationKind.IFILE);
				if (buffer != null) {
					return buffer.getDocument();
				}
			} finally {
				manager.disconnect(path, LocationKind.IFILE, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			RefactoringUIPlugin.log(e);
		}
		return new Document(RefactoringUIMessages.FileStatusContextViewer_error_reading_file);
	}
}
