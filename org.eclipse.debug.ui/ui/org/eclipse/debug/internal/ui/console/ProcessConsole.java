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
package org.eclipse.debug.internal.ui.console;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console for a system process
 * <p>
 * Clients may instantiate this class. This class is not intended for
 * sub-classing.
 * </p>
 * @since 3.0
 */
public class ProcessConsole implements IConsole {
	
	private IProcess fProcess = null;
	
	class ProcessConsoleLabelProvider extends LabelProvider implements IDebugEventSetListener {
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
		 */
		public void handleDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				if (event.getSource().equals(getProcess())) {
					DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
						public void run() {
							fireLabelProviderChanged(new LabelProviderChangedEvent(ProcessConsoleLabelProvider.this, ProcessConsole.this));
						}
					});
				}
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			DebugPlugin.getDefault().removeDebugEventListener(this);
			super.dispose();
		}

		public ProcessConsoleLabelProvider() {
			DebugPlugin.getDefault().addDebugEventListener(this);	
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return DebugUIPlugin.getDefaultLabelProvider().getImage(getProcess());
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return DebugUIPlugin.getDefaultLabelProvider().getText(getProcess());
		}

	}

	/**
	 * Proxy to a console document
	 */
	public ProcessConsole(IProcess process) {
		fProcess = process;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#createLabelProvider()
	 */
	public ILabelProvider createLabelProvider() {
		return new ProcessConsoleLabelProvider();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#createPage(org.eclipse.debug.internal.ui.console.IConsoleView)
	 */
	public IPageBookViewPage createPage(IConsoleView view) {
		return new ProcessConsolePage(view, this);
	}
		
	/**
	 * Returns the process associated with this console.
	 * 
	 * @return the process associated with this console
	 */
	public IProcess getProcess() {
		return fProcess;
	}

}
