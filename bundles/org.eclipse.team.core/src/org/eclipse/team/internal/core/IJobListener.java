/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core;

import org.eclipse.core.runtime.QualifiedName;

/**
 * This interface allows interested parties to receive notification
 * when work has started or stopped for a given job type. The <code>started</code>
 * method is invoked when the first job is started for the given <code>jobType</code>.
 * The <code>finish</code> method is called when the last job of a given type stops.
 * Several jobs for the job type may start and stop in the interum without causing
 * notification to the listener.
 */
public interface IJobListener {
	public void started(QualifiedName jobType);
	public void finished(QualifiedName jobType);
}
