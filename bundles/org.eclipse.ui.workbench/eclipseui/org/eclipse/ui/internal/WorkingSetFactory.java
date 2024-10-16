/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * A WorkingSetFactory is used to recreate a persisted WorkingSet object.
 */
public class WorkingSetFactory implements IElementFactory {

	@Override
	public IAdaptable createElement(IMemento memento) {
		String workingSetName = memento.getString(IWorkbenchConstants.TAG_NAME);
		String label = memento.getString(IWorkbenchConstants.TAG_LABEL);
		if (label == null) {
			label = workingSetName;
		}
		String workingSetEditPageId = memento.getString(IWorkbenchConstants.TAG_EDIT_PAGE_ID);
		String aggregateString = memento.getString(AbstractWorkingSet.TAG_AGGREGATE);
		boolean isAggregate = aggregateString != null && Boolean.parseBoolean(aggregateString);

		if (workingSetName == null) {
			return null;
		}

		AbstractWorkingSet workingSet = null;

		if (isAggregate) {
			workingSet = new AggregateWorkingSet(workingSetName, label, memento);
		} else {
			workingSet = new WorkingSet(workingSetName, label, memento);
		}

		if (workingSetEditPageId != null) {
			workingSet.setId(workingSetEditPageId);
		} else if (!isAggregate) {
			// working sets created with builds 20020418 and 20020419 will not
			// have an edit page id. fix this automatically.
			workingSet.setId("org.eclipse.ui.resourceWorkingSetPage"); //$NON-NLS-1$
		}
		return workingSet;
	}
}
