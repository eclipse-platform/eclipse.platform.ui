package org.eclipse.jface.examples.databinding.compositetable.day;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DayEditorTest {

   private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
   private DayEditor dayEditor = null;

   /**
    * This method initializes dayEditor	
    *
    */
   private void createDayEditor() {
      dayEditor = new DayEditor(sShell, SWT.NONE);
      dayEditor.setTimeBreakdown(7, 4);
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      // TODO Auto-generated method stub

      /* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
       * for the correct SWT library path in order to run with the SWT dlls. 
       * The dlls are located in the SWT plugin jar.  
       * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
       *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
       */
      Display display = Display.getDefault();
      DayEditorTest thisClass = new DayEditorTest();
      thisClass.createSShell();
      thisClass.sShell.open();
      while (!thisClass.sShell.isDisposed()) {
         if (!display.readAndDispatch())
            display.sleep();
      }
      display.dispose();
   }

   /**
    * This method initializes sShell
    */
   private void createSShell() {
      sShell = new Shell();
      sShell.setText("Day Editor Test");
      sShell.setLayout(new FillLayout());
      createDayEditor();
      sShell.setSize(new org.eclipse.swt.graphics.Point(800,592));
   }

}
