package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.update.configuration.*;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.DefaultContentProvider;
import org.eclipse.update.internal.ui.parts.OverlayIcon;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class HistoryView
	extends BaseTreeView
	implements ILocalSiteChangedListener {
	private Image configImage;
	private Image featureImage;
	private Image unconfFeatureImage;
	private Image errorFeatureImage;
	private Image siteImage;
	private Image currentConfigImage;
	private Action revertAction;
	private Action preserveAction;
	private Action removePreservedAction;
	private Action propertiesAction;
	private IUpdateModelChangedListener modelListener;
	private SavedFolder savedFolder;
	private static final String KEY_RESTORE = "HistoryView.Popup.restore";
	private static final String KEY_PRESERVE = "HistoryView.Popup.preserve";
	private static final String KEY_REMOVE_PRESERVED =
		"HistoryView.Popup.removePreserved";
	private static final String KEY_SAVED_FOLDER = "HistoryView.savedFolder";

	class SavedFolder {
		private String label;
		private Image image;

		public SavedFolder() {
			label = UpdateUIPlugin.getResourceString(KEY_SAVED_FOLDER);
			String imageKey = ISharedImages.IMG_OBJ_FOLDER;
			image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}

		public Image getImage() {
			return image;
		}

		public String toString() {
			return label;
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

	class HistoryProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		/**
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof SavedFolder) {
				return ((SavedFolder) parent).getChildren();
			}
			if (parent instanceof PreservedConfiguration) {
				// resolve the adapter
				parent = ((PreservedConfiguration) parent).getConfiguration();
			}
			if (parent instanceof IInstallConfiguration) {
				return getConfigurationSites((IInstallConfiguration) parent);
			}
			if (parent instanceof IConfiguredSiteAdapter) {
				IConfiguredSiteAdapter csite = (IConfiguredSiteAdapter) parent;
				return getConfiguredFeatures(csite);
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
			IConfiguredSite csite = adapter.getConfigurationSite();
			IFeatureReference[] crefs = csite.getConfiguredFeatures();
			ISite site = csite.getSite();
			Object[] result = new Object[crefs.length];

			for (int i = 0; i < crefs.length; i++) {
				IFeature feature = null;
				try {
					feature = crefs[i].getFeature();
				} catch (CoreException e) {
				} finally {
					if (feature == null)
						feature = new MissingFeature(site, crefs[i].getURL());
				}
				result[i] = new ConfiguredFeatureAdapter(adapter, feature, true);
			}
			return result;
		}

		/**
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object child) {
			return null;
		}

		/**
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object input) {
			if (input instanceof ILocalSite) {
				Object[] history = ((ILocalSite) input).getConfigurationHistory();
				Object[] result = new Object[1 + history.length];
				result[0] = savedFolder;
				for (int i = 1; i <= history.length; i++) {
					result[i] = history[i - 1];
				}
				return result;
			}
			return new Object[0];
		}
	}

	class HistoryLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof IInstallConfiguration) {
				IInstallConfiguration config = (IInstallConfiguration) obj;
				return config.getLabel();
			}
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSiteAdapter ad = (IConfiguredSiteAdapter) obj;
				IConfiguredSite csite = ad.getConfigurationSite();
				ISite site = csite.getSite();
				return site.getURL().toString();
			}
			if (obj instanceof IFeatureAdapter) {
				try {
					IFeature feature = ((IFeatureAdapter) obj).getFeature();
					String version = feature.getVersionedIdentifier().getVersion().toString();
					return feature.getLabel() + " " + version;
				} catch (CoreException e) {
					return "error";
				}
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			if (obj instanceof SavedFolder)
				return ((SavedFolder) obj).getImage();
			if (obj instanceof IFeatureAdapter)
				return getFeatureImage((IFeatureAdapter) obj);
			if (obj instanceof IConfiguredSiteAdapter)
				return siteImage;
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
			try {
				if (adapter.getFeature() instanceof MissingFeature)
					return errorFeatureImage;
			} catch (CoreException e) {
			}
			boolean configured = true;
			if (adapter instanceof IConfiguredFeatureAdapter) {
				configured = ((IConfiguredFeatureAdapter) adapter).isConfigured();
			}
			return (configured ? featureImage : unconfFeatureImage);
		}
	}

	public HistoryView() {
		featureImage = UpdateUIPluginImages.DESC_FEATURE_OBJ.createImage();
		unconfFeatureImage = UpdateUIPluginImages.DESC_UNCONF_FEATURE_OBJ.createImage();
		siteImage = UpdateUIPluginImages.DESC_SITE_OBJ.createImage();
		configImage = UpdateUIPluginImages.DESC_CONFIG_OBJ.createImage();
		ImageDescriptor cdesc =
			new OverlayIcon(
				UpdateUIPluginImages.DESC_CONFIG_OBJ,
				new ImageDescriptor[][] { {
			}, {
				UpdateUIPluginImages.DESC_CURRENT_CO }
		});
		currentConfigImage = cdesc.createImage();
		cdesc =
			new OverlayIcon(
				UpdateUIPluginImages.DESC_FEATURE_OBJ,
				new ImageDescriptor[][] { {
			}, {
			}, {
				UpdateUIPluginImages.DESC_ERROR_CO }
		});
		errorFeatureImage = cdesc.createImage();
		savedFolder = new SavedFolder();
	}

	public void initProviders() {
		viewer.setContentProvider(new HistoryProvider());
		viewer.setLabelProvider(new HistoryLabelProvider());
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			viewer.setInput(localSite);
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

	protected void partControlCreated() {
	}

	public void dispose() {
		featureImage.dispose();
		unconfFeatureImage.dispose();
		errorFeatureImage.dispose();
		siteImage.dispose();
		configImage.dispose();
		currentConfigImage.dispose();

		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			localSite.removeLocalSiteChangedListener(this);
		} catch (CoreException e) {
			UpdateUIPlugin.logException(e);
		}
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(modelListener);
		super.dispose();
	}

	private Object getSelectedObject() {
		ISelection selection = viewer.getSelection();

		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() == 1) {
				return ssel.getFirstElement();
			}
		}
		return null;
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

	protected void makeActions() {
		super.makeActions();
		revertAction = new Action() {
			public void run() {
				Object obj = getSelectedObject();
				IInstallConfiguration target = getSelectedConfiguration(obj, false);
				if (target != null)
					RevertSection.performRevert(target);
			}
		};
		revertAction.setText(UpdateUIPlugin.getResourceString(KEY_RESTORE));
		preserveAction = new Action() {
			public void run() {
				Object obj = getSelectedObject();
				IInstallConfiguration target = getSelectedConfiguration(obj, false);
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
				IInstallConfiguration target = getSelectedConfiguration(obj, true);
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
			new PropertyDialogAction(UpdateUIPlugin.getActiveWorkbenchShell(), viewer);
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
	}

	public void currentInstallConfigurationChanged(IInstallConfiguration configuration) {
		viewer.refresh();
	}

	public void installConfigurationRemoved(IInstallConfiguration configuration) {
		viewer.refresh();
	}

}