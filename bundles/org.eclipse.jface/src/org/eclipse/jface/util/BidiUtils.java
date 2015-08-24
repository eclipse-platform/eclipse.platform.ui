/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
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

import org.eclipse.equinox.bidi.StructuredTextTypeHandlerFactory;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SegmentListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * This class provides API to handle Base Text Direction (BTD) and
 * Structured Text support for SWT Text widgets.
 *
 * @since 3.9
 */
public final class BidiUtils {

	/**
	 * Left-To-Right Base Text Direction.
	 * @see #getTextDirection()
	 */
	public static final String LEFT_TO_RIGHT = "ltr"; //$NON-NLS-1$

	/**
	 * Right-To-Left Base Text Direction.
	 * @see #getTextDirection()
	 */
	public static final String RIGHT_TO_LEFT = "rtl";//$NON-NLS-1$

	/**
	 * Auto (contextual) Base Text Direction.
	 * @see #getTextDirection()
	 */
	public static final String AUTO = "auto";//$NON-NLS-1$

	/**
	 * Base Text Direction defined in {@link BidiUtils#getTextDirection()}
	 * @see #getSegmentListener(String)
	 * @see #applyBidiProcessing(Text, String)
	 */
	public static final String BTD_DEFAULT = "default";//$NON-NLS-1$

	/**
	 * Visual Left-To-Right Text Direction.
	 * <p>
	 * <b>Note:</b> This handling type is deprecated and should only be used
	 * when interfacing with legacy systems that store data in visual order.
	 *
	 * @see <a
	 *      href="http://www.w3.org/International/questions/qa-visual-vs-logical">http://www.w3.org/International/questions/qa-visual-vs-logical</a>
	 * @see #getSegmentListener(String)
	 * @see #applyBidiProcessing(Text, String)
	 *
	 * @since 3.11
	 */
	public static final String VISUAL_LEFT_TO_RIGHT = "visualltr"; //$NON-NLS-1$

	/**
	 * Visual Right-To-Left Text Direction
	 * <p>
	 * <b>Note:</b> This handling type is deprecated and should only be used
	 * when interfacing with legacy systems that store data in visual order.
	 *
	 * @see <a
	 *      href="http://www.w3.org/International/questions/qa-visual-vs-logical">http://www.w3.org/International/questions/qa-visual-vs-logical</a>
	 * @see #getSegmentListener(String)
	 * @see #applyBidiProcessing(Text, String)
	 *
	 * @since 3.11
	 */
	public static final String VISUAL_RIGHT_TO_LEFT = "visualrtl";//$NON-NLS-1$

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
	 * Segment listener for LTR Visual Text Direction
	 */
	private static final SegmentListener VISUAL_TEXT_DIRECTION_LTR = new VisualTextDirectionSegmentListener(
			VISUAL_LEFT_TO_RIGHT);

	/**
	 * Segment listener for RTL Visual Text Direction
	 */
	private static final SegmentListener VISUAL_TEXT_DIRECTION_RTL = new VisualTextDirectionSegmentListener(
			VISUAL_RIGHT_TO_LEFT);

	/**
	 * Listener cache. Map from structured text type id ({@link String}) to
	 * structured text segment listener ({@link SegmentListener}).
	 */
	private static final Map<String, SegmentListener> structuredTextSegmentListeners = new HashMap<>();

	/**
	 * The LRE char
	 */
	static final char LRE = 0x202A;

	/**
	 * The LRM char
	 */
	static final char LRM = 0x200E;

	/**
	 * The PDF char
	 */
	static final char PDF = 0x202C;

	/**
	 * The RLE char
	 */
	static final char RLE = 0x202B;

	/**
	 * The LRO char
	 */
	static final char LRO = 0x202D;

	/**
	 * The RLO char
	 */
	static final char RLO = 0x202E;

	private static boolean bidiSupport = false;
	private static String textDirection = "";//$NON-NLS-1$

	private BidiUtils() {
		// no instances
	}

	/**
	 * Returns the Base Text Direction. Possible values are:
	 * <ul>
	 * <li>{@link BidiUtils#LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#RIGHT_TO_LEFT}</li>
	 * <li>{@link BidiUtils#AUTO}</li>
	 * <li><code>null</code> (no direction set)</li>
	 * </ul>
	 *
	 * @return the base text direction
	 */
	public static String getTextDirection() {
		return textDirection;
	}

	/**
	 * Sets the Base Text Direction. Possible values are:
	 * <ul>
	 * <li>{@link BidiUtils#LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#RIGHT_TO_LEFT}</li>
	 * <li>{@link BidiUtils#AUTO}</li>
	 * <li><code>null</code> (no default direction)</li>
	 * </ul>
	 *
	 * @param direction the text direction to set
	 * @throws IllegalArgumentException if <code>direction</code> is not legal
	 */
	public static void setTextDirection(String direction) {
		if (direction == null || LEFT_TO_RIGHT.equals(direction) || RIGHT_TO_LEFT.equals(direction) || AUTO.equals(direction)) {
			textDirection = direction;
		} else {
			throw new IllegalArgumentException(direction);
		}
	}

