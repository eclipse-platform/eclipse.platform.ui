/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.stream;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.boot.PlatformURLHandler;

/**
 * A helper class used to create a mapping between URLs and InputStreams. URLs
 * can have the "stream" protocol, followed by a unique path. For instance:
 * <code>stream://my/path/to/my/stream</code>
 * <p></p>
 * Sample usage:
 * <code>
 * <pre>
 * InputStreamRegistry.register(url, input);
 * try {
 * 	// perform the test
 * 	InputStream actual = InputStreamRegistry.get(url);
 * 	assertNotNull("1.0", actual);
 * 	// this test should also close the streams
 * 	assertEquals("1.1", expected, actual);
 * } finally {
 * 	InputStreamRegistry.unregister(url);
 * }
 * </pre>
 * </code>
 */
public class InputStreamRegistry {
	private static Map registry = new HashMap();

	static {
		// register the "stream" protocol for handling.
		PlatformURLHandler.register("stream", InputStreamURLConnection.class); //$NON-NLS-1$
	}

	/**
	 * Clear the registry.
	 */
	public static void clear() {
		registry = new HashMap();
	}

	/**
	 * Adds the given stream to the registry.
	 */
	public static void register(URL url, InputStream stream) {
		registry.put(url, stream);
	}

	/**
	 * Returns the stream associated with the given URL, or <code>null</code>.
	 */
	public static InputStream get(URL url) {
		return (InputStream) registry.get(url);
	}

	/**
	 * Remove the stream associated with the given URL from the registry.
	 */
	public static void unregister(URL url) {
		registry.remove(url);
	}
}