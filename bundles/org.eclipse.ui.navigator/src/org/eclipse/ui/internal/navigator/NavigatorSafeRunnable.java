/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.5
 */
public abstract class NavigatorSafeRunnable extends SafeRunnable {

	protected String _message;
	protected IConfigurationElement _element;
	protected Object _object;

	public NavigatorSafeRunnable() {
		super();
	}

	public NavigatorSafeRunnable(String message) {
		_message = message;
	}

	public NavigatorSafeRunnable(IConfigurationElement element) {
		_element = element;
	}

	/**
	 * @param object
	 *            an object to provide additional context
	 */
	public NavigatorSafeRunnable(IConfigurationElement element, Object object) {
		_element = element;
		_object = object;
	}

	@Override
	public abstract void run() throws Exception;

	@Override
	public void handleException(Throwable e) {
		String msg = _message;
		if (msg == null)
			msg = e.getMessage() != null ? e.getMessage() : e.toString();
		if (_element != null) {
			msg += ": " + //$NON-NLS-1$
					NLS.bind(CommonNavigatorMessages.Exception_Invoking_Extension, new Object[] {
							_element.getAttribute("id") + ": " + _element.getName(), _object }); //$NON-NLS-1$ //$NON-NLS-2$
		}
		NavigatorPlugin.log(IStatus.ERROR, 0, msg, e);
	}

}
