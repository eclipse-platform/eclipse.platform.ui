/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

/**
 * Abstract base implementation of all workbench editors.
 * <p>
 * This class should be subclassed by clients wishing to define new editors.
 * The name of the subclass should be given as the <code>"class"</code> 
 * attribute in a <code>editor</code> extension contributed to the workbench's
 * view extension point (named <code>"org.eclipse.ui.editors"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.editors"&GT;
 *      &LT;editor id="com.example.myplugin.ed"
 *         name="My Editor"
 *         icon="./images/cedit.gif"
 * 		   extensions="foo" 
 * 		   class="com.example.myplugin.MyFooEditor" 
 * 		   contributorClass="com.example.myplugin.MyFooEditorContributor" 
 *      /&GT;
 * &LT;/extension&GT;
 * </pre>
 * where <code>com.example.myplugin.MyEditor</code> is the name of the
 * <code>EditorPart</code> subclass.
 * </p>
 * <p>
 * Subclasses must implement the following methods:
 * <ul>
 *   <li><code>IEditorPart.init</code> - to initialize editor when assigned its site</li>
 *   <li><code>IWorkbenchPart.createPartControl</code> - to create the editor's controls </li>
 *   <li><code>IWorkbenchPart.setFocus</code> - to accept focus</li>
 *   <li><code>IEditorPart.isDirty</code> - to decide whether a significant change has
 *       occurred</li>
 *   <li><code>IEditorPart.doSave</code> - to save contents of editor</li>
 *   <li><code>IEditorPart.doSaveAs</code> - to save contents of editor</li>
 *   <li><code>IEditorPart.isSaveAsAllowed</code> - to control Save As</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend or reimplement the following methods as required:
 * <ul>
 *   <li><code>IExecutableExtension.setInitializationData</code> - extend to provide additional 
 *       initialization when editor extension is instantiated</li>
 *   <li><code>IWorkbenchPart.dispose</code> - extend to provide additional cleanup</li>
 *   <li><code>IAdaptable.getAdapter</code> - reimplement to make the editor
 *       adaptable</li>
 * </ul>
 * </p>
 */
public abstract class EditorPart extends WorkbenchPart implements IEditorPart {

    /**
     * Editor input, or <code>null</code> if none.
     */
    private IEditorInput editorInput = null;

    /**
     * Listens to PROP_TITLE property changes in this object until the first call to
     * setContentDescription. Used for compatibility with old parts that call setTitle
     * or overload getTitle instead of using setContentDescription. 
     */
    private IPropertyListener compatibilityTitleListener = new IPropertyListener() {
        /* (non-Javadoc)
         * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
         */
        public void propertyChanged(Object source, int propId) {
            if (propId == IWorkbenchPartConstants.PROP_TITLE) {
                setDefaultPartName();
            }
        }
    };

    /**
     * Creates a new workbench editor.
     */
    protected EditorPart() {
        super();

        addPropertyListener(compatibilityTitleListener);
    }

    /* (non-Javadoc)
     * Saves the contents of this editor.
     * <p>
     * Subclasses must override this method to implement the open-save-close lifecycle
     * for an editor.  For greater details, see <code>IEditorPart</code>
     * </p>
     *
     * @see IEditorPart
     */
    public abstract void doSave(IProgressMonitor monitor);

    /* (non-Javadoc)
     * Saves the contents of this editor to another object.
     * <p>
     * Subclasses must override this method to implement the open-save-close lifecycle
     * for an editor.  For greater details, see <code>IEditorPart</code>
     * </p>
     *
     * @see IEditorPart
     */
    public abstract void doSaveAs();

    /* (non-Javadoc)
     * Method declared on IEditorPart.
     */
    public IEditorInput getEditorInput() {
        return editorInput;
    }

    /* (non-Javadoc)
     * Method declared on IEditorPart.
     */
    public IEditorSite getEditorSite() {
        return (IEditorSite) getSite();
    }

    /* (non-Javadoc)
     * Gets the title tool tip text of this part.
     *
     * @return the tool tip text
     */
    public String getTitleToolTip() {
        if (editorInput == null) {
			return super.getTitleToolTip();
		} else {
			return editorInput.getToolTipText();
		}
    }

