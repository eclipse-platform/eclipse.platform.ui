/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class State implements Comparable {

	final static int MAXIMUM_PATHS = 8;
	final static String ELEMENT = "state";

	static State create(Path configuration, Path locale, Path platform, Path scope)
		throws IllegalArgumentException {
		List paths = new ArrayList();
		paths.add(scope);			
		paths.add(configuration);
		paths.add(platform);
		paths.add(locale);
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

	List getPaths() {
		return paths;	
	}

	int match(State state) {
		int match = 0;
		
		for (int i = 0; i < paths.size(); i++) {
			int path = ((Path) paths.get(i)).match((Path) state.paths.get(i)); 
			
			if (path == -1 || path >= 16)
				return -1;	
			else 
				match += path << (MAXIMUM_PATHS - 1 - i) * 8;
		}		
		
		return match;
	}

	public int compareTo(Object object) {
		if (!(object instanceof State))
			throw new ClassCastException();	
		
		return Util.compare(paths.iterator(), ((State) object).paths.iterator());
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof State)) 
			return false;
		
		return paths.equals(((State) object).paths); 
	}
}
