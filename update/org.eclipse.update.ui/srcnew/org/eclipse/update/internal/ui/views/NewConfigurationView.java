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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.operations.IUpdateModelChangedListener;
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
	private static final String STATE_SHOW_UNCONF =
		"ConfigurationView.showUnconf";
	private static final String STATE_SHOW_SITES =
		"ConfigurationView.showSites";
	private static final String STATE_SHOW_NESTED_FEATURES =
		"ConfigurationView.showNestedFeatures";
	private static final String KEY_PRESERVE =
		"ConfigurationView.Popup.preserve";
	private static final String KEY_MISSING_FEATURE =
		"ConfigurationView.missingFeature";

	private Action showSitesAction;
	private Action showNestedFeaturesAction;
	private SwapVersionAction swapVersionAction;
	private FeatureStateAction featureStateAction;
	private UninstallFeatureAction uninstallFeatureAction;
	private InstallOptionalFeatureAction installOptFeatureAction;
	private Action showUnconfFeaturesAction;
	private RevertConfigurationAction revertAction;
	private SaveConfigurationAction preserveAction;
	private Action propertiesAction;
	private SiteStateAction2 siteStateAction;
	private SashForm splitter;
	private ConfigurationPreview preview;
	private Hashtable previewTasks;

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
		public void inputChanged(
			Viewer viewer,
			Object oldInput,
			Object newInput) {
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
				return (localSite != null) ? new Object[] { localSite }
				: new Object[0];
			}

			if (parent instanceof ILocalSite) {
				Object[] csites = openLocalSite();
				if (showSitesAction.isChecked())
					return csites;
				ArrayList result = new ArrayList();
				boolean showUnconf = showUnconfFeaturesAction.isChecked();
				for (int i = 0; i < csites.length; i++) {
					IConfiguredSiteAdapter adapter =
						(IConfiguredSiteAdapter) csites[i];
					Object[] roots = getFeatures(adapter, !showUnconf);
					for (int j = 0; j < roots.length; j++) {
						result.add(roots[j]);
					}
				}
				return result.toArray();
			}

			if (parent instanceof IConfiguredSiteAdapter) {
				return getFeatures(
					(IConfiguredSiteAdapter) parent,
					!showUnconfFeaturesAction.isChecked());
			}
			if (parent instanceof ConfiguredFeatureAdapter
				&& showNestedFeaturesAction.isChecked()) {
				return ((ConfiguredFeatureAdapter) parent).getIncludedFeatures(
					null);
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			return null;
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ConfiguredFeatureAdapter) {
				return showNestedFeaturesAction.isChecked()
					&& ((ConfiguredFeatureAdapter) parent).hasIncludedFeatures(
						null);
			}
			if (parent instanceof ConfiguredSiteAdapter) {
				IConfiguredSite site =
					((ConfiguredSiteAdapter) parent).getConfiguredSite();
				if (site.isEnabled()) {
					if (!showUnconfFeaturesAction.isChecked())
						return site.getConfiguredFeatures().length > 0;
					return site.getFeatureReferences().length > 0;
				}
				return (showUnconfFeaturesAction.isChecked());
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
						feature
							.getVersionedIdentifier()
							.getVersion()
							.toString();
					String pending = "";
					if (UpdateManager
						.getOperationsManager()
						.findPendingChange(feature)
						!= null)
						pending = " (pending changes)";
					return feature.getLabel() + " " + version + pending;
				} catch (CoreException e) {
					return "Error";
				}
			}
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			UpdateLabelProvider provider =
				UpdateUI.getDefault().getLabelProvider();
			if (obj instanceof ILocalSite)
				return eclipseImage;

			if (obj instanceof ConfiguredFeatureAdapter)
				return getFeatureImage(
					provider,
					(ConfiguredFeatureAdapter) obj);

			if (obj instanceof IConfiguredSiteAdapter) {
				IConfiguredSite csite =
					((IConfiguredSiteAdapter) obj).getConfiguredSite();
				int flags =
					csite.isUpdatable() ? 0 : UpdateLabelProvider.F_LINKED;
				if (!csite.isEnabled())
					flags |= UpdateLabelProvider.F_UNCONFIGURED;
				return provider.get(
					provider.getLocalSiteDescriptor(csite),
					flags);
			}
			return null;
		}

		private Image getFeatureImage(
			UpdateLabelProvider provider,
			ConfiguredFeatureAdapter adapter) {
			try {
				IFeature feature = adapter.getFeature(null);
				if (feature instanceof MissingFeature) {
					if (((MissingFeature) feature).isOptional())
						return provider.get(
							UpdateUIImages.DESC_NOTINST_FEATURE_OBJ);
					return provider.get(
						UpdateUIImages.DESC_FEATURE_OBJ,
						UpdateLabelProvider.F_ERROR);
				}

				boolean efix = feature.isPatch();
				ImageDescriptor baseDesc =
					efix
						? UpdateUIImages.DESC_EFIX_OBJ
						: (adapter.isConfigured()
							? UpdateUIImages.DESC_FEATURE_OBJ
							: UpdateUIImages.DESC_UNCONF_FEATURE_OBJ);

				int flags = 0;
				if (efix && !adapter.isConfigured())
					flags |= UpdateLabelProvider.F_UNCONFIGURED;
				if (UpdateManager
					.getOperationsManager()
					.findPendingChange(feature)
					== null) {

					int code =
						getStatusCode(
							feature,
							getLocalSite().getFeatureStatus(feature));
					switch (code) {
						case IFeature.STATUS_UNHAPPY :
							flags |= UpdateLabelProvider.F_ERROR;
							break;
						case IFeature.STATUS_AMBIGUOUS :
							flags |= UpdateLabelProvider.F_WARNING;
							break;
						default :
							if (adapter.isConfigured() && adapter.isUpdated())
								flags |= UpdateLabelProvider.F_UPDATED;
							break;
					}
				}
				return provider.get(baseDesc, flags);
			} catch (CoreException e) {
				return provider.get(
					UpdateUIImages.DESC_FEATURE_OBJ,
					UpdateLabelProvider.F_ERROR);
			}
		}
	}
	
	class PreviewTask implements IPreviewTask {
		private String name;
		private String desc;
		private IAction action;
		public PreviewTask(String name, String desc, IAction action) {
			this.name = name;
			this.desc = desc;
			this.action = action;
		}
		
		public String getName() {
			if (name!=null) return name;
			return action.getText();
		}
		public String getDescription() {
			return desc;
		}
		public void run() {
			action.run();
		}
		public boolean isEnabled() {
			return action.isEnabled();
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
		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			localSite.addLocalSiteChangedListener(this);
		} catch (CoreException e) {
			UpdateUI.logException(e);
		}

		modelListener = new IUpdateModelChangedListener() {
			public void objectsAdded(Object parent, Object[] children) {
			}
			public void objectsRemoved(Object parent, Object[] children) {
			}
			public void objectChanged(final Object obj, String property) {
				if (refreshLock)
					return;
				Control control = getControl();
				if (!control.isDisposed()) {
					control.getDisplay().asyncExec(new Runnable() {
						public void run() {
							treeViewer.refresh();
						}
					});
				}
			}
		};
		UpdateManager.getOperationsManager().addUpdateModelChangedListener(
			modelListener);
		WorkbenchHelp.setHelp(
			getControl(),
			"org.eclipse.update.ui.ConfigurationView");
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
					IInstallConfiguration config =
						localSite.getCurrentConfiguration();
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
				IInstallConfiguration config =
					localSite.getCurrentConfiguration();
				config.removeInstallConfigurationChangedListener(this);
			} catch (CoreException e) {
				UpdateUI.logException(e);
			}
			initialized = false;
		}
		UpdateManager.getOperationsManager().removeUpdateModelChangedListener(modelListener);
		if (preview!=null)
			preview.dispose();
		super.dispose();
	}

	protected void makeActions() {
		super.makeActions();

		initDrillDown();

		featureStateAction = new FeatureStateAction();

		siteStateAction = new SiteStateAction2();

		preserveAction =
			new SaveConfigurationAction(UpdateUI.getString(KEY_PRESERVE));
		WorkbenchHelp.setHelp(
			preserveAction,
			"org.eclipse.update.ui.CofigurationView_preserveAction");

		revertAction = new RevertConfigurationAction("Revert...");
		WorkbenchHelp.setHelp(
			revertAction,
			"org.eclipse.update.ui.CofigurationView_revertAction");

		propertiesAction =
			new PropertyDialogAction(
				UpdateUI.getActiveWorkbenchShell(),
				getTreeViewer());
		propertiesAction.setEnabled(false);
		WorkbenchHelp.setHelp(
			propertiesAction,
			"org.eclipse.update.ui.CofigurationView_propertiesAction");

		uninstallFeatureAction = new UninstallFeatureAction("Uninstall");
		
		installOptFeatureAction = new InstallOptionalFeatureAction("Install");

		swapVersionAction = new SwapVersionAction("&Another Version...");

		makeShowUnconfiguredFeaturesAction();
		makeShowSitesAction();
		makeShowNestedFeaturesAction();
		makePreviewTasks();
		getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES, propertiesAction);
	}

	private void makeShowNestedFeaturesAction() {
		final Preferences pref = UpdateUI.getDefault().getPluginPreferences();
		pref.setDefault(STATE_SHOW_NESTED_FEATURES, true);
		showNestedFeaturesAction = new Action() {
			public void run() {
				getTreeViewer().refresh();
				pref.setValue(
					STATE_SHOW_NESTED_FEATURES,
					showNestedFeaturesAction.isChecked());
			}
		};
		showNestedFeaturesAction.setText("Show Nested Features");
		showNestedFeaturesAction.setImageDescriptor(
			UpdateUIImages.DESC_SHOW_HIERARCHY);
		showNestedFeaturesAction.setHoverImageDescriptor(
			UpdateUIImages.DESC_SHOW_HIERARCHY_H);
		showNestedFeaturesAction.setDisabledImageDescriptor(
			UpdateUIImages.DESC_SHOW_HIERARCHY_D);
		
		showNestedFeaturesAction.setChecked(
			pref.getBoolean(STATE_SHOW_NESTED_FEATURES));
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
		showSitesAction.setText("Show Install Locations");
		showSitesAction.setImageDescriptor(UpdateUIImages.DESC_LSITE_OBJ);
		showSitesAction.setChecked(pref.getBoolean(STATE_SHOW_SITES));
		showSitesAction.setToolTipText("Show Install Locations");
	}

	private void makeShowUnconfiguredFeaturesAction() {
		final Preferences pref = UpdateUI.getDefault().getPluginPreferences();
		pref.setDefault(STATE_SHOW_UNCONF, false);
		showUnconfFeaturesAction = new Action() {
			public void run() {
				getTreeViewer().refresh();
				pref.setValue(
					STATE_SHOW_UNCONF,
					showUnconfFeaturesAction.isChecked());
				UpdateUI.getDefault().savePluginPreferences();
			}
		};
		WorkbenchHelp.setHelp(
			showUnconfFeaturesAction,
			"org.eclipse.update.ui.CofigurationView_showUnconfFeaturesAction");
		showUnconfFeaturesAction.setText(
			UpdateUI.getString(KEY_SHOW_UNCONF_FEATURES));
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
		if (selection instanceof IStructuredSelection
			&& !selection.isEmpty()) {
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
			manager.add(preserveAction);
			manager.add(new Separator());
		} else if (obj instanceof IConfiguredSiteAdapter) {
			manager.add(siteStateAction);
			manager.add(new Separator());
		} else if (obj instanceof ConfiguredFeatureAdapter) {
			try {
				MenuManager mgr = new MenuManager("Replace with");
				mgr.add(swapVersionAction);
				manager.add(mgr);

				manager.add(featureStateAction);

				IFeature feature = ((ConfiguredFeatureAdapter) obj).getFeature(null);
				if (feature instanceof MissingFeature) {
					manager.add(installOptFeatureAction);
				} else {
					manager.add(uninstallFeatureAction);
				}
				manager.add(new Separator());
			} catch (CoreException e) {
			}
		}

		addDrillDownAdapter(manager);

		if (obj instanceof IFeatureAdapter) {
			manager.add(new Separator());
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
						feature =
							new MissingFeature(ref.getSite(), ref.getURL());
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
			if (getViewSite().getWorkbenchWindow().getShell().isVisible())
				getViewSite().getWorkbenchWindow().run(true, false, op);
			else
				op.run(new NullProgressMonitor());
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
				ConfiguredFeatureAdapter cf =
					(ConfiguredFeatureAdapter) list.get(i);
				IFeature feature = cf.getFeature(null);
				if (feature != null)
					addChildFeatures(
						feature,
						cf.getConfiguredSite(),
						children,
						cf.isConfigured());
			}
			for (int i = 0; i < list.size(); i++) {
				ConfiguredFeatureAdapter cf =
					(ConfiguredFeatureAdapter) list.get(i);
				IFeature feature = cf.getFeature(null);
				if (feature != null
					&& isChildFeature(feature, children) == false)
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
			IIncludedFeatureReference[] included =
				feature.getIncludedFeatureReferences();
			for (int i = 0; i < included.length; i++) {
				IFeature childFeature;
				try {
					childFeature =
						included[i].getFeature(!configured, csite, null);
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
			if (feature
				.getVersionedIdentifier()
				.equals(child.getVersionedIdentifier()))
				return true;
		}
		return false;
	}
	protected Object getRootObject() {
		return UpdateUI.getDefault().getUpdateModel();
	}

	protected void handleDoubleClick(DoubleClickEvent e) {
		if (e.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) e.getSelection();
			if (ssel.size() == 1
				&& ssel.getFirstElement() instanceof IFeatureAdapter)
				propertiesAction.run();
		}
	}
	
	public void createPartControl(Composite parent) {
		splitter = new SashForm(parent, SWT.HORIZONTAL);
		super.createPartControl(splitter);
		preview = new ConfigurationPreview(this);
		preview.createControl(splitter);
		splitter.setWeights(new int [] {1, 2});
		getTreeViewer().expandToLevel(2);
	}
	
	public Control getControl() {
		return splitter;
	}

	private int getStatusCode(IFeature feature, IStatus status) {
		int code = status.getCode();
		if (code == IFeature.STATUS_UNHAPPY) {
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				for (int i = 0; i < children.length; i++) {
					IStatus child = children[i];
					if (child.isMultiStatus()
						|| child.getCode() != IFeature.STATUS_DISABLED)
						return code;
				}
				// If we are here, global status is unhappy
				// because one or more included features
				// is disabled.
				if (UpdateManager.hasObsoletePatches(feature)) {
					// The disabled included features
					// are old patches that are now
					// subsumed by better versions of
					// the features they were designed to
					// patch.
					return IFeature.STATUS_HAPPY;
				}
			}
		}
		return code;
	}
	
	protected void handleSelectionChanged(SelectionChangedEvent e) {
		IStructuredSelection ssel = (IStructuredSelection) e.getSelection();
		Object obj = ssel.getFirstElement();
		if (obj instanceof IFeatureAdapter) {
			try {
				ConfiguredFeatureAdapter adapter = (ConfiguredFeatureAdapter) obj;
				IFeature feature = adapter.getFeature(null);
				boolean enable = (adapter.isOptional() || !adapter.isIncluded());

				swapVersionAction.setEnabled(enable);
				featureStateAction.setFeature(adapter);
				featureStateAction.setEnabled(enable);
				if (feature instanceof MissingFeature) {
					MissingFeature mf = (MissingFeature) feature;
					installOptFeatureAction.setEnabled(
						mf.isOptional() && mf.getOriginatingSiteURL() != null);
					uninstallFeatureAction.setEnabled(false);
				} else {
					uninstallFeatureAction.setEnabled(enable && !adapter.isConfigured());
					installOptFeatureAction.setEnabled(false);
				}
			} catch (CoreException ex) {
				UpdateUI.logException(ex);
			}
			propertiesAction.setEnabled(true);
		} else {
			propertiesAction.setEnabled(false);
		}
		if (obj instanceof ILocalSite) {
			preserveAction.setConfiguration(((ILocalSite) obj).getCurrentConfiguration());
			preserveAction.setEnabled(true);
		} else if (obj instanceof IConfiguredSiteAdapter) {
			siteStateAction.setSite(((IConfiguredSiteAdapter) obj).getConfiguredSite());
			siteStateAction.setEnabled(true);
		}
		preview.setSelection(ssel);
	}

	private void makePreviewTasks() {
		previewTasks = new Hashtable();
		Class key;
		ArrayList array = new ArrayList();
		// local site tasks
		key = ILocalSite.class;
		array.add(new PreviewTask("Revert to Previous", "You can revert to one of the previous configurations if you are having problems with the current one.", revertAction));
		array.add(new PreviewTask("Save", "As new configurations are added, the old ones eventually get deleted. Use this task to save a good configuration you can always revert to.", preserveAction));
		previewTasks.put(key, array.toArray(new IPreviewTask[array.size()]));

		// configured site tasks
		array.clear();
		key = IConfiguredSiteAdapter.class;
		array.add(new PreviewTask(null, "You can enable or disable an entire install location. Disabling a location is equivalent to disabling every feature in it.", siteStateAction));
		previewTasks.put(key, array.toArray(new IPreviewTask[array.size()]));
		
		// feature adapter tasks
		array.clear();
		key = IFeatureAdapter.class;
		array.add(new PreviewTask("Replace With Another Version", "This tasks allows you to disable the current version of the feature and replace it with another version from the list of currently disabled features.", swapVersionAction));
		array.add(new PreviewTask(null, "You can enable or disable a feature. Function provided by the feature will be removed but the feature itself will still be present to be enabled later.", featureStateAction));
		array.add(new PreviewTask("Install from Originating Server", "This optional feature was not originally installed. You can install it now by connecting to the originating server of the parent.", installOptFeatureAction));
		array.add(new PreviewTask("Uninstall", "This feature is currently not used and can be uninstalled from the product.", uninstallFeatureAction));  
		array.add(new PreviewTask("Show Properties", "This task allows you to view properties of the feature such as version, provider name, license agreement etc.", propertiesAction));
		previewTasks.put(key, array.toArray(new IPreviewTask[array.size()]));
	}

	public IPreviewTask [] getPreviewTasks(Object object) {
		IPreviewTask [] tasks = null;
		
		if (object instanceof IFeatureAdapter)
			tasks = (IPreviewTask[])previewTasks.get(IFeatureAdapter.class);
		if (object instanceof ILocalSite)
			tasks = (IPreviewTask[])previewTasks.get(ILocalSite.class);
		if (object instanceof IConfiguredSiteAdapter)
			tasks = (IPreviewTask[])previewTasks.get(IConfiguredSiteAdapter.class);
		if (tasks!=null) return tasks;
		return new IPreviewTask[0];
	}
}
