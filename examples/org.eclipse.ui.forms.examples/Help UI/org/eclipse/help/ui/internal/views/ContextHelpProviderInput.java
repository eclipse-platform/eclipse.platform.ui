/*
 * Created on Dec 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui.internal.views;

import org.eclipse.swt.widgets.Control;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContextHelpProviderInput {
	private IContextHelpProvider provider;
	private Control control;
	public ContextHelpProviderInput(IContextHelpProvider provider, Control control) {
		this.provider = provider;
		this.control =control;
	}
	
	public IContextHelpProvider getProvider() {
		return provider;
	}
	public Control getControl() {
		return control;
	}
}