    /* (non-Javadoc)
     * Initializes the editor part with a site and input.
     * <p>
     * Subclasses of <code>EditorPart</code> must implement this method.  Within
     * the implementation subclasses should verify that the input type is acceptable
     * and then save the site and input.  Here is sample code:
     * </p>
     * <pre>
     *		if (!(input instanceof IFileEditorInput))
     *			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
     *		setSite(site);
     *		setInput(input);
     * </pre>
     */
    public abstract void init(IEditorSite site, IEditorInput input)
            throws PartInitException;

    /* (non-Javadoc)
     * Returns whether the contents of this editor have changed since the last save
     * operation.
     * <p>
     * Subclasses must override this method to implement the open-save-close lifecycle
     * for an editor.  For greater details, see <code>IEditorPart</code>
     * </p>
     *
     * @see IEditorPart
     */
    public abstract boolean isDirty();

    /* (non-Javadoc)
     * Returns whether the "save as" operation is supported by this editor.
     * <p>
     * Subclasses must override this method to implement the open-save-close lifecycle
     * for an editor.  For greater details, see <code>IEditorPart</code>
     * </p>
     *
     * @see IEditorPart
     */
    public abstract boolean isSaveAsAllowed();

    /* (non-Javadoc)
     * Returns whether the contents of this editor should be saved when the editor
     * is closed.
     * <p>
     * This method returns <code>true</code> if and only if the editor is dirty 
     * (<code>isDirty</code>).
     * </p>
     */
    public boolean isSaveOnCloseNeeded() {
        return isDirty();
    }

    /**
     * Sets the input to this editor.  This method simply updates the internal
     * member variable.
     * 
     * <p>Unlike most of the other set methods on this class, this method does 
     * not fire a property change. Clients that call this method from a subclass 
     * must ensure that they fire an IWorkbenchPartConstants.PROP_INPUT property 
     * change after calling this method but before leaving whatever public method
     * they are in. Clients that expose this method as public API must fire
     * the property change within their implementation of setInput.</p>
     * 
     * <p>Note that firing a property change may cause listeners to immediately 
     * reach back and call methods on this editor. Care should be taken not to 
     * fire the property change until the editor has fully updated its internal 
     * state to reflect the new input.</p>
     *
     * @param input the editor input
     * 
     * @see #setInputWithNotify(IEditorInput)
     */
    protected void setInput(IEditorInput input) {
        editorInput = input;
    }
    
    /**
     * Sets the input to this editor and fires a PROP_INPUT property change if
     * the input has changed.  This is the convenience method implementation.
     * 
     * <p>Note that firing a property change may cause other objects to reach back
     * and invoke methods on the editor. Care should be taken not to call this method
     * until the editor has fully updated its internal state to reflect the
     * new input.</p>
     * 
     * @since 3.2
     *
     * @param input the editor input
     */
    protected void setInputWithNotify(IEditorInput input) {
        if (input != editorInput) {
            editorInput = input;
            firePropertyChange(PROP_INPUT);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setContentDescription(java.lang.String)
     */
    protected void setContentDescription(String description) {
        if (compatibilityTitleListener != null) {
            removePropertyListener(compatibilityTitleListener);
            compatibilityTitleListener = null;
        }

        super.setContentDescription(description);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setPartName(java.lang.String)
     */
    protected void setPartName(String partName) {
        if (compatibilityTitleListener != null) {
            removePropertyListener(compatibilityTitleListener);
            compatibilityTitleListener = null;
        }

        super.setPartName(partName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement cfig,
            String propertyName, Object data) {
        super.setInitializationData(cfig, propertyName, data);

        setDefaultPartName();
    }
    

    private void setDefaultPartName() {
        if (compatibilityTitleListener == null) {
            return;
        }

        internalSetPartName(getTitle());
    }

    /**
     * Set the default title for the receiver.
     */
    void setDefaultTitle() {
        setTitle(getPartName());
    }

    /**
     * Checks that the given site is valid for this type of part.
     * The site for an editor must be an <code>IEditorSite</code>.
     * 
     * @param site the site to check
     * @since 3.1
     */
    protected final void checkSite(IWorkbenchPartSite site) {
        super.checkSite(site);
        Assert.isTrue(site instanceof IEditorSite, "The site for an editor must be an IEditorSite"); //$NON-NLS-1$
    }    

}
