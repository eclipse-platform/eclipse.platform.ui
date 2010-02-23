/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.WorkbenchException;

/**
 * A workbench window pulldown action.
 */
public class WWinPluginPulldown extends WWinPluginAction {

	/**
	 * The proxy for creating the menu. There is always a menu proxy. This value
	 * can't be <code>null</code>.
	 */
	private final IMenuCreator menuProxy;

	private class MenuProxy implements IMenuCreator {

		/**
		 * A wrapper for loading the menu that defends against possible
		 * exceptions triggered outside of the workbench.
		 * 
		 * @since 3.0
		 */
		private class MenuLoader implements ISafeRunnable {

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
			 *            The parent of the menu to be loaded; this value must
			 *            not be <code>null</code>.
			 */
			private MenuLoader(
					final IWorkbenchWindowPulldownDelegate2 delegate,
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
			 *            The parent of the menu to be loaded; this value must
			 *            not be <code>null</code>.
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
		 * @see IMenuCreator#getMenu(Control)
		 */
		public Menu getMenu(Control parent) {
			IWorkbenchWindowPulldownDelegate delegate = getPulldownDelegate();
			if (delegate != null) {
				final MenuLoader menuLoader = new MenuLoader(delegate, parent);
				SafeRunner.run(menuLoader);
				return menuLoader.getMenu();
			}

			return null;
		}

		/**
		 * @see IMenuCreator#getMenu(Menu)
		 */
		public Menu getMenu(Menu parent) {
			IWorkbenchWindowPulldownDelegate delegate = getPulldownDelegate();

			if (delegate instanceof IWorkbenchWindowPulldownDelegate2) {
				IWorkbenchWindowPulldownDelegate2 delegate2 = (IWorkbenchWindowPulldownDelegate2) delegate;
				final MenuLoader menuLoader = new MenuLoader(delegate2, parent);
				SafeRunner.run(menuLoader);
				return menuLoader.getMenu();
			}

			return null;
		}

		/**
		 * @see IMenuCreator#dispose()
		 */
		public void dispose() {
			// do nothing
		}
	}

	/**
	 * Constructs a new instance of <code>WWinPluginPulldown</code>.
	 * 
	 * @param actionElement
	 *            The registry element from which the pulldown delegate should
	 *            be created; must not be <code>null</code>.
	 * @param id
	 *            The identifier of this action delegate; may be
	 *            <code>null</code>.
	 * @param window
	 *            The workbench window on which this pulldown should act; must
	 *            not be <code>null</code>.
	 * @param style
	 *            The style.
	 */
	public WWinPluginPulldown(IConfigurationElement actionElement,
			IWorkbenchWindow window, String id, int style) {
		super(actionElement, window, id, style);
		menuProxy = new MenuProxy();
		setMenuCreator(menuProxy);
	}

	/*
	 * (non-Javadoc) Method declared on PluginAction.
	 */
	protected IActionDelegate validateDelegate(Object obj)
			throws WorkbenchException {
		if (obj instanceof IWorkbenchWindowPulldownDelegate) {
			return (IWorkbenchWindowPulldownDelegate) obj;
		}

		throw new WorkbenchException(
				"Action must implement IWorkbenchWindowPulldownDelegate"); //$NON-NLS-1$
	}

	/**
	 * Returns the pulldown delegate. If it does not exist it is created. Can
	 * return <code>null</code> if delegate creation failed.
	 */
	protected IWorkbenchWindowPulldownDelegate getPulldownDelegate() {
		IActionDelegate delegate = getDelegate();
		if (delegate == null) {
			createDelegate();
			delegate = getDelegate();
		}
		return (IWorkbenchWindowPulldownDelegate) delegate;
	}
}
