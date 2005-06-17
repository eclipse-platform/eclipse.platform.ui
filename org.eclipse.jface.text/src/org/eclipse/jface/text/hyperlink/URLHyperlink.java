/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;

import org.eclipse.jface.text.Assert;
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
	 * @param region
	 * @param urlString
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
			String platform= SWT.getPlatform();
			if ("motif".equals(platform) || "gtk".equals(platform)) { //$NON-NLS-1$ //$NON-NLS-2$
				Program program= Program.findProgram("html"); //$NON-NLS-1$
				if (program == null)
					program= Program.findProgram("htm"); //$NON-NLS-1$
				if (program != null)
					program.execute(fURLString);
			} else
				Program.launch(fURLString);
			fURLString= null;
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
		return null;
	}
}
