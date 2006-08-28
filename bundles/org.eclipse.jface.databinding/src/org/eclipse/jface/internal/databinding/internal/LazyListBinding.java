/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.internal.databinding.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.BindingEvent;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor.NewObject;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiffEntry;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.WritableValue;
import org.eclipse.jface.internal.databinding.provisional.validation.ValidationError;

/**
 * A binding for pairs of things that operate in a lazy but synchronous manner.
 * This binding purely synchronizes two list-like things where the target side
 * operates in an event-driven manner and the model side operates in a random-
 * access manner.
 * <p>
 * Unlike other list bindings, this binding does <strong>not</strong> attempt
 * to notify observers when the contents of the individual elements inside the 
 * lists change, but only when the lists themselves receive new elements or 
 * have elements removed.
 */
public class LazyListBinding extends Binding implements ILazyListElementProvider {

	private boolean updating = false;

	private final ILazyDataRequestor targetList;
	private ILazyListElementProvider modelList;

	private LazyInsertDeleteProvider lazyInsertDeleteProvider;

	private class DelegatingInsertDeleteProvider extends LazyInsertDeleteProvider {
		private LazyInsertDeleteProvider localLazyInsertDeleteProvider;
		
		/**
		 * @param parent
		 */
		public DelegatingInsertDeleteProvider(LazyInsertDeleteProvider parent) {
			this.localLazyInsertDeleteProvider = parent;
		}
		
