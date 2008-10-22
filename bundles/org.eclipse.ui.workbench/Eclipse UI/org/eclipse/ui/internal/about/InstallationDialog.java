/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.about;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.about.IInstallationPageContainer;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.internal.IWorkbenchThemeConstants;
import org.eclipse.ui.internal.menus.InternalMenuService;
import org.eclipse.ui.internal.menus.SlaveMenuService;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.5
 * 
 */
public class InstallationDialog extends Dialog {
	private static final String ID = "ID"; //$NON-NLS-1$

	private CTabFolder folder;
	private IServiceLocator serviceLocator;
	private ToolBarManager toolbarManager;
	private ButtonManager buttonManager;
	private IMenuService menuService = null;

	/**
	 * @param parentShell
	 */
	protected InstallationDialog(Shell parentShell, IServiceLocator locator) {
		super(parentShell);
		this.serviceLocator = locator;
		menuService = new SlaveMenuService((InternalMenuService) serviceLocator
				.getService(IMenuService.class), serviceLocator, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets
	 * .Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		// TODO
		// This should use the title of the about dialog, this
		// is a temporary hack so that p2 can launch the dialog
		// with a proper title until this is actually hooked into
		// the about dialog.
		newShell.setText("Installation Information"); //$NON-NLS-1$
		newShell.setSize(600, 768);

	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		createToolbar(composite);

		folder = new CTabFolder(composite, SWT.MULTI | SWT.BORDER);
		configureFolder();
		GridData folderData = new GridData(SWT.FILL, SWT.FILL, true, true);
		folderData.widthHint = SWT.DEFAULT;
		folderData.heightHint = SWT.DEFAULT;
		folder.setLayoutData(folderData);

		IConfigurationElement[] elements = loadElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			CTabItem item = new CTabItem(folder, SWT.NONE);
			item.setText(element
					.getAttribute(IWorkbenchRegistryConstants.ATT_NAME));
			item.setData(element);
			Composite control = new Composite(folder, SWT.BORDER);
			control.setLayout(new GridLayout());
			item.setControl(control);

		}
		folder.addSelectionListener(createFolderSelectionListener());
		return composite;
	}

	private SelectionAdapter createFolderSelectionListener() {
		return new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				final CTabItem item = (CTabItem) e.item;
				String id = null;
				if (item.getData() instanceof IConfigurationElement) {
					final IConfigurationElement element = (IConfigurationElement) item
							.getData();

					id = element
							.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
					item.setData(ID, id);

					Composite pageComposite = (Composite) item.getControl();
					try {
						final InstallationPage page = (InstallationPage) element
								.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
						page.createControl(pageComposite);
						page.init(createDialogServiceLocator(item));
						item.setData(page);
						item.addDisposeListener(new DisposeListener() {

							public void widgetDisposed(DisposeEvent e) {
								page.dispose();
							}
						});
						pageComposite.layout(true, true);

					} catch (CoreException e1) {
						Label label = new Label(pageComposite, SWT.NONE);
						label.setText(e1.getMessage());
						item.setData(null);
					}

					
				} else {
					id = (String) item.getData(ID);
				}
				
				menuService.releaseContributions(toolbarManager);
				menuService.populateContributionManager(toolbarManager,
						InstallationDialog.this.getToolbarURI(id));
				toolbarManager.update(true);

				menuService.releaseContributions(buttonManager);
				menuService.populateContributionManager(buttonManager,
						InstallationDialog.this.getButtonBarURI(id));
				buttonManager.update(true);
				createButton(buttonManager.getParent(), IDialogConstants.OK_ID,
						IDialogConstants.OK_LABEL, true);
				// Layout the button manager again now that the OK button is there.
				// Must do this before we layout the button bar or else the button
				// manager will not know about the OK button
				buttonManager.getParent().layout();
				// Now we layout the button bar itself so it will accommodate the
				// button manager's buttons
				getButtonBar().getParent().layout();
			}
		};
	}

	private void createToolbar(Composite composite) {
		toolbarManager = new ToolBarManager(SWT.NONE);
		toolbarManager.createControl(composite);
		ToolBar toolbar = toolbarManager.getControl();
		{
			GridData toolbarData = new GridData(SWT.FILL, SWT.FILL, true, false);
			toolbarData.widthHint = SWT.DEFAULT;
			toolbarData.heightHint = SWT.DEFAULT;
			toolbar.setLayoutData(toolbarData);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		buttonManager = new ButtonManager(parent);
	}

	/**
	 * @param folder2
	 */
	private void configureFolder() {
		ColorRegistry reg = JFaceResources.getColorRegistry();
		Color c1 = reg.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START), c2 = reg
				.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END);
		folder.setSelectionBackground(new Color[] { c1, c2 }, new int[] { 50 },
				true);
		folder.setSelectionForeground(reg
				.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR));
		folder.setSimple(PlatformUI.getPreferenceStore().getBoolean(
				IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS));

	}

	/**
	 * @return
	 */
	private IConfigurationElement[] loadElements() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui", "installationPages"); //$NON-NLS-1$ //$NON-NLS-2$
		return point.getConfigurationElements();
	}

	private String getButtonBarURI(String id) {
		return "toolbar:org.eclipse.ui.installationDialog.buttonbar/" + id; //$NON-NLS-1$
	}

	protected String getToolbarURI(String id) {
		return "toolbar:org.eclipse.ui.installationDialog/" + id; //$NON-NLS-1$
	}

	private IServiceLocator createDialogServiceLocator(final CTabItem item) {
		return new IServiceLocator() {

			public Object getService(Class api) {
				if (api == IInstallationPageContainer.class)
					return new IInstallationPageContainer() {

						public String getButtonBarURI() {
							return InstallationDialog.this
									.getButtonBarURI((String) item.getData(ID));
						}

						public String getToolbarURI() {
							return InstallationDialog.this
									.getToolbarURI((String) item.getData(ID));
						}

						public void updateMessage() {
							// TBD

						}
						
						public void close() {
							InstallationDialog.this.close();
						}
					};
				else if (api == IMenuService.class) {
					return menuService;
				}
				return serviceLocator.getService(api);
			}

			public boolean hasService(Class api) {
				if (api == IInstallationPageContainer.class)
					return true;
				return serviceLocator.hasService(api);
			}
		};
	}
}

class ButtonManager extends ContributionManager {

	private Composite composite;

	/**
	 * 
	 */
	public ButtonManager(Composite composite) {
		this.composite = composite;
	}

	/**
	 * @return
	 */
	public Composite getParent() {
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	public void update(boolean force) {
		if (composite == null || composite.isDisposed())
			return;
		GC metricsGC = new GC(composite);
		FontMetrics metrics = metricsGC.getFontMetrics();
		metricsGC.dispose();
		IContributionItem[] items = getItems();
		Control[] children = composite.getChildren();

		int visibleChildren = 0;
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			control.dispose();
		}

		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if (item.isVisible()) {
				item.fill(composite);
				children = composite.getChildren();
				Control itemControl = children[children.length - 1];
				setButtonLayoutData(metrics, itemControl);
				visibleChildren++;
			}
		}
		GridLayout compositeLayout = (GridLayout) composite.getLayout();
		compositeLayout.numColumns = visibleChildren;
		composite.layout(true);
	}
	
	protected void setButtonLayoutData(FontMetrics metrics, Control button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = Dialog.convertHorizontalDLUsToPixels(metrics, IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
	}
}
