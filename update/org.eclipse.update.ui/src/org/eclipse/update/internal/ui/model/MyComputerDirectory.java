package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.update.configuration.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class MyComputerDirectory
	extends UIModelObject
	implements IWorkbenchAdapter {
	private static final String KEY_VOLUME_CDROM = "MyComputerDirectory.cdrom";
	private static final String KEY_VOLUME_FLOPPY_3 =
		"MyComputerDirectory.floppy3";
	private static final String KEY_VOLUME_FLOPPY_5 =
		"MyComputerDirectory.floppy5";
	private UIModelObject parent;
	private File file;
	private IVolume volume;
	Object[] children;

	public MyComputerDirectory(
		UIModelObject parent,
		File file,
		IVolume volume) {
		this.parent = parent;
		this.file = file;
		this.volume = volume;
	}

	public MyComputerDirectory(UIModelObject parent, File file) {
		this(parent, file, null);
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IWorkbenchAdapter.class)) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	public IVolume getVolume() {
		return volume;
	}
	
	private String getVolumeName() {
		String name = file.getPath();
		if (name.endsWith(File.separator) && name.length()>1) {
			name = name.substring(0, name.length()-1);
		}
		return name;
	}

	public String getName() {
		String fileName = volume != null ? getVolumeName() : file.getName();
		if (volume != null) {
			String nativeLabel = volume.getLabel();
			if (nativeLabel == null || nativeLabel.length() == 0) {
				// set well-known names for types
				int type = volume.getType();
				switch (type) {
					case LocalSystemInfo.VOLUME_CDROM :
						nativeLabel =
							UpdateUIPlugin.getResourceString(KEY_VOLUME_CDROM);
						break;
					case LocalSystemInfo.VOLUME_FLOPPY_3 :
						nativeLabel =
							UpdateUIPlugin.getResourceString(
								KEY_VOLUME_FLOPPY_3);
						break;
					case LocalSystemInfo.VOLUME_FLOPPY_5 :
						nativeLabel =
							UpdateUIPlugin.getResourceString(
								KEY_VOLUME_FLOPPY_5);
						break;
				}
			}
			if (nativeLabel != null && nativeLabel.length()>0) {
				return nativeLabel + " (" + fileName + ")";
			} else {
				return fileName;
			}
		}
		return fileName;
	}

	public File getFile() {
		return file;
	}

	public String toString() {
		return getName();
	}

	public boolean hasChildren(Object parent) {
		if (file.isDirectory()) {
			final boolean[] result = new boolean[1];
			BusyIndicator
				.showWhile(
					UpdateUIPlugin.getActiveWorkbenchShell().getDisplay(),
					new Runnable() {
				public void run() {
					File[] children = file.listFiles();
					result[0] = children != null && children.length > 0;
				}
			});
			return result[0];
		}
		return false;
	}

	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */

	public Object[] getChildren(Object parent) {
		BusyIndicator
			.showWhile(
				UpdateUIPlugin.getActiveWorkbenchShell().getDisplay(),
				new Runnable() {
			public void run() {
				File[] files = file.listFiles();
				if (files==null) {
					children = new Object[0];
					return;
				}

				children = new Object[files.length];
				for (int i = 0; i < files.length; i++) {
					File file = files[i];

					if (file.isDirectory()) {
						SiteBookmark site = createSite(file);
						if (site != null)
							children[i] = site;
						else
							children[i] =
								new MyComputerDirectory(
									MyComputerDirectory.this,
									file);
					} else {
						children[i] =
							new MyComputerFile(MyComputerDirectory.this, file);
					}
				}
			}
		});
		return children;
	}

	static SiteBookmark createSite(File file) {
		try {
			File siteXML = new File(file, "site.xml");
			if (siteXML.exists() == false)
				return null;
			URL url =
				new URL("file:" + file.getAbsolutePath() + File.separator);
			SiteBookmark site = new SiteBookmark(file.getName(), url);
			site.setType(SiteBookmark.LOCAL);
			return site;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object obj) {
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
			ISharedImages.IMG_OBJ_FOLDER);
	}

	public Image getImage(Object obj) {
		return PlatformUI.getWorkbench().getSharedImages().getImage(
			ISharedImages.IMG_OBJ_FOLDER);
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object obj) {
		return getName();
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object arg0) {
		return parent;
	}
}