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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Path implements Comparable {

	final static int MAXIMUM_PATH_ITEMS = 16;

	private final static int HASH_FACTOR = 67;
	private final static int HASH_INITIAL = 77;
	
	static Path create() {
		return new Path(Collections.EMPTY_LIST);
	}

	static Path create(String string)
		throws IllegalArgumentException {
		return new Path(Collections.singletonList(string));
	}

	static Path create(String[] strings)
		throws IllegalArgumentException {
		return new Path(Arrays.asList(strings));
	}

	static Path create(List strings)
		throws IllegalArgumentException {
		return new Path(strings);
	}

	private List strings;

	private Path(List strings)
		throws IllegalArgumentException {
		super();
		
		if (strings == null)
			throw new IllegalArgumentException();
		
		this.strings = Collections.unmodifiableList(new ArrayList(strings));

		if (this.strings.size() >= MAXIMUM_PATH_ITEMS)
			throw new IllegalArgumentException();
		
		Iterator iterator = this.strings.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof String))
				throw new IllegalArgumentException();
	}

	public int compareTo(Object object) {
		return Util.compare(strings, ((Path) object).strings);
	}
	
	public boolean equals(Object object) {
		return object instanceof Path && strings.equals(((Path) object).strings);
	}

	public List getStrings() {
		return strings;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = strings.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + iterator.next().hashCode();

		return result;
	}

	public boolean isChildOf(Path path, boolean equals) {
		if (path == null)
			return false;

		return Util.isChildOf(strings, path.strings, equals);
	}

	public int match(Path path)
		throws IllegalArgumentException {
		if (path == null)
			throw new IllegalArgumentException();
			
		if (path.isChildOf(this, true)) 
			return path.strings.size() - strings.size();
		else 
			return -1;
	}

	public String toString() {
		return strings.toString();	
	}
}
