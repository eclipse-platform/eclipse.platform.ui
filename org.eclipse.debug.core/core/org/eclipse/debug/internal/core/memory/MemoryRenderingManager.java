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


package org.eclipse.debug.internal.core.memory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.DebugCoreMessages;

/**
 * @since 3.0
 */

public class MemoryRenderingManager implements IMemoryRenderingManager, IDebugEventSetListener, IMemoryBlockListener
{
	private ArrayList listeners = new ArrayList();
	private ArrayList fRenderings = new ArrayList();
	private Hashtable fMemoryRenderingInfo = new Hashtable();
	private ArrayList fRenderingInfoOrderList = new ArrayList();
	private Hashtable fDefaultRenderings = new Hashtable();
	private Hashtable fRenderingBinds = new Hashtable();
	private Hashtable fDynamicRenderingMap = new Hashtable();
	private Hashtable fDynamicRenderingFactory = new Hashtable();
	
	private static final int ADDED = 0;
	private static final int REMOVED = 1;
	
	private static final String RENDERING_EXT = "memoryRenderings"; //$NON-NLS-1$
	private static final String RENDERING_ELEMENT = "rendering"; //$NON-NLS-1$
	private static final String RENDERING_PROPERTY_ELEMENT = "rendering_property"; //$NON-NLS-1$
	private static final String RENDERING_ID = "renderingId"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$
	private static final String DEFAULT_RENDERING="default_renderings"; //$NON-NLS-1$
	private static final String MEMORYBLOCKCLASS = "memoryBlockClass"; //$NON-NLS-1$
	private static final String RENDERINGS="renderingIds"; //$NON-NLS-1$
	private static final String RENDERING_BIND = "rendering_binding"; //$NON-NLS-1$
	private static final String RENDERING_FACTORY = "renderingFactory"; //$NON-NLS-1$
	private static final String DYNAMIC_RENDERING_FACTORY = "dynamicRenderingFactory"; //$NON-NLS-1$
	
	private boolean fHandleAddEvent = true;
		
	
	/**
	 * Notifies a memory block listener  in a safe runnable to
	 * handle exceptions.
	 */
	class MemoryRenderingManagerNotifier implements ISafeRunnable {
		
