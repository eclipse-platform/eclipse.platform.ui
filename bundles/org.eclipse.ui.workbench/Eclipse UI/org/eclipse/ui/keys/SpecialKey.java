/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.keys;

import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class SpecialKey extends NaturalKey {

	private final static String ARROW_DOWN_NAME = "ARROW_DOWN"; //$NON-NLS-1$
	private final static String ARROW_LEFT_NAME = "ARROW_LEFT"; //$NON-NLS-1$
	private final static String ARROW_RIGHT_NAME = "ARROW_RIGHT"; //$NON-NLS-1$
	private final static String ARROW_UP_NAME = "ARROW_UP"; //$NON-NLS-1$
	private final static String END_NAME = "END"; //$NON-NLS-1$
	private final static String F1_NAME = "F1"; //$NON-NLS-1$
	private final static String F10_NAME = "F10"; //$NON-NLS-1$
	private final static String F11_NAME = "F11"; //$NON-NLS-1$
	private final static String F12_NAME = "F12"; //$NON-NLS-1$
	private final static String F2_NAME = "F2"; //$NON-NLS-1$
	private final static String F3_NAME = "F3"; //$NON-NLS-1$
	private final static String F4_NAME = "F4"; //$NON-NLS-1$
	private final static String F5_NAME = "F5"; //$NON-NLS-1$
	private final static String F6_NAME = "F6"; //$NON-NLS-1$
	private final static String F7_NAME = "F7"; //$NON-NLS-1$
	private final static String F8_NAME = "F8"; //$NON-NLS-1$
	private final static String F9_NAME = "F9"; //$NON-NLS-1$
	private final static String HOME_NAME = "HOME"; //$NON-NLS-1$
	private final static String INSERT_NAME = "INSERT"; //$NON-NLS-1$
	private final static String PAGE_DOWN_NAME = "PAGE_DOWN"; //$NON-NLS-1$
	private final static String PAGE_UP_NAME = "PAGE_UP"; //$NON-NLS-1$		
	
	public final static SpecialKey ARROW_DOWN = new SpecialKey(ARROW_DOWN_NAME); 
	public final static SpecialKey ARROW_LEFT = new SpecialKey(ARROW_LEFT_NAME);
	public final static SpecialKey ARROW_RIGHT = new SpecialKey(ARROW_RIGHT_NAME);
	public final static SpecialKey ARROW_UP = new SpecialKey(ARROW_UP_NAME);
	public final static SpecialKey END = new SpecialKey(END_NAME);
	public final static SpecialKey F1 = new SpecialKey(F1_NAME);
	public final static SpecialKey F10 = new SpecialKey(F10_NAME);
	public final static SpecialKey F11 = new SpecialKey(F11_NAME);
	public final static SpecialKey F12 = new SpecialKey(F12_NAME);
	public final static SpecialKey F2 = new SpecialKey(F2_NAME);
	public final static SpecialKey F3 = new SpecialKey(F3_NAME);
	public final static SpecialKey F4 = new SpecialKey(F4_NAME);
	public final static SpecialKey F5 = new SpecialKey(F5_NAME);
	public final static SpecialKey F6 = new SpecialKey(F6_NAME);
	public final static SpecialKey F7 = new SpecialKey(F7_NAME);
	public final static SpecialKey F8 = new SpecialKey(F8_NAME);
	public final static SpecialKey F9 = new SpecialKey(F9_NAME);
	public final static SpecialKey HOME = new SpecialKey(HOME_NAME);
	public final static SpecialKey INSERT = new SpecialKey(INSERT_NAME);
	public final static SpecialKey PAGE_DOWN = new SpecialKey(PAGE_DOWN_NAME);
	public final static SpecialKey PAGE_UP = new SpecialKey(PAGE_UP_NAME);

	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(SpecialKey.class.getName());		

	private SpecialKey(String name) {
		super(name);
	}

	public String format() {
		// TODO consider platform-specific resource bundles
		if ("carbon".equals(SWT.getPlatform())) { //$NON-NLS-1$    	
			if (ARROW_DOWN_NAME.equals(name))
				return Character.toString('\u2193');
			
			if (ARROW_LEFT_NAME.equals(name))
			    return Character.toString('\u2190');
			
			if (ARROW_RIGHT_NAME.equals(name))
			    return Character.toString('\u2192');
			
			if (ARROW_UP_NAME.equals(name))
				return Character.toString('\u2191');
			
			if (END_NAME.equals(name))
			    return Character.toString('\u2198');
			
			/* TODO SWT currently does not distinguish the enter key from the return key
			if (ENTER_NAME.equals(name))
				Character.toString('\u2324');
			*/
			
			if (HOME_NAME.equals(name))
				Character.toString('\u2196');				
			
			if (PAGE_DOWN_NAME.equals(name))
				Character.toString('\u21DF');					
			
			if (PAGE_UP_NAME.equals(name))
				Character.toString('\u21DE');	
		}
		
		return Util.translateString(RESOURCE_BUNDLE, name, name, false, false);
	}
}
