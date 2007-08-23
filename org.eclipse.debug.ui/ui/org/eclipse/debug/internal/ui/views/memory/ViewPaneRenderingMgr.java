/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A View Pane Rendering Manager manages all the rendering from a view pane.
 * It is responsible for handling debug events and removing renderings
 * from the view pane as a debug session is terminated.
 * In addition, the rendering manager is responsible for persisting memory renderings.
 * Renderings need to be persisted when the memory view is disposed.  If the view is
 * opened again, the same set of renderings will be created in the view pane if 
 * the renderings are still valid.
 * @since 3.1
 */
public class ViewPaneRenderingMgr implements IDebugEventSetListener{

	private ArrayList fRenderings = new ArrayList();
	private IMemoryRenderingContainer fViewPane;
	
	private static final String RENDERINGS_TAG = "persistedMemoryRenderings"; //$NON-NLS-1$
	private static final String MEMORY_RENDERING_TAG = "memoryRendering"; //$NON-NLS-1$
	private static final String MEMORY_BLOCK = "memoryBlock"; //$NON-NLS-1$
	private static final String RENDERING_ID = "renderingId"; //$NON-NLS-1$
	
	public ViewPaneRenderingMgr(IMemoryRenderingContainer viewPane)
	{
		fViewPane = viewPane;
		loadPersistedRenderings(getPrefId());
	}

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
		}
		
		storeRenderings();
	}
	

	public void addMemoryBlockRendering(IMemoryRendering rendering) {
		
		// do not allow duplicated objects
		if (fRenderings.contains(rendering))
			return;
		
		fRenderings.add(rendering);
		
		// add listener for the first memory block added
		if (fRenderings.size() == 1)
		{
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
		
		storeRenderings();
	}


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
		
		storeRenderings();
	}

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
				if (rendering.getMemoryBlock() == mem && renderingId.equals(rendering.getRenderingId()))
				{
					ret.add(rendering);
				}
			}
		}
		
		return (IMemoryRendering[])ret.toArray(new IMemoryRendering[ret.size()]);
	}
	
	public IMemoryRendering[] getRenderings()
	{
		return (IMemoryRendering[])fRenderings.toArray(new IMemoryRendering[fRenderings.size()]);
	}

	public IMemoryRendering[] getRenderingsFromDebugTarget(IDebugTarget target)
	{
		ArrayList ret = new ArrayList();
		for (int i=0; i<fRenderings.size(); i++)
		{
			if (fRenderings.get(i) instanceof IMemoryRendering)
			{
				IMemoryRendering rendering = (IMemoryRendering)fRenderings.get(i);
				if (rendering.getMemoryBlock().getDebugTarget() == target)
				{
					ret.add(rendering);
				}
			}
		}
		
		return (IMemoryRendering[])ret.toArray(new IMemoryRendering[ret.size()]);
	}
	

	public IMemoryRendering[] getRenderingsFromMemoryBlock(IMemoryBlock block)
	{
		ArrayList ret = new ArrayList();
		for (int i=0; i<fRenderings.size(); i++)
		{
			if (fRenderings.get(i) instanceof IMemoryRendering)
			{
				IMemoryRendering rendering = (IMemoryRendering)fRenderings.get(i);
				if (rendering.getMemoryBlock() == block)
				{
					ret.add(rendering);
				}
			}
		}
		
		return (IMemoryRendering[])ret.toArray(new IMemoryRendering[ret.size()]);		
	}	
	
	

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
				
				// returns empty array if dt == null
				IMemoryRendering[] deletedrendering = getRenderingsFromDebugTarget(dt);
				
				for (int i=0; i<deletedrendering.length; i++)
				{
					removeMemoryBlockRendering(deletedrendering[i].getMemoryBlock(), deletedrendering[i].getRenderingId());
					fViewPane.removeMemoryRendering(deletedrendering[i]);
				}
			}
		}
	}

	public void dispose()
	{
		// remove all renderings
		fRenderings.clear();
		
		String secondaryId = getViewSiteSecondaryId();
		if (secondaryId != null)
		{
			// do not save renderings if this is not the primary rendering view
			String prefid = getPrefId();
			Preferences prefs = DebugUIPlugin.getDefault().getPluginPreferences();
			prefs.setToDefault(prefid);
		}
			
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	/**
	 * Store renderings as preferences.  If renderings are stored, renderings
	 * can be persisted even after the memory view is closed.
	 */
	private void storeRenderings()
	{
		Preferences prefs = DebugUIPlugin.getDefault().getPluginPreferences();
		String renderingsStr= IInternalDebugCoreConstants.EMPTY_STRING;
		try {
			renderingsStr= getRenderingsAsXML();
		} catch (IOException e) {
			DebugUIPlugin.log(e);
		} catch (ParserConfigurationException e) {
			DebugUIPlugin.log(e);
		} catch (TransformerException e) {
			DebugUIPlugin.log(e);
		}
		
		String prefid = getPrefId();
		
		if (renderingsStr != null)
			prefs.setValue(prefid, renderingsStr);
		else
			prefs.setToDefault(prefid);
	}

	private String getPrefId() {
		// constructs id based on memory view's secondary id + the rendering view pane id
		// format:  secondaryId:viewPaneId
		StringBuffer id = new StringBuffer();
		IMemoryRenderingSite renderingSite = fViewPane.getMemoryRenderingSite();
		IWorkbenchPartSite ps = renderingSite.getSite();
		if (ps instanceof IViewSite)
		{
			IViewSite vs = (IViewSite)ps;
			String secondaryId = vs.getSecondaryId();
			if (secondaryId != null)
			{
				id.append(secondaryId);
				id.append(":"); //$NON-NLS-1$
			}
			
		}
		id.append(fViewPane.getId());
		String prefId = id.toString();
		return prefId;
	}
	
	/**
	 * Convert renderings to xml text
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private String getRenderingsAsXML() throws IOException, ParserConfigurationException, TransformerException {
		IMemoryRendering[] renderings= (IMemoryRendering[])fRenderings.toArray(new IMemoryRendering[fRenderings.size()]);
		
		if (renderings.length == 0)
			return null;
		
		Document document= LaunchManager.getDocument();
		Element rootElement= document.createElement(RENDERINGS_TAG);
		document.appendChild(rootElement);
		for (int i = 0; i < renderings.length; i++) {
			IMemoryRendering rendering= renderings[i];
			Element element= document.createElement(MEMORY_RENDERING_TAG); 
			element.setAttribute(MEMORY_BLOCK, Integer.toString(rendering.getMemoryBlock().hashCode()));
			element.setAttribute(RENDERING_ID, rendering.getRenderingId());
			rootElement.appendChild(element);
		}
		return LaunchManager.serializeDocument(document);
	}

	/**
	 * Load renderings currently stored.
	 */
	private void loadPersistedRenderings(String prefId) {
		String renderingsStr= DebugUIPlugin.getDefault().getPluginPreferences().getString(prefId);
		if (renderingsStr.length() == 0) {
			return;
		}
		Element root;
		try {
			root = DebugPlugin.parseDocument(renderingsStr);
		} catch (CoreException e) {
			DebugUIPlugin.logErrorMessage("An exception occurred while loading memory renderings."); //$NON-NLS-1$
			return;
		}
		if (!root.getNodeName().equals(RENDERINGS_TAG)) {
			DebugUIPlugin.logErrorMessage("Invalid format encountered while loading memory renderings."); //$NON-NLS-1$
			return;
		}
		NodeList list= root.getChildNodes();
		boolean renderingsAdded= false;
		for (int i= 0, numItems= list.getLength(); i < numItems; i++) {
			Node node= list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (!element.getNodeName().equals(MEMORY_RENDERING_TAG)) {
					DebugUIPlugin.logErrorMessage("Invalid XML element encountered while loading memory rendering."); //$NON-NLS-1$
					continue;
				}
				String memoryBlockHashCode= element.getAttribute(MEMORY_BLOCK);
				String renderingId = element.getAttribute(RENDERING_ID);
				
				IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks();
				IMemoryBlock memoryBlock = null;
				for (int j=0; j<memoryBlocks.length; j++)
				{
					if (Integer.toString(memoryBlocks[j].hashCode()).equals(memoryBlockHashCode))
						memoryBlock = memoryBlocks[j];
				}
				
				if (memoryBlock != null)
				{
					IMemoryRenderingType[] types  = DebugUITools.getMemoryRenderingManager().getRenderingTypes(memoryBlock);
					IMemoryRenderingType type = null;
					for (int k=0; k<types.length; k++)
					{
						if (types[k].getId().equals(renderingId))
							type = types[k];
					}
				
					// if memory block is not found, the rendering is no longer valid
					// simply ignore the rendering
					if (type != null)
					{
						try {
		
							IMemoryRendering rendering = type.createRendering();
							if (rendering != null)
							{
								rendering.init(fViewPane, memoryBlock);
								if (!fRenderings.contains(rendering))
								{
								    fRenderings.add(rendering);
								    renderingsAdded= true;
								}
							}
						} catch (CoreException e1) {
							DebugUIPlugin.log(e1);
						}
					}
				}
			}
		}
		if (renderingsAdded) {
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
	}	
	
	/**
	 * @return secondary id, or null if not available
	 */
	private String getViewSiteSecondaryId()
	{
		IMemoryRenderingSite renderingSite = fViewPane.getMemoryRenderingSite();
		IWorkbenchPartSite ps = renderingSite.getSite();
		if (ps instanceof IViewSite)
		{
			IViewSite vs = (IViewSite)ps;
			String secondaryId = vs.getSecondaryId();
			return secondaryId;
		}
		return null;
	}
}
