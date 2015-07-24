/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.radioGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.examples.databinding.ducks.DuckType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * This object decorates a bunch of SWT.RADIO buttons and provides saner
 * selection semantics than you get by default with those radio buttons.
 * <p>
 * Its API is basically the same API as List, but with unnecessary methods
 * removed.
 */
public class RadioGroup {

   private final IRadioButton[] buttons;
   private final Object[] values;
   IRadioButton oldSelection = null;
   IRadioButton selectedButton = null;
   IRadioButton potentialNewSelection = null;

   /** (Non-API)
    * Interface IRadioButton.  A duck interface that is used internally by RadioGroup
    * and by RadioGroup's unit tests.
    */
   public static interface IRadioButton {
      void setData(String string, Object object);
      void addSelectionListener(SelectionListener selectionListener);
      void setSelection(boolean b);
      boolean getSelection();
      boolean isFocusControl();
      String getText();
      void setText(String string);
      void notifyListeners(int eventType, Event object);
   }

   /**
    * Constructs an instance of this widget given an array of Button objects to wrap.
    * The Button objects must have been created with the SWT.RADIO style bit set,
    * and they must all be in the same Composite.
    *
    * @param radioButtons Object[] an array of radio buttons to wrap.
    * @param values Object[] an array of objects corresponding to the value of each radio button.
    */
   public RadioGroup(Object[] radioButtons, Object[] values) {
      IRadioButton[] buttons = new IRadioButton[radioButtons.length];
      if (buttons.length < 1) {
         throw new IllegalArgumentException("A RadioGroup must manage at least one Button");
      }
      for (int i = 0; i < buttons.length; i++) {
         if (!DuckType.instanceOf(IRadioButton.class, radioButtons[i])) {
            throw new IllegalArgumentException("A radio button was not passed");
         }
         buttons[i] = (IRadioButton) DuckType.implement(IRadioButton.class, radioButtons[i]);
         buttons[i].setData(Integer.toString(i), new Integer(i));
         buttons[i].addSelectionListener(selectionListener);
      }
      this.buttons = buttons;
      this.values = values;
   }

   /**
    * Returns the object corresponding to the currently-selected radio button
    * or null if no radio button is selected.
    *
    * @return the object corresponding to the currently-selected radio button
    * or null if no radio button is selected.
    */
   public Object getSelection() {
      int selectionIndex = getSelectionIndex();
      if (selectionIndex < 0)
         return "";
      return values[selectionIndex];
   }

   /**
    * Sets the selected radio button to the radio button whose model object
    * equals() the object specified by newSelection.  If !newSelection.equals()
    * any model object managed by this radio group, deselects all radio buttons.
    *
    * @param newSelection A model object corresponding to one of the model
    * objects associated with one of the radio buttons.
    */
   public void setSelection(Object newSelection) {
      deselectAll();
      for (int i = 0; i < values.length; i++) {
         if (values[i].equals(newSelection)) {
            setSelection(i);
            return;
         }
      }
   }

   private SelectionListener selectionListener = new SelectionListener() {
      @Override
	public void widgetDefaultSelected(SelectionEvent e) {
         widgetSelected(e);
      }

      @Override
	public void widgetSelected(SelectionEvent e) {
         potentialNewSelection = getButton(e);
         if (! potentialNewSelection.getSelection()) {
            return;
         }
         if (potentialNewSelection.equals(selectedButton)) {
            return;
         }

         if (fireWidgetChangeSelectionEvent(e)) {
            oldSelection = selectedButton;
            selectedButton = potentialNewSelection;
            if (oldSelection == null) {
               oldSelection = selectedButton;
            }

            fireWidgetSelectedEvent(e);
         }
      }

      private IRadioButton getButton(SelectionEvent e) {
         // If the actual IRadioButton is a test fixture, then the test fixture can't
         // set e.widget, so the button object will be in e.data instead and a dummy
         // Widget will be in e.widget.
         if (e.data != null) {
            return (IRadioButton) e.data;
         }
         return (IRadioButton) DuckType.implement(IRadioButton.class, e.widget);
      }
   };

