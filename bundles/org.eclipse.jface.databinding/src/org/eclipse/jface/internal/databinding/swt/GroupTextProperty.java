/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource Muenchen GmbH and others.
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class GroupTextProperty extends WidgetStringValueProperty<Group> {

	@Override
	String doGetStringValue(Group source) {
		return source.getText();
	}

	@Override
	void doSetStringValue(Group source, String value) {
		source.setText(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "Group.text <String>"; //$NON-NLS-1$
	}
}