/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.tools.workbench.utility.classfile;

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.persistence.tools.workbench.utility.classfile.tools.ClassFileDataInputStream;
import org.eclipse.persistence.tools.workbench.utility.io.IndentingPrintWriter;


/**
 * This class models a class file long constant:
 *     u1 tag;
 *     u4 high_bytes;
 *     u4 low_bytes;
 *
 * See "The Java Virtual Machine Specification" Chapter 4.
 */
public class LongConstant extends Constant {
    private long value;

    /**
     * Construct a class file constant from the specified stream
     * of byte codes.
     */
    LongConstant(ConstantPool pool, byte tag, ClassFileDataInputStream stream) throws IOException {
        super(pool, tag, stream);
    }

    @Override
    void initialize(ClassFileDataInputStream stream) throws IOException {
        this.value = stream.readLong();
    }

    @Override
    public void displayStringOn(IndentingPrintWriter writer) {
        super.displayStringOn(writer);
        writer.print(" value: ");
        writer.println(this.value);
    }

    @Override
    public String description() {
        return "long";
    }

    @Override
    public void printFieldInitializationClauseOn(PrintWriter writer) {
        writer.print(this.value);
        writer.print('L');
    }

    @Override
    boolean consumesTwoPoolEntries() {
        return true;
    }

    public long longValue() {
        return this.value;
    }

    @Override
    public Object value() {
        return new Long(this.value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
