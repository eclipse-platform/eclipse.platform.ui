/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding;

import java.util.Collection;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.IUpdatableCellProvider;
import org.eclipse.jface.databinding.IUpdatableValue;
import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.Updatable;
import org.eclipse.jface.databinding.beans.TableModelDescription;

/**
 * @since 3.2
 * 
 */
public class NestedUpdatableCellProvider extends Updatable implements IUpdatableCellProvider, EventSource {

	private final class ReadableSetWrapper extends AbstractUpdatableSet implements EventSource {
		protected Collection computeElements() {
			return innerUpdatableCellProvider.getReadableSet().toCollection();
		}

		public ChangeEvent fireEvent(int changeType, Object oldValue, Object newValue, Object parent, int position) {
			return fireChangeEvent(changeType, oldValue, newValue, parent, position);
		}
	}

	private final class InnerChangeListener implements IChangeListener {
		private EventSource fireFrom;
		private InnerChangeListener(EventSource fireFrom) {
			this.fireFrom = fireFrom;
		}
		public void handleChange(ChangeEvent changeEvent) {
			if (!updating) {
				ChangeEvent nestedEvent = fireFrom.fireEvent(changeEvent.getChangeType(), changeEvent
						.getOldValue(), changeEvent.getNewValue(), changeEvent.getParent(), changeEvent.getPosition());
				if(nestedEvent.getVeto()) {
					changeEvent.setVeto(true);
				}
			}
		}
	}

	private boolean updating = false;

	private ReadableSetWrapper readableSet = new ReadableSetWrapper();

	// Change listeners for the detail side of the master/detail relationship
	private IChangeListener innerSetChangeListener = new InnerChangeListener(readableSet);
	private IChangeListener innerCellChangeListener = new InnerChangeListener(this);

	private Object currentOuterValue;

	private IUpdatableCellProvider innerUpdatableCellProvider;

	private IDataBindingContext databindingContext;

	private TableModelDescription tableModelDescription;

	/**
	 *
	 * @param databindingContext
	 * @param outerUpdatableValue The master part of the master/detail relationship handled by this NestedUpdatable
	 * @param feature
	 * @param elementType 
	 */
	public NestedUpdatableCellProvider(IDataBindingContext databindingContext,
			final IUpdatableValue outerUpdatableValue, TableModelDescription tableModelDescription) {
		this.databindingContext = databindingContext;
		this.tableModelDescription = tableModelDescription;
		updateInnerUpdatable(outerUpdatableValue);
		IChangeListener outerChangeListener = new IChangeListener() {
			public void handleChange(ChangeEvent changeEvent) {
				// master has changed, update detail and fire "refresh" change event
				if ((changeEvent.getChangeType() &  (ChangeEvent.CHANGE | ChangeEvent.ADD | ChangeEvent.ADD_MANY | ChangeEvent.REMOVE | ChangeEvent.REMOVE_MANY)) != 0) {
					updateInnerUpdatable(outerUpdatableValue);
					readableSet.fireEvent(ChangeEvent.CHANGE, null, null, null, ChangeEvent.POSITION_UNKNOWN);
				}
			}
		};
		outerUpdatableValue.addChangeListener(outerChangeListener);
	}

	private void updateInnerUpdatable(IUpdatableValue outerUpdatableValue) {
		currentOuterValue = outerUpdatableValue.getValue();
		if (innerUpdatableCellProvider != null) {
			innerUpdatableCellProvider.removeChangeListener(innerCellChangeListener);
			innerUpdatableCellProvider.getReadableSet().removeChangeListener(innerSetChangeListener);
			innerUpdatableCellProvider.dispose();
		}
		if (currentOuterValue == null) {
			innerUpdatableCellProvider = null;
		} else {
			this.innerUpdatableCellProvider = (IUpdatableCellProvider) databindingContext
					.createUpdatable(new TableModelDescription(new Property(
							currentOuterValue, tableModelDescription.getCollectionProperty().getPropertyID()), tableModelDescription.getColumnIDs()));
			innerUpdatableCellProvider.addChangeListener(innerCellChangeListener);
			innerUpdatableCellProvider.getReadableSet().addChangeListener(innerSetChangeListener);
		}
	}

	public void dispose() {
		super.dispose();
		if (innerUpdatableCellProvider != null) {
			innerUpdatableCellProvider.dispose();
		}
		currentOuterValue = null;
		databindingContext = null;
		tableModelDescription = null;
		innerUpdatableCellProvider = null;
		innerCellChangeListener = null;
		innerSetChangeListener = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.IUpdatableCellProvider#getReadableSet()
	 */
	public IReadableSet getReadableSet() {
		return readableSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ICellProvider#getCellValue(java.lang.Object, int)
	 */
	public Object getCellValue(Object element, int index) {
		return innerUpdatableCellProvider.getCellValue(element, index);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ICellProvider#setCellValue(java.lang.Object, int, java.lang.Object)
	 */
	public void setCellValue(Object element, int index, Object value) {
		innerUpdatableCellProvider.setCellValue(element, index, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.EventSource#fireEvent(int, java.lang.Object, java.lang.Object, java.lang.Object, int)
	 */
	public ChangeEvent fireEvent(int changeType, Object oldValue, Object newValue, Object parent, int position) {
		// TODO Auto-generated method stub
		return fireChangeEvent(changeType, oldValue, newValue, parent, position);
	}

}
