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

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.IRowContentProvider;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.observable.AbstractObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.swt.widgets.Control;

/**
 * An ILazyDataRequestor implementation for CompositeTable.
 * 
 * @since 3.3
 */
public class CompositeTableLazyDataRequestor extends AbstractObservable implements ILazyDataRequestor {
	private DataBindingContext parentContext;
	private CompositeTable table;
	private IRowBinder rowBinder;
	
	public CompositeTableLazyDataRequestor(CompositeTable table, IRowBinder rowBinder) {
		this.table = table;
		this.rowBinder = rowBinder;
		table.addRowContentProvider(contentProvider);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#dispose()
	 */
	public void dispose() {
		table.removeRowContentProvider(contentProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#setSize(int)
	 */
	public void setSize(int size) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void addElementProvider(ILazyListElementProvider p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void removeElementProvider(ILazyListElementProvider p) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void addInsertDeleteProvider(LazyInsertDeleteProvider p) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void removeInsertDeleteProvider(LazyInsertDeleteProvider p) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#add(int, java.lang.Object)
	 */
	public void add(int position, Object element) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#remove(int)
	 */
	public Object remove(int position) {
		// TODO Auto-generated method stub
		return null;
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
			
		}
	};

}