		private IMemoryRenderingListener fListener;
		private int fType;
		private IMemoryRendering fRendering;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			DebugPlugin.log(exception);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.MemoryBlockRenderingAdded(fRendering);
					break;
				case REMOVED:
					fListener.MemoryBlockRenderingRemoved(fRendering);
					break;
			}			
		}

		/**
		 * Notfied listeners of added/removed rendering events
		 */
		public void notify(int update, IMemoryRendering rendering) {
			if (listeners != null) {
				fType = update;
				fRendering = rendering;
				Object[] copiedListeners= listeners.toArray(new IMemoryRenderingListener[listeners.size()]);
				for (int i= 0; i < copiedListeners.length; i++) {
					fListener = (IMemoryRenderingListener)copiedListeners[i];
					Platform.run(this);
				}			
			}
			fListener = null;
		}
	}
	
	public MemoryRenderingManager()
	{
		MemoryBlockManager.getMemoryBlockManager().addListener(this);
		buildMemoryRenderingInfo();		
	}
	
	/**
	 * Build rendering info in the manager
	 * Then read in extended rendering types
	 */
	private void buildMemoryRenderingInfo()
	{	
		// get rendering extensions
		getExtendedRendering();
	}
	
	/**
	 * Read in and store extension to rendering
	 */
	private void getExtendedRendering() {
		IExtensionPoint rendering= Platform.getExtensionRegistry().getExtensionPoint(DebugPlugin.getUniqueIdentifier(), RENDERING_EXT);
		IExtension[] extensions = rendering.getExtensions();
		
		for (int i=0; i<extensions.length; i++)
		{
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			
			for (int j=0; j<elements.length; j++)
			{
				if (elements[j].getName().equals(RENDERING_ELEMENT))
				{	
					addRendering(elements[j]);
				}
				else if (elements[j].getName().equals(RENDERING_PROPERTY_ELEMENT))
				{
					addRenderingProperty(elements[j]);
				}
				else if (elements[j].getName().equals(DEFAULT_RENDERING))
				{
					addDefaultRenderings(elements[j]);
				}
				else if (elements[j].getName().equals(RENDERING_BIND))
				{
					addRenderingBind(elements[j]);
				}
				else
				{
					DebugPlugin.logMessage("Unknown element in rendering extenstion: " + elements[j].getName(), null); //$NON-NLS-1$			
				}
			}
		}
	}
	
	/**
	 * @param elements
	 * @param j
	 */
	private void addRendering(IConfigurationElement element) {
		
		String renderingId = element.getAttribute(RENDERING_ID);
		String name = element.getAttribute(NAME);
		
		// if any of them is null, do not add, log an error
		if (renderingId == null ||
		    name == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugPlugin.logMessage("Rendering defined is malformed: " + extension, null); //$NON-NLS-1$
		}
		else
		{	
			MemoryRenderingInfo info = new MemoryRenderingInfo(renderingId, name,  element);
			
			if (fMemoryRenderingInfo.containsKey(renderingId))
			{
				// if a rendering already exists with the same id, log a warning of duplicated rendering
				Status status = new Status(IStatus.WARNING, DebugPlugin.getUniqueIdentifier(),	0, 
						"Duplicated rendering definition: " + renderingId, null); //$NON-NLS-1$
				DebugPlugin.log(status);
			}
			
			fMemoryRenderingInfo.put(renderingId, info);
			fRenderingInfoOrderList.add(renderingId);
		}
		
		// get sub-elements from rendering and parse as properties
		IConfigurationElement[] subElements = element.getChildren();
		for (int k=0; k < subElements.length; k++)
		{
			if (subElements[k].getName().equals(RENDERING_PROPERTY_ELEMENT))
			{
				addRenderingProperty(subElements[k]);
			}
			else
			{
				DebugPlugin.logMessage("Unknown element in rendering extenstion: " + element.getName(), null); //$NON-NLS-1$					
			}
		}
	}

	/**
	 * @param elements
	 * @param j
	 */
	private void addRenderingProperty(IConfigurationElement element) {
		String renderingId = element.getAttribute(RENDERING_ID);
		String propertyId = element.getAttribute(NAME);
		String propertyValue = element.getAttribute(VALUE);
		
		if (renderingId == null ||
			propertyId == null || 
			propertyValue == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugPlugin.logMessage("Rendering property defined is malformed: " + extension, null); //$NON-NLS-1$
		}
		else
		{
			// find the rendering
			MemoryRenderingInfo info = (MemoryRenderingInfo)fMemoryRenderingInfo.get(renderingId);
			
			if (info == null){
				DebugPlugin.logMessage("Rendering info for this property is not found: " + propertyId, null); //$NON-NLS-1$
			}
			else
			{	
				// add the property to the rendering
				info.addProperty(propertyId, element);
			}
		}
	}
	
	/**
	 * Process the configuration element into default rendering for
	 * a type of memory block.
	 * @param element
	 */
	private void addDefaultRenderings(IConfigurationElement element)
	{
		String memoryBlockClass = element.getAttribute(MEMORYBLOCKCLASS);
		String renderings = element.getAttribute(RENDERINGS);
		
		if(memoryBlockClass == null || renderings == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugPlugin.logMessage("Default rendering defined is malformed: " + extension, null); //$NON-NLS-1$			
			return;
		}
		
		ArrayList renderingsArray = new ArrayList();
		
		// seperate renderings and create an array
		int idx = renderings.indexOf(","); //$NON-NLS-1$
		if (idx == -1)
		{
			renderingsArray.add(renderings);
		}
		else
		{
			StringTokenizer tokenizer = new StringTokenizer(renderings, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreElements())
			{
				String rendering = tokenizer.nextToken();
				
				rendering = rendering.trim();
				
				// check if rendering is valid
				
				renderingsArray.add(rendering);
			}
		}
		
		if (fDefaultRenderings == null)
		{
			fDefaultRenderings = new Hashtable();
		}
		
		// check hash table to see if something is alreay added
		ArrayList definedrenderings = (ArrayList)fDefaultRenderings.get(memoryBlockClass);
		
		if (definedrenderings == null)
		{
			// add renderings to hashtable
			fDefaultRenderings.put(memoryBlockClass, renderingsArray);
		}
		else
		{
			for (int i=0; i<renderingsArray.size(); i++)
			{
				// append to the list
				if (!definedrenderings.contains(renderingsArray.get(i)))
				{
					definedrenderings.add(renderingsArray.get(i));
				}
			}
		}
	}
	
	private void addRenderingBind(IConfigurationElement element){

		String memoryBlockClass = element.getAttribute(MEMORYBLOCKCLASS);
		String renderings = element.getAttribute(RENDERINGS);
		
		if(memoryBlockClass == null || renderings == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugPlugin.logMessage("Rendering bind defined is malformed: " + extension, null); //$NON-NLS-1$			
			return;
		}
		
		ArrayList renderingsArray = new ArrayList();
		
		// seperate renderings and create an array
		int idx = renderings.indexOf(","); //$NON-NLS-1$
		if (idx == -1)
		{
			renderingsArray.add(renderings);
		}
		else
		{
			StringTokenizer tokenizer = new StringTokenizer(renderings, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreElements())
			{
				String rendering = tokenizer.nextToken();
				rendering = rendering.trim();
				
				// check if rendering is valid
				
				renderingsArray.add(rendering);
			}
		}
		
		if (fRenderingBinds == null)
		{
			fRenderingBinds = new Hashtable();
		}
		
		// check hash table to see if something is alreay added
		ArrayList renderingIds = (ArrayList)fRenderingBinds.get(memoryBlockClass);
		
		if (renderingIds == null)
		{
			// add renderings to hashtable
			fRenderingBinds.put(memoryBlockClass, renderingsArray);
		}
		else
		{
			for (int i=0; i<renderingsArray.size(); i++)
			{
				// append to the list
				if (!renderingIds.contains(renderingsArray.get(i)))
				{
					renderingIds.add(renderingsArray.get(i));
				}
			}
		}
	}

	private MemoryRenderingManagerNotifier getMemoryBlockNotifier() {
		return new MemoryRenderingManagerNotifier();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#addMemoryBlockRendering(org.eclipse.debug.core.model.IMemoryBlock, java.lang.String)
	 */
	public IMemoryRendering addMemoryBlockRendering(IMemoryBlock mem, String renderingId) throws DebugException
	{
		if (fRenderings == null)
			return null;
		
		IMemoryRendering newRendering = createRendering(mem, renderingId);
		
		// if an error has occurred, or if user has canceled
		if (newRendering == null)
			return newRendering;
		
		if (fRenderings.contains(newRendering))
			return newRendering;
		
		fRenderings.add(newRendering);
		
		// add listener for the first memory block added
		if (fRenderings.size() == 1)
		{
			DebugPlugin.getDefault().addDebugEventListener(this);
		}

		notifyListeners(ADDED, newRendering);
		
		return newRendering;
		
	}
	
	/**
	 * @param mem
	 * @param renderingId
	 * @return the memory rendering created by the factory or default rendering.
	 * Returns null if an error has occurred
	 */
	public IMemoryRendering createRendering(IMemoryBlock mem, String renderingId) throws DebugException{
		IMemoryRenderingInfo info = getRenderingInfo(renderingId);
		
		if (info != null){
			IConfigurationElement element = info.getConfigElement();
			
			if (element != null){
				
				String factoryAtt = element.getAttribute(RENDERING_FACTORY);
				
				if (factoryAtt != null){
					Object obj = null;
					try {
						obj = element.createExecutableExtension(RENDERING_FACTORY);
					} catch (CoreException e) {
						
						// throw a debug exception due to error
						IStatus stat = e.getStatus();
						DebugException de = new DebugException(stat);
						throw de;
					}
					
					if (obj == null)
						return new MemoryRendering(mem, renderingId);
					
					if(obj instanceof IMemoryRenderingFactory)
					{	
						IMemoryRenderingFactory factory = (IMemoryRenderingFactory)obj;
						
						IMemoryRendering rendering = null;
						rendering = factory.createRendering(mem, renderingId);		
						return rendering;
					}
				}
			}
			else
			{
				String message= MessageFormat.format(DebugCoreMessages.getString("MemoryRenderingManager.ErrorMsg"), new String[]{renderingId}); //$NON-NLS-1$
				// throw a debug exception because the rendering info cannot be located
				Status status = new Status(IStatus.ERROR, 
						DebugPlugin.getUniqueIdentifier(),
						0, message , null); //$NON-NLS-1$
				DebugException de = new DebugException(status);
				throw de;				
			}
		}
		else
		{
			String message= MessageFormat.format(DebugCoreMessages.getString("MemoryRenderingManager.ErrorMsg"), new String[]{renderingId}); //$NON-NLS-1$
			// throw a debug exception because the rendering info cannot be located
			Status status = new Status(IStatus.ERROR, 
					DebugPlugin.getUniqueIdentifier(),
					0, message, null); //$NON-NLS-1$
			DebugException de = new DebugException(status);
			throw de;
		}
		return new MemoryRendering(mem, renderingId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#removeMemoryBlockRendering(org.eclipse.debug.core.model.IMemoryBlock, java.lang.String)
	 */
	public void removeMemoryBlockRendering(IMemoryBlock mem, String renderingId)
	{
		if(fRenderings == null)
			return;
		
		IMemoryRendering[] toRemove = getRenderings(mem, renderingId);
		
		for (int i=0; i<toRemove.length; i++)
		{
			fRenderings.remove(toRemove[i]);
			
			// remove listener after the last memory block has been removed
			if (fRenderings.size() == 0)
			{
				DebugPlugin.getDefault().removeDebugEventListener(this);
			}
			
			notifyListeners(REMOVED, toRemove[i]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#addMemoryBlockRendering(org.eclipse.debug.ui.IMemoryRendering)
	 */
	public void addMemoryBlockRendering(IMemoryRendering rendering) throws DebugException{
		
		// do not allow duplicated objects
		if (fRenderings.contains(rendering))
			return;
		
		fRenderings.add(rendering);
		
		// add listener for the first memory block added
		if (fRenderings.size() == 1)
		{
			DebugPlugin.getDefault().addDebugEventListener(this);
		}

		notifyListeners(ADDED, rendering);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#removeMemoryBlockRendering(org.eclipse.debug.ui.IMemoryRendering)
	 */
	public void removeMemoryBlockRendering(IMemoryRendering rendering) {
		if(rendering == null)
			return;
		
		if(!fRenderings.contains(rendering))
			return;
		
		fRenderings.remove(rendering);
		
		// remove listener after the last memory block has been removed
		if (fRenderings.size() == 0)
		{
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}
		
		notifyListeners(REMOVED, rendering);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#getRenderings(org.eclipse.debug.core.model.IMemoryBlock, java.lang.String)
	 */
	public IMemoryRendering[] getRenderings(IMemoryBlock mem, String renderingId)
	{
		if (renderingId == null)
		{
			return getRenderingsFromMemoryBlock(mem);
		}
		
		ArrayList ret = new ArrayList();
		for (int i=0; i<fRenderings.size(); i++)
		{
			if (fRenderings.get(i) instanceof IMemoryRendering)
			{
				IMemoryRendering rendering = (IMemoryRendering)fRenderings.get(i);
				if (rendering.getBlock() == mem && renderingId.equals(rendering.getRenderingId()))
				{
					ret.add(rendering);
				}
			}
		}
		
		return (IMemoryRendering[])ret.toArray(new IMemoryRendering[ret.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#getRenderingsFromDebugTarget(org.eclipse.debug.core.model.IDebugTarget)
	 */
	public IMemoryRendering[] getRenderingsFromDebugTarget(IDebugTarget target)
	{
		ArrayList ret = new ArrayList();
		for (int i=0; i<fRenderings.size(); i++)
		{
			if (fRenderings.get(i) instanceof IMemoryRendering)
			{
				IMemoryRendering rendering = (IMemoryRendering)fRenderings.get(i);
				if (rendering.getBlock().getDebugTarget() == target)
				{
					ret.add(rendering);
				}
			}
		}
		
		return (IMemoryRendering[])ret.toArray(new IMemoryRendering[ret.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#getRenderingsFromMemoryBlock(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public IMemoryRendering[] getRenderingsFromMemoryBlock(IMemoryBlock block)
	{
		ArrayList ret = new ArrayList();
		for (int i=0; i<fRenderings.size(); i++)
		{
			if (fRenderings.get(i) instanceof IMemoryRendering)
			{
				IMemoryRendering rendering = (IMemoryRendering)fRenderings.get(i);
				if (rendering.getBlock() == block)
				{
					ret.add(rendering);
				}
			}
		}
		
		return (IMemoryRendering[])ret.toArray(new IMemoryRendering[ret.size()]);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#addListener(org.eclipse.debug.ui.IMemoryBlockListener)
	 */
	public void addListener(IMemoryRenderingListener listener)
	{
		if(listeners == null)
			return;
		
		if(listener == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryRenderingManager.addListener", null); //$NON-NLS-1$
			return;
		}
		
		if (!listeners.contains(listener))
			listeners.add(listener);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#removeListener(org.eclipse.debug.ui.IMemoryBlockListener)
	 */
	public void removeListener(IMemoryRenderingListener listener)
	{
		if(listeners == null)
			return;
		
		if(listener == null){
			DebugPlugin.logMessage("Null argument passed into IMemoryRenderingManager.removeListener", null); //$NON-NLS-1$
			return;
		}		
		
		if (listeners.contains(listener))
			listeners.remove(listener);
		
	}

	private void notifyListeners(int update, IMemoryRendering rendering)
	{
		getMemoryBlockNotifier().notify(update, rendering);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		
		for (int i=0; i < events.length; i++)
			handleDebugEvent(events[i]);
		
	}
	
	public void handleDebugEvent(DebugEvent event) {
		Object obj = event.getSource();
		IDebugTarget dt = null;
		
		if (event.getKind() == DebugEvent.TERMINATE)
		{
			// a terminate event could happen from an IThread or IDebugTarget
			// Only handle terminate event from debug target
			if (obj instanceof IDebugTarget)
			{
				dt = ((IDebugTarget)obj);
			}			
			
			// returns empty array if dt == null
			IMemoryRendering[] deletedrendering = getRenderingsFromDebugTarget(dt);
			
			for (int i=0; i<deletedrendering.length; i++)
			{
				removeMemoryBlockRendering(deletedrendering[i].getBlock(), deletedrendering[i].getRenderingId());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockListener#MemoryBlockAdded(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void MemoryBlockAdded(IMemoryBlock memory)
	{
		if (fHandleAddEvent)
		{
			// get default renderings
			String renderingIds[] = getDefaultRenderings(memory);
			
			// add renderings
			for (int i=0; i<renderingIds.length; i++)
			{
				try {
					addMemoryBlockRendering(memory, renderingIds[i]);
				} catch (DebugException e) {
					// catch error silently
					// log error
					DebugPlugin.logMessage("Cannot create default rendering: " + renderingIds[i], null); //$NON-NLS-1$
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryBlockListener#MemoryBlockRemoved(org.eclipse.debug.core.model.IMemoryBlock)
	 */
	public void MemoryBlockRemoved(IMemoryBlock memory)
	{
		// remove all renderings related to the deleted memory block
		IMemoryRendering[] renderings = getRenderingsFromMemoryBlock(memory);
		
		for (int i=0; i<renderings.length; i++)
		{
			removeMemoryBlockRendering(renderings[i].getBlock(), renderings[i].getRenderingId());
		}	
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#getRenderingInfo(java.lang.String)
	 */
	public IMemoryRenderingInfo getRenderingInfo(String renderingId)
	{
		MemoryRenderingInfo info = (MemoryRenderingInfo)fMemoryRenderingInfo.get(renderingId);
		
		if (info != null) {
			return info;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#getAllRenderingInfo(java.lang.Object)
	 */
	public IMemoryRenderingInfo[] getAllRenderingInfo(Object obj)
	{
		String[] hierarchy = getHierarchy(obj);
		
		ArrayList renderingIds = new ArrayList();
		ArrayList renderingInfos = new ArrayList();
		
		// get all rendering ids
		for (int i=0; i<hierarchy.length; i++)
		{	
			ArrayList ids = (ArrayList)fRenderingBinds.get(hierarchy[i]);
			
			if (ids != null)
			{
				for (int j=0; j<ids.size(); j++)
				{	
					if (!renderingIds.contains(ids.get(j)))
						renderingIds.add(ids.get(j));
				}
			}
		}
		
		// get all rendering infos
		for (int i=0; i<renderingIds.size(); i++){
			IMemoryRenderingInfo info = (IMemoryRenderingInfo)fMemoryRenderingInfo.get(renderingIds.get(i));
			IDynamicRenderingInfo[] dynamic = null;
			
			if (info != null)
				dynamic = getDynamicRenderingInfo(info);
			
			if (dynamic != null)
			{
				for (int j=0; j<dynamic.length; j++)
				{
					IMemoryRenderingInfo dynamicInfo = (IMemoryRenderingInfo)fMemoryRenderingInfo.get(dynamic[j].getRenderingId());
					renderingInfos.add(dynamicInfo);
				}
			}
			else if (info!= null)
			{
				renderingInfos.add(info);
			}
		}
		
		return (IMemoryRenderingInfo[])renderingInfos.toArray(new IMemoryRenderingInfo[renderingInfos.size()]);
	}
	
	private IDynamicRenderingInfo[] getDynamicRenderingInfo(IMemoryRenderingInfo rendering)
	{	
		IConfigurationElement element = rendering.getPropertyConfigElement(DYNAMIC_RENDERING_FACTORY);
		
		try {
			if (element != null){
				
				Object obj;
				
				obj = fDynamicRenderingFactory.get(rendering.getRenderingId());
				
				if (obj == null)
					obj = element.createExecutableExtension(VALUE);
				
				if (obj != null && obj instanceof IDynamicRenderingFactory)
				{
					fDynamicRenderingFactory.put(rendering.getRenderingId(), obj);
					IDynamicRenderingInfo[] dynamicRenderingTypes = ((IDynamicRenderingFactory)obj).getRenderingInfos();
					
					if (dynamicRenderingTypes != null)
					{
						addRenderingInfo(dynamicRenderingTypes);
						
						// now compare the returned list to what is orginally cached
						Enumeration enumeration = fDynamicRenderingMap.keys();
						
						while (enumeration.hasMoreElements())
						{
							String dynamicRenderingId = (String)enumeration.nextElement();
							String staticRenderingId = (String)fDynamicRenderingMap.get(dynamicRenderingId);
													
							if (staticRenderingId.equals(rendering.getRenderingId()))
							{
								boolean found = false;
								// check that this dynamic rendering still exists
								for (int i=0; i<dynamicRenderingTypes.length; i++)
								{
									if (dynamicRenderingTypes[i].getRenderingId().equals(dynamicRenderingId))
									{
										found = true;
										break;
									}
								}
								if (!found)
								{
									// if the rendering no longer exists, remove rendering info
									fMemoryRenderingInfo.remove(dynamicRenderingId);
									fDynamicRenderingMap.remove(dynamicRenderingId);
								}
							}
						}
						
						// update map before returning
						String staticRenderingId = rendering.getRenderingId();
						for (int i=0; i<dynamicRenderingTypes.length; i++)
						{
							fDynamicRenderingMap.put(dynamicRenderingTypes[i].getRenderingId(), staticRenderingId);
						}
						
						return dynamicRenderingTypes;
					}
					return null;
				}
				
			}
		} catch (CoreException e) {
			DebugPlugin.logMessage("Cannot create the dynamic rendering factory for " + element.getDeclaringExtension().getUniqueIdentifier(), null); //$NON-NLS-1$
			return null;
		}
		return null;
	}
	
	private IMemoryRenderingInfo createRenderingInfo(IDynamicRenderingInfo info)
	{
		if (info == null)
			return null;
			
		if (info.getParentRenderingInfo() == null)
		{
			DebugPlugin.logMessage("Dynamic rendering info does not have a parent " +  info.getRenderingId(), null); //$NON-NLS-1$
			return null;		
		}
	
		IMemoryRenderingInfo parent = info.getParentRenderingInfo();
		MemoryRenderingInfo dynamicInfo = new MemoryRenderingInfo(info.getRenderingId(), info.getName(), info.getParentRenderingInfo().getConfigElement());
	
		IConfigurationElement[] properties = parent.getAllProperties();
		
		for (int i=0; i<properties.length; i++)
		{
			String name = properties[i].getAttribute(NAME);
			if (name != null)
			{
				if (!name.equals(DYNAMIC_RENDERING_FACTORY))
					dynamicInfo.addProperty(name, properties[i]);
			}
		}
		
		return dynamicInfo;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingManager#getDefaultRenderings(java.lang.Object)
	 */
	public String[] getDefaultRenderings(Object obj) {
		
		if (fDefaultRenderings == null)
		{
			return new String[0];
		}
		
		if (obj == null)
		{
			return new String[0];
		}
		
		// get all rendering info supporting the object
		IMemoryRenderingInfo[] supported = getAllRenderingInfo(obj);
		ArrayList results = new ArrayList();
		
		// match it with default renderings
		String hierarchy[] = getHierarchy(obj);
		
		// get defaults for the entire hierarchy
		for (int i=0; i<hierarchy.length; i++)
		{
			ArrayList defaults = (ArrayList)fDefaultRenderings.get(hierarchy[i]);
			// if defaults is defined
			if (defaults != null)
			{
				for (int j=0; j<defaults.size(); j++)
				{
					// check if the default is supported
					for (int k=0; k<supported.length; k++)
					{
						if (supported[k].getRenderingId().equals(defaults.get(j)))
						{
							results.add(supported[k].getRenderingId());
						}
					}
				}
			}
		}
		 
		// return the list
		return (String[])results.toArray(new String[results.size()]);
	}
	
	protected void addRenderingInfo (IDynamicRenderingInfo[] dynamicRenderingTypes)
	{
		if (dynamicRenderingTypes != null)
		{	
			// store in fMemoryRenderingInfo arrays so they can be queried
			for (int i=0; i<dynamicRenderingTypes.length; i++)
			{
				IMemoryRenderingInfo dynamicInfo;
				if (fMemoryRenderingInfo.get(dynamicRenderingTypes[i].getRenderingId()) == null)
				{
					dynamicInfo = createRenderingInfo(dynamicRenderingTypes[i]);
					
					if (dynamicInfo != null)
					{
						fMemoryRenderingInfo.put(dynamicRenderingTypes[i].getRenderingId(), dynamicInfo);
					}
				}
			}
		}		
	}
	
	/**
	 * @param obj
	 * @return all superclasses and interfaces
	 */
	private String[] getHierarchy(Object obj)
	{
		ArrayList hierarchy = new ArrayList();
		
		// get class name
		hierarchy.add(obj.getClass().getName());
		
		// get all super classes
		Class superClass = obj.getClass().getSuperclass();
		
		while (superClass != null)
		{
			hierarchy.add(superClass.getName());
			superClass = superClass.getSuperclass();
		}
		
		// get all interfaces
		ArrayList interfaces = new ArrayList();
		Class[] baseInterfaces = obj.getClass().getInterfaces();
		
		for (int i=0; i<baseInterfaces.length; i++)
		{
			interfaces.add(baseInterfaces[i]);
		}
		
		getInterfaces(interfaces, baseInterfaces);
		
		for (int i=0; i<interfaces.size(); i++)
		{
			hierarchy.add(((Class)interfaces.get(i)).getName());
		}
		
		return (String[])hierarchy.toArray(new String[hierarchy.size()]);
		
	}
		
	private void getInterfaces(ArrayList list, Class[] interfaces)
	{	
		Class[] superInterfaces = new Class[0];
		for (int i=0 ;i<interfaces.length; i++)
		{
			superInterfaces = interfaces[i].getInterfaces();
			
			for (int j=0; j<superInterfaces.length; j++)
			{
				list.add(superInterfaces[j]);
			}
			
			getInterfaces(list, superInterfaces);   
		}
	}
	
	/**
	 * Clean up when the plugin is shut down.
	 */
	public void shutdown()
	{
		// clean up
		if (listeners != null)
		{	
			listeners.clear();
			listeners = null;
		}
		
		if (fRenderings != null)
		{	
			fRenderings.clear();
			fRenderings = null;
		}
		
		if (fMemoryRenderingInfo != null)
		{
			fMemoryRenderingInfo.clear();
			fMemoryRenderingInfo = null;
		}
		
		if (fRenderingInfoOrderList != null)
		{	
			fRenderingInfoOrderList.clear();
			fRenderingInfoOrderList = null;
		}
		
		if (fDynamicRenderingMap != null)
		{
			fDynamicRenderingMap.clear();
			fDynamicRenderingMap = null;
		}
		
		if (fDynamicRenderingFactory != null)
		{
			fDynamicRenderingFactory.clear();
			fDynamicRenderingFactory = null;
		}
		
		// remove listener
		MemoryBlockManager.getMemoryBlockManager().removeListener(this);
	}
	
	public void setHandleMemoryBlockAddedEvent(boolean handleEvt)
	{
		fHandleAddEvent = handleEvt;
	}
}
