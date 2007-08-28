/*******************************************************************************
 * Copyright (c) 1998, 2007 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0, which accompanies this distribution
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  


 
package org.eclipse.persistence.testing.tests.ejb.ejbqltesting;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.Vector;
import javax.persistence.Query;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;


import org.eclipse.persistence.queries.ReportQuery;

import org.eclipse.persistence.testing.models.jpa.advanced.Address;
import org.eclipse.persistence.testing.models.jpa.advanced.Employee;
import org.eclipse.persistence.testing.models.jpa.advanced.EmployeePopulator;
import org.eclipse.persistence.testing.models.jpa.advanced.AdvancedTableCreator;
import org.eclipse.persistence.testing.models.jpa.relationships.Customer;
import org.eclipse.persistence.testing.models.jpa.relationships.RelationshipsExamples;
import org.eclipse.persistence.testing.models.jpa.relationships.RelationshipsTableManager;
import org.eclipse.persistence.testing.models.jpa.advanced.compositepk.Cubicle;
import org.eclipse.persistence.testing.models.jpa.advanced.compositepk.Scientist;

import org.eclipse.persistence.testing.framework.junit.JUnitTestCase;
import junit.extensions.TestSetup;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.testing.models.jpa.advanced.compositepk.CompositePKTableCreator;

/**
 * <p>
 * <b>Purpose</b>: Test complex aggregate EJBQL functionality.
 * <p>
 * <b>Description</b>: This class creates a test suite, initializes the database
 * and adds tests to the suite.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Run tests for complex aggregate EJBQL functionality
 * </ul>
 * @see org.eclipse.persistence.testing.models.jpa.advanced.EmployeePopulator
 * @see JUnitDomainObjectComparer
 */
 
//This test suite demonstrates the bug 4616218, waiting for bug fix
public class JUnitJPQLComplexAggregateTestSuite extends JUnitTestCase
{
    static JUnitDomainObjectComparer comparer;        //the global comparer object used in all tests
  
    public JUnitJPQLComplexAggregateTestSuite()
    {
        super();
    }
  
    public JUnitJPQLComplexAggregateTestSuite(String name)
    {
        super(name);
    }
  
    //This method is run at the end of EVERY test case method
    public void tearDown()
    {
        clearCache();
    }
  
    //This suite contains all tests contained in this class
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.setName("JUnitJPQLComplexAggregateTestSuite");
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexAVGTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountDistinctWithGroupByAndHavingTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountDistinctWithGroupByTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountDistinctWithGroupByTest2"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexHavingWithAggregate"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountWithGroupByTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexDistinctCountTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexMaxTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexMinTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexSumTest"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountDistinctOnBaseQueryClass"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountOnJoinedVariableSimplePK"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountOnJoinedVariableCompositePK"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountOnJoinedVariableOverManyToManySelfRefRelationship"));
        suite.addTest(new JUnitJPQLComplexAggregateTestSuite("complexCountOnJoinedCompositePK"));

