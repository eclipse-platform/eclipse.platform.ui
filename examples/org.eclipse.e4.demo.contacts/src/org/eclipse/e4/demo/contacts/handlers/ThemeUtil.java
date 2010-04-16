/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ThemeUtil {

	public static void switchTheme(MWindow window, final String css) {
		final Shell shell = (Shell) window.getWidget();
		if (shell == null) {
			return;
		}
		
		Display display = shell.getDisplay();
		final CSSEngine engine = (CSSEngine) display
				.getData("org.eclipse.e4.ui.css.core.engine");

		display.syncExec(new Runnable() {
			public void run() {
				try {
					URL url = FileLocator.resolve(new URL(
							"platform:/plugin/org.eclipse.e4.demo.contacts/css/"
									+ css));

					InputStream stream = url.openStream();
					InputStreamReader streamReader = new InputStreamReader(
							stream);
					engine.reset();
					engine.parseStyleSheet(streamReader);
					stream.close();
					streamReader.close();
					
					try {
						shell.setRedraw(false);
						shell.reskin(SWT.ALL);
					} finally {
						shell.setRedraw(true);
					}
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	public static void applyDialogStyles(IStylingEngine engine, Control control) {
		if (engine != null) {
			Shell shell = control.getShell();
			if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
				shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
			}

			engine.style(shell);
		}
	}
}
