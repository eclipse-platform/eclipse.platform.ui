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

import java.util.List;

import org.eclipse.ui.internal.util.Util;

final class State implements Comparable {

	final static int MAXIMUM_PATHS = 8;

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = State.class.getName().hashCode();

	static State getInstance(List paths) {
		return new State(paths);
	}

	private List paths;

	private State(List paths) {
		super();
		this.paths = Util.safeCopy(paths, Path.class);
		
		if (this.paths.size() >= MAXIMUM_PATHS)
			throw new IllegalArgumentException();
	}

	public int compareTo(Object object) {
		State state = (State) object;
		int compareTo = Util.compare(paths, state.paths);
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof State))
			return false;

		State state = (State) object;	
		return paths.equals(state.paths);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + paths.hashCode();
		return result;
	}

	public String toString() {
		return paths.toString();	
	}

	List getPaths() {
		return paths;
	}

	boolean isChildOf(State state, boolean equals) {
		if (state == null)
			throw new NullPointerException();

		return Util.isChildOf(paths, state.paths, equals);
	}

	int match(State state) {
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
}
