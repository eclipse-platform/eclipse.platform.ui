/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor.text;


import org.eclipse.ant.internal.ui.editor.AntEditor;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class XMLReconcilingStrategy implements IReconcilingStrategy {

	private ITextEditor fEditor;

	public XMLReconcilingStrategy(ITextEditor editor) {
		fEditor= editor;
	}
	
	private void internalReconcile(DirtyRegion dirtyRegion) {
		IDocumentProvider provider= fEditor.getDocumentProvider();
		if (provider instanceof AntEditorDocumentProvider) {
			AntEditorDocumentProvider documentProvider= (AntEditorDocumentProvider) provider;
			AntModel model= documentProvider.getAntModel(fEditor.getEditorInput());
			if (model != null)
				model.reconcile(dirtyRegion);
		}
	}

	/*
	 * @see IReconcilingStrategy#reconcile(IRegion)
	 */
	public void reconcile(IRegion partition) {
		internalReconcile(null);
	}

	/*
	 * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		internalReconcile(dirtyRegion);
		if (fEditor instanceof AntEditor) {
			((AntEditor)fEditor).reconciled();
		}
	}

	/*
	 * @see IReconcilingStrategy#setDocument(IDocument)
	 */
	public void setDocument(IDocument document) {
	}
}