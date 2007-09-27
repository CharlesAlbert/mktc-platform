package org.marketcetera.photon.model;

import junit.framework.TestCase;

import org.marketcetera.photon.core.GroupIDComparator;
import org.marketcetera.photon.core.IncomingMessageHolder;
import org.marketcetera.photon.core.MessageHolder;

public class GroupIDComparatorTest extends TestCase {

	public void testGroupIDComparator() throws Exception {
		GroupIDComparator comparator = new GroupIDComparator();
		MessageHolder mha = new IncomingMessageHolder(null, "A");
		MessageHolder mhc = new IncomingMessageHolder(null, "C");
		MessageHolder mha2 = new IncomingMessageHolder(null, "A");
		MessageHolder mhnull = new IncomingMessageHolder(null, null);
		assertEquals(-2, comparator.compare(mha, mhc));
		assertEquals(0, comparator.compare(mha, mha2));
		assertEquals(2, comparator.compare(mhc, mha));
		assertEquals(1, comparator.compare(mha, null));
		assertEquals(-1, comparator.compare(null, mha));
		assertEquals(1, comparator.compare(mha, mhnull));
		assertEquals(-1, comparator.compare(mhnull, mha));
	}
}
