/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matt Carter - bug 170668
 *     Brad Reynolds - bug 170848
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.internal.databinding.provisional.swt.AbstractSWTObservableValue;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * @since 1.0
 * 
 */
public class ControlObservableValue extends AbstractSWTObservableValue {

	private final Control control;

	private final String attribute;

	private Object valueType;
	
	private FocusListener focusListener;

	private ControlListener controlListener;

	private Boolean currentFocus;

	private Point currentPoint;

	private Rectangle currentBounds;

	private boolean updating;

	
	private static final Map SUPPORTED_ATTRIBUTES = new HashMap();
	static {
		SUPPORTED_ATTRIBUTES.put(SWTProperties.ENABLED, Boolean.TYPE);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.VISIBLE, Boolean.TYPE);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.TOOLTIP_TEXT, String.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.FOREGROUND, Color.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.BACKGROUND, Color.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.FONT, Font.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.LOCATION, Point.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.SIZE, Point.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.FOCUS, Boolean.class);
		SUPPORTED_ATTRIBUTES.put(SWTProperties.BOUNDS, Rectangle.class);
	}
	
	/**
	 * @param control
	 * @param attribute
	 */
	public ControlObservableValue(Control control, String attribute) {
		super(control);
		this.control = control;
		this.attribute = attribute;
		if (SUPPORTED_ATTRIBUTES.keySet().contains(attribute)) {
			this.valueType = SUPPORTED_ATTRIBUTES.get(attribute); 
		} else {
			throw new IllegalArgumentException();
		}
		
		init();
	}
	
	/**
	 * @param realm 
	 * @param control
	 * @param attribute
	 */
	public ControlObservableValue(Realm realm, Control control, String attribute) {
		super(realm, control);
		this.control = control;
		this.attribute = attribute;
		if (SUPPORTED_ATTRIBUTES.keySet().contains(attribute)) {
			this.valueType = SUPPORTED_ATTRIBUTES.get(attribute); 
		} else {
			throw new IllegalArgumentException();
		}
		
		init();
	}

	private void init() {
		if (SWTProperties.SIZE.equals(attribute)
				|| SWTProperties.LOCATION.equals(attribute)
				|| SWTProperties.BOUNDS.equals(attribute)) {
			this.currentPoint = SWTProperties.SIZE.equals(attribute) ? control
					.getSize() : control.getLocation();

			controlListener = new ControlListener() {

				public void controlMoved(ControlEvent e) {
					if (SWTProperties.LOCATION.equals(attribute)) {
						if (!updating) {
							Point oldValue = currentPoint;
							currentPoint = control.getLocation();

							notifyIfChanged(oldValue, currentPoint);
						}
					} else if (SWTProperties.BOUNDS.equals(attribute)) {
						if (!updating) {
							Rectangle oldValue = currentBounds;
							currentBounds = control.getBounds();

							notifyIfChanged(oldValue, currentBounds);
						}
					}
				}

				public void controlResized(ControlEvent e) {
					if (SWTProperties.LOCATION.equals(attribute)) {
						if (!updating) {
							Point oldValue = currentPoint;
							currentPoint = control.getSize();

							notifyIfChanged(oldValue, currentPoint);
						}
					} else if (SWTProperties.BOUNDS.equals(attribute)) {
						if (!updating) {
							Rectangle oldValue = currentBounds;
							currentBounds = control.getBounds();

							notifyIfChanged(oldValue, currentBounds);
						}
					}
				}
			};
			control.addControlListener(controlListener);
		} else if (SWTProperties.FOCUS.equals(attribute)) {
			this.currentFocus = control == control.getDisplay()
					.getFocusControl() ? Boolean.TRUE : Boolean.FALSE;
			focusListener = new FocusListener() {

				public void focusGained(FocusEvent e) {
					if (!updating) {
						Boolean oldValue = currentFocus;
						currentFocus = Boolean.TRUE;
						notifyIfChanged(oldValue, currentFocus);
					}
				}

				public void focusLost(FocusEvent e) {
					if (!updating) {
						Boolean oldValue = currentFocus;
						currentFocus = Boolean.FALSE;
						notifyIfChanged(oldValue, currentFocus);
					}
				}
			};
			control.addFocusListener(focusListener);
		}
	}
	
	public void doSetValue(Object value) {
		Object oldValue = doGetValue();
		
		if (SWTProperties.SIZE.equals(attribute)) {
			try {
				updating = true;
				control.setSize((Point) value);
			} finally {
				updating = false;
				currentPoint = control.getSize();
			}
		} else if (SWTProperties.LOCATION.equals(attribute)) {
			try {
				updating = true;
				control.setLocation((Point) value);
			} finally {
				updating = false;
				currentPoint = control.getLocation();
			}
		} else if (SWTProperties.FOCUS.equals(attribute)) {
			try {
				updating = true;
				if (Boolean.TRUE.equals(value)) {
					currentFocus = control.setFocus() ? Boolean.TRUE
							: Boolean.FALSE;
				} else {
					// TODO Not possible force the focus to leave the control
					// Maybe focus should the move to the Shell containing the
					// control
					this.currentFocus = control == control.getDisplay()
							.getFocusControl() ? Boolean.TRUE : Boolean.FALSE;
				}
			} finally {
				updating = false;
			}
		} else if (SWTProperties.BOUNDS.equals(attribute)) {
			try {
				updating = true;
				control.setBounds((Rectangle) value);
			} finally {
				updating = false;
				currentBounds = control.getBounds();
			}
		} else if (attribute.equals(SWTProperties.ENABLED)) {
			control.setEnabled(((Boolean) value).booleanValue());
		} else if (attribute.equals(SWTProperties.VISIBLE)) {
			control.setVisible(((Boolean) value).booleanValue());
		} else if (attribute.equals(SWTProperties.TOOLTIP_TEXT)) {
			control.setToolTipText((String) value);
		} else if (attribute.equals(SWTProperties.FOREGROUND)) {
			control.setForeground((Color) value);
		} else if (attribute.equals(SWTProperties.BACKGROUND)) {
			control.setBackground((Color) value);
		} else if (attribute.equals(SWTProperties.FONT)) {
			control.setFont((Font) value);
		}
		
		notifyIfChanged(oldValue, value);
	}

	public Object doGetValue() {
		if (attribute.equals(SWTProperties.ENABLED)) {
			return control.getEnabled() ? Boolean.TRUE : Boolean.FALSE;
		}
		if (attribute.equals(SWTProperties.VISIBLE)) {
			return control.getVisible() ? Boolean.TRUE : Boolean.FALSE;
		}
		if (attribute.equals(SWTProperties.TOOLTIP_TEXT)) {
			return control.getToolTipText();			
		}
		if (attribute.equals(SWTProperties.FOREGROUND))	 {
			return control.getForeground();
		}
		if (attribute.equals(SWTProperties.BACKGROUND)) {
			return control.getBackground();
		}
		if (attribute.equals(SWTProperties.FONT)) {
			return control.getFont();
		}
		if (SWTProperties.SIZE.equals(attribute)) {
			return control.getSize();
		}
		if (SWTProperties.LOCATION.equals(attribute)) {
			return control.getLocation();
		}
		if (SWTProperties.FOCUS.equals(attribute)) {
			return control == control.getDisplay().getFocusControl() ? Boolean.TRUE
					: Boolean.FALSE;
		}
		if (SWTProperties.BOUNDS.equals(attribute)) {
			return control.getBounds();
		}
		
		return null;
	}

	public Object getValueType() {
		return valueType;
	}
	
	private void notifyIfChanged(Object oldValue, Object newValue) {
		if (!Util.equals(oldValue, newValue)) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));			
		}
	}
}
