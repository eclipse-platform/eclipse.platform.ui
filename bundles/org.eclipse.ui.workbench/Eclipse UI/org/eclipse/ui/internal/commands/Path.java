/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.ui.internal.util.Util;

final class Path implements Comparable {

	final static int MAXIMUM_STRINGS = 16;

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Path.class.getName().hashCode();
	
	static Path getInstance() {
		return new Path(Collections.EMPTY_LIST);
	}

	static Path getInstance(String string) {
		return new Path(Collections.singletonList(string));
	}

	static Path getInstance(String[] strings) {
		return new Path(Arrays.asList(strings));
	}

	static Path getInstance(List strings) {
		return new Path(strings);
	}

	private List strings;

	private Path(List strings) {
		super();
		this.strings = Util.safeCopy(strings, String.class);

		if (this.strings.size() >= MAXIMUM_STRINGS)
			throw new IllegalArgumentException();
	}

	public int compareTo(Object object) {
		Path path = (Path) object;
		int compareTo = Util.compare(strings, path.strings);
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Path))
			return false;

		Path path = (Path) object;	
		return strings.equals(path.strings);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + strings.hashCode();
		return result;
	}

	public String toString() {
		return strings.toString();	
	}

	List getStrings() {
		return strings;
	}

	boolean isChildOf(Path path, boolean equals) {
		if (path == null)
			throw new NullPointerException();

		return Util.isChildOf(strings, path.strings, equals);
	}

	int match(Path path) {
		if (path == null)
			throw new NullPointerException();
			
		if (path.isChildOf(this, true)) 
			return path.strings.size() - strings.size();
		else 
			return -1;
	}
}
