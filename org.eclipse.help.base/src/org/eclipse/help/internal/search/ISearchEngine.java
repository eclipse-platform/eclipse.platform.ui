/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.internal.search;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ISearchEngine {
	void run(String query, ISearchScope scope, ISearchEngineResultCollector collector, IProgressMonitor monitor) throws CoreException;
	void cancel();
}
