/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.outline;


import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;

import org.eclipse.ui.externaltools.internal.ant.editor.IOutlineCreationListener;
import org.eclipse.ui.externaltools.internal.ant.editor.PlantyEditor;


public class XMLReconcilingStrategy implements IReconcilingStrategy, IOutlineCreationListener {

	private PlantyEditor fEditor;
	private boolean fIsDocumentSetOnModel;
	private IDocument fDocument;

	public XMLReconcilingStrategy(PlantyEditor editor) {
		fEditor= editor;
	}
	
	private void internalReconcile() {
		PlantyContentOutlinePage model= fEditor.getOutlinePage();
		if (model != null && model instanceof PlantyContentOutlinePageNew) {
			PlantyContentOutlinePageNew modelNew= (PlantyContentOutlinePageNew) model;

			synchronized (this) {
				if (!isDocumentSetOnModel())
					setDocumentOnModel(modelNew);
			}

			modelNew.reconcile();
		}
	}

	/*
	 * @see IReconcilingStrategy#reconcile(IRegion)
	 */
	public void reconcile(IRegion partition) {
		internalReconcile();
	}

	/*
	 * @see IReconcilingStrategy#reconcile(DirtyRegion, IRegion)
	 */
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		internalReconcile();
	}

	/*
	 * @see IReconcilingStrategy#setDocument(IDocument)
	 */
	public synchronized void setDocument(IDocument document) {
		fDocument= document;
		unsetDocumentOnModel();
	}

	/*
	 * @see org.eclipse.ui.externaltools.internal.ant.editor.IOutlineCreationListener#outlineCreated()
	 */
	public synchronized void outlineCreated() {
		unsetDocumentOnModel();
	}

	private void setDocumentOnModel(PlantyContentOutlinePageNew model) {
		model.setInput(fDocument);
		fIsDocumentSetOnModel= true;
	}

	private void unsetDocumentOnModel() {
		fIsDocumentSetOnModel= false;
	}

	private boolean isDocumentSetOnModel() {
		return fIsDocumentSetOnModel;
	}

}