/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.util.List;
import javax.inject.Inject;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.workbench.ui.renderers.swt.StackRenderer;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ViewReference;
import org.eclipse.ui.internal.WorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

public class CompatibilityView extends CompatibilityPart {

	private ViewReference reference;

	@Inject
	CompatibilityView(MPart part, ViewReference ref) {
		super(part);
		reference = ref;
	}

	public IViewPart getView() {
		return (IViewPart) getPart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#getReference()
	 */
	@Override
	public WorkbenchPartReference getReference() {
		return reference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.e4.compatibility.CompatibilityPart#createPartControl
	 * (org.eclipse.ui.IWorkbenchPart, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createPartControl(IWorkbenchPart legacyPart, Composite parent) {
		// Some views (i.e. Console) require that the actual ToolBar be
		// instantiated before they are
		ToolBarManager tbm = (ToolBarManager) ((ViewPart) legacyPart).getViewSite().getActionBars()
				.getToolBarManager();
		ToolBar tb = tbm.createControl(parent);

		super.createPartControl(legacyPart, parent);

		// dispose the tb, it will be re-created when the tab is shown
		tb.dispose();

		MenuManager mm = (MenuManager) ((ViewPart) legacyPart).getViewSite().getActionBars()
				.getMenuManager();
		if (mm.getItems().length > 0) {
			Control partCtrl = (Control) part.getWidget();
			partCtrl.setData("legacyMM", mm); //$NON-NLS-1$
			List<MMenu> menus = part.getMenus();
			if (menus.size() == 0) {
				MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();

				// HACK!! Identifies this to the menu renderer
				menu.getTags().add("LegacyMenu"); //$NON-NLS-1$
				menu.getTags().add(StackRenderer.TAG_VIEW_MENU);
				menus.add(menu);
			}
		}

		Control partCtrl = (Control) part.getWidget();
		partCtrl.setData("legacyTBM", tbm); //$NON-NLS-1$
		// Construct the toolbar (if necessary)
		if (tbm.getItems().length > 0) {
			MToolBar toolbar = part.getToolbar();
			if (toolbar == null) {
				toolbar = MenuFactoryImpl.eINSTANCE.createToolBar();

				// HACK!! Identifies this to the TB renderer
				toolbar.getTags().add("LegacyTB"); //$NON-NLS-1$
				part.setToolbar(toolbar);
			}
		}
	}
}
