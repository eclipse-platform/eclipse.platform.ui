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
package org.eclipse.compare.examples.xml;

import org.eclipse.jface.action.Action;

class SelectMatcherAction extends Action {

	private XMLStructureViewer fViewer;
	private String fDesc;
	
	/**
	 *	Create a new instance of this class
	 */
	public SelectMatcherAction(String desc, XMLStructureViewer viewer) {
		fViewer = viewer;
		fDesc = desc;
		setText(fDesc);
		setToolTipText(fDesc);
	}

	public void run() {
		((XMLStructureCreator)fViewer.getStructureCreator()).setIdMap(fDesc);
		fViewer.contentChanged();
	}
}
