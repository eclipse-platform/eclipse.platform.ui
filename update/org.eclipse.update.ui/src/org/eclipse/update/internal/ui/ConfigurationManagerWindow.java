/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.views.*;

/**
 * Configuration Manager window.
 */
public class ConfigurationManagerWindow
	extends ApplicationWindow
	{
	private ConfigurationView view;
	private GlobalAction propertiesAction;
	private IAction propertiesActionHandler;
	
	class GlobalAction extends Action implements IPropertyChangeListener {
		private IAction handler;

		public GlobalAction() {
		}

		public void setActionHandler(IAction action) {
			if (handler != null) {
				handler.removePropertyChangeListener(this);
				handler = null;
			}
			if (action != null) {
				this.handler = action;
				action.addPropertyChangeListener(this);
			}
			if (handler != null) {
				setEnabled(handler.isEnabled());
				setChecked(handler.isChecked());
			}
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(Action.ENABLED)) {
				Boolean bool = (Boolean) event.getNewValue();
				setEnabled(bool.booleanValue());
			} else if (event.getProperty().equals(Action.CHECKED)) {
				Boolean bool = (Boolean) event.getNewValue();
				setChecked(bool.booleanValue());
			}
		}

		public void run() {
			if (handler != null)
				handler.run();
		}
	}

	/**
	 * @param parentShell
	 */
	public ConfigurationManagerWindow(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
		// Setup window.
		addMenuBar();
		addActions();
		addToolBar(SWT.FLAT);
//		addStatusLine();
	}

	private void addActions() {
		IMenuManager menuBar = getMenuBarManager();
		IMenuManager fileMenu = new MenuManager(UpdateUI.getString("ConfigurationManagerWindow.fileMenu")); //$NON-NLS-1$
		menuBar.add(fileMenu);

		propertiesAction = new GlobalAction();
		propertiesAction.setText(UpdateUI.getString("ConfigurationManagerWindow.properties")); //$NON-NLS-1$
		propertiesAction.setEnabled(false);

		fileMenu.add(propertiesAction);
		fileMenu.add(new Separator());

		Action closeAction = new Action() {
			public void run() {
				close();
			}
		};
		closeAction.setText(UpdateUI.getString("ConfigurationManagerWindow.close")); //$NON-NLS-1$
		fileMenu.add(closeAction);
	}
	
	private void hookGlobalActions() {
		if(propertiesActionHandler!=null)
			propertiesAction.setActionHandler(propertiesActionHandler);
	}
	
	protected Control createContents(Composite parent) {
		view = new ConfigurationView(this);
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		container.setLayout(layout);

		GridData gd;
		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.heightHint = 1;
		separator.setLayoutData(gd);

		view.createPartControl(container);
		Control viewControl = view.getControl();
		gd = new GridData(GridData.FILL_BOTH);
		viewControl.setLayoutData(gd);
		hookGlobalActions();

		updateActionBars();

		try {
			ILocalSite localSite = SiteManager.getLocalSite();
			view.getTreeViewer().setSelection(new StructuredSelection(localSite));
		}
		catch (CoreException e) {
		}
		
		UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
		getShell().setImage(provider.get(UpdateUIImages.DESC_CONFIGS_VIEW, 0));
		
		return container;
	}

	private void updateActionBars() {
		getMenuBarManager().updateAll(false);
		getToolBarManager().update(false);
//		getStatusLineManager().update(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		if (view != null)
			view.dispose();
		return super.close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		// set the title
		getShell().setText(UpdateUI.getString("ConfigurationManagerAction.title")); //$NON-NLS-1$
		getShell().setSize(800, 600);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		// update action bars
		updateActionBars();
		return super.open();
	}
	
	public void setPropertiesActionHandler(IAction handler){
		propertiesActionHandler=handler;
	}
}
