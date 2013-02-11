/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.SegmentListener;
import org.eclipse.swt.widgets.Text;

/**
 * This class provides API to handle Base Text Direction (BTD) and
 * Structured Text support for SWT Text widgets. 
 * 
 * @since 3.9
 */
public final class BidiUtils {

	/**
	 * Left To Right Base Text Direction
	 */
	public static final String LEFT_TO_RIGHT = "ltr"; //$NON-NLS-1$
	
	/**
	 * Right To Left Base Text Direction
	 */
	public static final String RIGHT_TO_LEFT = "rtl";//$NON-NLS-1$

	/**
	 * Auto (Contextual) Base Text Direction
	 */
	public static final String AUTO = "auto";//$NON-NLS-1$
	
	/**
	 * Bidi support is enabled
	 */
	public static final String BIDI_SUPPORT_ON = "y";//$NON-NLS-1$
	
	/**
	 * Bidi support is disabled
	 */
	public static final String BIDI_SUPPORT_OFF = "n";//$NON-NLS-1$
	
	/**
	 * Base Text Direction defined in {@link BidiUtils#getTextDirection()}
	 * 
	 */
	public static final String BTD_DEFAULT = "default";//$NON-NLS-1$
	
	/**
	 * Segment listener for LTR Base Text Direction
	 */
	private static final SegmentListener BASE_TEXT_DIRECTION_LTR = new BaseTextDirectionSegmentListener(LEFT_TO_RIGHT);
	
	/**
	 * Segment listener for RTL Base Text Direction
	 */
	private static final SegmentListener BASE_TEXT_DIRECTION_RTL = new BaseTextDirectionSegmentListener(RIGHT_TO_LEFT);
	
	
	/**
	 * Segment listener for Auto (Contextual) Base Text Direction
	 */
	private static final SegmentListener BASE_TEXT_DIRECTION_AUTO = new BaseTextDirectionSegmentListener(AUTO);
	
	/**
	 * The map containing all the structured text segment listeners
	 */
	private static final Map structuredTextSegmentListenerMap = new HashMap();
	
	/**
	 * The LRE char
	 */
	protected static final char LRE = 0x202A;
	
	/**
	 * The LRM char
	 */
	protected static final char LRM = 0x200E;
	
	/**
	 * The PDF char
	 */
	protected static final char PDF = 0x202C;
	
	/**
	 * The RLE char
	 */
	protected static final char RLE = 0x202B;
	
	/**
	 * Singleton.
	 */
	private static BidiUtils instance;
	
	/**
	 * Return the singleton instance of this class.
	 * 
	 * @return the singleton instance
	 * 
	 */
	public static BidiUtils getInstance() {
		if (instance == null) {
			instance = new BidiUtils();
		}

		return instance;
	}
	
	private String bidiSupport = BIDI_SUPPORT_OFF;
	private String textDirection = "";//$NON-NLS-1$
	
	/**
	 * @return the textDirection.
	 * <p>
	 * Possible values are:
	 * <ul>
	 * <li> {@link BidiUtils#LEFT_TO_RIGHT}
	 * <li> {@link BidiUtils#RIGHT_TO_LEFT}
	 * <li> {@link BidiUtils#AUTO}
	 * <li> <code>""</code>
	 * </ul>
	 * 
	 * 
	 */
	public String getTextDirection() {
		return textDirection;
	}

	/**
	 * @param textDirection the text direction to set.
	 * <p>
	 * Possible values are:
	 * <ul>
	 * <li> {@link BidiUtils#LEFT_TO_RIGHT}
	 * <li> {@link BidiUtils#RIGHT_TO_LEFT}
	 * <li> {@link BidiUtils#AUTO}
	 * </ul>
	 */
	public void setTextDirection(String textDirection) {
		if (textDirection != null
				&& !textDirection.equals(this.textDirection)) {
			this.textDirection = textDirection;			
		}
	}

	/**
	 * @return the bidiSupport.
	 * <p>
	 * Possible values are:
	 * <ul>
	 * <li> {@link BidiUtils#BIDI_SUPPORT_ON}
	 * <li> {@link BidiUtils#BIDI_SUPPORT_OFF}
	 * </ul>
	 */
	public String getBidiSupport() {
		return bidiSupport;
	}

