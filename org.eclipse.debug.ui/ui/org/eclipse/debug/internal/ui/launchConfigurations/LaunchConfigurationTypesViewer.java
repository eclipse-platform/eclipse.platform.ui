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
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * A viewer that displays launch configuration types, filtered by
 * workbench capabilities.
 * 
 * @since 3.1
 */
public class LaunchConfigurationTypesViewer extends TableViewer {
	
	class ContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationTypes();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}

	/**
	 * Constructs a viewer to display launch configuration types
	 * 
	 * @param parent composite this viewer is contained in
	 * @param launchGroup the launch group being displayed
	 */
	public LaunchConfigurationTypesViewer(Composite parent, ILaunchGroup launchGroup) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		setContentProvider(new ContentProvider());
		setLabelProvider(DebugUITools.newDebugModelPresentation());
		setSorter(new ViewerSorter());
		addFilter(new LaunchGroupFilter(launchGroup));
		setInput(DebugPlugin.getDefault().getLaunchManager());
	}

}
