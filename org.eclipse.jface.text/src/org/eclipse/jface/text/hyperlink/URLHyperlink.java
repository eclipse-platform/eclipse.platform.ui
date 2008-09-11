/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#open()
	 */
	public void open() {
		if (fURLString != null) {
			Program.launch(fURLString);
			return;
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getHyperlinkText()
	 */
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
