/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.search.*;

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
	private boolean newSearchNeeded;
	
	public SearchRunner2(Shell shell, IRunnableContext context) {
		this.shell = shell;
		this.context = context;
	}
	
	public ISearchProvider2 getSearchProvider() {
		return searchProvider;
	}
	
	public void setSearchProvider(ISearchProvider2 searchProvider) {
		this.searchProvider = searchProvider;
		newSearchNeeded = true;
	}
	
	public void setNewSearchNeeded(boolean value) {
		newSearchNeeded = value;
	}
	
	public boolean isNewSearchNeeded() {
		return newSearchNeeded;
	}

	public PendingOperation [] runSearch() {
		if (searchProvider==null) return new PendingOperation[0];
		try {
			context.run(true, true, getSearchOperation());
			newSearchNeeded=false;
			return createPendingChanges(searchProvider.getSearch());
		} catch (InterruptedException e) {
			UpdateUI.logException(e);
			return null;
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
					return null;
				}
			}
			UpdateUI.logException(e);
			return null;
		}
	}
	private PendingOperation [] createPendingChanges(SearchObject searchObject) {
		ArrayList result = new ArrayList();
		Object[] sites = searchObject.getChildren(null);
		for (int i = 0; i < sites.length; i++) {
			SearchResultSite site = (SearchResultSite) sites[i];
			createPendingChanges(site, result);
		}
		return (PendingOperation[]) result.toArray(new PendingOperation[result.size()]);
	}

	private void createPendingChanges(
		SearchResultSite site,
		ArrayList result) {
		Object[] candidates = site.getChildren(null);
		for (int i = 0; i < candidates.length; i++) {
			SimpleFeatureAdapter adapter = (SimpleFeatureAdapter) candidates[i];
			try {
				IFeature feature = adapter.getFeature(null);
				IFeature[] installed =
					UpdateManager.getInstalledFeatures(feature);
				IFeature oldFeature = null;
				if (installed.length>0)
					oldFeature = installed[0];
				PendingOperation change = new FeatureInstallOperation(feature);
				result.add(change);
			} catch (CoreException e) {
				UpdateUI.logException(e);
			}
		}
	}
	
	private IRunnableWithProgress getSearchOperation() {
		return searchProvider.getSearch().getSearchOperation(
			shell.getDisplay(),
			searchProvider.getCategory().getQueries());
	}
}
