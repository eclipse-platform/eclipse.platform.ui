package org.eclipse.jface.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.EventListener;

/**
 * Listener for property changes.
 * <p>
 * Usage:
 * <pre>
 * IPropertyChangeListener listener =
 *   new IPropertyChangeListener() {
 *      public void propertyChange(PropertyChangeEvent event) {
 *         ... // code to deal with occurrence of property change
 *      }
 *   };
 * emitter.addPropertyChangeListener(listener);
 * ...
 * emitter.removePropertyChangeListener(listener);
 * </pre>
 * </p>
 */
public interface IPropertyChangeListener extends EventListener {
/**
 * Notification that a property has changed.
 * <p>
 * This method gets called when the observed object fires a property
 * change event.
 * </p>
 *
 * @param event the property change event object describing which property
 * changed and how
 */
public void propertyChange(PropertyChangeEvent event);
}
