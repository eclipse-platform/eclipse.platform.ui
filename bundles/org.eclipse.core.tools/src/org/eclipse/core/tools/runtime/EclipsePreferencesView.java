/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.runtime;

import java.util.ArrayList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.tools.CollapseAllAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;

public class EclipsePreferencesView extends ViewPart {

	private TreeViewer viewer;
	private IAction collapseAllAction;

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

		private IEclipsePreferences invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					invisibleRoot = Platform.getPreferencesService().getRootNode();
				return new Object[] {invisibleRoot};
			}
			return getChildren(parent);
		}

		public void dispose() {
			// do nothing
		}

		public Object getParent(Object child) {
			if (child instanceof IEclipsePreferences)
				return ((IEclipsePreferences) child).parent();
			return null;
		}

		public Object[] getChildren(Object parent) {
			ArrayList result = new ArrayList();
			if (parent instanceof IEclipsePreferences) {
				IEclipsePreferences node = (IEclipsePreferences) parent;
				try {
					String[] childrenNames = node.childrenNames();
					for (int i = 0; childrenNames != null && i < childrenNames.length; i++)
						result.add(node.node(childrenNames[i]));
					String[] keys = node.keys();
					for (int i = 0; i < keys.length; i++)
						result.add(keys[i] + '=' + node.get(keys[i], "")); //$NON-NLS-1$
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
			return result.toArray(new Object[result.size()]);
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof IEclipsePreferences)
				try {
					IEclipsePreferences node = (IEclipsePreferences) parent;
					return node.childrenNames().length != 0 || node.keys().length != 0;
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			return false;
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			String result = obj.toString();
			if (obj instanceof IEclipsePreferences) {
				IEclipsePreferences node = (IEclipsePreferences) obj;
				result = node.name();
				if (result.length() == 0)
					result = "<root>"; //$NON-NLS-1$
			}
			return result;
		}

		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof IEclipsePreferences)
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	public EclipsePreferencesView() {
		super();
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput(getViewSite());
		getViewSite().setSelectionProvider(viewer);
		IActionBars bars = getViewSite().getActionBars();
		collapseAllAction = new CollapseAllAction(viewer);
		bars.getToolBarManager().add(collapseAllAction);
		bars.updateActionBars();
	}

	public void dispose() {
		super.dispose();
		collapseAllAction = null;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}