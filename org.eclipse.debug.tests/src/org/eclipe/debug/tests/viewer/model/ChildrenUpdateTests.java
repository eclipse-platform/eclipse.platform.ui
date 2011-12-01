/*******************************************************************************
 *  Copyright (c) 2007, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.viewers.model.ChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.ILabelUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.IInternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.TreeModelContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IStateUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Tests coalescing of children update requests.
 * 
 * @since 3.3
 */
public class ChildrenUpdateTests extends TestCase {
	
	class BogusModelContentProvider extends TreeModelContentProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.viewers.model.ModelContentProvider#getViewer()
		 */
		protected IInternalTreeModelViewer getViewer() {
			return new IInternalTreeModelViewer(){
			
				public void setSelection(ISelection selection) {}
				public void removeSelectionChangedListener(ISelectionChangedListener listener) {}
				public void addSelectionChangedListener(ISelectionChangedListener listener) {}
				public void updateViewer(IModelDelta delta) {}
				public void setSelection(ISelection selection, boolean reveal, boolean force) {}
				public void clearSelectionQuiet() {}
				public boolean trySelection(ISelection selection, boolean reveal, boolean force) { return true; }
				public void setInput(Object object) {}
				public void setAutoExpandLevel(int level) {}
				public boolean saveElementState(TreePath path, ModelDelta delta, int flags) { return true; }
				public void removeStateUpdateListener(IStateUpdateListener listener) {}
				public void removeViewerUpdateListener(IViewerUpdateListener listener) {}
				public void removeModelChangedListener(IModelChangedListener listener) {}
				public void removeLabelUpdateListener(ILabelUpdateListener listener) {}
                public void addViewerUpdateListener(IViewerUpdateListener listener) {}
                public void addStateUpdateListener(IStateUpdateListener listener) {}
                public void addModelChangedListener(IModelChangedListener listener) {}
                public void addLabelUpdateListener(ILabelUpdateListener listener) {}
                public void update(Object element) {}
                public void setHasChildren(Object elementOrTreePath, boolean hasChildren) {}
                public void setExpandedState(Object elementOrTreePath, boolean expanded) {}
                public void setChildCount(Object elementOrTreePath, int count) {}
                public void reveal(TreePath path, int index) {}
                public void replace(Object parentOrTreePath, int index, Object element) {}
                public void remove(Object parentOrTreePath, int index) {}
                public void remove(Object elementOrTreePath) {}
                public void refresh() {}
                public void refresh(Object element) {}
			
				public ISelection getSelection() {
					return null;
				}
			
				public IPresentationContext getPresentationContext() {
					return null;
				}
			
				public Object getInput() {
					return null;
				}
			
				public ViewerLabel getElementLabel(TreePath path, String columnId) {
					return null;
				}
			
				public Display getDisplay() {
					return DebugUIPlugin.getStandardDisplay();
				}
			
				public int getAutoExpandLevel() {
					return 0;
				}
			
			
				public boolean overrideSelection(ISelection current, ISelection candidate) {
					return false;
				}
			
				public void insert(Object parentOrTreePath, Object element, int position) {
				}
			
				public TreePath getTopElementPath() {
					return null;
				}
			
				public ViewerFilter[] getFilters() {
					return null;
				}

				public void addFilter(ViewerFilter filter) {}
				public void setFilters(ViewerFilter[] filters) {}
				
				public boolean getExpandedState(Object elementOrTreePath) {
					return false;
				}
			
				public Object getChildElement(TreePath path, int index) {
					return null;
				}
			
                public boolean getHasChildren(Object elementOrTreePath) {
                    return false;
                }
            
				public int getChildCount(TreePath path) {
					return 0;
				}
			
				public int findElementIndex(TreePath parentPath, Object element) {
					return 0;
				}
			
				public void expandToLevel(Object elementOrTreePath, int level) {
				}
			
				public void autoExpand(TreePath elementPath) {
				}

                public boolean getElementChildrenRealized(TreePath parentPath) {
                    return false;
                }
                
                public boolean getElementChecked(TreePath path) {
                    return false;
                }
                
                public boolean getElementGrayed(TreePath path) {
                    return false;
                }
                
                public void setElementChecked(TreePath path, boolean checked, boolean grayed) {
                }

                public TreePath[] getElementPaths(Object element) {
                    return null;
                }
                public void setElementData(TreePath path, int numColumns, String[] labels, ImageDescriptor[] images,
                    FontData[] fontDatas, RGB[] foregrounds, RGB[] backgrounds) {
                }
                public String[] getVisibleColumns() {
                    return null;
                }                
			};
		}
	}
	
	/**
	 * @param name
	 */
	public ChildrenUpdateTests(String name) {
		super(name);
	}
	
	protected TreeModelContentProvider getContentProvider() {
		return new BogusModelContentProvider();
	}
	
	/**
	 * Tests coalescing of requests
	 */
	public void testCoalesce () {
		Object element = new Object();
		TreeModelContentProvider cp = getContentProvider();
		ChildrenUpdate update1 = new ChildrenUpdate(cp, element, TreePath.EMPTY, element, 1, null);
		ChildrenUpdate update2 = new ChildrenUpdate(cp, element, TreePath.EMPTY, element, 2, null);
		assertTrue("Should coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 2, update1.getLength());
		
		update2 = new ChildrenUpdate(cp, element, TreePath.EMPTY, element, 3, null);
		assertTrue("Should coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 3, update1.getLength());
		
		update2 = new ChildrenUpdate(cp, element, TreePath.EMPTY, element, 2, null);
		assertTrue("Should coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 3, update1.getLength());		
		
		update2 = new ChildrenUpdate(cp, element, TreePath.EMPTY, element, 5, null);
		assertFalse("Should not coalesce", update1.coalesce(update2));
		assertEquals("Wrong offset", 1, update1.getOffset());
		assertEquals("Wrong length", 3, update1.getLength());
	}
}
