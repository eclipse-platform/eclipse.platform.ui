/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.parts;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.*;

public class VolumeLabelProvider extends LabelProvider {
	private static final String KEY_VOLUME_CDROM = "VolumeLabelProvider.cdrom";
	private static final String KEY_VOLUME_FLOPPY_3 =
		"VolumeLabelProvider.floppy3";
	private static final String KEY_VOLUME_FLOPPY_5 =
		"VolumeLabelProvider.floppy5";
	private static final String KEY_LOCAL_DISK =
		"VolumeLabelProvider.localDisk";
	private Image cdImage;
	private Image vfixedImage;
	private Image floppyImage;
	private Image vremovableImage;
	private Image vremoteImage;
	/**
	 * Constructor for VolumeLabelProvider.
	 */
	public VolumeLabelProvider() {
		floppyImage = UpdateUIImages.DESC_FLOPPY_OBJ.createImage();
		cdImage = UpdateUIImages.DESC_CD_OBJ.createImage();
		vfixedImage = UpdateUIImages.DESC_VFIXED_OBJ.createImage();
		vremoteImage = UpdateUIImages.DESC_VREMOTE_OBJ.createImage();
		vremovableImage =
			UpdateUIImages.DESC_VREMOVABLE_OBJ.createImage();
	}

	public String getText(Object obj) {
		if (obj instanceof IVolume) {
			return getVolumeText((IVolume) obj);
		}
		return obj.toString();
	}

	private String getVolumeText(IVolume volume) {
		String fileName = getVolumeName(volume);
		String nativeLabel = volume.getLabel();
		if (nativeLabel == null || nativeLabel.length() == 0) {
			// set well-known names for types
			int type = volume.getType();
			switch (type) {
				case LocalSystemInfo.VOLUME_CDROM :
					nativeLabel =
						UpdateUI.getString(KEY_VOLUME_CDROM);
					break;
				case LocalSystemInfo.VOLUME_FLOPPY_3 :
					nativeLabel =
						UpdateUI.getString(KEY_VOLUME_FLOPPY_3);
					break;
				case LocalSystemInfo.VOLUME_FLOPPY_5 :
					nativeLabel =
						UpdateUI.getString(KEY_VOLUME_FLOPPY_5);
					break;
				case LocalSystemInfo.VOLUME_FIXED:
					nativeLabel = 
						UpdateUI.getString(KEY_LOCAL_DISK);
					break;
			}
		}
		if (nativeLabel != null && nativeLabel.length() > 0) {
			return nativeLabel + " (" + fileName + ")";
		} else {
			return fileName;
		}
	}
	private String getVolumeName(IVolume volume) {
		String name = volume.getFile().getPath();
		if (name.endsWith(File.separator) && name.length()>1) {
			name = name.substring(0, name.length()-1);
		}
		return name;
	}

	public Image getImage(Object obj) {
		if (obj instanceof IVolume) {
			return getVolumeImage((IVolume) obj);
		}
		return vfixedImage;
	}
	private Image getVolumeImage(IVolume volume) {
		switch (volume.getType()) {
			case LocalSystemInfo.VOLUME_CDROM :
				return cdImage;
			case LocalSystemInfo.VOLUME_FIXED :
				return vfixedImage;
			case LocalSystemInfo.VOLUME_FLOPPY_3 :
			case LocalSystemInfo.VOLUME_FLOPPY_5 :
				return floppyImage;
			case LocalSystemInfo.VOLUME_RAMDISK :
			case LocalSystemInfo.VOLUME_REMOVABLE :
				return vremovableImage;
			case LocalSystemInfo.VOLUME_REMOTE :
				return vremoteImage;
		}
		return null;
	}
}