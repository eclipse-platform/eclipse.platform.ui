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


package org.eclipse.ui.texteditor;


import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * A ruler action which can select the textual range of a marker 
 * that has a visual representation in a vertical ruler.
 * @since 3.0
 */
public class SelectMarkerRulerAction2 extends TextEditorAction implements IAnnotationListener {

	/**
	 * Creates a new action for the given ruler and editor. The action configures
	 * its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or  <code>null</code> if none
	 * @param editor the editor
	 * 
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public SelectMarkerRulerAction2(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	public void setEditor(ITextEditor editor) {
		if (getTextEditor() != null) {
			IVerticalRulerInfo service= (IVerticalRulerInfo) getTextEditor().getAdapter(IVerticalRulerInfo.class);
			if (service instanceof IVerticalRulerInfoExtension)
				((IVerticalRulerInfoExtension) service).removeAnnotationListener(this);
		}
		super.setEditor(editor);
		if (getTextEditor() != null) {
			IVerticalRulerInfo service= (IVerticalRulerInfo) getTextEditor().getAdapter(IVerticalRulerInfo.class);
			if (service instanceof IVerticalRulerInfoExtension)
				((IVerticalRulerInfoExtension) service).addAnnotationListener(this);
		}
	}
	
	/**
	 * Returns the <code>AbstractMarkerAnnotationModel</code> of the editor's input.
	 *
	 * @return the marker annotation model or <code>null</code> if there's none
	 */
	protected IAnnotationModel getAnnotationModel() {
		IDocumentProvider provider= getTextEditor().getDocumentProvider();
		return provider.getAnnotationModel(getTextEditor().getEditorInput());
	}

	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationListener#annotationSelected(org.eclipse.ui.texteditor.AnnotationEvent)
	 */
	public void annotationSelected(AnnotationEvent event) {
	}

	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationListener#annotationDefaultSelected(org.eclipse.ui.texteditor.AnnotationEvent)
	 */
	public void annotationDefaultSelected(AnnotationEvent event) {
		Annotation a= event.getAnnotation();
		IAnnotationModel model= getAnnotationModel();
		Position position= model.getPosition(a);
		if (position == null)
			return;
		
		getTextEditor().selectAndReveal(position.offset, position.length);
	}

	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationListener#annotationContextMenuAboutToShow(org.eclipse.ui.texteditor.AnnotationEvent, org.eclipse.swt.widgets.Menu)
	 */
	public void annotationContextMenuAboutToShow(AnnotationEvent event, Menu menu) {
	}
}
