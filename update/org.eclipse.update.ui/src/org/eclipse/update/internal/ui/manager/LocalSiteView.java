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
import org.eclipse.update.core.IConfigurationSite;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.update.ui.internal.model.UpdateModel;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class LocalSiteView extends BaseTreeView 
				implements IInstallConfigurationChangedListener,
							ISiteChangedListener {
private static final String KEY_CURRENT = "LocalSiteView.current";
private Image eclipseImage;
private Image updatesImage;
private Image featureImage;
private Image siteImage;
private Image installSiteImage;
private Image linkedSiteImage;
private boolean initialized;
private UpdateModelChangedListener modelListener;

class UpdateModelChangedListener implements IUpdateModelChangedListener {
	/**
	 * @see IUpdateModelChangedListener#objectAdded(Object, Object)
	 */
	public void objectAdded(Object parent, Object child) {
		if (parent instanceof AvailableUpdates) {
			viewer.add(parent, child);
		}
	}

	/**
	 * @see IUpdateModelChangedListener#objectRemoved(Object, Object)
	 */
	public void objectRemoved(Object parent, Object child) {
		if (parent instanceof AvailableUpdates) {
			viewer.remove(child);
		}
	}

	/**
	 * @see IUpdateModelChangedListener#objectChanged(Object, String)
	 */
	public void objectChanged(Object object, String property) {
		if (object instanceof AvailableUpdates &&
		    property.equals(AvailableUpdates.P_REFRESH)) {
		   viewer.refresh(object);
		   viewer.expandToLevel(object, 999);
		}
	}

}
	
class LocalSiteProvider extends DefaultContentProvider 
						implements ITreeContentProvider {
	/**
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parent) {
		if (parent instanceof UpdateModel) {
			UpdateModel model = (UpdateModel)parent;
			ILocalSite localSite = getLocalSite();
			if (localSite!=null)
				return new Object [] { getLocalSite(), model.getUpdates() };
			else
				return new Object [0];
		}
		if (parent instanceof ILocalSite) {
			return openLocalSite();
		}
		if (parent instanceof IConfigurationSite) {
			IConfigurationSite csite = (IConfigurationSite)parent;
			return getSiteFeatures(csite.getSite());
		}
		if (parent instanceof AvailableUpdates) {
			return ((AvailableUpdates)parent).getChildren(parent);
		}
		if (parent instanceof UpdateSearchSite) {
			return ((UpdateSearchSite)parent).getChildren(parent);
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
		if (child instanceof IConfigurationSite)
		   return getLocalSite();
		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object parent) {
		if (parent instanceof AvailableUpdates) {
			AvailableUpdates updates = (AvailableUpdates)parent;
			return updates.hasUpdates();
		}
		if (parent instanceof UpdateSearchSite) {
			UpdateSearchSite updateSearch = (UpdateSearchSite)parent;
			return updateSearch.getChildren(parent).length>0;
		}
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
		if (obj instanceof ILocalSite) {
			return UpdateUIPlugin.getResourceString(KEY_CURRENT);
		}
		if (obj instanceof IConfigurationSite) {
			IConfigurationSite csite = (IConfigurationSite)obj;
			ISite site = csite.getSite();
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
		if (obj  instanceof ILocalSite)
		   return eclipseImage;
		if (obj instanceof AvailableUpdates)
		   return updatesImage;
		if (obj instanceof IFeature)
		   return featureImage;
		if (obj instanceof IConfigurationSite) {
			IConfigurationSite csite = (IConfigurationSite)obj;
			if (csite.isInstallSite())
				return installSiteImage;
			else
				return linkedSiteImage;
		}
		if (obj instanceof UpdateSearchSite)
		   return siteImage;
		return null;
	}
}

public LocalSiteView() {
	eclipseImage = UpdateUIPluginImages.DESC_ECLIPSE_OBJ.createImage();
	updatesImage = UpdateUIPluginImages.DESC_UPDATES_OBJ.createImage();
	featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
	ImageDescriptor siteDesc = UpdateUIPluginImages.DESC_SITE_OBJ;
	siteImage = siteDesc.createImage();
	ImageDescriptor installSiteDesc = new OverlayIcon(siteDesc, new ImageDescriptor [][]
				{{ UpdateUIPluginImages.DESC_INSTALLABLE_CO }});
	installSiteImage = installSiteDesc.createImage();
	ImageDescriptor linkedSiteDesc = new OverlayIcon(siteDesc, new ImageDescriptor [][]
				{{ UpdateUIPluginImages.DESC_LINKED_CO }});
	linkedSiteImage = linkedSiteDesc.createImage();
	modelListener = new UpdateModelChangedListener();
}

public void selectUpdateObject() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	viewer.setSelection(new StructuredSelection(model.getUpdates()), true);
	
}

public void initProviders() {
	viewer.setContentProvider(new LocalSiteProvider());
	viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
	viewer.setLabelProvider(new LocalSiteLabelProvider());
}

protected void partControlCreated() {
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.addUpdateModelChangedListener(modelListener);
}

private ILocalSite getLocalSite() {
	try {
		return SiteManager.getLocalSite();
	}
	catch (CoreException e) {
		UpdateUIPlugin.logException(e);
		return null;
	}
}

private Object [] openLocalSite() {
	try {
		ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		Object [] result = config.getConfigurationSites();
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

public void dispose() {
	eclipseImage.dispose();
	featureImage.dispose();
	siteImage.dispose();
	installSiteImage.dispose();
	linkedSiteImage.dispose();
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
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	model.removeUpdateModelChangedListener(modelListener);
	super.dispose();
}

private void registerListeners() {
	try {
	   	ILocalSite localSite = SiteManager.getLocalSite();
		IInstallConfiguration config = localSite.getCurrentConfiguration();
		config.addInstallConfigurationChangedListener(this);
		IConfigurationSite [] isites = config.getConfigurationSites();
		for (int i=0; i<isites.length; i++) {
			ISite site = isites[i].getSite();
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
		IConfigurationSite [] isites = config.getConfigurationSites();
		for (int i=0; i<isites.length; i++) {
			ISite site = isites[i].getSite();
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
	public void installSiteAdded(IConfigurationSite csite) {
		viewer.add(getLocalSite(), csite);
	}

	/**
	 * @see IInstallConfigurationChangedListener#installSiteRemoved(ISite)
	 */
	public void installSiteRemoved(IConfigurationSite site) {
		viewer.remove(site);
	}

	/**
	 * @see IInstallConfigurationChangedListener#linkedSiteAdded(ISite)
	 */
	public void linkedSiteAdded(IConfigurationSite site) {
	}

	/**
	 * @see IInstallConfigurationChangedListener#linkedSiteRemoved(ISite)
	 */
	public void linkedSiteRemoved(IConfigurationSite site) {
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

	/*
	 * @see IInstallConfigurationChangedListener#featureAdded(IConfigurationSite, IFeature)
	 */
	public void featureAdded(IConfigurationSite site, IFeature feature) {
		// should be removed as soon as deprecated
	}

	/*
	 * @see IInstallConfigurationChangedListener#featureRemoved(IConfigurationSite, IFeature)
	 */
	public void featureRemoved(IConfigurationSite site, IFeature feature) {
		// should be removed as soon as deprecated in the interface
	}

}
