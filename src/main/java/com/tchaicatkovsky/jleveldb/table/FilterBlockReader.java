package com.tchaicatkovsky.jleveldb.table;

import com.tchaicatkovsky.jleveldb.FilterPolicy;
import com.tchaicatkovsky.jleveldb.util.Coding;
import com.tchaicatkovsky.jleveldb.util.DefaultSlice;
import com.tchaicatkovsky.jleveldb.util.Slice;
import com.tchaicatkovsky.jleveldb.util.Strings;

public class FilterBlockReader {

	final FilterPolicy policy;
	byte[] data;    
	int begin;	// Pointer to filter data (at block-start)
	int end;	// Pointer to filter data (at block-end)
	int num;          // Number of entries in offset array
	int baseLg;      // Encoding parameter (see kFilterBaseLg in .cc file)
	
	public void delete() {

	}
	
	// REQUIRES: "contents" and *policy must stay live while *this is live.
	public FilterBlockReader(FilterPolicy policy, Slice contents) {
		this.policy = policy;
		this.data = null;
		begin = 0;
		end = 0;
		num = 0;
		baseLg = 0;
		
		int n = contents.size();
		if (n < 5)
			return;  // 1 byte for base_lg_ and 4 for start of offset array
		
		baseLg = (contents.data()[contents.offset() + n - 1] & 0xff);
		int lastWord = Coding.decodeFixedNat32(contents.data(), contents.offset()+n-5);
		if (lastWord > n - 5) 
			return;
		
		data = contents.data();
		begin  = contents.offset();
		end  = begin + lastWord;
		num = (n - 5 - lastWord) / 4;
		
		
	}
	
	public String debugString() {
		return String.format("[DEBUG] FilterBlockReader, baseLg=%d, begin=%d, end=%d, num=%d\n", 
				baseLg, begin, end, num);
	}
	
	public boolean keyMayMatch(long blockOffset, Slice key) {
		System.out.println("[DEBUG] keyMayMatch, key="+Strings.escapeString(key));
		long index = blockOffset >> baseLg;
		
		if (index < num) {
			int start = Coding.decodeFixedNat32(data, (int)(end + index * 4));
		    int limit = Coding.decodeFixedNat32(data, (int)(end + index * 4 + 4));
		    		    
		    if (start <= limit && limit <= (end - begin)) {
		    	Slice filter = new DefaultSlice(data, begin + start, limit - start);
		    	return policy.keyMayMatch(key, filter);
		    } else if (start == limit) {
		    	// Empty filters do not match any keys
		    	return false;
		    }
		}
		return true;  // Errors are treated as potential matches
	}
}