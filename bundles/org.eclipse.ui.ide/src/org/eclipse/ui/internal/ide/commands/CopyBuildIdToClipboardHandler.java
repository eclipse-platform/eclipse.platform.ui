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

import java.util.Properties;

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
 * Copies the main build information to the clipboard, including os version and
 * windowing system. Useful for debugging and bug reporting/verification.
 *
 * @since 3.4
 *
 */
public class CopyBuildIdToClipboardHandler extends AbstractHandler {

	/** Platform O.S. */
	private static final String OS_NAME = "os.name"; //$NON-NLS-1$
	/** O.S. Version */
	private static final String OS_VERSION = "os.version"; //$NON-NLS-1$
	/** Platform architecture property name */
	private static final String OSGI_ARCH = "osgi.arch"; //$NON-NLS-1$
	/** Platform windowing system */
	private static final String OSGI_WS = "osgi.ws"; //$NON-NLS-1$
	/** GTK version */
	private static final String SWT_GTK_VERSION = "org.eclipse.swt.internal.gtk.version"; //$NON-NLS-1$
	/** WebKitGTK version */
	static final String SWT_WEBKITGTK_VERSION = "org.eclipse.swt.internal.webkitgtk.version"; //$NON-NLS-1$

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

		Properties sp = System.getProperties();
		String osInfo = String.format("OS: %s, v.%s, %s / %s", //$NON-NLS-1$
				sp.get(OS_NAME), sp.get(OS_VERSION), sp.get(OSGI_ARCH), sp.get(OSGI_WS));

		// defined in gtk systems
		String gtkVer = sp.getProperty(SWT_GTK_VERSION);
		if (gtkVer != null) {
			osInfo += String.format(" %s", gtkVer); //$NON-NLS-1$
		}
		// defined after launching WebKit, e.g. in Welcome Window
		String webkitGtkVer = sp.getProperty(SWT_WEBKITGTK_VERSION);
		if (webkitGtkVer != null) {
			osInfo += String.format(", WebKit %s", webkitGtkVer); //$NON-NLS-1$
		}

		String toCopy = String.format("%s%n%s%n%s%n%s%n", lines[0], lines[2], lines[3], osInfo); //$NON-NLS-1$

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
