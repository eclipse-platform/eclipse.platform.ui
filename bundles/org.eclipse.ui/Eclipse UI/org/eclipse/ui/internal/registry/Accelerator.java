package org.eclipse.ui.internal.registry;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
public class Accelerator {
	private static final String DEFAULT_LOCALE  = "all";
	private static final String DEFAULT_PLATFORM = "all";
	private String id;
	private String key;
	private String locale;
	private String platform;
	
	public Accelerator(String id, String key, String locale, String platform) {
		this.id = id;
		this.key = key;
		
		if(locale==null) {
			this.locale = DEFAULT_LOCALE;	
		} else {
			this.locale = locale;	
		}
		
		if(platform==null) {
			this.platform = DEFAULT_PLATFORM;
		} else {
			this.platform = platform;	
		}
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
