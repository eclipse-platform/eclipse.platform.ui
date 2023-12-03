/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.readers;

import java.io.IOException;
import java.io.Reader;


/**
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.</p>
 *
 * @since 3.14
 */
public abstract class SingleCharacterReader extends Reader {

	/**
	 * @see Reader#read()
	 */
	@Override
	public abstract int read() throws IOException;

	/**
	 * @see Reader#read(char[],int,int)
	 */
	@Override
	public int read(char cbuf[], int off, int len) throws IOException {
		int end= off + len;
		for (int i= off; i < end; i++) {
			int ch= read();
			if (ch == -1) {
				if (i == off)
					return -1;
				return i - off;
			}
			cbuf[i]= (char)ch;
		}
		return len;
	}

	/**
	 * @see Reader#ready()
	 */
	@Override
	public boolean ready() throws IOException {
		return true;
	}

	/**
	 * Returns the readable content as string.
	 * @return the readable content as string
	 * @exception IOException in case reading fails
	 */
	public String getString() throws IOException {
		StringBuilder buf= new StringBuilder();
		int ch;
		while ((ch= read()) != -1) {
			buf.append((char)ch);
		}
		return buf.toString();
	}
}
