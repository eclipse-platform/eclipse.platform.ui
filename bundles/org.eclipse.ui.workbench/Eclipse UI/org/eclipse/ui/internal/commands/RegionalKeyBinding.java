/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

public final class RegionalKeyBinding implements Comparable {

	private final static int HASH_INITIAL = 97;
	private final static int HASH_FACTOR = 107;

	public static RegionalKeyBinding create(KeyBinding keyBinding, String locale, String platform)
		throws IllegalArgumentException {
		return new RegionalKeyBinding(keyBinding, locale, platform);
	}	

	private KeyBinding keyBinding;
	private String locale;
	private String platform;	

	private RegionalKeyBinding(KeyBinding keyBinding, String locale, String platform)
		throws IllegalArgumentException {
		super();
		
		if (keyBinding == null || locale == null || platform == null) 
			throw new IllegalArgumentException();	
		
		this.keyBinding = keyBinding;
		this.locale = locale;
		this.platform = platform;
	}

	public int compareTo(Object object) {
		RegionalKeyBinding regionalKeyBinding = (RegionalKeyBinding) object;
		int compareTo = keyBinding.compareTo(regionalKeyBinding.keyBinding);

		if (compareTo == 0) {
			compareTo = locale.compareTo(regionalKeyBinding.locale);

			if (compareTo == 0)
				compareTo = platform.compareTo(regionalKeyBinding.platform);
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof RegionalKeyBinding))
			return false;
		
		RegionalKeyBinding regionalBinding = (RegionalKeyBinding) object;
		return keyBinding.equals(regionalBinding.keyBinding) && locale.equals(regionalBinding.locale) && platform.equals(regionalBinding.platform);
	}
	
	public KeyBinding getKeyBinding() {
		return keyBinding;	
	}

	public String getLocale() {
		return locale;
	}
	
	public String getPlatform() {
		return platform;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + keyBinding.hashCode();		
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		return result;
	}
}