		public NewObject insertElementAt(LazyInsertEvent insertEvent) {
			NewObject newObject;
			try {
				updating = true;
				BindingEvent e = new BindingEvent(modelList, targetList, null,
						BindingEvent.EVENT_LAZY_INSERT,
						BindingEvent.PIPELINE_AFTER_GET);
				e.originalValue = insertEvent;
				if (failure(errMsg(fireBindingEvent(e)))) {
					return null;
				}
				
				newObject = localLazyInsertDeleteProvider.insertElementAt(insertEvent);
				
				e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
				failure(errMsg(fireBindingEvent(e)));
			} finally {
				updating = false;
			}
			if (newObject != null) {
				fetchNewIterator();
			}
			return newObject;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider#canDeleteElementAt(org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent)
		 */
		public boolean canDeleteElementAt(LazyDeleteEvent e) {
			return localLazyInsertDeleteProvider.canDeleteElementAt(e);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider#deleteElementAt(org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent)
		 */
		public void deleteElementAt(LazyDeleteEvent deleteEvent) {
			try {
				updating = true;
				BindingEvent e = new BindingEvent(modelList, targetList, null,
						BindingEvent.EVENT_LAZY_DELETE,
						BindingEvent.PIPELINE_AFTER_GET);
				e.originalValue = deleteEvent;
				failure(errMsg(fireBindingEvent(e)));

				localLazyInsertDeleteProvider.deleteElementAt(deleteEvent);
				
				e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
				failure(errMsg(fireBindingEvent(e)));
			} finally {
				updating = false;
			}
			fetchNewIterator();
		}
	}
	
	/**
	 * @param context
	 * @param targetList
	 * @param target
	 * @param modelList
	 * @param model
	 * @param bindSpec
	 */
	public LazyListBinding(DataBindingContext context, ILazyDataRequestor targetList,
			ILazyListElementProvider modelList, BindSpec bindSpec) {
		super(context);
		this.targetList = targetList;
		this.modelList = modelList;
		this.targetList.addElementProvider(this);
		lazyInsertDeleteProvider = new DelegatingInsertDeleteProvider(bindSpec.getLazyInsertDeleteProvider());
		this.targetList.addInsertDeleteProvider(lazyInsertDeleteProvider);
		
		// TODO validation/conversion as specified by the bindSpec
		modelList.addListChangeListener(modelChangeListener);
		
		fetchNewIterator();
		updateTargetFromModel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.Binding#dispose()
	 */
	public void dispose() {
		targetList.removeElementProvider(this);
		targetList.removeInsertDeleteProvider(lazyInsertDeleteProvider);
		modelList.removeListChangeListener(modelChangeListener);
		disposed=true;
	}
	
	private IListChangeListener modelChangeListener = new IListChangeListener() {
		public void handleListChange(IObservableList source, ListDiff diff) {
			if (updating) {
				return;
			}
			fetchNewIterator();
			
			// TODO validation
			BindingEvent e = new BindingEvent(modelList, targetList, diff,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			updating = true;
			try {
				// get setDiff from event object - might have been modified by a
				// listener
				ListDiff listDiff = (ListDiff) e.diff;
				ListDiffEntry[] differences = listDiff.getDifferences();
				
				// FIXME: guessing that 20 is a good number for a good user experience for now. 
				if (differences.length > 1) {
					targetList.setSize(modelList.size());
				} else {
					for (int i = 0; i < differences.length; i++) {
						ListDiffEntry entry = differences[0];
						if (entry.isAddition()) {
							targetList.add(entry.getPosition(), entry.getElement());
						} else {
							targetList.remove(entry.getPosition());
						}
					}
				}
				e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
				if (failure(errMsg(fireBindingEvent(e)))) {
					return;
				}
			} finally {
				updating = false;
			}
		}
	};
	

	private WritableValue partialValidationErrorObservable = new WritableValue(
			null);

	private WritableValue validationErrorObservable = new WritableValue(null);


	private ValidationError errMsg(ValidationError validationError) {
		partialValidationErrorObservable.setValue(null);
		validationErrorObservable.setValue(validationError);
		return validationError;
	}

	private boolean failure(ValidationError errorMessage) {
		// FIXME: Need to fire a BindingEvent here
		if (errorMessage != null
				&& errorMessage.status == ValidationError.ERROR) {
			return true;
		}
		return false;
	}

	public void updateTargetFromModel() {
		updating = true;
		try {
			int sizeToSetOnTarget = modelList.size();
			
			BindingEvent e = new BindingEvent(modelList, targetList, null,
					BindingEvent.EVENT_COPY_TO_TARGET,
					BindingEvent.PIPELINE_AFTER_GET);
			e.originalValue = new Integer(sizeToSetOnTarget);
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
			
			targetList.setSize(sizeToSetOnTarget);
			
			e.pipelinePosition = BindingEvent.PIPELINE_AFTER_CHANGE;
			if (failure(errMsg(fireBindingEvent(e)))) {
				return;
			}
		} finally {
			updating = false;
		}
	}

	public IObservableValue getValidationError() {
		return validationErrorObservable;
	}

	public IObservableValue getPartialValidationError() {
		return partialValidationErrorObservable;
	}

	public void updateModelFromTarget() {
		throw new UnsupportedOperationException("Lazy targets don't support full copies"); //$NON-NLS-1$
	}

	private RandomAccessListIterator iterator = null;
	
	/**
	 * 
	 */
	public void fetchNewIterator() {
		iterator = new RandomAccessListIterator(modelList);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider#get(int)
	 */
	public Object get(int position) {
		Object result = iterator.get(position);
		
		BindingEvent e = new BindingEvent(modelList, targetList, null,
				BindingEvent.EVENT_COPY_TO_TARGET,
				BindingEvent.PIPELINE_AFTER_GET);
		e.originalValue = new Integer(position);
		e.convertedValue = result;

		failure(errMsg(fireBindingEvent(e)));
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#addListChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener)
	 */
	public void addListChangeListener(IListChangeListener listener) {
		//noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#getElementType()
	 */
	public Object getElementType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#isEmpty()
	 */
	public boolean isEmpty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#iterator()
	 */
	public Iterator iterator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#listIterator()
	 */
	public ListIterator listIterator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#remove(int)
	 */
	public Object remove(int index) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#removeListChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.list.IListChangeListener)
	 */
	public void removeListChangeListener(IListChangeListener listener) {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#size()
	 */
	public int size() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#toArray()
	 */
	public Object[] toArray() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.IObservableList#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] a) {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int arg0, Object arg1) {
		//noop
	}

	/* (non-Javadoc)
	 * @see java.util.List#clear()
	 */
	public void clear() {
		//noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#addChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener)
	 */
	public void addChangeListener(IChangeListener listener) {
		//noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#addStaleListener(org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener)
	 */
	public void addStaleListener(IStaleListener listener) {
		//noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#removeChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener)
	 */
	public void removeChangeListener(IChangeListener listener) {
		//noop
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#removeStaleListener(org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener)
	 */
	public void removeStaleListener(IStaleListener listener) {
		//noop
	}
}