/*
 * Created on Jun 9, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.parts;

import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author Wassim Melhem
 */
public class MyComputerLabelProvider extends LabelProvider {
	
	private VolumeLabelProvider volumeLabelProvider;

	public MyComputerLabelProvider() {
		super();
		UpdateUI.getDefault().getLabelProvider().connect(this);
		volumeLabelProvider = new VolumeLabelProvider();
	}
	
	public Image getImage(Object obj) {
		UpdateLabelProvider provider = UpdateUI.getDefault().getLabelProvider();
		if (obj instanceof MyComputer) {
			return provider.get(UpdateUIImages.DESC_COMPUTER_OBJ);
		}
		if (obj instanceof MyComputerDirectory) {
			IVolume volume = ((MyComputerDirectory) obj).getVolume();
			if (volume != null) {
				Image image = volumeLabelProvider.getImage(volume);
				if (image != null)
					return image;
			}
			return ((MyComputerDirectory) obj).getImage(obj);
		}
		
		if (obj instanceof MyComputerFile) {
			ImageDescriptor desc =
				((MyComputerFile) obj).getImageDescriptor(obj);
			return provider.get(desc);
		}
		
		if (obj instanceof ExtensionRoot) {
			return provider.get(UpdateUIImages.DESC_ESITE_OBJ);
		}
		
		if (obj instanceof SiteBookmark) {
			return provider.get(UpdateUIImages.DESC_SITE_OBJ);
		}
		
		return super.getImage(obj);
	}
	
	public String getText(Object obj) {
		if (obj instanceof MyComputerDirectory) {
			MyComputerDirectory dir = (MyComputerDirectory) obj;
			IVolume volume = dir.getVolume();
			if (volume != null)
				return volumeLabelProvider.getText(volume);
			else
				return dir.getLabel(dir);
		}
		return super.getText(obj);
	}
	
	public void dispose() {
		super.dispose();
		volumeLabelProvider.dispose();
		UpdateUI.getDefault().getLabelProvider().disconnect(this);
	}

}
