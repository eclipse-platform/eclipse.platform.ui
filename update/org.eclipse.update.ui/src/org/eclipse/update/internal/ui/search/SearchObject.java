package org.eclipse.update.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.jface.dialogs.IDialogSettings;

public class SearchObject extends NamedModelObject {
	public static final String P_REFRESH = "p_refresh";
	public static final String P_CATEGORY = "p_category";

	private static final String KEY_NAME = "Search.name";
	private static final String KEY_BEGIN = "Search.begin";
	private static final String KEY_MY_COMPUTER = "Search.myComputer";
	private static final String KEY_CONTACTING = "Search.contacting";
	private static final String KEY_CHECKING = "Search.checking";
	private static final String SETTINGS_SECTION = "search";
	private static final String S_MY_COMPUTER = "searchMyComputer";

	private Vector result = new Vector();
	private boolean searchInProgress;
	private BackgroundProgressMonitor backgroundProgress;
	private BackgroundThread searchThread;
	private boolean debug = false;
	private ISite[] myComputerSites = null;
	private String categoryId;
	private boolean categoryFixed;
	private Hashtable settings = new Hashtable();

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
	public SearchObject(String name, SearchCategoryDescriptor descriptor, boolean categoryFixed) {
		super(name);
		this.categoryId = descriptor.getId();
		this.categoryFixed = categoryFixed;
		backgroundProgress = new BackgroundProgressMonitor();
		backgroundProgress.addProgressMonitor(new SearchAdapter());
	}
	
	public boolean isCategoryFixed() {
		return categoryFixed;
	}

	public String getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(String id) {
		if (categoryId!=null && !categoryId.equals(id)) {
			settings.clear();
		}
		this.categoryId = id;
		notifyObjectChanged(P_CATEGORY);
	}

	public Hashtable getSettings() {
		return settings;
	}

	public static boolean getSearchMyComputer() {
		return getSettingsSection().getBoolean(S_MY_COMPUTER);
	}

	public static void setSearchMyComputer(boolean value) {
		getSettingsSection().put(S_MY_COMPUTER, value);
	}

	private static IDialogSettings getSettingsSection() {
		IDialogSettings master = UpdateUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = master.getSection(SETTINGS_SECTION);
		if (section == null)
			section = master.addNewSection(SETTINGS_SECTION);
		return section;
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
				Object source = candidates.get(i);
				searchOneSite((ISiteAdapter) source, query, monitor);
				monitor.worked(1);
			}
		}
		searchInProgress = false;
		monitor.done();
		asyncFireObjectChanged(this, P_REFRESH);
	}

	public void computeSearchSources(ArrayList sources) {
		addMyComputerSites(sources);
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
		UpdateSearchSite searchSite = null;
		for (int i = 0; i < refs.length; i++) {
			IFeature candidate = refs[i].getFeature();
			if (query.matches(candidate)) {
				// bingo - add this
				if (searchSite == null) {
					searchSite = new UpdateSearchSite(siteAdapter.getLabel(), site);
					result.add(searchSite);
					asyncFireObjectAdded(this, searchSite);
				}
				searchSite.addCandidate(new SimpleFeatureAdapter(candidate));
				asyncFireObjectAdded(searchSite, candidate);
			}
		}
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
		MyComputerSearchSettings settings = new MyComputerSearchSettings();
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
}