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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * @since 3.1
 */

public class MemoryRenderingManager implements IMemoryRenderingManager
{
	private Hashtable fMemoryRenderingTypes = new Hashtable();
	private ArrayList fRenderingTypesOrderList = new ArrayList();
	private Hashtable fDynamicRenderingMap = new Hashtable();
	private Hashtable fDynamicRenderingFactory = new Hashtable();
	private Hashtable fRenderingsEnablement = new Hashtable();
	private Hashtable fDefaultsEnablement = new Hashtable();
	
	private static final String RENDERING_EXT = "memoryRenderingTypes"; //$NON-NLS-1$
	private static final String RENDERING_ELEMENT = "rendering"; //$NON-NLS-1$
	private static final String RENDERING_PROPERTY_ELEMENT = "renderingProperty"; //$NON-NLS-1$
	private static final String RENDERING_ID = "renderingId"; //$NON-NLS-1$
	private static final String NAME = "name"; //$NON-NLS-1$
	private static final String VALUE = "value"; //$NON-NLS-1$
	private static final String DEFAULT_RENDERING="defaultRenderings"; //$NON-NLS-1$
	private static final String RENDERINGS="renderingIds"; //$NON-NLS-1$
	private static final String RENDERING_BIND = "renderingBinding"; //$NON-NLS-1$
	private static final String RENDERING_FACTORY = "renderingFactory"; //$NON-NLS-1$
	private static final String DYNAMIC_RENDERING_FACTORY = "dynamicRenderingFactory"; //$NON-NLS-1$
	private static final String ENABLEMENT = "enablement";  //$NON-NLS-1$
	private static final String VIEW_BINDING = "viewBinding";  //$NON-NLS-1$
	private static final String VIEW_IDS = "viewIds";  //$NON-NLS-1$
	
	/**
	 * The singleton memory rendering manager.
	 */
	private static MemoryRenderingManager fgMemoryRenderingManager;
	
	public MemoryRenderingManager()
	{
		buildMemoryRenderingTypes();		
	}
	
	/**
	 * Build rendering info in the manager
	 * Then read in extended rendering types
	 */
	private void buildMemoryRenderingTypes()
	{	
		// get rendering extensions
		processRenderingTypeExtensions();
	}
	
	/**
	 * Read in and store extension to rendering
	 */
	private void processRenderingTypeExtensions() {
		IExtensionPoint rendering= Platform.getExtensionRegistry().getExtensionPoint(DebugUIPlugin.getUniqueIdentifier(), RENDERING_EXT);
		IExtension[] extensions = rendering.getExtensions();
		
		for (int i=0; i<extensions.length; i++)
		{
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			
			for (int j=0; j<elements.length; j++)
			{
				if (elements[j].getName().equals(RENDERING_ELEMENT))
				{	
					addStaticRenderingType(elements[j]);
				}
				else if (elements[j].getName().equals(RENDERING_PROPERTY_ELEMENT))
				{
					addRenderingProperty(elements[j]);
				}
				else if (elements[j].getName().equals(DEFAULT_RENDERING))
				{
					addDefaultRenderingTypes(elements[j]);
				}
				else if (elements[j].getName().equals(RENDERING_BIND))
				{
					addRenderingBind(elements[j]);
				}
				else if (elements[j].getName().equals(VIEW_BINDING))
				{
					addViewBind(elements[j]);
				}
				else
				{
					DebugUIPlugin.logErrorMessage("Unknown element in rendering extenstion: " + elements[j].getName()); //$NON-NLS-1$			
				}
			}
		}
	}
	
