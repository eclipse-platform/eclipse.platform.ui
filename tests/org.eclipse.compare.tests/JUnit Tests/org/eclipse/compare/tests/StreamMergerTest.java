/*
 * Created on Apr 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.compare.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.eclipse.compare.IStreamMerger;
import org.eclipse.compare.internal.merge.TextStreamMerger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


public class StreamMergerTest extends TestCase {

    String encoding= "UTF-8"; //$NON-NLS-1$

    public StreamMergerTest(String name) {
        super(name);
    }

	public void testIncomingAddition() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\ndef\nxyz"; //$NON-NLS-1$
        String o= "abc\ndef\n123\nxyz"; //$NON-NLS-1$
        
        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);
        
        assertEquals(status.getSeverity(), IStatus.OK);
        assertEquals(status.getCode(), IStatus.OK);
        assertEquals(output.toString(), "abc\ndef\n123\nxyz\n"); //$NON-NLS-1$
	}
	
	public void testIncomingDeletion() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\ndef\nxyz"; //$NON-NLS-1$
        String o= "abc\nxyz"; //$NON-NLS-1$
        
        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);
        
        assertEquals(status.getSeverity(), IStatus.OK);
        assertEquals(status.getCode(), IStatus.OK);
        assertEquals(output.toString(), "abc\nxyz\n"); //$NON-NLS-1$
	}
	
	public void testIncomingReplacement() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\ndef\nxyz"; //$NON-NLS-1$
        String o= "abc\n123\nxyz"; //$NON-NLS-1$
        
        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);
        
        assertEquals(status.getSeverity(), IStatus.OK);
        assertEquals(status.getCode(), IStatus.OK);
        assertEquals(output.toString(), "abc\n123\nxyz\n"); //$NON-NLS-1$
	}
	
	public void testNonConflictingMerge() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\ndef\nxyz\nfoo"; //$NON-NLS-1$
        String o= "abc\n123\n456\nxyz"; //$NON-NLS-1$
        
        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);
        
        assertEquals(status.getSeverity(), IStatus.OK);
        assertEquals(status.getCode(), IStatus.OK);
        assertEquals(output.toString(), "abc\n123\n456\nxyz\nfoo\n"); //$NON-NLS-1$
	}
	
	public void testConflictingReplacement() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\nfoo\nxyz"; //$NON-NLS-1$
        String o= "abc\nbar\nxyz"; //$NON-NLS-1$

        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);

        assertEquals(status.getSeverity(), IStatus.ERROR);
        assertEquals(status.getCode(), IStreamMerger.CONFLICT);
	}
	
	public void testConflictingAddition() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\ndef\n123\nxyz"; //$NON-NLS-1$
        String o= "abc\ndef\n123\nxyz"; //$NON-NLS-1$

        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);

        assertEquals(status.getSeverity(), IStatus.OK);
        assertEquals(status.getCode(), IStatus.OK);
        assertEquals(output.toString(), "abc\ndef\n123\nxyz\n"); //$NON-NLS-1$
	}
	
	public void testConflictingDeletion() throws UnsupportedEncodingException {
	    
        String a= "abc\ndef\nxyz"; //$NON-NLS-1$
        String t= "abc\nxyz"; //$NON-NLS-1$
        String o= "abc\nxyz"; //$NON-NLS-1$

        StringBuffer output= new StringBuffer();
        
        IStatus status= merge(output, a, t, o);

        assertEquals(status.getSeverity(), IStatus.OK);
        assertEquals(status.getCode(), IStatus.OK);
        assertEquals(output.toString(), "abc\nxyz\n"); //$NON-NLS-1$
	}
	
	private IStatus merge(StringBuffer output, String a, String m, String y) throws UnsupportedEncodingException {
        InputStream ancestor= new ByteArrayInputStream(a.getBytes(encoding));
        InputStream target= new ByteArrayInputStream(m.getBytes(encoding));
        InputStream other= new ByteArrayInputStream(y.getBytes(encoding));
        
        ByteArrayOutputStream os= new ByteArrayOutputStream();

        IStreamMerger merger= new TextStreamMerger();
        IStatus status= merger.merge(os, encoding, ancestor, encoding, target, encoding, other, encoding, (IProgressMonitor)null);

        output.append(new String(os.toByteArray(), encoding));
 
        return status;
	}
}
