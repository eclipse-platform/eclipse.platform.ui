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

import org.eclipse.ui.IMemento;

public final class RegionalBinding implements Comparable {

	public final static String ELEMENT = "regionalBinding"; //$NON-NLS-1$
	private final static int HASH_INITIAL = 97;
	private final static int HASH_FACTOR = 107;
	private final static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$		
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	public static RegionalBinding create(Binding binding, String locale, String platform)
		throws IllegalArgumentException {
		return new RegionalBinding(binding, locale, platform);
	}

	public static RegionalBinding read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		IMemento mementoBinding = memento.getChild(Binding.ELEMENT);
		
		if (mementoBinding == null)
			throw new IllegalArgumentException();
			
		Binding binding = Binding.read(mementoBinding);
		String locale = memento.getString(ATTRIBUTE_LOCALE);
		
		if (locale == null)
			locale = ZERO_LENGTH_STRING;

		String platform = memento.getString(ATTRIBUTE_PLATFORM);

		if (platform == null)
			platform = ZERO_LENGTH_STRING;

		return RegionalBinding.create(binding, locale, platform);
	}
	
	private Binding binding;
	private String locale;
	private String platform;	

	private RegionalBinding(Binding binding, String locale, String platform)
		throws IllegalArgumentException {
		super();
		
		if (binding == null || locale == null || platform == null) 
			throw new IllegalArgumentException();	
		
		this.binding = binding;
		this.locale = locale;
		this.platform = platform;
	}

	public Binding getBinding() {
		return binding;	
	}

	public String getLocale() {
		return locale;
	}
	
	public String getPlatform() {
		return platform;
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		binding.write(memento.createChild(Binding.ELEMENT));
		memento.putString(ATTRIBUTE_LOCALE, locale);
		memento.putString(ATTRIBUTE_PLATFORM, platform);
	}

	public int compareTo(Object object) {
		RegionalBinding regionalBinding = (RegionalBinding) object;
		int compareTo = binding.compareTo(regionalBinding.binding);

		if (compareTo == 0) {
			compareTo = locale.compareTo(regionalBinding.locale);

			if (compareTo == 0)
				compareTo = platform.compareTo(regionalBinding.platform);
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof RegionalBinding))
			return false;
		
		RegionalBinding regionalBinding = (RegionalBinding) object;
		return binding.equals(regionalBinding.binding) && locale.equals(regionalBinding.locale) && platform.equals(regionalBinding.platform);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + binding.hashCode();		
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		return result;
	}
}