	/**
	 * @param element
	 */
	private void addStaticRenderingType(IConfigurationElement element) {
		
		String renderingId = element.getAttribute(RENDERING_ID);
		String name = element.getAttribute(NAME);
		
		// if any of them is null, do not add, log an error
		if (renderingId == null ||
		    name == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugUIPlugin.logErrorMessage("Rendering defined is malformed: " + extension); //$NON-NLS-1$
		}
		else
		{	
			MemoryRenderingType info = new MemoryRenderingType(renderingId, name,  element);
			
			if (fMemoryRenderingTypes.containsKey(renderingId))
			{
				// if a rendering already exists with the same id, log a warning of duplicated rendering
				Status status = new Status(IStatus.WARNING, DebugUIPlugin.getUniqueIdentifier(),	0, 
						"Duplicated rendering definition: " + renderingId, null); //$NON-NLS-1$
				DebugUIPlugin.log(status);
			}
			
			fMemoryRenderingTypes.put(renderingId, info);
			fRenderingTypesOrderList.add(renderingId);
		}
		
		// get sub-elements from rendering and parse as properties
		IConfigurationElement[] subElements = element.getChildren();
		for (int k=0; k < subElements.length; k++)
		{
			if (subElements[k].getName().equals(RENDERING_PROPERTY_ELEMENT))
			{
				addRenderingProperty(subElements[k]);
			}
			else if (subElements[k].getName().equals(VIEW_BINDING))
			{
				addViewBind(subElements[k]);
			}
			else
			{
				DebugUIPlugin.logErrorMessage("Unknown element in rendering extenstion: " + element.getName()); //$NON-NLS-1$					
			}
		}
	}

