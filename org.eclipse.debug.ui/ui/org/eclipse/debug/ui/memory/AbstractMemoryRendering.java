/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.memory;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.views.memory.PropertyChangeNotifier;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Abstract implementation of a memory rendering.
 * <p>
 * To contribute an action to a rendering, an <code>objectContribution</code> can
 * be used on a rendering implementation class itself using a 
 * <code>popupMenus</code> extension. Additionaly, the context menu created
 * by <code>createPopupMenu()</code> is registered with an identifier of this
 * rendering's container identifier. Actions may also be contributed to the
 * container's context menu specifically by using a  <code>viewerContribution</code>
 * on a <code>popupMenus</code> extension that has a <code>targetID</code> refering
 * to this rendering container's identifier. 
 * </p>
 * <p>
 * Clients implementing memory renderings must subclass this class.
 * </p>
 * @since 3.1
 */
public abstract class AbstractMemoryRendering extends PlatformObject implements IMemoryRendering{

	private IMemoryBlock fMemoryBlock;
	private IMemoryRenderingContainer fContainer;
	private ListenerList fPropertyListeners;
	private boolean fVisible = true;
	private MenuManager fPopupMenuMgr;
	private String fRenderingId;
	
	/**
	 * Cient may provide a label decorator adapter from its memory block
	 * to decorate the label of a rendering.
	 * @since 3.2 
	 */
	private ILabelDecorator fLabelDecorator;
	
