/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Chris Gross chris.gross@us.ibm.com Bug 107443
 *******************************************************************************/
package org.eclipse.ui.tests.performance.presentations;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.internal.DefaultStackPresentationSite;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

public class PresentationTestbed {
    
    private StackPresentation presentation;
    private List partList = new ArrayList();
    private IPresentablePart selectedPart;
    private Composite control;
    
    private DefaultStackPresentationSite site = new DefaultStackPresentationSite() {
        public void close(IPresentablePart[] toClose) {}
        public void flushLayout() {}
        public IPresentablePart[] getPartList() {
            return (IPresentablePart[]) partList.toArray(new IPresentablePart[partList.size()]);
        }
        public IPresentablePart getSelectedPart() {
            return selectedPart;
        }
        public boolean isPartMoveable(IPresentablePart toMove) {
            return true;
        }
        public boolean isStackMoveable() {
            return true;
        }
        /* (non-Javadoc)
         * @see org.eclipse.ui.presentations.IStackPresentationSite#getProperty(java.lang.String)
         */
        public String getProperty(String id) {
        	return null;
        }
    };
    
    public PresentationTestbed(Composite parentComposite, AbstractPresentationFactory factory, int type) {
        presentation = PresentationFactoryUtil.createPresentation(factory, type, parentComposite, site, null, null);
        site.setPresentation(presentation);
        control = new Composite(parentComposite, SWT.NONE);
        control.addControlListener(new ControlListener() {
            public void controlMoved(ControlEvent e) {
                updatePresentationBounds();
            }
            public void controlResized(ControlEvent e) {
                updatePresentationBounds();
            }
        });
        
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                site.dispose();
            } 
        });
        
        control.setLayout(new Layout() {
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                int widthHint = wHint == SWT.DEFAULT ? ISizeProvider.INFINITE : wHint;
                int heightHint = hHint == SWT.DEFAULT ? ISizeProvider.INFINITE : hHint;
                
                int width = 200;
                int height = 200;
                StackPresentation presentation = site.getPresentation();
                if (presentation != null) {
                    width = presentation.computePreferredSize(true, widthHint, heightHint, widthHint);
                    height = presentation.computePreferredSize(false, heightHint, widthHint, heightHint);
                }
                
                if (width == ISizeProvider.INFINITE) {
                    width = 200;
                }

                if (height == ISizeProvider.INFINITE) {
                    height = 200;
                }
                
                return new Point(width, height);
            }
           
            protected void layout(Composite composite, boolean flushCache) {
            }
        });
        
        control.setVisible(false);
        site.setActive(StackPresentation.AS_ACTIVE_FOCUS);
        site.setState(IStackPresentationSite.STATE_RESTORED);
    }
    
    public Control getControl() {
        return control;
    }
    
    public void add(IPresentablePart part) {
        partList.add(part);
        site.getPresentation().addPart(part, null);
    }
    
    public void remove(IPresentablePart part) {
        Assert.assertTrue(part != selectedPart);
        partList.remove(part);
        site.getPresentation().removePart(part);
    }
    
    public void setSelection(IPresentablePart newSelection) {
        Assert.assertTrue(partList.contains(newSelection));
        
        selectedPart = newSelection;
        if (selectedPart != null) {
            site.selectPart(newSelection);
        }
    }
    
    public void setState(int newState) {
        site.setPresentationState(newState);
    }
    
    public void setActive(int activeState) {
        site.setActive(activeState);
    }
    
    public IPresentablePart[] getPartList() {
        return (IPresentablePart[]) partList.toArray(new IPresentablePart[partList.size()]);
    }
    
    private void updatePresentationBounds() {
        StackPresentation presentation = site.getPresentation();
        if (presentation != null) {
            presentation.setBounds(control.getBounds());
        }
    }
    
    public IPresentablePart getSelection() {
        return selectedPart;
    }
}
