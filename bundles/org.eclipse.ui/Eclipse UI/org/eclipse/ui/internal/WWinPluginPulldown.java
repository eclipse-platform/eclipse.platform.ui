package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;

/**
 * A workbench window pulldown action.  
 */
public class WWinPluginPulldown extends WWinPluginAction {
	private IMenuCreator menuProxy;
	private class MenuProxy implements IMenuCreator {
		private Menu menu;
		public Menu getMenu(Control parent) {
			IWorkbenchWindowPulldownDelegate delegate = getPulldownDelegate();
			if (delegate != null) {
				return delegate.getMenu(parent);
			} else {
				return null;
			}
		}
		public Menu getMenu(Menu parent) {
			return null;
		}
		public void dispose() {
		}
	};
/**
 * WWinPluginPulldown constructor comment.
 * @param actionElement org.eclipse.core.runtime.IConfigurationElement
 * @param runAttribute java.lang.String
 * @param window org.eclipse.ui.IWorkbenchWindow
 */
public WWinPluginPulldown(IConfigurationElement actionElement, String runAttribute, 
	IWorkbenchWindow window) {
	super(actionElement, runAttribute, window);
	menuProxy = new MenuProxy();
	setMenuCreator(menuProxy);
}
/**
 * Creates an instance of the delegate class.
 */
protected IActionDelegate createDelegate() {
	IActionDelegate delegate = super.createDelegate();
	if (delegate instanceof IWorkbenchWindowPulldownDelegate) {
		IWorkbenchWindowPulldownDelegate pulldown =
			(IWorkbenchWindowPulldownDelegate) delegate;
		return delegate;
	} else {
		WorkbenchPlugin.log(
			"Action should implement IWorkbenchWindowPluginDelegate: " + getText());//$NON-NLS-1$
		return null;
	}
}
/**
 * Returns the pulldown delegate.  If it does not exist it is created.
 */
protected IWorkbenchWindowPulldownDelegate getPulldownDelegate() {
	IActionDelegate delegate = getDelegate();
	if (delegate == null) {
		delegate = createDelegate();
		setDelegate(delegate);
	}
	return (IWorkbenchWindowPulldownDelegate)delegate;
}
}
