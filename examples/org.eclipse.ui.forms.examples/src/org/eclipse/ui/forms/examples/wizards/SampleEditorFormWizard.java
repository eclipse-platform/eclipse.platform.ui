/*
 * Created on Oct 20, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SampleEditorFormWizard extends Wizard {
	public SampleEditorFormWizard() {
		setForcePreviousAndNextButtons(true);
		setDefaultPageImageDescriptor(ExamplesPlugin.getDefault().getImageRegistry().getDescriptor(ExamplesPlugin.IMG_WIZBAN));
		setWindowTitle("New Plug-in Project");
	}
	
	public void addPages() {
		addPage(new SampleEditorFormPage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("Processing...", 20);
				try {
					for (int i = 0; i < 20; i++) {
						Thread.currentThread().sleep(200);
						monitor.worked(1);
					}
					monitor.done();
				} catch (InterruptedException e) {
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		}
		catch (InvocationTargetException e) {
			System.out.println(e);
		}
		catch (InterruptedException e) {
			System.out.println(e);
		}
		return true;
	}
}
