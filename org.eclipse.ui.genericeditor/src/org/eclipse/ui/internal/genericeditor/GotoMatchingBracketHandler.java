/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Angelo ZERR (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Go to matching bracket handler.
 *
 */
public class GotoMatchingBracketHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		if (part instanceof ExtensionBasedTextEditor) {
			((ExtensionBasedTextEditor) part).gotoMatchingBracket();
		}
		return null;
	}

}
