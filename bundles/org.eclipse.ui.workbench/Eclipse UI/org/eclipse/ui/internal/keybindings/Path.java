/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;

public final class Path implements Comparable {

	public final static int MAXIMUM_PATH_ITEMS = 16;
	public final static String ELEMENT = "path"; //$NON-NLS-1$

	public static Path create() {
		return new Path(Collections.EMPTY_LIST);
	}

	public static Path create(PathItem pathItem)
		throws IllegalArgumentException {
		return new Path(Collections.singletonList(pathItem));
	}

	public static Path create(PathItem[] pathItems)
		throws IllegalArgumentException {
		return new Path(Arrays.asList(pathItems));
	}

	public static Path create(List pathItems)
		throws IllegalArgumentException {
		return new Path(pathItems);
	}

	public static Path read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren(PathItem.ELEMENT);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List pathItems = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)					
			pathItems.add(PathItem.read(mementos[i]));
		
		return Path.create(pathItems);
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
			if (!(iterator.next() instanceof PathItem))
				throw new IllegalArgumentException();
	}

	public List getPathItems() {
		return pathItems;
	}

	public int match(Path path)
		throws IllegalArgumentException {
		if (path == null)
			throw new IllegalArgumentException();
			
		if (path.equalsOrIsChildOf(this)) 
			return path.pathItems.size() - pathItems.size();
		else 
			return -1;
	}

	public int compareTo(Object object) {
		if (!(object instanceof Path))
			throw new ClassCastException();

		return Util.compare(pathItems.iterator(), ((Path) object).pathItems.iterator());
	}
	
	public boolean equals(Object object) {
		return object instanceof Path && pathItems.equals(((Path) object).pathItems);
	}

	public int hashCode() {
		final int i0 = 62;
		final int i1 = 37;
		int result = i0;		
		Iterator iterator = pathItems.iterator();
		
		while (iterator.hasNext())
			result = result * i1 + ((PathItem) iterator.next()).hashCode();

		return result;
	}

	public boolean equalsOrIsChildOf(Path path) {
		return pathItems.size() >= path.pathItems.size() && pathItems.subList(0, path.pathItems.size()).equals(path.pathItems);
	}
			
	public boolean isChildOf(Path path) {
		return pathItems.size() > path.pathItems.size() && pathItems.subList(0, path.pathItems.size()).equals(path.pathItems);
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = getPathItems().iterator();
		
		while (iterator.hasNext())
			((PathItem) iterator.next()).write(memento.createChild(PathItem.ELEMENT)); 
	}
}