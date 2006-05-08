/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.DeviceResourceDescriptor;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Contains a bunch of helper methods that allow JFace resource descriptors to be passed
 * directly to SWT widgets without worrying about resource allocation. This class is internal,
 * but it should be moved into JFace if the pattern is found generally useful. The current
 * implementation uses a lot of reflection to save repeated code, but this could all be inlined
 * (without reflection) if performance turns out to be a problem.
 *  
 * <p>
 * For example, an Image might be passed to a TableItem as follows: 
 * <p>
 * 
 * <code>
 *      ImageDescriptor someDescriptor = ...;
 *      TableItem someTableItem = ...;
 *      ResourceManager manager = JFaceResources.getResources();
 *      
 *      Image actualImage = manager.createImage(someDescriptor);
 *      someTableItem.setImage(actualImage);
 *      
 *      // do something with the table item
 *      
 *      someTableItem.dispose();
 *      manager.destroyImage(someDescriptor);
 * </code>
 *
 * <p>
 * It is much more convenient to do the following:
 * </p>
 * 
 * <code>
 *      ImageDescriptor someDescriptor = ...;
 *      TableItem someTableItem = ...;
 *      
 *      Descriptors.setImage(someTableItem, someDescriptor);
 *      
 *      // do something with the table item
 *      
 *      someTableItem.dispose();
 * </code>
 * 
 * <p>
 * This class tries to behave as if the table item itself had a set method that took a descriptor.
 * Resource allocation and deallocation happens for free. All the methods are leakproof. That is, 
 * if any images, colors, etc. need to be allocated and passed to the SWT widget, they will be 
 * deallocated automatically when the widget goes away (the implementation hooks a dispose listener 
 * on the widget which cleans up as soon as the widget is disposed). 
 * </p>
 * 
 * @since 3.1
 */
public final class Descriptors {
    private static final String DISPOSE_LIST = "Descriptors.disposeList"; //$NON-NLS-1$
    
    private Descriptors() {
    }
    
    private static final class ResourceMethod {
        ResourceMethod(Method m, String id) {
            method = m;
            this.id = id;
        }
        
        Method method;
        DeviceResourceDescriptor oldDescriptor;
        String id;
                
        public void invoke(Widget toCall, DeviceResourceDescriptor newDescriptor) {
            if (newDescriptor == oldDescriptor) {
                return;
            }
            
            ResourceManager mgr = JFaceResources.getResources(toCall.getDisplay());
            
            Object newResource;
            try {
                newResource = newDescriptor == null? null : mgr.create(newDescriptor);
            } catch (DeviceResourceException e1) {
                WorkbenchPlugin.log(e1);
                return;
            }
            
            try {
                method.invoke(toCall, new Object[] {newResource});
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (IllegalAccessException e) {
                WorkbenchPlugin.log(e);
                return;
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException)e.getTargetException();
                }
                WorkbenchPlugin.log(e);
                return;
            }
            
            // Deallocate the old image 
            if (oldDescriptor != null) {
                // Dispose the image
                mgr.destroy(oldDescriptor);
            }
            
            // Remember the new image for next time
            
            oldDescriptor = newDescriptor;            
        }

