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
import java.lang.reflect.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.part.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.operations.IUpdateModelChangedListener;

/**
 * Insert the type's description here.
 * @see ViewPart
 */
public class NewConfigurationView
	extends ViewPart
	implements
		IInstallConfigurationChangedListener,
		IConfiguredSiteChangedListener,
		ILocalSiteChangedListener {
	private TreeViewer treeViewer;
	private DrillDownAdapter drillDownAdapter;
	private Action collapseAllAction;
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
	private Action propertiesAction;
	private SiteStateAction2 siteStateAction;
	private Action installationHistoryAction;
	private Action newExtensionLocationAction;
	private FindUpdatesAction findUpdatesAction;
	private SashForm splitter;
	private ConfigurationPreview preview;
	private Hashtable previewTasks;
	private String viewName;

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
				IFeatureAdapter[] nested =
					((ConfiguredFeatureAdapter) parent).getIncludedFeatures(
						null);
				if (showUnconfFeaturesAction.isChecked())
					return nested;
				ArrayList result = new ArrayList();
				for (int i = 0; i < nested.length; i++) {
					if (((ConfiguredFeatureAdapter) nested[i]).isConfigured())
						result.add(nested[i]);
				}
				return (IFeatureAdapter[]) result.toArray(
					new IFeatureAdapter[result.size()]);
			}
			return new Object[0];
		}

		public Object getParent(Object child) {
			return null;
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof ConfiguredFeatureAdapter) {
				if (!showNestedFeaturesAction.isChecked())
					return false;
				IFeatureAdapter[] features =
					((ConfiguredFeatureAdapter) parent).getIncludedFeatures(
						null);

				if (showUnconfFeaturesAction.isChecked())
					return features.length > 0;

				for (int i = 0; i < features.length; i++) {
					if (((ConfiguredFeatureAdapter) features[i])
						.isConfigured())
						return true;
				}
				return false;
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
					if (OperationsManager.findPendingOperation(feature)
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
				if (OperationsManager.findPendingOperation(feature) == null) {
					ILocalSite localSite = getLocalSite();
					if (localSite != null) {
						int code =
							getStatusCode(
								feature,
								localSite.getFeatureStatus(feature));
						switch (code) {
							case IFeature.STATUS_UNHAPPY :
								flags |= UpdateLabelProvider.F_ERROR;
								break;
							case IFeature.STATUS_AMBIGUOUS :
								flags |= UpdateLabelProvider.F_WARNING;
								break;
							default :
								if (adapter.isConfigured()
									&& adapter.isUpdated())
									flags |= UpdateLabelProvider.F_UPDATED;
								break;
						}
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
			if (name != null)
				return name;
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
		treeViewer.setContentProvider(new LocalSiteProvider());
		treeViewer.setLabelProvider(new LocalSiteLabelProvider());
		treeViewer.setInput(UpdateUI.getDefault().getUpdateModel());
		treeViewer.setSorter(new ConfigurationSorter());
		ILocalSite localSite = getLocalSite();
		if (localSite != null)
			localSite.addLocalSiteChangedListener(this);

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
							handleSelectionChanged(
								(IStructuredSelection) treeViewer
									.getSelection());
						}
					});
				}
			}
		};
		OperationsManager.addUpdateModelChangedListener(modelListener);
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
				ILocalSite localSite = getLocalSite();
				if (localSite == null)
					return;
				IInstallConfiguration config =
					getLocalSite().getCurrentConfiguration();
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
			}
		});
		return bag[0];
	}

	public void dispose() {
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
		if (initialized) {
			ILocalSite localSite = getLocalSite();
			if (localSite != null) {
				localSite.removeLocalSiteChangedListener(this);
				IInstallConfiguration config =
					localSite.getCurrentConfiguration();
				config.removeInstallConfigurationChangedListener(this);
			}
			initialized = false;
		}
		OperationsManager.removeUpdateModelChangedListener(modelListener);
		if (preview != null)
			preview.dispose();
		super.dispose();
	}

	protected void makeActions() {
		collapseAllAction = new Action() {
			public void run() {
				treeViewer.getControl().setRedraw(false);
				treeViewer.collapseToLevel(
					treeViewer.getInput(),
					TreeViewer.ALL_LEVELS);
				treeViewer.getControl().setRedraw(true);
			}
		};
		collapseAllAction.setText("Collapse All");
		collapseAllAction.setToolTipText("Collapse All");
		collapseAllAction.setImageDescriptor(UpdateUIImages.DESC_COLLAPSE_ALL);

		drillDownAdapter = new DrillDownAdapter(treeViewer);

		featureStateAction = new FeatureStateAction();

		siteStateAction = new SiteStateAction2();

		revertAction = new RevertConfigurationAction("Revert...");
		WorkbenchHelp.setHelp(
			revertAction,
			"org.eclipse.update.ui.CofigurationView_revertAction");

		installationHistoryAction =
			new InstallationHistoryAction(
				"Installation History",
				UpdateUIImages.DESC_HISTORY_OBJ);
		newExtensionLocationAction =
			new NewExtensionLocationAction(
				"Extension Location...",
				UpdateUIImages.DESC_ESITE_OBJ);
		propertiesAction =
			new PropertyDialogAction(
				UpdateUI.getActiveWorkbenchShell(),
				treeViewer);
		WorkbenchHelp.setHelp(
			propertiesAction,
			"org.eclipse.update.ui.CofigurationView_propertiesAction");

		uninstallFeatureAction = new UninstallFeatureAction("Uninstall");

		installOptFeatureAction =
			new InstallOptionalFeatureAction(
				getControl().getShell(),
				"Install");

		swapVersionAction = new SwapVersionAction("&Another Version...");

		findUpdatesAction =
			new FindUpdatesAction(getControl().getShell(), "Find Updates...");

		makeShowUnconfiguredFeaturesAction();
		makeShowSitesAction();
		makeShowNestedFeaturesAction();
		makePreviewTasks();
		getViewSite().getActionBars().setGlobalActionHandler(
			IWorkbenchActionConstants.PROPERTIES,
			propertiesAction);
	}

	private void makeShowNestedFeaturesAction() {
		final Preferences pref = UpdateUI.getDefault().getPluginPreferences();
		pref.setDefault(STATE_SHOW_NESTED_FEATURES, true);
		showNestedFeaturesAction = new Action() {
			public void run() {
				treeViewer.refresh();
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
				treeViewer.refresh();
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
				pref.setValue(
					STATE_SHOW_UNCONF,
					showUnconfFeaturesAction.isChecked());
				UpdateUI.getDefault().savePluginPreferences();
				treeViewer.refresh();
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
		drillDownAdapter.addNavigationActions(bars.getToolBarManager());
		tbm.add(new Separator());
		tbm.add(collapseAllAction);
	}

	protected Object getSelectedObject() {
		ISelection selection = treeViewer.getSelection();
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
			manager.add(findUpdatesAction);
		} else if (obj instanceof IConfiguredSiteAdapter) {
			manager.add(siteStateAction);
		}

		if (obj instanceof ILocalSite
			|| obj instanceof IConfiguredSiteAdapter) {
			manager.add(new Separator());
			MenuManager mgr = new MenuManager("New");
			mgr.add(newExtensionLocationAction);
			manager.add(mgr);
			manager.add(new Separator());
		} else if (obj instanceof ConfiguredFeatureAdapter) {
			try {
				MenuManager mgr = new MenuManager("Replace with");
				mgr.add(swapVersionAction);
				manager.add(mgr);

				manager.add(featureStateAction);

				IFeature feature =
					((ConfiguredFeatureAdapter) obj).getFeature(null);
				if (feature instanceof MissingFeature) {
					manager.add(installOptFeatureAction);
				} else {
					manager.add(uninstallFeatureAction);
				}
				manager.add(new Separator());
				manager.add(findUpdatesAction);
				manager.add(new Separator());
			} catch (CoreException e) {
			}
		}

		drillDownAdapter.addNavigationActions(manager);

		if (obj instanceof ILocalSite) {
			manager.add(new Separator());
			manager.add(installationHistoryAction);
		}

		if (obj instanceof IFeatureAdapter
			|| obj instanceof ILocalSite
			|| obj instanceof IConfiguredSiteAdapter) {
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
					treeViewer.refresh();
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

	protected void handleDoubleClick(DoubleClickEvent e) {
		if (e.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) e.getSelection();
			Object obj = ssel.getFirstElement();
			if (obj!=null)
				propertiesAction.run();
		}
	}

	public void createPartControl(Composite parent) {
		splitter = new SashForm(parent, SWT.HORIZONTAL);
		splitter.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite leftContainer = createLineContainer(splitter);
		Composite rightContainer = createLineContainer(splitter);
		createTreeViewer(leftContainer);
		makeActions();
		createVerticalLine(leftContainer);
		createVerticalLine(rightContainer);
		preview = new ConfigurationPreview(this);
		preview.createControl(rightContainer);
		preview.getScrollingControl().setLayoutData(
			new GridData(GridData.FILL_BOTH));
		splitter.setWeights(new int[] { 2, 3 });
		fillActionBars(getViewSite().getActionBars());

		treeViewer.expandToLevel(2);
	}

	private void createTreeViewer(Composite parent) {
		treeViewer =
			new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer.setUseHashlookup(true);
		initProviders();

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker("additions"));
				fillContextMenu(manager);
			}
		});

		treeViewer.getControl().setMenu(
			menuMgr.createContextMenu(treeViewer.getControl()));
		getSite().registerContextMenu(menuMgr, treeViewer);

		treeViewer
			.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});

		getSite().setSelectionProvider(treeViewer);

	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	private Composite createLineContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		container.setLayout(layout);
		return container;
	}

	private void createVerticalLine(Composite parent) {
		Label line = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gd.widthHint = 1;
		line.setLayoutData(gd);
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
				if (UpdateUtils.hasObsoletePatches(feature)) {
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

	protected void handleSelectionChanged(IStructuredSelection ssel) {
		Object obj = ssel.getFirstElement();
		if (obj instanceof IFeatureAdapter) {
			try {
				ConfiguredFeatureAdapter adapter =
					(ConfiguredFeatureAdapter) obj;
				IFeature feature = adapter.getFeature(null);
				boolean enable =
					(adapter.isOptional() || !adapter.isIncluded());
				boolean missing = feature instanceof MissingFeature;

				swapVersionAction.setEnabled(enable && !missing);

				featureStateAction.setFeature(adapter);
				featureStateAction.setEnabled(enable && !missing);

				if (enable && !missing && adapter.isConfigured()) {
					findUpdatesAction.setEnabled(true);
					findUpdatesAction.setFeature(feature);
				} else {
					findUpdatesAction.setEnabled(false);
				}

				if (missing) {
					MissingFeature mf = (MissingFeature) feature;
					installOptFeatureAction.setEnabled(
						mf.isOptional() && mf.getOriginatingSiteURL() != null);
					installOptFeatureAction.setFeature(mf);
					uninstallFeatureAction.setEnabled(false);
				} else {
					uninstallFeatureAction.setEnabled(
						enable && !adapter.isConfigured());
					installOptFeatureAction.setEnabled(false);
				}
			} catch (CoreException ex) {
				UpdateUI.logException(ex);
			}
		}
		if (obj instanceof ILocalSite) {
			propertiesAction.setEnabled(true);
			findUpdatesAction.setEnabled(true);
			findUpdatesAction.setFeature(null);
		} else if (obj instanceof IConfiguredSiteAdapter) {
			siteStateAction.setSite(
				((IConfiguredSiteAdapter) obj).getConfiguredSite());
			siteStateAction.setEnabled(true);
		}
		preview.setSelection(ssel);
	}

	protected void handleSelectionChanged(SelectionChangedEvent e) {
		handleSelectionChanged(((IStructuredSelection) e.getSelection()));
	}

	private void makePreviewTasks() {
		previewTasks = new Hashtable();
		Class key;
		ArrayList array = new ArrayList();
		// local site tasks
		key = ILocalSite.class;
		array.add(
			new PreviewTask(
				"Revert to Previous",
				"You can revert to one of the previous configurations if you are having problems with the current one.",
				revertAction));
		array.add(
			new PreviewTask(
				"Add an Extension Location",
				"This task allows you to locate and add an extension location to the current configuration.",
				newExtensionLocationAction));
		array.add(
			new PreviewTask(
				"Show Activities",
				"This task allows you to view the activities that caused the creation of this configuration.",
				propertiesAction));
		array.add(
			new PreviewTask(
				"Scan for Updates",
				"Search for updates for all the installed features.",
				findUpdatesAction));
		array.add(
			new PreviewTask(
				"View Installation History",
				"This task allows you to view all activities since the installation of the product.",
				installationHistoryAction));

		previewTasks.put(key, array.toArray(new IPreviewTask[array.size()]));

		// configured site tasks
		array.clear();
		key = IConfiguredSiteAdapter.class;
		array.add(
			new PreviewTask(
				null,
				"You can enable or disable an entire install location. Disabling a location is equivalent to disabling every feature in it.",
				siteStateAction));
		array.add(
			new PreviewTask(
				"Add an Extension Location",
				"Locate and add an extension location to the current configuration.",
				newExtensionLocationAction));
		array.add(
			new PreviewTask(
				"Show Properties",
				"View the properties of the install location.",
				propertiesAction));
		previewTasks.put(key, array.toArray(new IPreviewTask[array.size()]));

		// feature adapter tasks
		array.clear();
		key = IFeatureAdapter.class;
		array.add(
			new PreviewTask(
				null,
				"You can enable or disable a feature. Function provided by the feature will be removed but the feature itself will still be present to be enabled later.",
				featureStateAction));
		array.add(
			new PreviewTask(
				"Install from Originating Server",
				"This optional feature was not originally installed. You can install it now by connecting to the originating server of the parent.",
				installOptFeatureAction));
		array.add(
			new PreviewTask(
				"Replace With Another Version",
				"This task will disable the current version of the feature and replace it with another version from the list of the currently disabled features.",
				swapVersionAction));
		array.add(
			new PreviewTask(
				"Uninstall",
				"This feature is currently not used and can be uninstalled from the product.",
				uninstallFeatureAction));
		array.add(
			new PreviewTask(
				"Scan for Updates",
				"Search for updates for this feature.",
				findUpdatesAction));
		array.add(
			new PreviewTask(
				"Show Properties",
				"View properties of the feature such as version, provider name, license agreement etc.",
				propertiesAction));
		previewTasks.put(key, array.toArray(new IPreviewTask[array.size()]));
	}

	public IPreviewTask[] getPreviewTasks(Object object) {
		IPreviewTask[] tasks = null;

		if (object instanceof IFeatureAdapter)
			tasks = (IPreviewTask[]) previewTasks.get(IFeatureAdapter.class);
		if (object instanceof ILocalSite)
			tasks = (IPreviewTask[]) previewTasks.get(ILocalSite.class);
		if (object instanceof IConfiguredSiteAdapter)
			tasks =
				(IPreviewTask[]) previewTasks.get(IConfiguredSiteAdapter.class);
		return (tasks != null) ? tasks : new IPreviewTask[0];
	}

	void updateTitle(Object newInput) {
		if (newInput == null
			|| newInput.equals(UpdateUI.getDefault().getUpdateModel())) {
			// restore old
			setTitle(getViewName());
			setTitleToolTip(getTitle());
		} else {
			String name =
				((LabelProvider) treeViewer.getLabelProvider()).getText(
					newInput);
			setTitle(getViewName() + ": " + name); //$NON-NLS-1$
			setTitleToolTip(getTitle());
		}
	}
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public void setFocus() {
	}

}
