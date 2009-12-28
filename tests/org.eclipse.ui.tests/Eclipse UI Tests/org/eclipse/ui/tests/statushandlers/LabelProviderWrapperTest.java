/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.statushandlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.internal.statushandlers.IStatusDialogConstants;
import org.eclipse.ui.internal.statushandlers.LabelProviderWrapper;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;

import junit.framework.TestCase;

/**
 * @since 3.5
 *
 */
public class LabelProviderWrapperTest extends TestCase {

	private LabelProviderWrapper wrapper;
	private Map dialogState = new HashMap();
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		dialogState = new HashMap();
		dialogState.put(IStatusDialogConstants.STATUS_ADAPTERS, new HashSet());
		((Collection)dialogState.get(IStatusDialogConstants.STATUS_ADAPTERS)).add(Status.OK_STATUS);
		wrapper = new LabelProviderWrapper(dialogState);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		wrapper = null;
	}
	
	public void testDisposing(){
		final boolean[] disposed = new boolean[]{false};
		ITableLabelProvider provider = new ITableLabelProvider() {
			public void removeListener(ILabelProviderListener listener) {
			}
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}
			public void dispose() {
				disposed[0] = true;
			}
			public void addListener(ILabelProviderListener listener) {
			}
			public String getColumnText(Object element, int columnIndex) {
				return null;
			}
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		};
		dialogState.put(IStatusDialogConstants.CUSTOM_LABEL_PROVIDER, provider);
		dialogState.put(IStatusDialogConstants.MODALITY_SWITCH, Boolean.TRUE);
		wrapper.dispose();
		assertFalse("Provider should not be disposed during modality switch",
				disposed[0]);
		
		dialogState.put(IStatusDialogConstants.MODALITY_SWITCH, Boolean.FALSE);
		wrapper.dispose();
		assertTrue("Provider should be disposed", disposed[0]);
	}

	public void testImages(){
		StatusAdapter saError = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "errorMessage"));
		assertEquals(wrapper.getSWTImage(SWT.ICON_ERROR), wrapper.getImage(saError));
		
		StatusAdapter saWarning = new StatusAdapter(new Status(IStatus.WARNING, "org.eclipse.ui.tests", "warningMessage"));
		assertEquals(wrapper.getSWTImage(SWT.ICON_WARNING), wrapper.getImage(saWarning));
		
		StatusAdapter saInfo = new StatusAdapter(new Status(IStatus.INFO, "org.eclipse.ui.tests", "infoMessage"));
		assertEquals(wrapper.getSWTImage(SWT.ICON_INFORMATION), wrapper.getImage(saInfo));
		
		StatusAdapter cancelOK = new StatusAdapter(new Status(IStatus.CANCEL, "org.eclipse.ui.tests", "cancelMessage"));
		assertEquals(wrapper.getSWTImage(SWT.ICON_INFORMATION), wrapper.getImage(cancelOK));
		
		StatusAdapter saOK = new StatusAdapter(new Status(IStatus.OK, "org.eclipse.ui.tests", "okMessage"));
		assertEquals(wrapper.getSWTImage(SWT.ICON_INFORMATION), wrapper.getImage(saOK));
	}
	
	/*
	 *	StatusAdapter contains all information necessary to display the dialog. 
	 */
	public void testProvidedText_1(){
		final String title = "title";
		final String message = "errorMessage";
		
		StatusAdapter saError = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", message));
		saError.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, title);
		
		assertEquals(title, wrapper.getMainMessage(saError));
		assertEquals(message, wrapper.getSecondaryMessage(saError));
		
		//pretend to have more statuses
		((Collection)dialogState.get(IStatusDialogConstants.STATUS_ADAPTERS)).add(Status.CANCEL_STATUS);
		
		assertEquals(message, wrapper.getMainMessage(saError));
		assertEquals(title, wrapper.getColumnText(saError, 0));
	}
	
	
	public void testDecorating(){
		dialogState.put(IStatusDialogConstants.DECORATOR, new ILabelDecorator() {
			public void removeListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			public boolean isLabelProperty(Object element, String property) {
				// TODO Auto-generated method stub
				return false;
			}
			
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
			
			public void addListener(ILabelProviderListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			public String decorateText(String text, Object element) {
				return "decorated"+text;
			}
			
			public Image decorateImage(Image image, Object element) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		StatusAdapter saError = new StatusAdapter(new Status(IStatus.ERROR, "org.eclipse.ui.tests", "message"));
		saError.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, "title");
		assertEquals("decoratedtitle", wrapper.getMainMessage(saError));
		assertEquals("decoratedmessage", wrapper.getSecondaryMessage(saError));
	}
}
