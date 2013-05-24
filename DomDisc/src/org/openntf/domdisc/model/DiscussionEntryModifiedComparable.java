package org.openntf.domdisc.model;

import java.util.Comparator;

public class DiscussionEntryModifiedComparable implements Comparator<DiscussionEntry> {

	@Override
	public int compare(DiscussionEntry lhs, DiscussionEntry rhs) {
		
		String lhsDate = lhs.getModified();
		String rhsDate = rhs.getModified();
		
		if (lhsDate == null) {
			lhsDate="1970-01-01T01:01:01Z";
		}
		
		if (rhsDate == null) {
			rhsDate="1970-01-01T01:01:01Z";
		}
		
		int compareValue = lhsDate.compareTo(rhsDate);
		
		if (compareValue == 0) {
			return 0;
		} 
		
		if (compareValue > 0) {
			return -11;
		}
		
		if (compareValue < 0) {
			return 1;
		}
		
		return 0;
	}


}