        return new TestSetup(suite) {
     
            //This method is run at the end of the SUITE only
            protected void tearDown() {
                clearCache();
            }
            
            //This method is run at the start of the SUITE only
            protected void setUp() {
                clearCache();
                
                //get session to start setup
                DatabaseSession session = JUnitTestCase.getServerSession();
                
                //create a new EmployeePopulator
                EmployeePopulator employeePopulator = new EmployeePopulator();
                
                new AdvancedTableCreator().replaceTables(session);
                new CompositePKTableCreator().replaceTables(session);

                RelationshipsExamples relationshipExamples = new RelationshipsExamples();
                new RelationshipsTableManager().replaceTables(session);
                
                //initialize the global comparer object
                comparer = new JUnitDomainObjectComparer();
                
                //set the session for the comparer to use
                comparer.setSession((AbstractSession)session.getActiveSession());              
                
                //Populate the tables
                employeePopulator.buildExamples();
                
                //Persist the examples in the database
                employeePopulator.persistExample(session);  

                //populate the relationships model and persist as well
                relationshipExamples.buildExamples(session);
            }     
        };    
  }
  
    public void complexAVGTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        
        Expression exp = expbldr.get("lastName").equal("Smith");
        
        rq.setReferenceClass(Employee.class);
        rq.setSelectionCriteria(exp);
        rq.returnSingleAttribute();
        rq.dontRetrievePrimaryKeys();
        rq.useDistinct();
        rq.addAverage("salary", Double.class);
        
        String ejbqlString = "SELECT AVG(DISTINCT emp.salary) FROM Employee emp WHERE emp.lastName = \"Smith\"";      
        
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Double expectedResult = (Double)expectedResultVector.get(0);
        
        clearCache();
        
        Double result = (Double) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex AVG test failed", expectedResult, result);
    }
    
    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * with Count and group by
     */
    public void complexCountDistinctWithGroupByAndHavingTest()
    {
        String havingFilterString = "Toronto";
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
        //Need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT",exp.distinct(),Long.class );
        rq.addGrouping(exp2);
        rq.setHavingExpression(exp2.equal(havingFilterString));
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);
        
        String ejbqlString3 = "SELECT a.city, COUNT( DISTINCT e ) FROM Address a JOIN a.employees e GROUP BY a.city HAVING a.city =?1";
        Query q = em.createQuery(ejbqlString3);
        q.setParameter(1,havingFilterString);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT test failed", comparer.compareObjects(result, expectedResult));                      
    }


    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * where DISTINCT is used with Count and group by
     */
    public void complexCountDistinctWithGroupByTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
        
        //need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.  
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT",exp.distinct(),Long.class );
        rq.addGrouping(exp2);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);
        
        String ejbqlString3 = "SELECT a.city, COUNT( DISTINCT e ) FROM Address a JOIN a.employees e GROUP BY a.city";
        Query q = em.createQuery(ejbqlString3);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT(Distinct) with Group By test failed", comparer.compareObjects(result, expectedResult));                      
    }
    
    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * where DISTINCT is used with Count and group by
     */
    public void complexCountDistinctWithGroupByTest2()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
        
        //need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.  
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT1",exp, Long.class);
        rq.addCount("COUNT2",exp.get("lastName").distinct(),Long.class );
        rq.addGrouping(exp2);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);
        
        String ejbqlString3 = "SELECT a.city, COUNT( e ), COUNT( DISTINCT e.lastName ) FROM Address a JOIN a.employees e GROUP BY a.city";
        Query q = em.createQuery(ejbqlString3);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT(Distinct) with Group By test failed", comparer.compareObjects(result, expectedResult));                      
    }
    
    /**
     * Test for partial fix of GF 932. 
     */
    public void complexHavingWithAggregate()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();
        
        // Test using the project id in COUNT, GROUP BY and HAVING
        ExpressionBuilder employeeBuilder = new ExpressionBuilder(Employee.class);
        ReportQuery rq = new ReportQuery(Employee.class, employeeBuilder);
        Expression projects = employeeBuilder.anyOf("projects");
        Expression pid = projects.get("id");
        Expression count = pid.count();
        rq.addAttribute("id", pid);
        rq.addAttribute("COUNT", count, Long.class);
        rq.addGrouping(pid);
        rq.setHavingExpression(count.greaterThan(1));
        rq.setShouldReturnWithoutReportQueryResult(true);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);

        String jpql = 
            "SELECT p.id, COUNT(p.id) FROM Employee e JOIN e.projects p " + 
            "GROUP BY p.id HAVING COUNT(p.id)>1";
        List result = em.createQuery(jpql).getResultList();

        Assert.assertTrue("Complex HAVING with aggregate function failed", 
                          comparer.compareObjects(result, expectedResult));   

        // Test using the project itself in COUNT, GROUP BY and HAVING
        employeeBuilder = new ExpressionBuilder(Employee.class);
        rq = new ReportQuery(Employee.class, employeeBuilder);
        projects = employeeBuilder.anyOf("projects");
        count = projects.count();
        rq.addAttribute("projects", projects);
        rq.addAttribute("COUNT", count, Long.class);
        rq.addGrouping(projects);
        rq.setHavingExpression(count.greaterThan(1));
        rq.setShouldReturnWithoutReportQueryResult(true);
        expectedResult = (Vector) em.getActiveSession().executeQuery(rq);

        jpql = 
            "SELECT p, COUNT(p) FROM Employee e JOIN e.projects p " + 
            "GROUP BY p HAVING COUNT(p)>1";
        result = em.createQuery(jpql).getResultList();

        Assert.assertTrue("Complex HAVING with aggregate function failed", 
                          comparer.compareObjects(result, expectedResult));
    }
    
    public void complexCountTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        
        Expression exp = expbldr.get("lastName").equal("Smith");
        
        rq.setReferenceClass(Employee.class);
        rq.setSelectionCriteria(exp);
        rq.returnSingleAttribute();
        rq.dontRetrievePrimaryKeys();
        rq.addCount("COUNT", expbldr, Long.class);
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Long expectedResult = (Long) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT COUNT(emp) FROM Employee emp WHERE emp.lastName = \"Smith\"";    
        Long result = (Long) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex COUNT test failed", expectedResult, result);
    }
    
    /*
     * test for gf675, using count, group by and having fails.  This test is specific for a a use case
     * with Count and group by
     */
    public void complexCountWithGroupByTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
        //Need to set the class in the expressionbuilder, as the Count(Distinct) will cause the 
        // query to change and be built around the Employee class instead of the Address class.  
        ExpressionBuilder expbldr = new ExpressionBuilder(Address.class);
            
        ReportQuery rq = new ReportQuery(Address.class, expbldr);
        Expression exp = expbldr.anyOf("employees");

        Expression exp2 = expbldr.get("city");
        rq.addAttribute("city", exp2);
        rq.addCount("COUNT",exp.distinct(),Long.class );
        rq.addGrouping(exp2);
        Vector expectedResult = (Vector) em.getActiveSession().executeQuery(rq);

        String ejbqlString3 = "SELECT a.city, COUNT( DISTINCT e ) FROM Address a JOIN a.employees e GROUP BY a.city";
        Query q = em.createQuery(ejbqlString3);
        List result = (List) q.getResultList();
        
        Assert.assertTrue("Complex COUNT with Group By test failed", comparer.compareObjects(result, expectedResult));                      
    }
    
    public void complexDistinctCountTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);

        Expression exp = expbldr.get("lastName").equal("Smith");
   
        rq.setReferenceClass(Employee.class);
        rq.setSelectionCriteria(exp);
        rq.useDistinct();
        rq.returnSingleAttribute();
        rq.dontRetrievePrimaryKeys();
        rq.addCount("COUNT", expbldr.get("lastName").distinct(), Long.class);
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Long expectedResult = (Long) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT COUNT(DISTINCT emp.lastName) FROM Employee emp WHERE emp.lastName = \"Smith\"";
        Long result = (Long) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex DISTINCT COUNT test failed", expectedResult, result);
    }
    
    public void complexMaxTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        rq.setReferenceClass(Employee.class);
        rq.returnSingleAttribute();     
        rq.dontRetrievePrimaryKeys();
        rq.addAttribute("salary", expbldr.get("salary").distinct().maximum());
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Number expectedResult = (Number) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT MAX(DISTINCT emp.salary) FROM Employee emp";
        Number result = (Number) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex MAX test failed", expectedResult, result);
    }
    
    public void complexMinTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        rq.setReferenceClass(Employee.class);
        rq.returnSingleAttribute();     
        rq.dontRetrievePrimaryKeys();
        rq.addAttribute("salary", expbldr.get("salary").distinct().minimum());
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Number expectedResult = (Number) expectedResultVector.get(0);

        String ejbqlString = "SELECT MIN(DISTINCT emp.salary) FROM Employee emp";
        Number result = (Number) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex MIN test failed", expectedResult, result);  
        
    }
    
    public void complexSumTest()
    {
        org.eclipse.persistence.jpa.EntityManager em = (org.eclipse.persistence.jpa.EntityManager) createEntityManager();                  
               
        ExpressionBuilder expbldr = new ExpressionBuilder();
            
        ReportQuery rq = new ReportQuery(Employee.class, expbldr);
        rq.setReferenceClass(Employee.class);
        rq.returnSingleAttribute();     
        rq.dontRetrievePrimaryKeys();
        rq.addAttribute("salary", expbldr.get("salary").distinct().sum(), Long.class);
        Vector expectedResultVector = (Vector) em.getActiveSession().executeQuery(rq);
        Long expectedResult = (Long) expectedResultVector.get(0);
        
        String ejbqlString = "SELECT SUM(DISTINCT emp.salary) FROM Employee emp";
        Long result = (Long) em.createQuery(ejbqlString).getSingleResult();
 
        Assert.assertEquals("Complex SUMtest failed", expectedResult, result);
        
    }

    /**
     * Test case glassfish issue 2725: 
     */
    public void complexCountDistinctOnBaseQueryClass()
    {
        org.eclipse.persistence.jpa.EntityManager em = 
            (org.eclipse.persistence.jpa.EntityManager) createEntityManager();
        
        Long expectedResult = Long.valueOf(em.getActiveSession().readAllObjects(Employee.class).size());
        
        String jpql = "SELECT COUNT(DISTINCT e) FROM Employee e";
        Query q = em.createQuery(jpql);
        Long result = (Long) q.getSingleResult();

        Assert.assertEquals("Complex COUNT DISTINCT on base query class ", expectedResult, result);
    }
    
    /**
     * Test case glassfish issue 2497: 
     */
    public void complexCountOnJoinedVariableSimplePK()
    {
        org.eclipse.persistence.jpa.EntityManager em = 
            (org.eclipse.persistence.jpa.EntityManager) createEntityManager();

        // Need to create the expected result manually, because using the
        // TopLink query API would run into the same issue 2497.
        List expectedResult = Arrays.asList(new Long[] { Long.valueOf(1), Long.valueOf(0), 
                                                         Long.valueOf(0), Long.valueOf(1) });
        Collections.sort(expectedResult);

        String jpql = "SELECT COUNT(o) FROM Customer c LEFT JOIN c.orders o GROUP BY c.name";
        Query q = em.createQuery(jpql);
        List result = (List) q.getResultList();
        Collections.sort(result);

        Assert.assertEquals("Complex COUNT on joined variable simple PK", expectedResult, result);

        jpql = "SELECT COUNT(DISTINCT o) FROM Customer c LEFT JOIN c.orders o GROUP BY c.name";
        q = em.createQuery(jpql);
        result = (List) q.getResultList();
        Collections.sort(result);

        Assert.assertEquals("Complex COUNT DISTINCT on joined variable simple PK", expectedResult, result);
    }

    /**
     * Test case glassfish issue 2497: 
     */
    public void complexCountOnJoinedVariableCompositePK()
    {
        org.eclipse.persistence.jpa.EntityManager em = 
            (org.eclipse.persistence.jpa.EntityManager) createEntityManager();

        // Need to create the expected result manually, because using the
        // TopLink query API would run into the same issue 2497.
        List expectedResult = Arrays.asList(new Long[] { Long.valueOf(2), Long.valueOf(5), Long.valueOf(3) });
        Collections.sort(expectedResult);

        String jpql = "SELECT COUNT(p) FROM Employee e LEFT JOIN e.phoneNumbers p WHERE e.lastName LIKE 'S%' GROUP BY e.lastName";
        Query q = em.createQuery(jpql);
        List result = (List) q.getResultList();
        Collections.sort(result);

        Assert.assertEquals("Complex COUNT on outer joined variable composite PK", expectedResult, result);

        // COUNT DISTINCT with inner join
        jpql = "SELECT COUNT(DISTINCT p) FROM Employee e JOIN e.phoneNumbers p WHERE e.lastName LIKE 'S%' GROUP BY e.lastName";
        q = em.createQuery(jpql);
        result = (List) q.getResultList();
        Collections.sort(result);
  
        Assert.assertEquals("Complex DISTINCT COUNT on inner joined variable composite PK", expectedResult, result);
}

    /**
     * Test case bug 6155093: 
     */
    public void complexCountOnJoinedCompositePK()
    {
        org.eclipse.persistence.jpa.EntityManager em = 
            (org.eclipse.persistence.jpa.EntityManager) createEntityManager();
        try{
            em.getTransaction().begin();
                Scientist s = new Scientist();
                s.setFirstName("John");
                s.setLastName("Doe");
                Cubicle c = new Cubicle();
                c.setCode("G");
        
                c.setScientist(s);
                s.setCubicle(c);
                em.persist(c);
                em.persist(s);
            em.flush();

            // Need to create the expected result manually, because using the
            // TopLink query API would run into the same issue 6155093.
            List expectedResult = Arrays.asList(new Long[] { Long.valueOf(1) });
            Collections.sort(expectedResult);
    
            // COUNT DISTINCT with inner join
            String jpql = "SELECT COUNT(DISTINCT p) FROM Scientist e JOIN e.cubicle p WHERE e.lastName LIKE 'D%'";
            Query q = em.createQuery(jpql);
            List result = (List) q.getResultList();
            Collections.sort(result);
        
            Assert.assertEquals("Complex COUNT on joined variable composite PK", expectedResult, result);
        }finally{
            em.getTransaction().rollback();
        }
}

    /**
     * Test case glassfish issue 2440: 
     * On derby a JPQL query including a LEFT JOIN on a ManyToMany
     * relationship field of the same class (self-referencing relationship)
     * runs into a NPE in SQLSelectStatement.appendFromClauseForOuterJoin.
     */
    public void complexCountOnJoinedVariableOverManyToManySelfRefRelationship()
    {
        org.eclipse.persistence.jpa.EntityManager em = 
            (org.eclipse.persistence.jpa.EntityManager) createEntityManager();

        Long zero = Long.valueOf(0);
        List expectedResult = Arrays.asList(new Long[] { zero, zero, zero, zero });

        String jpql = "SELECT COUNT(cc) FROM Customer c LEFT JOIN c.cCustomers cc GROUP BY c.name";
        Query q = em.createQuery(jpql);
        List result = (List) q.getResultList();

        Assert.assertEquals("Complex COUNT on joined variable over ManyToMany self refrenceing relationship failed", 
                            expectedResult, result);
    }
    
    public static void main(String[] args)
    {
         junit.swingui.TestRunner.main(args);
    }
}