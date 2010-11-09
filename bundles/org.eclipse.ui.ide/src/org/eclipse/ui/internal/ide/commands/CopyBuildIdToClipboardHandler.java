/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ConfigurationInfo;

/**
 * Copies the build ID to the clipboard. Useful for debugging and bug
 * reporting/verification.
 * 
 * @since 3.4
 * 
 */
public class CopyBuildIdToClipboardHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final String buildId = ConfigurationInfo.getBuildId();
		if (buildId == null || buildId.length() == 0)
			throw new ExecutionException("No build ID in this instance."); //$NON-NLS-1$
		Clipboard clipboard = null;
		try {
			clipboard = new Clipboard(HandlerUtil.getActiveShell(event)
					.getDisplay());
			clipboard.setContents(new Object[] { buildId },
					new Transfer[] { TextTransfer.getInstance() });
		} finally {
			if (clipboard != null) {
				clipboard.dispose();
			}
		}
		return null;
	}
}
