/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import org.eclipse.core.expressions.Expression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.about.InstallationPage;

/**
 * Abstract superclass of the individual about dialogs that appear outside of
 * the InstallationDialog These dialogs contain a single installation page, and
 * scope the page to something more specific than it would be in the standard
 * installation dialog.
 * 
 * It is important that the visibility and enablement expressions of
 * contributions to this dialog, and the source variables that drive them, do
 * not conflict with those used inside the normal InstallationDialog. Otherwise,
 * the button manager of the InstallationDialog will be affected by changes in
 * the launched dialog. Where commands have enablement expressions in this
 * dialog, we use a unique command id so that there are no handler conflicts
 * with the regular dialog.
 */

public abstract class ProductInfoDialog extends InstallationDialog {

	ProductInfoPage page;
	String title;
	String helpContextId;
	Object previouslyActivePage;
	Object previouslyActiveSelection;

	protected ProductInfoDialog(Shell shell) {
		super(shell, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		// capture the previous variables values.  This is used for
		// nested product info dialogs, such as a feature dialog that opens a
		// plugins dialog
		previouslyActivePage = getSourceProvider().getCurrentState().get(
				InstallationDialogSourceProvider.ACTIVE_PRODUCT_DIALOG_PAGE);
		previouslyActiveSelection = getSourceProvider()
				.getCurrentState()
				.get(
						InstallationDialogSourceProvider.ACTIVE_PRODUCT_DIALOG_PAGE_SELECTION);

	}

	public void initializeDialog(ProductInfoPage page, String title,
			String helpContextId) {
		this.page = page;
		this.title = title;
		this.helpContextId = helpContextId;
	}

	protected void createFolderItems(TabFolder folder) {
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(title);
		Composite control = new Composite(folder, SWT.BORDER);
		control.setLayout(new GridLayout());
		item.setControl(control);
		page.createControl(control);
		// must set up the page data before creating the service locator.
		// The id is used by the dialog service locator
		item.setData(page);
		item.setData(ID, page.getId());
		page.init(getDialogServiceLocator());
		item.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				page.dispose();
			}
		});
		control.layout(true, true);
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
		newShell.setText(title);
		if (helpContextId != null)
			PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
					helpContextId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.about.IInstallationPageContainer#getButtonBarURI()
	 */
	public String getButtonBarURI() {
		return super.getButtonBarURI() + "." + page.getId(); //$NON-NLS-1$
	}

	protected void updateContributions(String id, InstallationPage page) {
		InstallationDialogSourceProvider sp = getSourceProvider();
		if (sp != null) {
			sp.setProductDialogPage((ProductInfoPage) page);
		}
		getButtonManager().update(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.about.IInstallationPageContainer#getActivePageExpression
	 * (org.eclipse.ui.about.InstallationPage)
	 */
	public Expression getActivePageExpression(InstallationPage page) {
		// We have our own menu URI and do not share with other pages, so
		// there is no visibility expression needed.
		return null;
	}

	protected void resetVariables(InstallationDialogSourceProvider sp) {
		sp.setProductDialogPage(previouslyActivePage);
		sp.setProductDialogPageSelection(previouslyActiveSelection);
	}

}
