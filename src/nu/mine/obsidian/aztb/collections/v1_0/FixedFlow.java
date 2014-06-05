package nu.mine.obsidian.aztb.collections.v1_0;

/* Copyright (C) 2014 Nicklas Damgren (aka AnorZaken)
 * 
 * This file is part of AZTB (AnorZakens ToolBox).
 *
 * AZTB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AZTB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AZTB.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.lang.reflect.Array; //Only used in constructor

/**
 * A flow* of {@link T &ltT&gt} elements. 
 * <p/><i>*A flow is a "cache" of the latest &#123{@link #getCapacity()}&#125 number of
 * elements added to it. (Once it reaches full capacity continuing to add elements will always
 * replace the oldest element in the flow.)
 * 
 * @author AnorZaken
 * @version 1.0
 * 
 * @param <T> Type stored in the flow
 */
public class FixedFlow<T>
{
	protected final T[] flow;
	protected final int CAP; //CAP must be power of 2
	protected int index; //After filling the array for the first time this is the index of the last stored entry
	protected int size = 0;
	
	// ===================
	
	/**
	 * Create a {@link FixedFlow} with capacity of 128.
	 */
	public FixedFlow(final Class<T> clazz) {
		this(clazz, 128);
	}
	
	/**
	 * Create a {@link FixedFlow} with capacity {@code capacity} (rounded up to nearest power of 2).
	 */
	public FixedFlow(final Class<T> clazz, final int capacity)
	{
		if (capacity < 0)
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
		CAP = roundUpToPowerOf2(capacity);
		index = CAP - 1;
		@SuppressWarnings("unchecked")
		T[] arr = (T[]) Array.newInstance(clazz, CAP);
		this.flow = arr;
	}
	
	// ===================
	
	protected static int roundUpToPowerOf2(int number)
	{
        int rounded = (rounded = Integer.highestOneBit(number)) == 0 ? 1
        		: (Integer.bitCount(number) > 1 ? rounded << 1 : rounded);
        return rounded;
    }
	
	// ===================
	
	/**
	 * Get the (power of 2) capacity of this flow.
	 */
	public final int getCapacity() {
		return CAP;
	}
	
	/**
	 * Get the current length of this flow. 
	 * <br><i>Note: Once the flow reaches full capacity it will never decrease in length.</i>
	 */
	public final int getLength() {
		return size;
	}
	
	/**
	 * Adds the next element into the flow. <i>O(1)</i><br>
	 * If at full capacity this will replace the oldest element in the flow.
	 * @param nextElement element to add
	 */
	public void add(final T nextElement)
	{
		if(size < CAP)
			flow[size++] = nextElement;
		else
			flow[index = ((index + 1)%CAP)] = nextElement;
	}
	
	/**
	 * Get element at index. <i>O(1)
	 * <br>Index 0 is the oldest element in the flow.<br>Bounds <b>not</b> checked!</i>
	 */
	public T get(final int index) //No bounds checking!
	{
		return flow[(this.index + index + 1) % CAP];
	}
	
	/**
	 * Set element at index. <i>O(1)
	 * <br>Index 0 is the oldest element in the flow.<br>Bounds <b>not</b> checked!</i>
	 */
	public void set(final int index, final T element) //No bounds checking!
	{
		flow[(this.index + index + 1) % CAP] = element;
	}
	
	/**
	 * Check if this flow contains {@code element}. <i>O(n)
	 * <br>(Searches from newest to oldest element.)</i>
	 * @param element element to search for
	 * @return <code>true</code> if this flow contains {@code element}, otherwise <code>false</code>.
	 */
	public boolean contains(final T element)
	{
		for(int i = size; i >= 0; --i)
			if(flow[i] == element)
				return true;
		return false;
	}
}