	/**
	 * @param bidiSupport the bidi support to set.
	 * 
	 * <p>
	 * Possible values are:
	 * <ul>
	 * <li> {@link BidiUtils#BIDI_SUPPORT_ON}
	 * <li> {@link BidiUtils#BIDI_SUPPORT_OFF}
	 * </ul>
	 */	
	public void setBidiSupport(String bidiSupport) {
		if (bidiSupport != null
				&& !bidiSupport.equals(this.bidiSupport)) {
			this.bidiSupport = bidiSupport;				
		}
	}
	
	/**
	 * @param field 	the SWT Text field to process.
	 * @param handlingType 	the type of handling
	 * 
	 *            <p>
	 *            Possible values are:
	 *            <ul>
	 *            <li> {@link BidiUtils#LEFT_TO_RIGHT}
	 *            <li> {@link BidiUtils#RIGHT_TO_LEFT}
	 *            <li> {@link BidiUtils#AUTO}
	 *            <li> {@link BidiUtils#BTD_DEFAULT}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#EMAIL}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#FILE}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#JAVA}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#REGEXP}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#COMMA_DELIMITED}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#PROPERTY}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#RTL_ARITHMETIC}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#SQL}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#UNDERSCORE}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#XPATH}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#SYSTEM_USER}
	 *            <li>
	 *            {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory#URL}
	 *            <li>
	 *            or every other structured text type available from {@link org.eclipse.equinox.bidi.STextTypeHandlerFactory}
	 *            </ul>
	 *            <p>
	 *            One should use this API as follows:
	 *            <p>
	 *            <code><pre>
	 * 				Text inputField = new Text(shell, SWT.NONE);
	 * 				BidiUtils.applyBidiProcessing(inputField, BidiUtils.BTD_DEFAULT);
	 * 
	 * 				Text emailField = new Text(shell, SWT.NONE);	  			
	 * 				BidiUtils.applyBidiProcessing(emailField, BidiUtils.EMAIL);
	 * 			  </pre></code>
	 *            <p>
	 *            The 3 values {@link BidiUtils#LEFT_TO_RIGHT},
	 *            {@link BidiUtils#RIGHT_TO_LEFT} {@link BidiUtils#AUTO} are
	 *            usable whatever Bidi support is on or off.
	 *            <p>
	 *            The remaining values are usable only if bidi support is on.
	 *            <p>
	 *            The 4 first values {@link BidiUtils#LEFT_TO_RIGHT},
	 *            {@link BidiUtils#RIGHT_TO_LEFT}, {@link BidiUtils#AUTO} and
	 *            {@link BidiUtils#BTD_DEFAULT} are for Base Text Direction
	 *            handling. The remaining values are for Structured Text
	 *            handling.
	 *            <p>
	 *            Note:
	 *            {@link org.eclipse.swt.widgets.Text#addSegmentListener(SegmentListener)}
	 *            is currently only implemented on Windows and GTK.
	 */
	public static void applyBidiProcessing(Text field, String handlingType) {
		SegmentListener listener = null;
		if ((listener = getSegmentListener(handlingType)) != null) {
			field.addSegmentListener(listener);
		}		
	}
	
	private static SegmentListener getSegmentListener(String handlingType) {
		SegmentListener listener = null;
		if (LEFT_TO_RIGHT.equals(handlingType)) {
			listener = BASE_TEXT_DIRECTION_LTR;			
		}
		else if (RIGHT_TO_LEFT.equals(handlingType)) {
			listener = BASE_TEXT_DIRECTION_RTL;
		}
		else if (AUTO.equals(handlingType)) {
			listener = BASE_TEXT_DIRECTION_AUTO;
		}
		else {
			if (BIDI_SUPPORT_ON.equals(getInstance().getBidiSupport())) {
				if (BTD_DEFAULT.equals(handlingType)) {
					if (LEFT_TO_RIGHT.equals(getInstance().getTextDirection())) {
						listener = BASE_TEXT_DIRECTION_LTR;
					}
					else if (RIGHT_TO_LEFT.equals(getInstance().getTextDirection())) {
						listener = BASE_TEXT_DIRECTION_RTL;
					}
					else if (AUTO.equals(getInstance().getTextDirection())) {
						listener = BASE_TEXT_DIRECTION_AUTO;
					}
				}
				
				Object handler = structuredTextSegmentListenerMap.get(handlingType);
				if (handler != null) {
					listener = (SegmentListener) handler;
				}
				else {
					listener = new StructuredTextSegmentListener(handlingType);
					structuredTextSegmentListenerMap.put(handlingType, listener);
				}
			}
		}
		return listener;
	}
}
