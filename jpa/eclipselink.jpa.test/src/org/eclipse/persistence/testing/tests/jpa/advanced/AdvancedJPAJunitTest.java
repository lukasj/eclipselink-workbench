/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.testing.tests.jpa.advanced;

import java.util.Collection;
import javax.persistence.EntityManager;

import junit.framework.*;
import junit.extensions.TestSetup;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.invalidation.TimeToLiveCacheInvalidationPolicy;
import org.eclipse.persistence.descriptors.invalidation.CacheInvalidationPolicy;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.eclipse.persistence.mappings.ForeignReferenceMapping;
import org.eclipse.persistence.internal.jpa.EJBQueryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.helper.ClassConstants;

import org.eclipse.persistence.testing.framework.junit.JUnitTestCase;
import org.eclipse.persistence.testing.models.jpa.advanced.Address;
import org.eclipse.persistence.testing.models.jpa.advanced.AdvancedTableCreator;
import org.eclipse.persistence.testing.models.jpa.advanced.Department;
import org.eclipse.persistence.testing.models.jpa.advanced.Employee;
import org.eclipse.persistence.testing.models.jpa.advanced.EmployeePopulator;
import org.eclipse.persistence.testing.models.jpa.advanced.Equipment;
import org.eclipse.persistence.testing.models.jpa.advanced.EquipmentCode;
import org.eclipse.persistence.testing.models.jpa.advanced.GoldBuyer;
import org.eclipse.persistence.testing.models.jpa.advanced.PhoneNumber;

/**
 * This test suite tests TopLink JPA annotations extensions.
 */
public class AdvancedJPAJunitTest extends JUnitTestCase {
    private static int empId;
    private static int penelopeId;
    private static int deptId;
    private static int buyerId;
    private static long visa = 1234567890;
    private static long amex = 1987654321;
    private static long diners = 1192837465;
    private static long mastercard = 1647382910;
    private static String newResponsibility = "The most useless responsibility ever.";
    
    public AdvancedJPAJunitTest() {
        super();
    }
    
