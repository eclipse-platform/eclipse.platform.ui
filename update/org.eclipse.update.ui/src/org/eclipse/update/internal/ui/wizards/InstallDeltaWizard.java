package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;

public class InstallDeltaWizard
	extends Wizard
	implements IInstallDeltaHandler {
	private static final String KEY_WTITLE = "InstallDeltaWizard.wtitle";
	private static final String KEY_PROCESSING = "InstallDeltaWizard.processing";
	private ISessionDelta[] deltas;
	private InstallDeltaWizardPage page;

	/**
	 * Constructor for InstallDeltaWizard.
	 */
	public InstallDeltaWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUIPlugin.getResourceString(KEY_WTITLE));
		setDefaultPageImageDescriptor(UpdateUIPluginImages.DESC_UPDATE_WIZ);
	}
	
	public void addPages() {
		page = new InstallDeltaWizardPage(deltas);
		addPage(page);
	}

	/**
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		final ISessionDelta[] selectedDeltas = page.getSelectedDeltas();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(selectedDeltas, monitor);
				}
				catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
				finally {
					monitor.done();
				}
			}
		};
		try {
		getContainer().run(true, false, op);
		}
		catch (InvocationTargetException e) {
			UpdateUIPlugin.logException(e);
			return false;
		}
		catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	private void doFinish(ISessionDelta [] selectedDeltas, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(UpdateUIPlugin.getResourceString(KEY_PROCESSING), selectedDeltas.length);
		for (int i=0; i<selectedDeltas.length; i++) {
			ISessionDelta delta = selectedDeltas[i];
			boolean doConfigure = true; //?? how do I know?
			delta.configureSessionDelta(doConfigure, monitor);
			monitor.worked(1);
		}
	}

	/**
	 * @see IInstallDeltaHandler#init(ISessionDelta[])
	 */
	public void init(ISessionDelta[] deltas) {
		this.deltas = deltas;
	}

	/**
	 * @see IInstallDeltaHandler#open()
	 */
	public void open() {
		WizardDialog dialog = new WizardDialog(UpdateUIPlugin.getActiveWorkbenchShell(), this);
		dialog.create();
		dialog.getShell().setSize(500, 500);
		dialog.getShell().setText("Pending Changes");
		dialog.open();
	}
}