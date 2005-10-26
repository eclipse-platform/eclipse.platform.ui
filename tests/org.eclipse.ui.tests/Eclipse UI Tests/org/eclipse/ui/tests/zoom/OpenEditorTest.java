/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.zoom;

import junit.framework.Assert;


public class OpenEditorTest extends ZoomTestCase {
    public OpenEditorTest(String name) {
        super(name);
    }

    /** 
     * <p>Test: Zoom an editor then open an existing editor in the same stack. Do not force activation.</p>
     * <p>Expected result: the new editor is zoomed and active</p> 
     */
    public void testOpenExistingEditorInZoomedStack() {
        zoom(editor1);
        openEditor(file2, false);
        Assert.assertTrue(isZoomed(editor2));
        Assert.assertTrue(page.getActivePart() == editor2);
    }

    /** 
     * <p>Test: Open a new editor while a view is zoomed. Do not force activation.</p>
     * <p>Expected result: the page remains zoomed, the view is active</p> 
     * 
     * <p>Note: the expected result changed intentionally on 05/04/18</p>
     */
    public void testOpenNewEditorWhileViewZoomed() {
        close(editor1);
        
        zoom(stackedView1);
        openEditor(file1, false);
        
        assertZoomed(stackedView1);
        assertActive(stackedView1);
    }
    
    /** 
     * <p>Test: Zoom an editor then open a new editor in the same stack. Do not force activation.</p>
     * <p>Expected result: the new editor is zoomed and active</p> 
     */
    public void testOpenNewEditorInZoomedStack() {
        close(editor2);
        
        zoom(editor1);
        openEditor(file2, false);
        Assert.assertTrue(isZoomed(editor2));
        Assert.assertTrue(page.getActivePart() == editor2);
    }
    
    /** 
     * <p>Test: Open an existing editor while a view is zoomed. Do not force activation.</p>
     * <p>Expected result: the page remains zoomed, the view is active</p> 
     */
    public void testOpenExistingEditorWhileViewZoomed() {
        zoom(stackedView1);
        openEditor(file1, false);
        
        assertZoomed(stackedView1);
        assertActive(stackedView1);
    }

    /** 
     * <p>Test: Open an existing editor while a view is zoomed. Use the activate-on-open mode.</p>
     * <p>Expected result: the page is unzoomed, the view is active</p> 
     */
    public void testOpenAndActivateExistingEditorWhileViewZoomed() {
        zoom(stackedView1);
        openEditor(file1, true);

        assertZoomed(null);
        assertActive(editor1);
    }

    /** 
     * <p>Test: Open a new editor while a view is zoomed. Use the activate-on-open mode.</p>
     * <p>Expected result: the page is unzoomed, the view is active</p> 
     */
    public void testOpenAndActivateNewEditorWhileViewZoomed() {
        close(editor1);
        
        zoom(stackedView1);
        openEditor(file1, true);
       
        assertZoomed(null);
        assertActive(editor1);
    }

    /** 
     * <p>Test: Zoom an editor then open an existing in the same stack. Use the activate-on-open mode.</p>
     * <p>Expected result: the new editor is zoomed and active</p> 
     */
    public void testOpenAndActivateExistingEditorInZoomedStack() {
        zoom(editor1);
        openEditor(file2, true);

        assertZoomed(editor2);
        assertActive(editor2);
    }

    /** 
     * <p>Test: Zoom an editor then open a new editor in the same stack. Use the activate-on-open mode.</p>
     * <p>Expected result: the new editor is zoomed and active</p> 
     */
    public void testOpenAndActivateNewEditorInZoomedStack() {
        close(editor2);
        
        zoom(editor1);
        openEditor(file2, true);

        assertZoomed(editor2);
        assertActive(editor2);
    }
    
    /** 
     * <p>Test: Zoom an editor then open an existing editor in a different stack. Do not force activation.</p>
     * <p>Expected result: the page remains zoomed and the original editor is active</p> 
     */
    public void testOpenExistingEditorInOtherStack() {
        zoom(editor3);
        openEditor(file2, false);
        
        assertZoomed(editor3);
        assertActive(editor3);
    }

    /** 
     * <p>Test: Zoom an editor then open an existing editor in a different stack. Use the activate-on-open mode.</p>
     * <p>Expected result: the page is unzoomed and the new editor and active</p> 
     */
    public void testOpenAndActivateExistingEditorInOtherStack() {
        zoom(editor3);
        openEditor(file2, true);

        assertZoomed(null);
        assertActive(editor2);
    }
    
}
