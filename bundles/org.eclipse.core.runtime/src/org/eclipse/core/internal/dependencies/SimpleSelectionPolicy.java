/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.dependencies;

import java.util.*;

/**
 * Simple selection policy.
 */

public class SimpleSelectionPolicy implements ISelectionPolicy {
	public Set selectMultiple(IElementSet elementSet) {
		// all satisfied are selected
		return new HashSet(elementSet.getSatisfied());
	}
	public IElement selectSingle(IElementSet elementSet) {		
		// just pick the satisfied element with the highest version
		IElement highest = null;
		for (Iterator satisfiedIter = elementSet.getSatisfied().iterator(); satisfiedIter.hasNext();) {
			IElement satisfiedVersion = (IElement) satisfiedIter.next();
			if (highest == null || elementSet.getSystem().compare(satisfiedVersion.getVersionId(), highest.getVersionId()) > 0)
				highest = satisfiedVersion;
		}
		return highest;
	}
}