	/**
	 * @param element
	 */
	private void addRenderingProperty(IConfigurationElement element) {
		String renderingId = element.getAttribute(RENDERING_ID);
		String propertyId = element.getAttribute(NAME);
		String propertyValue = element.getAttribute(VALUE);
		
		if (renderingId == null)
		{
			Object obj = element.getParent();
			
			if (obj != null && obj instanceof IConfigurationElement)
			{
				IConfigurationElement parentElm = (IConfigurationElement)obj;
				renderingId = parentElm.getAttribute(RENDERING_ID);
			}
		}
		
		if (renderingId == null ||
			propertyId == null || 
			propertyValue == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugUIPlugin.logErrorMessage("Rendering property defined is malformed: " + extension); //$NON-NLS-1$
		}
		else
		{
			// find the rendering
			MemoryRenderingType info = (MemoryRenderingType)fMemoryRenderingTypes.get(renderingId);
			
			if (info == null){
				DebugUIPlugin.logErrorMessage("Rendering info for this property is not found: " + propertyId); //$NON-NLS-1$
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
	private void addDefaultRenderingTypes(IConfigurationElement element)
	{
		String renderings = element.getAttribute(RENDERINGS);
		IConfigurationElement[] enablementElms = element.getChildren(ENABLEMENT);
		
		if (renderings == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugUIPlugin.logErrorMessage("Default rendering defined is malformed: " + extension); //$NON-NLS-1$			
			return;			
		}
		
		if(enablementElms == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugUIPlugin.logErrorMessage("Default rendering defined is malformed: " + extension); //$NON-NLS-1$			
			return;			
		}
		
		ArrayList renderingsArray = breakStringIntoArray(renderings);
		
		if (enablementElms != null && enablementElms.length > 0)
		{
			for (int j=0; j<enablementElms.length; j++)
			{
				if (fDefaultsEnablement == null)
					fDefaultsEnablement = new Hashtable();
				
				Expression enablementExp = null;
				try {
					IConfigurationElement enablement = enablementElms[j]; 
					if (enablement != null) {
						enablementExp = ExpressionConverter.getDefault().perform(enablement);
					}
				} catch (CoreException e) {
					String extension = element.getDeclaringExtension().getUniqueIdentifier();
					DebugUIPlugin.logErrorMessage("Cannot create eanblement expression of the default renderings from  " + extension); //$NON-NLS-1$
					return;
				}
				
				ArrayList definedrenderings = (ArrayList)fDefaultsEnablement.get(enablementExp);
				if (definedrenderings == null)
				{
					// add renderings to hashtable
					fDefaultsEnablement.put(enablementExp, renderingsArray);
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
		}
	}
	
	private void addRenderingBind(IConfigurationElement element){

		String renderings = element.getAttribute(RENDERINGS);
		IConfigurationElement[] enablementElms = element.getChildren(ENABLEMENT);
		
		if (renderings == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugUIPlugin.logErrorMessage("Rendering bind defined is malformed: " + extension); //$NON-NLS-1$			
			return;		
		}
		
		if(enablementElms == null)
		{
			String extension = element.getDeclaringExtension().getUniqueIdentifier();
			DebugUIPlugin.logErrorMessage("Rendering bind defined is malformed: " + extension); //$NON-NLS-1$			
			return;					
		}
		
		
		if (enablementElms != null && enablementElms.length > 0)
		{
			for (int j=0; j<enablementElms.length; j++)
			{
				if (fRenderingsEnablement == null)
					fRenderingsEnablement = new Hashtable();
				Expression enablementExp = null;
				try {
					IConfigurationElement enablement = enablementElms[j]; 
					if (enablement != null) {
						enablementExp = ExpressionConverter.getDefault().perform(enablement);
					}
				} catch (CoreException e) {
					String extension = element.getDeclaringExtension().getUniqueIdentifier();
					DebugUIPlugin.logErrorMessage("Cannot create eanblement expression of the rendering bind from  " + extension); //$NON-NLS-1$
					return;
				}
				
				if (enablementExp != null)
				{
					ArrayList renderingsArray = breakStringIntoArray(renderings);
					// check hash table to see if something is alreay added
					ArrayList renderingIds = (ArrayList)fRenderingsEnablement.get(enablementExp);		
					
					if (renderingIds == null)
					{
						fRenderingsEnablement.put(enablementExp, renderingsArray);
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
			}
		}
	}
	
	private void addViewBind(IConfigurationElement element)
	{
		String renderingId = element.getAttribute(RENDERING_ID);
		String viewIds = element.getAttribute(VIEW_IDS);
		
		if (renderingId == null)
		{
			Object obj = element.getParent();
			
			if (obj != null && obj instanceof IConfigurationElement)
			{
				IConfigurationElement parent = (IConfigurationElement) obj;
				renderingId = parent.getAttribute(RENDERING_ID);
			}
		}
		
		if (renderingId == null)
		{
			DebugUIPlugin.logErrorMessage("renderingId not defined in view binding extension: " + element.getDeclaringExtension().getExtensionPointUniqueIdentifier()); //$NON-NLS-1$
			return;
		}
		
		if (viewIds == null)
		{
			DebugUIPlugin.logErrorMessage("viewIds not defined in view binding extension: " + element.getDeclaringExtension().getExtensionPointUniqueIdentifier()); //$NON-NLS-1$
			return;
		}
		
		ArrayList supportedViews = breakStringIntoArray(viewIds);
		
		MemoryRenderingType info = (MemoryRenderingType)fMemoryRenderingTypes.get(renderingId);
		
		if (info == null){
			DebugUIPlugin.logErrorMessage("Rendering info for this rendering type is not found: " + renderingId); //$NON-NLS-1$
		}
		else
		{	
			// add the property to the rendering
			info.addViewBindings((String[])supportedViews.toArray(new String[supportedViews.size()]));
		}
	}

	/**
	 * @param renderings
	 */
	private ArrayList breakStringIntoArray(String str) {
		ArrayList returnedArray = new ArrayList();
		
		// seperate renderings and create an array
		int idx = str.indexOf(","); //$NON-NLS-1$
		if (idx == -1)
		{
			returnedArray.add(str);
		}
		else
		{
			StringTokenizer tokenizer = new StringTokenizer(str, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreElements())
			{
				String rendering = tokenizer.nextToken();
				rendering = rendering.trim();
				
				// check if rendering is valid
				
				returnedArray.add(rendering);
			}
		}
		return returnedArray;
	}
	/**
	 * @param mem
	 * @param renderingId
	 * @return the memory rendering created by the factory or default rendering.
	 * Returns null if an error has occurred
	 */
	public IMemoryRendering createRendering(IMemoryBlock mem, String renderingId) throws DebugException{
		IMemoryRenderingType info = getRenderingTypeById(renderingId);
		
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
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryRenderingManager#getRenderingTypeById(java.lang.String)
	 */
	public IMemoryRenderingType getRenderingTypeById(String renderingId)
	{
		MemoryRenderingType info = (MemoryRenderingType)fMemoryRenderingTypes.get(renderingId);
		
		if (info != null) {
			return info;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryRenderingManager#getRenderingTypes(java.lang.Object)
	 */
	public IMemoryRenderingType[] getRenderingTypes(Object obj)
	{	
		ArrayList renderingIds = new ArrayList();
		
		// get all rendering ids from fRenderingsEnablement (supported way of enabling a memory rendering type)
		Enumeration enumeration = fRenderingsEnablement.keys();
		EvaluationContext evalContext = new EvaluationContext(null, obj);
		while (enumeration.hasMoreElements())
		{
			Object key = enumeration.nextElement();
			try {
				if (key instanceof Expression)
				{
					Expression expression = (Expression)key;
					EvaluationResult result = expression.evaluate(evalContext);
					if (result.equals(EvaluationResult.TRUE))
					{
						ArrayList ids = (ArrayList)fRenderingsEnablement.get(expression);
						if (ids != null)
						{
							for (int j=0; j<ids.size(); j++)
							{	
								if (!renderingIds.contains(ids.get(j)))
									renderingIds.add(ids.get(j));
							}
						}						
					}
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		
		return getRenderingTypesByIds((String[])renderingIds.toArray(new String[renderingIds.size()]));
		
	}
	
	
	

	/**
	 * @param renderingIds
	 * @param renderingTypes
	 */
	private IMemoryRenderingType[] getRenderingTypesByIds(String[] renderingIds) {
		ArrayList renderingTypes = new ArrayList();
		
		// get all rendering infos
		for (int i=0; i<renderingIds.length; i++){
			IMemoryRenderingType info = (IMemoryRenderingType)fMemoryRenderingTypes.get(renderingIds[i]);
			IDynamicRenderingType[] dynamic = null;
			
			if (info != null)
				dynamic = getDynamicRenderingType(info);
			
			if (dynamic != null)
			{
				for (int j=0; j<dynamic.length; j++)
				{
					IMemoryRenderingType dynamicInfo = (IMemoryRenderingType)fMemoryRenderingTypes.get(dynamic[j].getRenderingId());
					renderingTypes.add(dynamicInfo);
				}
			}
			else if (info!= null)
			{
				renderingTypes.add(info);
			}
		}
		return (IMemoryRenderingType[])renderingTypes.toArray(new IMemoryRenderingType[renderingTypes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryRenderingManager#getRenderingTypes(java.lang.Object, java.lang.String)
	 */
	public IMemoryRenderingType[] getRenderingTypes(Object obj, String viewId) {
		IMemoryRenderingType[] renderingTypes = getRenderingTypes(obj);
		ArrayList returnList = new ArrayList();
		
		for (int i=0; i<renderingTypes.length; i++)
		{
			String[] viewIds = renderingTypes[i].getSupportedViewIds();

			for (int j=0; j<viewIds.length; j++)
			{
				if (viewIds[j].equals(viewId))
					returnList.add(renderingTypes[i]);
				
			}
		}
		
		return (IMemoryRenderingType[])returnList.toArray(new IMemoryRenderingType[returnList.size()]);
	}
	
	private IDynamicRenderingType[] getDynamicRenderingType(IMemoryRenderingType rendering)
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
					IDynamicRenderingType[] dynamicRenderingTypes = ((IDynamicRenderingFactory)obj).getRenderingTypes();
					
					if (dynamicRenderingTypes != null)
					{
						addDynamicRenderingTypes(dynamicRenderingTypes);
						
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
									fMemoryRenderingTypes.remove(dynamicRenderingId);
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
			DebugUIPlugin.logErrorMessage("Cannot create the dynamic rendering factory for " + element.getDeclaringExtension().getUniqueIdentifier()); //$NON-NLS-1$
			return null;
		}
		return null;
	}
	
	private IMemoryRenderingType createRenderingType(IDynamicRenderingType info)
	{
		if (info == null)
			return null;
			
		if (info.getParentRenderingType() == null)
		{
			DebugUIPlugin.logErrorMessage("Dynamic rendering info does not have a parent " +  info.getRenderingId()); //$NON-NLS-1$
			return null;		
		}
	
		IMemoryRenderingType parent = info.getParentRenderingType();
		MemoryRenderingType dynamicInfo = new MemoryRenderingType(info.getRenderingId(), info.getName(), info.getParentRenderingType().getConfigElement());
	
		// copy all properties
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
		
		// copy view bindings
		String[] supportedViews = parent.getSupportedViewIds();
		dynamicInfo.addViewBindings(supportedViews);
		
		return dynamicInfo;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryRenderingManager#getDefaultRenderingTypes(java.lang.Object)
	 */
	public IMemoryRenderingType[] getDefaultRenderingTypes(Object obj) {
		
		if (obj == null)
		{
			return new IMemoryRenderingType[0];
		}
		
		ArrayList results = new ArrayList();
		
		// get default renderings specified via expression enablment
		Enumeration enumeration = fDefaultsEnablement.keys();
		EvaluationContext evalContext = new EvaluationContext(null, obj);
		while (enumeration.hasMoreElements())
		{
			Object key = enumeration.nextElement();
			try {
				if (key instanceof Expression)
				{
					Expression expression = (Expression)key;
					EvaluationResult result = expression.evaluate(evalContext);
					if (result.equals(EvaluationResult.TRUE))
					{
						ArrayList ids = (ArrayList)fDefaultsEnablement.get(expression);
						if (ids != null)
						{
							for (int j=0; j<ids.size(); j++)
							{	
								if (isValidRenderingType(obj, (String)ids.get(j)) && !results.contains(ids.get(j)))
									results.add(ids.get(j));
							}
						}						
					}
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		 
		// return the list
		return  getRenderingTypesByIds((String[])results.toArray(new String[results.size()]));
	}
	
	private boolean isValidRenderingType(Object obj, String renderingId)
	{
		IMemoryRenderingType[] supported = getRenderingTypes(obj);
		for (int i=0; i<supported.length; i++)
		{
			if (supported[i].getRenderingId().equals(renderingId))
				return true;
		}
		return false;
	}
	
	protected void addDynamicRenderingTypes (IDynamicRenderingType[] dynamicRenderingTypes)
	{
		if (dynamicRenderingTypes != null)
		{	
			// store in fMemoryRenderingTypes arrays so they can be queried
			for (int i=0; i<dynamicRenderingTypes.length; i++)
			{
				IMemoryRenderingType dynamicInfo;
				if (fMemoryRenderingTypes.get(dynamicRenderingTypes[i].getRenderingId()) == null)
				{
					dynamicInfo = createRenderingType(dynamicRenderingTypes[i]);
					
					if (dynamicInfo != null)
					{
						fMemoryRenderingTypes.put(dynamicRenderingTypes[i].getRenderingId(), dynamicInfo);
					}
				}
			}
		}		
	}
	
	/**
	 * Clean up when the plugin is shut down.
	 */
	public void shutdown()
	{
		
		if (fMemoryRenderingTypes != null)
		{
			fMemoryRenderingTypes.clear();
			fMemoryRenderingTypes = null;
		}
		
		if (fRenderingTypesOrderList != null)
		{	
			fRenderingTypesOrderList.clear();
			fRenderingTypesOrderList = null;
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
		
	}

	/**
	 * Returns the memory rendering manager.
	 * @return the memory rendering manager.
	 * @see IMemoryRenderingManager
	 * @since 3.0
	 */
	public static IMemoryRenderingManager getMemoryRenderingManager() {
		if (fgMemoryRenderingManager == null)
		{
			fgMemoryRenderingManager = new MemoryRenderingManager();
		}
		
		return fgMemoryRenderingManager;
	}

	public static void pluginShutdown() {
		if (fgMemoryRenderingManager != null) {
			fgMemoryRenderingManager.shutdown();
		}
	}

}
