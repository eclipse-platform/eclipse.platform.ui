/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * A color registry maintains a mapping between symbolic color names and SWT 
 * <code>Color</code>s.
 * <p>
 * A color registry owns all of the <code>Color</code> objects registered with 
 * it, and automatically disposes of them when the SWT Display that creates the 
 * <code>Color</code>s is disposed. Because of this, clients do not need to 
 * (indeed, must not attempt to) dispose of <code>Color</code> objects 
 * themselves.
 * </p>
 * <p>
 * Methods are provided for registering listeners that will be kept
 * apprised of changes to list of registed colors.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 * 
 * @since 3.0
 */
public class ColorRegistry {

	/**
	 * This registries <code>Display</code>. All colors will be allocated using 
	 * it.
	 */
	private Display display;

	/**
	 * List of property change listeners (element type: <code>org.eclipse.jface.util.IPropertyChangeListener</code>).
	 */
	private ListenerList listeners = new ListenerList();

	/**
	 * Collection of <code>Color</code> that are now stale to be disposed when 
	 * it is safe to do so (i.e. on shutdown).
	 */
	private List staleColors = new ArrayList();

	/**
	 * Table of known colors, keyed by symbolic color name (key type: <code>String</code>,
	 * value type: <code>org.eclipse.swt.graphics.Color</code>.
	 */
	private Map stringToColor = new HashMap(7);

	/**
	 * Table of known color data, keyed by symbolic color name (key type:
	 * <code>String</code>, value type: <code>org.eclipse.swt.graphics.RGB</code>).
	 */
	private Map stringToRGB = new HashMap(7);

	/**
	 * Create a new instance of the receiver that is hooked to the current 
	 * display.
	 * 
	 * @see org.eclipse.swt.widgets.Display#getCurrent()
	 */
	public ColorRegistry() {
		this(Display.getCurrent());
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param display the <code>Display</code> to hook into.
	 */
	public ColorRegistry(Display display) {
		Assert.isNotNull(display);
		this.display = display;
		hookDisplayDispose(display);
	}

	/**
	 * Adds a property change listener to this registry.
	 * 
	 * @param listener a property change listener
	 */
	public void addListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Create a new <code>Color</code> on the receivers <code>Display</code>.
	 * 
	 * @param symbolicName the symbolic color name.
	 * @param rgb the <code>RGB</code> data for the color.
	 * @return the new <code>Color</code> object.
	 */
	private Color createColor(String symbolicName, RGB rgb) {
		return new Color(display, rgb);
	}

	/**
	 * Dispose of all of the <code>Color</code>s in this iterator.
	 * 
	 * @param Iterator over <code>Collection</code> of <code>Color</code>
	 */
	private void disposeColors(Iterator iterator) {
		while (iterator.hasNext()) {
			Object next = iterator.next();
			((Color) next).dispose();
		}
	}

	/**
	 * Fires a <code>PropertyChangeEvent</code>.
	 * 
	 * @param name the name of the symbolic color that is changing.
	 * @param oldValue the old <code>RGB</code> value.
	 * @param newValue the new <code>RGB</code> value.
	 */
	private void fireColorMappingChanged(
		String name,
		RGB oldValue,
		RGB newValue) {
		final String finalName = name;
		final Object[] listeners = this.listeners.getListeners();
		if (listeners.length > 0) {
			PropertyChangeEvent event =
				new PropertyChangeEvent(this, finalName, oldValue, newValue);
			for (int i = 0; i < listeners.length; ++i) {
				try {
					((IPropertyChangeListener) listeners[i]).propertyChange(
						event);
				} catch (Exception e) {
					// TODO: how to log?
				}
			}
		}
	}

	/**
	 * Returns the <code>color</code> associated with the given symbolic color 
	 * name, or <code>null</code> if no such definition exists.
	 * 
	 * @param symbolicName symbolic color name.
	 * @return the <code>Color</code>.
	 */
	public Color get(String symbolicName) {

		Assert.isNotNull(symbolicName);
		Object result = stringToColor.get(symbolicName);
		if (result != null)
			return (Color) result;

		result = stringToRGB.get(symbolicName);
		if (result == null)
			return null;

		// Create the color and update the mapping so it can
		// be shared.
		Color color = createColor(symbolicName, (RGB) result);

		if (color != null)
			stringToColor.put(symbolicName, color);

		return color;
	}

	/**
	 * Returns the color data associated with the given symbolic color name.
	 *
	 * @param symbolicName symbolic color name.
	 * @return the <code>RGB</code> data.
	 */
	public RGB getRGB(String symbolicName) {
		Assert.isNotNull(symbolicName);
		return (RGB) stringToRGB.get(symbolicName);
	}

	/**
	 * Shut downs this resource registry and disposes of all registered colors.
	 */
	private void handleDisplayDispose() {
		disposeColors(stringToColor.values().iterator());
		disposeColors(staleColors.iterator());
		stringToColor.clear();
		staleColors.clear();
		listeners.clear();
	}

	/**
	 * Return whether or not the receiver has a value for the supplied color 
	 * key.
	 * 
	 * @param colorKey the key for the color.
	 * @return <code>true</code> if there is a key for the color.
	 */
	public boolean hasValueFor(String colorKey) {
		return stringToRGB.containsKey(colorKey);
	}

	/**
	 * Hook a dispose listener on the SWT display.
	 */
	private void hookDisplayDispose(Display display) {
		display.disposeExec(new Runnable() {
			public void run() {
				handleDisplayDispose();
			}
		});
	}

	/**
	 * Adds (or replaces) a color to this color registry under the given 
	 * symbolic name.
	 * <p>
	 * A property change event is reported whenever the mapping from a symbolic
	 * name to a color changes. The source of the event is this registry; the
	 * property name is the symbolic color name.
	 * </p>
	 * 
	 * @param symbolicName the symbolic color name
	 * @param colorData an <code>RGB</code> object
	 */
	public void put(String symbolicName, RGB colorData) {
		put(symbolicName, colorData, true);
	}

	/**
	 * Adds (or replaces) a color to this color registry under the given 
	 * symbolic name.
	 * <p>
	 * A property change event is reported whenever the mapping from a symbolic
	 * name to a color changes. The source of the event is this registry; the
	 * property name is the symbolic color name.
	 * </p>
	 * 
	 * @param symbolicName the symbolic color name
	 * @param colorData an <code>RGB</code> object
	 * @param update - fire a color mapping changed if true. False if this
	 *            method is called from the get method as no setting has
	 *            changed.
	 */
	private void put(String symbolicName, RGB colorData, boolean update) {

		Assert.isNotNull(symbolicName);
		Assert.isNotNull(colorData);

		RGB existing = (RGB) stringToRGB.get(symbolicName);
		if (colorData.equals(existing))
			return;

		Color oldColor = (Color) stringToColor.remove(symbolicName);
		stringToRGB.put(symbolicName, colorData);
		if (update)
			fireColorMappingChanged(symbolicName, existing, colorData);

		if (oldColor != null)
			staleColors.add(oldColor);
	}

	/**
	 * Removes the given listener from this registry. Has no affect if the
	 * listener is not registered.
	 * 
	 * @param listener a property change listener
	 */
	public void removeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}
}
