/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.net.URL;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

class URLHintProvider implements Supplier<Point> {

	private static final Pattern QUERY_PATTERN = Pattern.compile("&size=(\\d+)x(\\d+)"); //$NON-NLS-1$
	private static final Pattern PATH_PATTERN = Pattern.compile("/(\\d+)x(\\d+)/"); //$NON-NLS-1$

	private URL url;

	public URLHintProvider(URL url) {
		this.url = url;
	}

	@Override
	public Point get() {
		String query = url.getQuery();
		Matcher matcher;
		if (query != null && !query.isEmpty()) {
			matcher = QUERY_PATTERN.matcher("&" + query); //$NON-NLS-1$
		} else {
			String path = url.getPath();
			matcher = PATH_PATTERN.matcher(path);
		}
		if (matcher.find()) {
			return new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
		}
		return null;
	}

}
