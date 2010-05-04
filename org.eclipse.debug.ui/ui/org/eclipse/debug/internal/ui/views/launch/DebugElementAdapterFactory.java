/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - support for alternative expression view content providers
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *     Wind Rvier Systems - added support for columns (bug 235646)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.debug.internal.ui.elements.adapters.AsynchronousDebugLabelAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultBreakpointsViewInput;
import org.eclipse.debug.internal.ui.elements.adapters.DefaultViewerInputProvider;
import org.eclipse.debug.internal.ui.elements.adapters.MemoryBlockContentAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.MemoryBlockLabelAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.MemoryRetrievalContentAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.MemorySegmentLabelAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.StackFrameSourceDisplayAdapter;
import org.eclipse.debug.internal.ui.elements.adapters.StackFrameViewerInputProvider;
import org.eclipse.debug.internal.ui.elements.adapters.VariableColumnFactoryAdapter;
import org.eclipse.debug.internal.ui.model.elements.BreakpointContainerLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.BreakpointContainerMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.BreakpointContentProvider;
import org.eclipse.debug.internal.ui.model.elements.BreakpointLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.BreakpointManagerContentProvider;
import org.eclipse.debug.internal.ui.model.elements.BreakpointManagerInputMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.BreakpointMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.DebugElementLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.DebugTargetContentProvider;
import org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider;
import org.eclipse.debug.internal.ui.model.elements.ExpressionLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.ExpressionManagerContentProvider;
import org.eclipse.debug.internal.ui.model.elements.ExpressionManagerMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.ExpressionMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.LaunchContentProvider;
import org.eclipse.debug.internal.ui.model.elements.LaunchManagerContentProvider;
import org.eclipse.debug.internal.ui.model.elements.MemoryBlockContentProvider;
import org.eclipse.debug.internal.ui.model.elements.MemoryBlockLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.MemoryRetrievalContentProvider;
import org.eclipse.debug.internal.ui.model.elements.MemoryViewElementMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.ProcessContentProvider;
import org.eclipse.debug.internal.ui.model.elements.RegisterGroupContentProvider;
import org.eclipse.debug.internal.ui.model.elements.RegisterGroupLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.RegisterGroupMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.StackFrameContentProvider;
import org.eclipse.debug.internal.ui.model.elements.StackFrameMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.ThreadContentProvider;
import org.eclipse.debug.internal.ui.model.elements.VariableContentProvider;
import org.eclipse.debug.internal.ui.model.elements.VariableEditor;
import org.eclipse.debug.internal.ui.model.elements.VariableLabelProvider;
import org.eclipse.debug.internal.ui.model.elements.VariableMementoProvider;
import org.eclipse.debug.internal.ui.model.elements.WatchExpressionEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementEditor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IAsynchronousLabelAdapter;
import org.eclipse.debug.internal.ui.viewers.update.DefaultModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.update.DefaultModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.views.memory.renderings.MemorySegment;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

/**
 * DebugElementAdapterFactory
 */
public class DebugElementAdapterFactory implements IAdapterFactory {
	
	private static IModelProxyFactory fgModelProxyFactoryAdapter = new DefaultModelProxyFactory();
	private static ISourceDisplay fgStackFrameSourceDisplayAdapter = new StackFrameSourceDisplayAdapter();
	private static IModelSelectionPolicyFactory fgModelSelectionPolicyFactoryAdapter = new DefaultModelSelectionPolicyFactory();
    
    private static IAsynchronousLabelAdapter fgDebugLabelAdapter = new AsynchronousDebugLabelAdapter();
    private static IAsynchronousLabelAdapter fgMemoryBlockLabelAdapter = new MemoryBlockLabelAdapter();
    private static IAsynchronousLabelAdapter fgTableRenderingLineLabelAdapter = new MemorySegmentLabelAdapter();
    
    private static IElementLabelProvider fgLPDebugElement = new DebugElementLabelProvider();
    private static IElementLabelProvider fgLPVariable = new VariableLabelProvider();
    private static IElementLabelProvider fgLPExpression = new ExpressionLabelProvider();
    private static IElementLabelProvider fgLPRegisterGroup = new RegisterGroupLabelProvider();
    private static IElementLabelProvider fgLPMemoryBlock = new MemoryBlockLabelProvider();     
    private static IElementLabelProvider fgLPBreakpoint = new BreakpointLabelProvider();
    private static IElementLabelProvider fgLPBreakpointContainer = new BreakpointContainerLabelProvider(); 
    private static IElementEditor fgEEVariable = new VariableEditor();
    private static IElementEditor fgEEWatchExpression = new WatchExpressionEditor();
    
