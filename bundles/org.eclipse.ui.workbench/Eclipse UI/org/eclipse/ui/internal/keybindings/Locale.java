package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IMemento;

public final class Locale {
	
	public final static String TAG = "locale";		

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
			
		return Locale.create(Path.read(memento.getChild(Path.TAG)));
	}

	public static void write(IMemento memento, Locale locale)
		throws IllegalArgumentException {
		if (memento == null || locale == null)
			throw new IllegalArgumentException();
		
		Path.write(memento.createChild(Path.TAG), locale.getPath());
	}

	public static Locale system() {
		List elements = new ArrayList();
		java.util.Locale locale = java.util.Locale.getDefault();
		
		if (locale != null) {
			String language = locale.getLanguage();
			
			if (language != null && language.length() > 0) {
				elements.add(Element.create(language));
				String country = locale.getCountry();
				
				if (country != null && country.length() > 0) {
					elements.add(Element.create(country));
					String variant = locale.getVariant();
					
					if (variant != null && variant.length() > 0)
						elements.add(Element.create(variant));
				}
			}
		}
	
		return Locale.create(Path.create(elements));	
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
}
