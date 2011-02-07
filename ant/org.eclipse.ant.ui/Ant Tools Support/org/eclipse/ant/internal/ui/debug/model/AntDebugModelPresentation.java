/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.debug.model;

import java.io.File;

import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.launching.debug.model.AntLineBreakpoint;
import org.eclipse.ant.internal.launching.debug.model.AntProperties;
import org.eclipse.ant.internal.launching.debug.model.AntProperty;
import org.eclipse.ant.internal.launching.debug.model.AntStackFrame;
import org.eclipse.ant.internal.launching.debug.model.AntThread;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.preferences.AntObjectLabelProvider;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.sourcelookup.containers.LocalFileStorage;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.icu.text.MessageFormat;

/**
 * Renders Ant debug elements
 */
public class AntDebugModelPresentation extends LabelProvider implements IDebugModelPresentationExtension {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attribute, Object value) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof AntProperty) {
			return AntObjectLabelProvider.getPropertyImage();
		} else if (element instanceof AntProperties) {
			return AntObjectLabelProvider.getPropertyImage();
        }
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof AntStackFrame) {
			AntStackFrame frame= (AntStackFrame) element;
			return getStackFrameText(frame);
		} else if (element instanceof AntThread) {
			AntThread thread= (AntThread) element;
			return getThreadText(thread);
		} else if (element instanceof AntProperty) {
		    AntProperty property= (AntProperty) element;
		    return property.getText();  
        } else if (element instanceof AntProperties) {
		   return ((AntProperties)element).getName();
        }
		
		return null;
	}

    private String getThreadText(AntThread thread) {
        String name= thread.getName();
        if (name != null) {
            StringBuffer text= new StringBuffer(name);
            if (thread.isSuspended()) {
                IBreakpoint[] breakpoints= thread.getBreakpoints();
                if (breakpoints.length > 0) {
                    AntLineBreakpoint breakpoint= (AntLineBreakpoint) breakpoints[0];
                    IMarker marker= breakpoint.getMarker();
                    String fileName= marker.getResource().getFullPath().lastSegment();
                    String lineNumber= Integer.toString(marker.getAttribute(IMarker.LINE_NUMBER, -1));
                    String breakpointString= null;
                    if (breakpoint.isRunToLine()) {
                        breakpointString= MessageFormat.format(DebugModelMessages.AntDebugModelPresentation_5, new String[] {lineNumber, fileName});
                    } else {
                        breakpointString= MessageFormat.format(DebugModelMessages.AntDebugModelPresentation_2, new String[]{lineNumber, fileName});                            
                    }
                    text.append(MessageFormat.format(DebugModelMessages.AntDebugModelPresentation_3, new String[]{breakpointString}));
                } else {
                    text.append(DebugModelMessages.AntDebugModelPresentation_4);
                }
            }
            
            return text.toString();
        }
        return null;
    }

    private String getStackFrameText(AntStackFrame frame) {
        String name= frame.getName();
        if (name != null) {
            StringBuffer text= new StringBuffer(name);
            int lineNumber= frame.getLineNumber();
            String lineNumberString= null;
            if (lineNumber == 0) {
                lineNumberString= DebugModelMessages.AntDebugModelPresentation_0;
            } else {
                lineNumberString= Integer.toString(lineNumber);
            }
            text.append(MessageFormat.format(DebugModelMessages.AntDebugModelPresentation_1, new String[]{lineNumberString}));
            return text.toString();
        }
        return null;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		String detail = IAntCoreConstants.EMPTY_STRING;
		try {
			detail = value.getValueString();
		} catch (DebugException e) {
		}
		listener.detailComputed(value, detail);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		if (element instanceof IFile) {
			return new FileEditorInput((IFile)element);
		}
		if (element instanceof ILineBreakpoint) {
			return new FileEditorInput((IFile)((ILineBreakpoint)element).getMarker().getResource());
		}
        if (element instanceof LocalFileStorage) {
        	File file= ((LocalFileStorage)element).getFile();
        	IFileStore fileStore;
			try {
				fileStore = EFS.getStore(file.toURI());
				return new FileStoreEditorInput(fileStore);
			} catch (CoreException e) {
				AntUIPlugin.log(e);
				return null;
			}
        }
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object element) {
		return "org.eclipse.ant.ui.internal.editor.AntEditor"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentationExtension#requiresUIThread(java.lang.Object)
	 */
	public boolean requiresUIThread(Object element) {
		return !AntUIImages.isInitialized();
	}
}
