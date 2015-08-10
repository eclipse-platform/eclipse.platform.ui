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
package org.eclipse.ui.tests.views.properties.tabbed.override;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;

/**
 * The label provider for the override tests view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int index) {
		return getImage(obj);
	}

	@Override
	public String getColumnText(Object obj, int index) {
		return getText(obj);
	}

	@Override
	public Image getImage(Object object) {
		if (object instanceof Element) {
			Element element = (Element) object;
			return element.getImage();
		}
		return super.getImage(object);
	}

	@Override
	public String getText(Object object) {
		if (object instanceof Element) {
			Element element = (Element) object;
			return element.getName();
		}
		return super.getText(object);
	}
}