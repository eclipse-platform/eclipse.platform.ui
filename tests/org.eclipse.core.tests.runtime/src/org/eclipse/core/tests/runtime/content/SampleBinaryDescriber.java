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
package org.eclipse.core.tests.runtime.content;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

public class SampleBinaryDescriber implements IContentDescriber {
	public final static String TAG = "SAMPLE_BINARY_TAG";

	public int describe(InputStream contents, IContentDescription description, int optionsMask) throws IOException {
		byte[] buffer = new byte[TAG.length()];
		if (contents.read(buffer) != buffer.length)
			return INVALID;
		return (new String(buffer).equals(TAG)) ? VALID : INVALID;
	}

	public int getSupportedOptions() {
		return 0;
	}
}