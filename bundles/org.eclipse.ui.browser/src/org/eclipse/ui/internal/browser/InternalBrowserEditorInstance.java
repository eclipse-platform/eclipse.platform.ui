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
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.AbstractWebBrowser;
/**
 * An instance of a running Web browser.
 */
public class InternalBrowserEditorInstance extends AbstractWebBrowser {
	protected int style;
	protected String name;
	protected String tooltip;
	protected WebBrowserEditor editor;
	protected IPartListener listener;

	public InternalBrowserEditorInstance(String id, int style, String name, String tooltip) {
		super(id);
		this.style = style;
		this.name = name;
		this.tooltip = tooltip;
	}

	public void openURL(URL url) throws PartInitException {
		WebBrowserEditorInput input = new WebBrowserEditorInput(url, style);
		
		if (editor != null) {
			editor.init(editor.getEditorSite(), input);
		} else {
			try {
				IWorkbenchWindow workbenchWindow = WebBrowserUIPlugin.getInstance().getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage page = workbenchWindow.getActivePage();
				IEditorPart editorPart = page.openEditor(input, WebBrowserEditor.WEB_BROWSER_EDITOR_ID);
				editor = (WebBrowserEditor) editorPart;
				listener = new IPartListener() {
					public void partActivated(IWorkbenchPart part) {
						// ignore
					}

					public void partBroughtToTop(IWorkbenchPart part) {
						// ignore
					}

					public void partClosed(IWorkbenchPart part) {
						if (part.equals(editor)) {
							editor = null;
							page.removePartListener(listener);
							DefaultBrowserSupport.getInstance().removeBrowser(getId());
						}
					}

					public void partDeactivated(IWorkbenchPart part) {
						// ignore
					}

					public void partOpened(IWorkbenchPart part) {
						// ignore
					}
				};
				page.addPartListener(listener);
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error opening Web browser", e);
			}
		}
	}

	public boolean close() {
		try {
			editor.closeEditor();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}