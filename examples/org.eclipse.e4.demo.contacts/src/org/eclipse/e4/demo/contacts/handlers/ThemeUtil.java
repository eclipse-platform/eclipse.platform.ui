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
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ThemeUtil {

	public static void switchTheme(IWorkbench workbench, final String css) {
		if (workbench instanceof Workbench) {
			Workbench wb = (Workbench) workbench;
			final Shell shell = (Shell) wb.getWindow();
			Display display = shell.getDisplay();
			final CSSEngine engine = (CSSEngine) display
					.getData("org.eclipse.e4.ui.css.core.engine");

			display.syncExec(new Runnable() {
				public void run() {
					try {
						URL url = FileLocator.resolve(new URL(
								"platform:/plugin/org.eclipse.e4.demo.contacts/css/"
										+ css));

						InputStreamReader streamReader = new InputStreamReader(
								url.openStream());
						engine.reset();
						engine.parseStyleSheet(streamReader);
						engine.applyStyles(shell, true, false);
						shell.layout(true, true);
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
	}
}
