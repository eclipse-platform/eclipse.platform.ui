/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.multipageeditor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.2
 * 
 */
public class ContextTextEditor extends TextEditor {
	public static final String TEXT_CONTEXT_ID = "org.eclipse.ui.textEditorScope";

	public static final String CONTEXT_ID = "org.eclipse.ui.tests.multipageeditor.contextEditor";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#init(org.eclipse.ui.IEditorSite,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		IServiceLocator locator = getSite();
		IContextService contextService = (IContextService) locator
				.getService(IContextService.class);

		// if this was instantiated as a regular editor, the context would
		// be governed by part activation ... embedded in an MPEP,
		// the context should be governed by page activation.
		contextService.activateContext(CONTEXT_ID);
	}
}
