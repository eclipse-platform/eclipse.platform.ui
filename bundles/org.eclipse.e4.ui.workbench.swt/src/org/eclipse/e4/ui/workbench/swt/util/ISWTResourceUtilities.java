/*******************************************************************************
 * Copyright (c) 2009, 2015 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - ongoing development
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.util;

import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public interface ISWTResourceUtilities extends
		IResourceUtilities<ImageDescriptor> {

	/**
	 * Low-level utility to stamp an adornment onto a given Image.
	 *
	 * @param toAdorn
	 *            The image to be adorned (must not be null)
	 * @param adornment
	 *            The image to adorn with.
	 *
	 * @return The adorned image or 'toAdorn' if 'adornment' is null
	 */
	public Image adornImage(Image toAdorn, Image adornment);
}