	/**
	 * Constructs a new rendering of the given type.
	 * 
	 * @param renderingId memory rendering type identifier
	 */
	public AbstractMemoryRendering(String renderingId)
	{
		fRenderingId = renderingId;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#init(org.eclipse.debug.ui.memory.IMemoryRenderingSite, org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void init(IMemoryRenderingContainer container, IMemoryBlock block) {
		fContainer = container;
		fMemoryBlock = block;
		
		fLabelDecorator = (ILabelDecorator)fMemoryBlock.getAdapter(ILabelDecorator.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#dispose()
	 */
	public void dispose()
	{
		// disconnect from memory block when rendering is disposed
		if (fMemoryBlock instanceof IMemoryBlockExtension)
		{
			((IMemoryBlockExtension)fMemoryBlock).disconnect(this);
		}
		
		if (fPropertyListeners != null)
			fPropertyListeners = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#activated()
	 */
	public void activated() {
		if (fContainer.getMemoryRenderingSite().getSynchronizationService() != null)
			fContainer.getMemoryRenderingSite().getSynchronizationService().setSynchronizationProvider(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#deactivated()
	 */
	public void deactivated() {
		// do nothing when deactivated
		// we do not want to set the sync provider from rendering site
		// to null because Linux GTK unexpectedly fires deactivated event.
		// If we reset the sync provider upon a deactivated event, it screws
		// up synchronizatoin on Linux GTK.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#becomesVisible()
	 */
	public void becomesVisible() {
		fVisible = true;
		if (fMemoryBlock instanceof IMemoryBlockExtension)
		{
			((IMemoryBlockExtension)fMemoryBlock).connect(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#becomesHidden()
	 */
	public void becomesHidden() {
		fVisible = false;
		if (fMemoryBlock instanceof IMemoryBlockExtension)
		{
			((IMemoryBlockExtension)fMemoryBlock).disconnect(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#getMemoryBlock()
	 */
	public IMemoryBlock getMemoryBlock() {
		return fMemoryBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#getRenderingId()
	 */
	public String getRenderingId()
	{
		return fRenderingId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
				
		if (fPropertyListeners == null)
			fPropertyListeners = new ListenerList();
		
		fPropertyListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		
		if (fPropertyListeners == null)
			return;
		fPropertyListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#getImage()
	 */
	public Image getImage() {
		return decorateImage(null);
	}
	
	/**
	 * Decorates and returns this rendering's image.
	 * 
	 * @param image base image
	 * @return decorated image
	 * @since 3.2
	 */	
	protected Image decorateImage(Image image) {
		if (fLabelDecorator != null)
			return fLabelDecorator.decorateImage(image, this);
		return image;		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRendering#getLabel()
	 */
	public  String getLabel()
	{
		if (fMemoryBlock == null)
			return ""; //$NON-NLS-1$
		
		StringBuffer label = new StringBuffer(""); //$NON-NLS-1$
		
		if (fMemoryBlock instanceof IMemoryBlockExtension)
		{
			String expression = ((IMemoryBlockExtension)fMemoryBlock).getExpression();
			
			if (expression == null)
				expression = ""; //$NON-NLS-1$
			
			label.append(expression);
			
			if (expression.startsWith("&")) //$NON-NLS-1$
				label.insert(0, "&"); //$NON-NLS-1$		
			
			// TODO:  also may call #getBigBaseAddress on UI thread
			// show full address if the rendering is visible
			// hide address if the rendering is invisible
			try {
				if (fVisible && ((IMemoryBlockExtension)fMemoryBlock).getBigBaseAddress() != null)
				{	
					label.append(" : 0x"); //$NON-NLS-1$
					label.append(((IMemoryBlockExtension)fMemoryBlock).getBigBaseAddress().toString(16).toUpperCase());
				}
			} catch (DebugException e) {
				// do nothing... the label will not show memory block's address
			}
		}
		else
		{
			long address = fMemoryBlock.getStartAddress();
			label.append(Long.toHexString(address).toUpperCase());
		}
		
		IMemoryRenderingType type = DebugUITools.getMemoryRenderingManager().getRenderingType(getRenderingId());
		
		if (type != null)
		{
			String preName = type.getLabel();
			
			if (preName != null)
			{
				label.append(" <"); //$NON-NLS-1$
				label.append(preName);
				label.append(">");  //$NON-NLS-1$
			}
		}
		
		return decorateLabel(label.toString());
	}
	
	/**
	 * Decorates and returns this rendering's label.
	 * 
	 * @param label base label
	 * @return decorated label
	 * @since 3.2
	 */
	protected String decorateLabel(String label) {
		if (fLabelDecorator != null)
			return fLabelDecorator.decorateText(label.toString(), this);
		return label.toString();		
	}
	
	/**
	 * Helper method for creating a pop up menu in the rendering for a control.
	 * Call this method when a context menu is required for a control
	 * in a rendering.
	 * <p>
	 * To contribute an action to a rendering, an <code>objectContribution</code> can
	 * be used on a rendering implementation class itself using a 
	 * <code>popupMenus</code> extension. Additionaly, the context menu created
	 * by this method is registered with an identifier of this rendering's container.
	 * Actions may also be contributed to the context menu specifically by using a 
	 * <code>viewerContribution</code> on a <code>popupMenus</code> extension
	 * that has a <code>targetID</code> refering to this rendering container's identifier. 
	 * </p>
	 * <p>
	 * Clients are expected to become a menu listener for their pop  up 
	 * menu if they require to fill the context menu for the rendering.
	 * </p>
	 * @param control - control to create the pop up menu for
	 */
	protected void createPopupMenu(Control control)
	{
		if (fPopupMenuMgr == null)
		{
			fPopupMenuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			fPopupMenuMgr.setRemoveAllWhenShown(true);
			IMemoryRenderingSite site = fContainer.getMemoryRenderingSite();
			String menuId = fContainer.getId();
						
			ISelectionProvider selProvider = site.getSite().getSelectionProvider();
			
			// must add additions seperator before registering the menu
			// otherwise, we will get an error
			fPopupMenuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				}});
			
			site.getSite().registerContextMenu(menuId, fPopupMenuMgr, selProvider);
		}
		
		Menu popupMenu = fPopupMenuMgr.createContextMenu(control);
		control.setMenu(popupMenu);
	}
	
	/**
	 * Returns the pop up menu manager for this rendering, or <code>null</code>
	 * if none.
	 * 
	 * @return the pop up menu manager for this rendering, or <code>null</code>
	 */
	protected MenuManager getPopupMenuManager()
	{
		return fPopupMenuMgr;
	}
	
	/**
	 * Fires the given event to all registered listeners.
	 * 
	 * @param event the event to fire
	 */
	protected void firePropertyChangedEvent(PropertyChangeEvent event)
	{
		if (fPropertyListeners == null)
			return;
		
		Object[] listeners = fPropertyListeners.getListeners();
		
		for (int i=0; i<listeners.length; i++)
		{	
			PropertyChangeNotifier notifier = new PropertyChangeNotifier((IPropertyChangeListener)listeners[i], event);
			Platform.run(notifier);
		}
	}
	
	/**
	 * Returns the container hosting this memory rendering.
	 * 
	 * @return the container hosting this memory rendering
	 */
	public IMemoryRenderingContainer getMemoryRenderingContainer()
	{
		return fContainer;
	}

	/**
	 * Returns whether this rendering is currently visible.
	 * 
	 * @return whether this rendering is currently visible
	 */
	public boolean isVisible() {
		return fVisible;
	}
}
