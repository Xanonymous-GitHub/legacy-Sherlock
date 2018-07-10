import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.io.File;
import uk.ac.warwick.dcs.sherlock.services.fileSystem.filters.AcceptedFileFilter;

class AcceptedFileFilterTest {

	@Test
	void testAccept1() {
		File f = new File("fake.java");
		AcceptedFileFilter filter = new AcceptedFileFilter();
		assertTrue(filter.accept(f));
	}
	void testAccept2() {
		File f = new File("fake.txt");
		AcceptedFileFilter filter = new AcceptedFileFilter();
		assertTrue(filter.accept(f));
	}
	void testFail() {
		File f = new File("fake.somethingelse");
		AcceptedFileFilter filter = new AcceptedFileFilter();
		assertFalse(filter.accept(f));
	}
}