   private List widgetChangeListeners = new LinkedList();

   protected boolean fireWidgetChangeSelectionEvent(SelectionEvent e) {
      for (Iterator listenersIter = widgetChangeListeners.iterator(); listenersIter.hasNext();) {
         VetoableSelectionListener listener = (VetoableSelectionListener) listenersIter.next();
         listener.canWidgetChangeSelection(e);
         if (!e.doit) {
            rollbackSelection();
            return false;
         }
      }
      return true;
   }

   private void rollbackSelection() {
      Display.getCurrent().asyncExec(new Runnable() {
         @Override
		public void run() {
            potentialNewSelection.setSelection(false);
            selectedButton.setSelection(true);
//            selectedButton.notifyListeners(SWT.Selection, null);
         }
      });
   }


   /**
    * Adds the listener to the collection of listeners who will
    * be notified when the receiver's selection is about to change, by sending
    * it one of the messages defined in the <code>VetoableSelectionListener</code>
    * interface.
    * <p>
    * <code>widgetSelected</code> is called when the selection changes.
    * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
    * </p>
    *
    * @param listener the listener which should be notified
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    *
    * @see VetoableSelectionListener
    * @see #removeVetoableSelectionListener
    * @see SelectionEvent
    */
   public void addVetoableSelectionListener(VetoableSelectionListener listener) {
      widgetChangeListeners.add(listener);
   }

   /**
    * Removes the listener from the collection of listeners who will
    * be notified when the receiver's selection is about to change.
    *
    * @param listener the listener which should no longer be notified
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    *
    * @see VetoableSelectionListener
    * @see #addVetoableSelectionListener
    */
   public void removeVetoableSelectionListener(VetoableSelectionListener listener) {
      widgetChangeListeners.remove(listener);
   }


   private List widgetSelectedListeners = new ArrayList();

   protected void fireWidgetSelectedEvent(SelectionEvent e) {
      for (Iterator listenersIter = widgetSelectedListeners.iterator(); listenersIter.hasNext();) {
         SelectionListener listener = (SelectionListener) listenersIter.next();
         listener.widgetSelected(e);
      }
   }

   protected void fireWidgetDefaultSelectedEvent(SelectionEvent e) {
      fireWidgetSelectedEvent(e);
   }

   /**
    * Adds the listener to the collection of listeners who will
    * be notified when the receiver's selection changes, by sending
    * it one of the messages defined in the <code>SelectionListener</code>
    * interface.
    * <p>
    * <code>widgetSelected</code> is called when the selection changes.
    * <code>widgetDefaultSelected</code> is typically called when an item is double-clicked.
    * </p>
    *
    * @param listener the listener which should be notified
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    *
    * @see SelectionListener
    * @see #removeSelectionListener
    * @see SelectionEvent
    */
   public void addSelectionListener(SelectionListener listener) {
      widgetSelectedListeners.add(listener);
   }

   /**
    * Removes the listener from the collection of listeners who will
    * be notified when the receiver's selection changes.
    *
    * @param listener the listener which should no longer be notified
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    *
    * @see SelectionListener
    * @see #addSelectionListener
    */
   public void removeSelectionListener(SelectionListener listener) {
      widgetSelectedListeners.remove(listener);
   }

   /**
    * Deselects the item at the given zero-relative index in the receiver.
    * If the item at the index was already deselected, it remains
    * deselected. Indices that are out of range are ignored.
    *
    * @param index the index of the item to deselect
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public void deselect (int index) {
      if (index < 0 || index >= buttons.length)
         return;
      buttons[index].setSelection(false);
   }

   /**
    * Deselects all selected items in the receiver.
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public void deselectAll () {
      for (int i = 0; i < buttons.length; i++)
         buttons[i].setSelection(false);
   }

   /**
    * Returns the zero-relative index of the item which currently
    * has the focus in the receiver, or -1 if no item has focus.
    *
    * @return the index of the selected item
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public int getFocusIndex () {
      for (int i = 0; i < buttons.length; i++) {
         if (buttons[i].isFocusControl()) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Returns the item at the given, zero-relative index in the
    * receiver. Throws an exception if the index is out of range.
    *
    * @param index the index of the item to return
    * @return the item at the given index
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    *
    * FIXME: tck - this should be renamed to getItemText()
    */
   public String getItem (int index) {
      if (index < 0 || index >= buttons.length)
         SWT.error(SWT.ERROR_INVALID_RANGE, null, "getItem for a nonexistant item");
      return buttons[index].getText();
   }

