/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.libraries.antlr.runtime;

import java.io.*;

/** A kind of ReaderStream that pulls from an InputStream.
 *  Useful for reading from stdin and specifying file encodings etc...
  */
public class ANTLRInputStream extends ANTLRReaderStream {
	public ANTLRInputStream() {
	}

	public ANTLRInputStream(InputStream input) throws IOException {
		this(input, null);
	}

	public ANTLRInputStream(InputStream input, int size) throws IOException {
		this(input, size, null);
	}

	public ANTLRInputStream(InputStream input, String encoding) throws IOException {
		this(input, INITIAL_BUFFER_SIZE, encoding);
	}

	public ANTLRInputStream(InputStream input, int size, String encoding) throws IOException {
		this(input, size, READ_BUFFER_SIZE, encoding);
	}

	public ANTLRInputStream(InputStream input,
							int size,
							int readBufferSize,
							String encoding)
		throws IOException
	{
		InputStreamReader isr;
		if ( encoding!=null ) {
			isr = new InputStreamReader(input, encoding);
		}
		else {
			isr = new InputStreamReader(input);
		}
		load(isr, size, readBufferSize);
	}
}