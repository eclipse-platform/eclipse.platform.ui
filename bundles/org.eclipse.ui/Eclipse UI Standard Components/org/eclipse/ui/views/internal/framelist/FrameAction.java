package org.eclipse.ui.views.internal.framelist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Abstract superclass for actions dealing with frames or a frame list.
 * This listens for changes to the frame list and updates itself
 * accordingly.
 * 
 * @deprecated This has been promoted to API and will be removed for 2.0.  
 *   Use the corresponding class in package org.eclipse.ui.views.framelist instead.
 */
public abstract class FrameAction extends Action {
	private FrameList frameList;

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			FrameAction.this.handlePropertyChange(event);
		}
	};
	
/**
 * Creates a new frame action on the given frame list,
 * and hooks a property change listener on it.
 */
protected FrameAction(FrameList frameList) {
	this.frameList = frameList;
	frameList.addPropertyChangeListener(propertyChangeListener);
}
/**
 * Disposes this frame action.
 * The default implementation unhooks the property change listener from the frame list.
 */
public void dispose() {
	frameList.removePropertyChangeListener(propertyChangeListener);
}
/**
 * Returns the frame list.
 */
public FrameList getFrameList() {
	return frameList;
}
/**
 * Returns the image descriptor with the given relative path.
 */
static ImageDescriptor getImageDescriptor(String relativePath) {
	String iconPath = "icons/full/";//$NON-NLS-1$
	try {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		URL installURL = plugin.getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + relativePath);
		return ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
		// should not happen
		return ImageDescriptor.getMissingImageDescriptor();
	}
}
/**
 * Handles a property change event from the frame list.
 * The default implementation calls <code>update</code>.
 */
protected void handlePropertyChange(PropertyChangeEvent event) {
	update();
}
/**
 * Updates this action.  The default implementation does nothing.
 */
public void update() {
}
}
