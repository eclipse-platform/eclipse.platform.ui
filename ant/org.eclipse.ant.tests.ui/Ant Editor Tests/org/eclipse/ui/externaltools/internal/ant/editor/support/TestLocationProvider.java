/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor.support;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ant.ui.internal.editor.outline.ILocationProvider;


public class TestLocationProvider implements ILocationProvider {

	private String buildFileName;
	
	public TestLocationProvider(String buildFileName) {
		this.buildFileName= buildFileName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.editor.outline.ILocationProvider#getLocation()
	 */
	public IPath getLocation() {
		return new Path(buildFileName);
	}
}
