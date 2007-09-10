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
package org.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Instances of this class provide a {@link WorkingSetConfigurationBlock}
 * wrapped with an SWT Group container.
 * 
 * <strong>Please note that this API is experimental and may change before 3.4
 * ships.</strong>
 * 
 * @since 3.4
 */
public final class WorkingSetGroup {

	private WorkingSetConfigurationBlock workingSetBlock;

	/**
	 * Create a new instance of this class.
	 * 
	 * @param composite
	 *            parent composite
	 * @param currentSelection
	 *            the initial working set selection to pass to the
	 *            {@link WorkingSetConfigurationBlock}
	 * @param workingSetTypes
	 *            the types of working sets that can be selected by the
	 *            {@link WorkingSetConfigurationBlock}
	 */
	public WorkingSetGroup(Composite composite,
			IStructuredSelection currentSelection, String[] workingSetTypes) {
		Group workingSetGroup = new Group(composite, SWT.NONE);
		workingSetGroup.setFont(composite.getFont());
		workingSetGroup
				.setText(WorkbenchMessages.WorkingSetGroup_WorkingSets_group);
		workingSetGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
				false));
		workingSetGroup.setLayout(new GridLayout(1, false));

		workingSetBlock = new WorkingSetConfigurationBlock(workingSetTypes,
				WorkbenchPlugin.getDefault().getDialogSettings());
		workingSetBlock.setWorkingSets(workingSetBlock
				.findApplicableWorkingSets(currentSelection));
		workingSetBlock.createContent(workingSetGroup);
	}

	/**
	 * Return the working sets selected by the contained
	 * {@link WorkingSetConfigurationBlock}.
	 * 
	 * @return the selected working sets
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		return workingSetBlock.getSelectedWorkingSets();
	}
}