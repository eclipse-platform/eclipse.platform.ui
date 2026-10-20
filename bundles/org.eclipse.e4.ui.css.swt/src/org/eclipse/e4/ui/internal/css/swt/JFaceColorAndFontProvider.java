/*******************************************************************************
 * Copyright (c) 2013, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Resolves color and font definitions from the JFace registries, which the
 * workbench keeps in sync with its current theme.
 */
class JFaceColorAndFontProvider implements IColorAndFontProvider {

	@Override
	public FontData[] getFont(String symbolicName) {
		FontRegistry registry = JFaceResources.getFontRegistry();
		// getFontData returns the default font for unknown keys
		return registry.hasValueFor(symbolicName) ? registry.getFontData(symbolicName) : null;
	}

	@Override
	public RGB getColor(String symbolicName) {
		return JFaceResources.getColorRegistry().getRGB(symbolicName);
	}
}
