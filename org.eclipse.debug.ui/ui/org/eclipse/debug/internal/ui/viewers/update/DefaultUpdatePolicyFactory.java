/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.internal.ui.viewers.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.IUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.IUpdatePolicyFactory;

/**
 * @since 3.2
 */
public class DefaultUpdatePolicyFactory implements IUpdatePolicyFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.IUpdatePolicyFactory#createUpdatePolicy(java.lang.Object,
	 *      org.eclipse.debug.ui.viewers.IPresentationContext)
	 */
	public IUpdatePolicy createUpdatePolicy(IPresentationContext context) {
		return new DefaultUpdatePolicy();
	}

}
