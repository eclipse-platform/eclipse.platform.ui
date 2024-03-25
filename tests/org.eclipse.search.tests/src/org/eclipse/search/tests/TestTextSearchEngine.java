/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.search.tests;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;

/*
 * This class only exists so PDE doesn't show an error as it's referenced in plugin.xml
 * But it is never instantiated nor used.
 */
public class TestTextSearchEngine extends TextSearchEngine {

	public TestTextSearchEngine() {
	}

	@Override
	public IStatus search(TextSearchScope scope, TextSearchRequestor requestor, Pattern searchPattern, IProgressMonitor monitor) {
		throw new UnsupportedOperationException("Unimplemented method 'search'");
	}

	@Override
	public IStatus search(IFile[] scope, TextSearchRequestor requestor, Pattern searchPattern, IProgressMonitor monitor) {
		throw new UnsupportedOperationException("Unimplemented method 'search'");
	}

}
