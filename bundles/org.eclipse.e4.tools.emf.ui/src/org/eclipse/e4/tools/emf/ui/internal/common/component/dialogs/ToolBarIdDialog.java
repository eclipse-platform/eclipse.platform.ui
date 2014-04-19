/*******************************************************************************
 * Copyright (c) 2013
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolaj Hoess <nicohoess@gmail.com> - initial implementation (Bug 396975)
 *     Andrej Brummelhuis <andrejbrummelhuis@gmail.com> - initial implementation (Bug 396975)
 *     Adrian Alcaide <adrian4912@gmail.com> - initial implementation (Bug 396975)
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class ToolBarIdDialog extends AbstractIdDialog<MToolBarContribution, MToolBar> {

	public ToolBarIdDialog(Shell parentShell, IModelResource resource, MToolBarContribution toolbarContribution, EditingDomain domain, EModelService modelService, Messages Messages) {
		super(parentShell, resource, toolbarContribution, domain, modelService, Messages);
	}

	@Override
	protected String getShellTitle() {
		return messages.ToolBarIdDialog_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return messages.ToolBarIdDialog_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return messages.ToolBarIdDialog_DialogMessage;
	}

	@Override
	protected String getLabelText() {
		return messages.ToolBarIdDialog_Id;
	}

	@Override
	protected List<MToolBar> getViewerInput() {
		List<MToolBar> result = new ArrayList<MToolBar>();
		if (resource.getRoot().get(0) instanceof MApplication) {

			MApplication ma = ((MApplication) resource.getRoot().get(0));

			List<MToolBar> tbs = modelService.findElements(ma, null, MToolBar.class, null);
			result.addAll(tbs);

		} else if (resource.getRoot().get(0) instanceof MModelFragments) {

			for (MApplicationElement f : ((MModelFragments) resource.getRoot().get(0)).getImports()) {
				if (f instanceof MToolBar) {
					result.add((MToolBar) f);
				}
			}

		}
		return result;
	}

	@Override
	protected EAttribute getFeatureLiteral() {
		return MenuPackageImpl.Literals.TOOL_BAR_CONTRIBUTION__PARENT_ID;
	}

	@Override
	protected String getListItemInformation(MToolBar listItem) {
		return getAdditionalInfoForToolBar(listItem);
	}

	/**
	 * Returns additional information for the given {@link MToolBar}. At the
	 * moment the position of the parent {@link MTrimBar} is returned.
	 *
	 * TODO Check which information is most relevant for the user
	 *
	 * @param toolBar
	 * @return String
	 */
	private String getAdditionalInfoForToolBar(MToolBar toolBar) {
		MElementContainer<?> container = toolBar.getParent();
		if (container == null) {
			// If the MToolBar is imported into a fragment getParent() will
			// return null
			return "imported"; //$NON-NLS-1$
		}
		while (container instanceof MTrimBar == false) {
			container = container.getParent();
		}
		return ((MTrimBar) container).getSide().getName();
	}
}