/*
 * Created on May 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.search;

import org.eclipse.core.runtime.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdateSearchRequest {
	private UpdateSearchInput input;
	private IUpdateSearchResultCollector collector;

	public UpdateSearchRequest(UpdateSearchInput input, IUpdateSearchResultCollector resultCollector) {
		this.input = input;
		this.collector = collector;
	}

	public void execute(IProgressMonitor monitor) throws CoreException {
	}
}