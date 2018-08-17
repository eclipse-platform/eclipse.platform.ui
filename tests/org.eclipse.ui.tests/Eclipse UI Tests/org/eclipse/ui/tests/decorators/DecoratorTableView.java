/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
package org.eclipse.ui.tests.decorators;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * The DecoratorTableView is a view that tests the decorator support for tables.
 */
public class DecoratorTableView extends DecoratorTestPart {

	TableViewer viewer;

	/**
	 * Create a new instance of the receiver.
	 */
	public DecoratorTableView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent);

		viewer.setLabelProvider(getLabelProvider());

		viewer.setContentProvider(new TestTableContentProvider());
		viewer.setInput(this);

		GridData data = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL | GridData.FILL_BOTH);

		viewer.getControl().setLayoutData(data);

	}

	@Override
	public void setFocus() {
		// XXX Auto-generated method stub

	}



}
