/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris McGee (IBM) - Bug 380325 - Release filesystem fragment providing Java 7 NIO2 support
 *******************************************************************************/
package org.eclipse.core.internal.filesystem.java7;

import java.nio.file.FileSystems;
import java.util.Set;
import org.eclipse.core.internal.filesystem.local.NativeHandler;

/**
 * Handler factory for NativeHandler's using only Java 7 API's.
 */
public class HandlerFactory {
	private static final NativeHandler HANDLER;

	static {
		Set<String> views = FileSystems.getDefault().supportedFileAttributeViews();
		if (views.contains("posix")) { //$NON-NLS-1$
			HANDLER = new PosixHandler();
		} else if (views.contains("dos")) { //$NON-NLS-1$
			HANDLER = new DosHandler();
		} else {
			HANDLER = new DefaultHandler();
		}
	}

	public static NativeHandler getHandler() {
		return HANDLER;
	}
}
