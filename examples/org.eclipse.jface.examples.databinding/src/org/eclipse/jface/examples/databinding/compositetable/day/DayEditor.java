package org.eclipse.jface.examples.databinding.compositetable.day;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.IRowConstructionListener;
import org.eclipse.jface.examples.databinding.compositetable.IRowContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class DayEditor extends Composite {
   private CompositeTable compositeTable = null;

   public DayEditor(Composite parent, int style) {
      super(parent, style);
      this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
      this.setLayout(new FillLayout());
   }

   /**
    * Method setTimeBreakdown.  Call this method exactly once after constructing
    * the day control in order to set the number of day columns to display.
    * <p>
    * This method may be executed exactly once.  Executing more than once will
    * result in undefined behavior.
    * 
    * @param numberOfColumns The number of columns to display.
    * @param numberOfDivisionsInHour 1 == one line per hour; 2 == every 1/2 hour; 4 = every 1/4 hour; etc...
    */
   public void setTimeBreakdown(int numberOfColumns, int numberOfDivisionsInHour) {
      if (numberOfDivisionsInHour < 1) {
         throw new IllegalArgumentException("There must be at least one division in the hour");
      }
      createCompositeTable(numberOfColumns, numberOfDivisionsInHour);
   }

   /**
    * This method initializes compositeTable	
    * 
    * @param numberOfColumns The number of day columns to display
    */
   private void createCompositeTable(final int numberOfColumns, final int numberOfDivisionsInHour) {
      compositeTable = new CompositeTable(this, SWT.NONE);
      compositeTable.setNumRowsInCollection(24*numberOfDivisionsInHour);
      compositeTable.setRunTime(true);
      compositeTable.addRowConstructionListener(new IRowConstructionListener() {
         public void rowConstructed(Control newRow) {
            Days days = (Days) newRow;
            days.setNumberOfColumns(numberOfColumns);
         }
      });
      compositeTable.addRowContentProvider(new IRowContentProvider() {
         Calendar calendar = new GregorianCalendar();

         public void refresh(CompositeTable sender, int currentObjectOffset, Control row) {
            calendar.set(Calendar.HOUR_OF_DAY, currentObjectOffset / numberOfDivisionsInHour);
            calendar.set(Calendar.MINUTE, (int)((double)currentObjectOffset%numberOfDivisionsInHour/numberOfDivisionsInHour*60));
            Days days = (Days) row;
            days.setCurrentTime(calendar.getTime());
         }
      });
      new Days(compositeTable, SWT.NONE);
   }
   
   
}  //  @jve:decl-index=0:visual-constraint="10,10"
