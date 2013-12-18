/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.api.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

/**
 * Records the association of {@link org.apache.lucene.analysis.Analyzer}s to specific keys. The semantics associated
 * with keys is left up to specific applications to decide, provided that an association of a key and an Analyzer
 * allows the unique (in the context of the application) association between a {@link org.apache.lucene.document
 * .Document} and an Analyzer as specified by {@link #getAnalyzerFor(org.apache.lucene.document.Document)}.
 *
 * An example application could record the association of a language code to an Analyzer specific to that particular
 * language. It would then be up to the application to decide which language (if any) is associated with a given
 * Document instance.
 *
 * @author Christophe Laprun
 */
public interface AnalyzerRegistry<T> {
    /**
     * Retrieves an {@link org.apache.lucene.analysis.Analyzer} instance capable of best processing the specified
     * {@link org.apache.lucene.document.Document}.
     *
     * @param document the Document instance we want to process with a specific Analyzer
     * @return an Analyzer instance best
     */
    Analyzer getAnalyzerFor(Document document);

    /**
     * Retrieves the key associated with the specified document.
     *
     * @param document the Document instance we want to retrieve the associated key for
     * @return the key associated with the specified document such that <code>get(getKeyFor(document)) ==
     * getAnalyzerFor(document)</code> or <code>null</code> if no such key exists
     */
    T getKeyFor(Document document);

    /**
     * Whether or not this AnalyzerRegistry accepts the given key.
     *
     * @param key the key we want to check for acceptance
     * @return <code>true</code> if this AnalyzerRegistry accepts the given key, <code>false</code> otherwise.
     */
    boolean acceptKey(Object key);

    /**
     * Retrieves the Analyzer instance associated with the specified the key.
     *
     * @param key the key identifying which Analyzer to retrieve
     * @return the Analyzer instance associated with the specified key or <code>null</code> otherwise.
     */
    Analyzer getAnalyzer(T key);
}
