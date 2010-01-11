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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.util.PrefUtil;

public class PerspectiveRegistry implements IPerspectiveRegistry {

	@Inject
	private IExtensionRegistry extensionRegistry;

	private Map<String, IPerspectiveDescriptor> descriptors = new HashMap<String, IPerspectiveDescriptor>();

	@PostConstruct
	void postConstruct() {
		IExtensionPoint point = extensionRegistry.getExtensionPoint("org.eclipse.ui.perspectives"); //$NON-NLS-1$
		for (IConfigurationElement element : point.getConfigurationElements()) {
			String id = element.getAttribute("id"); //$NON-NLS-1$
			String label = element.getAttribute("name"); //$NON-NLS-1$

			descriptors.put(id, new PerspectiveDescriptor(id, label));
		}
	}

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
		return descriptors.get(perspectiveId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#findPerspectiveWithLabel(java.lang.String)
	 */
	public IPerspectiveDescriptor findPerspectiveWithLabel(String label) {
		for (IPerspectiveDescriptor descriptor : descriptors.values()) {
			if (descriptor.getLabel().equals(label)) {
				return descriptor;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#getDefaultPerspective()
	 */
	public String getDefaultPerspective() {
		return PrefUtil.getAPIPreferenceStore().getString(
				IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#getPerspectives()
	 */
	public IPerspectiveDescriptor[] getPerspectives() {
		return descriptors.values().toArray(new IPerspectiveDescriptor[descriptors.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#setDefaultPerspective(java.lang.String)
	 */
	public void setDefaultPerspective(String id) {
		IPerspectiveDescriptor desc = findPerspectiveWithId(id);
		if (desc != null) {
			PrefUtil.getAPIPreferenceStore().setValue(
					IWorkbenchPreferenceConstants.DEFAULT_PERSPECTIVE_ID, id);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveRegistry#revertPerspective(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void revertPerspective(IPerspectiveDescriptor perspToRevert) {
		// TODO Auto-generated method stub

	}

}
