/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

public final class RegionalBinding implements Comparable {

	private final static int HASH_INITIAL = 97;
	private final static int HASH_FACTOR = 107;

	public static RegionalBinding create(Binding binding, String locale, String platform)
		throws IllegalArgumentException {
		return new RegionalBinding(binding, locale, platform);
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
