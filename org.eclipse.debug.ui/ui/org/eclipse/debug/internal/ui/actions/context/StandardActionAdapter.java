/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions.context;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.internal.ui.viewers.AsynchronousSchedulingRuleFactory;

/**
 * Common function for standard debug action adapter implemetnations.
 * 
 * @since 3.2
 *
 */
public class StandardActionAdapter {

	/**
	 * Scheduling rule for updating action enablement.
	 * 
	 * @return scheduling rule or <code>null</code>
	 */
	protected ISchedulingRule createUpdateSchedulingRule() {
		return AsynchronousSchedulingRuleFactory.getDefault().newSerialRule();
	}
}
