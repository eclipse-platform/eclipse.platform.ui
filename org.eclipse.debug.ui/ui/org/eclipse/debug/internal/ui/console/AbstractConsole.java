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
package org.eclipse.debug.internal.ui.console;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.IPropertyListener;

/**
 * Common function for consoles.
 * <p>
 * Clients implementing consoles should subclass this class.
 * </p>
 * @since 3.0
 */
public abstract class AbstractConsole implements IConsole {
	
	// property listeners
	private ListenerList fListeners;
	
	/**
	 * Console name
	 */
	private String fName = null;
	
	/**
	 * Console image descriptor
	 */
	private ImageDescriptor fImageDescriptor = null;
	
	/**
	 * Used to notify this console of lifecycle methods <code>init()</code>
	 * and <code>dispose()</code>.
	 */
	class Lifecycle implements IConsoleListener {
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesAdded(org.eclipse.debug.internal.ui.console.IConsole[])
		 */
		public void consolesAdded(IConsole[] consoles) {
			for (int i = 0; i < consoles.length; i++) {
				IConsole console = consoles[i];
				if (console == AbstractConsole.this) {
					init();
				}
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.console.IConsoleListener#consolesRemoved(org.eclipse.debug.internal.ui.console.IConsole[])
		 */
		public void consolesRemoved(IConsole[] consoles) {
			for (int i = 0; i < consoles.length; i++) {
				IConsole console = consoles[i];
				if (console == AbstractConsole.this) {
					DebugUIPlugin.getDefault().getConsoleManager().removeConsoleListener(this);
					dispose();
				}
			}
		}
	}
	
	/**
	 * Notifies listeners of property changes, handling any exceptions
	 */
	class PropertyNotifier implements ISafeRunnable {
		
		private IPropertyListener fListener;
		private int fProperty;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Exception occurred during console property change notification.", exception);
			DebugUIPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fListener.propertyChanged(AbstractConsole.this, fProperty);
		}

		/**
		 * Notifies listeners of the property change
		 * 
		 * @param property the property that has changed
		 */
		public void notify(int property) {
			if (fListeners == null) {
				return;
			}
			fProperty = property;
			Object[] copiedListeners= fListeners.getListeners();
			for (int i= 0; i < copiedListeners.length; i++) {
				fListener = (IPropertyListener)copiedListeners[i];
				Platform.run(this);
			}	
			fListener = null;			
		}
	}		
	
	/**
	 * Constructs a new console with the given name and image.
	 * 
	 * @param name console name, cannot be <code>null</code>
	 * @param imageDescriptor image descriptor, or <code>null</code> if none
	 */
	public AbstractConsole(String name, ImageDescriptor imageDescriptor) {
		setName(name);
		setImageDescriptor(imageDescriptor);
		DebugUIPlugin.getDefault().getConsoleManager().addConsoleListener(new Lifecycle());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#getName()
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Sets the name of this console to the specified value and notifies
	 * property listeners of the change.
	 * 
	 * @param name the new name
	 */
	protected void setName(String name) {
		fName = name;
		firePropertyChange(IConsole.PROP_NAME);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return fImageDescriptor;
	}
	
	/**
	 * Sets the image descriptor for this console to the specified value and notifies
	 * property listeners of the change.
	 * 
	 * @param imageDescriptor the new image descriptor
	 */
	protected void setImageDescriptor(ImageDescriptor imageDescriptor) {
		fImageDescriptor =imageDescriptor;
		firePropertyChange(IConsole.PROP_IMAGE);
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		if (fListeners == null) {
			fListeners = new ListenerList();
		}
		fListeners.add(listener);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#removePropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		if (fListeners != null) {
			fListeners.remove(listener);
		}
	}

	/**
	 * Notify all listeners that the given property has changed.
	 * 
	 * @param property property identifier
	 */
	protected void firePropertyChange(int property) {
		if (fListeners == null) {
			return;
		}
		PropertyNotifier notifier = new PropertyNotifier();
		notifier.notify(property);
	}
	
	/**
	 * Called when this console is added to the console manager. Default
	 * implementation does nothing. Subclasses may override.
	 */
	protected void init() {
	}
	
	/**
	 * Called when this console is removed from the console manager. Default
	 * implementation does nothing. Subclasses may override.
	 */
	protected void dispose() {
	}
}
