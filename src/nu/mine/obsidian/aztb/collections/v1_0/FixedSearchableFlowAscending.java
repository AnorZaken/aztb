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

/**
 * A searchable flow* of <u>ascending</u> {@link IFlowElement IFlowElements}. 
 * <p/><i>*A flow is a "cache" of the latest &#123{@link #getCapacity()}&#125 number of
 * elements added to it. (Once it reaches full capacity continuing to add elements will always
 * replace the oldest element in the flow.) 
 * <p/>{@link #tryAdd(IFlowElement)} is O(1)<br>
 * {@link #find(Object)} is O(log2(capacity))</i>
 * 
 * @author AnorZaken
 * @version 1.0
 * 
 * @param <U> Type used as "key" in {@link IFlowElement}
 * @param <T> {@link IFlowElement}&lt{@link U}&gt
 * @see IFlowElement
 */
public final class FixedSearchableFlowAscending<U, T extends FixedSearchableFlowAscending.IFlowElement<U>>
		extends FixedFlow<T>
{
	/**
	 * 
	 * @author AnorZaken
	 * @param <U> Type used as "key"
	 * @see #getKey()
	 * @see #compareToKey(U)
	 */
	public static interface IFlowElement<U>
	{
		/**
		 * Get the key for this element.
		 * @return A non-<code>null</code> {@link U key} for this {@link IFlowElement}.
		 * @see #compareToKey(Object)
		 */
		public U getKey();
		/**
		 * Same principle as {@link Comparable#compareTo(Object)}, except compare type is different from object type.
		 * @see Comparable#compareTo(Object)
		 */
		public int compareToKey(U key);
	}
	
	// ===================
	
	/**
	 * Create a {@link FixedSearchableFlowAscending} with capacity of 128.
	 */
	public FixedSearchableFlowAscending(final Class<T> clazz) {
		super(clazz, 128);
	}
	
	/**
	 * Create a {@link FixedSearchableFlowAscending} with capacity {@code capacity} (rounded up to nearest power of 2).
	 */
	public FixedSearchableFlowAscending(final Class<T> clazz, final int capacity) {
		super(clazz, capacity);
	}
	
	// ===================
	
	/**
	 * Tries to add the next element into the flow. <i>O(1)</i><br>
	 * This will fail if the element is <code>null</code> or doesn't abide to the strictly
	 * ascending order of this flow.
	 * @param nextElement element to add
	 * @return {@code true} if element added, otherwise {@code false}
	 */
	public boolean tryAdd(final T nextElement)
	{
		if (nextElement == null)
			return false;
		if(size < CAP) {
			if(size == 0 || flow[size].compareToKey(nextElement.getKey()) < 0) {
				flow[size++] = nextElement;
				return true;
			}
		}
		else if(flow[index].compareToKey(nextElement.getKey()) < 0) {
			flow[index = ((index + 1)%CAP)] = nextElement;
			return true;
		}
		return false;
	}
	
	/**
	 * Searches the flow for an element matching {@code key}. <i>O(1)*<p/>
	 * *Time depends on the capacity of the flow (which is constant): O(log2(capacity)).</i>
	 * @param key {@link U} key of element searched for.
	 * @return The sought element or <code>null</code> if no matching element currently exists in the flow.
	 */
	public T find(final U key) //Search O(1) (constant time - depends on log2(CAP))
	{
		if(size == 0) //Edge-case: if array is empty!
			return null;
		
		final int s = size - 1;
		//Because array is power of 2 we can't split it evenly around a center element (right side has 1 more element)...
		//...so check rightmost element first (also optimization because fairly likely that we requested latest element).
		T t = flow[Math.min(index, s)];
		if(t.compareToKey(key) == 0)
			return t;
		else if(t.compareToKey(key) < 0) //optimization: requested an element larger than the largest (=latest) stored element
			return null;
		int i = index + CAP / 2;
		for(int step = CAP / 4; step > 0; step = step / 2)
		{
			t = flow[Math.min(i%CAP, s)];
			final int c = t.compareToKey(key);
			if(c == 0)
				return t;
			else if(c > 0)
				i -= step;
			else
				i += step;
		}
		t = flow[Math.min(i%CAP, s)];
		if(t.compareToKey(key) == 0)
			return t;
		
		return null; //element matching key not found
	}
	
	// ===================
	
	/**
	 * Adds the next element into the flow. <i>O(1)</i><br>
	 * If at full capacity this will replace the oldest element in the flow.
	 * @param nextElement element to add
	 * @throws IllegalArgumentException if {@code nextElement} is {@code null} or would violate strictly ascending relation
	 */
	@Override
	public void add(T nextElement)
	{
		if (nextElement == null)
			throw new IllegalArgumentException("nextElement can't be null");
		if(size < CAP) {
			if(size == 0 || flow[size - 1].compareToKey(nextElement.getKey()) < 0) {
				flow[size++] = nextElement;
				return;
			}
		}
		else if(flow[index].compareToKey(nextElement.getKey()) < 0) {
			flow[index = ((index + 1)%CAP)] = nextElement;
			return;
		}
		throw new IllegalArgumentException("nextElement not strictly ascending");
	}
	
	/**
	 * Check if this flow contains {@code element}. <i>O(1)*<p/>
	 * *Time depends on the capacity of the flow (which is constant): O(log2(capacity)).</i>
	 * @param element element to search for
	 * @return <code>true</code> if this flow contains {@code element}, otherwise <code>false</code>.
	 */
	@Override
	public boolean contains(T element) {
		return element == null ? false : (find(element.getKey()) == element);
	}
	
	/**
	 * Get element at index. <i>O(1)
	 * <br>Index 0 is the oldest element in the flow.</i>
	 * @throws IndexOutOfBoundsException if {@code index} out of bounds
	 */
	@Override
	public T get(int index)
	{
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		else
			return super.get(index);
	}
	
	/**
	 * Get element at index. <i>O(1)
	 * <br>Index 0 is the oldest element in the flow.</i>
	 * <p/><b>Note that this will throw if putting the provided element at the specified index would
	 * violate the strictly ascending relation between elements in this flow!</b>
	 * @throws IllegalArgumentException if {@code element} is {@code null}
	 * @throws IndexOutOfBoundsException if {@code index} out of bounds
	 * @throws IllegalStateException if putting {@code element} at {@code index} would violate strictly ascending relation
	 */
	@Override
	public void set(int index, T element)
	{
		if(element == null)
			throw new IllegalArgumentException("element can't be null");
		else if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		else if( (index != 0 && super.get(index - 1).compareToKey(element.getKey()) >= 0)
				|| (index != (size - 1) && super.get(index + 1).compareToKey(element.getKey()) <= 0) )
			throw new IllegalStateException("putting element at index " + index + " would violate strictly ascending relation");
		else
			super.set(index, element);
	}
}
