/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.ant.internal.ui.console;

import org.eclipse.ui.console.PatternMatchEvent;

public class JikesJavacPatternMatcher extends AbstractJavacPatternMatcher {

	/*
	 * [javac] Found 1 semantic error compiling "/Users/kevinbarnes/Eclipse/runtime-workspace/Foo/src/CarriageReturn.java": [javac] 3.
	 * System.out.printer("\r");
	 */
	@Override
	public void matchFound(PatternMatchEvent event) {
		String matchedText = getMatchedText(event);
		if (matchedText == null) {
			return;
		}
		int start = matchedText.indexOf('\"') + 1;
		int end = matchedText.indexOf('\"', start);
		String filePath = matchedText.substring(start, end);
		int eventOffset = event.getOffset();
		int fileStart = matchedText.indexOf(filePath) + eventOffset;
		int fileLength = filePath.length();

		int lineNumber = getLineNumber(eventOffset, false);
		// TODO determine if error or warning
		addLink(filePath, lineNumber, fileStart, fileLength, fgErrorType);
	}
}