package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugEvent;import org.eclipse.debug.core.ILaunch;import org.eclipse.debug.core.model.IDebugElement;import org.eclipse.debug.core.model.IProcess;

/**
 * Provides the contents for a run mode of the Launches viewer.
 */
public class ProcessesContentProvider extends DebugContentProvider {
	
	/**
	 * @see BasicContentProvider#doHandleDebug(Event)
	 */
	public void doHandleDebugEvent(DebugEvent event) {
		Object element= event.getSource();
		if (element instanceof IDebugElement) {
			// the processes view is not interested in anything
			//other than launches and processes
			return;
		}
		switch (event.getKind()) {
			case DebugEvent.CREATE :
				insert(element);
				break;
			case DebugEvent.TERMINATE :
				Object parent= getParent(element);
				refresh(parent);
				updateButtons();
				break;
			case DebugEvent.CHANGE :
				refresh(element);
				updateButtons();
				break;
		}
	}

	/**
	 * @see ILaunchListener
	 */
	public void launchRegistered(final ILaunch launch) {
		Runnable r= new Runnable() {
			public void run() {
				if (!isDisposed()) {
					insert(launch);
					IProcess[] ps= launch.getProcesses();
					if (ps != null && ps.length > 0) {
						selectAndReveal(ps[0]);
					}
				}
			}
		};

		asyncExec(r);
	}
}

