/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.runtime;

import java.io.*;
import org.eclipse.core.tools.BaseTextView;

public class StackTraceView extends BaseTextView {

	/** The id by which this view is known in the plug-in registry */
	public static final String VIEW_ID = StackTraceView.class.getName();

	public StackTraceView() {
		super();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	public void setInput(String stackFileName, long begin, long end) {
		if (begin == -1 || end == -1) {
			viewer.getDocument().set("No stack trace available.\nSee <eclipse install>/plugins/org.eclipse.osgi/.options"); //$NON-NLS-1$
			viewer.refresh();
			return;
		}

		try {
			byte[] chars = new byte[0];
			FileInputStream fis = new FileInputStream(stackFileName);
			try {
				fis.skip(begin);
				chars = new byte[(int) (end - begin)];
				fis.read(chars);
			} finally {
				fis.close();
			}
			viewer.getDocument().set(new String(chars));
			viewer.refresh();
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
