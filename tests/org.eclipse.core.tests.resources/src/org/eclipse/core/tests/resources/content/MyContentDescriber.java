/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

public class MyContentDescriber implements IContentDescriber {
	public static final Object[] MY_OPTION_VALUES = {"FOO", null, "BAR"};
	public static final String SIGNATURE = "MY_CONTENTS";

	public static final QualifiedName[] MY_OPTIONS = {new QualifiedName(ContentTypeTest.PI_RESOURCES_TESTS, "my_option_1"), new QualifiedName(ContentTypeTest.PI_RESOURCES_TESTS, "my_option_2"), new QualifiedName(ContentTypeTest.PI_RESOURCES_TESTS, "my_option_3")};

	public MyContentDescriber() {
		super();
	}

	public int describe(InputStream contents, IContentDescription description) throws IOException {
		byte[] signature = SIGNATURE.getBytes("US-ASCII");
		byte[] buffer = new byte[signature.length];
		if (contents.read(buffer) != buffer.length)
			return INVALID;
		for (int i = 0; i < signature.length; i++)
			if (signature[i] != buffer[i])
				return INVALID;
		if (description == null)
			return VALID;
		for (int i = 0; i < MY_OPTIONS.length; i++)
			setIfRequested(description, i);
		return VALID;
	}

	public QualifiedName[] getSupportedOptions() {
		return MY_OPTIONS;
	}

	private void setIfRequested(IContentDescription description, int option) {
		if (description.isRequested(MY_OPTIONS[option]))
			description.setProperty(MY_OPTIONS[option], MY_OPTION_VALUES[option]);
	}
}
