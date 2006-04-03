package org.eclipse.jface.examples.databinding.compositetable.day;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.IRowConstructionListener;
import org.eclipse.jface.examples.databinding.compositetable.IRowContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.TimeSlice;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * A DayEditor is an SWT control that can display events on a time line that can
 * span one or more days.
 * 
 * @since 3.2
 */
public class DayEditor extends Composite implements IEventEditor {
	/**
	 * The default start hour.  Normally 8:00 AM
	 */
	private static final int DEFAULT_START_HOUR = 8;
	private CompositeTable compositeTable = null;

	/**
	 * Constructor DayEditor.  Constructs a calendar control that can display
	 * events on one or more days.
	 * 
	 * @param parent
	 * @param style
	 */
	public DayEditor(Composite parent, int style) {
		super(parent, style);
		this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		this.setLayout(new FillLayout());
	}

	
	private CalendarableModel model = new CalendarableModel();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setTimeBreakdown(int, int)
	 */
	public void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour) {
		model.setTimeBreakdown(numberOfDays, numberOfDivisionsInHour);
		
		if (compositeTable != null) {
			compositeTable.dispose();
		}
		
		createCompositeTable(numberOfDays, numberOfDivisionsInHour);
	}

	/**
	 * This method initializes compositeTable
	 * 
	 * @param numberOfDays
	 *            The number of day columns to display
	 */
	private void createCompositeTable(final int numberOfDays,
			final int numberOfDivisionsInHour) {
		compositeTable = new CompositeTable(this, SWT.NONE);
		new TimeSlice(compositeTable, SWT.NONE);		// The prototype row
		
		compositeTable.setNumRowsInCollection( (24-startHour) * numberOfDivisionsInHour+1);
		compositeTable.setRunTime(true);
		
		compositeTable.addRowConstructionListener(new IRowConstructionListener() {
					public void rowConstructed(Control newRow) {
						TimeSlice days = (TimeSlice) newRow;
						days.setNumberOfColumns(numberOfDays);
					}
				});
		compositeTable.addRowContentProvider(new IRowContentProvider() {
			Calendar calendar = new GregorianCalendar();

			public void refresh(CompositeTable sender, int currentObjectOffset,
					Control row) {

				// Decrement currentObjectOffset for each all-day event line we need.
				--currentObjectOffset;
				
				TimeSlice timeSlice = (TimeSlice) row;
				if (currentObjectOffset < 0) {
					timeSlice.setCurrentTime(null);
				} else {
					calendar.set(Calendar.HOUR_OF_DAY, 
							computeHourFromRow(currentObjectOffset));
					calendar.set(Calendar.MINUTE,
							computeMinuteFromRow(currentObjectOffset));
					timeSlice.setCurrentTime(calendar.getTime());
				}
			}

			private int computeHourFromRow(int currentObjectOffset) {
				return currentObjectOffset
						/ numberOfDivisionsInHour + startHour;
			}

			private int computeMinuteFromRow(int currentObjectOffset) {
				int minute = (int) ((double) currentObjectOffset
						% numberOfDivisionsInHour
						/ numberOfDivisionsInHour * 60);
				return minute;
			}
		});
	}
	
	private int startHour = DEFAULT_START_HOUR;
	private int defaultStartHour = DEFAULT_START_HOUR;

	/**
	 * @return Returns the defaultStartHour.
	 */
	public int getDefaultStartHour() {
		return defaultStartHour;
	}

	/**
	 * @param defaultStartHour The defaultStartHour to set.
	 */
	public void setDefaultStartHour(int defaultStartHour) {
		this.defaultStartHour = defaultStartHour;
		startHour = defaultStartHour;	// temporary; used for layout purposes
		refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setDayEventCountProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider)
	 */
	public void setDayEventCountProvider(EventCountProvider eventCountProvider) {
		model.setDayEventCountProvider(eventCountProvider);
		refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setEventContentProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider)
	 */
	public void setEventContentProvider(EventContentProvider eventContentProvider) {
		model.setEventContentProvider(eventContentProvider);
		refresh();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setStartDate(java.util.Date)
	 */
	public void setStartDate(Date startDate) {
		model.setStartDate(startDate);
		refresh();
	}

	/**
	 * Refresh everything in the display.
	 */
	private void refresh() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#refresh(java.util.Date)
	 */
	public void refresh(Date date) {
		model.refresh(date);
		refresh();
	}


} // @jve:decl-index=0:visual-constraint="10,10"


