/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.browser.AbstractWebBrowser;

/**
 * An instance of a running Web browser.
 */
public abstract class InternalBrowserInstance extends AbstractWebBrowser {
	protected int style;

	protected String name;

	protected String tooltip;

	protected IWorkbenchPart part;

	protected IPartListener listener;
	
	private Integer windowKey;

	public InternalBrowserInstance(String id, int style, String name,
			String tooltip) {
		super(WebBrowserUtil.encodeStyle(id, style));
		this.style = style;
		this.name = name;
		this.tooltip = tooltip;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	protected void hookPart(final IWorkbenchPage page, IWorkbenchPart part2) {
		this.part = part2;
		listener = new IPartListener() {
			public void partActivated(IWorkbenchPart part3) {
				// ignore
			}

			public void partBroughtToTop(IWorkbenchPart part3) {
				// ignore
			}

			public void partClosed(IWorkbenchPart part3) {
				if (part3.equals(InternalBrowserInstance.this.part)) {
					InternalBrowserInstance.this.part = null;
					page.removePartListener(listener);
					DefaultBrowserSupport.getInstance().removeBrowser(
							InternalBrowserInstance.this);
				}
			}

			public void partDeactivated(IWorkbenchPart part3) {
				// ignore
			}

			public void partOpened(IWorkbenchPart part3) {
				// ignore
			}
		};
		page.addPartListener(listener);
		IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
		windowKey = new Integer(window.hashCode());
	}

	public Integer getWindowKey() {
		return windowKey;
	}
}
