/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		IServiceLocator locator = getSite();
		IContextService contextService = locator
				.getService(IContextService.class);

		// if this was instantiated as a regular editor, the context would
		// be governed by part activation ... embedded in an MPEP,
		// the context should be governed by page activation.
		contextService.activateContext(CONTEXT_ID);
	}
}