        public void dispose() {
            // Deallocate the old image 
            if (oldDescriptor != null) {
                ResourceManager mgr = JFaceResources.getResources();
                // Dispose the image
                mgr.destroy(oldDescriptor);
                oldDescriptor = null;
            }                

        }
    }
    
    private static DisposeListener disposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
            doDispose(e.widget);
        }
    };
    
    // Item //////////////////////////////////////////////////////////////////////////////////
   
    /**
     * Sets the image on the given ToolItem. The image will be automatically allocated and
     * disposed as needed.
     * 
     * @since 3.1 
     *
     * @param item
     * @param descriptor
     */
    public static void setImage(Item item, ImageDescriptor descriptor) {
        callMethod(item, "setImage", descriptor, Image.class); //$NON-NLS-1$
    }
    
    // ToolItem //////////////////////////////////////////////////////////////////////////////
   
    public static void setHotImage(ToolItem item, ImageDescriptor descriptor) {
        callMethod(item, "setHotImage", descriptor, Image.class); //$NON-NLS-1$
    }

    public static void setDisabledImage(ToolItem item, ImageDescriptor descriptor) {
        callMethod(item, "setDisabledImage", descriptor, Image.class); //$NON-NLS-1$
    }

    // TableItem //////////////////////////////////////////////////////////////////////////////
    
    public static void setFont(TableItem item, FontDescriptor descriptor) {
        callMethod(item, "setFont", descriptor, Font.class); //$NON-NLS-1$
    }
    
    public static void setBackground(TableItem item, ColorDescriptor descriptor) {
        callMethod(item, "setBackground", descriptor, Color.class); //$NON-NLS-1$
    }

    public static void setForeground(TableItem item, ColorDescriptor descriptor) {
        callMethod(item, "setForeground", descriptor, Color.class); //$NON-NLS-1$
    }
    
    // Control ///////////////////////////////////////////////////////////////////////////////
    
    public static void setBackground(Control control, ColorDescriptor descriptor) {
        callMethod(control, "setBackground", descriptor, Color.class); //$NON-NLS-1$
    }
    
    public static void setForeground(Control control, ColorDescriptor descriptor) {
        callMethod(control, "setForeground", descriptor, Color.class); //$NON-NLS-1$
    }
    
    // Button ///////////////////////////////////////////////////////////////////////////////
    
    public static void setImage(Button button, ImageDescriptor descriptor) {
        callMethod(button, "setImage", descriptor, Image.class); //$NON-NLS-1$
    }

    public static void setImage(Label label, ImageDescriptor descriptor) {
        callMethod(label, "setImage", descriptor, Image.class); //$NON-NLS-1$
    }
    
    private static ResourceMethod getResourceMethod(Widget toCall, String methodName, Class resourceType) throws NoSuchMethodException {
        Object oldData = toCall.getData(DISPOSE_LIST);
        
        if (oldData instanceof List) {
            // Check for existing data
            for (Iterator iter = ((List)oldData).iterator(); iter.hasNext();) {
                ResourceMethod method = (ResourceMethod) iter.next();
                
                if (method.id == methodName) {
                    return method;
                }
            }
        } if (oldData instanceof ResourceMethod) {
            if (((ResourceMethod)oldData).id == methodName) {
                return ((ResourceMethod)oldData); 
            }
            
            List newList = new ArrayList();
            newList.add(oldData);
            oldData = newList;
            toCall.setData(DISPOSE_LIST, oldData);
        }
        
        // At this point, the DISPOSE_LIST data is either null or points to an ArrayList
        
        Class clazz = toCall.getClass();
        
        Method method;
        try {
            method = clazz.getMethod(methodName, new Class[] {resourceType});
        } catch (SecurityException e) {
            throw e;
        }
        
        ResourceMethod result = new ResourceMethod(method, methodName);

        if (oldData == null) {
            toCall.setData(DISPOSE_LIST, result);
            toCall.addDisposeListener(disposeListener);
        } else {
            ((List)oldData).add(result);
        }
        
        return result;
    }
    
    private static void callMethod(Widget toCall, String methodName, DeviceResourceDescriptor descriptor, Class resourceType) {
        ResourceMethod method;
        try {
            method = getResourceMethod(toCall, methodName, resourceType);
        } catch (NoSuchMethodException e) {
            WorkbenchPlugin.log(e);
            return;
        }
       
        method.invoke(toCall, descriptor);        
    }
    
    private static void doDispose(Widget widget) {
        Object oldData = widget.getData(DISPOSE_LIST);
        
        if (oldData instanceof ArrayList) {
            ArrayList list = ((ArrayList)oldData);
            ResourceMethod[] data = (ResourceMethod[]) list.toArray(new ResourceMethod[list.size()]);
            
            // Clear out the images
            for (int i = 0; i < data.length; i++) {
                ResourceMethod method = data[i];

                method.dispose();                
            }
        } 
        
        if (oldData instanceof ResourceMethod) {
            ((ResourceMethod)oldData).dispose();
        }
    }
    
}
