package org.eclipse.update.examples;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.swt.widgets.MessageBox;

/**
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
  /**
   * The constructor.
   */
   public SampleAction() {
   }

  /**
   * @see IWorkbenchWindowActionDelegate#run
   */
   public void run(IAction arg0)  {
		MessageBox messageBox = new MessageBox(null);
		messageBox.setText(ExamplesPlugin.getResourceString("title"));
		messageBox.setMessage(ExamplesPlugin.getResourceString("msg"));
		messageBox.open();   		
   }

  /**
   * @see IWorkbenchWindowActionDelegate#selectionChanged
   */
   public void selectionChanged(IAction arg0, ISelection arg1)  {
   }

  /**
   * @see IWorkbenchWindowActionDelegate#dispose
   */
   public void dispose()  {
   }

  /**
   * @see IWorkbenchWindowActionDelegate#init
   */
   public void init(IWorkbenchWindow arg0)  {
   }
}
