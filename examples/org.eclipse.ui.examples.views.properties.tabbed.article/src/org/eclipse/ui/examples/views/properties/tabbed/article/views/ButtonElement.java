/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * This class represents a Button Element in the Sample View
 *
 * TIP: By implementing the <code>IWorkbenchAdapter</code> interface, we can
 * easily add objects of this type to viewers and parts in the workbench. When a
 * viewer contains <code>IWorkbenchAdapter</code>, the generic
 * <code>WorkbenchContentProvider</code> and
 * <code>WorkbenchLabelProvider</code> can be used to provide navigation and
 * display for that viewer.
 */
public class ButtonElement
	implements IWorkbenchAdapter, IAdaptable {

	private final String headingName;

	private final Button ctl;

	/**
	 * Creates a new ButtonElement.
	 *
	 * @param initBtn
	 *            the control of this element
	 * @param heading
	 *            text corresponding to the heading
	 */
	public ButtonElement(Button initBtn, String heading) {
		this.headingName = heading;
		this.ctl = initBtn;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IWorkbenchAdapter.class)
			return adapter.cast(this);
		if (adapter == IPropertySource.class)
			return adapter.cast(new ButtonElementProperties(this));
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	@Override
	public String getLabel(Object o) {
		return headingName;
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	/**
	 * Retrieve the control for this element.
	 *
	 * @return the control for this element.
	 */
	public Button getControl() {
		return ctl;
	}

	@Override
	public Object[] getChildren(Object o) {
		return null;
	}

}