    private static IAsynchronousContentAdapter fgAsyncMemoryRetrieval = new MemoryRetrievalContentAdapter();
    private static IAsynchronousContentAdapter fgAsyncMemoryBlock = new MemoryBlockContentAdapter();
    
    private static IElementContentProvider fgCPLaunchManger = new LaunchManagerContentProvider();
    private static IElementContentProvider fgCPLaunch = new LaunchContentProvider();
    private static IElementContentProvider fgCPProcess = new ProcessContentProvider();
    private static IElementContentProvider fgCPTarget = new DebugTargetContentProvider();
    private static IElementContentProvider fgCPThread = new ThreadContentProvider();
    private static IElementContentProvider fgCPFrame = new StackFrameContentProvider();
    private static IElementContentProvider fgCPVariable = new VariableContentProvider();
    private static IElementContentProvider fgCPExpressionManager = new ExpressionManagerContentProvider();  
    private static IElementContentProvider fgCPExpression = new ExpressionContentProvider();
    private static IElementContentProvider fgCPRegisterGroup = new RegisterGroupContentProvider();
    private static IElementContentProvider fgCPMemoryRetrieval = new MemoryRetrievalContentProvider();
    private static IElementContentProvider fgCPMemoryBlock = new MemoryBlockContentProvider();   
    private static IElementContentProvider fgCPBreakpointManager = new BreakpointManagerContentProvider();
    private static IElementContentProvider fgCPBreakpoint = new BreakpointContentProvider();
    
    private static IElementMementoProvider fgMPFrame = new StackFrameMementoProvider();
    private static IElementMementoProvider fgMPVariable = new VariableMementoProvider();
    private static IElementMementoProvider fgMPExpression = new ExpressionMementoProvider();
    private static IElementMementoProvider fgMPRegisterGroup = new RegisterGroupMementoProvider();
    private static IElementMementoProvider fgMPExpressionManager = new ExpressionManagerMementoProvider();
    private static IElementMementoProvider fgMPMemory = new MemoryViewElementMementoProvider();
    private static IElementMementoProvider fgMPBreakpointManagerInput = new BreakpointManagerInputMementoProvider();
    private static IElementMementoProvider fgMPBreakpointContainer = new BreakpointContainerMementoProvider();
    private static IElementMementoProvider fgMPBreakpoint = new BreakpointMementoProvider();
    
    private static IColumnPresentationFactory fgVariableColumnFactory = new VariableColumnFactoryAdapter();
    
