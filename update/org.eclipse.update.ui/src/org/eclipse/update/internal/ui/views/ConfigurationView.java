package org.eclipse.update.internal.ui.views;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.forms.RevertSection;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class ConfigurationView
	extends BaseTreeView
	implements
		IInstallConfigurationChangedListener,
		IConfiguredSiteChangedListener,
		ILocalSiteChangedListener {
	private static final String KEY_CURRENT = "ConfigurationView.current";
	private static final String KEY_SHOW_UNCONF_FEATURES =
		"ConfigurationView.showUnconfFeatures";
	private static final String KEY_SHOW_UNCONF_FEATURES_TOOLTIP =
		"ConfigurationView.showUnconfFeatures.tooltip";
	private Image eclipseImage;
	private Image featureImage;
	private Image unconfFeatureImage;
	private Image siteImage;
	private Image installSiteImage;
	private Image linkedSiteImage;
	private Image configImage;
	private Image currentConfigImage;
	private Image historyImage;
	private Image savedImage;
	private boolean initialized;
	private SavedFolder savedFolder;
	private HistoryFolder historyFolder;
	private Action showUnconfFeaturesAction;
	private Action revertAction;
	private Action preserveAction;
	private Action removePreservedAction;
	private Action propertiesAction;
	private IUpdateModelChangedListener modelListener;
	private DrillDownAdapter drillDownAdapter;
	private static final String KEY_RESTORE = "ConfigurationView.Popup.restore";
	private static final String KEY_PRESERVE =
		"ConfigurationView.Popup.preserve";
	private static final String KEY_REMOVE_PRESERVED =
		"ConfigurationView.Popup.removePreserved";
	private static final String KEY_HISTORY_FOLDER =
		"ConfigurationView.historyFolder";
	private static final String KEY_SAVED_FOLDER =
		"ConfigurationView.savedFolder";

	abstract class ViewFolder extends UIModelObject {
		private String label;
		private Image image;

		public ViewFolder(String label) {
			this.label = label;
			String imageKey = ISharedImages.IMG_OBJ_FOLDER;
			image =
				PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}

		public Object getAdapter(Class key) {
			return null;
		}

		public Image getImage() {
			return image;
		}

		public String toString() {
			return label;
		}
		public abstract Object[] getChildren();
	}

	class SavedFolder extends ViewFolder {
		public SavedFolder() {
			super(UpdateUIPlugin.getResourceString(KEY_SAVED_FOLDER));
		}
		public Object[] getChildren() {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				return makeChildren(localSite.getPreservedConfigurations());
			} catch (CoreException e) {
				return new Object[0];
			}
		}

		private Object[] makeChildren(IInstallConfiguration[] preserved) {
			Object[] children = new Object[preserved.length];
			for (int i = 0; i < preserved.length; i++) {
				children[i] = new PreservedConfiguration(preserved[i]);
			}
			return children;
		}
	}

	class HistoryFolder extends ViewFolder {
		public HistoryFolder() {
			super(UpdateUIPlugin.getResourceString(KEY_HISTORY_FOLDER));
		}
		public Object[] getChildren() {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				return localSite.getConfigurationHistory();
			} catch (CoreException e) {
				return new Object[0];
			}
		}
	}

	class UpdateModelChangedListener implements IUpdateModelChangedListener {
		/**
		 * @see IUpdateModelChangedListener#objectAdded(Object, Object)
		 */
		public void objectsAdded(Object parent, Object[] children) {
		}

		/**
		 * @see IUpdateModelChangedListener#objectRemoved(Object, Object)
		 */
		public void objectsRemoved(Object parent, Object[] children) {
		}

		/**
		 * @see IUpdateModelChangedListener#objectChanged(Object, String)
		 */
		public void objectChanged(Object object, String property) {
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
					return new Object[] {
						getLocalSite(),
						historyFolder,
						savedFolder };
				else
					return new Object[0];
			}
			if (parent instanceof ILocalSite) {
				return openLocalSite();
			}
			if (parent instanceof ViewFolder) {
				return ((ViewFolder) parent).getChildren();
			}
			if (parent instanceof PreservedConfiguration) {
				// resolve the adapter
				parent = ((PreservedConfiguration) parent).getConfiguration();
			}
			if (parent instanceof IInstallConfiguration) {
				return getConfigurationSites((IInstallConfiguration) parent);
			}
			if (parent instanceof IConfiguredSiteAdapter) {
				IConfiguredSiteAdapter adapter =
					(IConfiguredSiteAdapter) parent;
				boolean showUnconf = showUnconfFeaturesAction.isChecked();
				if (showUnconf)
					return getAllFeatures(adapter);
				else
					return getConfiguredFeatures(adapter);
			}
			if (parent instanceof ConfiguredFeatureAdapter) {
				return ((ConfiguredFeatureAdapter)parent).getIncludedFeatures();
			}
			return new Object[0];
		}

		private Object[] getConfigurationSites(IInstallConfiguration config) {
			IConfiguredSite[] sites = config.getConfiguredSites();
			Object[] adapters = new Object[sites.length];
			for (int i = 0; i < sites.length; i++) {
				adapters[i] = new ConfigurationSiteAdapter(config, sites[i]);
			}
			return adapters;
		}

		private Object[] getConfiguredFeatures(IConfiguredSiteAdapter adapter) {
			try {
				IConfiguredSite csite = adapter.getConfigurationSite();
				IFeatureReference[] refs = csite.getConfiguredFeatures();
				ArrayList result = new ArrayList();
				for (int i = 0; i < refs.length; i++) {
					IFeature feature = refs[i].getFeature();
					result.add(
						new ConfiguredFeatureAdapter(adapter, feature, true));
				}
				return getRootFeatures(result);
			} catch (CoreException e) {
				UpdateUIPlugin.logException(e);
				return new Object[0];
			}
		}

		private Object[] getAllFeatures(IConfiguredSiteAdapter adapter) {
			IConfiguredSite csite = adapter.getConfigurationSite();
			ISite site = csite.getSite();
			IFeatureReference[] allRefs = site.getFeatureReferences();
			ArrayList result = new ArrayList();

			for (int i = 0; i < allRefs.length; i++) {
				IFeature feature;
				try {
					feature = allRefs[i].getFeature();
				} catch (CoreException e) {
					feature = new MissingFeature(site, allRefs[i].getURL());
				}
				result.add(
					new ConfiguredFeatureAdapter(
						adapter,
						feature,
						csite.isConfigured(feature)));
			}
			return getRootFeatures(result);
		}

		private Object[] getRootFeatures(ArrayList list) {
			ArrayList children = new ArrayList();
			ArrayList result = new ArrayList();
			try {
				for (int i = 0; i < list.size(); i++) {
					ConfiguredFeatureAdapter cf =
						(ConfiguredFeatureAdapter) list.get(i);
					IFeature feature = cf.getFeature();
					if (feature!=null)
						addChildFeatures(feature, children);
				}
				for (int i = 0; i < list.size(); i++) {
					ConfiguredFeatureAdapter cf =
						(ConfiguredFeatureAdapter) list.get(i);
					IFeature feature = cf.getFeature();
					if (feature!=null && isChildFeature(feature, children) == false)
						result.add(cf);
				}
			} catch (CoreException e) {
				return list.toArray();
			}
			return result.toArray();
		}

		private void addChildFeatures(IFeature feature, ArrayList children) {
			try {
				IFeatureReference[] included =
					feature.getIncludedFeatureReferences();
				for (int i = 0; i < included.length; i++) {
					IFeature childFeature = included[i].getFeature();
					children.add(childFeature);
				}
			} catch (CoreException e) {
				// FIXME at least log
			}
		}

		private boolean isChildFeature(IFeature feature, ArrayList children) {
			for (int i = 0; i < children.size(); i++) {
				IFeature child = (IFeature) children.get(i);
				if (feature
					.getVersionedIdentifier()
					.equals(child.getVersionedIdentifier()))
					return true;
			}
			return false;
		} /**
													 * @see ITreeContentProvider#getParent(Object)
													 */
		public Object getParent(Object child) {
			return null;
		} /**
													 * @see ITreeContentProvider#hasChildren(Object)
													 */
		public boolean hasChildren(Object parent) {
			if (parent instanceof ConfiguredFeatureAdapter) {
				return ((ConfiguredFeatureAdapter)parent).hasIncludedFeatures();
			}
			return true;
		} /**
													 * @see IStructuredContentProvider#getElements(Object)
													 */
		public Object[] getElements(Object input) {
			return getChildren(input);
		}
	}

	class LocalSiteLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof ILocalSite) {
				AboutInfo info = UpdateUIPlugin.getDefault().getAboutInfo();
				String productName = info.getProductName();
				if (productName!=null) return productName;
				return UpdateUIPlugin.getResourceString(KEY_CURRENT);
			}
			if (obj instanceof IInstallConfiguration) {
				IInstallConfiguration config = (IInstallConfiguration) obj;
				return config.getLabel();
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
					String version =
						feature
							.getVersionedIdentifier()
							.getVersion()
							.toString();
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
			if (obj instanceof IFeatureAdapter) {
				return getFeatureImage((IFeatureAdapter) obj);
			}
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfigurationSite();
				if (csite.verifyUpdatableStatus().isOK())
					return installSiteImage;
				else
					return linkedSiteImage;
			}
			if (obj instanceof SavedFolder) {
				return savedImage;
			}
			if (obj instanceof HistoryFolder) {
				return historyImage;
			}
			/*			
			if (obj instanceof ViewFolder) {
				return ((ViewFolder) obj).getImage();
			}
			*/
			if (obj instanceof PreservedConfiguration) {
				obj = ((PreservedConfiguration) obj).getConfiguration();
			}
			if (obj instanceof IInstallConfiguration) {
				IInstallConfiguration config = (IInstallConfiguration) obj;
				if (config.isCurrent())
					return currentConfigImage;
				return configImage;
			}
			return null;
		}

		private Image getFeatureImage(IFeatureAdapter adapter) {
			boolean configured = true;
			if (adapter instanceof IConfiguredFeatureAdapter) {
				configured =
					((IConfiguredFeatureAdapter) adapter).isConfigured();
			}
			return (configured ? featureImage : unconfFeatureImage);
		}
	}

	public ConfigurationView() {
		ImageDescriptor edesc = UpdateUIPluginImages.DESC_APP_OBJ;
		AboutInfo info = UpdateUIPlugin.getDefault().getAboutInfo();
		if (info.getWindowImage()!=null)
			edesc = info.getWindowImage();
		eclipseImage = edesc.createImage();
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
		unconfFeatureImage =
			UpdateUIPluginImages.DESC_UNCONF_FEATURE_OBJ.createImage();
		ImageDescriptor siteDesc = UpdateUIPluginImages.DESC_LSITE_OBJ;
		siteImage = siteDesc.createImage();
		ImageDescriptor installSiteDesc = UpdateUIPluginImages.DESC_LSITE_OBJ;
		/*
			new OverlayIcon(
				siteDesc,
				new ImageDescriptor[][] { {
					UpdateUIPluginImages
					.DESC_INSTALLABLE_CO }
		});
		*/
		installSiteImage = installSiteDesc.createImage();
		ImageDescriptor linkedSiteDesc =
			new OverlayIcon(
				siteDesc,
				new ImageDescriptor[][] { {
					UpdateUIPluginImages
					.DESC_LINKED_CO }
		});
		linkedSiteImage = linkedSiteDesc.createImage();
		configImage = UpdateUIPluginImages.DESC_CONFIG_OBJ.createImage();
		ImageDescriptor cdesc =
			new OverlayIcon(
				UpdateUIPluginImages.DESC_CONFIG_OBJ,
				new ImageDescriptor[][] { {
			}, {
				UpdateUIPluginImages.DESC_CURRENT_CO }
		});
		currentConfigImage = cdesc.createImage();
		savedImage = UpdateUIPluginImages.DESC_SAVED_OBJ.createImage();
		historyImage= UpdateUIPluginImages.DESC_HISTORY_OBJ.createImage();
		savedFolder = new SavedFolder();
		historyFolder = new HistoryFolder();
	}

	public void initProviders() {
		viewer.setContentProvider(new LocalSiteProvider());
		viewer.setInput(UpdateUIPlugin.getDefault().getUpdateModel());
		viewer.setLabelProvider(new LocalSiteLabelProvider());
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			localSite.addLocalSiteChangedListener(this);
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		modelListener = new IUpdateModelChangedListener() {
			public void objectsAdded(Object parent, Object[] children) {
			}
			public void objectsRemoved(Object parent, Object[] children) {
			}
			public void objectChanged(Object obj, String property) {
				viewer.update(obj, null);
			}
		};
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
		savedImage.dispose();
		historyImage.dispose();
		configImage.dispose();
		currentConfigImage.dispose();
		if (initialized) {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				localSite.removeLocalSiteChangedListener(this);
				IInstallConfiguration config =
					localSite.getCurrentConfiguration();
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
	private Object getSelectedObject() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection
			&& !selection.isEmpty()) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() == 1) {
				return ssel.getFirstElement();
			}
		}
		return null;
	}

	public void selectHistoryFolder() {
		viewer.setExpandedState(historyFolder, true);
		viewer.setSelection(new StructuredSelection(historyFolder), true);
	}
	public void selectCurrentConfiguration() {
		viewer.setSelection(new StructuredSelection(getLocalSite()), true);
	}

	private IInstallConfiguration getSelectedConfiguration(
		Object obj,
		boolean onlyPreserved) {
		if (!onlyPreserved && obj instanceof IInstallConfiguration)
			return (IInstallConfiguration) obj;
		if (obj instanceof PreservedConfiguration)
			return ((PreservedConfiguration) obj).getConfiguration();
		return null;
	}

	private boolean isPreserved(IInstallConfiguration config) {
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			IInstallConfiguration[] preservedConfigs =
				localSite.getPreservedConfigurations();
			for (int i = 0; i < preservedConfigs.length; i++) {
				if (preservedConfigs[i].equals(config))
					return true;
			}
			return false;
		} catch (CoreException e) {
			return false;
		}
	}

	protected void makeActions() {
		super.makeActions();
		showUnconfFeaturesAction = new Action() {
			public void run() {
				viewer.refresh(getLocalSite());
			}
		};
		showUnconfFeaturesAction.setText(
			UpdateUIPlugin.getResourceString(KEY_SHOW_UNCONF_FEATURES));
		showUnconfFeaturesAction.setImageDescriptor(
			UpdateUIPluginImages.DESC_UNCONF_FEATURE_OBJ);
		showUnconfFeaturesAction.setChecked(false);
		showUnconfFeaturesAction.setToolTipText(
			UpdateUIPlugin.getResourceString(KEY_SHOW_UNCONF_FEATURES_TOOLTIP));
		drillDownAdapter = new DrillDownAdapter(viewer);
		super.makeActions();
		revertAction = new Action() {
			public void run() {
				Object obj = getSelectedObject();
				IInstallConfiguration target =
					getSelectedConfiguration(obj, false);
				if (target != null)
					RevertSection.performRevert(target);
			}
		};
		revertAction.setText(UpdateUIPlugin.getResourceString(KEY_RESTORE));
		preserveAction = new Action() {
			public void run() {
				Object obj = getSelectedObject();
				IInstallConfiguration target =
					getSelectedConfiguration(obj, false);
				if (target == null)
					return;
				try {
					ILocalSite localSite = SiteManager.getLocalSite();
					localSite.addToPreservedConfigurations(target);
					localSite.save();
					viewer.refresh(savedFolder);
				} catch (CoreException e) {
					UpdateUIPlugin.logException(e);
				}
			}
		};
		preserveAction.setText(UpdateUIPlugin.getResourceString(KEY_PRESERVE));
		removePreservedAction = new Action() {
			public void run() {
				Object obj = getSelectedObject();
				IInstallConfiguration target =
					getSelectedConfiguration(obj, true);
				if (target == null)
					return;
				if (isPreserved(target) == false)
					return;
				try {
					ILocalSite localSite = SiteManager.getLocalSite();
					localSite.removeFromPreservedConfigurations(target);
					localSite.save();
					viewer.remove(obj);
				} catch (CoreException e) {
					UpdateUIPlugin.logException(e);
				}
			}
		};
		removePreservedAction.setText(
			UpdateUIPlugin.getResourceString(KEY_REMOVE_PRESERVED));
		propertiesAction =
			new PropertyDialogAction(
				UpdateUIPlugin.getActiveWorkbenchShell(),
				viewer);
	}

	protected void fillActionBars(IActionBars bars) {
		IToolBarManager tbm = bars.getToolBarManager();
		drillDownAdapter.addNavigationActions(tbm);
		tbm.add(new Separator());
		tbm.add(showUnconfFeaturesAction);
	}
	protected void fillContextMenu(IMenuManager manager) {
		Object obj = getSelectedObject();
		IInstallConfiguration config = getSelectedConfiguration(obj, false);
		if (config != null && !config.isCurrent()) {
			manager.add(revertAction);
			manager.add(new Separator());
		}
		if (config != null && !isPreserved(config)) {
			manager.add(preserveAction);
		}
		config = getSelectedConfiguration(obj, true);
		if (config != null) {
			manager.add(removePreservedAction);
		}
		super.fillContextMenu(manager);
		if (obj instanceof PreservedConfiguration
			|| obj instanceof IInstallConfiguration)
			manager.add(propertiesAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		super.fillContextMenu(manager);
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
	} /**
						 * @see IInstallConfigurationChangedListener#installSiteAdded(ISite)
						 */
	public void installSiteAdded(IConfiguredSite csite) {
		asyncRefresh();
	} /**
						 * @see IInstallConfigurationChangedListener#installSiteRemoved(ISite)
						 */
	public void installSiteRemoved(IConfiguredSite site) {
		asyncRefresh();
	} /**
						 * @see IConfiguredSiteChangedListener#featureInstalled(IFeature)
						 */
	public void featureInstalled(IFeature feature) {
		asyncRefresh();
	} /**
						 * @see IConfiguredSiteChangedListener#featureUninstalled(IFeature)
						 */
	public void featureRemoved(IFeature feature) {
		asyncRefresh();
	} /**
						 * @see IConfiguredSiteChangedListener#featureUConfigured(IFeature)
						 */
	public void featureConfigured(IFeature feature) {
	};
	/**
	 * @see IConfiguredSiteChangedListener#featureUConfigured(IFeature)
	 */
	public void featureUnconfigured(IFeature feature) {
	};
	public void currentInstallConfigurationChanged(IInstallConfiguration configuration) {
		asyncRefresh();
	}

	public void installConfigurationRemoved(IInstallConfiguration configuration) {
		asyncRefresh();
	}
	
	private void asyncRefresh() {
		Control control = viewer.getControl();
		if (control.isDisposed()) return;
		control.getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
			}
		});
	}
			

}