package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;

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
			IWorkbenchWindowPulldownDelegate delegate = getPulldownDelegate();
			if (delegate instanceof IWorkbenchWindowPulldownDelegate2) {
				IWorkbenchWindowPulldownDelegate2 delegate2 = (IWorkbenchWindowPulldownDelegate2)delegate;
				return delegate2.getMenu(parent);
			}
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
public WWinPluginPulldown(IConfigurationElement actionElement, String runAttribute, IWorkbenchWindow window) 
{
	super(actionElement, runAttribute, window,null);
	menuProxy = new MenuProxy();
	setMenuCreator(menuProxy);
}

/** 
 * Initialize an action delegate.
 * Subclasses may override this.
 */
protected IActionDelegate initDelegate(Object obj) 
	throws WorkbenchException
{
	IActionDelegate del = super.initDelegate(obj);
	if (obj instanceof IWorkbenchWindowPulldownDelegate) {
		return del;
	} else
		throw new WorkbenchException("Action must implement IWorkbenchWindowPulldownDelegate"); //$NON-NLS-1$
}

/**
 * Returns the pulldown delegate.  If it does not exist it is created.
 */
protected IWorkbenchWindowPulldownDelegate getPulldownDelegate() {
	IActionDelegate delegate = getDelegate();
	if (delegate == null) {
		createDelegate();
		delegate = getDelegate();
	}
	return (IWorkbenchWindowPulldownDelegate)delegate;
}
}
