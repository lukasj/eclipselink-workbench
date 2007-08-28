/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.models.inheritance;

import org.eclipse.persistence.testing.models.inheritance.Animal_Matt;

/**
 * An extension to the DeepInheritanceTestModel, this class was required to have
 * a 1-1 to Animal_Matt and its subclasses, and to have a name that comes before
 * 'Animal' in Lexigraphical order.
 * This way Alligator will come before Animal_Matt and all its subclasses in
 * commit order by default, and tests 3019934, where in fact Alligator must be
 * placed last.
 * @author Stephen McRitchie
 */
public class Alligator {
    public int id;
    private Animal_Matt latestVictim;
    private String favoriteSwamp;

    public Alligator() {
    }

    public Animal_Matt getLatestVictim() {
        return latestVictim;
    }

    public String getFavoriteSwamp() {
        return favoriteSwamp;
    }

    public void setLatestVictim(Animal_Matt latestVictim) {
        this.latestVictim = latestVictim;
    }

    public void setFavoriteSwamp(String favoriteSwamp) {
        this.favoriteSwamp = favoriteSwamp;
    }
}