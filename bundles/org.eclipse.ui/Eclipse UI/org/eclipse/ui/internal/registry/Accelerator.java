package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Captures the attributes of a mapping between an accelerator key
 * and an action id. Accelerators may be specific to a locale and/or platform,
 * or they may general accelerators applicable to all platforms and locales. If
 * two accelerators exist, one with locale and/or platform of "all", and the other
 * with a more specific locale and/or platform, the more specific accelerator is
 * used.
 */
public class Accelerator {
	private static final String DEFAULT_LOCALE  = "all";
	private static final String DEFAULT_PLATFORM = "all";
	private String id;
	private String key;
	private String locale;
	private String platform;
	private Integer accelerators[][];
	
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
	public Accelerator(String id,int accelerator) {
		this.id = id;
		accelerators = new Integer[1][];
		accelerators[0] = new Integer[]{new Integer(accelerator)};
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
	public Integer[][] getAccelerators() {
		if(accelerators == null)
			accelerators = convertAccelerator();
		return accelerators;
	}
	/**
	 * Parses the given accelerator text, and converts it to a
	 * list (possibly of length 1) of accelerator key codes.
	 *
	 * @param acceleratorText the accelerator text
	 * @return the SWT key code list, or an empty list if there is no accelerator
	 */
	private Integer[][] convertAccelerator() {
		List accelerators = new ArrayList(1);
		StringTokenizer orTokenizer = new StringTokenizer(key,"||");
		while (orTokenizer.hasMoreTokens()) {
			List acc = new ArrayList(2);
			StringTokenizer spaceTokenizer = new StringTokenizer(orTokenizer.nextToken());
			while (spaceTokenizer.hasMoreTokens()) {
				int accelerator = 0;
				StringTokenizer keyTok = new StringTokenizer(spaceTokenizer.nextToken(), "+");    //$NON-NLS-1$
				int keyCode = -1;
				
				while (keyTok.hasMoreTokens()) {
					String token = keyTok.nextToken();
					// Every token except the last must be one of the modifiers Ctrl, Shift, or Alt.
					if (keyTok.hasMoreTokens()) {
						int modifier = Action.findModifier(token);
						if (modifier != 0) {
							accelerator |= modifier;
						} else {//Leave if there are none
							return new Integer[0][0];
						}
					} else {
						keyCode = Action.findKeyCode(token);
					}
				}
				if (keyCode != -1) {
					accelerator |= keyCode;
				}
				acc.add(new Integer(accelerator));
			}
			Integer result[] = new Integer[acc.size()];
			acc.toArray(result);
			accelerators.add(result);		
		}
		Integer result[][] = new Integer[accelerators.size()][];
		accelerators.toArray(result);
		return result;
	}
}
