/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.search.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchRunner2 {
	private Shell shell;
	private IRunnableContext context;
	private ISearchProvider2 searchProvider;
	private IUpdateSearchResultCollector collector;
	private boolean newSearchNeeded;
	
	public SearchRunner2(Shell shell, IRunnableContext context) {
		this.shell = shell;
		this.context = context;
	}
	
	public void setResultCollector(IUpdateSearchResultCollector collector) {
		this.collector = collector;
	}
	
	public ISearchProvider2 getSearchProvider() {
		return searchProvider;
	}
	
	public void setSearchProvider(ISearchProvider2 searchProvider) {
		if (this.searchProvider!=searchProvider)
			newSearchNeeded = true;
		this.searchProvider = searchProvider;
	}
	
	public void setNewSearchNeeded(boolean value) {
		newSearchNeeded = value;
	}
	
	public boolean isNewSearchNeeded() {
		return newSearchNeeded;
	}

	public void runSearch() {
		if (searchProvider==null) return;
		try {
			context.run(true, true, getSearchOperation(collector));
			newSearchNeeded=false;
		} catch (InterruptedException e) {
			UpdateUI.logException(e);
			return;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				CoreException ce = (CoreException)t;
				IStatus status = ce.getStatus();
				if (status!=null &&
					status.getCode()==ISite.SITE_ACCESS_EXCEPTION) {
					// Just show this but do not throw exception
					// because there may be results anyway.
					ErrorDialog.openError(shell,
						UpdateUI.getString("Connection Error"),
						null, 
						status);
					return;
				}
			}
			UpdateUI.logException(e);
			return;
		}
	}

	private IRunnableWithProgress getSearchOperation(final IUpdateSearchResultCollector collector) {
		final UpdateSearchRequest request = searchProvider.getSearchRequest();
		
		IRunnableWithProgress op = new IRunnableWithProgress () {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					request.performSearch(collector, monitor);
				}
				catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
				finally {
					monitor.done();
				}
			}
		};
		return op;
	}
}
