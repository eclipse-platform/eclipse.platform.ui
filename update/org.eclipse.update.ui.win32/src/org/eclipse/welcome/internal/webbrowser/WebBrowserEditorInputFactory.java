package org.eclipse.welcome.internal.webbrowser;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.IElementFactory;

/**
 */
public class WebBrowserEditorInputFactory implements IElementFactory {
	/**
	 */
	public WebBrowserEditorInputFactory() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		String name = memento.getString("name");
		String url = memento.getString("url");
		return new WebBrowserEditorInput(name, url);
	}

}
