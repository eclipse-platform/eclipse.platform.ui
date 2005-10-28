/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.menus.IDynamicMenu;
import org.eclipse.jface.menus.IMenuCollection;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * <p>
 * A proxy for a dynamic menu that has been defined in XML. This delays the
 * class loading until the dynamic menu is really asked to modify a menu
 * collection. Asking a proxy for anything but the attributes defined publicly
 * in this class will cause the proxy to instantiate the proxied handler.
 * </p>
 * 
 * @since 3.2
 */
final class DynamicMenuProxy implements IDynamicMenu {

	/**
	 * The configuration element from which the dynamic menu can be created.
	 * This value will exist until the element is converted into a real class --
	 * at which point this value will be set to <code>null</code>.
	 */
	private IConfigurationElement configurationElement;

	/**
	 * The real dynamic menu. This value is <code>null</code> until the proxy
	 * is forced to load the real dynamic menu. At this point, the configuration
	 * element is converted, nulled out, and this dynamic menu gains a
	 * reference.
	 */
	private IDynamicMenu dynamicMenu = null;

	/**
	 * The name of the configuration element attribute which contains the
	 * information necessary to instantiate the real dynamic menu.
	 */
	private final String dynamicMenuAttributeName;

	/**
	 * Constructs a new instance of <code>DynamicMenuProxy</code> with all the
	 * information it needs to try to load the class at a later point in time.
	 * 
	 * @param configurationElement
	 *            The configuration element from which the real class can be
	 *            loaded at run-time; must not be <code>null</code>.
	 * @param dynamicMenuAttributeName
	 *            The name of the attibute or element containing the dynamic
	 *            menu executable extension; must not be <code>null</code>.
	 */
	public DynamicMenuProxy(final IConfigurationElement configurationElement,
			final String dynamicMenuAttributeName) {
		if (configurationElement == null) {
			throw new NullPointerException(
					"The configuration element backing a dynamic menu proxy cannot be null"); //$NON-NLS-1$
		}

		if (dynamicMenuAttributeName == null) {
			throw new NullPointerException(
					"The attribute containing the dynamic menu class must be known"); //$NON-NLS-1$
		}

		this.configurationElement = configurationElement;
		this.dynamicMenuAttributeName = dynamicMenuAttributeName;
	}

	public final void aboutToShow(final IMenuCollection menu) {
		if (loadDynamicMenu()) {
			dynamicMenu.aboutToShow(menu);
		}
	}

	/**
	 * Loads the dynamic menu, if possible. If the dynamic menu is loaded, then
	 * the member variables are updated accordingly.
	 * 
	 * @return <code>true</code> if the dynamic menu is now non-null;
	 *         <code>false</code> otherwise.
	 */
	private final boolean loadDynamicMenu() {
		if (dynamicMenu == null) {
			// Load the handler.
			try {
				dynamicMenu = (IDynamicMenu) configurationElement
						.createExecutableExtension(dynamicMenuAttributeName);
				configurationElement = null;
				return true;

			} catch (final ClassCastException e) {
				final String message = "The proxied dynamic menu was the wrong class"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			} catch (final CoreException e) {
				final String message = "The proxied dynamic menu for '" + configurationElement.getAttribute(dynamicMenuAttributeName) //$NON-NLS-1$
						+ "' could not be loaded"; //$NON-NLS-1$
				final IStatus status = new Status(IStatus.ERROR,
						WorkbenchPlugin.PI_WORKBENCH, 0, message, e);
				WorkbenchPlugin.log(message, status);
				return false;

			}
		}

		return true;
	}

	public final String toString() {
		if (dynamicMenu == null) {
			return configurationElement.getAttribute(dynamicMenuAttributeName);
		}

		return dynamicMenu.toString();
	}
}
