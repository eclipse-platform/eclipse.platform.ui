/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.IOpenListener;

/**
 * <p>
 * Provides a custom interface for clients that require more information than the standard
 * {@link org.eclipse.jface.viewers.IOpenListener}&nbsp;allows. Clients may choose to implement this
 * interface for the openListener attribute of the
 * <b>org.eclipse.wst.common.navigator.views.navigatorContent </b> extension point.
 * </p>
 * <p>
 * Clients need not implement this interface if there is no cause to do so.
 * {@link ICommonOpenListener}&nbsp;is respected by the Common Navigator.
 * <p>
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public interface ICommonOpenListener extends IOpenListener {

	/**
	 * <p>
	 * Allows ICommonOpenListeners to customize their display based on the current viewer.
	 * </p>
	 * 
	 * @param aCommonNavigator
	 *            The instance of {@link CommonNavigator}&nbsp;that the current ICommonOpenListener
	 *            will be associated with
	 * @param aContentService
	 *            The instance of {@link NavigatorContentService}&nbsp;that the current
	 *            ICommonOpenListener will be associated with
	 */
	void initialize(CommonNavigator aCommonNavigator, NavigatorContentService aContentService);
}