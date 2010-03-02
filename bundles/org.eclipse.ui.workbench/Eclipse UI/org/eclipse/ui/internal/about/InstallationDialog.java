/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.about;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
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
import org.eclipse.ui.about.IInstallationPageContainer;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.internal.ConfigurationInfo;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.5
 * 
 */
public class InstallationDialog extends TrayDialog implements
		IInstallationPageContainer {
	class ButtonManager {

		private Composite composite;
		HashMap buttonMap = new HashMap(); // page id->Collection of page

		// buttons

		public ButtonManager(Composite composite) {
			this.composite = composite;
		}

		public Composite getParent() {
			return composite;
		}

		public void update(String currentPageId) {
			if (composite == null || composite.isDisposed())
				return;
			GC metricsGC = new GC(composite);
			FontMetrics metrics = metricsGC.getFontMetrics();
			metricsGC.dispose();
			List buttons = (List) buttonMap.get(currentPageId);
			Control[] children = composite.getChildren();

			int visibleChildren = 0;
			Button closeButton = getButton(IDialogConstants.CLOSE_ID);

			for (int i = 0; i < children.length; i++) {
				Control control = children[i];
				if (closeButton == control)
					closeButton.dispose();
				else {
					control.setVisible(false);
					setButtonLayoutData(metrics, control, false);
				}
			}
			if (buttons != null) {
				for (int i = 0; i < buttons.size(); i++) {
					Button button = (Button) buttons.get(i);
					button.setVisible(true);
					setButtonLayoutData(metrics, button, true);
					GridData data = (GridData) button.getLayoutData();
					data.exclude = false;
					visibleChildren++;
				}
			}

			GridLayout compositeLayout = (GridLayout) composite.getLayout();
			compositeLayout.numColumns = visibleChildren;
			composite.layout(true);
		}

		protected void setButtonLayoutData(FontMetrics metrics, Control button,
				boolean visible) {
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			int widthHint = Dialog.convertHorizontalDLUsToPixels(metrics,
					IDialogConstants.BUTTON_WIDTH);
			Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			data.widthHint = Math.max(widthHint, minSize.x);
			data.exclude = !visible;
			button.setLayoutData(data);
		}

		public void addPageButton(String id, Button button) {
			List list = (List) buttonMap.get(id);
			if (list == null) {
				list = new ArrayList(1);
				buttonMap.put(id, list);
			}
			list.add(button);
		}

		public void clear() {
			buttonMap = new HashMap();
		}
	}

	protected static final String ID = "ID"; //$NON-NLS-1$
	private static final String DIALOG_SETTINGS_SECTION = "InstallationDialogSettings"; //$NON-NLS-1$
	private final static int TAB_WIDTH_IN_DLUS = 440;
	private final static int TAB_HEIGHT_IN_DLUS = 320;

	private static String lastSelectedTabId = null;
	private TabFolder folder;
	IServiceLocator serviceLocator;
	private ButtonManager buttonManager;
	private Map pageToId = new HashMap();
	private Dialog modalParent;

	/**
	 * @param parentShell
	 * @param locator
	 */
	public InstallationDialog(Shell parentShell, IServiceLocator locator) {
		super(parentShell);
		serviceLocator = locator;

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
		folderData.widthHint = convertHorizontalDLUsToPixels(TAB_WIDTH_IN_DLUS);
		folderData.heightHint = convertVerticalDLUsToPixels(TAB_HEIGHT_IN_DLUS);
		folder.setLayoutData(folderData);
		folder.addSelectionListener(createFolderSelectionListener());
		folder.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				releaseContributions();
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
		// need to reapply the dialog font now that we've created new
		// tab items
		Dialog.applyDialogFont(folder);
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
				// new controls created since the dialog font was applied, so
				// apply again.
				Dialog.applyDialogFont(pageComposite);
				page.setPageContainer(this);
				// Must be done before creating the buttons because the control
				// button creation methods
				// use this map.
				pageToId.put(page, element
						.getAttribute(IWorkbenchRegistryConstants.ATT_ID));
				createButtons(page);
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
		buttonManager.update(id);
		Button button = createButton(buttonManager.getParent(),
				IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
		GridData gd = (GridData) button.getLayoutData();
		gd.horizontalAlignment = SWT.BEGINNING;
		gd.horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH) / 2;
		// Layout the button bar's parent and all of its children.  We must
		// cascade through all children because the buttons have changed and
		// because tray dialog inserts an extra composite in the button bar
		// hierarchy.
		getButtonBar().getParent().layout(true, true);

	}

	protected void createButtons(InstallationPage page) {
		page.createPageButtons(buttonManager.getParent());
		Dialog.applyDialogFont(buttonManager.getParent());
	}

	private void rememberSelectedTab(String pageId) {
		lastSelectedTabId = pageId;
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
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.makeColumnsEqualWidth = false;
		buttonManager = new ButtonManager(parent);
	}

	private void configureFolder() {
	}

	private IConfigurationElement[] loadElements() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui", "installationPages"); //$NON-NLS-1$ //$NON-NLS-2$
		return point.getConfigurationElements();
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
		buttonManager.clear();
	}

	public void closeModalContainers() {
		close();
		if (modalParent != null)
			modalParent.close();
	}

	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CLOSE_ID == buttonId) {
			okPressed();
		}
	}

	public void registerPageButton(InstallationPage page, Button button) {
		buttonManager.addPageButton(pageToId(page), button);
	}

	protected String pageToId(InstallationPage page) {
		String pageId = (String) pageToId.get(page);
		Assert.isLegal(pageId != null);
		return pageId;
	}

	/**
	 * Set the modal parent dialog that was used to launch this dialog. This
	 * should be used by any launching dialog so that the {
	 * {@link #closeModalContainers()} method can be properly implemented.
	 * 
	 * @param parent
	 *            the modal parent dialog that launched this dialog, or
	 *            <code>null</code> if there was no parent.
	 * 
	 *            This is an internal method and should not be used outside of
	 *            platform UI.
	 */
	public void setModalParent(Dialog parent) {
		this.modalParent = parent;
	}
}