    public AdvancedJPAJunitTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.swingui.TestRunner.main(args);
    }
    
    public void setUp() {
        super.setUp();
        clearCache();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("AdvancedJPAJunitTest");
        
        suite.addTest(new AdvancedJPAJunitTest("testJoinFetchAnnotation"));
        suite.addTest(new AdvancedJPAJunitTest("testVerifyEmployeeCacheSettings"));
        suite.addTest(new AdvancedJPAJunitTest("testVerifyEmployeeCustomizerSettings"));
        
        suite.addTest(new AdvancedJPAJunitTest("testUpdateEmployee"));
        suite.addTest(new AdvancedJPAJunitTest("testVerifyUpdatedEmployee"));
        
        suite.addTest(new AdvancedJPAJunitTest("testCreateNewBuyer"));
        suite.addTest(new AdvancedJPAJunitTest("testVerifyNewBuyer"));
        suite.addTest(new AdvancedJPAJunitTest("testBuyerOptimisticLocking"));
        
        suite.addTest(new AdvancedJPAJunitTest("testGiveFredASexChange"));
        suite.addTest(new AdvancedJPAJunitTest("testUpdatePenelopesPhoneNumberStatus"));
        suite.addTest(new AdvancedJPAJunitTest("testRemoveJillWithPrivateOwnedPhoneNumbers"));
        
        suite.addTest(new AdvancedJPAJunitTest("testCreateNewEquipment"));
        suite.addTest(new AdvancedJPAJunitTest("testAddNewEquipmentToDepartment"));
        suite.addTest(new AdvancedJPAJunitTest("testRemoveDepartmentWithPrivateOwnedEquipment"));
        suite.addTest(new AdvancedJPAJunitTest("testUpdateReadOnlyEquipmentCode"));
        
        suite.addTest(new AdvancedJPAJunitTest("testNamedStoredProcedureQuery"));
        suite.addTest(new AdvancedJPAJunitTest("testNamedStoredProcedureQueryInOut"));

        return new TestSetup(suite) {
            protected void setUp() { 
                ServerSession session = JUnitTestCase.getServerSession();
                
                new AdvancedTableCreator().replaceTables(session);
                
                // The EquipmentCode class 'should' be set to read only. We want 
                // to be able to create a couple in the Employee populator, so 
                // force the read only to false. If EquipmentCode is not 
                // actually read only, don't worry, we set the original read
                // only value back on the descriptor and the error will be 
                // caught in a later test in this suite.
                ClassDescriptor descriptor = session.getDescriptor(EquipmentCode.class);
                boolean shouldBeReadOnly = descriptor.shouldBeReadOnly();
                descriptor.setShouldBeReadOnly(false);
                
                // Populate the database with our examples.
                EmployeePopulator employeePopulator = new EmployeePopulator();         
                employeePopulator.buildExamples();
                employeePopulator.persistExample(session);
                
                descriptor.setShouldBeReadOnly(shouldBeReadOnly);
            }

            protected void tearDown() {
                clearCache();
            }
        };
    }
    
    /**
     * Verifies that settings from the Employee cache annotation have been set.
     */
    public void testVerifyEmployeeCacheSettings() {
        EntityManager em = createEntityManager("default1");
        ClassDescriptor descriptor = ((EntityManagerImpl) em).getServerSession().getDescriptorForAlias("Employee");
            
        if (descriptor == null) {
            fail("A descriptor for the Employee alias was not found in the default1 PU.");
        } else {            
            assertTrue("Incorrect cache type() setting.", descriptor.getIdentityMapClass().equals(ClassConstants.SoftCacheWeakIdentityMap_Class));
            assertTrue("Incorrect cache size() setting.", descriptor.getIdentityMapSize() == 730);
            assertFalse("Incorrect cache isolated() setting.", descriptor.isIsolated());
            assertFalse("Incorrect cache alwaysRefresh() setting.", descriptor.shouldAlwaysRefreshCache());
            
            // The diableHits() setting gets changed in the employee customizer. 
            // Its setting is checked in the test below.
            
            CacheInvalidationPolicy policy = descriptor.getCacheInvalidationPolicy();
            assertTrue("Incorrect cache expiry() policy setting.", policy instanceof TimeToLiveCacheInvalidationPolicy);
            assertTrue("Incorrect cache expiry() setting.", ((TimeToLiveCacheInvalidationPolicy) policy).getTimeToLive() == 1000);
            
            assertTrue("Incorrect cache coordinationType() settting.", descriptor.getCacheSynchronizationType() == ClassDescriptor.INVALIDATE_CHANGED_OBJECTS);
        }
        
        em.close();
    }
    
    /**
     * Verifies that settings from the EmployeeCustomizer have been set.
     */
    public void testVerifyEmployeeCustomizerSettings() {
        EntityManager em = createEntityManager();
        
        ClassDescriptor descriptor = ((EntityManagerImpl) em).getServerSession().getDescriptorForAlias("Employee");
            
        if (descriptor == null) {
            fail("A descriptor for the Employee alias was not found.");    
        } else {
            assertFalse("Disable cache hits was true. Customizer should have made it false.", descriptor.shouldDisableCacheHits());
        }
        
        em.close();
    }
    
    /**
     * Verifies that the join-fetch annotation was read correctly.
     */
    public void testJoinFetchAnnotation() {
        ServerSession session = JUnitTestCase.getServerSession();
        ClassDescriptor descriptor = session.getDescriptor(Employee.class);
        if (((ForeignReferenceMapping)descriptor.getMappingForAttributeName("department")).getJoinFetch() != ForeignReferenceMapping.OUTER_JOIN) {
            fail("JoinFetch annotation not read correctly for Employee.department.");
        }
    }
    
    /**
     * Tests:
     * - BasicCollection mapping
     * - Serialized Basic of type EnumSet.
     * - BasicCollection that uses an Enum converter (by detection).
     */
    public void testUpdateEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findAllSQLEmployees");
            Collection<Employee> employees = query.getResultCollection();
            
            if (employees.isEmpty()) {
                fail("No Employees were found. Test requires at least one Employee to be created in the EmployeePopulator.");
            } else {
                Employee emp = employees.iterator().next();
                emp.addResponsibility(newResponsibility);
                emp.setMondayToFridayWorkWeek();
                empId = emp.getId();
                em.getTransaction().commit();
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Verifies:
     * - a BasicCollection mapping.
     * - a BasicCollection that uses an Enum converter (by detection).
     */
    public void testVerifyUpdatedEmployee() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            Employee emp = em.find(Employee.class, empId);
            
            assertNotNull("The updated employee was not found.", emp);
            
            boolean found = false;
            for (String responsibility : (Collection<String>) emp.getResponsibilities()) {
                if (responsibility.equals(newResponsibility)) {
                    found = true;
                    break;
                }
            }
            
            assertTrue("The new responsibility was not added.", found);
            assertTrue("The basic collection using enums was not persisted correctly.", emp.worksMondayToFriday());
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests:
     * - BasicMap mapping with a TypeConverter on the map value and an 
     *   ObjectTypeConverter on the map key.
     * - Basic with a custom converter
     * - Serialized Basic of type EnumSet.
     */
    public void testCreateNewBuyer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {            
            GoldBuyer buyer = new GoldBuyer();
            
            buyer.setName("Guy Pelletier");
            buyer.setGender("Made of testosterone");
            buyer.setDescription("Loves to spend");
            buyer.addVisa(visa);
            buyer.addAmex(amex);
            buyer.addDinersClub(diners);
            buyer.addMastercard(mastercard);
            buyer.setSaturdayToSundayBuyingDays();
            em.persist(buyer);
            buyerId = buyer.getId();
            em.getTransaction().commit();    
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Verifies:
     * - BasicMap mapping with a TypeConverter on the map value and an 
     *   ObjectTypeConverter on the map key.
     * - Basic with a custom converter
     * - Serialized Basic of type EnumSet.
     */
    public void testVerifyNewBuyer() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {            
            GoldBuyer buyer = em.find(GoldBuyer.class, buyerId);
            
            assertNotNull("The new buyer was not found", buyer);
            assertTrue("Gender was not persisted correctly.", buyer.isMale());
            assertTrue("Visa card did not persist correctly.", buyer.hasVisa(visa));
            assertTrue("Amex card did not persist correctly.", buyer.hasAmex(amex));
            assertTrue("Diners Club card did not persist correctly.", buyer.hasDinersClub(diners));
            assertTrue("Mastercard card did not persist correctly.", buyer.hasMastercard(mastercard));
            assertTrue("The serialized enum set was not persisted correctly.", buyer.buysSaturdayToSunday());
            
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests an OptimisticLocking policy set on Buyer.
     */
    public void testBuyerOptimisticLocking() {
        EntityManager em1 = createEntityManager();
        EntityManager em2 = createEntityManager();
        em1.getTransaction().begin();
        em2.getTransaction().begin();
        RuntimeException caughtException = null;
        try {            
            GoldBuyer buyer1 = em1.find(GoldBuyer.class, buyerId);
            GoldBuyer buyer2 = em2.find(GoldBuyer.class, buyerId);
            
            buyer1.setName("Geezer");
            buyer2.setName("Guyzer");
            // Uses field locking, so need to update version.
            buyer1.setVersion(buyer1.getVersion() + 1);
            buyer2.setVersion(buyer2.getVersion() + 1);
            
            em1.getTransaction().commit();
            em2.getTransaction().commit();
            
            em1.close();
            em2.close();
        } catch (RuntimeException e) {
            caughtException = e;
            if (em1.getTransaction().isActive()){
                em1.getTransaction().rollback();
            }
            
            if (em2.getTransaction().isActive()){
                em2.getTransaction().rollback();
            }
            
            em1.close();
            em2.close();
        }
        if (caughtException == null) {
            fail("Optimistic lock exception was not thrown.");
        } else if (!(caughtException.getCause() instanceof javax.persistence.OptimisticLockException)) {
            // Re-throw exception to ensure stacktrace appears in test result.
            throw caughtException;
        }        
    }
    
    /**
     * Tests an ObjectTypeConverter on a direct to field mapping.
     */
    public void testGiveFredASexChange() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findAllEmployeesByFirstName");
            query.setParameter("firstname", "Fred");
            Collection<Employee> employees = query.getResultCollection();
            
            if (employees.isEmpty()) {
                // Did Fred chicken out?
                fail("No employees named Fred were found. Test requires at least one Fred to be created in the EmployeePopulator.");
            } else {
                Employee fred = employees.iterator().next();    
                fred.setFemale();
                fred.setFirstName("Penelope");
                penelopeId = fred.getId();
                
                em.getTransaction().commit();
                
                // Clear cache and clear the entity manager
                clearCache();    
                em.clear();
                
                Employee penelope = em.find(Employee.class, penelopeId);
                assertTrue("Fred's sex change to Penelope didn't occur.", penelope.isFemale());
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests a BasicCollection on an entity that uses a composite primary key.
     */
    public void testUpdatePenelopesPhoneNumberStatus() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            Employee emp = em.find(Employee.class, penelopeId);
            assertNotNull("The employee with id: [" + penelopeId + "] was not found.", emp);
            
            for (PhoneNumber phoneNumber : emp.getPhoneNumbers()) {
                phoneNumber.addStatus(PhoneNumber.PhoneStatus.ACTIVE);
                phoneNumber.addStatus(PhoneNumber.PhoneStatus.ASSIGNED);
            }
            
            em.getTransaction().commit();
                
            // Clear cache and clear the entity manager
            clearCache();    
            em.clear();
            
            Employee emp2 = em.find(Employee.class, penelopeId);
            
            for (PhoneNumber phone : emp2.getPhoneNumbers()) {
                assertTrue("", phone.getStatus().contains(PhoneNumber.PhoneStatus.ACTIVE));
                assertTrue("", phone.getStatus().contains(PhoneNumber.PhoneStatus.ASSIGNED));
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests a @PrivateOwned @OneToMany mapping.
     */
    public void testRemoveJillWithPrivateOwnedPhoneNumbers() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findAllEmployeesByFirstName");
            query.setParameter("firstname", "Jill");
            Collection<Employee> employees = query.getResultCollection();
            
            if (employees.isEmpty()) {
                fail("No employees named Jill were found. Test requires at least one Jill to be created in the EmployeePopulator.");
            } else {
                Employee jill = employees.iterator().next();    
                Collection<PhoneNumber> phoneNumbers = jill.getPhoneNumbers();
                
                if (phoneNumbers.isEmpty()) {
                    fail("Jill does not have any phone numbers. Test requires that Jill have atleast one phone number created in the EmployeePopulator.");    
                }
                
                // Re-assign her managed employees and remove from her list.
                for (Employee employee : jill.getManagedEmployees()) {
                    employee.setManager(jill.getManager());
                }
                jill.getManagedEmployees().clear();
                
                int jillId = jill.getId();
                
                em.remove(jill);
                em.getTransaction().commit();
                
                assertNull("Jill herself was not removed.", em.find(Employee.class, jillId));
                
                for (PhoneNumber phoneNumber : phoneNumbers) {
                    assertNull("Jill's phone numbers were not deleted.", em.find(PhoneNumber.class, phoneNumber.buildPK()));
                }
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Creates some new equipment objects.
     */
    public void testCreateNewEquipment() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            // Persist some equipment.
            Equipment equip1 = new Equipment();
            equip1.setDescription("Toaster");
            em.persist(equip1);
            
            Equipment equip2 = new Equipment();
            equip1.setDescription("Bucket");
            em.persist(equip2);
            
            Equipment equip3 = new Equipment();
            equip1.setDescription("Broom");
            em.persist(equip3);
            
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests adding objects to a 1-M mapping that uses a map.
     */
    public void testAddNewEquipmentToDepartment() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {    
            EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findAllSQLEquipment");
            Collection<Equipment> equipment = query.getResultCollection();
            
            if (equipment.isEmpty()) {
                fail("No Equipment was found. testCreateNewEquipment should have created new equipment and should have run before this test.");
            } else {                
                Department department = new Department();
                department.setName("Department with equipment");
                
                for (Equipment e : equipment) {
                    department.addEquipment(e);
                }
                
                em.persist(department);
                deptId = department.getId();
                em.getTransaction().commit();
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests a @NamedStoredProcedureQuery.
     */
    public void testNamedStoredProcedureQuery() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            Address address1 = new Address();
        
            address1.setCity("Ottawa");
            address1.setPostalCode("K1G6P3");
            address1.setProvince("ON");
            address1.setStreet("123 Street");
            address1.setCountry("Canada");

            em.persist(address1);
            em.getTransaction().commit();
            
            Address address2 = (Address) em.createNamedQuery("SProcAddress").setParameter("ADDRESS_ID", address1.getId()).getSingleResult();
            assertTrue("Address not found using stored procedure", address2.equals(address1));
            
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests a @NamedStoredProcedureQuery.
     */
    public void testNamedStoredProcedureQueryInOut() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {
            Address address1 = new Address();
        
            address1.setCity("Ottawa");
            address1.setPostalCode("K1G6P3");
            address1.setProvince("ON");
            address1.setStreet("123 Street");
            address1.setCountry("Canada");

            em.persist(address1);
            em.getTransaction().commit();

            Address address2 = (Address)em.createNamedQuery("SProcInOut").setParameter("ADDRESS_ID", address1.getId()).getSingleResult();
        
            assertTrue("Address not found using stored procedure", (address1.getId() == address2.getId()) && (address1.getStreet().equals(address2.getStreet())));
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests a @PrivateOwned @OneToMany mapping.
     */
    public void testRemoveDepartmentWithPrivateOwnedEquipment() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {    
            Department department = em.find(Department.class, deptId);
            
            if (department == null) {
                fail("Department with id="+deptId+", was not found.");
            } else {
                Collection<Equipment> equipment = department.getEquipment().values();
                
                if (equipment.isEmpty()){
                    fail("Department with id="+deptId+", did not have any equipment.");
                } else {
                    em.remove(department);
                    em.getTransaction().commit();
                
                    assertNull("Department itself was not removed.", em.find(Department.class, deptId));
                
                    for (Equipment e : equipment) {
                        assertNull("New equipment was not deleted.", em.find(Equipment.class, e.getId()));
                    }
                }
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
    
    /**
     * Tests trying to update a read only class.
     */
    public void testUpdateReadOnlyEquipmentCode() {
        EntityManager em = createEntityManager();
        em.getTransaction().begin();
        
        try {    
            EJBQueryImpl query = (EJBQueryImpl) em.createNamedQuery("findSQLEquipmentCodeA");
            EquipmentCode equipmentCode = (EquipmentCode) query.getSingleResult();
            
            equipmentCode.setCode("Z");
            em.getTransaction().commit();
            
            // Nothing should have been written to the database. Query for 
            // EquipmentCode A again. If an exception is caught, then it was 
            // not found, therefore, updated on the db.
            try {
                query.getSingleResult();
            } catch (Exception e) {
                fail("The read only EquipmentA was modified");
            }
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            
            em.close();
            // Re-throw exception to ensure stacktrace appears in test result.
            throw e;
        }
        
        em.close();
    }
}