
package org.eclipse.ant.internal.ui.antsupport.inputhandler;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SWTInputHandler extends DefaultInputHandler {
	
	/* (non-Javadoc)
	 * @see org.apache.tools.ant.input.InputHandler#handleInput(org.apache.tools.ant.input.InputRequest)
	 */
	public void handleInput(InputRequest request) throws BuildException {
		if (System.getProperty("eclipse.ant.noInput") != null) { //$NON-NLS-1$
			throw new BuildException("Unable to respond to input request as a result of the user specified -noinput command"); //$NON-NLS-1$
		}
		BuildException[] problem= new BuildException[1];
		Runnable runnable= getHandleInputRunnable(request, problem);
		Display.getDefault().syncExec(runnable);
		if (problem[0] != null) {
			throw problem[0];
		}
	}
	
	protected Runnable getHandleInputRunnable(final InputRequest request, final BuildException[] problem) {
		return new Runnable() {
			public void run() {
				String prompt = getPrompt(request);
		       	String title= "Ant Input Request"; //$NON-NLS-1$
				open(title, prompt, request);
		
//				InputDialog dialog= new InputDialog(null, title, prompt, "", validator); //$NON-NLS-1$
//				if (dialog.open() != Window.OK) {
//					problem[0]= new BuildException(AntSupportMessages.getString("AntInputHandler.Unable_to_respond_to_<input>_request_4")); //$NON-NLS-1$
//				}
			}
		};
	}
	
	private void open(String title, String prompt, final InputRequest request) {
		Display display = new Display ();
		Shell shell = new Shell (display);
		shell.setLayout (new FillLayout ());
		shell.setText(title);
		Text text = new Text (shell, SWT.SINGLE | SWT.BORDER);
		
		text.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String currentText = e.text;
				request.setInput(currentText);
				if (request.isInputValid()) {
					e.doit = false;
					return;
				}
			}
		});
	
		shell.pack ();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) {
                display.sleep ();
            }
		}
		display.dispose ();
	}
}