/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.IUriSchemeExtensionReader;
import org.eclipse.urischeme.IUriSchemeHandler;
import org.junit.Before;
import org.junit.Test;

public class UriSchemeProcessorUnitTest {

	private UriSchemeHandlerSpy abcHandler;
	private UriSchemeProcessor schemeProcessor;
	private UriSchemeExtensionReaderMock extensionReader;

	@Before
	public void setup() throws Exception {
		abcHandler = new UriSchemeHandlerSpy();
		extensionReader = new UriSchemeExtensionReaderMock();
		extensionReader.registration.put("abc", abcHandler);

		schemeProcessor = new UriSchemeProcessor();
		schemeProcessor.reader = this.extensionReader;
	}

	@Test
	public void callsRegisteredUriSchemeHandler() throws Exception {
		schemeProcessor.handleUri("abc", "abc://test");

		String errorMsg = "Registered handler was not called for '" + "abc://test" + "'";
		assertTrue(errorMsg, abcHandler.uris.contains("abc://test"));
	}

	@Test
	public void doesntCallHandlerForUnregisteredScheme() throws Exception {
		schemeProcessor.handleUri("xyz", "xyz://test");

		assertTrue(abcHandler.uris.isEmpty());
	}

	@Test
	public void buffersExtensionAndCreatesThemOnlyOnce() throws Exception {
		schemeProcessor.handleUri("abc", "abc://test");
		schemeProcessor.handleUri("abc", "abc://test");

		assertEquals(2, abcHandler.uris.size());
		assertEquals("Extension created more than once", 1, (int) extensionReader.readCount.get("abc"));
	}

	@Test(expected = CoreException.class)
	public void passesException() throws Exception {
		extensionReader.exception = new CoreException(Status.CANCEL_STATUS);
		schemeProcessor.handleUri("abc", "abc://test");
	}

	private static class UriSchemeExtensionReaderMock implements IUriSchemeExtensionReader {

		public CoreException exception;
		public Map<String, IUriSchemeHandler> registration = new HashMap<>();
		public Map<String, Integer> readCount = new HashMap<>();

		@Override
		public Collection<IScheme> getSchemes() {
			return null;
		}

		@Override
		public IUriSchemeHandler getHandlerFromExtensionPoint(String uriScheme) throws CoreException {
			if (exception != null) {
				throw exception;
			}
			readCount.putIfAbsent(uriScheme, 0);
			readCount.put(uriScheme, readCount.get(uriScheme) + 1);
			return registration.get(uriScheme);
		}

	}

}
