package org.eclipse.jface.wizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * The WizardTableOfContentsNode is the class that represents 
 * each node in the table of contents.
 */
public class WizardTableOfContentsNode {

	/**
	 * Image registry key for decision image (value <code>"toc_decision_image"</code>).
	 */
	private static final String TOC_IMG_DECISION = "toc_decision_image"; //$NON-NLS-1$

	/**
	 * Image registry key for last page image (value <code>"toc_last_image"</code>).
	 */
	private static final String TOC_IMG_LAST = "toc_last_image"; //$NON-NLS-1$

	/**
	 * Image registry key for next image (value <code>"toc_next_image"</code>).
	 */
	private static final String TOC_IMG_NEXT = "toc_next_image"; //$NON-NLS-1$
	
	/**
	 * The widgets that are created for this node.
	 */
	private Composite innerComposite;
	private Label imageLabel;
	private Label titleLabel;

	static {
		ImageRegistry reg = JFaceResources.getImageRegistry();
		reg.put(TOC_IMG_DECISION, ImageDescriptor.createFromFile(WizardTableOfContentsNode.class, "images/decision.gif")); //$NON-NLS-1$
		reg.put(TOC_IMG_LAST, ImageDescriptor.createFromFile(WizardTableOfContentsNode.class, "images/last.gif")); //$NON-NLS-1$
		reg.put(TOC_IMG_NEXT, ImageDescriptor.createFromFile(WizardTableOfContentsNode.class, "images/next.gif")); //$NON-NLS-1$
	}

	IWizardPage page;

	/**
	 * Create a new instance of the receiver with newPage as the page
	 * that is activated on selection.
	 * @param newPage
	 */
	public WizardTableOfContentsNode(IWizardPage newPage) {
		this.page = newPage;
	}

	/**
	 * Create the two labels used to select this node as direct children
	 * of the composite.
	 * @param Composite composite.
	 */
	public void createWidgets(Composite parentComposite) {

		innerComposite = new Composite(parentComposite, SWT.NULL);
		innerComposite.setLayout(new GridLayout());

		imageLabel = new Label(innerComposite, SWT.NULL);
		imageLabel.setImage(JFaceResources.getImage(TOC_IMG_NEXT));
		titleLabel = new Label(innerComposite, SWT.NULL);
		titleLabel.setText(this.page.getTitle());
		titleLabel.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
		MouseListener clickListener = new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				page.getWizard().getContainer().showPage(page);
			}
			public void mouseUp(MouseEvent e) {
			}
		};
		imageLabel.addMouseListener(clickListener);
		titleLabel.addMouseListener(clickListener);

	}

	/**
	 * Returns the page.
	 * @return IWizardPage
	 */
	public IWizardPage getPage() {
		return page;
	}

	/**
	 * Sets the page.
	 * @param page The page to set.
	 */
	public void setPage(IWizardPage page) {
		this.page = page;
	}

	/**
	 * Dispose the widgets for the receiver.
	 */
	public void dispose(){
		innerComposite.dispose();
	}
	
	/**
	 * 	Enable or disable the widgets.
	 * @param boolean
	 */
	public void setEnabled(boolean enabled){
		innerComposite.setEnabled(enabled);
	}

}
