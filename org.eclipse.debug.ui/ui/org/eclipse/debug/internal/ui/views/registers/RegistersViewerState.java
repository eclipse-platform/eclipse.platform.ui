/**********************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.debug.internal.ui.views.registers;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.debug.internal.ui.views.RemoteTreeViewer;
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
    
    public RegistersViewerState() {
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
            Object[] children = null;
            if (viewer instanceof RemoteTreeViewer) {
                children = ((RemoteTreeViewer) viewer).getCurrentChildren(parent);
            } else {
                children = contentProvider.getChildren(parent);
            }
            
            if (children == null)
            	return null;
            
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

    public AbstractViewerState copy() {
        RegistersViewerState copy = new RegistersViewerState();
        if (fSavedExpansion != null) {
            copy.fSavedExpansion = new ArrayList();
            for (Iterator iter = fSavedExpansion.iterator(); iter.hasNext();) {
                copy.fSavedExpansion.add(iter.next());
            }
        }
        
        if (fSelection != null) {
            copy.fSelection = new IPath[fSelection.length];
            for (int i = 0; i < fSelection.length; i++) {
                IPath sel = fSelection[i];
                copy.fSelection[i] = sel;
            }
        }
        return copy;
    }
}
