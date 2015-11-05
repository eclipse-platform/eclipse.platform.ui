/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Hashtable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

/**
 * A <code>IColorFactory</code> that may be used to select a color with a higher
 * contrast. The input colors are specified as per method number two in
 * {@link org.eclipse.core.runtime.IExecutableExtension}.
 * <p>
 * Example usage:
 *
 * <pre>
 * &lt;colorDefinition
 *     label="Declaration view background"
 *     id="org.eclipse.jdt.ui.DeclarationView.backgroundColor"&gt;
 *     &lt;colorFactory
 *             plugin="org.eclipse.ui"
 *             class="org.eclipse.ui.internal.themes.RGBContrastFactory"&gt;
 *         &lt;parameter name="foreground" value="0,0,0" /&gt;
 *         &lt;parameter name="background" value="COLOR_INFO_BACKGROUND" /&gt;
 *         &lt;parameter name="alternativeBackground" value="COLOR_LIST_BACKGROUND" /&gt;
 *     &lt;/colorFactory&gt;
 * &lt;/colorDefinition&gt;
 * </pre>
 *
 * <p>
 * Returns <em>background</em> if <em>foreground</em> is visibly distinct from
 * <em>background</em>. Otherwise, returns <em>alternativeBackground</em> if
 * that color has more difference in brightness to the foreground. If both
 * colors are bad, returns <em>background</em>. The color values may be
 * specified as RGB triples or as SWT constants.
 *
 * @see org.eclipse.swt.SWT
 * @since 3.107.100
 */
// This class is used by org.eclipse.jdt.ui/plugin.xml to fix
// https://bugs.eclipse.org/477487
public class RGBVisibleContrastColorFactory implements IColorFactory, IExecutableExtension {
	private String fg, bg, altBg;

	@Override
	public RGB createColor() {
		RGB cfg, cbg, cbgAlt;

		if (fg != null) {
			cfg = ColorUtil.getColorValue(fg);
		} else {
			cfg = new RGB(0, 0, 0);
		}
		if (bg != null) {
			cbg = ColorUtil.getColorValue(bg);
		} else {
			cbg = new RGB(255, 255, 255);
		}
		if (altBg != null) {
			cbgAlt = ColorUtil.getColorValue(altBg);
		} else {
			cbgAlt = new RGB(255, 255, 255);
		}

		float bfg = cfg.getHSB()[2];
		float bbg = cbg.getHSB()[2];
		float bbgAlt = cbgAlt.getHSB()[2];

		if (Math.abs(bbg - bfg) < 0.5f && Math.abs(bbgAlt - bfg) > Math.abs(bbg - bfg)) {
			return cbgAlt;
		}
		return cbg;
	}

	/**
	 * This executable extension requires parameters to be explicitly declared
	 * via the second method described in the <code>IExecutableExtension</code>
	 * documentation. This class expects that there will be three parameters,
	 * <code>foreground</code>, <code>background</code> and
	 * <code>alternativeBackground</code>. These values may either be RGB
	 * triples or SWT constants.
	 */
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		if (data instanceof Hashtable) {
			@SuppressWarnings("unchecked")
			Hashtable<String, String> table = (Hashtable<String, String>) data;
			fg = table.get("foreground"); //$NON-NLS-1$
			bg = table.get("background"); //$NON-NLS-1$
			altBg = table.get("alternativeBackground"); //$NON-NLS-1$
		}
	}
}