	/**
	 * Returns whether bidi support is enabled.
	 *
	 * @return <code>true</code> iff bidi support is enabled
	 */
	public static boolean getBidiSupport() {
		return bidiSupport;
	}

	/**
	 * Enables or disables bidi support.
	 *
	 * @param bidi <code>true</code> to enable bidi support, <code>false</code> to disable
	 */
	public static void setBidiSupport(boolean bidi) {
		bidiSupport = bidi;
	}

	/**
	 * Applies bidi processing to the given text field.
	 *
	 * <p>
	 * Possible values for <code>handlingType</code> are:
	 * <ul>
	 * <li>{@link BidiUtils#LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#RIGHT_TO_LEFT}</li>
	 * <li>{@link BidiUtils#AUTO}</li>
	 * <li>{@link BidiUtils#BTD_DEFAULT}</li>
	 * <li>{@link BidiUtils#VISUAL_LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#VISUAL_RIGHT_TO_LEFT}</li>
	 * <li>the <code>String</code> constants in
	 * {@link StructuredTextTypeHandlerFactory}</li>
	 * <li>if OSGi is running, the types that have been contributed to the
	 * <code>org.eclipse.equinox.bidi.bidiTypes</code> extension point.</li>
	 * </ul>
	 * <p>
	 * The 3 values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT}, and
	 * {@link #AUTO} are usable whether {@link #getBidiSupport() bidi support}
	 * is enabled or disabled.
	 * <p>
	 * The remaining values only have an effect if bidi support is enabled.
	 * <p>
	 * The 4 first values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT},
	 * {@link #AUTO}, and {@link #BTD_DEFAULT} are for Base Text Direction (BTD)
	 * handling. The remaining values are for Structured Text handling.
	 * <p>
	 * <strong>Note:</strong> If this method is called on a text control, then
	 * {@link #applyTextDirection(Control, String)} must not be called on the
	 * same control.
	 * <p>
	 * <strong>Note:</strong> The Structured Text handling only works if the
	 * <code>org.eclipse.equinox.bidi</code> bundle is on the classpath!
	 * </p>
	 *
	 * <p>
	 * <strong>Note:</strong>
	 * {@link org.eclipse.swt.widgets.Text#addSegmentListener(SegmentListener)}
	 * is currently only implemented on Windows and GTK, so this method won't
	 * have an effect on Cocoa.
	 *
	 * @param field
	 *            the text field
	 * @param handlingType
	 *            the type of handling
	 * @throws IllegalArgumentException
	 *             if <code>handlingType</code> is not a known type identifier
	 */
	public static void applyBidiProcessing(Text field, String handlingType) {
		SegmentListener listener = getSegmentListener(handlingType);
		if (listener != null) {
			field.addSegmentListener(listener);
			if (InternalPolicy.DEBUG_BIDI_UTILS) {
				int color = 0;
				if (LEFT_TO_RIGHT.equals(handlingType)) {
					color = SWT.COLOR_RED;
				} else if (RIGHT_TO_LEFT.equals(handlingType)) {
					color = SWT.COLOR_GREEN;
				} else if (BTD_DEFAULT.equals(handlingType)) {
					color = SWT.COLOR_YELLOW;
				} else if (AUTO.equals(handlingType)) {
					color = SWT.COLOR_MAGENTA;
				} else {
					color = SWT.COLOR_CYAN;
				}
				field.setBackground(field.getDisplay().getSystemColor(color));
				if (field.getMessage().length() == 0) {
					field.setMessage('<' + handlingType + '>');
				}
				if (field.getToolTipText() == null) {
					field.setToolTipText('<' + handlingType + '>');
				}
			}
		}
	}

