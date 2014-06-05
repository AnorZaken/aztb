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
 * A basic queue-like interface
 * @author AnorZaken
 * @version 1.0
 *
 * @param <E> Type stored in the BasicQueue
 * @see ArrayBlockQueueLong
 */
public interface BasicQueue<E>
{
	public boolean add(E e);
	public E peek();
	public E poll();
	
	/**
	 * Removes the top element of the queue without retrieving it.
	 * </p>Usage example:
	 * <ol>
	 *  <li>{@link #peek()}</li>
	 *  <li>inspect element - element no longer desirable?</li>
	 *  <li>{@link #thrash()}</li>
	 * </ol>
	 */
	public void thrash();
	public void clear();
	public int size();
	public boolean isEmpty();
}
