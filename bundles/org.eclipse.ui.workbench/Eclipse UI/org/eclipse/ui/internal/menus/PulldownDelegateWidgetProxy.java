/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A proxy for a {@link IWorkbenchWindowPulldownDelegate} on a pulldown action
 * set action. This delays the class loading until the delegate is really asked
 * for information. Asking a proxy for anything (except disposing) will cause
 * the proxy to instantiate the proxied delegate.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
final class PulldownDelegateWidgetProxy implements IWidget {

	/**
	 * A wrapper for loading the menu that defends against possible exceptions
	 * triggered outside of the workbench.
	 */
	private static final class MenuLoader implements ISafeRunnable {

		/**
		 * The parent for the menu to be created. This value is
		 * <code>null</code> if the parent is a menu.
		 */
		private final Control control;

		/**
		 * The delegate from which to load the menu.
		 */
		private final IWorkbenchWindowPulldownDelegate delegate;

		/**
		 * The loaded menu. This value is <code>null</code> if the load
		 * failed, or if it hasn't been loaded yet.
		 */
		private Menu menu = null;

		/**
		 * The parent for the menu to be created. This value is
		 * <code>null</code> if the parent is a control.
		 */
		private final Menu parent;

		/**
		 * Constructs a new instance of <code>MenuLoader</code>
		 * 
		 * @param delegate
		 *            The delegate from which the menu will be loaded; this
		 *            value must not be <code>null</code>.
		 * @param parent
		 *            The parent of the menu to be loaded; this value must not
		 *            be <code>null</code>.
		 */
		private MenuLoader(final IWorkbenchWindowPulldownDelegate2 delegate,
				final Menu parent) {
			this.delegate = delegate;
			this.parent = parent;
			this.control = null;
		}

		/**
		 * Constructs a new instance of <code>MenuLoader</code>
		 * 
		 * @param delegate
		 *            The delegate from which the menu will be loaded; this
		 *            value must not be <code>null</code>.
		 * @param parent
		 *            The parent of the menu to be loaded; this value must not
		 *            be <code>null</code>.
		 */
		private MenuLoader(final IWorkbenchWindowPulldownDelegate delegate,
				final Control parent) {
			this.delegate = delegate;
			this.parent = null;
			this.control = parent;
		}

		/**
		 * Returns the menu loaded, if any.
		 * 
		 * @return the loaded menu, or <code>null</code> if none.
		 */
		private Menu getMenu() {
			return menu;
		}

		/**
		 * @see ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			// Do nothing
		}

		/**
		 * @see ISafeRunnable#run()
		 */
		public void run() throws Exception {
			if (parent == null) {
				menu = delegate.getMenu(control);
			} else {
				menu = ((IWorkbenchWindowPulldownDelegate2) delegate)
						.getMenu(parent);
			}
		}
	}

	/**
	 * The configuration element from which the delegate can be created. This
	 * value will exist until the element is converted into a real class -- at
	 * which point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The real delegate. This value is <code>null</code> until the proxy is
	 * forced to load the real delegate. At this point, the configuration
	 * element is converted, nulled out, and this delegate gains a reference.
	 */
	private IWorkbenchWindowPulldownDelegate delegate = null;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real delegate.
	 */
	private final String delegateAttributeName;

	/**
	 * The widget created for this pulldown delegate. If this proxy has not been
	 * asked to fill or it has been disposed, then this value is
	 * <code>null</code>.
	 */
	private Widget widget = null;

	/**
	 * Constructs a new instance of <code>PulldownDelegateWidgetProxy</code>
	 * with all the information it needs to try to avoid loading until it is
	 * needed.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param delegateAttributeName
	 *            The name of the attibute or element containing the delegate;
	 *            must not be <code>null</code>.
	 */
	public PulldownDelegateWidgetProxy(
			final IConfigurationElement configurationElement,
			final String delegateAttributeName) {
		if (configurationElement == null) {
			throw new NullPointerException(
					"The configuration element backing a handler proxy cannot be null"); //$NON-NLS-1$
		}

		if (delegateAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the handler class must be known"); //$NON-NLS-1$
		}

		this.configurationElement = configurationElement;
		this.delegateAttributeName = delegateAttributeName;
	}

	/**
	 * Passes the dipose on to the proxied handler, if it has been loaded.
	 */
	public final void dispose() {
		if (delegate != null) {
			delegate.dispose();
		}
	}

	public void fill(Composite parent) {
		// TODO Auto-generated method stub

	}

	public void fill(CoolBar parent, int index) {
		// Does nothing.  Not supported.
	}

	private final DisposeListener disposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			if (e.widget == widget) {
				dispose();
				widget = null;

				// TODO Is this necessary?
				// disposeOldImages();
			}
		}
	};

	public final void fill(final Menu parent, final int index) {
		// Create the menu item.
		final MenuItem menuItem;
		if (index >= 0)
			menuItem = new MenuItem(parent, SWT.CASCADE, index);
		else
			menuItem = new MenuItem(parent, SWT.CASCADE);
		menuItem.setData(this);
		widget = menuItem;

		// Create the submenu.
		if (loadDelegate()
				&& (delegate instanceof IWorkbenchWindowPulldownDelegate2)) {
			final IWorkbenchWindowPulldownDelegate2 delegate2 = (IWorkbenchWindowPulldownDelegate2) delegate;
			final MenuLoader loader = new MenuLoader(delegate2, parent);
			Platform.run(loader);
			final Menu subMenu = loader.getMenu();
			if (subMenu != null) {
				menuItem.setMenu(subMenu);
			}
		}

		menuItem.addDisposeListener(disposeListener);
		
		// TODO Needs a way to be linked to a command.
		//menuItem.addListener(SWT.Selection, getMenuItemListener());
		//if (action.getHelpListener() != null)
		//	menuItem.addHelpListener(action.getHelpListener());

		// TODO Needs a way of updating itself
		// update(null);
	}

	public void fill(ToolBar parent, int index) {
		// Create the menu item.
		final ToolItem toolItem;
		if (index >= 0)
			toolItem = new ToolItem(parent, SWT.DROP_DOWN, index);
		else
			toolItem = new ToolItem(parent, SWT.DROP_DOWN);
		widget = toolItem;
		toolItem.setData(this);
		
		toolItem.addDisposeListener(disposeListener);
		
		// TODO Needs a way to be linked to a command.
		// TODO The menu is created on selection.
		//menuItem.addListener(SWT.Selection, getMenuItemListener());
		//if (action.getHelpListener() != null)
		//	menuItem.addHelpListener(action.getHelpListener());

		// TODO Needs a way of updating itself
		// update(null);
	}

	/**
	 * Loads the delegate, if possible. If the delegate is loaded, then the
	 * member variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the delegate is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadDelegate() {
		if (delegate == null) {
			// Load the handler.
			try {
				delegate = (IWorkbenchWindowPulldownDelegate) configurationElement
						.createExecutableExtension(delegateAttributeName);
				configurationElement = null;
				return true;

			} catch (final ClassCastException e) {
				final String message = "The proxied delegate was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			} catch (final CoreException e) {
				final String message = "The proxied delegate for '" + configurationElement.getAttribute(delegateAttributeName) //$NON-NLS-1$
						+ "' could not be loaded"; //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;
			}
		}

		return true;
	}

	public final String toString() {
		if (delegate == null) {
			return configurationElement.getAttribute(delegateAttributeName);
		}

		return delegate.toString();
	}
}
