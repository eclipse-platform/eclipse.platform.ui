/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands.keys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.internal.commands.Util;

public final class State implements Comparable {

	final static int MAXIMUM_PATHS = 8;
	private final static int HASH_INITIAL = 117;
	private final static int HASH_FACTOR = 127;

	static State create(List paths)
		throws IllegalArgumentException {
		return new State(paths);
	}

	private List paths;

	private State(List paths)
		throws IllegalArgumentException {
		super();
		
		if (paths == null)
			throw new IllegalArgumentException();
		
		this.paths = Collections.unmodifiableList(new ArrayList(paths));
		
		if (this.paths.size() >= MAXIMUM_PATHS)
			throw new IllegalArgumentException();
		
		Iterator iterator = this.paths.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Path))
				throw new IllegalArgumentException();
	}

	public List getPaths() {
		return paths;	
	}

	public int match(State state) {
		if (paths.size() != state.paths.size())
			return -1;
		
		int match = 0;

		for (int i = 0; i < paths.size(); i++) {
			int path = ((Path) paths.get(i)).match((Path) state.paths.get(i)); 
			
			if (path == -1 || path >= 16)
				return -1;	
			else 
				match += path << (MAXIMUM_PATHS - 1 - i) * 4;
		}		
		
		return match;
	}

	public int compareTo(Object object) {
		return Util.compare(paths, ((State) object).paths);
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof State)) 
			return false;
		
		return paths.equals(((State) object).paths); 
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = paths.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + ((Path) iterator.next()).hashCode();

		return result;
	}
}
