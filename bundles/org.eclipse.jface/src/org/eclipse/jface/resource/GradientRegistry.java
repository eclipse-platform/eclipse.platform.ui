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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * A gradient registry maintains a mapping between symbolic gradient names and SWT 
 * <code>Gradient</code>s.
 * <p>
 * A gradient registry owns all of the <code>Gradient</code> objects registered with 
 * it, and automatically disposes of them when the SWT Display that creates the 
 * <code>Gradient</code>s is disposed. Because of this, clients do not need to 
 * (indeed, must not attempt to) dispose of <code>Gradient</code> objects 
 * themselves.
 * </p>
 * <p>
 * Methods are provided for registering listeners that will be kept
 * apprised of changes to list of registed gradients.
 * </p>
 * <p>
 * Clients may instantiate this class (it was not designed to be subclassed).
 * </p>
 * 
 * @since 3.0
 */
public class GradientRegistry {

	/**
	 * This registries <code>Display</code>. All gradients will be allocated using 
	 * it.
	 */
	protected Display display;

	/**
	 * List of property change listeners (element type: <code>org.eclipse.jface.util.IPropertyChangeListener</code>).
	 */
	private ListenerList listeners = new ListenerList();

	/**
	 * Collection of <code>Gradient</code> that are now stale to be disposed when 
	 * it is safe to do so (i.e. on shutdown).
	 */
	private List staleGradients = new ArrayList();

	/**
	 * Table of known gradients, keyed by symbolic gradient name (key type: <code>String</code>,
	 * value type: <code>Gradient</code>.
	 */
	private Map stringToGradient = new HashMap(7);

	/**
	 * Table of known gradient data, keyed by symbolic gradient name (key type:
	 * <code>String</code>, value type: <code>GradientData</code>).
	 */
	private Map stringToGradientData = new HashMap(7);
	
	/**
	 * Runnable that cleans up the manager on disposal of the display.
	 */
	protected Runnable displayRunnable = new Runnable() {
		public void run() {
			clearCaches();
		}
	};

