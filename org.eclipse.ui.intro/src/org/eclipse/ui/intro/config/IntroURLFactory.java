/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.config;

import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.model.IntroURLParser;

/**
 * 
 */
public final class IntroURLFactory {
	/**
	 * Non-instantiable.
	 */
	private IntroURLFactory() {
		// do nothing
	}
	
	/**
	 * @param url
	 * @return
	 */
	public static IIntroURL createIntroURL(String url) {
		IntroURLParser parser = new IntroURLParser(url);
		if (parser.hasIntroUrl()) {
			IntroURL introURL = parser.getIntroURL();
			return introURL;
		}
		return null;
	}

}
