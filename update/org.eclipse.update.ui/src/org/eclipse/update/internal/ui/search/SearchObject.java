package org.eclipse.update.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.EnvironmentUtil;

public class SearchObject extends NamedModelObject {
	public static final String P_REFRESH = "p_refresh";
	public static final String P_CATEGORY = "p_category";

	private static final String KEY_NAME = "Search.name";
	private static final String KEY_BEGIN = "Search.begin";
	private static final String KEY_MY_COMPUTER = "Search.myComputer";
	private static final String KEY_CONTACTING = "Search.contacting";
	private static final String KEY_CHECKING = "Search.checking";
	protected static final String S_MY_COMPUTER = "searchMyComputer";
	protected static final String S_BOOKMARKS = "searchBookmarks";
	protected static final String S_DISCOVERY = "searchDiscovery";
	protected static final String S_FILTER = "searchFilter";
	protected static final String S_DRIVES = "searchDrives";

	private Vector result = new Vector();
	private boolean searchInProgress;
	private BackgroundProgressMonitor backgroundProgress;
	private BackgroundThread searchThread;
	private boolean debug = false;
	private ISite[] myComputerSites = null;
	private String categoryId;
	private boolean categoryFixed;
	private Hashtable settings = new Hashtable();
	private boolean persistent = true;
	private boolean instantSearch = false;

	class SearchAdapter extends MonitorAdapter {
		public void done() {
			searchInProgress = false;
		}
	}

	public SearchObject() {
		this("", null, false);
	}
	public SearchObject(String name, SearchCategoryDescriptor descriptor) {
		this(name, descriptor, false);
	}
	public SearchObject(
		String name,
		SearchCategoryDescriptor descriptor,
		boolean categoryFixed) {
		super(name);
		this.categoryId = descriptor.getId();
		this.categoryFixed = categoryFixed;
		backgroundProgress = new BackgroundProgressMonitor();
		backgroundProgress.addProgressMonitor(new SearchAdapter());
	}

	public boolean isCategoryFixed() {
		return categoryFixed;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean value) {
		this.persistent = value;
	}

	public boolean isInstantSearch() {
		return instantSearch;
	}

	public void setInstantSearch(boolean value) {
		this.instantSearch = value;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String id) {
		if (categoryId != null && !categoryId.equals(id)) {
			settings.clear();
		}
		this.categoryId = id;
		notifyObjectChanged(P_CATEGORY);
	}

	public Hashtable getSettings() {
		return settings;
	}

	public boolean getSearchMyComputer() {
		return getBooleanValue(S_MY_COMPUTER);
	}

	public void setSearchMyComputer(boolean value) {
		settings.put(S_MY_COMPUTER, value ? "true" : "false");
	}

	public void setSearchBookmarks(boolean value) {
		setBooleanValue(S_BOOKMARKS, value);
	}
	public boolean getSearchBookmarks() {
		return getBooleanValue(S_BOOKMARKS);
	}

	public void setSearchDiscovery(boolean value) {
		setBooleanValue(S_DISCOVERY, value);
	}
	public boolean getSearchDiscovery() {
		return getBooleanValue(S_DISCOVERY);
	}
	public String getDriveSettings() {
		return (String)settings.get(S_DRIVES);
	}
	public void setDriveSettings(String drives) {
		settings.put(S_DRIVES, drives);
	}
	
	public void setFilterEnvironment(boolean value) {
		setBooleanValue(S_FILTER, !value);
	}
	public boolean getFilterEnvironment() {
		return !getBooleanValue(S_FILTER);
	}

