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

import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * This is an implementation of {@link Archive} when container returns a
 * file: url that refers to a jar file. e.g. file:/tmp/a_ear/lib/pu.jar
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JarFileArchive implements Archive {
    private JarFile jarFile;

    private URL rootURL;

    private Logger logger;

    public JarFileArchive(JarFile jarFile) throws MalformedURLException {
        this(jarFile, Logger.global);
    }

    public JarFileArchive(JarFile jarFile, Logger logger)
            throws MalformedURLException {
        logger.entering("JarFileArchive", "JarFileArchive", // NOI18N
                new Object[]{jarFile});
        this.logger = logger;
        this.jarFile = jarFile;
        rootURL = new File(jarFile.getName()).toURI().toURL();
        logger.logp(Level.FINER, "JarFileArchive", "JarFileArchive", // NOI18N
                "rootURL = {0}", rootURL); // NOI18N
    }

    public Iterator<String> getEntries() {
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        ArrayList<String> result = new ArrayList<String>();
        while (jarEntries.hasMoreElements()) {
            final JarEntry jarEntry = jarEntries.nextElement();
            if(!jarEntry.isDirectory()) { // exclude directory entries
                result.add(jarEntry.getName());
            }
        }
        return result.iterator();
    }

    public InputStream getEntry(String entryPath) throws IOException {
        InputStream is = null;
        final ZipEntry entry = jarFile.getEntry(entryPath);
        if (entry != null) {
            is = jarFile.getInputStream(entry);
        }
        return is;
    }

    public URL getEntryAsURL(String entryPath) throws IOException {
        return jarFile.getEntry(entryPath)!= null ?
                new URL("jar:"+rootURL+"!/"+entryPath) : null; // NOI18N
    }

    public URL getRootURL() {
        return rootURL;
    }
}
