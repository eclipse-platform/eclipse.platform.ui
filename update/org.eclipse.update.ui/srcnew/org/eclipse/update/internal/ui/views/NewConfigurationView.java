/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class NewConfigurationView
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
	private static final String STATE_SHOW_UNCONF = "ConfigurationView.showUnconf";
	private static final String STATE_SHOW_SITES = "ConfigurationView.showSites";
	private static final String STATE_SHOW_NESTED_FEATURES = "ConfigurationView.showNestedFeatures";
	private static final String KEY_PRESERVE = "ConfigurationView.Popup.preserve";
	private static final String KEY_SHOW_STATUS = "ConfigurationView.Popup.showStatus";
	private static final String KEY_MISSING_FEATURE = "ConfigurationView.missingFeature";

	private Action showSitesAction;
	private Action showNestedFeaturesAction;
	private SwapVersionAction swapVersionAction;
	private FeatureStateAction featureStateAction;
	private UninstallFeatureAction uninstallFeatureAction;
	private Action showUnconfFeaturesAction;
	private RevertConfigurationAction revertAction;
	private SaveConfigurationAction preserveAction;
	private Action propertiesAction;
	private ShowFeatureStatusAction showStatusAction;
	private SiteStateAction siteStateAction;
	
	private IUpdateModelChangedListener modelListener;
	private boolean refreshLock = false;
	private Image eclipseImage;
	private boolean initialized;

	class ConfigurationSorter extends ViewerSorter {
		public int category(Object obj) {
			// sites
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfiguredSite();
				if (csite.isProductSite())
					return 1;
				if (csite.isExtensionSite())
					return 2;
				return 3;
			}
			return super.category(obj);
		}
	}

	class LocalSiteProvider
		extends DefaultContentProvider
		implements ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null)
				return;
			updateTitle(newInput);
		}
		/**
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parent) {
			if (parent instanceof UpdateModel) {
				ILocalSite localSite = getLocalSite();
				if (localSite != null)
					return new Object[] { localSite };
				else
					return new Object[0];
			}
			if (parent instanceof ILocalSite) {
				Object[] csites =  openLocalSite();
				if (showSitesAction.isChecked())
					return csites;
				ArrayList result = new ArrayList();
				boolean showUnconf = showUnconfFeaturesAction.isChecked();
				for (int i = 0; i < csites.length; i++) {
					IConfiguredSiteAdapter adapter = (IConfiguredSiteAdapter) csites[i];
					Object[] roots = getFeatures(adapter, !showUnconf);
					for (int j = 0; j < roots.length; j++) {
						result.add(roots[j]);
					}
				}
				return result.toArray();				
			}

			if (parent instanceof IConfiguredSiteAdapter) {
				IConfiguredSiteAdapter adapter = (IConfiguredSiteAdapter) parent;
				boolean showUnconf = showUnconfFeaturesAction.isChecked();
				return getFeatures(adapter, !showUnconf);
			}
			if (parent instanceof ConfiguredFeatureAdapter && showNestedFeaturesAction.isChecked()) {
				return ((ConfiguredFeatureAdapter) parent).getIncludedFeatures(null);
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			return null;
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ConfiguredFeatureAdapter) {
				return showNestedFeaturesAction.isChecked()
					&& ((ConfiguredFeatureAdapter) parent).hasIncludedFeatures(null);
			}
			if (parent instanceof ConfiguredSiteAdapter) {
				IConfiguredSite site =
					((ConfiguredSiteAdapter) parent).getConfiguredSite();
				if (showUnconfFeaturesAction.isChecked())
					return site.getFeatureReferences().length > 0;
				return site.getConfiguredFeatures().length > 0;
			}
			return true;
		}
		
		public Object[] getElements(Object input) {
			return getChildren(input);
		}
	}

	class LocalSiteLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof ILocalSite) {
				AboutInfo info = UpdateUI.getDefault().getAboutInfo();
				String productName = info.getProductName();
				if (productName != null)
					return productName;
				return UpdateUI.getString(KEY_CURRENT);
			}
			if (obj instanceof IInstallConfiguration) {
				IInstallConfiguration config = (IInstallConfiguration) obj;
				return config.getLabel();
			}
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfiguredSite();
				ISite site = csite.getSite();
				return site.getURL().toString();
			}
			if (obj instanceof IFeatureAdapter) {
				try {
					IFeature feature = ((IFeatureAdapter) obj).getFeature(null);
					if (feature instanceof MissingFeature) {
						return UpdateUI.getFormattedMessage(
							KEY_MISSING_FEATURE,
							feature.getLabel());
					}
					String version =
						feature.getVersionedIdentifier().getVersion().toString();
					return feature.getLabel() + " " + version;
				} catch (CoreException e) {
					return "Error";
				}
			}
			return super.getText(obj);
		}
		public Image getImage(Object obj) {
			UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
			if (obj instanceof ILocalSite)
				return eclipseImage;
			if (obj instanceof IFeatureAdapter) {
				return getFeatureImage(provider, (IFeatureAdapter) obj);
			}
			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfiguredSite();
				int flags = csite.isUpdatable() ? 0 : UpdateLabelProvider.F_LINKED;
				if (csite.isEnabled() == false)
					flags |= UpdateLabelProvider.F_UNCONFIGURED;
				ImageDescriptor desc = provider.getLocalSiteDescriptor(csite);
				return provider.get(desc, flags);
			}
			if (obj instanceof PreservedConfiguration) {
				obj = ((PreservedConfiguration) obj).getConfiguration();
			}
			if (obj instanceof IInstallConfiguration) {
				IInstallConfiguration config = (IInstallConfiguration) obj;
				int flags = config.isCurrent() ? SharedLabelProvider.F_CURRENT : 0;

				boolean currentTimeline = isCurrentTimeline(config);
				if (!currentTimeline)
					flags |= SharedLabelProvider.F_MOD;
				return provider.get(UpdateUIImages.DESC_CONFIG_OBJ, flags);
			}
			return null;
		}

		private boolean isCurrentTimeline(IInstallConfiguration config) {
			ILocalSite localSite = getLocalSite();
			if (localSite == null)
				return true;
			IInstallConfiguration cconfig = localSite.getCurrentConfiguration();
			return config.getTimeline() == cconfig.getTimeline();
		}

		private Image getFeatureImage(
			UpdateLabelProvider provider,
			IFeatureAdapter adapter) {
			boolean configured = true;
			boolean updated = false;
			boolean current = true;
			if (adapter instanceof IConfiguredFeatureAdapter) {
				IConfiguredFeatureAdapter cadapter = (IConfiguredFeatureAdapter) adapter;
				configured = cadapter.isConfigured();
				updated = cadapter.isUpdated();
				current = cadapter.getInstallConfiguration().isCurrent();
			}
			ILocalSite localSite = getLocalSite();
			try {
				IFeature feature = adapter.getFeature(null);
				if (feature instanceof MissingFeature) {
					MissingFeature mfeature = (MissingFeature) feature;
					if (mfeature.isOptional() == false)
						return provider.get(
							UpdateUIImages.DESC_FEATURE_OBJ,
							UpdateLabelProvider.F_ERROR);
					return provider.get(UpdateUIImages.DESC_NOTINST_FEATURE_OBJ);
				}
				int code = IFeature.STATUS_HAPPY;
				ImageDescriptor baseDesc;
				int flags = 0;
				if (current) {
					IStatus status = localSite.getFeatureStatus(feature);
					code = ShowFeatureStatusAction.getStatusCode(feature, status);
				}
				boolean efix = feature.isPatch();
				baseDesc =
					efix
						? UpdateUIImages.DESC_EFIX_OBJ
						: (configured
							? UpdateUIImages.DESC_FEATURE_OBJ
							: UpdateUIImages.DESC_UNCONF_FEATURE_OBJ);
				if (efix && !configured)
					flags |= UpdateLabelProvider.F_UNCONFIGURED;
				switch (code) {
					case IFeature.STATUS_UNHAPPY :
						flags |= UpdateLabelProvider.F_ERROR;
						break;
					case IFeature.STATUS_AMBIGUOUS :
						flags |= UpdateLabelProvider.F_WARNING;
						break;
					default :
						if (configured && updated)
							flags |= UpdateLabelProvider.F_UPDATED;
						break;
				}
				return provider.get(baseDesc, flags);
			} catch (CoreException e) {
				return provider.get(
					UpdateUIImages.DESC_FEATURE_OBJ,
					UpdateLabelProvider.F_ERROR);
			}
		}
	}

	public NewConfigurationView() {
		UpdateUI.getDefault().getLabelProvider().connect(this);
		initializeImages();
	}

	private void initializeImages() {
		ImageDescriptor edesc = UpdateUIImages.DESC_APP_OBJ;
		AboutInfo info = UpdateUI.getDefault().getAboutInfo();
		if (info.getWindowImage() != null)
			edesc = info.getWindowImage();
		eclipseImage = UpdateUI.getDefault().getLabelProvider().get(edesc);
	}

	public void initProviders() {
		final TreeViewer treeViewer = getTreeViewer();
		treeViewer.setContentProvider(new LocalSiteProvider());
		treeViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		treeViewer.setLabelProvider(new LocalSiteLabelProvider());
		treeViewer.setSorter(new ConfigurationSorter());
		treeViewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer v, Object parent, Object element) {
				if (element instanceof IConfiguredFeatureAdapter) {
					IConfiguredFeatureAdapter adapter =
						(IConfiguredFeatureAdapter) element;
					if (adapter.isConfigured())
						return true;
					boolean showUnconf = showUnconfFeaturesAction.isChecked();
					return showUnconf;
				}
				return true;
			}
		});
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			localSite.addLocalSiteChangedListener(this);
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		modelListener = new IUpdateModelChangedListener() {
			public void objectsAdded(Object parent, Object[] children) {
			}
			public void objectsRemoved(Object parent, Object[] children) {
			}
			public void objectChanged(final Object obj, String property) {
				if (refreshLock)
					return;
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						treeViewer.refresh();
					}
				});
			}
		};
		model.addUpdateModelChangedListener(modelListener);
		WorkbenchHelp.setHelp(getControl(), "org.eclipse.update.ui.ConfigurationView");
	}

	private ILocalSite getLocalSite() {
		try {
			return SiteManager.getLocalSite();
		} catch (CoreException e) {
			UpdateUI.logException(e);
			return null;
		}
	}

	private Object[] openLocalSite() {
		final Object[][] bag = new Object[1][];
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				try {
					ILocalSite localSite = SiteManager.getLocalSite();
					IInstallConfiguration config = localSite.getCurrentConfiguration();
					IConfiguredSite[] sites = config.getConfiguredSites();
					Object[] result = new Object[sites.length];
					for (int i = 0; i < sites.length; i++) {
						result[i] = new ConfiguredSiteAdapter(config, sites[i]);
					}
					if (!initialized) {
						config.addInstallConfigurationChangedListener(
							NewConfigurationView.this);
						initialized = true;
					}
					bag[0] = result;
				} catch (CoreException e) {
					UpdateUI.logException(e);
					bag[0] = new Object[0];
				}
			}
		});
		return bag[0];
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		if (initialized) {
			try {
				ILocalSite localSite = SiteManager.getLocalSite();
				localSite.removeLocalSiteChangedListener(this);
				IInstallConfiguration config = localSite.getCurrentConfiguration();
				config.removeInstallConfigurationChangedListener(this);
			} catch (CoreException e) {
				UpdateUI.logException(e);
			}
			initialized = false;
		}
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		model.removeUpdateModelChangedListener(modelListener);
		super.dispose();
	}

	protected void makeActions() {
		super.makeActions();

		initDrillDown();

		featureStateAction = new FeatureStateAction();

		siteStateAction = new SiteStateAction();

		preserveAction = new SaveConfigurationAction(UpdateUI.getString(KEY_PRESERVE));
		WorkbenchHelp.setHelp(
			preserveAction,
			"org.eclipse.update.ui.CofigurationView_preserveAction");

		showStatusAction =
			new ShowFeatureStatusAction(UpdateUI.getString(KEY_SHOW_STATUS));
		WorkbenchHelp.setHelp(
			showStatusAction,
			"org.eclipse.update.ui.CofigurationView_showStatusAction");

		revertAction = new RevertConfigurationAction("Revert...");
		WorkbenchHelp.setHelp(
			revertAction,
			"org.eclipse.update.ui.CofigurationView_revertAction");

		propertiesAction =
			new PropertyDialogAction(UpdateUI.getActiveWorkbenchShell(), getTreeViewer());
		WorkbenchHelp.setHelp(
			propertiesAction,
			"org.eclipse.update.ui.CofigurationView_propertiesAction");

		uninstallFeatureAction = new UninstallFeatureAction("Uninstall");
		
		swapVersionAction = new SwapVersionAction("another version...");

		makeShowUnconfiguredFeaturesAction();
		makeShowSitesAction();
		makeShowNestedFeaturesAction();

	}

	private void makeShowNestedFeaturesAction() {
		final Preferences pref = UpdateUI.getDefault().getPluginPreferences();
		pref.setDefault(STATE_SHOW_NESTED_FEATURES, true);
		showNestedFeaturesAction = new Action() {
			public void run() {
				getTreeViewer().refresh();
				pref.setValue(STATE_SHOW_NESTED_FEATURES, showNestedFeaturesAction.isChecked());
			}
		};
		showNestedFeaturesAction.setText("Show Nested Features");
		showNestedFeaturesAction.setImageDescriptor(UpdateUIImages.DESC_FEATURE_OBJ);
		showNestedFeaturesAction.setChecked(pref.getBoolean(STATE_SHOW_NESTED_FEATURES));
		showNestedFeaturesAction.setToolTipText("Show Nested Features");
	}

	private void makeShowSitesAction() {
		final Preferences pref = UpdateUI.getDefault().getPluginPreferences();
		pref.setDefault(STATE_SHOW_SITES, true);
		showSitesAction = new Action() {
			public void run() {
				getTreeViewer().refresh();
				pref.setValue(STATE_SHOW_SITES, showSitesAction.isChecked());
				UpdateUI.getDefault().savePluginPreferences();
			}
		};
		showSitesAction.setText("Show Sites");
		showSitesAction.setImageDescriptor(UpdateUIImages.DESC_SITE_OBJ);
		showSitesAction.setChecked(pref.getBoolean(STATE_SHOW_SITES));
		showSitesAction.setToolTipText("Show Sites");
	}

	private void makeShowUnconfiguredFeaturesAction() {
		final Preferences pref = UpdateUI.getDefault().getPluginPreferences();
		pref.setDefault(STATE_SHOW_UNCONF, false);
		showUnconfFeaturesAction = new Action() {
			public void run() {
				getTreeViewer().refresh();
				pref.setValue(STATE_SHOW_UNCONF, showUnconfFeaturesAction.isChecked());
				UpdateUI.getDefault().savePluginPreferences();
			}
		};
		WorkbenchHelp.setHelp(
			showUnconfFeaturesAction,
			"org.eclipse.update.ui.CofigurationView_showUnconfFeaturesAction");
		showUnconfFeaturesAction.setText(UpdateUI.getString(KEY_SHOW_UNCONF_FEATURES));
		showUnconfFeaturesAction.setImageDescriptor(
			UpdateUIImages.DESC_UNCONF_FEATURE_OBJ);
		showUnconfFeaturesAction.setChecked(pref.getBoolean(STATE_SHOW_UNCONF));
		showUnconfFeaturesAction.setToolTipText(
			UpdateUI.getString(KEY_SHOW_UNCONF_FEATURES_TOOLTIP));
	}

	protected void fillActionBars(IActionBars bars) {
		IToolBarManager tbm = bars.getToolBarManager();
		tbm.add(showSitesAction);
		tbm.add(showNestedFeaturesAction);
		tbm.add(showUnconfFeaturesAction);
		tbm.add(new Separator());
		addDrillDownAdapter(bars);
		tbm.add(new Separator());
		tbm.add(collapseAllAction);
	}

	protected Object getSelectedObject() {
		ISelection selection = getTreeViewer().getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() == 1)
				return ssel.getFirstElement();
		}
		return null;
	}

	protected void fillContextMenu(IMenuManager manager) {
		Object obj = getSelectedObject();

		if (obj instanceof ILocalSite) {
			manager.add(revertAction);
			preserveAction.setConfiguration(((ILocalSite) obj).getCurrentConfiguration());
			manager.add(preserveAction);
			manager.add(new Separator());
		} else if (obj instanceof IConfiguredSiteAdapter) {
			siteStateAction.setSite(((IConfiguredSiteAdapter) obj).getConfiguredSite());
			manager.add(siteStateAction);
			manager.add(new Separator());
		} else if (obj instanceof ConfiguredFeatureAdapter) {
			ConfiguredFeatureAdapter adapter = (ConfiguredFeatureAdapter) obj;
			MenuManager mgr = new MenuManager("Replace with");
			mgr.add(swapVersionAction);
			manager.add(mgr);
			featureStateAction.setFeature(adapter);
			manager.add(featureStateAction);
			manager.add(uninstallFeatureAction);
			manager.add(new Separator());
			
			boolean enable = (adapter.isOptional() || !adapter.isIncluded());
			swapVersionAction.setEnabled(enable);
			featureStateAction.setEnabled(enable);
			uninstallFeatureAction.setEnabled(enable);
		}

		addDrillDownAdapter(manager);

		if (obj instanceof IFeatureAdapter) {
			IFeatureAdapter adapter = (IFeatureAdapter) obj;
			manager.add(new Separator());
			showStatusAction.setFeature(adapter);
			manager.add(showStatusAction);
			manager.add(propertiesAction);
		}
	}

	public void installSiteAdded(IConfiguredSite csite) {
		asyncRefresh();
	}
	public void installSiteRemoved(IConfiguredSite site) {
		asyncRefresh();
	}
	public void featureInstalled(IFeature feature) {
		asyncRefresh();
	}
	public void featureRemoved(IFeature feature) {
		asyncRefresh();
	}
	public void featureConfigured(IFeature feature) {
	}

	public void featureUnconfigured(IFeature feature) {
	}

	public void currentInstallConfigurationChanged(IInstallConfiguration configuration) {
		asyncRefresh();
	}

	public void installConfigurationRemoved(IInstallConfiguration configuration) {
		asyncRefresh();
	}

	private void asyncRefresh() {
		Display display = SWTUtil.getStandardDisplay();
		if (display == null)
			return;
		if (getControl().isDisposed())
			return;
		display.asyncExec(new Runnable() {
			public void run() {
				if (!getControl().isDisposed())
					getTreeViewer().refresh();
			}
		});
	}

	private Object[] getFeatures(
		final IConfiguredSiteAdapter siteAdapter,
		final boolean configuredOnly) {
		final IConfiguredSite csite = siteAdapter.getConfiguredSite();
		final Object[][] bag = new Object[1][];
		refreshLock = true;

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				ArrayList result = new ArrayList();
				IFeatureReference[] refs;

				if (configuredOnly)
					refs = csite.getConfiguredFeatures();
				else {
					ISite site = csite.getSite();
					refs = site.getFeatureReferences();
				}
				monitor.beginTask(
					UpdateUI.getString("ConfigurationView.loading"),
					refs.length);

				for (int i = 0; i < refs.length; i++) {
					IFeatureReference ref = refs[i];
					IFeature feature;
					try {
						monitor.subTask(ref.getURL().toString());
						feature = ref.getFeature(null);
					} catch (CoreException e) {
						feature = new MissingFeature(ref.getSite(), ref.getURL());
					}
					monitor.worked(1);
					result.add(
						new ConfiguredFeatureAdapter(
							siteAdapter,
							feature,
							csite.isConfigured(feature),
							false,
							false));
				}
				monitor.done();
				bag[0] = getRootFeatures(result);
			}
		};
		try {
			getViewSite().getWorkbenchWindow().run(true, false, op);
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
		} finally {
			refreshLock = false;
		}
		return bag[0];
	}

	private Object[] getRootFeatures(ArrayList list) {
		ArrayList children = new ArrayList();
		ArrayList result = new ArrayList();
		try {
			for (int i = 0; i < list.size(); i++) {
				ConfiguredFeatureAdapter cf = (ConfiguredFeatureAdapter) list.get(i);
				IFeature feature = cf.getFeature(null);
				if (feature != null)
					addChildFeatures(
						feature,
						cf.getConfiguredSite(),
						children,
						cf.isConfigured());
			}
			for (int i = 0; i < list.size(); i++) {
				ConfiguredFeatureAdapter cf = (ConfiguredFeatureAdapter) list.get(i);
				IFeature feature = cf.getFeature(null);
				if (feature != null && isChildFeature(feature, children) == false)
					result.add(cf);
			}
		} catch (CoreException e) {
			return list.toArray();
		}
		return result.toArray();
	}

	private void addChildFeatures(
		IFeature feature,
		IConfiguredSite csite,
		ArrayList children,
		boolean configured) {
		try {
			IIncludedFeatureReference[] included = feature.getIncludedFeatureReferences();
			for (int i = 0; i < included.length; i++) {
				IFeature childFeature;
				try {
					childFeature = included[i].getFeature(!configured, csite, null);
				} catch (CoreException e) {
					childFeature = new MissingFeature(included[i]);
				}
				children.add(childFeature);
			}
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}
	}

	private boolean isChildFeature(IFeature feature, ArrayList children) {
		for (int i = 0; i < children.size(); i++) {
			IFeature child = (IFeature) children.get(i);
			if (feature.getVersionedIdentifier().equals(child.getVersionedIdentifier()))
				return true;
		}
		return false;
	}
	protected Object getRootObject() {
		return UpdateUI.getDefault().getUpdateModel();
	}
}
