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

public final class RegionalGestureBinding implements Comparable {

	private final static int HASH_INITIAL = 97;
	private final static int HASH_FACTOR = 107;

	public static RegionalGestureBinding create(GestureBinding gestureBinding, String locale, String platform)
		throws IllegalArgumentException {
		return new RegionalGestureBinding(gestureBinding, locale, platform);
	}	

	private GestureBinding gestureBinding;
	private String locale;
	private String platform;	

	private RegionalGestureBinding(GestureBinding gestureBinding, String locale, String platform)
		throws IllegalArgumentException {
		super();
		
		if (gestureBinding == null || locale == null || platform == null) 
			throw new IllegalArgumentException();	
		
		this.gestureBinding = gestureBinding;
		this.locale = locale;
		this.platform = platform;
	}

	public int compareTo(Object object) {
		RegionalGestureBinding regionalGestureBinding = (RegionalGestureBinding) object;
		int compareTo = gestureBinding.compareTo(regionalGestureBinding.gestureBinding);

		if (compareTo == 0) {
			compareTo = locale.compareTo(regionalGestureBinding.locale);

			if (compareTo == 0)
				compareTo = platform.compareTo(regionalGestureBinding.platform);
		}

		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof RegionalGestureBinding))
			return false;
		
		RegionalGestureBinding regionalBinding = (RegionalGestureBinding) object;
		return gestureBinding.equals(regionalBinding.gestureBinding) && locale.equals(regionalBinding.locale) && platform.equals(regionalBinding.platform);
	}
	
	public GestureBinding getGestureBinding() {
		return gestureBinding;	
	}

	public String getLocale() {
		return locale;
	}
	
	public String getPlatform() {
		return platform;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + gestureBinding.hashCode();		
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		return result;
	}
}
