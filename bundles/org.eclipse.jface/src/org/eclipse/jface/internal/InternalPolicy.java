/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal;

import java.util.Map;

/**
 * Internal class used for non-API debug flags.
 * 
 * @since 3.3
 */
public class InternalPolicy {

	/**
	 * (NON-API) A flag to indicate whether reentrant viewer calls should always be
	 * logged. If false, only the first reentrant call will cause a log entry.
	 * 
	 * @since 3.3
	 */
	public static boolean DEBUG_LOG_REENTRANT_VIEWER_CALLS = false;

	/**
	 * (NON-API) Instead of logging current conflicts they can be
	 * held here.  If there is a problem, they can be reported then.
	 */
	public static Map currentConflicts = null;
}
