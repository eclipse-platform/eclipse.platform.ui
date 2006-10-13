/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.util.*;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.team.ui.mapping.ICompareInputChangeEvent;

/**
 * An implementation of {@link ICompareInputChangeEvent}.
 */
public class CompareInputChangeEvent implements ICompareInputChangeEvent {

	private final Set inSync;
	private final Set changed;

	public CompareInputChangeEvent(Set inSync, Set changed) {
		this.inSync = inSync;
		this.changed = changed;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareInputChangeEvent#getChangedInputs()
	 */
	public ICompareInput[] getChangedInputs() {
		List result = new ArrayList();
		result.addAll(inSync);
		result.addAll(changed);
		return (ICompareInput[]) result.toArray(new ICompareInput[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareInputChangeEvent#hasChanged(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	public boolean hasChanged(ICompareInput input) {
		return changed.contains(input) || isInSync(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareInputChangeEvent#isInSync(org.eclipse.compare.structuremergeviewer.ICompareInput)
	 */
	public boolean isInSync(ICompareInput input) {
		return inSync.contains(input);
	}
	
	

}
