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

package org.eclipse.jface.examples.databinding.compositetable.day.binding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableItemEventHandler;
import org.eclipse.jface.examples.databinding.compositetable.day.CalendarableSelectionChangeListener;
import org.eclipse.jface.examples.databinding.compositetable.day.NewEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.SelectionChangeEvent;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.ICalendarableItemControl;
import org.eclipse.jface.examples.databinding.compositetable.reflect.ReflectedProperty;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.jface.internal.databinding.provisional.BindSpec;
import org.eclipse.jface.internal.databinding.provisional.Binding;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.observable.AbstractObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor;
import org.eclipse.jface.internal.databinding.provisional.observable.ILazyListElementProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyDeleteEvent;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertDeleteProvider;
import org.eclipse.jface.internal.databinding.provisional.observable.LazyInsertEvent;
import org.eclipse.swt.SWT;

/**
 * An Observable for IEventEditor objects.
 * 
 * @since 3.2
 */
public class EventEditorObservableLazyDataRequestor extends AbstractObservable implements ILazyDataRequestor {

	private IEventEditor editor;
	private MultiDayEventCalendar multiDayEventCalendar = new MultiDayEventCalendar();
	protected int modelSize = 0;
	
	private DataBindingContext dbc;
	private String startTimePropertyName;	// Required; rest are optional
	private String endTimePropertyName = null;
	private String textPropertyName = null;
	private String toolTipTextPropertyName = null;
	private String imagePropertyName = null;
	private String allDayEventPropertyName = null;
	
	private class EventToCalendarableItem {
		/**
		 * a in the pair (a, b)
		 */
		public Object event;

		/**
		 * b in the pair (a, b)
		 */
		public CalendarableItem item;

