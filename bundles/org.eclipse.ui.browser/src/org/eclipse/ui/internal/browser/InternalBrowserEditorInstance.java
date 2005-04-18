/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
/**
 * An instance of a running Web browser.
 */
public class InternalBrowserEditorInstance extends InternalBrowserInstance {

	public InternalBrowserEditorInstance(String id, int style, String name, String tooltip) {
		super(id, style, name, tooltip);
	}

	public void openURL(URL url) throws PartInitException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(url, style);
		input.setName(this.name);
		input.setToolTipText(this.tooltip);
        WebBrowserEditor editor = (WebBrowserEditor)part;
		
		if (editor != null) {
			editor.init(editor.getEditorSite(), input);
		} else {
			try {
				IWorkbenchWindow workbenchWindow = WebBrowserUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage page = workbenchWindow.getActivePage();
				IEditorPart editorPart = page.openEditor(input, WebBrowserEditor.WEB_BROWSER_EDITOR_ID);
                hookPart(page, editorPart);
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error opening Web browser", e); //$NON-NLS-1$
			}
		}
	}

	public boolean close() {
		try {
			return ((WebBrowserEditor)part).close();
		} catch (Exception e) {
			return false;
		}
	}
}