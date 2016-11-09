/*******************************************************************************
 * Copyright (c) 2000, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * A generic code editor that is aimed at being extended by contributions. Behavior
 * is supposed to be added via extensions, not by inheritance.
 * 
 * @since 1.0
 */
public class ExtensionBasedTextEditor extends TextEditor {

	private static final String CONTEXT_ID = "org.eclipse.ui.genericeditor.genericEditorContext"; //$NON-NLS-1$
	private ExtensionBasedTextViewerConfiguration configuration;

	/**
	 * 
	 */
	public ExtensionBasedTextEditor() {
		configuration = new ExtensionBasedTextViewerConfiguration(this, getPreferenceStore());
		setSourceViewerConfiguration(configuration);
	}
	
	@Override
	protected void setKeyBindingScopes(String[] scopes) {
		super.setKeyBindingScopes(new String[] { CONTEXT_ID });
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		configuration.watchDocument(getDocumentProvider().getDocument(input));
	}

}
