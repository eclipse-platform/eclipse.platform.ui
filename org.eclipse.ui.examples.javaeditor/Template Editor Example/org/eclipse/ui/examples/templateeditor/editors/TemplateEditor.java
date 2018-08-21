/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.templateeditor.editors;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * A simple xml editor with template capabilities.
 */
public class TemplateEditor extends AbstractDecoratedTextEditor {

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

	@Override
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	@Override
	protected void createActions() {
		super.createActions();

		IAction action= getAction(ITextEditorActionConstants.CONTENT_ASSIST);
		if (action != null) {
			action.setText(TemplateMessages.getString("Editor.template_proposals_action.label")); //$NON-NLS-1$
			action.setToolTipText(TemplateMessages.getString("Editor.template_proposals_action.tooltip")); //$NON-NLS-1$
			action.setDescription(TemplateMessages.getString("Editor.template_proposals_action.description")); //$NON-NLS-1$
		}
	}

}
