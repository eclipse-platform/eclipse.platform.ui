/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 496319
 ******************************************************************************/

package org.eclipse.ui.internal.ide.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.internal.ProductProperties;

/**
 * Copies the main build information to the clipboard. Useful for debugging and
 * bug reporting/verification.
 *
 * @since 3.4
 *
 */
public class CopyBuildIdToClipboardHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IProduct product = Platform.getProduct();
		if (product == null )
			throw new ExecutionException("No product is defined."); //$NON-NLS-1$

		String aboutText = ProductProperties.getAboutText(product);
		String lines[] = aboutText.split("\\r?\\n"); //$NON-NLS-1$
		if (lines.length<=3){
			throw new ExecutionException("Product About Text is not properly defined."); //$NON-NLS-1$
		}

		String toCopy = String.format("%s%n%s%n%s", lines[0], lines[2], lines[3]); //$NON-NLS-1$

		Clipboard clipboard = new Clipboard(null);
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			Transfer[] transfers = new Transfer[] { textTransfer };
			Object[] data = new Object[] { toCopy };
			clipboard.setContents(data, transfers);
		} finally {
			clipboard.dispose();
		}
		return null;
	}
}
