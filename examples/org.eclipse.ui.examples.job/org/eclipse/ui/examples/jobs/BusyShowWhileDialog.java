package org.eclipse.ui.examples.jobs;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.internal.progress.ProgressManager;
/**
 *BusyShowWhileDialog is a test of busyShowWhile in a modal dialog.
 */
public class BusyShowWhileDialog extends IconAndMessageDialog {
	/**
	 * @param parentShell
	 * @todo Generated comment
	 */
	public BusyShowWhileDialog(Shell parentShell) {
		super(parentShell);
		message = "Busy While Test";
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	protected Image getImage() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button detailsButton = createButton(parent, 4, "Start busy show while", false);
		detailsButton.addSelectionListener(new SelectionListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				try {
					ProgressManager.getInstance().busyCursorWhile(new IRunnableWithProgress() {
						/* (non-Javadoc)
						 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
						 */
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							long time = System.currentTimeMillis();
							long end = time + ProgressManager.LONG_OPERATION_MILLISECONDS
									+ ProgressManager.LONG_OPERATION_MILLISECONDS;
							while (end > System.currentTimeMillis()) {
								final Shell myShell = BusyShowWhileDialog.this.getShell();
								myShell.getDisplay().asyncExec(new Runnable() {
									/* (non-Javadoc)
									 * @see java.lang.Runnable#run()
									 */
									public void run() {
										if(myShell.isDisposed())
											return;
										myShell.getDisplay().sleep();
										myShell.setText(String.valueOf(System.currentTimeMillis()));
									}
								});
							}
						}
					});
				} catch (InvocationTargetException error) {
					error.printStackTrace();
				} catch (InterruptedException error) {
					error.printStackTrace();
				}
			}
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
}