/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;

import java.util.ArrayList;
import java.util.List;

public class DependencyNode extends AntNode {

	private List dependencies= new ArrayList();

	/**
	 * Creates a new dependency node containing the give dependencies
	 * 
	 * @param dependencies the dependencies in this node or <code>null</code>
	 */
	public DependencyNode(AntNode parent) {
		super(parent, AntViewElementsMessages.getString("DependencyNode.Dependencies_1")); //$NON-NLS-1$
	}

	/**
	 * Returns the dependencies stored in this node
	 * 
	 * @return String[] the dependency names stored in this node
	 */
	public String[] getDependencies() {
		if (dependencies.size() > 0) {
			return (String[])dependencies.toArray(new String[dependencies.size()]);
		}
		return new String[] {AntViewElementsMessages.getString("DependencyNode.<none>_2")}; //$NON-NLS-1$
	}
	
	/**
	 * Adds the given dependency to the dependencies stored in this node
	 * @param dependency the dependency to add
	 */
	public void add(String dependency) {
		dependencies.add(dependency);
	}

}
