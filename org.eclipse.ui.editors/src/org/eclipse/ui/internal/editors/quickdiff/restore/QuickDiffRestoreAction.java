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
package org.eclipse.ui.internal.editors.quickdiff.restore;

import java.util.ResourceBundle;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.ILineRestorer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public abstract class QuickDiffRestoreAction extends TextEditorAction {
	QuickDiffRestoreAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	protected abstract void runCompoundChange();
	
	public void run() {
		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;
		IRewriteTarget target= (IRewriteTarget)editor.getAdapter(IRewriteTarget.class);
		if (target != null)
			target.beginCompoundChange();
		runCompoundChange();
		if (target != null) 
			target.endCompoundChange();
		
	}

	protected ITextSelection getSelection() {
		if (getTextEditor() == null)
			return null;
		ISelectionProvider sp= getTextEditor().getSelectionProvider();
		if (sp == null)
			return null;
		ISelection s= sp.getSelection();
		if (s instanceof ITextSelection)
			return (ITextSelection)s;
		else
			return null;
	}

	private IAnnotationModelExtension getModel() {
		if (getTextEditor() == null)
			return null;
		IDocumentProvider provider= getTextEditor().getDocumentProvider();
		IEditorInput editorInput= getTextEditor().getEditorInput();
		IAnnotationModel m= provider.getAnnotationModel(editorInput);
		if (m instanceof IAnnotationModelExtension) {
			return (IAnnotationModelExtension)m;
		} else {
			return null;
		}
	}

	protected ILineDiffer getDiffer() {
		IAnnotationModelExtension extension= getModel();
		if (extension != null)
			return (ILineDiffer)extension.getAnnotationModel(ILineDiffer.ID);
		else
			return null;
	}

	protected ILineRestorer getRestorer() {
		ILineDiffer differ= getDiffer();
		if (differ instanceof ILineRestorer)
			return (ILineRestorer)differ;
		else
			return null;
	}

	protected IVerticalRulerInfo getRuler() {
		if (getTextEditor() != null)
			return (IVerticalRulerInfo)getTextEditor().getAdapter(IVerticalRulerInfo.class);
		else
			return null;
	}
}
