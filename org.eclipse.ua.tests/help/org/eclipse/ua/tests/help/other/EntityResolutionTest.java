/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import junit.framework.TestCase;

import org.eclipse.help.internal.entityresolver.LocalEntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class EntityResolutionTest extends TestCase {
	
	public void resolve(String systemId, boolean isSupportedDtd) {
		try {
			LocalEntityResolver resolver = new LocalEntityResolver();
			InputSource is = resolver.resolveEntity("publicId", systemId);
			Reader reader = is.getCharacterStream();
			InputStream stream = is.getByteStream();
			int read;
			if (reader != null) {
				char[] cbuf = new char[5];
				read = reader.read(cbuf);
			} else {
				byte buf[] = new byte[5];
				read = stream.read(buf);
			}
			if (isSupportedDtd) {
				assertTrue("Entity not found", read > 0);
			} else {
				assertTrue("Unsupported Entity did not return empty stream", read == -1);
			}
		} catch (SAXException e) {
			fail("SAX Exception thrown");
		} catch (IOException e) {
			fail("IO Exception thrown");
		}
	}
	
	public void testXhtml1()  {
		resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", true);
	}

    public void testFramset() {
	    resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd", true);
    }
    
    public void testFlat() {
	    resolve("http://www.w3.org/TR/xhtml11/DTD/xhtml11-flat.dtd", true);
    }
    
    public void testStrict() {
	    resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", true);
    }
    
    public void testTransitional() {
	    resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd", true);
    }
    
    public void testLat1() {
	    resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent", true);
    }
    
    public void testSpecial() {
	    resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent", true);
    }
    
    public void testResolveSymbol() {
	    resolve("http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent", true);
    }
	
	public void testUnsupportedDtds() throws SAXException, IOException {
		resolve("xyz", false);
		resolve("", false);
		resolve("http://www.w3.org/TR/xhtml2/DTD/xhtml2-transitional.dtd", false);
	}
		
}
