/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

/**
 * @since 3.3
 *
 */
public class WidgetAddition extends AdditionBase {

	AbstractWorkbenchWidget widget = null;
	
	public WidgetAddition(IConfigurationElement element, IMenuService service) {
		super(element, service);
	}
	
	AbstractWorkbenchWidget getWidget() {
		if (widget == null) {
			widget = loadWidget();
		}
		return widget;
	}

	/**
	 * @return
	 */
	private AbstractWorkbenchWidget loadWidget() {
		if (widget == null) {
			try {
				widget = (AbstractWorkbenchWidget) element.createExecutableExtension(IWorkbenchRegistryConstants.ATT_CLASS);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return widget;
	}

	private String getClassSpec() {
		return element.getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}
	
	public String toString() {
		return getClass().getName() + "() " + getClassSpec();   //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.AdditionBase#getContributionItem()
	 */
	public IContributionItem getContributionItem() {
		return new ContributionItem(getId()) {
			public void dispose() {}
			public void fill(Composite parent) {}
			public void fill(Menu parent, int index) {}

			public void fill(ToolBar parent, int index) {
				if (getWidget() != null) {
					Composite widgetContainer = new Composite(parent, SWT.NONE);
					getWidget().fill(widgetContainer);
					Point prefSize = getWidget().getPreferredSize();

					ToolItem sepItem = new ToolItem(parent, SWT.SEPARATOR, index);
					sepItem.setControl(widgetContainer);
					sepItem.setWidth(prefSize.x);
				}
			}
		};
	}
}
