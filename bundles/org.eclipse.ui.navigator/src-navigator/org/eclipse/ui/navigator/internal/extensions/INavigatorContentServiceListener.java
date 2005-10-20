/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;


/**
 * <p>
 * Used by clients who need to would like to listen for the load event of a
 * NavigatorContentDescriptorInstance.
 * <p>
 * The following class is experimental until fully documented.
 * </p>
 */
public interface INavigatorContentServiceListener {

	void onLoad(NavigatorContentExtension anExtension);
}
