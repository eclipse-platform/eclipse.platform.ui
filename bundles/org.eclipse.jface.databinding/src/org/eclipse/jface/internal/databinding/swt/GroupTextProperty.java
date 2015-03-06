/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource Muenchen GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugen Neufeld - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Group;

/**
 * Text Databinding Property for SWT Groups.
 *
 * @author Eugen Neufeld
 */
public class GroupTextProperty extends WidgetStringValueProperty {

	@Override
	String doGetStringValue(Object source) {
		return ((Group) source).getText();
	}

	@Override
	void doSetStringValue(Object source, String value) {
		((Group) source).setText(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "Group.text <String>"; //$NON-NLS-1$
	}
}