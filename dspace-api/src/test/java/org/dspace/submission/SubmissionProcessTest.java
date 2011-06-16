package org.dspace.submission;

import mockit.NonStrictExpectations;
import org.apache.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.submission.state.SubmissionProcess;
import org.dspace.submission.state.SubmissionStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
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


            //we need to commit the changes so we don't block the table for testing
            context.restoreAuthSystemState();
            context.commit();
        }
//        catch (AuthorizeException ex)
//        {
//            log.error("Authorization Error in init", ex);
//            fail("Authorization Error in init");
//        }
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

//    @Test
//    public void testGetFirstStep() {
//        SubmissionStep first = process.getFirstStep();
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

        SubmissionProcess process = new SubmissionProcess();
        Map<Integer, String> outcomes = new HashMap<Integer, String>();
        outcomes.put(0,"123");
        Role r = new Role("gaurav","test",false,Role.Scope.COLLECTION);
        SubmissionStep first = new SubmissionStep("name",r,null,null,outcomes);
        process.setFirstStep(first);
        process.setName("test-process");
        process.create(context);
        fail("Exception expected");
        SubmissionProcess testprocess = SubmissionProcess.findByName(context,"test-process");
        System.out.println(testprocess.getName());
        assertThat("TestCreate1",testprocess.getName(),equalTo("test-process"));
    }
}
