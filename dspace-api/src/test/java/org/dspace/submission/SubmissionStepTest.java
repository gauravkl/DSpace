package org.dspace.submission;

import mockit.NonStrictExpectations;
import org.apache.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.submission.state.SubmissionProcess;
import org.dspace.submission.state.SubmissionStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: gaurav
 * Date: 6/14/11
 * Time: 12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubmissionStepTest extends AbstractUnitTest {
    /** log4j category */
    private static final Logger log = Logger.getLogger(SubmissionStepTest.class);
    private static  int DEFAULT_ID =1;

    /**
     * <OriginalClass> instance for the tests
     */
    private SubmissionStep step;

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init()
    {
        super.init();
        try
        {
            //we have to create a new community in the database
            context.turnOffAuthorisationSystem();
            Map<Integer, Integer> outcomes = new HashMap<Integer, Integer>();
            outcomes.put(0,123);
            Role r = new Role("gauravinit","test",false,Role.Scope.COLLECTION);
            step = new SubmissionStep("init-step",r,null,null,outcomes);
            step.create(context);
            //we need to commit the changes so we don't block the table for testing
            context.restoreAuthSystemState();
            context.commit();
        }
        catch (AuthorizeException ex)
        {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init");
        }
        catch (SQLException ex)
        {
            log.error("SQL Error in init", ex);
             fail("SQL Error in init");
        }
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy()
    {
        step = null;
        super.destroy();
    }

    /**
     * Test of XXXX method, of class <OriginalClass>
     */
//    @Test
//    public void testXXXX() throws Exception
//    {
//        int id = c.getID();
//        <OriginalClass> found =  <OriginalClass>.find(context, id);
//        assertThat("testXXXX 0", found, notNullValue());
//        assertThat("testXXXX 1", found.getID(), equalTo(id));
//        assertThat("testXXXX 2", found.getName(), equalTo(""));
//    }

//    @Test
//    public void testGetFirstStep() {
//        SubmissionStep first = step.getFirstStep();
//        assertThat("testGetFirstStep 0", first, notNullValue());
//        assertThat("testGetFirstStep 1", first.getName(), notNullValue());
//        assertThat("testGetFirstStep 2", String.valueOf(first.getId()), equalTo(""));
//    }

    @Test(expected=SQLException.class)
    public void testCreate() throws Exception{
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.isAdmin(context); result = true;
            }
        };

        SubmissionStep step = new SubmissionStep();
        Map<Integer, Integer> outcomes = new HashMap<Integer, Integer>();
        outcomes.put(0,123);
        Role r = new Role("gaurav","test",false,Role.Scope.COLLECTION);
        SubmissionStep s = new SubmissionStep("test-step-2",r,null,null,outcomes);
        s.create(context);
        s.setName("test-step-2");
        //fail("Exception expected");
        SubmissionStep teststep = SubmissionStep.findByName(context,"test-step-2");
        System.out.println(teststep.getName());
        assertThat("TestCreate1",teststep.getName(),equalTo("test-step-2"));
    }

    /**
     * Test of XXXX method, of class <OriginalClass>
     */
//    @Test
//    public void testXXXX() throws Exception
//    {
//        int id = c.getID();
//        <OriginalClass> found =  <OriginalClass>.find(context, id);
//        assertThat("testXXXX 0", found, notNullValue());
//        assertThat("testXXXX 1", found.getID(), equalTo(id));
//        assertThat("testXXXX 2", found.getName(), equalTo(""));
//    }

    @Test
       public void testGetName()
       {
           assertThat("testGetName 0",step.getName(),notNullValue());
           assertThat("testGetName 1",step.getName(),not(equalTo("")));
           assertThat("testGetName 2",step.getName(),(equalTo("init-process")));
       }

    @Test
       public void testSetName()
       {
           String name = "new name";
           step.setName(name);
           assertThat("testSetName 0",step.getName(),notNullValue());
           assertThat("testSetName 1",step.getName(),not(equalTo("")));
           assertThat("testSetName 2",step.getName(),equalTo(name));
       }

       /**
        * Test of getSchemaID method, of class MetadataSchema.
        */
    @Test
    public void testGetID()
    {
       assertTrue("testGetSchemaID 0",step.getId()>=1);
    }


//    @Test
//    public void testGetStep() throws  Exception{
//        SubmissionStep first = process.getFirstStep();
//        SubmissionStep test = process.getStep(context,first.getId());
//        assertThat("testGetStep 0", test, notNullValue());
//        assertThat("testGetStep 1", test.getName(), notNullValue());
//        assertThat("testGetStep 2", test.getName(), equalTo("test-step"));
//    }

    /**
     * Test of findAll method, of class MetadataSchema.
     */
    @Test
    public void testFindAll() throws Exception
    {
        SubmissionStep[] found = SubmissionStep.findAll(context);
        assertThat("testFindAll 0",found, notNullValue());
        assertTrue("testFindAll 1",found.length >= 1);

        boolean added = false;
        for(SubmissionStep p: found)
        {
            if(p.getName().equals(step.getName()))
            {
                added = true;
            }
        }
        assertTrue("testFindAll 2",added);
    }

    @Test
    public void testFind() throws Exception
    {
        int id = 1;
        SubmissionStep found =  SubmissionStep.find(context, id);
        assertThat("testItemFind 0", found, notNullValue());
        assertThat("testItemFind 1", found.getId(), equalTo(id));
        assertThat("testItemFind 2", found.getName(), equalTo("init-step"));
    }

    @Test(expected=AuthorizeException.class)
    public void testDelete() throws Exception
    {
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.authorizeAction((Context) any, (Item) any,
                        Constants.REMOVE, true); result = null;
            }
        };

        int id = step.getId();
        step.delete(context);
        SubmissionStep found = SubmissionStep.find(context, id);
        assertThat("testDeleteAuth 0",found,nullValue());
    }
}
    


