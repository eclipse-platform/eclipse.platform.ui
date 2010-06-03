/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.compat.parts;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.ISaveablePart;

public class DISaveableViewPart extends DIViewPart implements ISaveablePart {

	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSaveOnCloseNeeded() {
		// TODO Auto-generated method stub
		return false;
	}	
}
