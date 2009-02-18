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

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.about.ActiveInstallationPageExpression;
import org.eclipse.ui.about.IInstallationPageContainer;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.internal.ConfigurationInfo;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * @since 3.5
 * 
 */
public class InstallationDialog extends Dialog implements
		IInstallationPageContainer {
	class ButtonManager extends ContributionManager {

		private Composite composite;

		public ButtonManager(Composite composite) {
			this.composite = composite;
		}

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
			int widthHint = Dialog.convertHorizontalDLUsToPixels(metrics,
					IDialogConstants.BUTTON_WIDTH);
			Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			data.widthHint = Math.max(widthHint, minSize.x);
			button.setLayoutData(data);
		}

	}

	protected static final String ID = "ID"; //$NON-NLS-1$
	private static final String DIALOG_SETTINGS_SECTION = "InstallationDialogSettings"; //$NON-NLS-1$
	private static final String URI = "toolbar:org.eclipse.ui.installationDialog.buttonbar"; //$NON-NLS-1$
	private static String lastSelectedTabId = null;
	private TabFolder folder;
	private IServiceLocator serviceLocator;
	private ButtonManager buttonManager;
	private InstallationDialogSourceProvider sourceProvider = null;

	/**
	 * @param parentShell
	 * @param locator
	 */
	public InstallationDialog(Shell parentShell, IServiceLocator locator) {
		super(parentShell);
		IServiceLocatorCreator slc = (IServiceLocatorCreator) locator
				.getService(IServiceLocatorCreator.class);
		createDialogServiceLocator(slc, locator);
		ISourceProviderService sps = (ISourceProviderService) serviceLocator
				.getService(ISourceProviderService.class);
		sourceProvider = (InstallationDialogSourceProvider) sps
				.getSourceProvider(InstallationDialogSourceProvider.ACTIVE_PRODUCT_DIALOG_PAGE);

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
		String productName = ""; //$NON-NLS-1$
		IProduct product = Platform.getProduct();
		if (product != null && product.getName() != null)
			productName = product.getName();
		newShell.setText(NLS.bind(
				WorkbenchMessages.InstallationDialog_ShellTitle, productName));
		newShell.setSize(600, 768);
	}

	/*
	 * (non-Javadoc)
	 * 
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

		folder = new TabFolder(composite, SWT.NONE);
		configureFolder();
		createFolderItems(folder);

		GridData folderData = new GridData(SWT.FILL, SWT.FILL, true, true);
		folderData.widthHint = SWT.DEFAULT;
		folderData.heightHint = SWT.DEFAULT;
		folder.setLayoutData(folderData);
		folder.addSelectionListener(createFolderSelectionListener());
		folder.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				releaseContributions();
				resetVariables(sourceProvider);
			}
		});
		return composite;
	}

	protected void createFolderItems(TabFolder folder) {
		IConfigurationElement[] elements = ConfigurationInfo
				.getSortedExtensions(loadElements());
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			TabItem item = new TabItem(folder, SWT.NONE);
			item.setText(element
					.getAttribute(IWorkbenchRegistryConstants.ATT_NAME));
			item.setData(element);
			item.setData(ID, element
					.getAttribute(IWorkbenchRegistryConstants.ATT_ID));

			Composite control = new Composite(folder, SWT.NONE);
			control.setLayout(new GridLayout());
			item.setControl(control);
		}
	}

	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		boolean selected = false;
		if (folder.getItemCount() > 0) {
			if (lastSelectedTabId != null) {
				TabItem[] items = folder.getItems();
				for (int i = 0; i < items.length; i++)
					if (items[i].getData(ID).equals(lastSelectedTabId)) {
						folder.setSelection(i);
						tabSelected(items[i]);
						selected = true;
						break;
					}
			}
			if (!selected)
				tabSelected(folder.getItem(0));
		}
		return control;
	}

	private SelectionAdapter createFolderSelectionListener() {
		return new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				tabSelected((TabItem) e.item);
			}
		};
	}

	/*
	 * Must be called after contributions and button manager are created.
	 */
	private void tabSelected(TabItem item) {
		if (item.getData() instanceof IConfigurationElement) {
			final IConfigurationElement element = (IConfigurationElement) item
					.getData();

			Composite pageComposite = (Composite) item.getControl();
			try {
				final InstallationPage page = (InstallationPage) element
						.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
				page.createControl(pageComposite);
				page.init(serviceLocator);
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

		}
		String id = (String) item.getData(ID);
		rememberSelectedTab(id);
		updateContributions(id, (InstallationPage) item.getData());
		Button button = createButton(buttonManager.getParent(), IDialogConstants.CLOSE_ID,
				IDialogConstants.CLOSE_LABEL, true);
		GridData gd = (GridData)button.getLayoutData();
		gd.horizontalAlignment = SWT.BEGINNING;
		gd.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH) / 2;
		// Layout the button manager again now that the OK button is there.
		// Must do this before we layout the button bar or else the button
		// manager will not know about the OK button
		buttonManager.getParent().layout();
		// Now we layout the button bar itself so it will accommodate the
		// button manager's buttons
		getButtonBar().getParent().layout();
	}

	private void rememberSelectedTab(String pageId) {
		lastSelectedTabId = pageId;
	}

	protected void updateContributions(String id, InstallationPage page) {
		// Changing the source provider will cause the contributions and the
		// button manager to be updated. If for some reason we don't have a
		// source provider, update it ourselves.
		if (sourceProvider != null)
			sourceProvider.setCurrentPage(id, page);
		else
			buttonManager.update(true);
	}

	protected InstallationDialogSourceProvider getSourceProvider() {
		return sourceProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// The button manager will handle the correct sizing of the buttons.
		// We do not want columns equal width because we are going to add some
		// padding in the final column (close button).
		GridLayout layout = (GridLayout)parent.getLayout();
		layout.makeColumnsEqualWidth = false;
		buttonManager = new ButtonManager(parent);
		((IMenuService) serviceLocator.getService(IMenuService.class))
				.populateContributionManager(buttonManager, getButtonBarURI());
	}

	private void configureFolder() {
	}

	private IConfigurationElement[] loadElements() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui", "installationPages"); //$NON-NLS-1$ //$NON-NLS-2$
		return point.getConfigurationElements();
	}

	protected void createDialogServiceLocator(IServiceLocatorCreator slc,
			IServiceLocator locator) {
		AbstractServiceFactory localFactory = new AbstractServiceFactory() {
			public Object create(Class serviceInterface,
					IServiceLocator parentLocator, IServiceLocator locator) {
				if (serviceInterface == IInstallationPageContainer.class) {
					return InstallationDialog.this;
				}
				return parentLocator.getService(serviceInterface);
			}
		};
		this.serviceLocator = slc.createServiceLocator(locator, localFactory,
				new IDisposable() {
					public void dispose() {
						close();
					}
				});
	}

	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = WorkbenchPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		return section;
	}

	protected void releaseContributions() {
		((IMenuService) serviceLocator.getService(IMenuService.class))
				.releaseContributions(buttonManager);
		buttonManager.removeAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.about.IInstallationPageContainer#getButtonBarURI()
	 */
	public String getButtonBarURI() {
		return URI;
	}

	public void closeContainer() {
		close();
	}

	protected IServiceLocator getDialogServiceLocator() {
		return serviceLocator;
	}

	protected ButtonManager getButtonManager() {
		return buttonManager;
	}

   /*
    * Used by ProductInfoPages to obtain the correct active page
    * expression.
    */
	public Expression getActivePageExpression(InstallationPage page) {
		return new ActiveInstallationPageExpression(page);
	}

	protected void resetVariables(InstallationDialogSourceProvider sp) {
		sp.resetAll();
	}
	
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CLOSE_ID == buttonId) {
			okPressed();
		}
	}
}
