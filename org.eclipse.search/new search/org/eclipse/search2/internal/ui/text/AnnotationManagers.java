/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;

import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class AnnotationManagers {
	static {
		fgManagerMap = new HashMap();
		IWindowListener listener = new IWindowListener() {
			public void windowActivated(IWorkbenchWindow window) {
				// ignore
			}

			public void windowDeactivated(IWorkbenchWindow window) {
				// ignore
			}

			public void windowClosed(IWorkbenchWindow window) {
				disposeAnnotationManager(window);
			}

			public void windowOpened(IWorkbenchWindow window) {
				// ignore
			}
		};
		PlatformUI.getWorkbench().addWindowListener(listener);
	}

	private static HashMap fgManagerMap;


	private static void disposeAnnotationManager(IWorkbenchWindow window) {
		WindowAnnotationManager mgr = (WindowAnnotationManager) fgManagerMap.remove(window);
		if (mgr != null)
			mgr.dispose();
	}

	public static void searchResultActivated(IWorkbenchWindow window, AbstractTextSearchResult result) {
		WindowAnnotationManager mgr= (WindowAnnotationManager) fgManagerMap.get(window);
		if (mgr == null) {
			mgr= new WindowAnnotationManager(window);
			fgManagerMap.put(window, mgr);
		}
		mgr.setSearchResult(result);
	}

}