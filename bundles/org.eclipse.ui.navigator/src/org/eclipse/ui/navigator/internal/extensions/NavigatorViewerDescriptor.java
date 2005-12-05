/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.extensions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * Encapsulates the
 * <code>org.eclipse.ui.navigator.viewer</code>
 * extension.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class NavigatorViewerDescriptor implements INavigatorViewerDescriptor { 
	
	private static final String TAG_INCLUDES = "includes"; //$NON-NLS-1$
	private static final String TAG_EXCLUDES = "excludes"; //$NON-NLS-1$
	
	private static final String TAG_CONTENT_EXTENSION = "contentExtension"; //$NON-NLS-1$
	private static final String ATT_PATTERN = "pattern"; //$NON-NLS-1$
	private static final String ATT_IS_ROOT = "isRoot"; //$NON-NLS-1$	

	private final String viewerId; 
	private String popupMenuId = null;
	
	private final Set rootPatterns = new HashSet();
	private final Set includePatterns = new HashSet();	
	private final Set excludePatterns = new HashSet();



	/**
	 * Creates a new content descriptor from a configuration element.
	 * 
	 * @param configElement
	 *            configuration element to create a descriptor from
	 */
	public NavigatorViewerDescriptor(String aViewerId) {
		super();
		this.viewerId = aViewerId;
	}

	public void consume(IConfigurationElement element) throws WorkbenchException {
		
		IConfigurationElement[] includesElement = element.getChildren(TAG_INCLUDES);
		
		if(includesElement.length == 1) {
			IConfigurationElement[] contentExtensionPatterns = includesElement[0].getChildren(TAG_CONTENT_EXTENSION);
			String isRootString = null;
			boolean isRoot = false;
			String patternString = null;
			Pattern compiledPattern = null;
			for(int i=0; i<contentExtensionPatterns.length; i++) {
				isRootString = contentExtensionPatterns[i].getAttribute(ATT_IS_ROOT);
				isRoot = (isRootString != null) ? Boolean.valueOf(isRootString.trim()).booleanValue() : false;
				
				patternString = contentExtensionPatterns[i].getAttribute(ATT_PATTERN);
				if(patternString == null)
					NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Attribute_Missing_Warning, 
							new Object [] {ATT_PATTERN, element.getDeclaringExtension().getUniqueIdentifier(), element.getDeclaringExtension().getNamespace()} ), null);
				else {
					compiledPattern = Pattern.compile(patternString);
					includePatterns.add(compiledPattern);
					if(isRoot)
						rootPatterns.add(compiledPattern);
				}				 
			}
		} else if(includesElement.length >= 1) {
			NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Too_many_elements_Warning, 
					new Object [] {TAG_INCLUDES, element.getDeclaringExtension().getUniqueIdentifier(), element.getDeclaringExtension().getNamespace()} ), null);
		}
		
		IConfigurationElement[] excludesElement = element.getChildren(TAG_EXCLUDES);
		
		if(excludesElement.length == 1) {
			IConfigurationElement[] contentExtensionPatterns = includesElement[0].getChildren(TAG_CONTENT_EXTENSION);
			String patternString = null;
			Pattern compiledPattern = null;
			for(int i=0; i<contentExtensionPatterns.length; i++) { 				 
				
				patternString = contentExtensionPatterns[i].getAttribute(ATT_PATTERN);
				if(patternString == null)
					NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Attribute_Missing_Warning, 
							new Object [] {ATT_PATTERN, element.getDeclaringExtension().getUniqueIdentifier(), element.getDeclaringExtension().getNamespace()} ), null);
				else {
					compiledPattern = Pattern.compile(patternString);
					excludePatterns.add(compiledPattern); 
				}				 
			}
		} else if(excludesElement.length >= 1) {
			NavigatorPlugin.logError(0, NLS.bind(CommonNavigatorMessages.Too_many_elements_Warning, 
					new Object [] {TAG_EXCLUDES, element.getDeclaringExtension().getUniqueIdentifier(), element.getDeclaringExtension().getNamespace()} ), null);
		}
		
//
//		if (rootExtensionId != null) {
//			addRootContentExtensionId(rootExtensionId);
//		} else {
//			IConfigurationElement[] rootContentExtensions = element.getChildren(TAG_ROOT_CONTENT_EXTENSION);
//			for (int i = 0; i < rootContentExtensions.length; i++)
//				addRootContentExtensionId(rootContentExtensions[i]);
//		}
//		if (rootExtensionId == null) {
//			throw new WorkbenchException(
//					NLS.bind(CommonNavigatorMessages.Attribute_Missing_Warning, 
//							new Object [] {ATT_ROOTID, element.getDeclaringExtension().getUniqueIdentifier(), element.getDeclaringExtension().getNamespace()} ));
//					
//		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#getViewerId()
	 */
	public String getViewerId() {
		return viewerId;
	}


	public void setPopupMenuId(String newPopupMenuId) {
 
		if (newPopupMenuId != null) {
			if (popupMenuId != null)
				NavigatorPlugin.log(NLS.bind(CommonNavigatorMessages.NavigatorViewerDescriptor_Popup_Menu_Overridden, new Object [] {getViewerId(), popupMenuId, newPopupMenuId} ));					
			popupMenuId = newPopupMenuId;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#getPopupMenuId()
	 */
	public String getPopupMenuId() {
		return popupMenuId != null ? popupMenuId : viewerId;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#isVisibleExtension(java.lang.String)
	 */
	public boolean isVisibleExtension(String aContentExtensionId) {
		Pattern pattern = null;
		for(Iterator itr = includePatterns.iterator(); itr.hasNext(); ) {
			pattern = (Pattern) itr.next();
			if(pattern.matcher(aContentExtensionId).matches())
				return true;
		}

		for(Iterator itr = excludePatterns.iterator(); itr.hasNext(); ) {
			pattern = (Pattern) itr.next();
			if(pattern.matcher(aContentExtensionId).matches())
				return false;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#isRootExtension(java.lang.String)
	 */
	public boolean isRootExtension(String aContentExtensionId) {
		if(rootPatterns.size() == 0)
			return false;
		Pattern pattern = null;
		for(Iterator itr = rootPatterns.iterator(); itr.hasNext(); ) {
			pattern = (Pattern) itr.next();
			if(pattern.matcher(aContentExtensionId).matches())
				return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.internal.extensions.INavigatorViewerDescriptor#hasOverriddenRootExtensions()
	 */
	public boolean hasOverriddenRootExtensions() {
		return rootPatterns.size() > 0;
	}

	/**
	 * @param descriptor
	 * @return
	 */
	public boolean filtersContentDescriptor(INavigatorContentDescriptor descriptor) {
		// TODO Implment a filter logic component to handle viewers that wish to isolate or exclude
		// specific content extensions

		return false;
	}


}
