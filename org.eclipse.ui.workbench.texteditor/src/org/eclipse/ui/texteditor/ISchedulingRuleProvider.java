/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A scheduling rule provider provides a scheduling rule which
 * can be used when running operations.
 *
 * @since 3.0
 */
public interface ISchedulingRuleProvider {

	/**
	 * Returns the scheduling rule.
	 *
	 * @return a scheduling rule or <code>null</code> if none
	 */
	ISchedulingRule getSchedulingRule();
}
