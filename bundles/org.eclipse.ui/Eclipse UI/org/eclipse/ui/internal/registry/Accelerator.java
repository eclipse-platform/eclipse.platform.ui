package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

/**
 * Captures the attributes of a mapping between an accelerator key
 * and an action id. Accelerators may be specific to a locale and/or platform,
 * or they may general accelerators applicable to all platforms and locales. If
 * two accelerators exist, one with locale and/or platform of "all", and the other
 * with a more specific locale and/or platform, the more specific accelerator is
 * used.
 */
public class Accelerator {
	private String id;
	private String key;
	private String locale;
	private String platform;
	private Integer accelerators[][];
	
	public static final String DEFAULT_LOCALE  = "all";
	public static final String DEFAULT_PLATFORM = "all";
	
	/**
	 * Create an instance of Accelerator and initializes 
	 * it with its id, key, locale and platform.
	 */		
	public Accelerator(String id, String key, String locale, String platform) {
		this.id = id;
		this.key = key;
		this.locale = locale;
		this.platform = platform;
		
		if(locale==null)
			this.locale = DEFAULT_LOCALE;
		if(platform==null)
			this.platform = DEFAULT_PLATFORM;
	}
	/**
	 * Create an instance of Accelerator and initializes 
	 * it with its id and accelerator.
	 */	
	public Accelerator(String id,int accelerator) {
		this(id,null,null,null);
		accelerators = new Integer[1][];
		accelerators[0] = new Integer[]{new Integer(accelerator)};
	}
	/**
	 * Return this accelerators' id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Return this accelerators' id or null if none is specified.
	 */
	public String getKey() {
		return key;	
	}
	/**
	 * Return this accelerators' locale of DEFAULT_LOCALE if none is spefified.
	 */
	public String getLocale() {
		return locale;	
	}
	/**
	 * Return this accelerators' platform of DEFAULT_PLATFORM if none is spefified.
	 */
	public String getPlatform() {
		return platform;	
	}
	/**
	 * Returns an array of array of Integers with all accelerators
	 * For example:
	 * if an accelerator is specified as Ctrl+X Ctrl+C || Ctrl+Shift+F4
	 * the return of this method will be "result = new Integer[2][]". 
	 * result[0] will have 2 elements: SWT.CTRL | 'X' and SWT.CTRL | 'C'; and 
	 * result[1] will have 1 element: SWT.CTRL | SWT.SHIFT | SWT.F4.
	 */
	public Integer[][] getAccelerators() {
		if(accelerators == null)
			accelerators = convertAccelerator();
		return accelerators;
	}
	/**
	 * Parses the given accelerator text, and converts it to a
	 * Integer[][] (possibly of length 1) of accelerator key codes.
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
