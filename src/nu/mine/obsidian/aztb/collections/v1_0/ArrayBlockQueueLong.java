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

import java.util.Deque;
import java.util.LinkedList;

/* This was made for "long" because that was what I needed when i wrote it,
 * but class is very small so can be changed to any other type in an instant.
 * (I was storing System.nanoTime() time-stamps)
 */

/**
 * A {@link BasicQueue} implemented as a {@link LinkedList} of fixed-size arrays.
 * </p>Rationale:<ul>
 *  <li>A (pure) LinkedList has higher memory footprint (many links, boxing of each element stored)</li>
 *  <li>An Array has a growing worst-case delay when resizing (not optimal for real-time + large collections)</li>
 *  <li>This is intended as compromise between the two... (medium memory-footprint, worst-case kept in check by
 *      "<code>blockSize</code>" - see {@link #ArrayBlockQueueLong(int)})</li>
 * </ul>
 * @author AnorZaken
 * @version 1.0
 * 
 * @see BasicQueue
 */
public final class ArrayBlockQueueLong implements BasicQueue<Long>
{
	private final int blockSize;
	private final Deque<long[]> blocks;
	
	private int blockHead; // [0 to blockSize-1]
	private int blockTail; // [1 to blockSize]
	
	/**
	 * Construct an {@link ArrayBlockQueueLong} with the specified <code>blockSize</code>.
	 * @param blockSize <ul><li>Small: best worst-case performance (Recommended)</li>
	 *  <li>Medium: slightly better memory efficiency for large collections</li>
	 *  <li>Large: just bad... no benefits. (Don't do this!)</li></ul>
	 *  (Without performance testing on target hardware it's impossible to give numbers.)
	 */
	public ArrayBlockQueueLong(int blockSize)
	{
		if(blockSize <= 0)
			throw new IllegalArgumentException("blockSize must be greater than zero");
		
		this.blockSize = blockSize;
		blocks = new LinkedList<long[]>();
		
		blockHead = 0;
		blockTail = blockSize;
	}
	
	// ---------------------------------------
	
	private void addBlockAdd(Long value)
	{
		blocks.add(new long[blockSize]);
		blockTail = 0;
		blocks.peekLast()[0] = value;
		blockTail = 1;
	}
	
	private Long removeBlockPoll(int index)
	{
		blockHead = 0;
		if(blocks.size() == 1)
			blockTail = blockSize;
		return blocks.pollFirst()[index];
	}
	
	private void removeBlock()
	{
		blockHead = 0;
		if(blocks.size() <= 1)
			blockTail = blockSize;
		blocks.pollFirst();
	}
	
	// -----------------------------------------
	
	@Override
	public boolean add(Long value)
	{
		if(blockTail == blockSize)
			addBlockAdd(value);
		else
		{
			blocks.peekLast()[blockTail] = value;
			blockTail += 1;
		}
		return true;
	}

	@Override
	public Long peek()
	{
		if(blocks.isEmpty())
			return null;
		else
			return blocks.peekFirst()[blockHead];
	}

	@Override
	public Long poll()
	{
		if(blocks.isEmpty())
			return null;
		else if(blockHead == blockSize - 1)
			return removeBlockPoll(blockSize - 1);
		else if(blocks.size() == 1 && blockHead == blockTail - 1)
			return removeBlockPoll(blockTail - 1);
		else
			return blocks.peekFirst()[blockHead++];
	}
	
	@Override
	public void thrash()
	{
		if(blocks.isEmpty())
			return;
		else if(blockHead == blockSize - 1 || (blocks.size() == 1 && blockHead == blockTail - 1))
			removeBlock();
		else
			blockHead += 1;
	}

	@Override
	public void clear()
	{
		blockHead = 0;
		blockTail = blockSize;
		blocks.clear();
	}

	@Override
	public int size()
	{
		return (blocks.size()-1) * blockSize - blockHead + blockTail;
	}

	@Override
	public boolean isEmpty()
	{
		return blocks.isEmpty();
	}

}
