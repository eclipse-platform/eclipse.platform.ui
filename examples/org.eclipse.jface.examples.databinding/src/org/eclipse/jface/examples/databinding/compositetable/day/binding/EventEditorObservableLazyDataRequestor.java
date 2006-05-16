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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler;
import org.eclipse.jface.examples.databinding.compositetable.reflect.ReflectedProperty;
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
import org.eclipse.swt.graphics.Image;

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
	private String allDayEventPropertyName = null;
	
	/**
	 * @param description
	 */
	public EventEditorObservableLazyDataRequestor(EventEditorBindingDescription d) {
		super();
		this.editor = d.editor;
		this.dbc = d.dbc;
		if (d.startTimePropertyName == null) {
			throw new IllegalArgumentException("Start time property description cannot be null");
		}
		this.startTimePropertyName = d.startTimePropertyName;
		this.endTimePropertyName = d.endTimePropertyName;
		this.allDayEventPropertyName = d.allDayEventPropertyName;
		this.textPropertyName = d.textPropertyName;
		this.toolTipTextPropertyName = d.toolTipTextPropertyName;
		this.imagePropertyName = d.imagePropertyName;
		
		editor.setEventCountProvider(eventCountProvider);
		editor.setEventContentProvider(eventContentProvider);
		editor.addItemInsertHandler(insertHandler);
		editor.addItemDeleteHandler(deleteHandler);
		editor.addItemDisposeHandler(itemDisposeHandler);
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return false;
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

	private Object getModelElementAt(int index) {
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
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#setSize(int)
	 */
	public void setSize(int size) {
		this.modelSize = size;
		editor.refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#add(int, java.lang.Object)
	 */
	public void add(int position, Object element) {
		// TODO
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#remove(int)
	 */
	public Object remove(int position) {
		// TODO Auto-generated method stub
		return null;
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
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(refreshDate);
    	gc.add(Calendar.DATE, 1);
    	return gc.getTime();
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
		ReflectedProperty property = new ReflectedProperty(it, startTimePropertyName);
		Date date = datePartOf((Date) property.get());
		return date;
	}
	
	private Date getBeginningTime(Object it) {
		ReflectedProperty property = new ReflectedProperty(it, startTimePropertyName);
		return ((Date) property.get());
	}
		
	private Date getEndingTime(Object it) {
		if (endTimePropertyName == null) {
			return getBeginningTime(it);
		}
		ReflectedProperty property = new ReflectedProperty(it, endTimePropertyName);
		return (Date)property.get();
	}
	
	// Event handlers here ----------------------------------------------------

	private Date currentlyFetching = null;
	private List dataForCurrentDate = null;
	private int currentOffset = 0;

	private boolean currentOffsetIsSame(Date day) {
		Object current = getModelElementAt(currentOffset);
		return getBeginningDate(current).equals(datePartOf(day));
	}

	private boolean currentOffsetIsAfter(Date day) {
		Object current = getModelElementAt(currentOffset);
		return getBeginningDate(current).after(datePartOf(day));
	}

	private boolean currentOffsetIsBefore(Date day) {
		Object current = getModelElementAt(currentOffset);
		return getBeginningDate(current).before(datePartOf(day));
	}
	
	protected void scrollToBeginningOfDay(Date day) {
		if (currentOffset >= modelSize) {
			currentOffset = modelSize-1;
		}
		if (currentOffset < 0) {
			currentOffset = 0;
		}
		
		while (currentOffsetIsBefore(day) && currentOffset < modelSize - 1) {
			++currentOffset;
		}
		while (currentOffsetIsAfter(day) && currentOffset > 0) {
			--currentOffset;
		}
		while (currentOffsetIsSame(day) && currentOffset > 0) {
			--currentOffset;
		}
		if (currentOffset < modelSize - 1) {
			++currentOffset;
		}
	}

	private void getDataForDate(Date day) {
		currentlyFetching = day;
		dataForCurrentDate = new LinkedList();
		
		if (modelSize < 1) {
			return;
		}
		
		scrollToBeginningOfDay(day);
		Object current = getModelElementAt(currentOffset);
		
		if (!getBeginningDate(current).equals(day)) {
			return;
		}
		
		while (getBeginningDate(current).equals(day)) {
			dataForCurrentDate.add(current);
			if (modelSize <= currentOffset) {
				break;
			}
			++currentOffset;
			if (currentOffset >= modelSize) {
				return;
			}
			current = getModelElementAt(currentOffset);
		}
	}

	private EventCountProvider eventCountProvider = new EventCountProvider() {
		public int getNumberOfEventsInDay(Date day) {
			if (currentlyFetching == null || !currentlyFetching.equals(day)) {
				getDataForDate(day);
			}
			return dataForCurrentDate.size();
		}
	};
	
	private Object getProperty(Object source, String propertyName) {
		if (propertyName != null) {
			return new ReflectedProperty(source, propertyName).get();
		}
		return null;
	}
	
	/**
	 * @param item
	 * @param sourceElement
	 */
	protected void setCalendarableItemProperties(CalendarableItem item, Object sourceElement) {
		item.setDate(getBeginningDate(sourceElement));
		// TODO : a lot of logic here to do.
		item.setStartTime(getBeginningTime(sourceElement));
		item.setEndTime(getEndingTime(sourceElement));

		Object data;
		data = getProperty(sourceElement, allDayEventPropertyName);
		if (data != null) {
			item.setAllDayEvent(((Boolean) data).booleanValue());
		} else {
			item.setAllDayEvent(false);
		}
		data = getProperty(sourceElement, textPropertyName);
		if (data != null) {
			item.setText((String)data);
		} else {
			item.setText("");
		}
		data = getProperty(sourceElement, toolTipTextPropertyName);
		if (data != null) {
			item.setToolTipText((String)data);
		} else {
			item.setToolTipText("");
		}
		data = getProperty(sourceElement, imagePropertyName);
		if (data != null) {
			item.setImage((Image)data);
		} else {
			item.setImage(null);
		}
	}

	private EventContentProvider eventContentProvider = new EventContentProvider() {
		public void refresh(Date day, CalendarableItem[] items) {
			if (currentlyFetching == null || !currentlyFetching.equals(day)) {
				getDataForDate(day);
			}
			
			Iterator source = dataForCurrentDate.iterator();
			for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
				Object sourceElement = source.next();
				setCalendarableItemProperties(items[itemIndex], sourceElement);
			}
		}
	};
	
	private CalendarableItemEventHandler itemDisposeHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			
		}
	};

	private CalendarableItemEventHandler insertHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
//			NewObject newObject = fireInsert(e.calendarableItem);
//			if (newObject == null) {
//				e.doit = false;
//				return;
//			}
//			Date firstDayOfEvent = getBeginningDate(newObject.it);
//			Date lastDayOfEvent = getEndingDate(newObject.it);
//			for (Date refreshDate = firstDayOfEvent; isDateBetweenInclusive(refreshDate, firstDayOfEvent, lastDayOfEvent); refreshDate = nextDay(refreshDate)) {
//				editor.refresh(refreshDate);
//			}
		}
	};

	private CalendarableItemEventHandler deleteHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			
		}
	};

}



