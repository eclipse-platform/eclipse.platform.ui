package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.SWT;
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.IInfo;
import org.eclipse.update.core.IInstallConfiguration;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteChangedListener;
import org.eclipse.update.core.VersionedIdentifier;
import java.util.*;
import org.eclipse.swt.graphics.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class LocalSiteView extends BaseTreeView 
				implements IInstallConfigurationChangedListener,
							ISiteChangedListener {
	
class FolderObject {
	String label;
	
	public FolderObject(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	public String toString() {
		return getLabel();
	}
}

FolderObject myEclipse = new FolderObject("Installed Features");
FolderObject updates = new FolderObject("Available Updates");
Image eclipseImage;
Image updatesImage;
Image featureImage;
Image siteImage;
boolean initialized;
	
class LocalSiteProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			return new Object [] { myEclipse, updates };
		}
		if (parent == myEclipse) {
			return openLocalSite();
		}
		if (parent instanceof ISite) {
			return getSiteFeatures((ISite)parent);
		}
		if (parent == updates) {
			return checkForUpdates();
		}
		return new Object[0];
	}
	
	private Object[] getSiteFeatures(ISite site) {
		try {
			IFeatureReference [] refs = site.getFeatureReferences();
			Object [] result = new Object[refs.length];
			for (int i=0; i<refs.length; i++) {
				result[i] = refs[i].getFeature();
			}
			return result;
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
			return new Object[0];
		}
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object child) {
		if (child instanceof IFeature)
		   return ((IFeature)child).getSite();
		if (child instanceof ISite)
		   return myEclipse;
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object parent) {
		return !(parent instanceof IFeature);
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object input) {
		return getChildren(input);
	}
}

class LocalSiteLabelProvider extends LabelProvider {
	public String getText(Object obj) {
		if (obj instanceof ISite) {
			ISite site = (ISite)obj;
			return site.getURL().toString();
		}
		if (obj instanceof IFeature) {
			IFeature feature = (IFeature)obj;
			String version = feature.getIdentifier().getVersion().toString();
			return feature.getLabel() + " "+version;
		}
		return super.getText(obj);
	}
	public Image getImage(Object obj) {
		if (obj.equals(myEclipse))
		   return eclipseImage;
		if (obj.equals(updates))
		   return updatesImage;
		if (obj instanceof IFeature)
		   return featureImage;
		if (obj instanceof ISite)
		   return siteImage;
		return null;
	}
}

public LocalSiteView() {
	eclipseImage = UpdateUIPluginImages.DESC_ECLIPSE_OBJ.createImage();
	updatesImage = UpdateUIPluginImages.DESC_UPDATES_OBJ.createImage();
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
}

public void initProviders() {
	viewer.setContentProvider(new LocalSiteProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
	viewer.setLabelProvider(new LocalSiteLabelProvider());
}

private Object [] openLocalSite() {
	try {
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		Object [] result = config.getInstallSites();
		if (!initialized) {
			config.addInstallConfigurationChangedListener(this);
			initialized = true;
		}
		return result;
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
		return new Object[0];
	}
}

private Object [] checkForUpdates() {
	BusyIndicator.showWhile(viewer.getTree().getDisplay(),
					new Runnable() {
		public void run() {
			try {
			   Thread.currentThread().sleep(2000);
			}
			catch (InterruptedException e) {
			}
		}
	});
	return new Object[0];
}

public void dispose() {
	eclipseImage.dispose();
	featureImage.dispose();
	siteImage.dispose();
	updatesImage.dispose();
	
	if (initialized) {
		try {
		   	ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			config.removeInstallConfigurationChangedListener(this);
		}
		catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
		initialized=false;
	}
	super.dispose();
}

private void registerListeners() {
	try {
	   	ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		config.addInstallConfigurationChangedListener(this);
		ISite [] isites = config.getInstallSites();
		for (int i=0; i<isites.length; i++) {
			ISite site = isites[i];
			site.addSiteChangedListener(this);
		}
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
	}
}

private void unregisterListeners() {
	try {
	   	ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		config.removeInstallConfigurationChangedListener(this);
		ISite [] isites = config.getInstallSites();
		for (int i=0; i<isites.length; i++) {
			ISite site = isites[i];
			site.removeSiteChangedListener(this);
		}
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
	}
}

	/**
	 * @see IInstallConfigurationChangedListener#installSiteAdded(ISite)
	 */
	public void installSiteAdded(ISite site) {
		viewer.add(myEclipse, site);
	}

	/**
	 * @see IInstallConfigurationChangedListener#installSiteRemoved(ISite)
	 */
	public void installSiteRemoved(ISite site) {
		viewer.remove(site);
	}

	/**
	 * @see IInstallConfigurationChangedListener#linkedSiteAdded(ISite)
	 */
	public void linkedSiteAdded(ISite site) {
	}

	/**
	 * @see IInstallConfigurationChangedListener#linkedSiteRemoved(ISite)
	 */
	public void linkedSiteRemoved(ISite site) {
	}

	/**
	 * @see ISiteChangedListener#featureUpdated(IFeature)
	 */
	public void featureUpdated(IFeature feature) {
		viewer.update(feature, new String [0]);
	}

	/**
	 * @see ISiteChangedListener#featureInstalled(IFeature)
	 */
	public void featureInstalled(IFeature feature) {
		viewer.add(feature.getSite(), feature);
	}

	/**
	 * @see ISiteChangedListener#featureUninstalled(IFeature)
	 */
	public void featureUninstalled(IFeature feature) {
		viewer.remove(feature);
	}

}
