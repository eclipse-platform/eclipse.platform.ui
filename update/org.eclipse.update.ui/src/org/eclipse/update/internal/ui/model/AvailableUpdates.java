package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.update.internal.ui.*;
import org.eclipse.jface.dialogs.IDialogSettings;

public class AvailableUpdates
	extends ModelObject
	implements IWorkbenchAdapter {
	private static final String KEY_NAME = "AvailableUpdates.name";
	private static final String KEY_BEGIN = "AvailableUpdates.search.begin";
	private static final String KEY_MY_COMPUTER = "AvailableUpdates.search.myComputer";
	private static final String KEY_CONTACTING =
		"AvailableUpdates.search.contacting";
	private static final String KEY_CHECKING = "AvailableUpdates.search.checking";

	public static final String P_REFRESH = "p_refresh";

	private Vector updates = new Vector();
	private boolean searchInProgress;
	private BackgroundProgressMonitor backgroundProgress;
	private BackgroundThread searchThread;
	private boolean debug = false;
	private ISite [] myComputerSites = null;
	private static final String SETTINGS_SECTION = "AvailableUpdates";
	private static final String S_MY_COMPUTER = "searchMyComputer";
	
	public static boolean getSearchMyComputer() {
		return getSettingsSection().getBoolean(S_MY_COMPUTER);
	}
	
	public static void setSearchMyComputer(boolean value) {
		getSettingsSection().put(S_MY_COMPUTER, value);
	}
	
	private static IDialogSettings getSettingsSection() {
		IDialogSettings master = UpdateUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = master.getSection(SETTINGS_SECTION);
		if (section==null) 
		   section = master.addNewSection(SETTINGS_SECTION);
		return section;
	}

	class SearchAdapter extends MonitorAdapter {
		public void done() {
			searchInProgress = false;
		}
	}

	public AvailableUpdates() {
		backgroundProgress = new BackgroundProgressMonitor();
		backgroundProgress.addProgressMonitor(new SearchAdapter());
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public String getName() {
		return UpdateUIPlugin.getResourceString(KEY_NAME);
	}

	public String toString() {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		return updates.toArray();
	}

	public boolean hasUpdates() {
		return updates.size() > 0;
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return UpdateUIPluginImages.DESC_UPDATES_OBJ;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return getModel();
	}

	public void attachProgressMonitor(IProgressMonitor monitor) {
		backgroundProgress.addProgressMonitor(monitor);
	}
	public void detachProgressMonitor(IProgressMonitor monitor) {
		backgroundProgress.removeProgressMonitor(monitor);
	}

	public void startSearch(Display display)
		throws InvocationTargetException, InterruptedException {
		if (searchInProgress)
			return;
		backgroundProgress.setDisplay(display);
		IRunnableWithProgress operation = getSearchOperation();
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

	public IRunnableWithProgress getSearchOperation() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				doSearch(monitor);
			}
		};
	}

	private void doSearch(IProgressMonitor monitor) {
		updates.clear();
		asyncFireObjectChanged(this, P_REFRESH);
		
		IFeature[] candidates = getInstalledFeatures();
		
		backgroundProgress.beginTask(
			UpdateUIPlugin.getResourceString(KEY_BEGIN),
			candidates.length);
		
		if (getSearchMyComputer()) {
			backgroundProgress.setTaskName(UpdateUIPlugin.getResourceString(KEY_MY_COMPUTER));
			initializeMyComputerSites(monitor);
			backgroundProgress.setTaskName(UpdateUIPlugin.getResourceString(KEY_BEGIN));
		}

		for (int i = 0; i < candidates.length; i++) {
			if (monitor.isCanceled()) {
				break;
			}
			IFeature feature = candidates[i];
			String versionedLabel = feature.getLabel();
			String version = feature.getVersionedIdentifier().getVersion().toString();
			versionedLabel += " " + version + ":";
			backgroundProgress.setTaskName(versionedLabel);
			findUpdates(candidates[i]);
			backgroundProgress.worked(1);
		}
		searchInProgress = false;
		monitor.done();
		UpdateModel model = getModel();
		asyncFireObjectChanged(this, P_REFRESH);
	}

	private IFeature[] getInstalledFeatures() {
		IFeature[] result = new IFeature[0];
		try {
			Vector candidates = new Vector();
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfiguredSite[] isites = config.getConfiguredSites();
			for (int i = 0; i < isites.length; i++) {
				ISite isite = isites[i].getSite();
				IFeatureReference[] refs = isite.getFeatureReferences();
				for (int j = 0; j < refs.length; j++) {
					IFeatureReference ref = refs[j];
					candidates.add(ref.getFeature());
				}
			}
			result = (IFeature[]) candidates.toArray(new IFeature[candidates.size()]);
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e, false);
		} finally {
			return result;
		}
	}

	private void findUpdates(IFeature feature) {
		IURLEntry updateInfo = feature.getUpdateSiteEntry();
		Vector searchSources = new Vector();
		if (updateInfo != null) {
			URL updateURL = updateInfo.getURL();
			if (updateURL != null) {
				searchSources.add(updateURL);
			}
		}
		addMyComputerSites(searchSources);

		try {
			for (int i = 0; i < searchSources.size(); i++) {
				Object source = searchSources.get(i);
				if (source instanceof URL)
					searchOneSite(feature, (URL) source, backgroundProgress);
				else if (source instanceof ISite)
					searchOneSite(feature, (ISite) source, backgroundProgress);
			}
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e, false);

		}
	}

	private void searchOneSite(
		IFeature feature,
		URL updateURL,
		IProgressMonitor monitor)
		throws CoreException {
		String pattern = UpdateUIPlugin.getResourceString(KEY_CONTACTING);
		String text = UpdateUIPlugin.getFormattedMessage(pattern, updateURL.toString());
		monitor.subTask(text);
		ISite site = SiteManager.getSite(updateURL);
		searchOneSite(feature, site, monitor);
	}

	private void searchOneSite(
		IFeature feature,
		ISite site,
		IProgressMonitor monitor)
		throws CoreException {
		IURLEntry updateInfo = feature.getUpdateSiteEntry();
		monitor.subTask(UpdateUIPlugin.getResourceString(KEY_CHECKING));
		IFeatureReference[] refs = site.getFeatureReferences();
		UpdateSearchSite searchSite = null;
		for (int i = 0; i < refs.length; i++) {
			IFeature candidate = refs[i].getFeature();
			if (isNewerVersion(feature, candidate)) {
				// bingo - add this
				if (searchSite == null) {
					searchSite = new UpdateSearchSite(updateInfo.getAnnotation(), site);
					updates.add(searchSite);
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
				model.fireObjectsAdded(parent, new Object[]{child});
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

	private boolean isNewerVersion(IFeature feature, IFeature candidate) {
		VersionedIdentifier fvi = feature.getVersionedIdentifier();
		VersionedIdentifier cvi = candidate.getVersionedIdentifier();
		Version fv = fvi.getVersion();
		Version cv = cvi.getVersion();
		return cv.compare(fv) > 0;
	}

	private void initializeMyComputerSites(IProgressMonitor monitor) {
		Vector sites = new Vector();
		MyComputer myComputer = new MyComputer();
		MyComputerSearchSettings settings = new MyComputerSearchSettings();
		myComputer.collectSites(sites, settings, monitor);
		if (sites.size()>0) {
			myComputerSites = (ISite[])sites.toArray(new ISite[sites.size()]);
		}
		else 
		   myComputerSites = null;
	}

	private void addMyComputerSites(Vector result) {
		if (myComputerSites!=null && getSearchMyComputer()) {
			for (int i=0; i<myComputerSites.length; i++) {
				result.add(myComputerSites[i]);
			}
		}
	}
}