package org.eclipse.jface.internal.databinding.beans;

import java.util.HashMap;

import org.eclipse.jface.databinding.IUpdatableCollection;

/**
 * Maintains a registry of all JavaBean collections that are being edited
 * using data binding.
 * 
 * @since 3.2
 */
public class EditedCollectionRegistry {
	private static EditedCollectionRegistry instance = null;
	
	/**
	 * Return the default instance.
	 * 
	 * @return the default EditedCollectionRegistry.
	 */
	public static EditedCollectionRegistry getDefault() {
		if (instance == null) {
			instance = new EditedCollectionRegistry();
		}
		return instance;
	}
	
	/**
	 * Sets the default EditedCollectionRegistry.
	 * 
	 * @param instance The new default EditedCollectionRegistry object.
	 */
	public static void setDefault(EditedCollectionRegistry instance) {
		EditedCollectionRegistry.instance = instance;
	}
	
	//-------------------------------------------------------------------
	
	private static class EditedCollection {
		public int count = 0;
		public Object collection;
		public IUpdatableCollection wrapper;
		
		public boolean equals(Object arg0) {
			return collection.equals(arg0);
		}
		
		public int hashCode() {
			return collection.hashCode();
		}
		
		public EditedCollection(Object collection) {
			this.collection = collection;
		}
	}
	
	private HashMap editedCollections = new HashMap();
	
	/**
	 * Method register.  Register a collection to edit.
	 * 
	 * #register and #unRegister maintain a registration count.
	 * The collection won't be unregistered until the same number
	 * of #unRegister calls have been made as #register calls.
	 * 
	 * @param collection The collection to edit.
	 */
	public synchronized void register(Object collection) {
		if (!editedCollections.containsKey(collection)) {
			EditedCollection editedCollection = new EditedCollection(collection);
			++editedCollection.count;
			editedCollections.put(collection, editedCollection);
			// TODO: Initialize editedCollection.wrapper
		} else {
			// TODO
		}
	}
	
	/**
	 * Method unRegister.  Unregister a collection.
	 * 
	 * #register and #unRegister maintain a registration count.
	 * The collection won't be unregistered until the same number
	 * of #unRegister calls have been made as #register calls.
	 * 
	 * @param collection the collection to no longer edit.
	 */
	public synchronized void unRegister(Object collection) {
		EditedCollection ec = (EditedCollection) editedCollections.get(collection);
		if (ec == null) {
			throw new IllegalArgumentException("Attept to unRegister an unregistered collection"); //$NON-NLS-1$
		}
		ec.count--;
		if (ec.count <= 0) {
			editedCollections.remove(collection);
		}
	}

	/**
	 * Method getUpdatable.  Returns an IUpdatableCollection given
	 * a collection to edit or null if an IUpdatableCollection
	 * cannot be made.  #register(Object collection) must have been
	 * previously called to register the specified collection.
	 * 
	 * @param collection The collection to edit.
	 * @return An IUpdatableCollection or null if none can be made.
	 */
	public IUpdatableCollection getUpdatable(Object collection) {
		EditedCollection ec = (EditedCollection) editedCollections.get(collection);
		if (ec == null) {
			throw new IllegalArgumentException("Attept to getUpdatable on an unregistered collection"); //$NON-NLS-1$
		}
		return ec.wrapper;
	}
}
