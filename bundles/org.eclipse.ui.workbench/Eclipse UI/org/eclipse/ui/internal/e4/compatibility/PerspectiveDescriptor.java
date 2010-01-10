/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;

public class PerspectiveDescriptor implements IPerspectiveDescriptor {

	private String id;
	private String label;

	PerspectiveDescriptor(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveDescriptor#getLabel()
	 */
	public String getLabel() {
		return label;
	}

}
