/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.content;

import java.io.InputStream;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

/**
 * A content describer that always rejects its input.
 * It is used in the tests, when a content type definition created
 * for name-based content type matching should not interfere with tests
 * that do content-based content type matching.
 */
public class NaySayerContentDescriber implements IContentDescriber {

	public int describe(InputStream contents, IContentDescription description) {
		return INVALID;
	}

	public QualifiedName[] getSupportedOptions() {
		return IContentDescription.ALL;
	}
}
