/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.internal.SharedImages;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.*;
import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * The Compare UI plug-in defines the entry point to initiate a configurable
 * compare operation on arbitrary resources. The result of the compare
 * is opened into a compare editor where the details can be browsed and
 * edited in dynamically selected structure and content viewers.
 * <p>
 * The Compare UI provides a registry for content and structure compare viewers,
 * which is initialized from extensions contributed to extension points
 * declared by this plug-in.
 * <p>
 * This class is the plug-in runtime class for the 
 * <code>"org.eclipse.compare"</code> plug-in.
 * </p>
 */
public final class CompareUIPlugin extends AbstractUIPlugin {
			
	private static boolean NORMALIZE_CASE= true;

	private final static String CLASS_ATTRIBUTE= "class";
	private final static String EXTENSIONS_ATTRIBUTE= "extensions";

	private static final String RESOURCE_BUNDLE= "org.eclipse.compare.internal.ComparePluginResources";

	private static final String PLUGIN_ID= "org.eclipse.compare";

	private static final String STRUCTURE_CREATOR_EXTENSION_POINT= "structureCreators";
	private static final String STRUCTURE_MERGEVIEWER_EXTENSION_POINT= "structureMergeViewers";
	private static final String CONTENT_MERGEVIEWER_EXTENSION_POINT= "contentMergeViewers";
	private static final String CONTENT_VIEWER_EXTENSION_POINT= "contentViewers";
	
	private static final String COMPARE_EDITOR= "org.eclipse.compare.CompareEditor";
	
	private static final String COMPARE_FAILED= "Compare failed";
	private static final String PROBLEMS_OPENING_EDITOR= "Problems Opening Editor";
	
	/** Maps type to icons */
	private static Map fgImages= new Hashtable(10);
	/** Maps type to ImageDescriptors */
	private static Map fgImageDescriptors= new Hashtable(10);
	/** Maps ImageDescriptors to Images */
	private static Map fgImages2= new Hashtable(10);
	
	private static Map fgStructureCreators= new Hashtable(10);
	private static Map fgStructureViewerDescriptors= new Hashtable(10);
	private static Map fgContentViewerDescriptors= new Hashtable(10);
	private static Map fgContentMergeViewerDescriptors= new Hashtable(10);
	
	private static List fgDisposeOnShutdownImages= new ArrayList();
	
	private static ResourceBundle fgResourceBundle;

	private static CompareUIPlugin fgComparePlugin;

	/**
	 * Creates the <code>CompareUIPlugin</code> object and registers all
	 * structure creators, content merge viewers, and structure merge viewers
	 * contributed to this plug-in's extension points.
	 * <p>
	 * Note that instances of plug-in runtime classes are automatically created 
	 * by the platform in the course of plug-in activation.
	 * </p>
	 *
	 * @param descriptor the plug-in descriptor
	 */
	public CompareUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
				
		fgComparePlugin= this;
		
