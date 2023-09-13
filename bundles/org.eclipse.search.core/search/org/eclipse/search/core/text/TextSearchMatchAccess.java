/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

package org.eclipse.search.core.text;

import org.eclipse.core.resources.IFile;

/**
 * A {@link TextSearchMatchAccess} gives access to a pattern match found by the {@link TextSearchEngine}.
 * <p>
 * Please note that <code>{@link TextSearchMatchAccess}</code> objects <b>do not
 * </b> have value semantic. The state of the object might change over
 * time especially since objects are reused for different call backs. Clients shall not keep a reference to
 * a {@link TextSearchMatchAccess} element.
 * </p>
 * <p>
 * This class should only be implemented by implementors of a {@link TextSearchEngine}.
 * </p>
 * @since 3.2
 */
public abstract class TextSearchMatchAccess {

	/**
	 * Returns the file the match was found in.
	 *
	 * @return the file the match was found.
	 */
	public abstract IFile getFile();

	/**
	 * Returns the offset of this search match.
	 *
	 * @return the offset of this search match
	 */
	public abstract int getMatchOffset();

	/**
	 * Returns the length of this search match.
	 *
	 * @return the length of this search match
	 */
	public abstract int getMatchLength();

	/**
	 * Returns the length of this file's content.
	 *
	 * @return the length of this file's content.
	 */
	public abstract int getFileContentLength();

	/**
	 * Returns a character of the file's content at the given offset
	 *
	 * @param offset the offset
	 * @return the character at the given offset
	 * @throws IndexOutOfBoundsException an {@link IndexOutOfBoundsException} is
	 * thrown when the <code>offset</code> is negative or not less than the file content's length.
	 */
	public abstract char getFileContentChar(int offset);

	/**
	 * Returns the file's content at the given offsets.
	 *
	 * @param offset the offset of the requested content
	 * 	@param length the of the requested content
	 * @return the substring of the file's content
	 * @throws IndexOutOfBoundsException an {@link IndexOutOfBoundsException} is
	 * thrown when the <code>offset</code> or the <code>length</code> are negative
	 * or when <code>offset + length</code> is not less than the file content's length.
	 */
	public abstract String getFileContent(int offset, int length);

}
