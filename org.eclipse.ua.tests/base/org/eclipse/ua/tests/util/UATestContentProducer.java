/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import org.eclipse.help.IHelpContentProducer;
import org.eclipse.help.internal.util.URLCoder;

public class UATestContentProducer implements IHelpContentProducer {

	public InputStream getInputStream(String pluginId, String href,
			Locale locale) {
		if (href.startsWith("generated/")) {
			return getGeneratedInputStream(href, locale);
		}
		return null;
	}

	// Format is generated/title/body content/suffix
	private InputStream getGeneratedInputStream(String href, Locale locale) {
		int slash1 = 9;
		int slash2 = href.indexOf('/', slash1 + 1);
		int dotHtml = href.indexOf(".html");
		String title = href.substring(slash1 + 1, slash2);
		String body = href.substring(slash2 + 1, dotHtml);
		String result = "<head><title>";
		result += filterNonAlpha(URLCoder.decode(title));
		result += "</title></head><body>";
        result +=filterNonAlpha(URLCoder.decode(body));
		result += "</body>";
		return new ByteArrayInputStream(result.getBytes());
	}

	private String filterNonAlpha(String input) {
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c == ' ' || Character.isLetter(c)) {
				output.append(c);
			}
		}
		System.out.println("Input = " + input + "output = " + output.toString());
		return output.toString();
	}

}
