package nanwang.pig.test;

import nanwang.pig.entity.DbHandler;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbHandlerTest {
	
	private DbHandler dbHandler;
	
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
		dbHandler.insertJob("This is the DbHandler test!", "Test uuid");
		DbHandler.close();
	}
}
