package nu.mine.obsidian.aztb.bukkit.metadata.v1_0;

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

import java.lang.ref.WeakReference;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/**
 * A slimmed down implementation of {@link MetadataValue} that holds no data.
 * <br><i>(Use this instead of a {@link FixedMetadataValue} storing <code>null</code>!)</i> 
 * @author AnorZaken
 * @version 1.0
 */
public final class EmptyMetadata implements MetadataValue
{
	private final WeakReference<Plugin> owningPlugin;
	
	public EmptyMetadata(Plugin owningPlugin)
	{
		if (owningPlugin == null)
			throw new IllegalArgumentException("owningPlugin can not be null");
		this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
	}
	
	@Override
	public boolean asBoolean() {
		return false;
	}

	@Override
	public byte asByte() {
		return 0;
	}

	@Override
	public double asDouble() {
		return 0D;
	}

	@Override
	public float asFloat() {
		return 0F;
	}

	@Override
	public int asInt() {
		return 0;
	}

	@Override
	public long asLong() {
		return 0L;
	}

	@Override
	public short asShort() {
		return 0;
	}

	@Override
	public String asString() {
		return null;
	}

	@Override
	public Plugin getOwningPlugin() {
		return owningPlugin.get();
	}

	@Override
	public void invalidate()
	{}

	@Override
	public Object value() {
		return null;
	}
}