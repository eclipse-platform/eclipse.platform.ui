/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IMemento;

public final class Locale {
	
	public final static String ELEMENT = "locale";		

	public static Locale create() {
		return new Locale(Path.create());
	}
		
	public static Locale create(Path path)
		throws IllegalArgumentException {
		return new Locale(path);
	}

	public static Locale read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		return Locale.create(Path.read(memento.getChild(Path.ELEMENT)));
	}

	public static Locale system() {
		List pathItems = new ArrayList();
		java.util.Locale locale = java.util.Locale.getDefault();
		
		if (locale != null) {
			String language = locale.getLanguage();
			
			if (language != null && language.length() > 0) {
				pathItems.add(PathItem.create(language));
				String country = locale.getCountry();
				
				if (country != null && country.length() > 0) {
					pathItems.add(PathItem.create(country));
					String variant = locale.getVariant();
					
					if (variant != null && variant.length() > 0)
						pathItems.add(PathItem.create(variant));
				}
			}
		}
	
		return Locale.create(Path.create(pathItems));	
	}
	
	private Path path;
	
	private Locale(Path path)
		throws IllegalArgumentException {
		super();
		
		if (path == null)
			throw new IllegalArgumentException();
		
		this.path = path;	
	}
	
	public Path getPath() {
		return path;	
	}

	public int match(Locale locale)
		throws IllegalArgumentException {
		if (locale == null)
			throw new IllegalArgumentException();
		
		return (path.match(locale.path));
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Locale))
			throw new ClassCastException();
			
		return path.compareTo(((Locale) object).path); 
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Locale))
			return false;
		
		return path.equals(((Locale) object).path);		
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		path.write(memento.createChild(Path.ELEMENT));
	}
}
