/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.internal.search;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FederatedSearchJob extends Job {
	public static final String FAMILY = "org.eclipse.help.base.searchEngine";
	private String expression;
	private FederatedSearchEntry entry;
	private ISearchEngineResultCollector collector;

	/**
	 * @param name
	 */
	public FederatedSearchJob(String expression, FederatedSearchEntry entry, ISearchEngineResultCollector collector) {
		super(entry.getEngineId());
		this.expression = expression;
		this.entry = entry;
		this.collector = collector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		try {
			entry.getEngine().run(expression, entry.getScope(), collector, monitor);
			return Status.OK_STATUS;
		}
		catch (CoreException e) {
			return e.getStatus();
		}
	}
	public boolean belongsTo(Object family) {
		return family == FAMILY;
	}
}
