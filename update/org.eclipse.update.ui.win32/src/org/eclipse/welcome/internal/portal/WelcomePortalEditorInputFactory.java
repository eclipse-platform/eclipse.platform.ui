package org.eclipse.welcome.internal.portal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.ui.IElementFactory;

/**
 */
public class WelcomePortalEditorInputFactory implements IElementFactory {
	/**
	 */
	public WelcomePortalEditorInputFactory() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
	 */
	public IAdaptable createElement(IMemento memento) {
		return new WelcomePortalEditorInput();
	}

}
