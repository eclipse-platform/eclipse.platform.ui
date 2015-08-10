/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;


/**
 * The editor input for the integrated web browser.
 */
public class WebBrowserEditorInput implements IEditorInput,
		IPersistableElement, IElementFactory {
	// --- constants to pass into constructor ---

	// if used, the toolbar will be available
	// public static final int SHOW_TOOLBAR = 1 << 1;

	// public static final int SHOW_GLOBAL_TOOLBAR = 1 << 2;

	// if used, the status bar will be available
	// public static final int SHOW_STATUSBAR = 1 << 3;

	// if used, the original URL will be saved and
	// the page can reopen to the same URL after
	// shutting down
	// public static final int SAVE_URL = 1 << 5;

	// if used, the browser will be transient and will not appear
	// in the most recently used file list, nor will it reopen after
	// restarting Eclipse
	// public static final int TRANSIENT = 1 << 6;

	private static final String ELEMENT_FACTORY_ID = "org.eclipse.ui.browser.elementFactory"; //$NON-NLS-1$

	private static final String MEMENTO_URL = "url"; //$NON-NLS-1$

	private static final String MEMENTO_STYLE = "style"; //$NON-NLS-1$

	private static final String MEMENTO_ID = "id"; //$NON-NLS-1$

	private static final String MEMENTO_NAME = "name"; //$NON-NLS-1$

	private static final String MEMENTO_TOOLTIP = "tooltip"; //$NON-NLS-1$

	private URL url;

	private int style;

	private String id = null;

	private String name;

	private String tooltip;

	/**
	 * ExternalBrowserInstance editor input for the homepage.
	 */
	public WebBrowserEditorInput() {
		this(null);
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url) {
		this(url, 0);
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url, int style) {
		super();
		this.url = url;
		this.style = style;
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url, int style, String browserId) {
		super();
		this.url = url;
		this.style = style;
		this.id = browserId;
	}

	/**
	 * WebBrowserEditorInput constructor comment.
	 */
	public WebBrowserEditorInput(URL url, boolean b) {
		this(url);
	}

	public void setName(String n) {
		name = n;
	}

	public void setToolTipText(String t) {
		tooltip = t;
	}

	/**
	 * Returns true if this page can reuse the browser that the given input is
	 * being displayed in, or false if it should open up in a new page.
	 *
	 * @param input
	 *            org.eclipse.ui.internal.browser.IWebBrowserEditorInput
	 * @return boolean
	 */
	public boolean canReplaceInput(WebBrowserEditorInput input) {
		Trace.trace(Trace.FINEST, "canReplaceInput " + this + " " + input); //$NON-NLS-1$ //$NON-NLS-2$
		// if ((style & FORCE_NEW_PAGE) != 0)
		// return false;
		// if (input.isToolbarVisible() != isToolbarVisible())
		// return false;
		// else
		if (input.isStatusbarVisible() != isStatusbarVisible())
			return false;
		else if (id != null) {
			String bid = input.getBrowserId();
			return id.equals(bid);
		} else
			return false;
	}

	/**
	 * Creates an <code>IElement</code> from the state captured within an
	 * <code>IMemento</code>.
	 *
	 * @param memento
	 *            a memento containing the state for an element
	 * @return an element, or <code>null</code> if the element could not be
	 *         created
	 */
	@Override
	public IAdaptable createElement(IMemento memento) {
		int style = 0;
		Integer integer = memento.getInteger(MEMENTO_STYLE);
		if (integer != null) {
			style = integer.intValue();
		}

		URL url = null;
		String str = memento.getString(MEMENTO_URL);
		if (str != null) {
			try {
				url = new URL(str);
			}
			catch (MalformedURLException e) {
				String msg = "Malformed URL while initializing browser editor"; //$NON-NLS-1$
				WebBrowserUIPlugin.logError(msg, e);
			}
		}

		String id = memento.getString(MEMENTO_ID);
		String name = memento.getString(MEMENTO_NAME);
		String tooltip = memento.getString(MEMENTO_TOOLTIP);

		WebBrowserEditorInput input = new WebBrowserEditorInput(url, style, id);
		input.setName(name);
		input.setToolTipText(tooltip);
		return input;
	}

	/**
	 * Tests to see if two input objects are equal in the sense that they can share an
	 * editor.
	 * @return true if the url and browser id are equal and the style bits are compatible
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof WebBrowserEditorInput))
			return false;
		WebBrowserEditorInput other = (WebBrowserEditorInput) obj;

		if (url != null) {
			if (other.url == null || !url.toExternalForm().equals(other.url.toExternalForm()))	 {
			    return false;
			}
		} else if (other.url != null) {
		    return false;
		}
		return canReplaceInput(other);
	}

	/*
	 * Returns whether the editor input exists.
	 */
	@Override
	public boolean exists() {
		if ((style & IWorkbenchBrowserSupport.PERSISTENT) != 0)
			return false;

		return true;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	/**
	 * Returns the ID of an element factory which can be used to recreate this
	 * object. An element factory extension with this ID must exist within the
	 * workbench registry.
	 *
	 * @return the element factory ID
	 */
	@Override
	public String getFactoryId() {
		return ELEMENT_FACTORY_ID;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageResource
				.getImageDescriptor(ImageResource.IMG_INTERNAL_BROWSER);
	}

	/**
	 * Returns true if the name is locked and cannot be changed.
	 *
	 * @return <code>true</code> if the name of the browser should not change
	 */
	protected boolean isNameLocked() {
		return (name != null);
	}

	@Override
	public String getName() {
		if (name != null)
			return name;

		return Messages.viewWebBrowserTitle;
	}

	@Override
	public IPersistableElement getPersistable() {
		if ((style & IWorkbenchBrowserSupport.PERSISTENT) == 0)
			return null;

		return this;
	}

	@Override
	public String getToolTipText() {
		if (tooltip != null)
			return tooltip;

		if (url != null)
			return url.toExternalForm();

		return Messages.viewWebBrowserTitle;
	}

	/**
	 * Returns the url.
	 *
	 * @return java.net.URL
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Returns the browser id. Browsers with a set id will always & only be
	 * replaced by browsers with the same id.
	 *
	 * @return String
	 */
	public String getBrowserId() {
		return id;
	}

	/**
	 * Returns true if the status bar should be shown.
	 *
	 * @return boolean
	 */
	public boolean isStatusbarVisible() {
		return (style & IWorkbenchBrowserSupport.STATUS) != 0;
	}

	/**
	 * Returns true if the toolbar should be shown.
	 *
	 * @return boolean
	 */
	public boolean isLocationBarLocal() {
		return (style & BrowserViewer.LOCATION_BAR) != 0;
	}

	public boolean isToolbarLocal() {
		return (style & BrowserViewer.BUTTON_BAR) != 0;
	}

	/**
	 * Saves the state of an element within a memento.
	 *
	 * @param memento
	 *            the storage area for element state
	 */
	@Override
	public void saveState(IMemento memento) {
		memento.putInteger(MEMENTO_STYLE, style);
		if ((style & IWorkbenchBrowserSupport.PERSISTENT) != 0 && url != null) {
			memento.putString(MEMENTO_URL, url.toExternalForm());
		}
		if (id != null) {
			memento.putString(MEMENTO_ID, id);
		}
		if (name != null) {
			memento.putString(MEMENTO_NAME, name);
		}
		if (tooltip != null) {
			memento.putString(MEMENTO_TOOLTIP, tooltip);
		}
	}

	@Override
	public String toString() {
		return "WebBrowserEditorInput[" + url + " " + style + " " + id + "]";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (url != null) {
			result = result + url.toExternalForm().hashCode();
		}
		if (id != null) {
			result = result + id.hashCode();
		}
		return result;
	}
}
