/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.TabItem;


/**
 * For showing a empty view tab when other plugins fail to create the rendering
 */
public class EmptyViewTab extends AbstractMemoryViewTab {

	TextViewer fTextViewer;
	
	/**
	 * @param newMemory
	 * @param newTab
	 * @param menuMgr
	 * @param renderingId
	 */
	public EmptyViewTab(IMemoryBlock newMemory, TabItem newTab, MenuManager menuMgr, IMemoryRendering rendering) {
		super(newMemory, newTab, menuMgr, rendering);
		
		maintainRefAndEnablement(false);
		
		fTabItem = newTab;
		fTextViewer = new TextViewer(newTab.getParent(), SWT.READ_ONLY);		
		
		fTabItem.setControl(fTextViewer.getControl());
		fTextViewer.setDocument(new Document());
		StyledText styleText = fTextViewer.getTextWidget();
		
		styleText.setText("\r\n\r\n" + DebugUIMessages.getString("EmptyViewTab.Unable_to_create") + "\n" + getRenderingName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		
		setTabName(newMemory);
	}
	

	/**
	 * 
	 */
	private String getRenderingName() {
		String name =
			MemoryBlockManager
			.getMemoryRenderingManager()
			.getRenderingInfo(getRenderingId())
			.getName();
		
		return name;
	}


	protected void setTabName(IMemoryBlock newMemory)
	{
		String tabName = null;

		tabName = ""; //$NON-NLS-1$
		try {
			if (newMemory instanceof IExtendedMemoryBlock) {
				tabName = ((IExtendedMemoryBlock) newMemory).getExpression();
				
				if (tabName == null)
				{
					tabName = DebugUIMessages.getString("EmptyViewTab.Unknown"); //$NON-NLS-1$
				}				
				
			} else {
				long address = newMemory.getStartAddress();
				tabName = Long.toHexString(address);
			}
		} catch (DebugException e) {
			tabName = DebugUIMessages.getString("EmptyViewTab.Unknown"); //$NON-NLS-1$
		}

		String name = getRenderingName();

		if (name != null)
			tabName += " <" + name + ">"; //$NON-NLS-1$ //$NON-NLS-2$

		fTabItem.setText(tabName);	
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#goToAddress(java.math.BigInteger)
	 */
	public void goToAddress(BigInteger address) throws DebugException {

	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#resetAtBaseAddress()
	 */
	public void resetAtBaseAddress() throws DebugException {

	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#refresh()
	 */
	public void refresh() {

	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#isDisplayingError()
	 */
	public boolean isDisplayingError() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#isEnabled()
	 */
	public boolean isEnabled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#setTabLabel(java.lang.String)
	 */
	public void setTabLabel(String label) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#getTabLabel()
	 */
	public String getTabLabel() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#getSelectedAddress()
	 */
	public BigInteger getSelectedAddress() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#getSelectedContent()
	 */
	public String getSelectedContent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryViewTab#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		// do not do anything
		// this view tab should never become an
		// enabled renderence to the memory block
	}
}
