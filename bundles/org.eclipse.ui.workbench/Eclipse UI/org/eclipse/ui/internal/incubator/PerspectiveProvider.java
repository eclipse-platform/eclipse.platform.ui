/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.incubator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @since 3.3
 * 
 */
public class PerspectiveProvider extends AbstractProvider {

	private AbstractElement[] cachedElements;
	private Map idToElement = new HashMap();

	public String getId() {
		return "org.eclipse.ui.perspectives"; //$NON-NLS-1$
	}

	public AbstractElement getElementForId(String id) {
		getElements();
		return (PerspectiveElement) idToElement.get(id);
	}

	public AbstractElement[] getElements() {
		if (cachedElements == null) {
			IPerspectiveDescriptor[] perspectives = PlatformUI.getWorkbench()
					.getPerspectiveRegistry().getPerspectives();
			cachedElements = new AbstractElement[perspectives.length];
			for (int i = 0; i < perspectives.length; i++) {
				PerspectiveElement perspectiveElement = new PerspectiveElement(
						perspectives[i]);
				cachedElements[i] = perspectiveElement;
				idToElement.put(perspectiveElement.getId(), perspectiveElement);
			}
		}
		return cachedElements;
	}

	public ImageDescriptor getImageDescriptor() {
		return WorkbenchImages
				.getImageDescriptor(IWorkbenchGraphicConstants.IMG_ETOOL_DEF_PERSPECTIVE);
	}

	public String getName() {
		return IncubatorMessages.CtrlEAction_Perspectives;
	}
}
