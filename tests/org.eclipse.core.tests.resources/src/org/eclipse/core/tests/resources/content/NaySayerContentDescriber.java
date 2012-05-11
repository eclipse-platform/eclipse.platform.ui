/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import java.io.*;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

/**
 * A content describer that always rejects its input.
 * It is used in the tests, when a content type definition created
 * for name-based content type matching should not interfere with tests
 * that do content-based content type matching.
 */
public class NaySayerContentDescriber implements ITextContentDescriber {

	public int describe(InputStream contents, IContentDescription description) throws IOException {
		for (int i = 0; contents.read() != -1 && i < 2048; i++) {
			// read some data so performance tests are more reallistic
		}
		return INVALID;
	}

	public int describe(Reader contents, IContentDescription description) throws IOException {
		for (int i = 0; contents.read() != -1 && i < 2048; i++) {
			// read some data so performance tests are more reallistic
		}
		return INVALID;
	}

	public QualifiedName[] getSupportedOptions() {
		return IContentDescription.ALL;
	}
}
