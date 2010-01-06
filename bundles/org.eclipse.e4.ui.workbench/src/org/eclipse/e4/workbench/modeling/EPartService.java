/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.modeling;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MPart;

public interface EPartService {
	public static final String PART_SERVICE_ROOT = "partServiceRoot"; //$NON-NLS-1$

	public void activate(MPart part);

	public void deactivate(MPart part);

	public void bringToTop(MPart part);

	public MPart findPart(String id);

	public Collection<MPart> getParts();

	public MPart getActivePart();

	public boolean isPartVisible(MPart part);

	// public MPart showPart(String id);
	//
	// public MPart showPart(MPart part);

}
