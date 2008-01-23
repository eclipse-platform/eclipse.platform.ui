/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugEditorPresentation;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugModelPresentationExtension;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IInstructionPointerPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * A model presentation that delegates to the appropriate extension. This
 * presentation contains a table of specialized presentations that are defined
 * as <code>org.eclipse.debug.ui.debugModelPresentations</code> extensions. When
 * asked to render an object from a debug model, this presentation delegates
 * to the extension registered for that debug model. 
 */
public class DelegatingModelPresentation implements IDebugModelPresentation, IDebugEditorPresentation,
	IColorProvider, IFontProvider, IInstructionPointerPresentation, IDebugModelPresentationExtension {
	
	/**
	 * A mapping of attribute ids to their values
	 * @see IDebugModelPresentation#setAttribute
	 */
	private HashMap fAttributes= new HashMap(3);
	/**
	 * A table of label providers keyed by debug model identifiers.
	 */
	private HashMap fLabelProviders= new HashMap(5);

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#removeAnnotations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IThread)
	 */
	public void removeAnnotations(IEditorPart editorPart, IThread thread) {
		IDebugModelPresentation presentation = getConfiguredPresentation(thread);
		if (presentation instanceof IDebugEditorPresentation) {
			((IDebugEditorPresentation)presentation).removeAnnotations(editorPart, thread);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugEditorPresentation#addAnnotations(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public boolean addAnnotations(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getConfiguredPresentation(frame);
		if (presentation instanceof IDebugEditorPresentation) {
			return((IDebugEditorPresentation)presentation).addAnnotations(editorPart, frame);
		}
		return false;
	}

	/**
	 * Constructs a new DelegatingLabelProvider that delegates to extensions
	 * of kind <code>org.eclipse.debug.ui.debugLabelProvider</code>
	 */
	public DelegatingModelPresentation() {
		IExtensionPoint point= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.ID_DEBUG_MODEL_PRESENTATION);
		if (point != null) {
			IExtension[] extensions= point.getExtensions();
			for (int i= 0; i < extensions.length; i++) {
				IExtension extension= extensions[i];
				IConfigurationElement[] configElements= extension.getConfigurationElements();
				for (int j= 0; j < configElements.length; j++) {
					IConfigurationElement elt= configElements[j];
					String id= elt.getAttribute("id"); //$NON-NLS-1$
					if (id != null) {
						IDebugModelPresentation lp= new LazyModelPresentation(this, elt);
						getLabelProviders().put(id, lp);
					}
				}
			}
		}
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
		Iterator i= getLabelProviders().values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).addListener(listener);
		}
	}

	/**
	 * Delegate to all extensions.
	 *
	 * @see IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		Iterator i= getLabelProviders().values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).dispose();
		}
	}

	/**
	 * @see IDebugModelPresentation#getImage(Object)
	 */
	public Image getImage(Object item) {
		// Attempt to delegate
		IDebugModelPresentation lp= getConfiguredPresentation(item);
		if (lp != null) {
			Image image= lp.getImage(item);
			if (image != null) {
				return image;
			}
		}
		// If no delegate returned an image, use the default
		return getDefaultImage(item);
	}
	
	/**
     * @see IDebugModelPresentation#getText(Object)
     */
    public String getText(Object item) {
    	IDebugModelPresentation lp= getConfiguredPresentation(item);
    	if (lp != null) {
    		return lp.getText(item);
    	}
    	return getDefaultText(item);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
	 */
	public IEditorInput getEditorInput(Object item) {
		IDebugModelPresentation lp= getConfiguredPresentation(item);
		if (lp != null) {
			return lp.getEditorInput(item);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
	 */
	public String getEditorId(IEditorInput input, Object objectInput) {
		IDebugModelPresentation lp= getConfiguredPresentation(objectInput);
		if (lp != null) {
			return lp.getEditorId(input, objectInput);
		}
		return null;
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
	 */
	public void computeDetail(IValue value, IValueDetailListener listener) {
		IDebugModelPresentation lp= getConfiguredPresentation(value);
		if (lp != null) {
			lp.computeDetail(value, listener);			
		} else {
			listener.detailComputed(value, getText(value));
		}
	}	

	/**
	 * Delegate to all extensions.
	 *
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
		Iterator i= getLabelProviders().values().iterator();
		while (i.hasNext()) {
			((ILabelProvider) i.next()).removeListener(listener);
		}
	}

	/**
	 * Delegate to the appropriate label provider.
	 *
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		if (element instanceof IDebugElement) {
			IDebugModelPresentation lp= getConfiguredPresentation(element);
			if (lp != null) {
				return lp.isLabelProperty(element, property);
			}
		}

		return true;
	}

	/**
	 * Returns a configured model presentation for the given object,
	 * or <code>null</code> if one is not registered.
	 */
	protected IDebugModelPresentation getConfiguredPresentation(Object element) {
		String id= null;
		if (element instanceof IDebugElement) {
			IDebugElement de= (IDebugElement) element;
			id= de.getModelIdentifier();
		} else if (element instanceof IMarker) {
			IMarker m= (IMarker) element;
			IBreakpoint bp = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(m);
			if (bp != null) {
				id= bp.getModelIdentifier();
			}
		} else if (element instanceof IBreakpoint) {
			id = ((IBreakpoint)element).getModelIdentifier();
		}
		if (id != null) {
			return getPresentation(id);
		}

		return null;
	}
	
	/**
	 * Returns the presentation registered for the given id, or <code>null</code>
	 * of nothing is registered for the id.
	 */
	public IDebugModelPresentation getPresentation(String id) {
		return (IDebugModelPresentation) getLabelProviders().get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String id, Object value) {
		if (value == null) {
			return;
		}
		basicSetAttribute(id, value);
		Iterator presentations = fLabelProviders.values().iterator();
		while (presentations.hasNext()) {
			((IDebugModelPresentation)presentations.next()).setAttribute(id, value);
		}
	}
	
	/**
	 * Sets the value of the given attribute without setting in child presentations.
	 * 
	 * @param id id
	 * @param value value
	 */
	protected void basicSetAttribute(String id, Object value) {
		getAttributes().put(id, value);
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
	 * Returns the raw attribute map
	 * @return the raw attribute map
	 */
	public HashMap getAttributes() {
		return fAttributes;
	}
	
	/**
	 * Returns a copy of the attribute map for this presentation.
	 * 
	 * @return a copy of the attribute map for this presentation
	 * @since 3.0
	 */
	public Map getAttributeMap() {
		return (Map) getAttributes().clone();
	}

	protected HashMap getLabelProviders() {
		return fLabelProviders;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        IDebugModelPresentation presentation = getConfiguredPresentation(element);
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
        IDebugModelPresentation presentation = getConfiguredPresentation(element);
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
        IDebugModelPresentation presentation = getConfiguredPresentation(element);
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
		IDebugModelPresentation presentation = getConfiguredPresentation(frame);
		Annotation annotation = null;
		String id = null;
		Image image = null;
		String text = null;
		if (presentation instanceof IInstructionPointerPresentation) {
			// first check if an annotaion object is provided
			IInstructionPointerPresentation pointerPresentation = (IInstructionPointerPresentation) presentation;
			annotation = pointerPresentation.getInstructionPointerAnnotation(editorPart, frame);
			if (annotation == null) {
				// next check for a marker annotation specification extension
				id = pointerPresentation.getInstructionPointerAnnotationType(editorPart, frame);
				if (id == null) {
					// check for an image
					image = pointerPresentation.getInstructionPointerImage(editorPart, frame);
				}
				text = pointerPresentation.getInstructionPointerText(editorPart, frame);
			}
		}
		if (annotation == null) {
            boolean defaultAnnotation = id == null;
			if (id == null || text == null || (defaultAnnotation && image == null)) {
				IThread thread = frame.getThread();
				IStackFrame tos = null;
				boolean top = false;
				try {
					tos = thread.getTopStackFrame();
					top = frame.equals(tos);
				} catch (DebugException de) {
				}
				if (id == null) {
					if (top) {
						id = IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_CURRENT;
					} else {
						id = IDebugUIConstants.ANNOTATION_TYPE_INSTRUCTION_POINTER_SECONDARY;
					}
				}
				if (text == null) {
					if (top) {
						text = DebugUIMessages.InstructionPointerAnnotation_0;
					} else {
						text = DebugUIMessages.InstructionPointerAnnotation_1;
					}
				}
				if (defaultAnnotation && image == null) {
					if (top) {
						image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER_TOP);
					} else {
						image = DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_INSTRUCTION_POINTER);
					}
				}
			}
			if (defaultAnnotation) {
				annotation = new InstructionPointerAnnotation(frame, id, text, image);
			} else {
				annotation = new DynamicInstructionPointerAnnotation(frame, id, text);
			}
		}
		return annotation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getMarkerAnnotationSpecificationId(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public String getInstructionPointerAnnotationType(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getConfiguredPresentation(frame);
		if (presentation instanceof IInstructionPointerPresentation) {
			return ((IInstructionPointerPresentation)presentation).getInstructionPointerAnnotationType(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getInstructionPointerImage(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public Image getInstructionPointerImage(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getConfiguredPresentation(frame);
		if (presentation instanceof IInstructionPointerPresentation) {
			return ((IInstructionPointerPresentation)presentation).getInstructionPointerImage(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IInstructionPointerPresentation#getInstructionPointerText(org.eclipse.ui.IEditorPart, org.eclipse.debug.core.model.IStackFrame)
	 */
	public String getInstructionPointerText(IEditorPart editorPart, IStackFrame frame) {
		IDebugModelPresentation presentation = getConfiguredPresentation(frame);
		if (presentation instanceof IInstructionPointerPresentation) {
			return ((IInstructionPointerPresentation)presentation).getInstructionPointerText(editorPart, frame);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDebugModelPresentationExtension#requiresUIThread(java.lang.Object)
	 */
	public boolean requiresUIThread(Object element) {
		IDebugModelPresentation presentation = getConfiguredPresentation(element);
		if (presentation == null) {
			// default label provider will be used
			return !DebugPluginImages.isInitialized();
		}
		if (presentation instanceof IDebugModelPresentationExtension) {
			return ((IDebugModelPresentationExtension)presentation).requiresUIThread(element);
		} 
		return false;
	}	
}
