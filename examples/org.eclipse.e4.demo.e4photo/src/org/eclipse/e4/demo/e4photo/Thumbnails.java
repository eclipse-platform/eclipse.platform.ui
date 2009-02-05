package org.eclipse.e4.demo.e4photo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.services.IBackgroundRunner;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;

public class Thumbnails {

	private Gallery gallery;
	private GalleryItem group;
	private final IEclipseContext outputContext;
	private final IBackgroundRunner backgroundRunner;
	private IContainer input;
	private volatile Runnable runnable;

	public Thumbnails(Composite parent, final IEclipseContext outputContext,
			IBackgroundRunner backgroundRunner) {
		this.outputContext = outputContext;
		this.backgroundRunner = backgroundRunner;
		parent.setLayout(new FillLayout());
		gallery = new Gallery(parent, SWT.V_SCROLL | SWT.MULTI);
		gallery.setData("org.eclipse.e4.ui.css.id", "thumbnails");

		Image itemImage = new Image(parent.getDisplay(), Program.findProgram("jpg")
				.getImageData());

		gallery.setGroupRenderer(new NoGroupRenderer());

		DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();
		ir.setShowLabels(false);
		ir.setDropShadowsSize(0);

		gallery.setItemRenderer(ir);

		group = new GalleryItem(gallery, SWT.NONE);
		group.setExpanded(true);

		gallery.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Object data = e.item.getData();
				outputContext.set(IServiceConstants.SELECTION, data);
			}
		});

	}

	private Point getBestSize(int originalX, int originalY, int maxX, int maxY) {
		double widthRatio = (double) originalX / (double) maxX;
		double heightRatio = (double) originalY / (double) maxY;

		double bestRatio = widthRatio > heightRatio ? widthRatio : heightRatio;

		int newWidth = (int) ((double) originalX / bestRatio);
		int newHeight = (int) ((double) originalY / bestRatio);

		return new Point(newWidth, newHeight);
	}

	public void setInput(IResource selection) {
		if (selection == null)
			return;
		IContainer newInput;
		if (selection instanceof IContainer)
			newInput = (IContainer)selection;
		else
			newInput = selection.getParent();

		// XXX checking if the same would be nice to have handled at the context
		// level:
		if (newInput != this.input) {
			this.input = newInput;
			this.runnable = null;

			try {
				IContainer container = (IContainer) input;
				IResource[] members;
				members = container.members();
				gallery.removeAll();
				group = new GalleryItem(gallery, SWT.NONE);
				group.setExpanded(true);
				final List images = new ArrayList();
				for (int i = 0; i < members.length; i++) {
					IResource resource = members[i];
					if (resource.getType() == IResource.FILE) {
						images.add(resource);
					}
				}
				if (images.size() == 0)
					return;
				final int[] counter = { 0 };
				runnable = new Runnable() {
					public void run() {
						if (runnable != this) {
							return;
						}
						addImage((IFile) images.get(counter[0]++));
						if (gallery != null && !gallery.isDisposed()
								&& counter[0] < images.size()) {
							gallery.getDisplay().asyncExec(this);
						}
					}
				};
				gallery.getDisplay().asyncExec(runnable);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void addImage(final IFile file) {
		// XXX we are reading the image data in the UI thread but shouldn't. 
		InputStream contents;
		try {
			contents = file.getContents();
			try {
				ImageData imageData = new ImageData(contents);

				Point size = getBestSize(imageData.width, imageData.height, 100, 100);

				ImageData scaled = imageData.scaledTo(size.x, size.y);
				GalleryItem item = new GalleryItem(group, SWT.NONE);
				item.setText(file.getName());
				Image image = new Image(gallery.getDisplay(), scaled);
				item.setImage(image);
				item.setData(file);
				
				//Required for bug #260406
				CSSSWTEngineImpl engine = (CSSSWTEngineImpl) gallery.getDisplay().getData("org.eclipse.e4.ui.css.core.engine");
				if(engine != null)
					engine.applyStyles(item, true);
		
				gallery.redraw();
			} catch (SWTException ex) {
			} finally {
				try {
					contents.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
