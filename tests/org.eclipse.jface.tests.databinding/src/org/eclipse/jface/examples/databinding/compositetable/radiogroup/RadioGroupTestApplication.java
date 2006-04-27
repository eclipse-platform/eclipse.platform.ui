package org.eclipse.jface.examples.databinding.compositetable.radiogroup;

import org.eclipse.jface.examples.databinding.radioGroup.RadioGroup;
import org.eclipse.jface.examples.databinding.radioGroup.VetoableSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class RadioGroupTestApplication {
   public static void main(String[] args) {
      new RadioGroupTestApplication().run();
   }

   private Shell shell;
   
   private void run() {
      Display display = new Display();
      shell = new Shell(display);
      
      shell.setLayout(new RowLayout(SWT.VERTICAL));
      Button b1 = new Button(shell, SWT.RADIO);
      b1.setText("Button 1");
      Button b2 = new Button(shell, SWT.RADIO);
      b2.setText("Button 2");
      
      final Button b3 = new Button(shell, SWT.CHECK);
      b3.setText("Read-only");
      
      RadioGroup rg = new RadioGroup(new Object[] {b1, b2}, new Object[] {"1", "2"});
      rg.addSelectionListener(new SelectionListener() {
         public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
         }
         public void widgetSelected(SelectionEvent e) {
            Button b = (Button) e.widget;
            System.out.println("Selected " + b.getText());
         }
      });
      rg.addVetoableSelectionListener(new VetoableSelectionListener() {
         public void canWidgetChangeSelection(SelectionEvent e) {
            if (b3.getSelection()) {
               e.doit = false;
            }
         }});
      
      shell.setSize(100, 100);
      shell.open();
      
      while (!shell.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }
      display.dispose();
   }
}