	/**
	 * Applies bidi processing to the given styled text field.
	 *
	 * <p>
	 * Possible values for <code>handlingType</code> are:
	 * <ul>
	 * <li>{@link BidiUtils#LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#RIGHT_TO_LEFT}</li>
	 * <li>{@link BidiUtils#AUTO}</li>
	 * <li>{@link BidiUtils#BTD_DEFAULT}</li>
	 * <li>{@link BidiUtils#VISUAL_LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#VISUAL_RIGHT_TO_LEFT}</li>
	 * <li>the <code>String</code> constants in
	 * {@link StructuredTextTypeHandlerFactory}</li>
	 * <li>if OSGi is running, the types that have been contributed to the
	 * <code>org.eclipse.equinox.bidi.bidiTypes</code> extension point.</li>
	 * </ul>
	 * <p>
	 * The 3 values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT}, and
	 * {@link #AUTO} are usable whether {@link #getBidiSupport() bidi support}
	 * is enabled or disabled.
	 * <p>
	 * The remaining values only have an effect if bidi support is enabled.
	 * <p>
	 * The 4 first values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT},
	 * {@link #AUTO}, and {@link #BTD_DEFAULT} are for Base Text Direction (BTD)
	 * handling. The remaining values are for Structured Text handling.
	 * <p>
	 * <strong>Note:</strong> If this method is called on a text control, then
	 * {@link #applyTextDirection(Control, String)} must not be called on the
	 * same control.
	 * <p>
	 * <strong>Note:</strong> The Structured Text handling only works if the
	 * <code>org.eclipse.equinox.bidi</code> bundle is on the classpath!
	 * </p>
	 *
	 * @param field
	 *            the styled text field
	 * @param handlingType
	 *            the type of handling
	 * @throws IllegalArgumentException
	 *             if <code>handlingType</code> is not a known type identifier
	 */
	public static void applyBidiProcessing(StyledText field, String handlingType) {
		final SegmentListener listener = getSegmentListener(handlingType);
		if (listener != null) {
			field.addBidiSegmentListener(listener::getSegments);
		}
	}

	/**
	 * Applies bidi processing to the given combo.
	 *
	 * <p>
	 * Possible values for <code>handlingType</code> are:
	 * <ul>
	 * <li>{@link BidiUtils#LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#RIGHT_TO_LEFT}</li>
	 * <li>{@link BidiUtils#AUTO}</li>
	 * <li>{@link BidiUtils#BTD_DEFAULT}</li>
	 * <li>{@link BidiUtils#VISUAL_LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#VISUAL_RIGHT_TO_LEFT}</li>
	 * <li>the <code>String</code> constants in
	 * {@link StructuredTextTypeHandlerFactory}</li>
	 * <li>if OSGi is running, the types that have been contributed to the
	 * <code>org.eclipse.equinox.bidi.bidiTypes</code> extension point.</li>
	 * </ul>
	 * <p>
	 * The 3 values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT}, and
	 * {@link #AUTO} are usable whether {@link #getBidiSupport() bidi support}
	 * is enabled or disabled.
	 * <p>
	 * The remaining values only have an effect if bidi support is enabled.
	 * <p>
	 * The 4 first values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT},
	 * {@link #AUTO}, and {@link #BTD_DEFAULT} are for Base Text Direction (BTD)
	 * handling. The remaining values are for Structured Text handling.
	 * <p>
	 * <strong>Note:</strong> If this method is called on a combo control, then
	 * {@link #applyTextDirection(Control, String)} must not be called on the
	 * same control.
	 * <p>
	 * <strong>Note:</strong> The Structured Text handling only works if the
	 * <code>org.eclipse.equinox.bidi</code> bundle is on the classpath!
	 * </p>
	 *
	 * <p>
	 * <strong>Note:</strong>
	 * {@link org.eclipse.swt.widgets.Combo#addSegmentListener(SegmentListener)}
	 * is currently only implemented on Windows so this method won't have an
	 * effect on Cocoa and GTK.
	 *
	 * @param combo
	 *            the combo field
	 * @param handlingType
	 *            the type of handling
	 * @throws IllegalArgumentException
	 *             if <code>handlingType</code> is not a known type identifier
	 * @since 3.10
	 */
	public static void applyBidiProcessing(Combo combo, String handlingType) {
		SegmentListener listener = getSegmentListener(handlingType);
		if (listener != null) {
			combo.addSegmentListener(listener);
			if (InternalPolicy.DEBUG_BIDI_UTILS) {
				int color = 0;
				if (LEFT_TO_RIGHT.equals(handlingType)) {
					color = SWT.COLOR_RED;
				} else if (RIGHT_TO_LEFT.equals(handlingType)) {
					color = SWT.COLOR_GREEN;
				} else if (BTD_DEFAULT.equals(handlingType)) {
					color = SWT.COLOR_YELLOW;
				} else if (AUTO.equals(handlingType)) {
					color = SWT.COLOR_MAGENTA;
				} else {
					color = SWT.COLOR_CYAN;
				}
				combo.setBackground(combo.getDisplay().getSystemColor(color));
				if (combo.getToolTipText() == null) {
					combo.setToolTipText('<' + handlingType + '>');
				}
			}
		}
	}

