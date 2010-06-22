/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

/**
 * Caches the preferred size of an SWT control
 * 
 * @since 3.0
 */
public class SizeCache {
    private Control control;

    private Point preferredSize;

    private int cachedWidthQuery;
    private int cachedWidthResult;

    private int cachedHeightQuery;
    private int cachedHeightResult;
    
    private int minimumWidth;
    private int heightAtMinimumWidth = -1;
    private int maximumWidth;
    
    /**
     * True iff we should recursively flush all children on the next layout
     */
    private boolean flushChildren;

    /**
     * True iff changing the height hint does not affect the preferred width and changing
     * the width hint does not change the preferred height
     */
    private boolean independentDimensions = false;

    /**
     * True iff the preferred height for any hint larger than the preferred width will not
     * change the preferred height.
     */
    private boolean preferredWidthOrLargerIsMinimumHeight = false;

    // HACK: these values estimate how much to subtract from the width and height
    // hints that get passed into computeSize, in order to produce a result
    // that is exactly the desired size. To be removed once bug 46112 is fixed (note:
    // bug 46112 is currently flagged as a duplicate, but there is still no workaround).
    private int widthAdjustment = 0;

    private int heightAdjustment = 0;

    private int minimumHeight;

    private int widthAtMinimumHeight = -1;
    
    // If the layout is dirty, this is the size of the control at the time its
    // layout was dirtied. null if the layout is not dirty.
    private Point dirtySize = null; 


    // END OF HACK

    public SizeCache() {
        this(null);
    }

    /**
     * Creates a cache for size computations on the given control
     * 
     * @param control the control for which sizes will be calculated, 
     * or null to always return (0,0) 
     */
    public SizeCache(Control control) {
        setControl(control);
    }

    /**
     * Sets the control whose size is being cached. Does nothing (will not
     * even flush the cache) if this is the same control as last time. 
     * 
     * @param newControl the control whose size is being cached, or null to always return (0,0)
     */
    public void setControl(Control newControl) {
        if (newControl != control) {
            control = newControl;
            if (control == null) {
                independentDimensions = true;
                preferredWidthOrLargerIsMinimumHeight = false;
                widthAdjustment = 0;
                heightAdjustment = 0;
            } else {
                independentDimensions = independentLengthAndWidth(control);
                preferredWidthOrLargerIsMinimumHeight = isPreferredWidthMaximum(control);
                computeHintOffset(control);
                flush();
            }
        }
    }

    /**
     * Returns the control whose size is being cached
     * 
     * @return the control whose size is being cached, or null if this cache always returns (0,0)
     */
    public Control getControl() {
        return control;
    }

    /**
     * Flush the cache (should be called if the control's contents may have changed since the
     * last query)
     */
    public void flush() {
        flush(true);
    }

    public void flush(boolean recursive) {
        preferredSize = null;
        cachedWidthQuery = -1;
        cachedWidthResult = -1;
        cachedHeightQuery = -1;
        cachedHeightResult = -1;
        minimumWidth = -1;
        maximumWidth = -1;
        minimumHeight = -1;
        heightAtMinimumWidth = -1;
        widthAtMinimumHeight = -1;
        
        if (recursive || dirtySize != null) {
            if (control == null || control.isDisposed()) {
                dirtySize = new Point(0,0);
                control = null;
            } else {
                dirtySize = control.getSize();
            }
        }
        
        this.flushChildren = this.flushChildren || recursive;
    }

    private Point getPreferredSize() {
        if (preferredSize == null) {
            preferredSize = controlComputeSize(SWT.DEFAULT, SWT.DEFAULT);
        }

        return preferredSize;
    }

