/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.debug.internal.ui.views.registers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Memento of the expanded and selected items in a registers viewer.
 * 
 */
public class RegistersViewerState extends AbstractViewerState {

	public RegistersViewerState( TreeViewer viewer ) {
		super( viewer );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState#encodeElement(org.eclipse.swt.widgets.TreeItem)
	 */
	public IPath encodeElement( TreeItem item ) throws DebugException {
		Object obj = item.getData();
		String name = ( obj instanceof IRegisterGroup ) ? 
						((IRegisterGroup)obj).getName() : ((IVariable)obj).getName();
		IPath path = new Path( name );
		TreeItem parent = item.getParentItem();
		while( parent != null ) {
			obj = parent.getData();
			name = ( obj instanceof IRegisterGroup ) ? 
					 ((IRegisterGroup)obj).getName() : ((IVariable)obj).getName();
			path = new Path( name ).append( path );
			parent = parent.getParentItem();
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState#decodePath(org.eclipse.core.runtime.IPath, org.eclipse.jface.viewers.TreeViewer)
	 */
	public Object decodePath( IPath path, TreeViewer viewer ) throws DebugException {
		ITreeContentProvider contentProvider = (ITreeContentProvider)viewer.getContentProvider();
		String[] names = path.segments();
		Object parent = viewer.getInput();
		Object element = null;
		for( int i = 0; i < names.length; i++ ) {
			element = null;
			Object[] children = contentProvider.getChildren( parent );
			String name = names[i];
			for( int j = 0; j < children.length; j++ ) {
				if ( children[j] instanceof IRegisterGroup ) {	
					if ( name.equals( ((IRegisterGroup)children[j]).getName() ) ) {
						element = children[j];
						break;
					}
				}
				else if ( children[j] instanceof IVariable ) {	
					if ( name.equals( ((IVariable)children[j]).getName() ) ) {
						element = children[j];
						break;
					}
				}
			}
			if ( element == null ) {
				return null;
			} 
			parent = element;
		}
		return element;
	}
}
