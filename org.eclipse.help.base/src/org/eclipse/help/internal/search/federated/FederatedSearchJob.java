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
package org.eclipse.help.internal.search.federated;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A federated search job.
 */
public class FederatedSearchJob extends Job {
	public static final String FAMILY = "org.eclipse.help.base.searchEngine"; //$NON-NLS-1$
	private String expression;
	private FederatedSearchEntry entry;

	/**
	 * @param name
	 */
	public FederatedSearchJob(String expression, FederatedSearchEntry entry) {
		super(entry.getEngineName());
		this.expression = expression;
		this.entry = entry;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		try {
			entry.getEngine().run(expression, entry.getScope(), entry.getResultCollector(), monitor);
			return Status.OK_STATUS;
		}
		catch (CoreException e) {
			return e.getStatus();
		}
	}
	public boolean belongsTo(Object family) {
		return family.equals(FAMILY);
	}
}
