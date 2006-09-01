/*******************************************************************************
 * Copyright (c) 2006 Coconut Palm Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Coconut Palm Software - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.binding;

import org.eclipse.jface.databinding.observable.AbstractObservable;
import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.IDeleteHandler;
import org.eclipse.jface.examples.databinding.compositetable.IInsertHandler;
import org.eclipse.jface.examples.databinding.compositetable.IRowContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.IRowFocusListener;
import org.eclipse.jface.examples.databinding.compositetable.RowFocusAdapter;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;
import org.eclipse.swt.widgets.Control;

/**
 * An ILazyDataRequestor implementation for CompositeTable.
 * 
 * @since 3.3
 */
public class CompositeTableObservableLazyDataRequestor extends AbstractObservable implements ILazyDataRequestor {
	private static final String DATABINDING_CONTEXT_KEY = "DATABINDING_CONTEXT";
	private DataBindingContext parentContext;
	private CompositeTable table;
	private IRowBinder rowBinder;
	
	/**
	 * Construct a CompositeTableLazyDataRequestor.
	 * 
	 * @param parentContext The parent data binding context.
	 * @param table The CompositeTable to bind
	 * @param rowBinder An IRowBinder that knows how to bind row objects in the CompositeTable
	 */
	public CompositeTableObservableLazyDataRequestor(DataBindingContext parentContext, CompositeTable table, IRowBinder rowBinder) {
		this.parentContext = parentContext;
		this.table = table;
		this.rowBinder = rowBinder;
		table.addRowContentProvider(contentProvider);
		table.addRowFocusListener(rowListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#dispose()
	 */
	public void dispose() {
		table.removeRowContentProvider(contentProvider);
		table.removeRowFocusListener(rowListener);
		if (insertDeleteProvider != null) {
			table.removeInsertHandler(insertHandler);
			table.removeDeleteHandler(deleteHandler);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#setSize(int)
	 */
	public void setSize(int size) {
		table.setNumRowsInCollection(size);
	}

	private ILazyListElementProvider elementProvider;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void addElementProvider(ILazyListElementProvider p) {
		this.elementProvider = p;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void removeElementProvider(ILazyListElementProvider p) {
		p = null;
	}
	
	private LazyInsertDeleteProvider insertDeleteProvider;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void addInsertDeleteProvider(LazyInsertDeleteProvider p) {
		this.insertDeleteProvider = p;
		table.addInsertHandler(insertHandler);
		table.addDeleteHandler(deleteHandler);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void removeInsertDeleteProvider(LazyInsertDeleteProvider p) {
		insertDeleteProvider = null;
		table.removeInsertHandler(insertHandler);
		table.removeDeleteHandler(deleteHandler);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#add(int, java.lang.Object)
	 */
	public void add(int position, Object element) {
		table.setNumRowsInCollection(table.getNumRowsInCollection()+1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#remove(int)
	 */
	public void remove(int position) {
		table.setNumRowsInCollection(table.getNumRowsInCollection()-1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		// A CompositeTableLazyDataRequestor is never stale
		return false;
	}
	
	// Event handlers ----------------------------------------------------------

	private IRowContentProvider contentProvider = new IRowContentProvider() {
		public void refresh(CompositeTable sender, int currentObjectOffset, Control row) {
			DataBindingContext bindings = (DataBindingContext) row.getData(DATABINDING_CONTEXT_KEY);
			if (bindings != null) {
				bindings.dispose();
			}
			
			Object object = elementProvider.get(currentObjectOffset);
			bindings = new DataBindingContext(parentContext);
			row.setData(DATABINDING_CONTEXT_KEY, bindings);
			
			rowBinder.bindRow(bindings, row, object);
		}
	};

	private IInsertHandler insertHandler = new IInsertHandler() {
		public int insert(int positionHint) {
			NewObject newObject = insertDeleteProvider.insertElementAt(new LazyInsertEvent(positionHint, null));
			if (newObject == null) {
				return -1;
			}
			return newObject.position;
		}
	};
	
	private IDeleteHandler deleteHandler = new IDeleteHandler() {
		public boolean canDelete(int rowInCollection) {
			return insertDeleteProvider.canDeleteElementAt(new LazyDeleteEvent(rowInCollection));
		}

		public void deleteRow(int rowInCollection) {
			insertDeleteProvider.deleteElementAt(new LazyDeleteEvent(rowInCollection));
		}
	};
	
	/*
	 * FIXME: Manage object-level commit / rollback here???
	 * 
	 * Yes.  Need to fire row validation events here?  Will user scrolling
	 * using the scroll bar cause a problem here?
	 */
	private IRowFocusListener rowListener = new RowFocusAdapter();
}
