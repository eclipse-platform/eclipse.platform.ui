package org.eclipse.update.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.URL;
import org.eclipse.update.core.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.model.*;
import java.util.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.update.internal.ui.*;

public class AvailableUpdates extends ModelObject implements IWorkbenchAdapter {
private static final String KEY_NAME = "AvailableUpdates.name";
private static final String KEY_BEGIN = "AvailableUpdates.search.begin";
private static final String KEY_CONTACTING = "AvailableUpdates.search.contacting";
private static final String KEY_CHECKING = "AvailableUpdates.search.checking";

	public static final String P_REFRESH = "p_refresh";

	private Vector updates = new Vector();
	private boolean searchInProgress;
	private BackgroundProgressMonitor backgroundProgress;
	private BackgroundThread searchThread;
	private boolean debug = false;

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
		return updates.size()>0;
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
	
	public void startSearch(Display display) throws InvocationTargetException, InterruptedException {
		if (searchInProgress) return;
		backgroundProgress.setDisplay(display);
		IRunnableWithProgress operation = getSearchOperation();
		searchThread = new BackgroundThread(operation, backgroundProgress, Display.getDefault());
		searchInProgress = true;
		searchThread.start();
		Throwable throwable= searchThread.getThrowable();
		if (throwable != null) {
			if (debug) {
				System.err.println("Exception in search background thread:");//$NON-NLS-1$
				throwable.printStackTrace();
				System.err.println("Called from:");//$NON-NLS-1$
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
		if (!searchInProgress || searchThread==null) return;
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
		
		IFeature [] candidates = getInstalledFeatures();
		backgroundProgress.beginTask(UpdateUIPlugin.getResourceString(KEY_BEGIN), candidates.length);
		for (int i=0; i<candidates.length; i++) {
			if (monitor.isCanceled()) {
				break;
			}
			IFeature feature = candidates[i];
			String versionedLabel = feature.getLabel();
			String version = feature.getIdentifier().getVersion().toString();
			versionedLabel += " "+version+":";
			backgroundProgress.setTaskName(versionedLabel);
			findUpdates(candidates[i]);
			backgroundProgress.worked(1);
		}
		searchInProgress = false;
		monitor.done();
		UpdateModel model = getModel();
		asyncFireObjectChanged(this, P_REFRESH);
	}
	
	private IFeature [] getInstalledFeatures() {
		IFeature [] result = new IFeature[0];
		try {
			Vector candidates = new Vector();
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfigurationSite [] isites = config.getConfigurationSites();
			for (int i=0; i<isites.length; i++) {
				ISite isite = isites[i].getSite();
				IFeatureReference [] refs = isite.getFeatureReferences();
				for (int j=0; j<refs.length; j++) {
					IFeatureReference ref = refs[j];
					candidates.add(ref.getFeature());
				}
			}
			result=(IFeature[])candidates.toArray(new IFeature[candidates.size()]);
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e, false);
		}
		finally {
			return result;
		}
	}
	
	private void findUpdates(IFeature feature) {
		IInfo updateInfo = feature.getUpdateInfo();
		if (updateInfo == null) return;
		URL updateURL = updateInfo.getURL();
		if (updateURL==null) return;
		String pattern = UpdateUIPlugin.getResourceString(KEY_CONTACTING);
		String text = UpdateUIPlugin.getFormattedMessage(pattern, updateURL.toString());
		backgroundProgress.subTask(text);
		try {
			ISite site = SiteManager.getSite(updateURL);
			backgroundProgress.subTask(UpdateUIPlugin.getResourceString(KEY_CHECKING));
			IFeatureReference [] refs = site.getFeatureReferences();
			UpdateSearchSite searchSite = null;
			for (int i=0; i<refs.length; i++) {
				IFeature candidate = refs[i].getFeature();
				if (isNewerVersion(feature, candidate)) {
					// bingo - add this
					if (searchSite==null) {
						searchSite = new UpdateSearchSite(updateInfo.getText(), site);
						updates.add(searchSite);
						asyncFireObjectAdded(this, searchSite);
					}
					searchSite.addCandidate(candidate);
					asyncFireObjectAdded(searchSite, candidate);
				}
			}
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e, false);
		}
	}
	
	private void asyncFireObjectAdded(final Object parent, final Object child) {
		Display display = backgroundProgress.getDisplay();
		final UpdateModel model = getModel();
		display.asyncExec(new Runnable() {
			public void run() {
				model.fireObjectAdded(parent, child);
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
		VersionedIdentifier fvi = feature.getIdentifier();
		VersionedIdentifier cvi = candidate.getIdentifier();
		if (!fvi.getIdentifier().equals(cvi.getIdentifier())) return false;
		Version fv = fvi.getVersion();
		Version cv = cvi.getVersion();
		return cv.compare(fv) > 0;
	}
}