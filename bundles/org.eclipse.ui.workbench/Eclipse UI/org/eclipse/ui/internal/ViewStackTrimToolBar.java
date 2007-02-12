/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.internal.layout.TrimToolBarBase;
import org.eclipse.ui.menus.CommandContributionItem;

public class ViewStackTrimToolBar extends TrimToolBarBase {
	private ViewStack viewStack = null;

	public ViewStackTrimToolBar(ViewStack vs, int curSide, WorkbenchWindow wbw) {
		super(vs.getID(), curSide, wbw);
		this.viewStack = vs;
		
		dock(curSide);
	}

	public void initToolBarManager(ToolBarManager mgr) {
		System.out.println("init"); //$NON-NLS-1$
		if (viewStack == null)
			return;

		mgr.setContextMenuManager(new MenuManager());
		MenuManager menuMgr = mgr.getContextMenuManager();

		// Add a 'Show View' command for each visible view
		LayoutPart[] stackParts = viewStack.getChildren();
		for (int i = 0; i < stackParts.length; i++) {
			if (stackParts[i] instanceof ViewPane) {
				ViewPane vp = (ViewPane) stackParts[i];
				IViewReference ref = (IViewReference) vp.getPartReference();
				ImageDescriptor desc = ImageDescriptor.createFromImage(ref.getTitleImage());
				Map params = new HashMap();
				params.put("org.eclipse.ui.views.showView.viewId", ref.getId()); //$NON-NLS-1$
				IContributionItem showCmd = new CommandContributionItem(viewStack.getID()+ref.getId(),
						"org.eclipse.ui.views.showView", params, //$NON-NLS-1$
							desc, null, null, null, null, ref.getTitle(),
							CommandContributionItem.STYLE_CHECK);
				mgr.add(showCmd);

				IContributionItem menuCmd = new CommandContributionItem(viewStack.getID()+ref.getId(),
						"org.eclipse.ui.views.showView", params, //$NON-NLS-1$
							desc, null, null, ref.getTitle(), null, null,
							CommandContributionItem.STYLE_CHECK);
				menuMgr.add(menuCmd);
			}
		}
	}

}
