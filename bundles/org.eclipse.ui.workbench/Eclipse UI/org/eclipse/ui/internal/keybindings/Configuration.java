/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class Configuration {
	
	public final static String TAG = "configuration";		
	
	public static Configuration create() {
		return new Configuration(Path.create());
	}
	
	public static Configuration create(Path path)
		throws IllegalArgumentException {
		return new Configuration(path);
	}

	public static Configuration read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		return Configuration.create(Path.read(memento.getChild(Path.TAG)));
	}

	public static void write(IMemento memento, Configuration configuration)
		throws IllegalArgumentException {
		if (memento == null || configuration == null)
			throw new IllegalArgumentException();
		
		Path.write(memento.createChild(Path.TAG), configuration.getPath());
	}
	
	private Path path;
	
	private Configuration(Path path)
		throws IllegalArgumentException {
		super();
		
		if (path == null)
			throw new IllegalArgumentException();
		
		this.path = path;	
	}
	
	public Path getPath() {
		return path;	
	}

	public int match(Configuration configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
		
		return (path.match(configuration.path));
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Configuration))
			throw new ClassCastException();
			
		return path.compareTo(((Configuration) object).path); 
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Configuration))
			return false;
		
		return path.equals(((Configuration) object).path);		
	}
}
