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
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.core.*;
import org.eclipse.update.core.ICategory;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.IURLEntry;
import org.eclipse.update.core.IPluginEntry;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.VersionedIdentifier;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.IConfiguredSiteChangedListener;
import org.eclipse.update.configuration.IInstallConfiguration;

import java.util.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.update.internal.ui.model.UpdateModel;
import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class LocalSiteView
	extends BaseTreeView
	implements IInstallConfigurationChangedListener, IConfiguredSiteChangedListener {
	private static final String KEY_CURRENT = "LocalSiteView.current";
	private static final String KEY_SHOW_UNCONF_FEATURES = "LocalSiteView.showUnconfFeatures";
	private static final String KEY_SHOW_UNCONF_FEATURES_TOOLTIP = "LocalSiteView.showUnconfFeatures.tooltip";
	private Image eclipseImage;
	private Image updatesImage;
	private Image featureImage;
	private Image unconfFeatureImage;
	private Image siteImage;
	private Image installSiteImage;
	private Image linkedSiteImage;
	private boolean initialized;
	private Action showUnconfFeaturesAction;
	private UpdateModelChangedListener modelListener;

	class UpdateModelChangedListener implements IUpdateModelChangedListener {
		/**
		 * @see IUpdateModelChangedListener#objectAdded(Object, Object)
		 */
		public void objectsAdded(Object parent, Object[] children) {
			if (parent instanceof AvailableUpdates) {
				viewer.add(parent, children);
			}
		}

		/**
		 * @see IUpdateModelChangedListener#objectRemoved(Object, Object)
		 */
		public void objectsRemoved(Object parent, Object [] children) {
			if (parent instanceof AvailableUpdates) {
				viewer.remove(children);
			}
		}

		/**
		 * @see IUpdateModelChangedListener#objectChanged(Object, String)
		 */
		public void objectChanged(Object object, String property) {
			if (object instanceof AvailableUpdates
				&& property.equals(AvailableUpdates.P_REFRESH)) {
				viewer.refresh(object);
				viewer.expandToLevel(object, 999);
			}
		}

	}

	class LocalSiteProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		/**
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof UpdateModel) {
				UpdateModel model = (UpdateModel) parent;
				ILocalSite localSite = getLocalSite();
				if (localSite != null)
					return new Object[] { getLocalSite(), model.getUpdates()};
				else
					return new Object[0];
			}
			if (parent instanceof ILocalSite) {
				return openLocalSite();
			}
			if (parent instanceof IConfiguredSiteAdapter) {
				IConfiguredSiteAdapter adapter = (IConfiguredSiteAdapter) parent;
				boolean showUnconf = showUnconfFeaturesAction.isChecked();
				if (showUnconf)
					return getAllFeatures(adapter);
				else 
					return getConfiguredFeatures(adapter);
			}
			if (parent instanceof AvailableUpdates) {
				return ((AvailableUpdates) parent).getChildren(parent);
			}
			if (parent instanceof UpdateSearchSite) {
				return ((UpdateSearchSite) parent).getChildren(parent);
			}
			return new Object[0];
		}

		private Object[] getConfiguredFeatures(IConfiguredSiteAdapter adapter) {
			try {
				IConfiguredSite csite = adapter.getConfigurationSite();
				IFeatureReference[] refs = csite.getConfiguredFeatures();
				Object[] result = new Object[refs.length];
				for (int i = 0; i < refs.length; i++) {
					IFeature feature = refs[i].getFeature();
					result[i] = new ConfiguredFeatureAdapter(adapter, feature, true);
				}
				return result;
			} catch (CoreException e) {
				UpdateUIPlugin.logException(e);
				return new Object[0];
			}
		}
		
		private Object[] getAllFeatures(IConfiguredSiteAdapter adapter) {
			IConfiguredSite csite = adapter.getConfigurationSite();
			IFeatureReference[] crefs = csite.getConfiguredFeatures();
			ISite site = csite.getSite();
			IFeatureReference[] allRefs = site.getFeatureReferences();
			Object[] result = new Object[allRefs.length];
			
			for (int i = 0; i < allRefs.length; i++) {
				IFeature feature;
				try {
					feature = allRefs[i].getFeature();
				} catch (CoreException e) {
					feature = new MissingFeature(site, allRefs[i].getURL());
				}
				boolean configured = isOnTheList(allRefs[i], crefs);
				result [i] = new ConfiguredFeatureAdapter(adapter, feature, configured);
			}
			return result;
		}
		
		private boolean isOnTheList(IFeatureReference ref, IFeatureReference [] refs) {
			for (int i=0; i<refs.length; i++) {
				IFeatureReference confRef = refs[i];
				if (confRef.getURL().equals(ref.getURL())) return true;
			}
			return false;
		}

		/**
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object child) {
			/*
			if (child instanceof IFeature)
			   return ((IFeature)child).getSite();
			if (child instanceof IConfiguredSite)
			   return getLocalSite();
			 */
			return null;
		}

		/**
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object parent) {
			if (parent instanceof AvailableUpdates) {
				AvailableUpdates updates = (AvailableUpdates) parent;
				return updates.hasUpdates();
			}
			if (parent instanceof UpdateSearchSite) {
				UpdateSearchSite updateSearch = (UpdateSearchSite) parent;
				return updateSearch.getChildren(parent).length > 0;
			}
			return !(parent instanceof IFeatureAdapter);
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
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfigurationSite();
				ISite site = csite.getSite();
				return site.getURL().toString();
			}
			if (obj instanceof IFeatureAdapter) {
				try {
					IFeature feature = ((IFeatureAdapter) obj).getFeature();
					String version = feature.getVersionedIdentifier().getVersion().toString();
					return feature.getLabel() + " " + version;
				} catch (CoreException e) {
					return "Error";
				}
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			if (obj instanceof ILocalSite)
				return eclipseImage;
			if (obj instanceof AvailableUpdates)
				return updatesImage;
			if (obj instanceof IFeatureAdapter) {
				return getFeatureImage((IFeatureAdapter)obj);
			}
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfigurationSite();
				if (csite.isUpdateable())
					return installSiteImage;
				else
					return linkedSiteImage;
			}
			if (obj instanceof UpdateSearchSite)
				return siteImage;
			return null;
		}

		private Image getFeatureImage(IFeatureAdapter adapter) {
			boolean configured = true;
			if (adapter instanceof IConfiguredFeatureAdapter) {
				configured = ((IConfiguredFeatureAdapter)adapter).isConfigured();
			}
			return (configured ? featureImage : unconfFeatureImage);
		}
	}

	public LocalSiteView() {
		eclipseImage = UpdateUIPluginImages.DESC_ECLIPSE_OBJ.createImage();
		updatesImage = UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_UPDATES_OBJ);
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
		unconfFeatureImage = UpdateUIPluginImages.DESC_UNCONF_FEATURE_OBJ.createImage();
		ImageDescriptor siteDesc = UpdateUIPluginImages.DESC_SITE_OBJ;
		siteImage = siteDesc.createImage();
		ImageDescriptor installSiteDesc =
			new OverlayIcon(
				siteDesc,
				new ImageDescriptor[][] { { UpdateUIPluginImages.DESC_INSTALLABLE_CO }
		});
		installSiteImage = installSiteDesc.createImage();
		ImageDescriptor linkedSiteDesc =
			new OverlayIcon(
				siteDesc,
				new ImageDescriptor[][] { { UpdateUIPluginImages.DESC_LINKED_CO }
		});
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
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
			return null;
		}
	}

	private Object[] openLocalSite() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			IConfiguredSite[] sites = config.getConfiguredSites();
			Object[] result = new Object[sites.length];
			for (int i = 0; i < sites.length; i++) {
				result[i] = new ConfigurationSiteAdapter(config, sites[i]);
			}
			if (!initialized) {
				config.addInstallConfigurationChangedListener(this);
				initialized = true;
			}
			return result;
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
			return new Object[0];
		}
	}

	public void dispose() {
		eclipseImage.dispose();
		featureImage.dispose();
		unconfFeatureImage.dispose();
		siteImage.dispose();
		installSiteImage.dispose();
		linkedSiteImage.dispose();

		if (initialized) {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				IInstallConfiguration config = localSite.getCurrentConfiguration();
				config.removeInstallConfigurationChangedListener(this);
			} catch (CoreException e) {
				UpdateUIPlugin.logException(e);
			}
			initialized = false;
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(modelListener);
		super.dispose();
	}
	
	protected void makeActions() {
		super.makeActions();
		showUnconfFeaturesAction = new Action() {
			public void run() {
				viewer.refresh(getLocalSite());
			}
		};	
		showUnconfFeaturesAction.setText(UpdateUIPlugin.getResourceString(KEY_SHOW_UNCONF_FEATURES));
		showUnconfFeaturesAction.setImageDescriptor(UpdateUIPluginImages.DESC_UNCONF_FEATURE_OBJ);
		showUnconfFeaturesAction.setChecked(false);
		showUnconfFeaturesAction.setToolTipText(UpdateUIPlugin.getResourceString(KEY_SHOW_UNCONF_FEATURES_TOOLTIP));
	}
	
	protected void fillActionBars(IActionBars bars) {
		IToolBarManager tbm = bars.getToolBarManager();
		tbm.add(showUnconfFeaturesAction);
	}
	
	private void registerListeners() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			config.addInstallConfigurationChangedListener(this);
			IConfiguredSite[] isites = config.getConfiguredSites();
			for (int i = 0; i < isites.length; i++) {
				isites[i].addConfiguredSiteChangedListener(this);
			}
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
	}

	private void unregisterListeners() {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration config = localSite.getCurrentConfiguration();
			config.removeInstallConfigurationChangedListener(this);
			IConfiguredSite[] isites = config.getConfiguredSites();
			for (int i = 0; i < isites.length; i++) {
				isites[i].removeConfiguredSiteChangedListener(this);
			}
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
	}

	/**
	 * @see IInstallConfigurationChangedListener#installSiteAdded(ISite)
	 */
	public void installSiteAdded(IConfiguredSite csite) {
		//viewer.add(getLocalSite(), csite);
		viewer.refresh(getLocalSite());
	}

	/**
	 * @see IInstallConfigurationChangedListener#installSiteRemoved(ISite)
	 */
	public void installSiteRemoved(IConfiguredSite site) {
		//viewer.remove(site);
		viewer.refresh(getLocalSite());
	}


	/**
	 * @see IConfiguredSiteChangedListener#featureInstalled(IFeature)
	 */
	public void featureInstalled(IFeature feature) {
		//viewer.add(feature.getSite(), feature);
		viewer.refresh();
	}

	/**
	 * @see IConfiguredSiteChangedListener#featureUninstalled(IFeature)
	 */
	public void featureUninstalled(IFeature feature) {
		//viewer.remove(feature);
		viewer.refresh();
	}
}