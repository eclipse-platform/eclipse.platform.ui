package org.eclipse.jface.examples.databinding.compositetable.radiogroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.radioGroup.RadioGroup;
import org.eclipse.jface.examples.databinding.radioGroup.VetoableSelectionListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public class RadioGroupTest extends TestCase {
   
   private static final String NEW_STRING = "**New String**";
   private static final String BUTTON_2_DATA = "2";
   private static final String BUTTON_1_DATA = "1";
   private static final String BUTTON_2_TEXT = "Button 2";
   private static final String BUTTON_1_TEXT = "Button 1";
   private MockRadioButton b1;
   private MockRadioButton b2;
   private RadioGroup rg;
   
   List listenerFired;

   protected void setUp() throws Exception {
      super.setUp();
      
      b1 = new MockRadioButton();
      b1.setText(BUTTON_1_TEXT);
      b2 = new MockRadioButton();
      b2.setText(BUTTON_2_TEXT);
      b1.setSiblings(new MockRadioButton[] {b2});
      b2.setSiblings(new MockRadioButton[] {b1});
      rg = new RadioGroup(new Object[] {b1, b2}, new Object[] {BUTTON_1_DATA, BUTTON_2_DATA});
      
      listenerFired = new ArrayList();
   }
   
   public void test_getButtons() throws Exception {
      assertEquals(rg.getButtons()[0], b1);
      assertEquals(rg.getButtons()[1], b2);
   }
   
   public void test_deselect() throws Exception {
      b1.setSelection(true);
      assertTrue(b1.getSelection());
      rg.deselect(0);
      assertFalse(b1.getSelection());
   }

   public void test_mock_deselectBehavior() throws Exception {
      b1.setSelection(true);
      assertTrue(b1.getSelection());
      b2.setSelection(true);
      assertFalse(b1.getSelection());
      assertTrue(b2.getSelection());
   }
   
   public void test_deselectAll() throws Exception {
      b1.setSelection(true);
      assertTrue(b1.getSelection());
      rg.deselectAll();
      assertFalse(b1.getSelection());
      assertFalse(b2.getSelection());
   }
   
   public void test_getFocusIndex() throws Exception {
      b2.setSelection(true);
      assertEquals(1, rg.getFocusIndex());
   }
   
   public void test_getItem() throws Exception {
      assertEquals(b1.getText(), rg.getItem(0));
      assertEquals(b2.getText(), rg.getItem(1));
   }
   
   public void test_getItemCount() throws Exception {
      assertEquals(2, rg.getItemCount());
   }
   
   public void test_getSelection() throws Exception {
      b2.setSelection(true);
      assertEquals(BUTTON_2_DATA, rg.getSelection());
      b1.setSelection(true);
      assertEquals(BUTTON_1_DATA, rg.getSelection());
   }
   
   public void test_getSelectionIndex() throws Exception {
      b2.setSelection(true);
      assertEquals(1, rg.getSelectionIndex());
      b1.setSelection(true);
      assertEquals(0, rg.getSelectionIndex());
   }

   public void test_indexOfString() throws Exception {
      assertEquals(0, rg.indexOf(BUTTON_1_TEXT));
      assertEquals(1, rg.indexOf(BUTTON_2_TEXT));
      assertEquals(-1, rg.indexOf("Barf, please"));
   }
   
   public void test_indexOfStringInt() throws Exception {
      assertEquals(-1, rg.indexOf(BUTTON_1_TEXT, 1));
      assertEquals(1, rg.indexOf(BUTTON_2_TEXT, 1));
      assertEquals(-1, rg.indexOf("Barf, please", 1));
   }
   
   public void test_isSelected() throws Exception {
      b2.setSelection(true);
      assertFalse(rg.isSelected(0));
      assertTrue(rg.isSelected(1));
   }
   
   public void test_select() throws Exception {
      rg.select(0);
      assertTrue(b1.getSelection());
      assertFalse(b2.getSelection());
      rg.select(1);
      assertFalse(b1.getSelection());
      assertTrue(b2.getSelection());
   }
   
   public void test_setItem() throws Exception {
      rg.setItem(0, NEW_STRING);
      assertEquals(NEW_STRING, b1.getText());
   }
   
   public void test_setSelection_index() throws Exception {
      rg.setSelection(1);
      assertTrue(b2.getSelection());
      rg.setSelection(0);
      assertTrue(b1.getSelection());
   }

   public void test_setSelection_object() throws Exception {
      rg.setSelection(BUTTON_2_DATA);
      assertTrue(b2.getSelection());
      rg.setSelection(BUTTON_1_DATA);
      assertTrue(b1.getSelection());
   }

   public void test_listenerFired_allSelectable_newButtonSelected() throws Exception {
      rg.setSelection(0);
      SelectionListener rgListener = new MockSelectionListener(null);
      rg.addSelectionListener(rgListener);
      
      rg.setSelection(1);
      assertEquals(1, listenerFired.size());
      
      Object[] expectedListeners = new Object[] {b2};
      assertListenersFired(expectedListeners);
   }

   public void test_listenerFired_allSelectable_selectedButtonSelected() throws Exception {
      rg.setSelection(0);
      SelectionListener rgListener = new MockSelectionListener(null);
      rg.addSelectionListener(rgListener);
      
      rg.setSelection(0);
      assertEquals(0, listenerFired.size());
   }
   
   public void test_listenerFired_veto2_selectedButtonSelected() throws Exception {
      rg.setSelection(0);
      MockSelectionListener rgListener = new MockSelectionListener(b2);
      rg.addSelectionListener(rgListener);
      rg.addVetoableSelectionListener(rgListener);
      
      rg.setSelection(1);
      twiddleEventLoopPlease();
      assertEquals(0, listenerFired.size());
      
      assertEquals(0, rg.getSelectionIndex());
      
   }

   private void twiddleEventLoopPlease() {
      while (Display.getCurrent().readAndDispatch()) {/**/}
   }
   
   
   
   private void assertListenersFired(Object[] expectedListeners) {
      for (int i = 0; i < expectedListeners.length; i++) {
         Object object = expectedListeners[i];
         assertEquals(object, listenerFired.remove(0));
      }
   }
   
   final class MockSelectionListener implements SelectionListener, VetoableSelectionListener {
      private Object notSelectable = null;

      public MockSelectionListener(Object notSelectable) {
         this.notSelectable = notSelectable;
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
         listenerFired.add(e.data);
      }

      public void widgetSelected(SelectionEvent e) {
         listenerFired.add(e.data);
      }

      public void canWidgetChangeSelection(SelectionEvent e) {
         e.doit = !e.data.equals(notSelectable);
      }
   }

   private class MockRadioButton implements RadioGroup.IRadioButton {
      
      private MockRadioButton[] siblings = null;
      
      public void setSiblings(MockRadioButton[] siblings) {
         this.siblings = siblings;
      }
      
      private List listeners = new LinkedList();
      
      public void addSelectionListener(SelectionListener selectionListener) {
         listeners.add(selectionListener);
      }

      private void fireSelectionChangeEvent(boolean oldValue, boolean newValue) {
         for (Iterator iter = listeners.iterator(); iter.hasNext();) {
            SelectionListener listener = (SelectionListener) iter.next();
            Event event = new Event();
            event.widget = new Button(new Shell(), 0);
            event.data = this;
            listener.widgetSelected(new SelectionEvent(event));
         }
      }
      
      private boolean selection = false;
      
      public boolean getSelection() {
         return selection;
      }

      public void setSelection(boolean newValue) {
         
         // OS event; set any other selected button to de-selected
         if (newValue == true) {
            for (int i = 0; i < siblings.length; i++) {
               MockRadioButton mock = siblings[i];
               if (mock.selection) {
                  mock.setSelection(false);
               }
            }
         }
         
         boolean oldValue = this.selection;
         selection = newValue;
         fireSelectionChangeEvent(oldValue, newValue);
      }
      
      public boolean isFocusControl() {
         return selection;
      }

      public void setData(String string, Object object) {
         // NOOP
      }

      private String text;
      
      public String getText() {
         return text;
      }
      
      public void setText(String text) {
         this.text = text;
      }

      public void notifyListeners(int eventType, Event object) {
         fireSelectionChangeEvent(selection, selection);
      }
   }
   
}
