/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.preference.IPreferenceStore;

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
	
	public static final String DTOOL_NEXT= "dlcl16/next_nav.gif";	//$NON-NLS-1$
	public static final String CTOOL_NEXT= "clcl16/next_nav.gif";	//$NON-NLS-1$
	public static final String ETOOL_NEXT= "elcl16/next_nav.gif";	//$NON-NLS-1$
	
	public static final String DTOOL_PREV= "dlcl16/prev_nav.gif";	//$NON-NLS-1$
	public static final String CTOOL_PREV= "clcl16/prev_nav.gif";	//$NON-NLS-1$
	public static final String ETOOL_PREV= "elcl16/prev_nav.gif";	//$NON-NLS-1$
				
	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR= 1;

	private static boolean NORMALIZE_CASE= true;

	private final static String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$
	private final static String EXTENSIONS_ATTRIBUTE= "extensions"; //$NON-NLS-1$

	public static final String PLUGIN_ID= "org.eclipse.compare"; //$NON-NLS-1$

	private static final String STRUCTURE_CREATOR_EXTENSION_POINT= "structureCreators"; //$NON-NLS-1$
	private static final String STRUCTURE_MERGEVIEWER_EXTENSION_POINT= "structureMergeViewers"; //$NON-NLS-1$
	private static final String CONTENT_MERGEVIEWER_EXTENSION_POINT= "contentMergeViewers"; //$NON-NLS-1$
	private static final String CONTENT_VIEWER_EXTENSION_POINT= "contentViewers"; //$NON-NLS-1$
	
	private static final String COMPARE_EDITOR= PLUGIN_ID + ".CompareEditor"; //$NON-NLS-1$
	
	private static final String STRUCTUREVIEWER_ALIASES_PREFERENCE_NAME= "StructureViewerAliases";	//$NON-NLS-1$
	
	/** Maps type to icons */
	private static Map fgImages= new Hashtable(10);
	/** Maps type to ImageDescriptors */
	private static Map fgImageDescriptors= new Hashtable(10);
	/** Maps ImageDescriptors to Images */
	private static Map fgImages2= new Hashtable(10);
	
	private static Map fgStructureCreators= new Hashtable(10);
	private static Map fgStructureViewerDescriptors= new Hashtable(10);
	private static Map fgStructureViewerAliases= new Hashtable(10);
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
		
		fgResourceBundle= descriptor.getResourceBundle();
		
		registerExtensions();
		
		initPreferenceStore();
	}
	
	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		super.initializeDefaultPreferences(store);
		
		ComparePreferencePage.initDefaults(store);		
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
		return fgResourceBundle;
	}
	
	public static IWorkbench getActiveWorkbench() {
		CompareUIPlugin plugin= getDefault();
		if (plugin == null)
			return null;
		return plugin.getWorkbench();
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbench workbench= getActiveWorkbench();
		if (workbench == null)
			return null;	
		return workbench.getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the active workkbench page or <code>null</code> if
	 * no active workkbench page can be determined.
	 *
	 * @return the active workkbench page or <code>null</code> if
	 * 	no active workkbench page can be determined
	 */
	private static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
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
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getShell();
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
			
		/*
		 * Converts the aliases into a single string before they are stored
		 * in the preference store.
		 * The format is:
		 * <key> '.' <alias> ' ' <key> '.' <alias> ...
		 */
		IPreferenceStore ps= getPreferenceStore();
		if (ps != null) {
			StringBuffer sb= new StringBuffer();
			Iterator iter= fgStructureViewerAliases.keySet().iterator();
			while (iter.hasNext()) {
				String key= (String) iter.next();
				String alias= (String) fgStructureViewerAliases.get(key);
				sb.append(key);
				sb.append('.');
				sb.append(alias);
				sb.append(' ');
			}
			ps.setValue(STRUCTUREVIEWER_ALIASES_PREFERENCE_NAME, sb.toString());
		}
		
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
	 * @see CompareEditorInput
	 */
	public void openCompareEditor(CompareEditorInput input) {
		
		if (compareResultOK(input)) {				
			IWorkbenchPage activePage= getActivePage();
			if (activePage != null) {
				try {
					activePage.openEditor(input, COMPARE_EDITOR);
				} catch (PartInitException e) {
					MessageDialog.openError(getShell(), Utilities.getString("CompareUIPlugin.openEditorError"), e.getMessage()); //$NON-NLS-1$
				}
			} else {
				MessageDialog.openError(getShell(),
						Utilities.getString("CompareUIPlugin.openEditorError"), //$NON-NLS-1$
						Utilities.getString("CompareUIPlugin.noActiveWorkbenchPage")); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Performs the comparison described by the given input and opens a
	 * compare dialog on the result.
	 *
	 * @param input the input on which to open the compare editor
	 * @see CompareEditorInput
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
				MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), message); //$NON-NLS-1$
				return false;
			}
			
			if (input.getCompareResult() == null) {
				MessageDialog.openInformation(shell, Utilities.getString("CompareUIPlugin.dialogTitle"), Utilities.getString("CompareUIPlugin.noDifferences")); //$NON-NLS-2$ //$NON-NLS-1$
				return false;
			}
			
			return true;

		} catch (InterruptedException x) {
			// cancelled by user		
		} catch (InvocationTargetException x) {
			MessageDialog.openError(shell, Utilities.getString("CompareUIPlugin.compareFailed"), x.getTargetException().getMessage()); //$NON-NLS-1$
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
				URL url= new URL(installURL, Utilities.getIconPath(null) + relativePath);
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
					id= (ImageDescriptor) fgImageDescriptors.get(normalizeCase("file")); //$NON-NLS-1$
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
		ImageDescriptor id= er.getImageDescriptor("foo." + type); //$NON-NLS-1$
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
			StringTokenizer tokenizer= new StringTokenizer(types, ","); //$NON-NLS-1$
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
		StringTokenizer tokenizer= new StringTokenizer(types, ","); //$NON-NLS-1$
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
		StringTokenizer tokenizer= new StringTokenizer(types, ","); //$NON-NLS-1$
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
		StringTokenizer tokenizer= new StringTokenizer(types, ","); //$NON-NLS-1$
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
		if (vd == null) {
			String alias= (String) fgStructureViewerAliases.get(type);
			if (alias != null)
				vd= (IViewerDescriptor) fgStructureViewerDescriptors.get(alias);
		}
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
		
		if (in instanceof IStreamContentAccessor) {
			String type= ITypedElement.TEXT_TYPE;
			
			if (in instanceof ITypedElement) {
				ITypedElement tin= (ITypedElement) in;
				String ty= tin.getType();
				if (ty != null)
					type= ty;
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
			return new SimpleTextViewer(parent);
		}

		if (!(in instanceof ICompareInput))
			return null;
			
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
			
		if (leftType != null || rightType != null) {
			boolean right_text= rightType != null && ITypedElement.TEXT_TYPE.equals(rightType);
			boolean left_text= leftType != null && ITypedElement.TEXT_TYPE.equals(leftType);
			if ((leftType == null && right_text) || (left_text && rightType == null) || (left_text && right_text))
				type= ITypedElement.TEXT_TYPE;
			else
				type= "binary"; //$NON-NLS-1$
			
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
	 * Returns ITypedElement.TEXT_TYPE if none of the first 10 lines is longer than 1000 bytes.
	 * Returns ITypedElement.UNKNOWN_TYPE otherwise.
	 * Returns <code>null</code> if the input isn't an <code>IStreamContentAccessor</code>.
	 */
	private static String guessType(ITypedElement input) {
		if (input instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca= (IStreamContentAccessor) input;
			InputStream is= null;
			try {
				is= sca.getContents();
				if (is == null)
					return null;
				int lineLength= 0;
				int lines= 0;
				while (lines < 10) {
					int c= is.read();
					if (c == -1)	// EOF
						break;
					if (c == '\n' || c == '\r') { // reset line length
						lineLength= 0;
						lines++;
					} else
						lineLength++;
					if (lineLength > 1000)
						return ITypedElement.UNKNOWN_TYPE;
				}
				return ITypedElement.TEXT_TYPE;
			} catch (CoreException ex) {
				// be silent and return UNKNOWN_TYPE
			} catch (IOException ex) {
				// be silent and return UNKNOWN_TYPE
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
					}
				}
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
	
	//---- alias mgmt
	
	private void initPreferenceStore() {
		//System.out.println("initPreferenceStore");
		IPreferenceStore ps= getPreferenceStore();
		if (ps != null) {
			String aliases= ps.getString(STRUCTUREVIEWER_ALIASES_PREFERENCE_NAME);
			//System.out.println("  <" + aliases + ">");
			if (aliases != null && aliases.length() > 0) {
				StringTokenizer st= new StringTokenizer(aliases, " ");	//$NON-NLS-1$
				while (st.hasMoreTokens()) {
					String pair= st.nextToken();
					int pos= pair.indexOf('.');
					if (pos > 0) {
						String key= pair.substring(0, pos);
						String alias= pair.substring(pos+1);
						fgStructureViewerAliases.put(key, alias);
						//System.out.println("<" + key + "><" + alias + ">");
					}
				}
			}
		}		
	}
	
	public static void addStructureViewerAlias(String type, String alias) {
		//System.out.println("addStructureViewerAlias: " + type + " " + alias);
		fgStructureViewerAliases.put(normalizeCase(alias), normalizeCase(type));
	}
	
	public static void removeAllStructureViewerAliases(String type) {
		String t= normalizeCase(type);
		Set entrySet= fgStructureViewerAliases.entrySet();
		for (Iterator iter= entrySet.iterator(); iter.hasNext(); ) {
			Map.Entry entry= (Map.Entry)iter.next();
			if (entry.getValue().equals(t))
				iter.remove();
		}
	}
	
	/**
	 * Returns an array of all editors that have an unsaved content. If the identical content is 
	 * presented in more than one editor, only one of those editor parts is part of the result.
	 * 
	 * @return an array of all dirty editor parts.
	 */
	public static IEditorPart[] getDirtyEditors() {
		Set inputs= new HashSet();
		List result= new ArrayList(0);
		IWorkbench workbench= getDefault().getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int x= 0; x < pages.length; x++) {
				IEditorPart[] editors= pages[x].getDirtyEditors();
				for (int z= 0; z < editors.length; z++) {
					IEditorPart ep= editors[z];
					IEditorInput input= ep.getEditorInput();
					if (!inputs.contains(input)) {
						inputs.add(input);
						result.add(ep);
					}
				}
			}
		}
		return (IEditorPart[])result.toArray(new IEditorPart[result.size()]);
	}
		
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), INTERNAL_ERROR, CompareMessages.getString("ComparePlugin.internal_error"), e)); //$NON-NLS-1$
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}
}
