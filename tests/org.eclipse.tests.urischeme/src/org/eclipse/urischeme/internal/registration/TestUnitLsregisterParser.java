/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     SAP SE - initial API and implementation
*******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

public class TestUnitLsregisterParser {

	private String lsregisterDump;

	@Before
	public void setup() {
		InputStream inputStream = getClass().getResourceAsStream("lsregister.txt");
		lsregisterDump = convert(inputStream);
	}

	@Test
	public void returnsPathForContainedScheme() {
		LsregisterParser parser = new LsregisterParser(lsregisterDump);
		assertEquals("/Users/myuser/Applications/Eclipse.app", parser.getAppFor("hello"));
	}

	@Test
	public void returnsEmptyStringForNotContainedScheme() {
		LsregisterParser parser = new LsregisterParser(lsregisterDump);
		assertEquals("", parser.getAppFor("nope"));
	}

	private String convert(InputStream inputStream) {
		try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\A").next();
		}
	}
}