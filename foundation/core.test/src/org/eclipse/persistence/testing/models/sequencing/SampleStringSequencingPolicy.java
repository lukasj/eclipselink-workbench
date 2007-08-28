/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.sequencing;

import java.util.Vector;
import org.eclipse.persistence.sequencing.TableSequence;
import org.eclipse.persistence.exceptions.ValidationException;

// This sample SequencingValueGenerationPolicy:
// 1. Produces Strings which may contain only letter from 'a' to 'z'
// 2. Overrides existing values if they are:
//        not of type String,
//        an empty String,
//        a String containing any character other than 'a'...'z'
public class SampleStringSequencingPolicy extends TableSequence {
    protected char min = 'a';
    protected char max = 'z';
    protected int num = max - min + 1;

    public SampleStringSequencingPolicy(String name, int size) {
        super(name, size);
    }

    // existingValue should be of type String, not empty, contain only 'a'..'z'
    public boolean shouldOverrideExistingValue(String seqName, Object existingValue) {
        String str;
        try {
            str = (String)existingValue;
        } catch (ClassCastException ex) {
            return true;
        }
        if (str.length() == 0) {
            return true;
        }
        boolean override = false;
        for (int i = 0; (i < str.length()) && !override; i++) {
            override = (min > str.charAt(i)) || (str.charAt(i) > max);
        }
        return override;
    }

    protected Vector createVector(Number sequence, String seqName, int size) {
        long last = sequence.longValue();
        long first = last - size + 1;
        if (first < 0) {
            throw ValidationException.sequenceSetupIncorrectly(seqName);
        }

        Vector sequencesForName = new Vector(size);

        for (long index = first; index <= last; index++) {
            String seqValue = createString(index);
            sequencesForName.addElement(seqValue);
        }
        return sequencesForName;
    }

    protected String createString(long n) {
        int size = 24;
        char[] ch = new char[size];
        int count = 0;
        while (n > 0) {
            int i = (int)(n % num);
            count++;
            ch[size - count] = (char)(min + i);
            n = n / num;
        }
        String str = new String(ch, size - count, count);
        return str;
    }
}