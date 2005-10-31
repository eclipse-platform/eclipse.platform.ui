/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * <p>
 * A custom interface for Common Navigator extensions that either (1) require
 * more information about the specific
 * {@link org.eclipse.ui.navigator.CommonViewer}&nbsp;they are
 * associated with, or (2) would like to return a custom description for use in
 * the Status Bar. Clients may choose to implement this interface for the
 * <i>labelProvider</i> attribute of the
 * <b>org.eclipse.ui.navigator.navigatorContent </b> extension
 * point.
 * </p>
 * <p>
 * Clients need not implement this interface if there is no cause to do so.
 * {@link org.eclipse.jface.viewers.ILabelProvider}&nbsp;is respected by the
 * Common Navigator.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public interface ICommonLabelProvider extends ILabelProvider, IMementoAware,
		IDescriptionProvider {

	/**
	 * <p>
	 * Provides the viewer id for any label provider initialization.
	 * </p>
	 * 
	 * @param aViewerId
	 *            The ID of the viewer that will be associated with the current
	 *            ICommonLabelProvider
	 */
	void init(IExtensionStateModel aStateModel,
			ITreeContentProvider aContentProvider);

}