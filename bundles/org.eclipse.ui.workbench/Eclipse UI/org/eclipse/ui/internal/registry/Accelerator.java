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
	//The id defined in XML
	private String id;
	//The key sequence defined in XML
	private String key;
	//The key sequence after being localized.
	private String text;
	//The locale which this accelerator was defined for.
	private String locale;
	//The platform which this accelerator was defined for.
	private String platform;
	//he key sequence after being converted to ints.
	private int accelerators[][];
	
	public static final String DEFAULT_LOCALE  = "all"; //$NON-NLS-1$
	public static final String DEFAULT_PLATFORM = "all"; //$NON-NLS-1$
	
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
		accelerators = new int[1][];
		accelerators[0] = new int[]{accelerator};
	}
	/**
	 * Return this accelerators' id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Return this accelerators' key or null if none is specified.
	 */
	public String getKey() {
		return key;	
	}
	/**
	 * Return this accelerator's key as a localized String.
	 */
	public String getText() {
		if(text != null)
			return text;
   		int acc[][] = getAccelerators();
    	StringBuffer accBuffer = new StringBuffer();
    	for (int i = 0; i < acc.length; i++) {
    		int orAcc[] = acc[i];
			for (int j = 0; j < orAcc.length; j++) {
				accBuffer.append(Action.convertAccelerator(orAcc[j]));
				if(j + 1 < orAcc.length)
					accBuffer.append(' ');
			}
			if(i + 1 < acc.length)
				accBuffer.append(", "); //$NON-NLS-1$
		}
		text = new String(accBuffer);
		return text;		
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
	public int[][] getAccelerators() {
		if(accelerators == null)
			accelerators = convertAccelerator();
		return accelerators;
	}
	/**
	 * Parses the given accelerator text, and converts it to a
	 * int[][] (possibly of length 1) of accelerator key codes.
	 */
	private int[][] convertAccelerator() {
		List accelerators = new ArrayList(1);
		StringTokenizer orTokenizer = new StringTokenizer(key,"||"); //$NON-NLS-1$
		while (orTokenizer.hasMoreTokens()) {
			List acc = new ArrayList(2);
			StringTokenizer spaceTokenizer = new StringTokenizer(orTokenizer.nextToken());
			while (spaceTokenizer.hasMoreTokens()) {
				int accelerator = Action.convertAccelerator(spaceTokenizer.nextToken());
				acc.add(new Integer(accelerator));
			}
			int result[] = new int[acc.size()];
			for (int i = 0; i < result.length; i++) {
				result[i] = ((Integer)acc.get(i)).intValue();
			}
			accelerators.add(result);		
		}
		int result[][] = new int[accelerators.size()][];
		accelerators.toArray(result);
		return result;
	}
}
