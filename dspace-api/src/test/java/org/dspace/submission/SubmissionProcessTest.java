package org.dspace.submission;

import mockit.NonStrictExpectations;
import org.apache.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
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
public class SubmissionProcessTest extends AbstractUnitTest {
    /** log4j category */
    private static final Logger log = Logger.getLogger(SubmissionProcessTest.class);

    /**
     * <OriginalClass> instance for the tests
     */
    private SubmissionProcess process;

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
            process = new SubmissionProcess();
            Map<Integer, Integer> outcomes = new HashMap<Integer, Integer>();
            outcomes.put(0,123);
            Role r = new Role("gauravinit","test",false,Role.Scope.COLLECTION);
            SubmissionStep first = new SubmissionStep("test-step",r,null,null,outcomes);
            process.setFirstStep(first);
            process.setName("init-process");
            process.create(context);

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
        process = null;
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

    @Test
       public void testGetName()
       {
           assertThat("testGetName 0",process.getName(),notNullValue());
           assertThat("testGetName 1",process.getName(),not(equalTo("")));
           assertThat("testGetName 2",process.getName(),(equalTo("init-process")));
       }

    @Test
       public void testSetName()
       {
           String name = "new name";
           process.setName(name);
           assertThat("testSetName 0",process.getName(),notNullValue());
           assertThat("testSetName 1",process.getName(),not(equalTo("")));
           assertThat("testSetName 2",process.getName(),equalTo(name));
       }

       /**
        * Test of getSchemaID method, of class MetadataSchema.
        */
    @Test
    public void testGetID()
    {
       assertTrue("testGetSchemaID 0",process.getID()>=1);
    }

    @Test
    public void testGetFirstStep() {
        SubmissionStep first = process.getFirstStep();
        assertThat("testGetFirstStep 0", first, notNullValue());
        assertThat("testGetFirstStep 1", first.getName(), notNullValue());
        //assertThat("testGetFirstStep 2", String.valueOf(first.getId()), equalTo(""));
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
        SubmissionProcess[] found = SubmissionProcess.findAll(context);
        assertThat("testFindAll 0",found, notNullValue());
        assertTrue("testFindAll 1",found.length >= 1);

        boolean added = false;
        for(SubmissionProcess p: found)
        {
            if(p.getName().equals(process.getName()))
            {
                added = true;
            }
        }
        assertTrue("testFindAll 2",added);
    }

    @Test
    public void testCreate() throws Exception{
        new NonStrictExpectations()
        {
            AuthorizeManager authManager;
            {
                AuthorizeManager.isAdmin(context); result = true;
            }
        };

        SubmissionProcess process = new SubmissionProcess();
        Map<Integer, Integer> outcomes = new HashMap<Integer, Integer>();
        outcomes.put(0,123);
        Role r = new Role("gaurav","test",false,Role.Scope.COLLECTION);
        SubmissionStep first = new SubmissionStep("name",r,null,null,outcomes);
        process.setFirstStep(first);
        process.setName("test-process");
        process.create(context);
        //fail("Exception expected");
        SubmissionProcess testprocess = SubmissionProcess.findByName(context,"test-process");
        System.out.println(testprocess.getName());
          assertThat("testItemFind 0", testprocess, notNullValue());
        assertThat("TestCreate1",testprocess.getName(),equalTo("test-process"));
    }
    @Test
    public void testFind() throws Exception
    {
        int id = 1;
        SubmissionProcess found =  SubmissionProcess.find(context, id);
        assertThat("testItemFind 0", found, notNullValue());
        assertThat("testItemFind 1", found.getID(), equalTo(id));
        assertThat("testItemFind 2", found.getName(), equalTo("init-process"));
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

        int id = process.getID();
        process.delete(context);
        SubmissionProcess found = SubmissionProcess.find(context, id);
        assertThat("testDeleteAuth 0",found,nullValue());
    }

}
