/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.swt.widgets.Display;


/**
 * Stores synchronization information for a memory block
 * Each object of synchronization information contains a memory block,
 * a list of views to be synchronized and a list of properties to be syncrhonized.
 * The views are responsible for defining properties to be synchronized and notifying
 * the synchronizer of properties changes.  This is only for keeping track of
 * values of synchronized properties and firing events when properties are changed.
 * 
 * Memory block serves as a key for synchronization.  Views displaying the same
 * memory block can be synchronized.  Views displaying different memory block
 * cannot be synchronized.
 * 
 * @since 3.0
 */
public class SynchronizeInfo
{
	private Hashtable fPropertyListeners;	// list of views to be synchronized
	private IMemoryBlock fBlock;			// memory block blocked by the views
	private Hashtable fProperties;			// list of properties to be synchronized

	/**
	 * Fire properties changes events in ISafeRunnable to ensure that
	 * exceptions are caught and handled.
	 */
	class PropertyChangeNotifier implements ISafeRunnable
	{
		ISynchronizedMemoryBlockView fView;
		String fPropertyId;
		Object fValue;
		
		PropertyChangeNotifier(ISynchronizedMemoryBlockView view, String propertyId, Object value)
		{
			fView = view;
			fPropertyId = propertyId;
			fValue = value;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			DebugUIPlugin.log(exception);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			fView.propertyChanged(fPropertyId, fValue);
		}
	}
	
	/**
	 * Wrapper for ISynchronizedMemoryBlockView
	 * Holds a list of property filters for the view.
	 */
	class PropertyListener 
	{
		ISynchronizedMemoryBlockView fView;
		String[] fFilters;
		
		public PropertyListener(ISynchronizedMemoryBlockView view, String[] properties)
		{
			fView = view;
			
			if(properties != null)
			{	
				fFilters = properties;
			}
		}

		/**
		 * If the property matches one of the filters, the property
		 * is valid and the view should be notified about its change.
		 * @param property
		 * @return if the property is specified in the filter
		 */
		public boolean isValidProperty(String property){
			if (fFilters == null)
				return true;
			for (int i=0; i<fFilters.length; i++)
			{
				if (fFilters[i].equals(property))
				{
					return true;
				}
			}
			return false;
		}

		/**
		 * Set property filters, indicating what property change events
		 * the listener is interested in.
		 * @param filters
		 */
		public void setPropertyFilters(String[] filters){	
			fFilters = filters;
		}

		/**
		 * @return Returns the fView.
		 */
		public ISynchronizedMemoryBlockView getView() {
			return fView;
		}
	}
	
	/**
	 * Create a new synchronization info object for the memory block
	 * @param block
	 */
	public SynchronizeInfo(IMemoryBlock block)
	{
		fBlock = block;
		fProperties = new Hashtable();
		fPropertyListeners = new Hashtable();
	}
	
	/**
	 * Add an ISynchronizedMemoryBlockView to the info object.  The 
	 * view will be notified when any of the properties changes.
	 * @param view
	 */
	public void addSynchronizedView(ISynchronizedMemoryBlockView view, String[] propertyIds)
	{
		PropertyListener listener = new PropertyListener(view, propertyIds);
		
		if (!fPropertyListeners.contains(listener))
		{
			fPropertyListeners.put(view, listener);
		}
	}
	
	/**
	 * Remove an ISynchronizedMemoryBlockView from the info object.
	 * The view will no longer be notified about synchronized
	 * properties changes.
	 * @param view
	 */
	public void removeSynchronizedView(ISynchronizedMemoryBlockView view)
	{
		if (fPropertyListeners.containsKey(view))
		{
			fPropertyListeners.remove(view);
		}
	}
	
	/**
	 * Set a property and its value to the info object
	 * @param propertyId
	 * @param value
	 */
	public void setProperty(String propertyId, Object value)
	{
		if (propertyId == null)
			return;
			
		if (value == null)
			return;
			
		fProperties.put(propertyId, value);
	}
	
	/**
	 * Returns the value of the property from the info object
	 * @param propertyId
	 * @return value of the property
	 */
	public Object getProperty(String propertyId)
	{
		if (propertyId == null)
			return null;
			
		Object value = fProperties.get(propertyId);
		
		return value;	
	}
	
	/**
	 * Fire property change events
	 * @param propertyId
	 */
	public void firePropertyChanged(final String propertyId)
	{
		if (!DebugUIPlugin.getDefault().getMemoryBlockViewSynchronizer().isEnabled())
			return;
		
		// Make sure the synchronizer does not swallow any events
		// Values of the properties are updated in the syncrhonizer immediately.
		// Change events are queued up on the UI Thread.
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				if (propertyId == null)
					return;
				
				Object value = fProperties.get(propertyId);
				if (value != null)
				{				
					Enumeration enumeration = fPropertyListeners.elements();
					
					while(enumeration.hasMoreElements())
					{
						PropertyListener listener = (PropertyListener)enumeration.nextElement();
						
						ISynchronizedMemoryBlockView view = listener.getView();
						
						// if view is enabled and if it's a valid property
						if (view.isEnabled() && listener.isValidProperty(propertyId)){
							PropertyChangeNotifier notifier = new PropertyChangeNotifier(view, propertyId, value);
							Platform.run(notifier);	
						}
					}
				}
			}
		});
	}
	
	/**
	 * @return number of views being synchronized.
	 */
	public int getNumberOfSynchronizedViews()
	{
		if(fPropertyListeners == null)
			return 0;
	
		return fPropertyListeners.size();
	}
	
	/**
	 * Set up property filter for the view.
	 * @param view
	 * @param filters
	 */
	public void setPropertyFilters(ISynchronizedMemoryBlockView view, String[] filters){
		PropertyListener listener = (PropertyListener)fPropertyListeners.get(view);
		
		if (listener != null){
			listener.setPropertyFilters(filters);
		}
	}	
	
	/**
	 * Clean up the synchronization info object
	 */
	public void delete()
	{
		if (fPropertyListeners != null){
			fPropertyListeners.clear();
			fPropertyListeners = null;
		}
		
		if (fProperties != null){
			fProperties.clear();
			fProperties = null;
		}
		
		if (fBlock != null){
			fBlock = null;
		}
	}
	

}
