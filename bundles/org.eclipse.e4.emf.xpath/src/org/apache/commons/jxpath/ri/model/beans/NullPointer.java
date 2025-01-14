/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.model.beans;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Pointer whose value is {@code null}.
 */
public class NullPointer extends PropertyOwnerPointer {
    private QName name;
    private String id;

    private static final long serialVersionUID = 2193425983220679887L;

    /**
     * Create a new NullPointer.
     * @param name node name
     * @param locale Locale
     */
    public NullPointer(final QName name, final Locale locale) {
        super(null, locale);
        this.name = name;
    }

    /**
     * Used for the root node.
     * @param parent parent pointer
     * @param name node name
     */
    public NullPointer(final NodePointer parent, final QName name) {
        super(parent);
        this.name = name;
    }

    /**
     * Create a new NullPointer.
     * @param locale Locale
     * @param id String
     */
    public NullPointer(final Locale locale, final String id) {
        super(null, locale);
        this.id = id;
    }

    @Override
    public QName getName() {
        return name;
    }

    @Override
    public Object getBaseValue() {
        return null;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean isActual() {
        return false;
    }

    @Override
    public PropertyPointer getPropertyPointer() {
        return new NullPropertyPointer(this);
    }

    @Override
    public NodePointer createPath(final JXPathContext context, final Object value) {
        if (parent != null) {
            return parent.createPath(context, value).getValuePointer();
        }
        throw new UnsupportedOperationException(
            "Cannot create the root object: " + asPath());
    }

    @Override
    public NodePointer createPath(final JXPathContext context) {
        if (parent != null) {
            return parent.createPath(context).getValuePointer();
        }
        throw new UnsupportedOperationException(
            "Cannot create the root object: " + asPath());
    }

    @Override
    public NodePointer createChild(
        final JXPathContext context,
        final QName name,
        final int index) {
        return createPath(context).createChild(context, name, index);
    }

    @Override
    public NodePointer createChild(
        final JXPathContext context,
        final QName name,
        final int index,
        final Object value) {
        return createPath(context).createChild(context, name, index, value);
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof NullPointer)) {
            return false;
        }

        final NullPointer other = (NullPointer) object;
        return name == other.name || name != null && name.equals(other.name);
    }

    @Override
    public String asPath() {
        if (id != null) {
            return "id(" + id + ")";
        }
        return parent == null ? "null()" : super.asPath();
    }

    @Override
    public int getLength() {
        return 0;
    }
}
