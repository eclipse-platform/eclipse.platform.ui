/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.contacts.model.internal;

import org.eclipse.e4.core.services.translation.TranslationService;

public class BinaryTranslatorProvider extends TranslationService {

	@Override
	public String translate(String key, String contributorURI) {
		if (key == null)
			return null;
		char[] charArray = key.toCharArray();
		StringBuffer tmp = new StringBuffer();
		tmp.append("0x");
		for(int i = 0; i < charArray.length; i++) {
			int value = charArray[i];
			tmp.append(Integer.toHexString(value));
		}
		return tmp.toString();
	}

}
