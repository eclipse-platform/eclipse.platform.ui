/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.jface.databinding.viewers;

import java.util.Collections;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.internal.databinding.swt.SWTUtil;
import org.eclipse.jface.viewers.IViewerLabelProvider;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * This label provider modifies an INotifyingLabelProvider by adding graying out entries
 * when they are being fetched by a ConcurrentTreeContentProvider.
 */
public final class DirtyIndicationLabelProvider extends ViewerLabelProvider {
    /**
     * 
     */
    private final UpdatableTreeContentProvider cp;

    private final Color busy;

    private final Color black;

    private IChangeListener dirtyListener;

    private Color grayColor;
    
    private IViewerLabelProvider toWrap;

    public DirtyIndicationLabelProvider(Control context, UpdatableTreeContentProvider cp, IViewerLabelProvider toConvert) {
        this(cp, context.getDisplay(), toConvert, context.getForeground(), context.getBackground());
    }
    
    /**
     * Creates a label provider that decorates an INotifyingLabelProvider with a busy indicator.
     *  
     * @param cp ContentProvider that will be used to determine the busy state. Elements are considered
     * busy if and only if they are being fetched by this content provider.
     * @param display display
     * @param convert label provider to decorate
     * @param foreground default foreground color
     * @param background table background color
     */
    public DirtyIndicationLabelProvider(UpdatableTreeContentProvider cp, Display display, IViewerLabelProvider convert, Color foreground, Color background) {
        super();
        toWrap = convert; 
        this.cp = cp;
        
        RGB fg = foreground.getRGB();
        RGB bg = background.getRGB();
        RGB grayed = SWTUtil.mix(fg, bg, 0.5);
        grayColor = new Color(display, grayed);
        this.busy = grayColor;
        this.black = foreground;
        
        dirtyListener = new IChangeListener() {
        	public void handleChange(ChangeEvent changeEvent) {
        		if (changeEvent.getChangeType() == ChangeEvent.STALE) {
        			dirtyChanged(changeEvent.getSource());
        		}
        	}
        };
        cp.addDirtyListener(dirtyListener);
    }

    private void dirtyChanged(Object element) {
        fireChangeEvent(Collections.singletonList(element));
    }

    public void updateLabel(ViewerLabel label, Object element) {
        label.setForeground(black);
        toWrap.updateLabel(label, element);
        
        if (cp.isDirty(element)) {
            label.setForeground(busy);
        }
    }

    public void dispose() {
        cp.removeDirtyListener(dirtyListener);
        super.dispose();
        grayColor.dispose();
    }
}