    /**
     * Computes the preferred size of the control.
     *  
     * @param widthHint the known width of the control (pixels) or SWT.DEFAULT if unknown
     * @param heightHint the known height of the control (pixels) or SWT.DEFAULT if unknown
     * @return the preferred size of the control
     */
    public Point computeSize(int widthHint, int heightHint) {
        if (control == null || control.isDisposed()) {
            return new Point(0, 0);
        }
        
        // If we're asking for a result smaller than the minimum width
        int minWidth = computeMinimumWidth();
        
        if (widthHint != SWT.DEFAULT && widthHint + widthAdjustment < minWidth) {
            if (heightHint == SWT.DEFAULT) {
                return new Point(minWidth, computeHeightAtMinimumWidth());   
            }
            
            widthHint = minWidth - widthAdjustment;
        }
        
        // If we're asking for a result smaller than the minimum height
        int minHeight = computeMinimumHeight();
        
        if (heightHint != SWT.DEFAULT && heightHint + heightAdjustment < minHeight) {
            if (widthHint == SWT.DEFAULT) {
                return new Point(computeWidthAtMinimumHeight(), minHeight);
            }
            
            heightHint = minHeight - heightAdjustment;
        }
        
        // If both dimensions were supplied in the input, compute the trivial result
        if (widthHint != SWT.DEFAULT && heightHint != SWT.DEFAULT) {                        
            return new Point(widthHint + widthAdjustment, heightHint + heightAdjustment);
        }

        // No hints given -- find the preferred size
        if (widthHint == SWT.DEFAULT && heightHint == SWT.DEFAULT) {
            return Geometry.copy(getPreferredSize());
        }

        // If the length and width are independent, compute the preferred size
        // and adjust whatever dimension was supplied in the input
        if (independentDimensions) {
            Point result = Geometry.copy(getPreferredSize());

            if (widthHint != SWT.DEFAULT) {
                result.x = widthHint + widthAdjustment;
            }

            if (heightHint != SWT.DEFAULT) {
                result.y = heightHint + heightAdjustment;
            }

            return result;
        }

        // Computing a height
        if (heightHint == SWT.DEFAULT) {
            // If we know the control's preferred size
            if (preferredSize != null) {
                // If the given width is the preferred width, then return the preferred size
                if (widthHint + widthAdjustment == preferredSize.x) {
                    return Geometry.copy(preferredSize);
                }
            }

            // If we have a cached height measurement
            if (cachedHeightQuery != -1) {
                // If this was measured with the same width hint
                if (cachedHeightQuery == widthHint) {
                    return new Point(widthHint + widthAdjustment, cachedHeightResult);
                }
            }

            // If this is a control where any hint larger than the
            // preferred width results in the minimum height, determine if
            // we can compute the result based on the preferred height
            if (preferredWidthOrLargerIsMinimumHeight) {
                // Computed the preferred size (if we don't already know it)
                getPreferredSize();

                // If the width hint is larger than the preferred width, then
                // we can compute the result from the preferred width
                if (widthHint + widthAdjustment >= preferredSize.x) {
                    return new Point(widthHint + widthAdjustment, preferredSize.y);
                }
            }

            // Else we can't find an existing size in the cache, so recompute
            // it from scratch.
            Point newHeight = controlComputeSize(widthHint - widthAdjustment, SWT.DEFAULT);

            cachedHeightQuery = heightHint;
            cachedHeightResult = newHeight.y;
            
            return newHeight;
        }

        // Computing a width
        if (widthHint == SWT.DEFAULT) {
            // If we know the control's preferred size
            if (preferredSize != null) {
                // If the given height is the preferred height, then return the preferred size
                if (heightHint + heightAdjustment == preferredSize.y) {
                    return Geometry.copy(preferredSize);
                }
            }

            // If we have a cached width measurement with the same height hint
            if (cachedWidthQuery == heightHint) {
                return new Point(cachedWidthResult, heightHint + heightAdjustment);
            }

            Point widthResult = controlComputeSize(SWT.DEFAULT, heightHint - heightAdjustment);

            cachedWidthQuery = heightHint;
            cachedWidthResult = widthResult.x;
            
            return widthResult;
        }

        return controlComputeSize(widthHint, heightHint);
    }
    
    /**
     * Compute the control's size, and ensure that non-default hints are returned verbatim
     * (this tries to compensate for SWT's hints, which aren't really the outer width of the
     * control).
     * 
     * @param widthHint the horizontal hint
     * @param heightHint the vertical hint
     * @return the control's size
     */
    public Point computeAdjustedSize(int widthHint, int heightHint) {
        int adjustedWidthHint = widthHint == SWT.DEFAULT ? SWT.DEFAULT : Math
                .max(0, widthHint - widthAdjustment);
        int adjustedHeightHint = heightHint == SWT.DEFAULT ? SWT.DEFAULT : Math
                .max(0, heightHint - heightAdjustment);

        Point result = computeSize(adjustedWidthHint, adjustedHeightHint);

        // If the amounts we subtracted off the widthHint and heightHint didn't do the trick, then
        // manually adjust the result to ensure that a non-default hint will return that result verbatim.
        
        return result;
    }

    /**
     * Returns true if the preferred length of the given control is 
     * independent of the width and visa-versa. If this returns true,
     * then changing the widthHint argument to control.computeSize will
     * never change the resulting height and changing the heightHint
     * will never change the resulting width. Returns false if unknown.
     * <p>
     * This information can be used to improve caching. Incorrectly returning
     * a value of false may decrease performance, but incorrectly returning 
     * a value of true will generate incorrect layouts... so always return
     * false if unsure.
     * </p>
     * 
     * @param control
     * @return
     */
    static boolean independentLengthAndWidth(Control control) {
        if (control == null || control.isDisposed()) {
            return true;
        }

        if (control instanceof Button || control instanceof ProgressBar
                || control instanceof Sash || control instanceof Scale
                || control instanceof Slider || control instanceof List
                || control instanceof Combo || control instanceof Tree) {
            return true;
        }

        if (control instanceof Label || control instanceof Text) {
            return (control.getStyle() & SWT.WRAP) == 0;
        }

        // Unless we're certain that the control has this property, we should
        // return false.

        return false;
    }
    