   /**
    * Returns the number of items contained in the receiver.
    *
    * @return the number of items
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public int getItemCount () {
      return buttons.length;
   }

   /**
    * Returns a (possibly empty) array of <code>String</code>s which
    * are the items in the receiver.
    * <p>
    * Note: This is not the actual structure used by the receiver
    * to maintain its list of items, so modifying the array will
    * not affect the receiver.
    * </p>
    *
    * @return the items in the receiver's list
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public String [] getItems () {
      List itemStrings = new ArrayList();
      for (int i = 0; i < buttons.length; i++) {
         itemStrings.add(buttons[i].getText());
      }
      return (String[]) itemStrings.toArray(new String[itemStrings.size()]);
   }

   public Object[] getButtons() {
      return buttons;
   }

   /**
    * Returns the zero-relative index of the item which is currently
    * selected in the receiver, or -1 if no item is selected.
    *
    * @return the index of the selected item or -1
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public int getSelectionIndex () {
      for (int i = 0; i < buttons.length; i++) {
         if (buttons[i].getSelection() == true) {
            return i;
         }
      }
      return -1;
   }

  /**
    * Gets the index of an item.
    * <p>
    * The list is searched starting at 0 until an
    * item is found that is equal to the search item.
    * If no item is found, -1 is returned.  Indexing
    * is zero based.
    *
    * @param string the search item
    * @return the index of the item
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public int indexOf (String string) {
      for (int i = 0; i < buttons.length; i++) {
         if (buttons[i].getText().equals(string)) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Searches the receiver's list starting at the given,
    * zero-relative index until an item is found that is equal
    * to the argument, and returns the index of that item. If
    * no item is found or the starting index is out of range,
    * returns -1.
    *
    * @param string the search item
    * @param start the zero-relative index at which to start the search
    * @return the index of the item
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public int indexOf (String string, int start) {
      for (int i = start; i < buttons.length; i++) {
         if (buttons[i].getText().equals(string)) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Returns <code>true</code> if the item is selected,
    * and <code>false</code> otherwise.  Indices out of
    * range are ignored.
    *
    * @param index the index of the item
    * @return the visibility state of the item at the index
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public boolean isSelected (int index) {
      return buttons[index].getSelection();
   }

   /**
    * Selects the item at the given zero-relative index in the receiver's
    * list.  If the item at the index was already selected, it remains
    * selected. Indices that are out of range are ignored.
    *
    * @param index the index of the item to select
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public void select (int index) {
      if (index < 0 || index >= buttons.length)
         return;
      buttons[index].setSelection(true);
   }

   /**
    * Sets the text of the item in the receiver's list at the given
    * zero-relative index to the string argument. This is equivalent
    * to <code>remove</code>'ing the old item at the index, and then
    * <code>add</code>'ing the new item at that index.
    *
    * @param index the index for the item
    * @param string the new text for the item
    *
    * @exception IllegalArgumentException <ul>
    *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
    *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
    * </ul>
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    */
   public void setItem (int index, String string) {
      if (index < 0 || index >= buttons.length)
         SWT.error(SWT.ERROR_INVALID_RANGE, null, "setItem for a nonexistant item");
      buttons[index].setText(string);
   }

   /**
    * Selects the item at the given zero-relative index in the receiver.
    * If the item at the index was already selected, it remains selected.
    * The current selection is first cleared, then the new item is selected.
    * Indices that are out of range are ignored.
    *
    * @param index the index of the item to select
    *
    * @exception SWTException <ul>
    *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    * </ul>
    * @see List#deselectAll()
    * @see List#select(int)
    */
   public void setSelection (int index) {
      if (index < 0 || index > buttons.length - 1) {
         return;
      }
      buttons[index].setSelection(true);
   }

}
