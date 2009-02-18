/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.about.IInstallationPageContainer;
import org.eclipse.ui.about.InstallationPage;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Abstract superclass of about dialog installation pages.  The ProductInfoPage
 * is set up so that the page can be hosted as one of many pages in the
 * InstallationDialog, or as the only page in a ProductInfoDialog.
 * When hosted inside a ProductInfoDialog, the page id, source variables,
 * command contributions, and associated visibility/enablement expressions, are
 * different than when hosted as a page in the InstallationDialog.  This is important
 * because another instance of the page may be launched from an InstallationDialog, and
 * this secondary page should not affect the contributions of the originating dialog.
 */

public abstract class ProductInfoPage extends InstallationPage implements
		IShellProvider {

	private IMenuService menuService;

	private InstallationDialog dialog;

	private AbstractContributionFactory factory;

	private IProduct product;

	private String productName;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.about.InstallationPage#init(org.eclipse.ui.services.
	 * IServiceLocator)
	 */
	public void init(IServiceLocator locator) {
		IInstallationPageContainer pageContainer = (IInstallationPageContainer) locator
				.getService(IInstallationPageContainer.class);
		// Must cast to dialog because we rely on the ProductInfoDialog framework
		// to provide us with the appropriate variables and visibility expressions
		dialog = (InstallationDialog)pageContainer;
		menuService = (IMenuService) locator.getService(IMenuService.class);
		// this assumes that the control is created before init
		addButtons();
	}

	protected IMenuService getMenuService() {
		return menuService;
	}

	protected InstallationDialog getInstallationDialog() {
		return dialog;
	}

	protected IProduct getProduct() {
		if (product == null)
			product = Platform.getProduct();
		return product;
	}

	public String getProductName() {
		if (productName == null) {
			if (getProduct() != null) {
				productName = getProduct().getName();
			}
			if (productName == null) {
				productName = WorkbenchMessages.AboutDialog_defaultProductName;
			}
		}
		return productName;
	}

	public void setProductName(String name) {
		productName = name;
	}

	abstract String getId();

	public final void createControl(Composite parent) {
		Control control = createPageControl(parent);
		control.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				releaseContributionFactory();
			}
		});
		setControl(control);
	}
	
	protected abstract Control createPageControl(Composite parent);

	protected Composite createOuterComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		return composite;
	}

	private void addButtons() {
		if (menuService == null)
			return;
		factory = makeContributionFactory();
		if (factory != null)
			menuService.addContributionFactory(factory);
	}

	protected AbstractContributionFactory makeContributionFactory() {
		return null;
	}

	protected void releaseContributionFactory() {
		if (factory != null) {
			menuService.removeContributionFactory(factory);
			factory = null;
		}
	}
	
	protected void copyToClipboard() {
		// Do nothing by default
	}
}
