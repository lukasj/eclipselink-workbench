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
package org.eclipse.persistence.tools.workbench.test.utility.diff.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.persistence.tools.workbench.utility.string.StringTools;

public class MultiClassEmployee implements Employee {
    private int id;
    private String name;
    private float salary;
    private String position;
    private Set comments;
    private Address address;
    private Collection dependents;
    private List cars;
    private Map phoneNumbers;    // keyed by description
    private Collection underlings;
    private List vacationBackups;
    private Map eatingPartners;    // keyed by meal


    public MultiClassEmployee(int id, String name) {
        super();
        this.id = id;
        this.name = name;
        this.salary = 0;
        this.position = "";
        this.comments = new HashSet();
        this.dependents = new ArrayList();
        this.cars = new ArrayList();
        this.phoneNumbers = new HashMap();
        this.underlings = new ArrayList();
        this.vacationBackups = new ArrayList();
        this.eatingPartners = new HashMap();
    }

    @Override
    public int getId() {
        return this.id;
    }
    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public void setName(String string) {
        this.name = string;
    }

    @Override
    public float getSalary() {
        return this.salary;
    }
    @Override
    public void setSalary(float f) {
        this.salary = f;
    }

    @Override
    public String getPosition() {
        return this.position;
    }
    @Override
    public void setPosition(String string) {
        this.position = string;
    }

    @Override
    public Iterator comments() {
        return this.comments.iterator();
    }
    @Override
    public void addComment(String comment) {
        this.comments.add(comment);
    }
    @Override
    public void clearComments() {
        this.comments.clear();
    }

    @Override
    public Address getAddress() {
        return this.address;
    }
    @Override
    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public Iterator dependents() {
        return this.dependents.iterator();
    }
    @Override
    public Dependent addDependent(String depName, String depDescription) {
        Dependent dependent = new Dependent(depName, depDescription);
        this.dependents.add(dependent);
        return dependent;
    }
    @Override
    public void clearDependents() {
        this.dependents.clear();
    }
    @Override
    public Dependent dependentNamed(String depName) {
        for (Iterator stream = this.dependents(); stream.hasNext(); ) {
            Dependent dependent = (Dependent) stream.next();
            if (dependent.getName().equals(depName)) {
                return dependent;
            }
        }
        throw new IllegalArgumentException("dependent not found: " + depName);
    }

    @Override
    public ListIterator cars() {
        return this.cars.listIterator();
    }
    @Override
    public Car addCar(String carName, String carDescription) {
        Car car = new Car(carName, carDescription);
        this.cars.add(car);
        return car;
    }
    @Override
    public void clearCars() {
        this.cars.clear();
    }
    @Override
    public Car carNamed(String carName) {
        for (Iterator stream = this.cars(); stream.hasNext(); ) {
            Car car = (Car) stream.next();
            if (car.getName().equals(carName)) {
                return car;
            }
        }
        throw new IllegalArgumentException("car not found: " + carName);
    }

    @Override
    public Iterator phoneNumbers() {
        return this.phoneNumbers.entrySet().iterator();
    }
    @Override
    public PhoneNumber addPhoneNumber(String phoneDescription, String areaCode, String exchange, String number, String extension) {
        PhoneNumber phone = new PhoneNumber(areaCode, exchange, number, extension);
        this.phoneNumbers.put(phoneDescription, phone);
        return phone;
    }
    @Override
    public PhoneNumber addPhoneNumber(String phoneDescription, String areaCode, String exchange, String number) {
        PhoneNumber phone = new PhoneNumber(areaCode, exchange, number);
        this.phoneNumbers.put(phoneDescription, phone);
        return phone;
    }
    @Override
    public void clearPhoneNumbers() {
        this.phoneNumbers.clear();
    }
    @Override
    public PhoneNumber getPhoneNumber(String phoneDescription) {
        return (PhoneNumber) this.phoneNumbers.get(phoneDescription);
    }

    @Override
    public Iterator underlings() {
        return this.underlings.iterator();
    }
    @Override
    public void addUnderling(Employee underling) {
        this.underlings.add(underling);
    }
    @Override
    public void clearUnderlings() {
        this.underlings.clear();
    }
    @Override
    public Employee underlingNamed(String underlingName) {
        for (Iterator stream = this.underlings(); stream.hasNext(); ) {
            Employee underling = (Employee) stream.next();
            if (underling.getName().equals(underlingName)) {
                return underling;
            }
        }
        throw new IllegalArgumentException("underling not found: " + underlingName);
    }

    @Override
    public Iterator vacationBackups() {
        return this.vacationBackups.iterator();
    }
    @Override
    public void addVacationBackup(Employee vacationBackup) {
        this.vacationBackups.add(vacationBackup);
    }
    @Override
    public void clearVacationBackups() {
        this.vacationBackups.clear();
    }
    @Override
    public Employee vacationBackupNamed(String vacationBackupName) {
        for (Iterator stream = this.vacationBackups(); stream.hasNext(); ) {
            Employee vacationBackup = (Employee) stream.next();
            if (vacationBackup.getName().equals(vacationBackupName)) {
                return vacationBackup;
            }
        }
        throw new IllegalArgumentException("vacation backup not found: " + vacationBackupName);
    }

    @Override
    public Iterator eatingPartners() {
        return this.eatingPartners.entrySet().iterator();
    }
    @Override
    public void setEatingPartner(String meal, Employee partner) {
        this.eatingPartners.put(meal, partner);
    }
    @Override
    public void clearEatingPartners() {
        this.eatingPartners.clear();
    }
    @Override
    public Employee getEatingPartner(String meal) {
        return (Employee) this.eatingPartners.get(meal);
    }

    @Override
    public String toString() {
        return StringTools.buildToStringFor(this, this.name);
    }

}
