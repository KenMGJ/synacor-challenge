package org.kenmgj;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.iterators.PermutationIterator;

/**
 * Hello world!
 *
 */
public class CoinPermutations
{

	public static void main( String[] args )
	{
		List<Integer> list = new ArrayList<Integer>();
		list.add(2);
		list.add(3);
		list.add(5);
		list.add(7);
		list.add(9);

		PermutationIterator<Integer> p = new PermutationIterator<Integer>(list);

		while(p.hasNext()) {

			List<Integer> ints = p.next();

			int value = ints.get(0) + ints.get(1) * (int) Math.pow(ints.get(2), 2) + (int) Math.pow(ints.get(3), 3) - ints.get(4);
			// System.out.println(value + " " + ints.get(0) + " " + ints.get(1) + " " + ints.get(2) + " " + ints.get(3) + " " + ints.get(4));
			if (value == 399) {
				System.out.println(ints.get(0) + " " + ints.get(1) + " " + ints.get(2) + " " + ints.get(3) + " " + ints.get(4));
			}
		}
	}
}
