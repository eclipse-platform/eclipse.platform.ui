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

import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * The DecoratorTableTreeView is the view that tests decorators
 * for table trees.
 */
public class DecoratorTableTreeView extends DecoratorTestPart {

	TableTreeViewer viewer;

	/**
	 * Create a new instance of the receiver.
	 */
	public DecoratorTableTreeView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableTreeViewer(parent);

		viewer.setLabelProvider(getLabelProvider());

		viewer.setContentProvider(new TestTreeContentProvider());
		viewer.setInput(this);

		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
				| GridData.FILL_BOTH);

		viewer.getControl().setLayoutData(data);

	}

	@Override
	public void setFocus() {
		// XXX Auto-generated method stub

	}

}
