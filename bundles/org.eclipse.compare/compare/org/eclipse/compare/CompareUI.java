/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare;

import java.util.ResourceBundle;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.IStructureCreatorDescriptor;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;


/**
 * The class <code>CompareUI</code> defines the entry point to initiate a configurable
 * compare operation on arbitrary resources. The result of the compare
 * is opened into a compare editor where the details can be browsed and
 * edited in dynamically selected structure and content viewers.
 * <p>
 * The Compare UI provides a registry for content and structure compare viewers,
 * which is initialized from extensions contributed to extension points
 * declared by this plug-in.
 */
public final class CompareUI {
	
	/**
	 * Name of the title property of a compare viewer.
 	 * If a property with this name is set
 	 * on the top level SWT control of a viewer, it is used as a title in the pane's
 	 * title bar.
 	 */
	public static final String COMPARE_VIEWER_TITLE= "org.eclipse.compare.CompareUI.CompareViewerTitle";
	
	/* (non Javadoc)
	 * non inatiatiable!
	 */
	private CompareUI() {
	}
	
	public static AbstractUIPlugin getPlugin() {
		return CompareUIPlugin.getDefault();
	}
	
	/**
	 * Returns this plug-in's resource bundle.
	 *
	 * @return the plugin's resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		return CompareUIPlugin.getResourceBundle();
	}
	
	/**
	 * Performs the comparison described by the given input and opens a
	 * compare editor on the result.
	 *
	 * @param input the input on which to open the compare editor
	 * @see ICompareEditorInput
	 */
	public static void openCompareEditor(CompareEditorInput input) {
		CompareUIPlugin plugin= CompareUIPlugin.getDefault();
		if (plugin != null)
			plugin.openCompareEditor(input);
	}
			
	/**
	 * Performs the comparison described by the given input and opens a
	 * modal compare dialog on the result.
	 *
	 * @param input the input on which to open the compare dialog
	 * @see ICompareEditorInput
	 */
	public static void openCompareDialog(CompareEditorInput input) {
		CompareUIPlugin plugin= CompareUIPlugin.getDefault();
		if (plugin != null)
			plugin.openCompareDialog(input);
	}
			
	/**
	 * Registers an image descriptor for the given type.
	 *
	 * @param type the type
	 * @param descriptor the image descriptor
	 */
	public static void registerImageDescriptor(String type, ImageDescriptor descriptor) {
		CompareUIPlugin.registerImageDescriptor(type, descriptor);
	}
	
	/**
	 * Returns a shared image for the given type, or a generic image if none
	 * has been registered for the given type.
	 * <p>
	 * Note: Images returned from this method will be automatically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param type the type
	 * @return the image
	 */
	public static Image getImage(String type) {
		return CompareUIPlugin.getImage(type);
	}
	
	/**
	 * Registers the given image for being disposed when this plug-in is shutdown.
	 *
	 * @param image the image to register for disposal
	 */
	public static void disposeOnShutdown(Image image) {
		CompareUIPlugin.disposeOnShutdown(image);
	}
	
	/**
	 * Returns a shared image for the given adaptable.
	 * This convenience method queries the given adaptable
	 * for its <code>IWorkbenchAdapter.getImageDescriptor</code>, which it
	 * uses to create an image if it does not already have one.
	 * <p>
	 * Note: Images returned from this method will be automatically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param adaptable the adaptable for which to find an image
	 * @return an image
	 */
	public static Image getImage(IAdaptable adaptable) {
		return CompareUIPlugin.getImage(adaptable);
	}
		
	/**
	 * Returns a structure compare viewer based on an old viewer and an input object.
	 * If the old viewer is suitable for showing the input, the old viewer
	 * is returned. Otherwise, the input's type is used to find a viewer descriptor in the registry
	 * which in turn is used to create a structure compare viewer under the given parent composite.
	 * If no viewer descriptor can be found <code>null</code> is returned.
	 *
	 * @param oldViewer a new viewer is only created if this old viewer cannot show the given input
	 * @param input the input object for which to find a structure viewer
	 * @param parent the SWT parent composite under which the new viewer is created
	 * @param configuration a configuration which is passed to a newly created viewer
	 * @return the compare viewer which is suitable for the given input object or <code>null</code>
	 */
	public static Viewer findStructureViewer(Viewer oldViewer, ICompareInput input, Composite parent,
				CompareConfiguration configuration) {

		return CompareUIPlugin.findStructureViewer(oldViewer, input, parent, configuration);
	}
	
	/**
	 * Returns a content compare viewer based on an old viewer and an input object.
	 * If the old viewer is suitable for showing the input the old viewer
	 * is returned. Otherwise the input's type is used to find a viewer descriptor in the registry
	 * which in turn is used to create a content compare viewer under the given parent composite.
	 * If no viewer descriptor can be found <code>null</code> is returned.
	 *
	 * @param oldViewer a new viewer is only created if this old viewer cannot show the given input
	 * @param input the input object for which to find a content viewer
	 * @param parent the SWT parent composite under which the new viewer is created
	 * @param configuration a configuration which is passed to a newly created viewer
	 * @return the compare viewer which is suitable for the given input object or <code>null</code>
	 */
	public static Viewer findContentViewer(Viewer oldViewer, ICompareInput input, Composite parent,
			CompareConfiguration configuration) {
		
		return CompareUIPlugin.findContentViewer(oldViewer, input, parent, configuration);
	}
}

