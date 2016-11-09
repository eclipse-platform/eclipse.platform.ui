/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Kurtakov - Bug 460787
 *     Sopot Cela - Bug 466829
 *******************************************************************************/
package org.eclipse.help.internal.search;

import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * Tokenizer breaking words around letters or digits. Also normalizes to lower
 * case.
 */
public class LowerCaseAndDigitsTokenizer extends CharTokenizer {

	@Override
	protected boolean isTokenChar(int c) {
		return Character.isLetterOrDigit(c);
	}

	@Override
	protected int normalize(int c) {
		return Character.toLowerCase(c);
	}

}
