/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class Scope {
	
	public final static String ELEMENT = "scope";		

	public static Scope create() {
		return new Scope(Path.create());
	}
	
	public static Scope create(Path path)
		throws IllegalArgumentException {
		return new Scope(path);
	}

	public static Scope read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		return Scope.create(Path.read(memento.getChild(Path.ELEMENT)));
	}

	private Path path;
	
	private Scope(Path path)
		throws IllegalArgumentException {
		super();
		
		if (path == null)
			throw new IllegalArgumentException();
		
		this.path = path;	
	}
	
	public Path getPath() {
		return path;	
	}

	public int match(Scope scope)
		throws IllegalArgumentException {
		if (scope == null)
			throw new IllegalArgumentException();
		
		return (path.match(scope.path));
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Scope))
			throw new ClassCastException();
			
		return path.compareTo(((Scope) object).path); 
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Scope))
			return false;
		
		return path.equals(((Scope) object).path);		
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		path.write(memento.createChild(Path.ELEMENT));
	}
}
