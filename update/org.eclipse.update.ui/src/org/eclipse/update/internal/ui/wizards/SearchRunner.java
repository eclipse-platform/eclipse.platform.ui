/*
 * Created on Apr 18, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.search.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SearchRunner {
	private Shell shell;
	private IRunnableContext context;
	private ISearchProvider searchProvider;
	private boolean newSearchNeeded;
	
	public SearchRunner(Shell shell, IRunnableContext context) {
		this.shell = shell;
		this.context = context;
	}
	
	public ISearchProvider getSearchProvider() {
		return searchProvider;
	}
	
	public void setSearchProvider(ISearchProvider searchProvider) {
		this.searchProvider = searchProvider;
		newSearchNeeded = true;
	}
	
	public void setNewSearchNeeded(boolean value) {
		newSearchNeeded = value;
	}
	
	public boolean isNewSearchNeeded() {
		return newSearchNeeded;
	}

	public PendingChange [] runSearch() {
		if (searchProvider==null) return new PendingChange[0];
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
	private PendingChange [] createPendingChanges(SearchObject searchObject) {
		ArrayList result = new ArrayList();
		Object[] sites = searchObject.getChildren(null);
		for (int i = 0; i < sites.length; i++) {
			SearchResultSite site = (SearchResultSite) sites[i];
			createPendingChanges(site, result);
		}
		return (PendingChange[]) result.toArray(new PendingChange[result.size()]);
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
					UpdateUI.getInstalledFeatures(feature);
				IFeature oldFeature = null;
				if (installed.length>0)
					oldFeature = installed[0];
				PendingChange change = new PendingChange(oldFeature, feature);
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
