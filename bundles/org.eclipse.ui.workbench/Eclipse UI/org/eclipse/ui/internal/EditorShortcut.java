package org.eclipse.ui.internal;

import java.text.Collator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.part.EditorPart;

public class EditorShortcut implements Comparable {
	
	private IMemento memento;
	private String title;
	private String tooltip;
	private String factoryId;
	private Image image;
	private ImageDescriptor imageDescriptor;
	private String path;
	
	private String id;
	private IEditorInput input;
	
	private Collator collator = Collator.getInstance();
	
	public static EditorShortcut create(IEditorReference editorRef) {
		WorkbenchPartReference ref = (WorkbenchPartReference)editorRef;
		if(ref.getPart(false) != null) {
			if(ref.getPart(false) instanceof EditorPart) {
				EditorPart part = (EditorPart)ref.getPart(false);
				IEditorInput input = part.getEditorInput();
				if(input.getPersistable() != null)
					return new EditorShortcut(ref,part);
			}
		} else if(ref.getMemento() != null) {
			return new EditorShortcut(ref);
		}
		return null;
	}
	public static EditorShortcut create(IMemento memento) {
		EditorShortcut e =  new EditorShortcut(memento);
		e.initImage();
		return e;
	}
	private EditorShortcut(IMemento mem) {
		id = mem.getString(IWorkbenchConstants.TAG_ID);
		title = mem.getString(IWorkbenchConstants.TAG_TITLE);
		tooltip = mem.getString(IWorkbenchConstants.TAG_TOOLTIP);
		memento = mem.getChild(IWorkbenchConstants.TAG_INPUT);
		factoryId = memento.getString(IWorkbenchConstants.TAG_FACTORY_ID);
		path = mem.getString(IWorkbenchConstants.TAG_PATH);	
		
		IEditorRegistry reg = WorkbenchPlugin.getDefault().getEditorRegistry();
		EditorDescriptor desc = (EditorDescriptor) reg.findEditor(id);
		
		IFile file = null;
		if(path != null) {
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
			if (res instanceof IFile)
				file = (IFile)res;
		}
		imageDescriptor = EditorManager.findImage(desc,file);	
	}
	
	private EditorShortcut(WorkbenchPartReference ref,EditorPart part) {
		input = part.getEditorInput(); 
		id = ((EditorSite)part.getEditorSite()).getEditorDescriptor().getId();
		title = part.getTitle();
		tooltip = part.getTitleToolTip();
		imageDescriptor = ref.getImageDescriptor();
		initImage();
	}
	
	private EditorShortcut(WorkbenchPartReference ref) {
		this(ref.getMemento());		
		title = ref.getTitle();
		tooltip = ref.getTitleToolTip();
		imageDescriptor = ref.getImageDescriptor();
		initImage();
		id = ref.getId();
	}
	
	private void initImage() {
		ReferenceCounter imageCache = WorkbenchImages.getImageCache();
		image = (Image)imageCache.get(imageDescriptor);
		if(image != null) {
			imageCache.addRef(imageDescriptor);
		}
		image = imageDescriptor.createImage();
		imageCache.put(imageDescriptor,image);
	}
	/**
	 * @see IWorkbenchPart#getTitle
	 */	
	public String getTitle(){
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
		getManager().fireShortcutRename(this);				
	}
	/**
	 * @see IWorkbenchPart#getTitleImage
	 */	
	public Image getTitleImage(){
		return image;
	}
	public IEditorInput getInput() {
		if(input != null)
			return input;
		if(memento == null)
			return null;
		IStatus status = restoreState();
		if(status.isOK())
			return input;
		//Todo: open a dialog to inform the user that there was an error;
		return null;
	}
	public String getId() {
		return id;
	}
	/**
	 * @see IWorkbenchPart#getTitleToolTip
	 */		
	public String getTitleToolTip(){
		return tooltip;
	}
	public void dispose() {
		if(image != null) {
			ReferenceCounter imageCache = WorkbenchImages.getImageCache();
			Image image = (Image)imageCache.get(imageDescriptor);
			if(image != null) {
				imageCache.removeRef(imageDescriptor);
			}
			image = null;			
		}
	}
	
	private IStatus restoreState() {
		final IStatus result[] = new IStatus[1];
		Platform.run(new SafeRunnable() {
			public void run() {
				// Get the input factory.
				if (factoryId == null) {
					WorkbenchPlugin.log("Unable to restore editor - no input factory ID."); //$NON-NLS-1$
					result[0] = unableToCreateEditor(null);
					return;
				}
				IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryId);
				if (factory == null) {
					WorkbenchPlugin.log("Unable to restore editor - cannot instantiate input element factory: " + factoryId); //$NON-NLS-1$
					result[0] = unableToCreateEditor(null);
					return;
				}

				// Get the input element.
				IAdaptable input = factory.createElement(memento);
				if (input == null) {
					WorkbenchPlugin.log("Unable to restore editor - createElement returned null for input element factory: " + factoryId); //$NON-NLS-1$
					result[0] = unableToCreateEditor(null);
					return;
				}
				if (!(input instanceof IEditorInput)) {
					WorkbenchPlugin.log("Unable to restore editor - createElement result is not an IEditorInput for input element factory: " + factoryId); //$NON-NLS-1$
					result[0] = unableToCreateEditor(null);
					return;
				}
				EditorShortcut.this.input = (IEditorInput) input;
			}
			public void handleException(Throwable e) {
				result[0] = unableToCreateEditor(e);
			}
		});
		if(result[0] != null)
			return result[0];
		else
			return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null);
	}
	
	/**
	 *  Returns an error status to be displayed when unable to create an editor.
	 */
	private IStatus unableToCreateEditor(Throwable t) {
		return new Status(
			IStatus.ERROR,PlatformUI.PLUGIN_ID,0,
			WorkbenchMessages.format("EditorManager.unableToCreateEditor",new String[]{title}),t);
	}
	
	public void saveState(IMemento mem) {
		mem.putString(IWorkbenchConstants.TAG_ID,id);
		mem.putString(IWorkbenchConstants.TAG_TITLE,title);
		mem.putString(IWorkbenchConstants.TAG_TOOLTIP,tooltip);
		mem.putString(IWorkbenchConstants.TAG_FACTORY_ID,factoryId);
		
		if(memento != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_INPUT);
			childMem.putMemento(memento);
		} else if(input != null) {
			IMemento childMem = mem.createChild(IWorkbenchConstants.TAG_INPUT);
			IPersistableElement persistable = input.getPersistable();
			childMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, persistable.getFactoryId());
			persistable.saveState(childMem);
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput)input).getFile();
				mem.putString(IWorkbenchConstants.TAG_PATH,file.getFullPath().toString());
			}			
		}
		
	}
	public boolean equals(Object o) {
		if(!(o instanceof EditorShortcut))
			return false;
		EditorShortcut other = (EditorShortcut)o;
		return title.equals(other.title);
	}
	public int compareTo(Object o) {
		EditorShortcut other = (EditorShortcut)o;
		return collator.compare(this.getTitle(),other.getTitle());
	}
	private EditorShortcutManager getManager() {
		return ((Workbench)PlatformUI.getWorkbench()).getEditorShortcutManager();
	}
}
