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

public class ResolutionVisitor implements IElementSetVisitor {
	private int order;
	public ResolutionVisitor(int order) {
		this.order = order;
	}
	public int getOrder() {
		return order;
	}
	public Collection getAncestors(ElementSet elementSet) {
		return elementSet.getRequired();
	}
	public Collection getDescendants(ElementSet elementSet) {
		return elementSet.getRequiring();
	}
	public void update(ElementSet elementSet) {
		// if there are no pre-requisites, all selected versions are resolved
		if (elementSet.getRequired().isEmpty()) {
			elementSet.setResolved(elementSet.getSelected());
			return;
		}
		Set resolved = new HashSet();

		for (Iterator elementsIter = elementSet.getSelected().iterator(); elementsIter.hasNext();) {
			IElement element = (IElement) elementsIter.next();
			IDependency[] dependencies = element.getDependencies();
			boolean versionResolved = true;
			for (int i = 0; i < dependencies.length; i++) {
				IElementSet requiredNode = elementSet.getSystem().getElementSet(dependencies[i].getRequiredObjectId());
				List requiredNodeResolvedVersions = new ArrayList(requiredNode.getResolved());
				Object highestRequiredVersionId = null;
				for (Iterator requiredNodeResolvedVersionsIter = requiredNodeResolvedVersions.iterator(); requiredNodeResolvedVersionsIter.hasNext();) {
					IElement requiredResolvedVersion = (IElement) requiredNodeResolvedVersionsIter.next();
					if (dependencies[i].getMatchRule().isSatisfied(dependencies[i].getRequiredVersionId(), requiredResolvedVersion.getVersionId()))
						if (highestRequiredVersionId == null || elementSet.getSystem().compare(requiredResolvedVersion.getVersionId(), highestRequiredVersionId) > 0)
							highestRequiredVersionId = requiredResolvedVersion.getVersionId();
				}
				if (highestRequiredVersionId == null && !dependencies[i].isOptional()) {
					versionResolved = false;
					break;
				}
				// new version id will be null if dependency cannot be satisfied but is optional
				elementSet.resolveDependency(element, dependencies[i], highestRequiredVersionId);
			}
			if (versionResolved)
				resolved.add(element);
		}
		elementSet.setResolved(resolved);
	}
}