	/**
	 * Create a new instance of the receiver that is hooked to the current 
	 * display.
	 * 
	 * @see org.eclipse.swt.widgets.Display#getCurrent()
	 */
	public GradientRegistry() {
		this(Display.getCurrent());
	}

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param display the <code>Display</code> to hook into.
	 */
	public GradientRegistry(Display display) {
		Assert.isNotNull(display);
		this.display = display;
		hookDisplayDispose();
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
	 * Create a new <code>Gradient</code> on the receivers <code>Display</code>.
	 * 
	 * @param symbolicName the symbolic gradient name.
	 * @param rgb the <code>GradientData</code> data for the gradient.
	 * @return the new <code>Gradient</code> object.
	 */
	private Gradient createGradient(String symbolicName, GradientData data) {
		return new Gradient(display, data);
	}

	/**
	 * Dispose of all of the <code>Gradient</code>s in this iterator.
	 * 
	 * @param Iterator over <code>Collection</code> of <code>Gradient</code>
	 */
	private void disposeGradients(Iterator iterator) {
		while (iterator.hasNext()) {
			Object next = iterator.next();
			((Gradient) next).dispose();
		}
	}

	/**
	 * Fires a <code>PropertyChangeEvent</code>.
	 * 
	 * @param name the name of the symbolic gradient that is changing.
	 * @param oldValue the old <code>GradientData</code> value.
	 * @param newValue the new <code>GradientData</code> value.
	 */
	protected void fireGradientMappingChanged(
		String name,
		GradientData oldValue,
		GradientData newValue) {
		final Object[] myListeners = this.listeners.getListeners();
		if (myListeners.length > 0) {
			PropertyChangeEvent event =
				new PropertyChangeEvent(this, name, oldValue, newValue);
			for (int i = 0; i < myListeners.length; ++i) {
				try {
					((IPropertyChangeListener) myListeners[i]).propertyChange(
						event);
				} catch (Exception e) {
					// TODO: how to log?
				}
			}
		}
	}

	/**
	 * Returns the <code>gradient</code> associated with the given symbolic gradient 
	 * name, or <code>null</code> if no such definition exists.
	 * 
	 * @param symbolicName symbolic gradient name.
	 * @return the <code>Gradient</code>.
	 */
	public Gradient get(String symbolicName) {

		Assert.isNotNull(symbolicName);
		Object result = stringToGradient.get(symbolicName);
		if (result != null)
			return (Gradient) result;

		result = stringToGradientData.get(symbolicName);
		if (result == null)
			return null;

		// Create the gradient and update the mapping so it can
		// be shared.
		Gradient gradient = createGradient(symbolicName, (GradientData) result);

		if (gradient != null)
			stringToGradient.put(symbolicName, gradient);

		return gradient;
	}
	
	/** 
	 * @return the set of keys this manager knows about.
	 */
	public Set getKeySet() {	    
	    return Collections.unmodifiableSet(stringToGradientData.keySet());
	}

	/**
	 * Returns the gradient data associated with the given symbolic gradient name.
	 *
	 * @param symbolicName symbolic gradient name.
	 * @return the <code>GradientData</code> data.
	 */
	public GradientData getGradientData(String symbolicName) {
		Assert.isNotNull(symbolicName);
		return (GradientData) stringToGradientData.get(symbolicName);
	}

	/**
	 * Disposes all currently allocated colours.
	 */
	private void clearCaches() {
		disposeGradients(stringToGradient.values().iterator());
		disposeGradients(staleGradients.iterator());
		stringToGradient.clear();
		staleGradients.clear();
		listeners.clear();						
	}

	/**
	 * Return whether or not the receiver has a value for the supplied gradient 
	 * key.
	 * 
	 * @param gradientKey the key for the gradient.
	 * @return <code>true</code> if there is a key for the gradient.
	 */
	public boolean hasValueFor(String gradientKey) {
		return stringToGradientData.containsKey(gradientKey);
	}

	/**
	 * Hook a dispose listener on the SWT display.
	 */
	private void hookDisplayDispose() {
		display.disposeExec(displayRunnable);
	}

	/**
	 * Adds (or replaces) a gradient to this gradient registry under the given 
	 * symbolic name.
	 * <p>
	 * A property change event is reported whenever the mapping from a symbolic
	 * name to a gradient changes. The source of the event is this registry; the
	 * property name is the symbolic gradient name.
	 * </p>
	 * 
	 * @param symbolicName the symbolic gradient name
	 * @param gradientData an <code>GradientData</code> object
	 */
	public void put(String symbolicName, GradientData gradientData) {
		put(symbolicName, gradientData, true);
	}	

    /**
	 * Adds (or replaces) a gradient to this gradient registry under the given 
	 * symbolic name.
	 * <p>
	 * A property change event is reported whenever the mapping from a symbolic
	 * name to a gradient changes. The source of the event is this registry; the
	 * property name is the symbolic gradient name.
	 * </p>
	 * 
	 * @param symbolicName the symbolic gradient name
	 * @param gradientData an <code>GradientData</code> object
	 * @param update - fire a gradient mapping changed if true. False if this
	 *            method is called from the get method as no setting has
	 *            changed.
	 */
	private void put(String symbolicName, GradientData gradientData, boolean update) {

		Assert.isNotNull(symbolicName);
		Assert.isNotNull(gradientData);

		GradientData existing = (GradientData) stringToGradientData.get(symbolicName);
		if (gradientData.equals(existing))
			return;

		Gradient oldGradient = (Gradient) stringToGradient.remove(symbolicName);
		stringToGradientData.put(symbolicName, gradientData);
		if (update)
			fireGradientMappingChanged(symbolicName, existing, gradientData);

		if (oldGradient != null)
			staleGradients.add(oldGradient);
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