	private boolean getBooleanValue(String key) {
		String value = (String) settings.get(key);
		if (value != null && value.equals("true"))
			return true;
		return false;
	}
	private void setBooleanValue(String key, boolean value) {
		settings.put(key, value ? "true" : "false");
	}

	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return result.toArray();
	}

	public boolean hasChildren() {
		return result.size() > 0;
	}

	public void attachProgressMonitor(IProgressMonitor monitor) {
		backgroundProgress.addProgressMonitor(monitor);
	}
	public void detachProgressMonitor(IProgressMonitor monitor) {
		backgroundProgress.removeProgressMonitor(monitor);
	}

	public void startSearch(Display display, ISearchQuery[] queries)
		throws InvocationTargetException, InterruptedException {
		if (searchInProgress)
			return;
		backgroundProgress.setDisplay(display);
		IRunnableWithProgress operation = getSearchOperation(queries);
		searchThread =
			new BackgroundThread(operation, backgroundProgress, Display.getDefault());
		searchInProgress = true;
		searchThread.start();
		Throwable throwable = searchThread.getThrowable();
		if (throwable != null) {
			if (debug) {
				System.err.println("Exception in search background thread:"); //$NON-NLS-1$
				throwable.printStackTrace();
				System.err.println("Called from:"); //$NON-NLS-1$
				// Don't create the InvocationTargetException on the throwable,
				// otherwise it will print its stack trace (from the other thread).
				new InvocationTargetException(null).printStackTrace();
			}
			if (throwable instanceof InvocationTargetException) {
				throw (InvocationTargetException) throwable;
			} else if (throwable instanceof InterruptedException) {
				throw (InterruptedException) throwable;
			} else if (throwable instanceof OperationCanceledException) {
				// See 1GAN3L5: ITPUI:WIN2000 - ModalContext converts OperationCancelException into InvocationTargetException
				throw new InterruptedException(throwable.getMessage());
			} else {
				throw new InvocationTargetException(throwable);
			}
		}
	}

	public boolean isSearchInProgress() {
		return searchInProgress;
	}

	public void stopSearch() {
		if (!searchInProgress || searchThread == null)
			return;
		backgroundProgress.setCanceled(true);
	}

	public IRunnableWithProgress getSearchOperation(final ISearchQuery[] queries) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doSearch(queries, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
	}

	private void doSearch(ISearchQuery[] queries, IProgressMonitor monitor)
		throws CoreException {
		result.clear();
		asyncFireObjectChanged(this, P_REFRESH);

		ArrayList candidates = new ArrayList();

		backgroundProgress.beginTask(
			UpdateUIPlugin.getResourceString(KEY_BEGIN),
			IProgressMonitor.UNKNOWN);

		if (getSearchMyComputer()) {
			backgroundProgress.setTaskName(
				UpdateUIPlugin.getResourceString(KEY_MY_COMPUTER));
			initializeMyComputerSites(monitor);
		}
		computeSearchSources(candidates);
		int ntasks = queries.length * (1 + candidates.size());

		backgroundProgress.beginTask(
			UpdateUIPlugin.getResourceString(KEY_BEGIN),
			ntasks);
			
		for (int i = 0; i < queries.length; i++) {
			ISearchQuery query = queries[i];
			ISiteAdapter site = query.getSearchSite();
			if (site != null) {
				searchOneSite(site, query, monitor);
				if (monitor.isCanceled())
					break;
			}
			monitor.worked(1);

			for (int j = 0; j < candidates.size(); j++) {
				if (monitor.isCanceled()) {
					break;
				}
				Object source = candidates.get(j);
				searchOneSite((ISiteAdapter) source, query, monitor);
				monitor.worked(1);
			}
			if (monitor.isCanceled())
				break;
		}
		searchInProgress = false;
		monitor.done();
		asyncFireObjectChanged(this, P_REFRESH);
	}

	public void computeSearchSources(ArrayList sources) {
		addMyComputerSites(sources);
		addBookmarks(sources);
	}

	private void searchOneSite(
		ISiteAdapter siteAdapter,
		ISearchQuery query,
		IProgressMonitor monitor)
		throws CoreException {
		String pattern = UpdateUIPlugin.getResourceString(KEY_CONTACTING);
		String text =
			UpdateUIPlugin.getFormattedMessage(pattern, siteAdapter.getLabel());
		monitor.subTask(text);
		URL siteURL = siteAdapter.getURL();
		ISite site = SiteManager.getSite(siteURL);

		monitor.subTask(UpdateUIPlugin.getResourceString(KEY_CHECKING));
		IFeatureReference[] refs = site.getFeatureReferences();

		for (int i = 0; i < refs.length; i++) {
			IFeature candidate = refs[i].getFeature();
			// filter out the feature for environment
			if (getFilterEnvironment() && isValidEnvironment(candidate)==false) continue;
			
			if (query.matches(candidate)) {
				// bingo - add this
				SearchResultSite searchSite = findResultSite(site);
				if (searchSite == null) {
					searchSite = new SearchResultSite(this, siteAdapter.getLabel(), site);
					result.add(searchSite);
					asyncFireObjectAdded(this, searchSite);
				}
				SimpleFeatureAdapter featureAdapter = new SimpleFeatureAdapter(candidate);
				searchSite.addCandidate(featureAdapter);
				asyncFireObjectAdded(searchSite, featureAdapter);
			}
		}
	}
	
	private SearchResultSite findResultSite(ISite site) {
		for (int i=0; i<result.size(); i++) {
			SearchResultSite resultSite = (SearchResultSite)result.get(i);
			if (resultSite.getSite().equals(site)) return resultSite;
		}
		return null;
	}
	
	private boolean isValidEnvironment(IFeature candidate) {
		return EnvironmentUtil.isValidEnvironment(candidate);
	}

	private void asyncFireObjectAdded(final Object parent, final Object child) {
		Display display = backgroundProgress.getDisplay();
		final UpdateModel model = getModel();
		display.asyncExec(new Runnable() {
			public void run() {
				model.fireObjectsAdded(parent, new Object[] { child });
			}
		});
	}

	private void asyncFireObjectChanged(final Object obj, final String property) {
		Display display = backgroundProgress.getDisplay();
		final UpdateModel model = getModel();
		display.asyncExec(new Runnable() {
			public void run() {
				model.fireObjectChanged(obj, property);
			}
		});
	}

	private void initializeMyComputerSites(IProgressMonitor monitor) {
		Vector sites = new Vector();
		MyComputer myComputer = new MyComputer();
		MyComputerSearchSettings settings = new MyComputerSearchSettings(this);
		myComputer.collectSites(sites, settings, monitor);
		if (sites.size() > 0) {
			myComputerSites = (ISite[]) sites.toArray(new ISite[sites.size()]);
		} else
			myComputerSites = null;
	}

	private void addMyComputerSites(ArrayList result) {
		if (myComputerSites != null && getSearchMyComputer()) {
			for (int i = 0; i < myComputerSites.length; i++) {
				result.add(myComputerSites[i]);
			}
		}
	}
	private void addBookmarks(ArrayList result) {
		if (getSearchBookmarks() == false)
			return;
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		SiteBookmark[] bookmarks = model.getBookmarkLeafs();
		for (int i = 0; i < bookmarks.length; i++) {
			SiteBookmark bookmark = bookmarks[i];
			result.add(bookmark);
		}
	}
}