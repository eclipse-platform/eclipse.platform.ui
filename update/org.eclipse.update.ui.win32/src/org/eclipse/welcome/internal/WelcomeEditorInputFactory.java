package org.eclipse.welcome.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.IElementFactory;

/**
 */
public class WelcomeEditorInputFactory implements IElementFactory {
	/**
	 */
	public WelcomeEditorInputFactory() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		String name = memento.getString("name");
		String url = memento.getString("url");
		return new WelcomeEditorInput(name, url);
	}

}
