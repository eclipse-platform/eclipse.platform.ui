/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.debug.ui.IInstructionPointerPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * A proxy to an IDebugModelPresentation extension. Instantiates the extension
 * when it is needed.
 */

public class LazyModelPresentation implements IDebugModelPresentation, IDebugEditorPresentation, 
	IColorProvider, IFontProvider, IInstructionPointerPresentation, IDebugModelPresentationExtension {
	
	/**
	 * A temporary mapping of attribute ids to their values
	 * @see IDebugModelPresentation#setAttribute
	 */
	protected HashMap fAttributes= new HashMap(3);

	/**
	 * The config element that defines the extension
	 */
	protected IConfigurationElement fConfig = null;
	
	/**
	 * The actual presentation instance - null until called upon
	 */
	protected IDebugModelPresentation fPresentation = null;
	
	/**
	 * Temp holding for listeners - we do not add to presentation until
	 * it needs to be instantiated.
	 */
	protected ListenerList fListeners= new ListenerList();	
	
	/**
	 * Non-null when nested inside a delegating model presentation
	 */
	private DelegatingModelPresentation fOwner = null;
	
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#removeAnntations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IThread)
	 */
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IDebugEditorPresentation) {
			((IDebugEditorPresentation)presentation).removeAnnotations(editorPart, thread);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#addAnnotations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IDebugEditorPresentation) {
			return ((IDebugEditorPresentation)presentation).addAnnotations(editorPart, frame);
		}
		return false;
	}

	/**
	 * Constructs a lazy presentation from the config element.
	 */
	public LazyModelPresentation(IConfigurationElement configElement) {
		fConfig = configElement;
	}
	
	/**
	 * Constructs a lazy presentation from the config element, owned by the specified
	 * delegating model presentation.
	 * 
	 * @param parent owning presentation
	 * @param configElement XML configuration element
	 */
	public LazyModelPresentation(DelegatingModelPresentation parent, IConfigurationElement configElement) {
		this(configElement);
		fOwner = parent;
	}	

	/**
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object element) {
		initImageRegistry();
		Image image = getPresentation().getImage(element);
        if (image == null) {
            image = getDefaultImage(element);
        }
        if (image != null) {
            int flags= computeAdornmentFlags(element);
            if (flags > 0) {
                CompositeDebugImageDescriptor descriptor= new CompositeDebugImageDescriptor(image, flags);
                return DebugUIPlugin.getImageDescriptorRegistry().get(descriptor);
            }
        }
        return image;
	}
	
	/**
	 * Initializes the image registry
	 */
	private synchronized void initImageRegistry() {
		if (!DebugPluginImages.isInitialized()) {
			DebugUIPlugin.getDefault().getImageRegistry();
		}
	}

	/**
     * Computes and return common adornment flags for the given element.
     * 
     * @param element
     * @return adornment flags defined in CompositeDebugImageDescriptor
     */
    private int computeAdornmentFlags(Object element) {
        if (element instanceof IBreakpoint) {
            if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
                return CompositeDebugImageDescriptor.SKIP_BREAKPOINT;
            }
        }
        return 0;
    }

    /**
     * Returns a default text label for the debug element
     */
    protected String getDefaultText(Object element) {
        return DebugUIPlugin.getDefaultLabelProvider().getText(element);
    }

    /**
     * Returns a default image for the debug element
     */
    protected Image getDefaultImage(Object element) {
        return DebugUIPlugin.getDefaultLabelProvider().getImage(element);
    }
    
    /**
	 * @see IDebugModelPresentation#getText(Object)
	 */
	public String getText(Object element) {
        if (!(element instanceof IndexedVariablePartition)) {
            // Attempt to delegate        
            String text = getPresentation().getText(element);
            if (text != null) {
                return text;
            }
        }
        // If no delegate returned a text label, use the default
        if (showVariableTypeNames()) {
            try {
                if (element instanceof IExpression) {
                    StringBuffer buf = new StringBuffer();
                    IValue value = ((IExpression)element).getValue();
                    if (value != null) {
                        String type = value.getReferenceTypeName();
                        if (type != null && type.length() > 0) {
                        	buf.append(type);
                        	buf.append(' ');
                        }
                    }
                    buf.append(getDefaultText(element));
                    return buf.toString(); 
                } else if (element instanceof IVariable) {
                    return new StringBuffer(((IVariable)element).getValue().getReferenceTypeName()).append(' ').append(getDefaultText(element)).toString();
                }
            } catch (DebugException de) {
                DebugUIPlugin.log(de);
            }
        }
        return getDefaultText(element);
	}
	
    /**
     * Whether or not to show variable type names.
     * This option is configured per model presentation.
     * This allows this option to be set per view, for example.
     */
    protected boolean showVariableTypeNames() {
        Boolean show= (Boolean) fAttributes.get(DISPLAY_VARIABLE_TYPE_NAMES);
        show= show == null ? Boolean.FALSE : show;
        return show.booleanValue();
    }
    
	/**
	 * @see IDebugModelPresentation#computeDetail(IValue, IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		getPresentation().computeDetail(value, listener);
	}	
	
	/**
	 * @see ISourcePresentation#getEditorInput(Object)
	 */
	public IEditorInput getEditorInput(Object element) {
		return getPresentation().getEditorInput(element);
	}
	
	/**
	 * @see ISourcePresentation#getEditorId(IEditorInput, Object)
	 */
	public String getEditorId(IEditorInput input, Object inputObject) {
		return getPresentation().getEditorId(input, inputObject);
	}

	/**
	 * @see IBaseLabelProvider#addListener(ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		if (fPresentation != null) {
			getPresentation().addListener(listener);
		}
		fListeners.add(listener);
	}

	/**
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (fPresentation != null) {
			getPresentation().dispose();
		}
		fListeners = null;
	}

	/**
	 * @see IBaseLabelProvider#isLabelProperty(Object, String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		if (fPresentation != null) {
			return getPresentation().isLabelProperty(element, property);
		} 
		return false;
	}

	/**
	 * @see IBaseLabelProvider#removeListener(ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		if (fPresentation != null) {
			getPresentation().removeListener(listener);
		}
		ListenerList listeners = fListeners;
		if (listeners != null) {
		    listeners.remove(listener);
		}
	}
	
	/**
	 * Returns the real presentation, instantiating if required.
	 */
	protected IDebugModelPresentation getPresentation() {
		if (fPresentation == null) {
		    synchronized (this) {
		        if (fPresentation != null) {
		            // In the case that the synchronization is enforced, the "blocked" thread
		            // should return the presentation configured by the "owning" thread.
		            return fPresentation;
		        }
				try {
					IDebugModelPresentation tempPresentation= (IDebugModelPresentation) DebugUIPlugin.createExtension(fConfig, "class"); //$NON-NLS-1$
					// configure it
					if (fListeners != null) {
						Object[] list = fListeners.getListeners();
						for (int i= 0; i < list.length; i++) {
						    tempPresentation.addListener((ILabelProviderListener)list[i]);
						}
					}
					Iterator keys= fAttributes.keySet().iterator();
					while (keys.hasNext()) {
						String key= (String)keys.next();
						tempPresentation.setAttribute(key, fAttributes.get(key));
					}
					// Only assign to the instance variable after it's been configured. Otherwise,
					// the synchronization is defeated (a thread could return the presentation before
					// it's been configured).
					fPresentation= tempPresentation;
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
		    }
		}
		return fPresentation;
	}

	/**
	 * @see IDebugModelPresentation#setAttribute(String, Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		if (fPresentation != null) {
			getPresentation().setAttribute(id, value);
		}

		fAttributes.put(id, value);
		
		if (fOwner != null) {
			fOwner.basicSetAttribute(id, value);
		}
	}
	
	/**
	 * Returns the identifier of the debug model this
	 * presentation is registered for.
	 */
	public String getDebugModelIdentifier() {
		return fConfig.getAttribute("id"); //$NON-NLS-1$
	}
	
	/**
	 * Returns a new source viewer configuration for the details
	 * area of the variables view, or <code>null</code> if
	 * unspecified.
	 * 
	 * @return source viewer configuration or <code>null</code>
	 * @exception CoreException if unable to create the specified
	 * 	source viewer configuration
	 */
	public SourceViewerConfiguration newDetailsViewerConfiguration() throws CoreException {
		String attr  = fConfig.getAttribute("detailsViewerConfiguration"); //$NON-NLS-1$
		if (attr != null) {
			return (SourceViewerConfiguration)fConfig.createExecutableExtension("detailsViewerConfiguration"); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Returns a copy of the attributes in this model presentation.
	 * 
	 * @return a copy of the attributes in this model presentation
	 * @since 3.0
	 */
	public Map getAttributeMap() {
		return (Map) fAttributes.clone();
	}
	
	/**
	 * Returns the raw attribute map
	 * @return the raw attribute map
	 */
	public Map getAttributes() {
		return fAttributes;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        IDebugModelPresentation presentation = getPresentation();
        if (presentation instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) presentation;
            return colorProvider.getForeground(element);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        IDebugModelPresentation presentation = getPresentation();
        if (presentation instanceof IColorProvider) {
            IColorProvider colorProvider = (IColorProvider) presentation;
            return colorProvider.getBackground(element);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        IDebugModelPresentation presentation = getPresentation();
        if (presentation instanceof IFontProvider) {
            IFontProvider fontProvider = (IFontProvider) presentation;
            return fontProvider.getFont(element);
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getInstructionPointerAnnotation(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public Annotation getInstructionPointerAnnotation(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IInstructionPointerPresentation) {
			IInstructionPointerPresentation pointerPresentation = (IInstructionPointerPresentation) presentation;
			return pointerPresentation.getInstructionPointerAnnotation(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getMarkerAnnotationSpecificationId(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public String getInstructionPointerAnnotationType(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IInstructionPointerPresentation) {
			IInstructionPointerPresentation pointerPresentation = (IInstructionPointerPresentation) presentation;
			return pointerPresentation.getInstructionPointerAnnotationType(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getInstructionPointerImage(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public Image getInstructionPointerImage(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IInstructionPointerPresentation) {
			IInstructionPointerPresentation pointerPresentation = (IInstructionPointerPresentation) presentation;
			return pointerPresentation.getInstructionPointerImage(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getInstructionPointerText(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public String getInstructionPointerText(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IInstructionPointerPresentation) {
			IInstructionPointerPresentation pointerPresentation = (IInstructionPointerPresentation) presentation;
			return pointerPresentation.getInstructionPointerText(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentationExtension#requiresUIThread(java.lang.Object)
	 */
	public boolean requiresUIThread(Object element) {
		if (!DebugPluginImages.isInitialized()) {
			// need UI thread for breakpoint adornment and default images
			return true;
		}
		IDebugModelPresentation presentation = getPresentation();
		if (presentation instanceof IDebugModelPresentationExtension) {
			return ((IDebugModelPresentationExtension) presentation).requiresUIThread(element);
		}
		return false;
	}
}
