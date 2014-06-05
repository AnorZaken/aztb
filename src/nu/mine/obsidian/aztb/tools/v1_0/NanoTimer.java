package nu.mine.obsidian.aztb.tools.v1_0;

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
 * Small stop-watch class that relies on {@link System#nanoTime()}.
 * @author AnorZaken
 * @version 1.0
 */
public final class NanoTimer
{
	public enum TimerState
	{
		Ready, Running, Stopped
	}
	
	private TimerState state = TimerState.Ready;
	
	private long mStartTime = 0;
	private long mStopTime = 0;
	
	private static final long ONE_SECOND_NANO = 1000000000L;
	
	public void start()
	{
		mStartTime = System.nanoTime();
		state = TimerState.Running;
	}
	
	public void stop()
	{
		if(state != TimerState.Ready)
		{
			mStopTime = System.nanoTime();
			state = TimerState.Stopped;
		}
	}
	
	public void reset()
	{
		state = TimerState.Ready;
	}
	
	public TimerState getState()
	{
		return state;
	}
	
	public long elapsedNanoSeconds_Full()
	{
		if(state == TimerState.Running)
			return System.nanoTime() - mStartTime;
		else if(state == TimerState.Stopped)
			return mStopTime - mStartTime;
		else
			return 0L;
	}
	
	public long elapsedSeconds_Full()
	{
		return elapsedNanoSeconds_Full() / ONE_SECOND_NANO;
	}
}