		/**
		 * Construct a Pair(a, b)
		 * 
		 * @param a a in the pair (a, b)
		 * @param b b in the pair (a, b)
		 */
		public EventToCalendarableItem(Object a, CalendarableItem  b) {
			this.event = a;
			this.item = b;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj.getClass() != EventToCalendarableItem.class) {
				return false;
			}
			EventToCalendarableItem other = (EventToCalendarableItem) obj;
			return event.equals(other.event) && item.equals(other.item);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return event.hashCode() + item.hashCode();
		}
	}
	
	
	private class MultiDayEventCalendar {
		private Map daysToEventsMap;
		private List eventsList;
		
		public MultiDayEventCalendar() {
			flush();
		}
		
		public void flush() {
			daysToEventsMap = new TreeMap();
			eventsList = new LinkedList();
			for (int i=0; i < modelSize; ++i) {
				Object event = getModelElementAt(i);
				add(event);
			}
		}
		
		public void add(Object event) {
			eventsList.add(event);
			Date beginningDate = getBeginningDate(event);
			Date endingDate = getEndingDate(event);
			for (Date currentDate = beginningDate; 
				currentDate.before(endingDate); 
				currentDate = nextDay(currentDate)) 
			{
				addEventToMap(currentDate, event);
			}
		}

		private void addEventToMap(Object date, Object event) {
			List events = (List) daysToEventsMap.get(date);
			if (events == null) {
				events = new LinkedList();
				daysToEventsMap.put(date, events);
			}
			EventToCalendarableItem eventToCalenderable = new EventToCalendarableItem(event, null);
			events.add(eventToCalenderable);
			daysToEventsMap.put(date, events);
		}
		
		public Object remove(int position) {
			Object toRemove = eventsList.remove(position);
			Date beginningDate = getBeginningDate(toRemove);
			Date endingDate = getEndingDate(toRemove);
			for (Date currentDate = beginningDate; 
				currentDate.before(endingDate); 
				currentDate = nextDay(currentDate)) 
			{
				removeEventFromMap(currentDate, toRemove);
			}
			return toRemove;
		}
		
		private void removeEventFromMap(Date date, Object event) {
			List events = (List) daysToEventsMap.get(date);
			if (events == null) {
				// TODO: Log warning here?
				return;
			}
			for (Iterator eventsIter = events.iterator(); eventsIter.hasNext();) {
				EventToCalendarableItem eventToCalendarable = (EventToCalendarableItem) eventsIter.next();
				if (eventToCalendarable.event.equals(event)) {
					eventsIter.remove();
					break;
				}
			}
			events.remove(event);
			if (events.size() < 1) {
				daysToEventsMap.remove(date);
			}
		}
		
		public void update(EventDateTimeDiff diff, Object event) {
			Date oldStartDateTime = setToStartOfDay(diff.getOldStartDateTime());
			Date oldEndDateTime = incrementDay(setToStartOfDay(diff.getOldEndDateTime()), 1);
			for (Date currentDate = oldStartDateTime; 
				currentDate.before(oldEndDateTime); 
				currentDate = nextDay(currentDate)) 
			{
				removeEventFromMap(currentDate, event);
			}
			add(event);
		}

		private List get(Date date) {
			date = setToStartOfDay(date);
			return (List) daysToEventsMap.get(date);
		}
		
		public int indexOf(Object event) {
			return eventsList.indexOf(event);
		}
		
		public void setCalendarableSelection(Object event, boolean selected) {
			Date beginningDate = getBeginningDate(event);
			Date endingDate = getEndingDate(event);
			
			for (Date currentDate = beginningDate; 
				currentDate.before(endingDate); 
				currentDate = nextDay(currentDate)) 
			{
				List events = (List) daysToEventsMap.get(currentDate);
				
				if (events == null) {	// If we just deleted this event, return
					return;
				}
				
				for (Iterator eventsIter = events.iterator(); eventsIter.hasNext();) {
					EventToCalendarableItem eventToCalendarable = (EventToCalendarableItem) eventsIter.next();
					if (eventToCalendarable.event.equals(event)) {
						if (eventToCalendarable.item != null) {
							ICalendarableItemControl control = eventToCalendarable.item.getControl();
							if (control != null) {
								control.setSelected(selected);
							}
						}
						break;
					}
				}
			}
		}
		
		public int getNumberOfEventsInDay(Date day) {
			List dataForDate = multiDayEventCalendar.get(day);
			if (dataForDate == null) {
				return 0;
			}
			return dataForDate.size();
		}

		public void refresh(Date day, CalendarableItem[] items) {
			List dataForDate = multiDayEventCalendar.get(day);
			if (dataForDate == null) {
				return;
			}
			
			Iterator sourceEventIter = dataForDate.iterator();
			for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
				EventToCalendarableItem sourceEventPair = (EventToCalendarableItem) sourceEventIter.next();
				sourceEventPair.item = items[itemIndex];
				Object sourceEvent = sourceEventPair.event;
				Date startDate = getBeginningDate(sourceEvent);
				Date endDate = getEndingTime(sourceEvent);
				int dayWithinEvent = differenceInDays(day, startDate);
				int numberOfDaysInEvent = differenceInDays(endDate, startDate)+1;
				bindCalendarableItemProperties(items[itemIndex], sourceEvent, dayWithinEvent, numberOfDaysInEvent);
			}
		}
		
		/**
		 * Returns 11:59:999 PM of the ending date of the passed event.
		 * 
		 * @param it The event
		 * @return The ending of the last day
		 */
		protected Date getEndingDate(Object it) {
			ReflectedProperty property = new ReflectedProperty(it, endTimePropertyName);
			Date endingDate = (Date) property.get();
			Date endOfEndingDate = setToEndOfDay(endingDate);
			return endOfEndingDate;
		}

		private int differenceInDays(Date date, Date daysToSubtract) {
			long difference = date.getTime() - daysToSubtract.getTime();
			difference /= (1000*60*60*24);
			return (int) difference;
		}
		
	}
	
	/**
	 * @param d
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
		editor.addItemDisposeHandler(itemDisposeHandler);
		editor.addItemInsertHandler(insertHandler);
		editor.addItemDeleteHandler(deleteHandler);
		editor.addItemEditHandler(editHandler);
		editor.addSelectionChangeListener(selectionListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.AbstractObservable#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (editor == null) {
			return;
		}
		editor.removeItemInsertHandler(insertHandler);
		editor.removeItemDeleteHandler(deleteHandler);
		editor.removeItemDisposeHandler(itemDisposeHandler);
		editor.removeItemEditHandler(editHandler);
		editor.setEventCountProvider(null);
		editor.setEventContentProvider(null);
		editor = null;  // encourage the garbage collector to run... ;-)
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return false;
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

	private NewObject fireInsert(CalendarableItem initializationData) {
		for (Iterator iter = insertDeleteProviders.iterator(); iter.hasNext();) {
			LazyInsertDeleteProvider p = (LazyInsertDeleteProvider) iter.next();
			NewObject result = p.insertElementAt(new LazyInsertEvent(0, initializationData));
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	private boolean fireDelete(int position) {
		for (Iterator iter = insertDeleteProviders.iterator(); iter.hasNext();) {
			LazyInsertDeleteProvider p = (LazyInsertDeleteProvider) iter.next();
			LazyDeleteEvent e = new LazyDeleteEvent(position);
			boolean result = p.canDeleteElementAt(e);
			if (result) {
				p.deleteElementAt(e);
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
		multiDayEventCalendar.flush();
		editor.refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#add(int, java.lang.Object)
	 */
	public void add(int position, Object element) {
		multiDayEventCalendar.add(element);
		modelSize++;
		editor.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.ILazyDataRequestor#remove(int)
	 */
	public Object remove(int position) {
		Object removed = multiDayEventCalendar.remove(position);
		modelSize--;
		editor.refresh();
		return removed;
	}
	
	// Utility methods here ---------------------------------------------------
	
    protected Date incrementDay(Date initialDate, int increment) {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(initialDate);
    	gc.add(Calendar.DATE, increment);
    	return gc.getTime();
    }
    
    protected Date nextDay(Date initialDate) {
    	return incrementDay(initialDate, 1);
    }
    
	protected static Date setToStartOfDay(Date rawDate) {
		GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(rawDate);
    	gc.set(Calendar.HOUR_OF_DAY, 0);
    	gc.set(Calendar.MINUTE, 0);
    	gc.set(Calendar.SECOND, 0);
    	gc.set(Calendar.MILLISECOND, 0);
    	return gc.getTime();
	}

	/**
	 * Returns 12 AM of the beginning date of the passed event.
	 * 
	 * @param it The event
	 * @return The beginning of the start day.
	 */
	protected Date getBeginningDate(Object it) {
		ReflectedProperty property = new ReflectedProperty(it, startTimePropertyName);
		Date date = setToStartOfDay((Date) property.get());
		return date;
	}
	
	protected static Date setToEndOfDay(Date date) {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		gc.set(Calendar.HOUR_OF_DAY, 23);
		gc.set(Calendar.MINUTE, 59);
		gc.set(Calendar.SECOND, 59);
		gc.set(Calendar.MILLISECOND, 999);
		Date time = gc.getTime();
		return time;
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

	protected void bindCalendarableItemProperties(
			CalendarableItem item, 
			Object sourceElement,
			int eventPosition, 
			int eventLength) {

		// Optional bindings first...
		if (allDayEventPropertyName != null) {
			bindCalendarableItem(
					item, CalendarableItem.PROP_ALL_DAY_EVENT, 
					sourceElement, allDayEventPropertyName, null);
		}
		if (textPropertyName != null) {
			bindCalendarableItem(
					item, CalendarableItem.PROP_TEXT, 
					sourceElement, textPropertyName, null);
		}
		if (toolTipTextPropertyName != null) {
			bindCalendarableItem(
					item, CalendarableItem.PROP_TOOL_TIP_TEXT, 
					sourceElement, toolTipTextPropertyName, null);
		}
		if (imagePropertyName != null) {
			bindCalendarableItem(
					item, CalendarableItem.PROP_IMAGE, 
					sourceElement, imagePropertyName, null);
		}
		
		// Now the standard bindings...
		item.setContinued(SWT.NULL);
		if (eventLength == 1) {
			item.setDate(getBeginningDate(sourceElement));
			item.setStartTime(getBeginningTime(sourceElement));
			item.setEndTime(getEndingTime(sourceElement));
		} else { // multiday event
			if (eventPosition == 0) { // first day of event
				Date day = getBeginningDate(sourceElement);
				item.setDate(day);
				item.setStartTime(getBeginningTime(sourceElement));
				item.setEndTime(setToEndOfDay(day));
				if (!item.isAllDayEvent())
					item.setContinued(SWT.BOTTOM);
			} else if (eventPosition == eventLength - 1) { // last day of event
				Date beginningOfEndDay = setToStartOfDay(getEndingTime(sourceElement));
				item.setDate(beginningOfEndDay);
				item.setStartTime(beginningOfEndDay);
				item.setEndTime(getEndingTime(sourceElement));
				if (!item.isAllDayEvent())
					item.setContinued(SWT.TOP);
			} else { // in between first and last day of event
				Date day = incrementDay(getBeginningDate(sourceElement), eventPosition);
				Date startOfDay = setToStartOfDay(day);
				item.setDate(startOfDay);
				item.setStartTime(startOfDay);
				item.setEndTime(setToEndOfDay(day));
				if (!item.isAllDayEvent())
					item.setContinued(SWT.TOP | SWT.BOTTOM);
			}
		}
		item.setData(CalendarableItem.DATA_KEY, sourceElement);	
	}
	
	private void bindCalendarableItem(CalendarableItem item, String itemPropertyName, Object sourceElement, String sourcePropertyName, BindSpec bindSpec) {
		Binding binding = dbc.bind(new Property(item, itemPropertyName), 
				new Property(sourceElement, sourcePropertyName), bindSpec);
		List bindingList = (List) item.getData(CalendarableItem.BINDING_KEY);
		if (bindingList == null) {
			bindingList = new ArrayList();
			item.setData(CalendarableItem.BINDING_KEY, bindingList);
		}
		bindingList.add(binding);
	}

	private CalendarableItemEventHandler itemDisposeHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			List bindings = (List)e.calendarableItem.getData(CalendarableItem.BINDING_KEY);
			if (bindings != null) {
				for (Iterator bindingIter = bindings.iterator(); bindingIter.hasNext();) {
					Binding binding = (Binding) bindingIter.next();
					binding.dispose();
				}
			}
		}
	};
	
	private EventCountProvider eventCountProvider = new EventCountProvider() {
		public int getNumberOfEventsInDay(Date day) {
			return multiDayEventCalendar.getNumberOfEventsInDay(day);
		}
	};
	
	private EventContentProvider eventContentProvider = new EventContentProvider() {
		public void refresh(Date day, CalendarableItem[] items) {
			multiDayEventCalendar.refresh(day, items);
		}
	};
	
	private CalendarableItemEventHandler insertHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			NewObject newObject = fireInsert(e.calendarableItem);
			if (newObject == null) {
				e.doit = false;
				return;
			}
			multiDayEventCalendar.add(newObject.it);
			Date firstDayOfEvent = getBeginningTime(newObject.it);
			Date lastDayOfEvent = getEndingTime(newObject.it);
			e.result = new NewEvent(newObject.it, new Date[] {firstDayOfEvent, lastDayOfEvent});
		}
	};

	private CalendarableItemEventHandler deleteHandler = new CalendarableItemEventHandler() {
		public void handleRequest(CalendarableItemEvent e) {
			int objectToDelete = multiDayEventCalendar.indexOf(e.calendarableItem.getData(CalendarableItem.DATA_KEY));
			if (!fireDelete(objectToDelete)) {
				e.doit = false;
				return;
			}
			multiDayEventCalendar.remove(objectToDelete);
		}
	};

	private CalendarableItemEventHandler editHandler = new CalendarableItemEventHandler() {
		public void requestHandled(CalendarableItemEvent e) {
			if (e.result != null && e.doit) {
				multiDayEventCalendar.update((EventDateTimeDiff) e.result, 
						e.calendarableItem.getData(CalendarableItem.DATA_KEY));
			}
		}
	};
	
	private CalendarableSelectionChangeListener selectionListener = new CalendarableSelectionChangeListener() {
		public void selectionChanged(SelectionChangeEvent e) {
			if (e.oldSelection != null) {
				multiDayEventCalendar.setCalendarableSelection(e.oldSelection.getData(CalendarableItem.DATA_KEY), false);
			}
			if (e.newSelection != null) {
				multiDayEventCalendar.setCalendarableSelection(e.newSelection.getData(CalendarableItem.DATA_KEY), true);
			}
		}
	};

}



