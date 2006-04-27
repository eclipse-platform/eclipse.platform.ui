package org.eclipse.jface.examples.databinding.radioGroup;

import org.eclipse.swt.events.SelectionEvent;

/**
 * Interface VetoableSelectionListener.  An interface for SelectionListeners
 * that permit the new selection to be vetoed before widgetSelected or
 * widgetDefaultSelected is called.
 */
public interface VetoableSelectionListener {
   /**
    * Method widgetCanChangeSelection.  Indicates that the selection is
    * about to be changed.  Setting e.doit to false will prevent the 
    * selection from changing.
    * 
    * @param e The SelectionEvent that is being processed.
    */
   public void canWidgetChangeSelection(SelectionEvent e);
}