	/**
	 * Returns a segment listener for the given <code>handlingType</code> that
	 * can e.g. be passed to {@link Text#addSegmentListener(SegmentListener)}.
	 *
	 * <p>
	 * <strong>Note:</strong> The Structured Text handling only works if the
	 * <code>org.eclipse.equinox.bidi</code> bundle is on the classpath!
	 * </p>
	 *
	 * @param handlingType
	 *            the handling type as specified in
	 *            {@link #applyBidiProcessing(Text, String)}
	 * @return the segment listener, or <code>null</code> if no handling is
	 *         required
	 * @throws IllegalArgumentException
	 *             if <code>handlingType</code> is not a known type identifier
	 * @see #applyBidiProcessing(Text, String)
	 */
	public static SegmentListener getSegmentListener(String handlingType) {
		SegmentListener listener = null;
		if (LEFT_TO_RIGHT.equals(handlingType)) {
			listener = BASE_TEXT_DIRECTION_LTR;
		} else if (RIGHT_TO_LEFT.equals(handlingType)) {
			listener = BASE_TEXT_DIRECTION_RTL;
		} else if (AUTO.equals(handlingType)) {
			listener = BASE_TEXT_DIRECTION_AUTO;

		} else if (getBidiSupport()) {
			if (BTD_DEFAULT.equals(handlingType)) {
				if (LEFT_TO_RIGHT.equals(getTextDirection())) {
					listener = BASE_TEXT_DIRECTION_LTR;
				} else if (RIGHT_TO_LEFT.equals(getTextDirection())) {
					listener = BASE_TEXT_DIRECTION_RTL;
				} else if (AUTO.equals(getTextDirection())) {
					listener = BASE_TEXT_DIRECTION_AUTO;
				}
			} else if (VISUAL_LEFT_TO_RIGHT.equals(handlingType)) {
				listener = VISUAL_TEXT_DIRECTION_LTR;
			} else if (VISUAL_RIGHT_TO_LEFT.equals(handlingType)) {
				listener = VISUAL_TEXT_DIRECTION_RTL;
			} else {
				Object handler = structuredTextSegmentListeners.get(handlingType);
				if (handler != null) {
					listener = (SegmentListener) handler;
				} else {
					listener = new StructuredTextSegmentListener(handlingType);
					structuredTextSegmentListeners.put(handlingType, listener);
				}
			}
		}
		return listener;
	}

	/**
	 * Applies a Base Text Direction to the given control (and its descendants,
	 * if it's a {@link Composite}).
	 *
	 * <p>
	 * Possible values for <code>textDirection</code> are:
	 * <ul>
	 * <li>{@link BidiUtils#LEFT_TO_RIGHT}</li>
	 * <li>{@link BidiUtils#RIGHT_TO_LEFT}</li>
	 * <li>{@link BidiUtils#AUTO}</li>
	 * <li>{@link BidiUtils#BTD_DEFAULT}</li>
	 * </ul>
	 * <p>
	 * The 3 values {@link #LEFT_TO_RIGHT}, {@link #RIGHT_TO_LEFT}, and
	 * {@link BidiUtils#AUTO} are usable whether {@link #getBidiSupport() bidi
	 * support} is enabled or disabled.
	 * <p>
	 * The remaining value {@link BidiUtils#BTD_DEFAULT} only has an effect if
	 * bidi support is enabled.
	 *
	 * <p>
	 * <strong>Note:</strong> If this method is called on a control, then no
	 * <code>applyBidiProcessing</code> method must be called on the same
	 * control.
	 * <p>
	 * <strong>Note:</strong>
	 * {@link org.eclipse.swt.widgets.Control#setTextDirection(int)} is
	 * currently only implemented on Windows, so the direction won't be
	 * inherited by descendants on GTK and Cocoa.
	 * <p>
	 *
	 * @param control
	 *            the control
	 * @param textDirection
	 *            the text direction
	 */
	public static void applyTextDirection(Control control, String textDirection) {
		int textDir = 0;

		if (LEFT_TO_RIGHT.equals(textDirection)) {
			textDir = SWT.LEFT_TO_RIGHT;
		} else if (RIGHT_TO_LEFT.equals(textDirection)) {
			textDir = SWT.RIGHT_TO_LEFT;
		} else if (AUTO.equals(textDirection)) {
			textDir = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
		} else if (getBidiSupport() && BTD_DEFAULT.equals(textDirection)) {
			if (LEFT_TO_RIGHT.equals(getTextDirection())) {
				textDir = SWT.LEFT_TO_RIGHT;
			} else if (RIGHT_TO_LEFT.equals(getTextDirection())) {
				textDir = SWT.RIGHT_TO_LEFT;
			} else if (AUTO.equals(getTextDirection())) {
				textDir = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
			}
		}

		if (control instanceof Text && textDir != 0) {
			applyBidiProcessing((Text) control, textDirection);
		} else if (control instanceof StyledText && textDir != 0) {
			applyBidiProcessing((StyledText) control, textDirection);
		} else if (control instanceof Combo && textDir != 0) {
			applyBidiProcessing((Combo) control, textDirection);
		} else if (textDir != 0) {
			control.setTextDirection(textDir);
		}
	}
}
