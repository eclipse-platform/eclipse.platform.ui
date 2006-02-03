/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import org.eclipse.search.ui.text.AbstractSearchMatchInformationProvider;

import org.eclipse.search.core.text.TextSearchMatchAccess;

/**
 * The minimal scanner that figures out the line-numbers only.
 */
public class LineNumberScanner extends AbstractSearchMatchInformationProvider {
	public void scanFile(TextSearchMatchAccess matchAccess) {
		int length= matchAccess.getFileContentLength();
		addLineOffset(0);

		boolean r= false;
		for (int i= 0; i < length; i++) {
			switch (matchAccess.getFileContentChar(i)) {
				case '\r':
					if (r) {
						addLineOffset(i);
					}
					r= true;
					break;

				case '\n':
					addLineOffset(i + 1);
					r= false;
					break;

				default:
					if (r) {
						addLineOffset(i);
						r= false;
					}
			}
		}
	}
}
