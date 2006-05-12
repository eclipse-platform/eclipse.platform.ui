/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day.binding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.AbstractObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;

/**
 * @since 3.2
 */
public class EventEditorObservableLazyDataRequestor extends AbstractObservable implements ILazyDataRequestor {

	private IEventEditor editor;
	private int modelSize;
	
	private DataBindingContext dbc;
	private String startTimePropertyName;	// Required; rest are optional
	private String endTimePropertyName = null;
	private String textPropertyName = null;
	private String toolTipTextPropertyName = null;
	private String imagePropertyName = null;
	
	/**
	 * @param editor
	 * @param dbc
	 * @param startTimePropertyName
	 * @param endTimePropertyName
	 * @param textPropertyName
	 * @param toolTipTextPropertyName
	 * @param imagePropertyName
	 */
	public EventEditorObservableLazyDataRequestor(IEventEditor editor,
			DataBindingContext dbc, String startTimePropertyName,
			String endTimePropertyName, String textPropertyName,
			String toolTipTextPropertyName, String imagePropertyName) {
		super();
		this.editor = editor;
		this.dbc = dbc;
		if (startTimePropertyName == null) {
			throw new IllegalArgumentException("Start time property description cannot be null");
		}
		this.startTimePropertyName = startTimePropertyName;
		this.endTimePropertyName = endTimePropertyName;
		this.textPropertyName = textPropertyName;
		this.toolTipTextPropertyName = toolTipTextPropertyName;
		this.imagePropertyName = imagePropertyName;
		
		editor.addItemInsertHandler(insertHandler);
		editor.addItemDeleteHandler(deleteHandler);
		editor.addItemDisposeHandler(itemDisposeHandler);
		editor.setEventCountProvider(eventCountProvider);
		editor.setEventContentProvider(eventContentProvider);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.AbstractObservable#dispose()
	 */
	public void dispose() {
		super.dispose();
		editor.removeItemInsertHandler(insertHandler);
		editor.removeItemDeleteHandler(deleteHandler);
		editor.removeItemDisposeHandler(itemDisposeHandler);
		editor.setEventCountProvider(null);
		editor.setEventContentProvider(null);
	}

	private IObservableValue createObservableIfNotNull(DataBindingContext dbc, Object target, String propertyName) {
		if (target == null) {
			throw new IllegalArgumentException("Target cannot be null");
		}
		if (propertyName == null) {
			return null;
		}
		return (IObservableValue) dbc.createObservable(new Property(target, propertyName));
	}

	private List elementProviders = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void addElementProvider(ILazyListElementProvider p) {
		elementProviders.add(p);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeElementProvider(org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider)
	 */
	public void removeElementProvider(ILazyListElementProvider p) {
		elementProviders.remove(p);
	}

	private Object fireElementProviders(int index) {
		for (Iterator epIter = elementProviders.iterator(); epIter.hasNext();) {
			ILazyListElementProvider p = (ILazyListElementProvider) epIter.next();
			Object result = p.get(index);
			if (result != null) {
				return result;
			}
		}
		throw new IndexOutOfBoundsException("Request for a nonexistent element");
	}
	
	private List insertDeleteProviders = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#addInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void addInsertDeleteProvider(LazyInsertDeleteProvider p) {
		insertDeleteProviders.add(p);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#removeInsertDeleteProvider(org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider)
	 */
	public void removeInsertDeleteProvider(LazyInsertDeleteProvider p) {
		insertDeleteProviders.remove(p);
	}

	private NewObject fireInsert(Object initializationData) {
		for (Iterator iter = insertDeleteProviders.iterator(); iter.hasNext();) {
			LazyInsertDeleteProvider p = (LazyInsertDeleteProvider) iter.next();
			NewObject result = p.insertElementAt(0, initializationData);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	private boolean fireDelete(int position) {
		for (Iterator iter = insertDeleteProviders.iterator(); iter.hasNext();) {
			LazyInsertDeleteProvider p = (LazyInsertDeleteProvider) iter.next();
			boolean result = p.deleteElementAt(position);
			if (result) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#add(int, java.lang.Object)
	 */
	public void add(int position, Object element) {
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#remove(int)
	 */
	public Object remove(int position) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#setSize(int)
	 */
	public void setSize(int size) {
		this.modelSize = size;
	}
	
	// Utility methods here ---------------------------------------------------
	
    public boolean isDateBetweenInclusive(Date testDate, Date startDate, Date endDate) {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(startDate);
    	GregorianCalendar startDatePortion = new GregorianCalendar();
    	startDatePortion.set(Calendar.YEAR, gc.get(Calendar.YEAR));
    	startDatePortion.set(Calendar.MONTH, gc.get(Calendar.MONTH));
    	startDatePortion.set(Calendar.DATE, gc.get(Calendar.DATE));
    	Date realStartDate = startDatePortion.getTime();
    	
    	GregorianCalendar endDatePortion = new GregorianCalendar();
    	endDatePortion.set(Calendar.YEAR, gc.get(Calendar.YEAR));
    	endDatePortion.set(Calendar.MONTH, gc.get(Calendar.MONTH));
    	endDatePortion.set(Calendar.DATE, gc.get(Calendar.DATE));
    	endDatePortion.add(Calendar.DATE, 1);
    	Date realEndDate = endDatePortion.getTime();
    	
    	boolean startDateOK = testDate.after(realStartDate) || testDate.equals(realStartDate);
		return startDateOK && testDate.before(realEndDate);
	}

    public Date getLastVisibleDate() {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(editor.getStartDate());
    	gc.add(Calendar.DATE, editor.getNumberOfDays()-1);
    	return gc.getTime();
    }
    
    private Date nextDay(Date refreshDate) {
    	return null;
    }
    
	private Date datePartOf(Date rawDate) {
		GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(rawDate);
    	gc.set(Calendar.HOUR_OF_DAY, 0);
    	gc.set(Calendar.MINUTE, 0);
    	gc.set(Calendar.SECOND, 0);
    	gc.set(Calendar.MILLISECOND, 0);
    	return gc.getTime();
	}

	private Date getBeginningDate(Object it) {
		IObservableValue dateObservable = (IObservableValue) dbc.createObservable(new Property(it, startTimePropertyName));
		return datePartOf((Date) dateObservable.getValue());
	}
	
	private Date getEndingDate(Object it) {
		if (endTimePropertyName == null) {
			return getBeginningDate(it);
		}
		IObservableValue dateObservable = (IObservableValue) dbc.createObservable(new Property(it, endTimePropertyName));
		return datePartOf((Date)dateObservable.getValue());
	}

	// Event handlers here ----------------------------------------------------

	private CalendarableItemEventHandler insertHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			NewObject newObject = fireInsert(e.calendarableItem);
			if (newObject == null) {
				e.doit = false;
				return;
			}
			Date firstDayOfEvent = getBeginningDate(newObject.it);
			Date lastDayOfEvent = getEndingDate(newObject.it);
			for (Date refreshDate = firstDayOfEvent; isDateBetweenInclusive(refreshDate, firstDayOfEvent, lastDayOfEvent); refreshDate = nextDay(refreshDate)) {
				editor.refresh(refreshDate);
			}
		}
	};

	private CalendarableItemEventHandler deleteHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			
		}
	};

	private CalendarableItemEventHandler itemDisposeHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			
		}
	};

	private EventCountProvider eventCountProvider = new EventCountProvider() {
		public int getNumberOfEventsInDay(Date day) {
			return 0;
		}
	};
	
	private EventContentProvider eventContentProvider = new EventContentProvider() {
		public void refresh(Date day, CalendarableItem[] controls) {
			
		}
	};
}



