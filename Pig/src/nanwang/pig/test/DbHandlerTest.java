package nanwang.pig.test;

import nanwang.pig.entity.DbHandler;
import nanwang.pig.entity.Job;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbHandlerTest {
	
	private DbHandler dbHandler = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		dbHandler = DbHandler.getInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Job job = new Job();
		job.setId(1);
		job.setOperation("This is the DbHandler test!");
		job.setUuid("Test uuid");
		
		dbHandler.insert(job);
		DbHandler.close();
	}
}
