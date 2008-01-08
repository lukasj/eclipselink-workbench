package org.eclipse.persistence.testing.tests.jpa.sessionbean;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.rmi.PortableRemoteObject;

import junit.framework.*;

import org.eclipse.persistence.exceptions.EclipseLinkException;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.testing.framework.junit.JUnitTestCase;
import org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced.Employee;
import org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced.Address;
import org.eclipse.persistence.testing.models.jpa.fieldaccess.advanced.AdvancedTableCreator;
import org.eclipse.persistence.testing.models.jpa.sessionbean.EmployeeService;

/**
 * EJB 3 SessionBean tests.
 * Testing using EclipseLink JPA in a JEE EJB 3 SessionBean environment.
 * These tests can only be run with a server.
 */
public class SessionBeanTests extends JUnitTestCase {
    protected EmployeeService service;
    
    public SessionBeanTests() {
        super();
    }

    public SessionBeanTests(String name) {
        super(name);
    }

    public SessionBeanTests(String name, boolean shouldRunTestOnServer) {
        super(name);
        this.shouldRunTestOnServer = shouldRunTestOnServer;
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("SessionBeanTests");
        suite.addTest(new SessionBeanTests("testSetup", true));
        suite.addTest(new SessionBeanTests("testFindAll", false));
        suite.addTest(new SessionBeanTests("testFindAllServer", true));
        suite.addTest(new SessionBeanTests("testMerge", false));
        suite.addTest(new SessionBeanTests("testMergeServer", true));

        return suite;
    }
    
    /**
     * The setup is done as a test, both to record its failure, and to alow execution in the server.
     */
    public void testSetup() throws Exception {
        new AdvancedTableCreator().replaceTables(JUnitTestCase.getServerSession("sessionbean"));
        Employee bob = new Employee();
        bob.setFirstName("Bob");
        bob.setLastName("Jones");
        getEmployeeService().insert(bob);
        Employee joe = new Employee();
        bob.setFirstName("Joe");
        bob.setLastName("Smith");
        getEmployeeService().insert(joe);
    }
    
    public EmployeeService getEmployeeService() throws Exception {
        if (service == null) {
            Properties properties = new Properties();
            String url = System.getProperty("oc4j-url");
            if (url != null) {
                properties.put("java.naming.provider.url", url);
            }
            Context context = new InitialContext(properties);
    
            try {
                service = (EmployeeService) PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/EmployeeService"), EmployeeService.class);
            } catch (NameNotFoundException notFoundException) {
                try {
                    service = (EmployeeService) PortableRemoteObject.narrow(context.lookup("ejb/EmployeeService"), EmployeeService.class);
                } catch (NameNotFoundException notFoundException2) {
                    throw new Error("Both lookups failed.");
                }
            }
        }
        return service;
    }
        
    public void testFindAll() throws Exception {
        List result = getEmployeeService().findAll();
        for (Iterator iterator = result.iterator(); iterator.hasNext(); ) {
            Employee employee = (Employee)iterator.next();
            employee.getFirstName();
            employee.getLastName();
            boolean caughtError = false;
            try {
                employee.getAddress();
            } catch (ValidationException exception) {
                caughtError = true;
                if (exception.getErrorCode() != ValidationException.INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION) {
                    throw exception;
                }
            }
            if (isOnServer() && !caughtError) {
                fail("INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION error not thrown.");
            } else {
                warning("Client serialization nulls non-instantiated 1-1s.");
            }
            employee.getDepartment();
            caughtError = false;
            try {
                employee.getPhoneNumbers().size();
            } catch (ValidationException exception) {
                caughtError = true;
                if (exception.getErrorCode() != ValidationException.INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION) {
                    throw exception;
                }
            }
            if (!caughtError) {
                fail("INSTANTIATING_VALUEHOLDER_WITH_NULL_SESSION error not thrown.");
            }
        }
    }
    
    public void testFindAllServer() throws Exception {        
        testFindAll();
    }
    
    public void testMerge() throws Exception {
        Employee employee = new Employee();
        employee.setFirstName("Bob");
        employee.setLastName("Smith");
        Employee manager = new Employee();
        manager.setFirstName("Jon");
        manager.setLastName("Way");
        employee.setAddress(new Address());
        employee.getAddress().setCity("Nepean");
        employee.setManager(manager);
        
        int id = getEmployeeService().insert(employee);
        
        employee = getEmployeeService().findById(id);
        employee.setLastName("Way");
        employee.getAddress().setCity("Kanata");
        getEmployeeService().update(employee);
        
        employee = getEmployeeService().findById(id);
        if (!employee.getLastName().equals("Way")) {
            fail("Last name not updated.");
        }
        if (!employee.getAddress().getCity().equals("Kanata")) {
            fail("City not updated.");
        }
        employee = getEmployeeService().fetchById(id);
        if (employee.getManager() == null) {
            if (isOnServer()) {
                fail("Manager merged to null.");
            } else {
                warning("Merge from client serialization nulls non-instantiated 1-1s.");
            }
        }
    }
    
    public void testMergeServer() throws Exception {        
        testMerge();
    }
    
}