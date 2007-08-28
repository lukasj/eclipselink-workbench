/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.internal.jpa.deployment;

import java.net.URL;
import java.net.JarURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an implementation of {@link Archive} which is used when container
 * returns a jar: URL. e.g. jar:file:/tmp/a_ear/b.war!/WEB-INF/classes/
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class DirectoryInsideJarURLArchive  implements Archive {
    /*
     * Implementation Note: This class does not have any dependency on TopLink
     * or GlassFish implementation classes. Please retain this searation.
     */

    private JarFile jarFile;

    // URL representation of this archive
    private URL rootURL;

    // distance from top of the JarFile to the entry
    private String relativeRootPath;

    private List<String> entries = new ArrayList<String>();

    private Logger logger;

    public DirectoryInsideJarURLArchive(URL url) throws IOException {
        this(url, Logger.global);
    }

    public DirectoryInsideJarURLArchive(URL url, Logger logger)
            throws IOException {
        logger.entering("DirectoryInsideJarURLArchive", "DirectoryInsideJarURLArchive",  // NOI18N
                new Object[]{url});
        this.logger = logger;
        assert(url.getProtocol().equals("jar")); // NOI18N
        rootURL = url;
        JarURLConnection conn =
                JarURLConnection.class.cast(url.openConnection());
        jarFile = conn.getJarFile();
        logger.logp(Level.FINER, "DirectoryInsideJarURLArchive",
                "DirectoryInsideJarURLArchive", "jarFile = {0}", jarFile);
        relativeRootPath = conn.getEntryName();
        init();
    }

    private void init() {
        for(Enumeration<JarEntry> jarEntries = jarFile.entries();
            jarEntries.hasMoreElements();) {
            JarEntry jarEntry = jarEntries.nextElement();
            if(jarEntry.isDirectory()) {
                continue;
            }
            String jarEntryName = jarEntry.getName();
            if (relativeRootPath==null) {
                entries.add(jarEntryName);
            } else if (jarEntryName.startsWith(relativeRootPath)) {
                entries.add(jarEntryName.substring(relativeRootPath.length()));
            }
        }
    }

    public Iterator<String> getEntries() {
        return entries.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        InputStream is = entries.contains(entryPath) ?
            jarFile.getInputStream(jarFile.getEntry(relativeRootPath + entryPath)) : null;
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        URL result = entries.contains(entryPath) ?
            new URL("jar:"+rootURL+"!/"+ relativeRootPath + entryPath) : null; // NOI18N
        return result;
    }

    public URL getRootURL() {
        return rootURL;
    }
}