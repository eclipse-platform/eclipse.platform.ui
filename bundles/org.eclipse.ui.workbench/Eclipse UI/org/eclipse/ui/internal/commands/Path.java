/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Path implements Comparable {

	final static int MAXIMUM_PATH_ITEMS = 16;

	private final static int HASH_FACTOR = 77;
	private final static int HASH_INITIAL = 87;
	
	static Path create() {
		return new Path(Collections.EMPTY_LIST);
	}

	static Path create(String pathItem)
		throws IllegalArgumentException {
		return new Path(Collections.singletonList(pathItem));
	}

	static Path create(String[] pathItems)
		throws IllegalArgumentException {
		return new Path(Arrays.asList(pathItems));
	}

	static Path create(List pathItems)
		throws IllegalArgumentException {
		return new Path(pathItems);
	}

	private List pathItems;

	private Path(List pathItems)
		throws IllegalArgumentException {
		super();
		
		if (pathItems == null)
			throw new IllegalArgumentException();
		
		this.pathItems = Collections.unmodifiableList(new ArrayList(pathItems));

		if (this.pathItems.size() >= MAXIMUM_PATH_ITEMS)
			throw new IllegalArgumentException();
		
		Iterator iterator = this.pathItems.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof String))
				throw new IllegalArgumentException();
	}

	public int compareTo(Object object) {
		return Util.compare(pathItems, ((Path) object).pathItems);
	}
	
	public boolean equals(Object object) {
		return object instanceof Path && pathItems.equals(((Path) object).pathItems);
	}

	public List getPathItems() {
		return pathItems;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = pathItems.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + iterator.next().hashCode();

		return result;
	}

	public boolean isChildOf(Path path, boolean equals) {
		if (path == null)
			return false;

		return Util.isChildOf(pathItems, path.pathItems, equals);
	}

	public int match(Path path)
		throws IllegalArgumentException {
		if (path == null)
			throw new IllegalArgumentException();
			
		if (path.isChildOf(this, true)) 
			return path.pathItems.size() - pathItems.size();
		else 
			return -1;
	}

	public String toString() {
		return pathItems.toString();	
	}
}