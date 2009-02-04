package org.eclipse.e4.demo.e4photo;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.services.IBackgroundRunner;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.behaviors.IHasInput;
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

public class Thumbnails implements IHasInput {

	private Gallery gallery;
	private GalleryItem group;
	private final IEclipseContext outputContext;
	private final IBackgroundRunner backgroundRunner;
	private IContainer input;

	public Thumbnails(Composite parent, final IEclipseContext outputContext,
			IBackgroundRunner backgroundRunner) {
		this.outputContext = outputContext;
		this.backgroundRunner = backgroundRunner;
		parent.setLayout(new FillLayout());
		gallery = new Gallery(parent, SWT.V_SCROLL | SWT.MULTI);
		gallery.setData("id", "thumbnails");

		Image itemImage = new Image(parent.getDisplay(), Program.findProgram("jpg")
				.getImageData());

		gallery.setGroupRenderer(new NoGroupRenderer());

		DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();
		ir.setShowLabels(false);
		ir.setDropShadowsSize(0);
		// ir.setSelectionBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));

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

	public Class getInputType() {
		return IContainer.class;
	}

	private Point getBestSize(int originalX, int originalY, int maxX, int maxY) {
		double widthRatio = (double) originalX / (double) maxX;
		double heightRatio = (double) originalY / (double) maxY;

		double bestRatio = widthRatio > heightRatio ? widthRatio : heightRatio;

		int newWidth = (int) ((double) originalX / bestRatio);
		int newHeight = (int) ((double) originalY / bestRatio);

		return new Point(newWidth, newHeight);
	}

	public void setInput(Object input) {
		if (input == null) {
			return;
		}
		if (input instanceof IFile)
			input = ((IFile) input).getParent();
		// XXX compare with previous input; if same container then skip
		// XXX this is actually be nice to have handled at the context level:
		// create child context
		// that has "input" being a container

		if (input != this.input) {
			this.input = (IContainer) input;

			try {
				IContainer container = (IContainer) input;
				IResource[] members;
				members = container.members();
				gallery.removeAll();
				group = new GalleryItem(gallery, SWT.NONE);
				group.setExpanded(true);
				for (int i = 0; i < members.length; i++) {
					IResource resource = members[i];
					if (resource.getType() == IResource.FILE) {
						addImage((IFile) resource);
					}
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void addImage(final IFile file) {
		gallery.getDisplay().asyncExec(new Runnable() {
			public void run() {
				InputStream contents;
				try {
					contents = file.getContents();
					try {
						ImageData imageData = new ImageData(contents);

						Point size = getBestSize(imageData.width, imageData.height, 100,
								100);

						ImageData scaled = imageData.scaledTo(size.x, size.y);
						GalleryItem item = new GalleryItem(group, SWT.NONE);
						item.setText(file.getName());
						Image image = new Image(gallery.getDisplay(), scaled);
						item.setImage(image);
						item.setData(file);
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
		});
	}
}
