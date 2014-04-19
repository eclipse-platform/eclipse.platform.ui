/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT, FHV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial implementation (Bug 396975)
 *     Nicolaj Hoess <nicohoess@gmail.com> - refactoring (Bug 396975)
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class MenuIdDialog extends AbstractIdDialog<MMenuContribution, MMenu> {

	public MenuIdDialog(Shell parentShell, IModelResource resource, MMenuContribution toolbarContribution, EditingDomain domain, EModelService modelService, Messages Messages) {
		super(parentShell, resource, toolbarContribution, domain, modelService, Messages);
	}

	@Override
	protected String getShellTitle() {
		return messages.MenuIdDialog_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return messages.MenuIdDialog_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return messages.MenuIdDialog_DialogMessage;
	}

	@Override
	protected String getLabelText() {
		return messages.MenuIdDialog_Id;
	}

	@Override
	protected List<MMenu> getViewerInput() {

		List<MMenu> result = new ArrayList<MMenu>();

		if (resource.getRoot().get(0) instanceof MApplication) {
			// include Window main-menu instances
			MApplication ma = ((MApplication) resource.getRoot().get(0));
			for (MWindow m : ma.getChildren()) {
				if (m.getMainMenu() != null)
					result.add(m.getMainMenu());
			}
			// include menu elements located within parts
			List<MPart> mp = modelService.findElements(ma, null, MPart.class, null);
			for (MPart mPart : mp) {
				result.addAll(mPart.getMenus());
			}
			// include menu elements carried by tool items
			List<MToolItem> mt = modelService.findElements(ma, null, MToolItem.class, null);
			for (MToolItem mToolItem : mt) {
				if (mToolItem.getMenu() != null)
					result.add(mToolItem.getMenu());
			}

			for (MMenu mMenuEntry : result.toArray(new MMenu[] {})) {
				performRecursiveCheck(mMenuEntry, result);
			}

		} else if (resource.getRoot().get(0) instanceof MModelFragments) {

			for (MApplicationElement f : ((MModelFragments) resource.getRoot().get(0)).getImports()) {
				if (f instanceof MMenu) {
					result.add((MMenu) f);
				}
			}
			viewer.setInput(result);

		}

		return result;
	}

	/**
	 * A menu may contain another {@link MMenu} as a child; we want them to be
	 * shown as an additional entry, so we recursively dive into these elements
	 *
	 * @param mMenu
	 * @param list
	 */
	private void performRecursiveCheck(MMenu mMenu, List<MMenu> list) {
		List<MMenuElement> children = mMenu.getChildren();
		for (MMenuElement child : children) {
			if (child instanceof MMenu) {
				MMenu mMenuChild = (MMenu) child;
				list.add(mMenuChild);
				performRecursiveCheck(mMenuChild, list);
			}
		}
	}

	@Override
	protected EAttribute getFeatureLiteral() {
		return MenuPackageImpl.Literals.MENU_CONTRIBUTION__PARENT_ID;
	}

	@Override
	protected String getListItemInformation(MMenu listItem) {
		return (listItem.getLabel() != null) ? listItem.getLabel() : ""; //$NON-NLS-1$
	}

}