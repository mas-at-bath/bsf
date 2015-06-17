// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
// 20130627 jeehanglee@gmail.com	create (duration, priority, deadline)	
//----------------------------------------------------------------------------

package jason.asSemantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class IntentionScheduler {
	
	public class PriorityComparator implements Comparator<Intention> {

		@Override
		public int compare(Intention i1, Intention i2) {
			return (int)(i2.findFirstIM().getPriority() - i1.findFirstIM().getPriority());
		}
	}
	
	public class DeadlineComparator implements Comparator<Intention> {

		@Override
		public int compare(Intention i1, Intention i2) {
			return (int)(i1.findFirstIM().getDeadline() - i2.findFirstIM().getDeadline());
		}
	}
	
	void IntentionScheduler() {
		// no-op
	}
	
	public void schedule(Queue<Intention> intentions) {
		if (intentions.size() < 2) 
			return;
		
		// copy original intentions into temporary list being capable of sorting
		List<Intention> intentionList = new ArrayList<Intention>();
		Iterator<Intention> iter = intentions.iterator();
		while (iter.hasNext()) {
			intentionList.add(iter.next());
		}
		
		// scheduling by priority and deadline
		sortByPriority(intentionList);
		sortByDeadline(intentionList);
		
		// restore the result into intention buffer
		intentions.clear();
		iter = intentionList.iterator();
		while (iter.hasNext()) {
			intentions.add(iter.next());
		}
	}
	
	private void sortByPriority(List<Intention> intentionList) {
		Comparator<Intention> comp = new PriorityComparator();
		Collections.sort(intentionList, comp);
	}
	
	private void sortByDeadline(List<Intention> intentionList) {
		List<Intention> feasible = new ArrayList<Intention>();
		long execStart = 0, ne = 0;
		long deadline = 0, duration = 0;
		
		for (Intention it : intentionList) {
			execStart = System.currentTimeMillis();
			duration = it.findFirstIM().getDuration();
			deadline = it.findFirstIM().getDeadline();
			
			if ((ne + duration - (System.currentTimeMillis() - execStart)) <= deadline) {
				feasible.add(it);
				ne += duration;
			}
		}
		
		Comparator<Intention> comp = new DeadlineComparator();
		Collections.sort(feasible, comp);
		
		intentionList.clear();
		for (Intention intention : feasible) {
			System.out.println(intention);
			intentionList.add(intention);
		}
		System.out.println("---------------------------------------------------------");
	}
}
