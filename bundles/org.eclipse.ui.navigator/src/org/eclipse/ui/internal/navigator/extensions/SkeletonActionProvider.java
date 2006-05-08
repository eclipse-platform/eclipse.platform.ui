/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * 
 * A skeleton action provider is used as a shortcut to avoid a bunch of null
 * checks. Instead, if a client fails to provide a valid Action Provider or
 * there is some problem instantiating the class, a skeleton (no-op singleton)
 * is used in its place. 
 * 
 * @since 3.2
 */
public final  class SkeletonActionProvider extends CommonActionProvider {

	/**
	 * A skeleton action provider is used as a shortcut to avoid a bunch of null
	 * checks. Instead, if a client fails to provide a valid Action Provider or
	 * there is some problem instantiating the class, a skeleton (no-op
	 * singleton) is used in its place.
	 */
	public static final CommonActionProvider INSTANCE = new SkeletonActionProvider();
 
	private SkeletonActionProvider() {
		super();
	}

	public void init(ICommonActionExtensionSite aConfig) {

	}
}
