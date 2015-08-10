/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 * ActionBarContributor for the Web browser. Just adds cut, copy, paste actions.
 */
public class WebBrowserEditorActionBarContributor extends EditorActionBarContributor {
	protected WebBrowserEditor editor;
	protected Action back;
	protected Action forward;
	protected Updater updater = new Updater();

	class Updater implements BrowserViewer.IBackNextListener {
		@Override
		public void updateBackNextBusy() {
			if (back == null)
				return;
			back.setEnabled(getWebBrowser().isBackEnabled());
			forward.setEnabled(getWebBrowser().isForwardEnabled());
			// busy.setBusy(getWebBrowser().loading);
		}
	}

	/**
	 * WebBrowserEditorActionBarContributor constructor comment.
	 */
	public WebBrowserEditorActionBarContributor() {
		super();
	}

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof WebBrowserEditor) {
			editor = (WebBrowserEditor) targetEditor;
		} else
			editor = null;
	}

	protected BrowserViewer getWebBrowser() {
		if (editor == null)
			return null;

		return editor.webBrowser;
	}
}
