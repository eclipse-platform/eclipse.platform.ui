/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A parser which understands the output from Lsregister, the Mac OS mimetype
 * registration.
 *
 */
public class LsregisterParser {

	private static final String ANY_LINES = "(?:.*\\n)*"; //$NON-NLS-1$
	private static final String ENTRY_SEPERATOR = "-{80}\n"; //$NON-NLS-1$
	private String lsregisterDump;

	/**
	 * @param lsregisterDump the content from <code>lsregister -dump</code>
	 */
	public LsregisterParser(String lsregisterDump) {
		this.lsregisterDump = lsregisterDump;
	}

	/**
	 * Searches the lsregister dump content for the handler of a given scheme and
	 * returns the path to that handler (app).
	 *
	 * @param scheme
	 * @return path to the app handling the scheme
	 */
	public String getAppFor(String scheme) {
		String[] split = lsregisterDump.split(ENTRY_SEPERATOR);

		String pathRegex = "^" + ANY_LINES + "\\spath:\\s*(.*)\\n" + ANY_LINES + "\\s*bindings:.*" + scheme + ":"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		Pattern pattern = Pattern.compile(pathRegex, Pattern.MULTILINE);

		Optional<String> pathOptional = Stream.of(split).parallel().//
				filter(s -> s.startsWith("BundleClass")). //$NON-NLS-1$
				filter(s -> s.contains(scheme + ":")). //$NON-NLS-1$
				map(s -> pattern.matcher(s)).//
				filter(m -> m.find()).//
				map(m -> m.group(1)).findFirst();

		return pathOptional.orElse(""); //$NON-NLS-1$

	}
}
