
package org.eclipse.ant.internal.ui.antsupport.inputhandler;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
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
		Display.getDefault().dispose ();
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
		Display display = new Display();
		final String[] result = new String[1];
		final Shell dialog = new Shell(display, SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL | SWT.RESIZE);
		dialog.setLayout(new GridLayout());
		GridData gd= new GridData(SWT.FILL);
		gd.horizontalSpan= 2;
		dialog.setLayoutData(gd);
		dialog.setText(title);
		Label l= new Label(dialog, SWT.WRAP);
		l.setText(prompt);
		Text text = new Text (dialog, SWT.BORDER | SWT.V_SCROLL);
		//text.setBounds (10, 10, 200, 200);
		
		text.addListener (SWT.Verify, new Listener () {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent (Event e) {
				String currentText = e.text;
				result[0]= currentText;
				//request.setInput(currentText);
				//if (request.isInputValid()) {
					e.doit= true;
					return;
				}
			//}
		});
		
		final Button ok = new Button(dialog, SWT.PUSH);
		ok.setText("Ok"); //$NON-NLS-1$
		Button cancel = new Button(dialog, SWT.PUSH);
		cancel.setText("Cancel"); //$NON-NLS-1$
		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				//result[0] = event.widget == ok;
				dialog.close();
			}
		};
		ok.addListener(SWT.Selection, listener);
		cancel.addListener(SWT.Selection, listener);
		dialog.pack();
		dialog.open();

		while (!dialog.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}

		display.dispose();
	}
}