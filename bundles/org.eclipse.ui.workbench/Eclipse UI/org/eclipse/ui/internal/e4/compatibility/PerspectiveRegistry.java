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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;

public class PerspectiveRegistry implements IPerspectiveRegistry {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#clonePerspective(java.lang.String, java.lang.String, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public IPerspectiveDescriptor clonePerspective(String id, String label,
			IPerspectiveDescriptor desc) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#deletePerspective(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void deletePerspective(IPerspectiveDescriptor persp) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithId(java.lang.String)
	 */
	public IPerspectiveDescriptor findPerspectiveWithId(String perspectiveId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithLabel(java.lang.String)
	 */
	public IPerspectiveDescriptor findPerspectiveWithLabel(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#getDefaultPerspective()
	 */
	public String getDefaultPerspective() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#getPerspectives()
	 */
	public IPerspectiveDescriptor[] getPerspectives() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#setDefaultPerspective(java.lang.String)
	 */
	public void setDefaultPerspective(String id) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#revertPerspective(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void revertPerspective(IPerspectiveDescriptor perspToRevert) {
		// TODO Auto-generated method stub

	}

}
