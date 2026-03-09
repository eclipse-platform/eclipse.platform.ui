/*******************************************************************************
 * Copyright (c) 2026 Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.reconciler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;

public class ReconcilerJobFamilies {

	/**
	 * Constant identifying the job family identifier for the background reconciler job.
	 *
	 * @see IJobManager#join(Object, IProgressMonitor)
	 */
	public static final Object FAMILY_RECONCILER= new Object();

}