		registerExtensions();
	}
	
	/**
	 * Registers all structure creators, content merge viewers, and structure merge viewers
	 * that are found in the XML plugin files.
	 */
	private void registerExtensions() {
		IPluginRegistry registry= Platform.getPluginRegistry();
		
		// collect all IStructureCreators
		IConfigurationElement[] elements= registry.getConfigurationElementsFor(PLUGIN_ID, STRUCTURE_CREATOR_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			final IConfigurationElement conf= elements[i];
			String extensions= conf.getAttribute(EXTENSIONS_ATTRIBUTE);
			registerStructureCreator(extensions,
				new IStructureCreatorDescriptor() {
					public IStructureCreator createStructureCreator() {
						try {
							return (IStructureCreator) conf.createExecutableExtension(CLASS_ATTRIBUTE);
						} catch (CoreException ex) {
						}
						return null;
					}
				}
			);
		}
				
		// collect all viewers which define the structure mergeviewer extension point
		elements= registry.getConfigurationElementsFor(PLUGIN_ID, STRUCTURE_MERGEVIEWER_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			ViewerDescriptor desc= new ViewerDescriptor(elements[i]);
			String ext= desc.getExtension();
			if (ext != null)
				registerStructureViewerDescriptor(desc.getExtension(), desc);
		}
		
		// collect all viewers which define the content mergeviewer extension point
		elements= registry.getConfigurationElementsFor(PLUGIN_ID, CONTENT_MERGEVIEWER_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			ViewerDescriptor desc= new ViewerDescriptor(elements[i]);
			String ext= desc.getExtension();
			if (ext != null)
				registerContentMergeViewerDescriptor(desc.getExtension(), desc);
		}
		
		// collect all viewers which define the content viewer extension point
		elements= registry.getConfigurationElementsFor(PLUGIN_ID, CONTENT_VIEWER_EXTENSION_POINT);
		for (int i= 0; i < elements.length; i++) {
			ViewerDescriptor desc= new ViewerDescriptor(elements[i]);
			String ext= desc.getExtension();
			if (ext != null)
				registerContentViewerDescriptor(desc.getExtension(), desc);
		}
	}
	
	/**
	 * Returns the singleton instance of this plug-in runtime class.
	 *
	 * @return the compare plug-in instance
	 */
	public static CompareUIPlugin getDefault() {
		return fgComparePlugin;
	}
	
	/**
	 * Returns this plug-in's resource bundle.
	 *
	 * @return the plugin's resource bundle
	 */
	public static ResourceBundle getResourceBundle() {
		if (fgResourceBundle == null)
			fgResourceBundle= ResourceBundle.getBundle(RESOURCE_BUNDLE);
		return fgResourceBundle;
	}
	
	/**
	 * Returns the active workkbench page or <code>null</code> if
	 * no active workkbench page can be determined.
	 *
	 * @return the active workkbench page or <code>null</code> if
	 * 	no active workkbench page can be determined
	 */
	private static IWorkbenchPage getActivePage() {
		CompareUIPlugin plugin= getDefault();
		if (plugin == null)
			return null;
		IWorkbench workbench= plugin.getWorkbench();
		if (workbench == null)
			return null;	
		IWorkbenchWindow window= workbench.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getActivePage();
	}
	
	/**
	 * Returns the SWT Shell of the active workbench window or <code>null</code> if
	 * no workbench window is active.
	 *
	 * @return the SWT Shell of the active workbench window, or <code>null</code> if
	 * 	no workbench window is active
	 */
	public static Shell getShell() {
		CompareUIPlugin p= getDefault();
		if (p == null)
			return null;
		IWorkbench wb= p.getWorkbench();
		if (wb == null)
			return null;
		IWorkbenchWindow ww= wb.getActiveWorkbenchWindow();
		if (ww == null)
			return null;
		return ww.getShell();
	}

	/**
	 * Registers the given image for being disposed when this plug-in is shutdown.
	 *
	 * @param image the image to register for disposal
	 */
	public static void disposeOnShutdown(Image image) {
		if (image != null)
			fgDisposeOnShutdownImages.add(image);
	}
	
	/* (non-Javadoc)
	 * Method declared on Plugin.
	 * Frees all resources of the compare plug-in.
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		
		if (fgDisposeOnShutdownImages != null) {
			Iterator i= fgDisposeOnShutdownImages.iterator();
			while (i.hasNext()) {
				Image img= (Image) i.next();
				if (!img.isDisposed())
					img.dispose();
			}
			fgImages= null;
		}
	}
	
	/**
	 * Performs the comparison described by the given input and opens a
	 * compare editor on the result.
	 *
	 * @param input the input on which to open the compare editor
	 * @see ICompareEditorInput
	 */
	public void openCompareEditor(CompareEditorInput input) {
		
		if (compareResultOK(input)) {				
			IWorkbenchPage activePage= getActivePage();
			if (activePage != null) {
				try {
					activePage.openEditor(input, COMPARE_EDITOR);
				} catch (PartInitException e) {
					MessageDialog.openError(getShell(), PROBLEMS_OPENING_EDITOR, e.getMessage());
				}
			} else {
				MessageDialog.openError(getShell(), PROBLEMS_OPENING_EDITOR, "Can't find active workbench page");
			}
		}
	}

	/**
	 * Performs the comparison described by the given input and opens a
	 * compare dialog on the result.
	 *
	 * @param input the input on which to open the compare editor
	 * @see ICompareEditorInput
	 */
	public void openCompareDialog(final CompareEditorInput input) {
				
		if (compareResultOK(input)) {
			CompareDialog dialog= new CompareDialog(getShell(), input);
			dialog.open();
		}
	}
	
	/**
	 * @return <code>true</code> if compare result is OK to show, <code>false</code> otherwise
	 */
	private boolean compareResultOK(CompareEditorInput input) {
		final Shell shell= getShell();
		try {
			
			// run operation in separate thread and make it canceable
			new ProgressMonitorDialog(shell).run(true, true, input);
			
			String message= input.getMessage();
			if (message != null) {
				MessageDialog.openError(shell, COMPARE_FAILED, message);
				return false;
			}
			
			if (input.getCompareResult() == null) {
				MessageDialog.openInformation(shell, COMPARE_FAILED, "There are no differences between the selected inputs");
				return false;
			}
			
			return true;

		} catch (InterruptedException x) {
			// cancelled by user		
		} catch (InvocationTargetException x) {
			MessageDialog.openError(shell, COMPARE_FAILED, x.getTargetException().getMessage());
		}
		return false;
	}
		
	/**
	 * Registers an image for the given type.
	 */
	private static void registerImage(String type, Image image, boolean dispose) {
		fgImages.put(normalizeCase(type), image);
		if (image != null && dispose) {
			fgDisposeOnShutdownImages.add(image);
		}
	}
	
	/**
	 * Registers an image descriptor for the given type.
	 *
	 * @param type the type
	 * @param descriptor the image descriptor
	 */
	public static void registerImageDescriptor(String type, ImageDescriptor descriptor) {
		fgImageDescriptors.put(normalizeCase(type), descriptor);
	}
	
	public static ImageDescriptor getImageDescriptor(String relativePath) {
		
		URL installURL= null;
		if (fgComparePlugin != null)
			installURL= fgComparePlugin.getDescriptor().getInstallURL();
					
		if (installURL != null) {
			try {
				URL url= new URL(installURL, "icons/full/" + relativePath);
				return ImageDescriptor.createFromURL(url);
			} catch (MalformedURLException e) {
				Assert.isTrue(false);
			}
		}
		return null;
	}
	
	/**
	 * Returns a shared image for the given type, or a generic image if none
	 * has been registered for the given type.
	 * <p>
	 * Note: Images returned from this method will be automitically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param type the type
	 * @return the image
	 */
	public static Image getImage(String type) {
		
		type= normalizeCase(type);
		
		boolean dispose= false;
		Image image= null;
		if (type != null)
			image= (Image) fgImages.get(type);
		if (image == null) {
			ImageDescriptor id= (ImageDescriptor) fgImageDescriptors.get(type);
			if (id != null) {
				image= id.createImage();
				dispose= true;
			}
				
			if (image == null) {
				if (fgComparePlugin != null) {
					if (ITypedElement.FOLDER_TYPE.equals(type)) {
						image= getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
						//image= SharedImages.getImage(ISharedImages.IMG_OBJ_FOLDER);
					} else {
						image= createWorkbenchImage(type);
						dispose= true;
					}
				} else {
					id= (ImageDescriptor) fgImageDescriptors.get(normalizeCase("file"));
					image= id.createImage();
					dispose= true;
				}
			}
			if (image != null)
				registerImage(type, image, dispose);
		}
		return image;
	}
	
	/**
	 * Returns a shared image for the given adaptable.
	 * This convenience method queries the given adaptable
	 * for its <code>IWorkbenchAdapter.getImageDescriptor</code>, which it
	 * uses to create an image if it does not already have one.
	 * <p>
	 * Note: Images returned from this method will be automitically disposed
	 * of when this plug-in shuts down. Callers must not dispose of these
	 * images themselves.
	 * </p>
	 *
	 * @param adaptable the adaptable for which to find an image
	 * @return an image
	 */
	public static Image getImage(IAdaptable adaptable) {
		if (adaptable != null) {
			Object o= adaptable.getAdapter(IWorkbenchAdapter.class);
			if (o instanceof IWorkbenchAdapter) {
				ImageDescriptor id= ((IWorkbenchAdapter) o).getImageDescriptor(adaptable);
				if (id != null) {
					Image image= (Image)fgImages2.get(id);
					if (image == null) {
						image= id.createImage();
						try {
							fgImages2.put(id, image);
						} catch (NullPointerException ex) {
							System.out.println("NPE in CompareUIPlugin.getImage");
						}
						fgDisposeOnShutdownImages.add(image);

					}
					return image;
				}
			}
		}
		return null;
	}
	
	private static Image createWorkbenchImage(String type) {
		IEditorRegistry er= getDefault().getWorkbench().getEditorRegistry();
		ImageDescriptor id= er.getImageDescriptor("foo." + type);
		return id.createImage();
	}
	
	/**
	 * Registers the given structure creator descriptor for one or more types.
	 *
	 * @param types one or more types separated by commas and whitespace
	 * @param descriptor the descriptor to register
	 */
	public static void registerStructureCreator(String types, IStructureCreatorDescriptor descriptor) {
		if (types != null) {
			StringTokenizer tokenizer= new StringTokenizer(types, ",");
			while (tokenizer.hasMoreElements()) {
				String extension= tokenizer.nextToken().trim();
				fgStructureCreators.put(normalizeCase(extension), descriptor);
			}
		}
	}
	
	/**
	 * Returns an structure creator descriptor for the given type.
	 *
	 * @param type the type for which to find a descriptor
	 * @return a descriptor for the given type, or <code>null</code> if no
	 *   descriptor has been registered
	 */
	public static IStructureCreatorDescriptor getStructureCreator(String type) {
		return (IStructureCreatorDescriptor) fgStructureCreators.get(normalizeCase(type));
	}
	
	/**
	 * Registers the given structure viewer descriptor for one or more types.
	 *
	 * @param types one or more types separated by commas and whitespace
	 * @param the descriptor to register
	 */
	public static void registerStructureViewerDescriptor(String types, IViewerDescriptor descriptor) {
		StringTokenizer tokenizer= new StringTokenizer(types, ",");
		while (tokenizer.hasMoreElements()) {
			String extension= tokenizer.nextToken().trim();
			fgStructureViewerDescriptors.put(normalizeCase(extension), descriptor);
		}
	}
		
	/**
	 * Registers the given content merge viewer descriptor for one or more types.
	 *
	 * @param types one or more types separated by commas and whitespace
	 * @param descriptor the descriptor to register
	 */
	public static void registerContentMergeViewerDescriptor(String types, IViewerDescriptor descriptor) {
		StringTokenizer tokenizer= new StringTokenizer(types, ",");
		while (tokenizer.hasMoreElements()) {
			String extension= tokenizer.nextToken().trim();
			fgContentMergeViewerDescriptors.put(normalizeCase(extension), descriptor);
		}
	}
	
	/**
	 * Registers the given content viewer descriptor for one or more types.
	 *
	 * @param types one or more types separated by commas and whitespace
	 * @param descriptor the descriptor to register
	 */
	public static void registerContentViewerDescriptor(String types, IViewerDescriptor descriptor) {
		StringTokenizer tokenizer= new StringTokenizer(types, ",");
		while (tokenizer.hasMoreElements()) {
			String extension= tokenizer.nextToken().trim();
			fgContentViewerDescriptors.put(normalizeCase(extension), descriptor);
		}
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

		if (input.getLeft() == null || input.getRight() == null)	// we don't show the structure of additions or deletions
			return null;
			
		String type= getType(input);
		if (type == null)
			return null;
			
		type= normalizeCase(type);
			
		IViewerDescriptor vd= (IViewerDescriptor) fgStructureViewerDescriptors.get(type);
		if (vd != null)
			return vd.createViewer(oldViewer, parent, configuration);
			
		IStructureCreatorDescriptor scc= getStructureCreator(type);
		if (scc != null) {
			IStructureCreator sc= scc.createStructureCreator();
			if (sc != null) {
				StructureDiffViewer sdv= new StructureDiffViewer(parent, configuration);
				sdv.setStructureCreator(sc);
				return sdv;
			}
		}
		return null;
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
	public static Viewer findContentViewer(Viewer oldViewer, Object in, Composite parent, CompareConfiguration cc) {
		
		if (! (in instanceof ICompareInput)) {
			String type= ITypedElement.TEXT_TYPE;
			if (in instanceof ITypedElement) {
				ITypedElement tin= (ITypedElement) in;
				type= tin.getType();
			} 
			type= normalizeCase(type);
			
			IViewerDescriptor vd= (IViewerDescriptor) fgContentViewerDescriptors.get(type);
			Viewer viewer= null;
			if (vd != null) {
				viewer= vd.createViewer(oldViewer, parent, cc);
				if (viewer != null)
					return viewer;
			}
			// fallback
			return new TextViewer(parent);
		}

		ICompareInput input= (ICompareInput) in;
		String type= getType(input);
		type= normalizeCase(type);
		
		if (ITypedElement.FOLDER_TYPE.equals(type))
			return null;
			
		if (type != null) {
			IViewerDescriptor vd= (IViewerDescriptor) fgContentMergeViewerDescriptors.get(type);
			Viewer viewer= null;
			if (vd != null) {
				viewer= vd.createViewer(oldViewer, parent, cc);
				if (viewer != null)
					return viewer;
			}
		}
		
		// fallback
		String leftType= guessType(input.getLeft());
		String rightType= guessType(input.getRight());
			
		if (leftType != null && rightType != null) {
			if (ITypedElement.TEXT_TYPE.equals(leftType) && ITypedElement.TEXT_TYPE.equals(rightType))
				type= ITypedElement.TEXT_TYPE;
			else
				type= "binary";
			
			IViewerDescriptor vd= (IViewerDescriptor) fgContentMergeViewerDescriptors.get(normalizeCase(type));
			if (vd != null)
				return vd.createViewer(oldViewer, parent, cc);
		}
		return null;
	}
	
	/**
	 * Determines the type of the given threeway input by analyzing
	 * the types (file extension) of the individual parts.
	 * Returns null if no type can be determined.
	 */
	private static String getType(ICompareInput input) {
		ITypedElement ancestor= input.getAncestor();
		ITypedElement left= input.getLeft();
		ITypedElement right= input.getRight();
		
		String[] types= new String[3];
		int cnt= 0;
		
		if (ancestor != null) {
			String type= ancestor.getType();
			if (type != null)
				types[cnt++]= type;
		}
		if (left != null) {
			String type= left.getType();
			if (type != null)
				types[cnt++]= type;
		}
		if (right != null) {
			String type= right.getType();
			if (type != null)
				types[cnt++]= type;
		}
		boolean homogenous= false;
		switch (cnt) {
		case 1:
			homogenous= true;
			break;
		case 2:
			homogenous= types[0].equals(types[1]);
			break;
		case 3:
			homogenous= types[0].equals(types[1]) && types[1].equals(types[2]);
			break;
		}
		if (homogenous)
			return types[0];
		return null;
	}
	
	/**
	 * Guesses the file type of the given input.
	 * Returns ITypedElement.TEXT_TYPE if the first 1000 bytes contain only values in the range 0-127.
	 * Returns ITypedElement.UNKNOWN_TYPE if a single byte is out of this range.
	 * Returns <code>null</code> if the input isn't an <code>IStreamContentAccessor</code>.
	 */
	private static String guessType(ITypedElement input) {
		if (input instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca= (IStreamContentAccessor) input;
			try {
				InputStream is= sca.getContents();
				if (is == null)
					return null;
				for (int i= 0; i < 1000; i++)
					if (is.read() >= 128)
						return ITypedElement.UNKNOWN_TYPE;
				return ITypedElement.TEXT_TYPE;
			} catch (CoreException ex) {
				// be silent and return UNKNOWN_TYPE
			} catch (IOException ex) {
				// be silent and return UNKNOWN_TYPE
			}
			return ITypedElement.UNKNOWN_TYPE;
		}
		return null;
	}
	
	private static IViewerDescriptor getContentViewerDescriptor2(String type) {
		return (IViewerDescriptor) fgContentMergeViewerDescriptors.get(normalizeCase(type));
	}
	
	private static String normalizeCase(String s) {
		if (NORMALIZE_CASE && s != null)
			return s.toUpperCase();
		return s;
	}
}

