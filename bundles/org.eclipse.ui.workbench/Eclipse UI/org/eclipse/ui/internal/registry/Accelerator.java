package org.eclipse.ui.internal.registry;

/**
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

public final class Accelerator {

	private String id;
	private String key;
	private String locale;
	private String platform;

	Accelerator(String id, String key, String locale, String platform) {
		super();
		this.id = id;
		this.key = key;
		this.locale = locale;
		this.platform = platform;
	}

	public String getId() {
		return id;
	}

	public String getKey() {
		return key;	
	}

	public String getLocale() {
		return locale;	
	}

	public String getPlatform() {
		return platform;	
	}	
}
