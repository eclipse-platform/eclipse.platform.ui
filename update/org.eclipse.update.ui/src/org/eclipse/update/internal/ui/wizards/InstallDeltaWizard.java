/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.lang.reflect.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.custom.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.parts.*;

public class InstallDeltaWizard
	extends Wizard
	implements IInstallDeltaHandler {
	private ISessionDelta[] deltas;
	private InstallDeltaWizardPage page;
	private int processed = 0;

	/**
	 * Constructor for InstallDeltaWizard.
	 */
	public InstallDeltaWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(UpdateUI.getString("InstallDeltaWizard.wtitle")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_UPDATE_WIZ);
	}

	public void addPages() {
		page = new InstallDeltaWizardPage(deltas);
		addPage(page);
	}

	/**
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		final DeltaAdapter[] adapters = page.getDeltaAdapters();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
				try {
					doFinish(adapters, monitor);
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
			UpdateUI.logException(e);
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	private void analyzeAdapters(DeltaAdapter[] adapters, ArrayList selected, ArrayList removed) {
		for (int i=0; i<adapters.length; i++) {
			DeltaAdapter adapter = adapters[i];
			if (adapter.isRemoved())
				removed.add(adapter);
			else if (adapter.isSelected())
				selected.add(adapter);
		}
	}

	private void doFinish(
		DeltaAdapter [] adapters,
		IProgressMonitor monitor)
		throws CoreException {
			
		ArrayList selectedDeltas = new ArrayList();
		ArrayList removedDeltas = new ArrayList();
		analyzeAdapters(adapters, selectedDeltas, removedDeltas);
			
		monitor.beginTask(
			UpdateUI.getString("InstallDeltaWizard.processing"), //$NON-NLS-1$
			selectedDeltas.size() + removedDeltas.size());
		processed = 0;
		for (int i = 0; i < removedDeltas.size(); i++) {
			DeltaAdapter adapter = (DeltaAdapter)removedDeltas.get(i);
			ISessionDelta delta = adapter.getDelta();
			delta.delete();
			monitor.worked(1);
			if (monitor.isCanceled())
				return;
		}
		for (int i = 0; i < selectedDeltas.size(); i++) {
			DeltaAdapter adapter = (DeltaAdapter)selectedDeltas.get(i);
			ISessionDelta delta = adapter.getDelta();
			IFeatureReference [] refs = delta.getFeatureReferences();
			delta.process(refs, monitor);
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
						UpdateUI.getActiveWorkbenchShell(),
						InstallDeltaWizard.this);
				dialog.create();
				dialog.getShell().setSize(500, 500);
				dialog.open();
				if (processed > 0)
					UpdateUI.requestRestart();
			}
		});
	}
}