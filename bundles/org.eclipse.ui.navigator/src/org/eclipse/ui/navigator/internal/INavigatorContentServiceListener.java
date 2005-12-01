/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal;

import org.eclipse.ui.navigator.internal.extensions.NavigatorContentExtension;


/**
 * <p>
 * Used by clients who need to would like to listen for the load event of a
 * NavigatorContentDescriptorInstance.
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface INavigatorContentServiceListener {

	
	void onLoad(NavigatorContentExtension anExtension);
}
