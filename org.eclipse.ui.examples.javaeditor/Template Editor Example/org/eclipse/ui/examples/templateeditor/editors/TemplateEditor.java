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
package org.eclipse.ui.examples.templateeditor.editors;

import org.eclipse.jface.action.IAction;

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;

/**
 * A simple xml editor with template capabilities.
 */
public class TemplateEditor extends AbstractDecoratedTextEditor {

	private static final String TEMPLATE_PROPOSALS= "template_proposals_action"; //$NON-NLS-1$
	private ColorManager colorManager;

	/**
	 * Creates a new template editor.
	 */
	public TemplateEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	
	protected void createActions() {
		super.createActions();

		IAction action= new TextOperationAction(
				TemplateMessages.getResourceBundle(),
				"Editor." + TEMPLATE_PROPOSALS + ".", //$NON-NLS-1$ //$NON-NLS-2$
				this,
				ISourceViewer.CONTENTASSIST_PROPOSALS);
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction(TEMPLATE_PROPOSALS, action);
		markAsStateDependentAction(TEMPLATE_PROPOSALS, true);
	}

}