    /**
     * Try to figure out how much we need to subtract from the hints that we
     * pass into the given control's computeSize(...) method. This tries to
     * compensate for bug 46112. To be removed once SWT provides an "official"
     * way to compute one dimension of a control's size given the other known
     * dimension.
     * 
     * @param control
     */
    private void computeHintOffset(Control control) {
        if (control instanceof Scrollable) {
            // For scrollables, subtract off the trim size
            Scrollable scrollable = (Scrollable) control;
            Rectangle trim = scrollable.computeTrim(0, 0, 0, 0);

            widthAdjustment = trim.width;
            heightAdjustment = trim.height;
        } else {
            // For non-composites, subtract off 2 * the border size
            widthAdjustment = control.getBorderWidth() * 2;
            heightAdjustment = widthAdjustment;
        }
    }

    private Point controlComputeSize(int widthHint, int heightHint) {
        Point result = control.computeSize(widthHint, heightHint, flushChildren);
        flushChildren = false;
        
        return result;
    }

    /**
     * Returns true only if the control will return a constant height for any
     * width hint larger than the preferred width. Returns false if there is
     * any situation in which the control does not have this property.
     * 
     * <p>
     * Note: this method is only important for wrapping controls, and it can
     * safely return false for anything else. AFAIK, all SWT controls have this
     * property, but to be safe they will only be added to the list once the
     * property has been confirmed.
     * </p> 
     * 
     * @param control
     * @return
     */
    private static boolean isPreferredWidthMaximum(Control control) {
        return (control instanceof ToolBar
        //|| control instanceof CoolBar
        || control instanceof Label);
    }
    
    public int computeMinimumWidth() {
        if (minimumWidth == -1) {
    		if (control instanceof Composite) {
    			Layout layout = ((Composite)control).getLayout();
    			if (layout instanceof ILayoutExtension) {
    				minimumWidth = ((ILayoutExtension)layout).computeMinimumWidth((Composite)control, flushChildren);
    				flushChildren = false;
    			}
    		}
        }

        if (minimumWidth == -1) {
            Point minWidth = controlComputeSize(FormUtil.getWidthHint(5, control), SWT.DEFAULT); 
            minimumWidth = minWidth.x;
            heightAtMinimumWidth = minWidth.y;
        }
		
		return minimumWidth;
    }
    
    public int computeMaximumWidth() {
        if (maximumWidth == -1) {
    		if (control instanceof Composite) {
    			Layout layout = ((Composite)control).getLayout();
    			if (layout instanceof ILayoutExtension) {
    				maximumWidth = ((ILayoutExtension)layout).computeMaximumWidth((Composite)control, flushChildren);
    				flushChildren = false;
    			}
    		}
        }

        if (maximumWidth == -1) {
            maximumWidth = getPreferredSize().x;
        }
		
        return maximumWidth;
	}
    
    private int computeHeightAtMinimumWidth() {
        int minimumWidth = computeMinimumWidth();
        
        if (heightAtMinimumWidth == -1) {
            heightAtMinimumWidth = controlComputeSize(minimumWidth - widthAdjustment, SWT.DEFAULT).y;
        }
        
        return heightAtMinimumWidth;
    }
    
    private int computeWidthAtMinimumHeight() {
        int minimumHeight = computeMinimumHeight();
        
        if (widthAtMinimumHeight == -1) {
            widthAtMinimumHeight = controlComputeSize(SWT.DEFAULT, minimumHeight - heightAdjustment).x;
        }
        
        return widthAtMinimumHeight;
    }

    private int computeMinimumHeight() {
        if (minimumHeight == -1) {
            Point sizeAtMinHeight = controlComputeSize(SWT.DEFAULT, 0);
            
            minimumHeight = sizeAtMinHeight.y;
            widthAtMinimumHeight = sizeAtMinHeight.x;
        }
        
        return minimumHeight;
    }
    
    public Point computeMinimumSize() {
        return new Point(computeMinimumWidth(), computeMinimumHeight());
    }

    public void setSize(Point newSize) {
        if (control != null) {
            control.setSize(newSize);
        }
        
        layoutIfNecessary();
    }
    
    public void setSize(int width, int height) {
        if (control != null) {
            control.setSize(width, height);
        }
        
        layoutIfNecessary();        
    }
    
    public void setBounds(int x, int y, int width, int height) {
        if (control != null) {
            control.setBounds(x, y, width, height);
        }
        
        layoutIfNecessary();        
    }
    
    public void setBounds(Rectangle bounds) {
        if (control != null) {
            control.setBounds(bounds);
        }
        
        layoutIfNecessary();
    }

    public void layoutIfNecessary() {
        if (dirtySize != null && control != null && control instanceof Composite) {
            if (control.getSize().equals(dirtySize)) {
	            ((Composite)control).layout(flushChildren);
	            flushChildren = false;
            }
        }
        dirtySize = null;
    }
}
