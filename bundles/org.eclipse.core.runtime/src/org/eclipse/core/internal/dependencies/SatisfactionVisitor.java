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

public class SatisfactionVisitor implements IElementSetVisitor {
	private int order;
	public SatisfactionVisitor(int order) {
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
		// if there are no pre-requisites, all available versions are satisfied
		if (elementSet.getRequired().isEmpty()) {
			elementSet.setSatisfied(elementSet.getAvailable());
			return;
		}

		Set satisfied = new HashSet();
		for (Iterator elementsIter = elementSet.getAvailable().iterator(); elementsIter.hasNext();) {
			IElement element = (IElement) elementsIter.next();
			IDependency[] dependencies = element.getDependencies();
			boolean versionSatisfied = true;
			for (int i = 0; i < dependencies.length; i++) {
				// optional pre-requisites are not relevant for satisfaction
				if (dependencies[i].isOptional())
					continue;

				IElementSet requiredNode = elementSet.getSystem().getElementSet(dependencies[i].getRequiredObjectId());

				Collection requiredNodeSatisfiedVersions = requiredNode.getSatisfied();
				boolean depSatisfied = false;
				for (Iterator requiredNodeSatisfiedVersionsIter = requiredNodeSatisfiedVersions.iterator(); requiredNodeSatisfiedVersionsIter.hasNext();) {
					IElement requiredSatisfiedVersion = (IElement) requiredNodeSatisfiedVersionsIter.next();
					if (dependencies[i].getMatchRule().isSatisfied(dependencies[i].getRequiredVersionId(), requiredSatisfiedVersion.getVersionId())) {
						depSatisfied = true;
						break;
					}
				}
				if (!depSatisfied) {
					versionSatisfied = false;
					break;
				}
			}
			if (versionSatisfied)
				satisfied.add(element);
		}
		elementSet.setSatisfied(satisfied);
	}

}
