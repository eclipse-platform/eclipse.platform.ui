package org.eclipse.jface.examples.databinding.mask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class EditMaskTest {

   public static void main(String[] args) {
      Display display = new Display();
      Shell shell = new Shell(display);

      Text text = new Text(shell, SWT.BORDER);
      text.setText("(999) 999-9999   ");
      new EditMask(text, "(###) ###-####");
      
      shell.setLayout(new RowLayout());
      shell.setSize(800, 600);
      shell.open();
      while (!shell.isDisposed()) {
         if (!display.readAndDispatch()) {
            display.sleep();
         }
      }
   }

}
