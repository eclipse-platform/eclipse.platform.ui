package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.PlatformUI;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.SWTUtil;

public class InstallDeltaWizard
	extends Wizard
	implements IInstallDeltaHandler {
	private static final String KEY_WTITLE = "InstallDeltaWizard.wtitle";
	private static final String KEY_PROCESSING =
		"InstallDeltaWizard.processing";
	private static final String KEY_RESTART_MESSAGE =
		"InstallDeltaWizard.restart.message";
	private ISessionDelta[] deltas;
	private InstallDeltaWizardPage page;
	private int processed = 0;

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
		final ISessionDelta[] removedDeltas = page.getRemovedDeltas();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				try {
					doFinish(selectedDeltas, removedDeltas, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			UpdateUIPlugin.logException(e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	private void doFinish(
		ISessionDelta[] selectedDeltas,
		ISessionDelta[] removedDeltas,
		IProgressMonitor monitor)
		throws CoreException {
		monitor.beginTask(
			UpdateUIPlugin.getResourceString(KEY_PROCESSING),
			selectedDeltas.length + removedDeltas.length);
		processed = 0;
		for (int i = 0; i < removedDeltas.length; i++) {
			ISessionDelta delta = removedDeltas[i];
			delta.delete();
			monitor.worked(1);
			if (monitor.isCanceled())
				return;
		}
		for (int i = 0; i < selectedDeltas.length; i++) {
			ISessionDelta delta = selectedDeltas[i];
			delta.process(monitor);
			monitor.worked(1);
			processed++;
			if (monitor.isCanceled())
				return;
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
		BusyIndicator.showWhile(SWTUtil.getStandardDisplay(), new Runnable() {
			public void run() {
				WizardDialog dialog =
					new WizardDialog(
						UpdateUIPlugin.getActiveWorkbenchShell(),
						InstallDeltaWizard.this);
				dialog.create();
				dialog.getShell().setSize(500, 500);
				dialog.open();
				if (processed > 0)
					UpdateUIPlugin.informRestartNeeded();
			}
		});
	}
}