    private static IViewerInputProvider fgDefaultViewerInputProvider = new DefaultViewerInputProvider();
    private static IViewerInputProvider fgStackFrameViewerInputProvider = new StackFrameViewerInputProvider();
    
    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType.isInstance(adaptableObject)) {
			return adaptableObject;
		}
        
        if (adapterType.equals(IAsynchronousContentAdapter.class)) {
            if (adaptableObject instanceof IMemoryBlockRetrieval) {
            	return fgAsyncMemoryRetrieval;
            }
            if (adaptableObject instanceof IMemoryBlock) {
            	return fgAsyncMemoryBlock;
            }
        }
        
        if (adapterType.equals(IElementContentProvider.class)) {
            if (adaptableObject instanceof ILaunchManager) {
                return fgCPLaunchManger;
            }
            if (adaptableObject instanceof ILaunch) {
                return fgCPLaunch;
            }
            if (adaptableObject instanceof IProcess) {
                return fgCPProcess;
            }
            if (adaptableObject instanceof IDebugTarget) {
            	return fgCPTarget;
            }
            if (adaptableObject instanceof IMemoryBlockRetrieval)
            {
            	return fgCPMemoryRetrieval;
            }
            if (adaptableObject instanceof IThread) {
            	return fgCPThread;
            }
            if (adaptableObject instanceof IStackFrame) {
            	return fgCPFrame;
            }
            if (adaptableObject instanceof IVariable) {
            	return fgCPVariable;
            }
            if (adaptableObject instanceof IExpressionManager) {
            	return fgCPExpressionManager;
            }           
            if (adaptableObject instanceof IExpression) {
            	return fgCPExpression;
            }
            if (adaptableObject instanceof IRegisterGroup) {
            	return fgCPRegisterGroup;
            }
            if (adaptableObject instanceof IMemoryBlock) {
            	return fgCPMemoryBlock;
            }            
            if (adaptableObject instanceof DefaultBreakpointsViewInput) {
            	return fgCPBreakpointManager;
            }
            if (adaptableObject instanceof IBreakpoint) {
            	return fgCPBreakpoint;
            }
        }        
        
        if (adapterType.equals(IAsynchronousLabelAdapter.class)) {
        	if (adaptableObject instanceof IMemoryBlock) {
        		return fgMemoryBlockLabelAdapter;
        	}
        	
        	if (adaptableObject instanceof MemorySegment) {
        		return fgTableRenderingLineLabelAdapter;
        	}
        	return fgDebugLabelAdapter;
        }
        
        if (adapterType.equals(IElementLabelProvider.class)) {
        	if (adaptableObject instanceof IVariable) {
        		return fgLPVariable;
        	}
        	if (adaptableObject instanceof IExpression) {
        		return fgLPExpression;
        	}
        	if (adaptableObject instanceof IRegisterGroup) {
        		return fgLPRegisterGroup;
        	}
        	if (adaptableObject instanceof IMemoryBlock) {
        		return fgLPMemoryBlock;
        	} 
        	if (adaptableObject instanceof IBreakpoint) {
        		return fgLPBreakpoint;
        	}
        	if (adaptableObject instanceof IBreakpointContainer) {
        		return fgLPBreakpointContainer;
        	}     	
        	return fgLPDebugElement;
        }        
        
        if (adapterType.equals(IModelProxyFactory.class)) {
        	if (adaptableObject instanceof ILaunch || adaptableObject instanceof IDebugTarget ||
        			adaptableObject instanceof IProcess || adaptableObject instanceof ILaunchManager ||
        			adaptableObject instanceof IStackFrame || adaptableObject instanceof IExpressionManager ||
        			adaptableObject instanceof IExpression || adaptableObject instanceof IMemoryBlockRetrieval ||
        			adaptableObject instanceof IMemoryBlock ||
        			adaptableObject instanceof DefaultBreakpointsViewInput ||
        			adaptableObject instanceof IBreakpoint ||
        			adaptableObject instanceof IBreakpointContainer)
        	return fgModelProxyFactoryAdapter;
        }
        
        if (adapterType.equals(ISourceDisplay.class)) {
        	if (adaptableObject instanceof IStackFrame) {
        		return fgStackFrameSourceDisplayAdapter;
        	}
        }
        
        if (adapterType.equals(IModelSelectionPolicyFactory.class)) {
        	if (adaptableObject instanceof IDebugElement) {
        		return fgModelSelectionPolicyFactoryAdapter;
        	}
        }
        
        if (adapterType.equals(IColumnPresentationFactory.class)) {
            if (adaptableObject instanceof IStackFrame || adaptableObject instanceof IExpressionManager) {
                return fgVariableColumnFactory;
            }
        }   
        
        if (adapterType.equals(IElementMementoProvider.class)) {
        	if (adaptableObject instanceof IStackFrame) {
        		return fgMPFrame;
        	}
        	if (adaptableObject instanceof IVariable) {
        		return fgMPVariable;
        	}
        	if (adaptableObject instanceof IRegisterGroup) {
        		return fgMPRegisterGroup;
        	}
        	if (adaptableObject instanceof IExpression) {
        		return fgMPExpression;
        	}
        	if (adaptableObject instanceof IExpressionManager) {
        		return fgMPExpressionManager;
        	}
        	if (adaptableObject instanceof IMemoryBlockRetrieval) {
        		return fgMPMemory;
        	}
        	if (adaptableObject instanceof IBreakpoint) {
        		return fgMPBreakpoint;
        	}
        	if (adaptableObject instanceof IBreakpointContainer) {
        		return fgMPBreakpointContainer;
        	}
        	if (adaptableObject instanceof DefaultBreakpointsViewInput) {
        		return fgMPBreakpointManagerInput;
        	}
        }
        
        if (adapterType.equals(IElementEditor.class)) {
        	if (adaptableObject instanceof IVariable) {
        		return fgEEVariable;
        	}
            if (adaptableObject instanceof IWatchExpression) {
                return fgEEWatchExpression;
            }
        }
        
        if (adapterType.equals(IViewerInputProvider.class)) {
        	if (adaptableObject instanceof IStackFrame) {
        		return fgStackFrameViewerInputProvider;
        	} else {
        	    return fgDefaultViewerInputProvider;
        	}
        }         
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList() {
        return new Class[] {
        		IAsynchronousLabelAdapter.class,
        		IAsynchronousContentAdapter.class,
        		IModelProxyFactory.class,
        		ISourceDisplay.class,
        		IModelSelectionPolicyFactory.class,
        		IColumnPresentationFactory.class,
        		IElementContentProvider.class,
        		IElementLabelProvider.class,
        		IElementMementoProvider.class,
        		IElementEditor.class,
        		IViewerInputProvider.class};
    }

}
