package org.eclipse.e4.tools.emf.ui.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public abstract class ImageTooltip extends ToolTip {
	private Image image;
	
	public ImageTooltip(Control control) {
		super(control);

	}

	@Override
	protected boolean shouldCreateToolTip(Event event) {
		if( getImageURI() != null ) {
			return super.shouldCreateToolTip(event);	
		}
		return false;
	}
	
	@Override
	protected Composite createToolTipContentArea(Event event, Composite parent) {
		clearResources();
		parent = new Composite(parent, SWT.NONE);
		parent.setBackground(event.widget.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);
		parent.setLayout(new GridLayout(2, false));

		URI uri = getImageURI(); 
		
		if( uri != null ) {
			int fileSize = -1;
			ByteArrayOutputStream out = null;
			InputStream stream = null;
			InputStream bStream = null;
			String errorMessage = "<Unknow Error>";
			try {
				URL url = new URL(uri.toString());
				stream = url.openStream();
				
				if( stream != null ) {
					out = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];
					int length;
					while( ( length = stream.read(buf)) != -1 ) {
						out.write(buf,0,length);
					}
					fileSize = out.size();
					bStream = new ByteArrayInputStream(out.toByteArray());
					image = new Image(parent.getDisplay(),bStream);
				}
			} catch (MalformedURLException e) {
				errorMessage = e.getMessage();
			} catch (FileNotFoundException e) {
				if( uri.isPlatform() ) {
					errorMessage = "File '" + e.getMessage() + "' not found in bundle '"+uri.segment(1)+"'";	
				} else {
					errorMessage = e.getMessage();
				}
			} catch (IOException e) {
				errorMessage = e.getMessage();
			} catch(Exception e) {
				errorMessage = e.getMessage();
			}finally {
				if(out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if( bStream != null ) {
					try {
						bStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				if( stream != null ) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			// ---------------------------------
			Label l = new Label(parent, SWT.NONE);
			l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
			l.setText("Icon:");
			
			l = new Label(parent,SWT.NONE);
			if( image == null ) {
				System.err.println(errorMessage);
				l.setText(errorMessage);
			} else {
				l.setImage(image);
			}

			// ---------------------------------
			
			l = new Label(parent, SWT.NONE);
			l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
			l.setText("Name:");
			
			l = new Label(parent,SWT.NONE);
			l.setText(uri.lastSegment());
			
			// ---------------------------------
			
			l = new Label(parent, SWT.NONE);
			l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
			l.setText("Dimensions:");
			
			l = new Label(parent,SWT.NONE);
			if( image != null ) {
				l.setText(image.getBounds().width + "x" + image.getBounds().height+" px");	
			} else {
				l.setText("0x0 px");
			}
			

			// ---------------------------------
			
			l = new Label(parent, SWT.NONE);
			l.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
			l.setText("File-Size:");
			
			l = new Label(parent,SWT.NONE);
			l.setText( new DecimalFormat("#,##0.00").format((fileSize / 1024.0)) + "KB" );			
		}
		
		return parent;
	}

	@Override
	protected void afterHideToolTip(Event event) {
		super.afterHideToolTip(event);
		clearResources();
	}
	
	protected abstract URI getImageURI();
	
	private void clearResources() {
		if( image != null ) {
			image.dispose();
			image = null;
		}
	}
}
