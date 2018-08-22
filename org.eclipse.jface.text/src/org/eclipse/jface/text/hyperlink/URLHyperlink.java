/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jface.text.hyperlink;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.swt.program.Program;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.IRegion;


/**
 * URL hyperlink.
 *
 * @since 3.1
 */
public class URLHyperlink implements IHyperlink {

	private String fURLString;
	private IRegion fRegion;

	/**
	 * Creates a new URL hyperlink.
	 *
	 * @param region the region
	 * @param urlString the URL string
	 */
	public URLHyperlink(IRegion region, String urlString) {
		Assert.isNotNull(urlString);
		Assert.isNotNull(region);

		fRegion= region;
		fURLString= urlString;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	@Override
	public void open() {
		if (fURLString != null) {
			Program.launch(fURLString);
			return;
		}
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		return MessageFormat.format(HyperlinkMessages.getString("URLHyperlink.hyperlinkText"), new Object[] { fURLString }); //$NON-NLS-1$
	}

	/**
	 * Returns the URL string of this hyperlink.
	 *
	 * @return the URL string
	 * @since 3.2
	 */
	public String getURLString() {
		return fURLString;
	}

}
