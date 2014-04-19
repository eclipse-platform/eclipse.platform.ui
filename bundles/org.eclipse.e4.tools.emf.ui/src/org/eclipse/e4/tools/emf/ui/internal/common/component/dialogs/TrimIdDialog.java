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
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.model.fragment.MModelFragments;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class TrimIdDialog extends AbstractIdDialog<MTrimContribution, MTrimBar> {

	public TrimIdDialog(Shell parentShell, IModelResource resource, MTrimContribution toolbarContribution, EditingDomain domain, EModelService modelService, Messages Messages) {
		super(parentShell, resource, toolbarContribution, domain, modelService, Messages);
	}

	@Override
	protected String getShellTitle() {
		return messages.TrimBarIdDialog_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return messages.TrimBarIdDialog_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return messages.TrimBarIdDialog_DialogMessage;
	}

	@Override
	protected String getLabelText() {
		return messages.TrimBarIdDialog_Id;
	}

	@Override
	protected List<MTrimBar> getViewerInput() {
		List<MTrimBar> result = new ArrayList<MTrimBar>();

		if (resource.getRoot().get(0) instanceof MApplication) {

			MApplication ma = ((MApplication) resource.getRoot().get(0));

			List<MTrimBar> tbs = modelService.findElements(ma, null, MTrimBar.class, null);
			result.addAll(tbs);

		} else if (resource.getRoot().get(0) instanceof MModelFragments) {

			for (MApplicationElement f : ((MModelFragments) resource.getRoot().get(0)).getImports()) {
				if (f instanceof MTrimBar) {
					result.add((MTrimBar) f);
				}
			}
		}

		return result;
	}

	@Override
	protected EAttribute getFeatureLiteral() {
		return MenuPackageImpl.Literals.TRIM_CONTRIBUTION__PARENT_ID;
	}

	@Override
	protected String getListItemInformation(MTrimBar listItem) {
		return listItem.getSide().getName();
	}

}