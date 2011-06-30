package org.dspace.submission;

/**
 * Created by IntelliJ IDEA.
 * User: gaurav
 * Date: 6/23/11
 * Time: 2:16 AM
 * To change this template use File | Settings | File Templates.
 */

import mockit.NonStrictExpectations;
import org.apache.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.submission.state.actions.SubmissionAction;
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
public class SubmissionActionTest extends AbstractUnitTest {
    /** log4j category */
    private static final Logger log = Logger.getLogger(SubmissionActionTest.class);
    private static  int DEFAULT_ID =1;

    /**
     * <OriginalClass> instance for the tests
     */
    private SubmissionAction action;

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
            action = new SubmissionAction();
            action.create(context);
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
        action = null;
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
//        assertThat("testXXXX 2", found.getBean_id(), equalTo(""));
//    }

//    @Test
//    public void testGetFirstStep() {
//        SubmissionAction first = action.getFirstStep();
//        assertThat("testGetFirstStep 0", first, notNullValue());
//        assertThat("testGetFirstStep 1", first.getBean_id(), notNullValue());
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

        SubmissionAction action = new SubmissionAction();
        action.create(context);
        action.setBean_id("test-action-2");
        //fail("Exception expected");
        SubmissionAction testaction = SubmissionAction.findByBean(context,"test-action-2");
        System.out.println(testaction.getBean_id());
        assertThat("TestCreate1",testaction.getBean_id(),equalTo("test-action-2"));
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
//        assertThat("testXXXX 2", found.getBean_id(), equalTo(""));
//    }

    @Test
       public void testGetName()
       {
           assertThat("testGetName 0",action.getBean_id(),notNullValue());
           assertThat("testGetName 1",action.getBean_id(),not(equalTo("")));
           assertThat("testGetName 2",action.getBean_id(),(equalTo("init-process")));
       }

    @Test
       public void testSetName()
       {
           String name = "new name";
           action.setBean_id(name);
           assertThat("testSetName 0",action.getBean_id(),notNullValue());
           assertThat("testSetName 1",action.getBean_id(),not(equalTo("")));
           assertThat("testSetName 2",action.getBean_id(),equalTo(name));
       }

       /**
        * Test of getSchemaID method, of class MetadataSchema.
        */
    @Test
    public void testGetID()
    {
       assertTrue("testGetSchemaID 0",action.getId()>=1);
    }


//    @Test
//    public void testGetStep() throws  Exception{
//        SubmissionAction first = process.getFirstStep();
//        SubmissionAction test = process.getStep(context,first.getId());
//        assertThat("testGetStep 0", test, notNullValue());
//        assertThat("testGetStep 1", test.getBean_id(), notNullValue());
//        assertThat("testGetStep 2", test.getBean_id(), equalTo("test-action"));
//    }



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

        int id = action.getId();
        action.delete(context);
        //SubmissionAction found = SubmissionAction.find(context, id);
        //assertThat("testDeleteAuth 0",found,nullValue());
    }
}
    


