package org.eclipse.ui.externaltools.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.ui.IActionFilter;

/**
 * Describes the public attributes for an external tool and the acceptables values
 * each may have.
 * <p>
 * Beside the attributes defined below, all of the extra attribute keys are
 * also available. Only use extra attribute keys which have been made public
 * by clients.
 * </p><p>
 * This interface is not to be extended or implemented by clients
 * </p>
 *
 * @see IActionFilter
 */
public interface IExternalToolFilter extends IActionFilter {
	/**
	 * An attribute indicating the external tool type (value <code>"type"</code>).  
	 * The attribute value should match one of the external tool types defined in 
	 * the external tool's type extension point.
	 */
	public static final String TYPE = "type"; //$NON-NLS-1$
}
