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
import java.util.List;

import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

/**
 * Generic {@link MetadataValue} class similar to {@link FixedMetadataValue} except it allows modification
 * of the stored value.
 * <br>Useful when you need a value that can change, but don't need the full power of {@link LazyMetadataValue}.
 * <br>Also has a static retrieval method to simplify use:
 * <br>&nbsp;{@link #getMetadataValue(Metadatable, String, Class, Plugin)}
 * @author AnorZaken
 * @version 1.0
 *
 * @param <T> Type stored in the Metadata
 * @see FinalMetadata
 * @see #getMetadataValue(Metadatable, String, Class, Plugin)
 * @see #setValue(Object) set(T)
 * @see #getValue()
 */
public final class GenericMetadata<T> implements MetadataValue
{
	private final WeakReference<Plugin> owningPlugin;
	private final Class<?> clazz;
	private T data;
	
	/**
	 * Creates a {@link GenericMetadata GenericMetadata&lt;T&gt;} with the stored value initialized to <code>null</code>.
	 * @param owningPlugin {@link Plugin} that owns this Metadata
	 * @param clazz Class of {@link GenericMetadata T}
	 * @throws IllegalArgumentException if any argument is <code>null</code>
	 * @see #GenericMetadata(Plugin, Object) GenericMetadata(Plugin, T)
	 */
	public GenericMetadata(Plugin owningPlugin, Class<T> clazz)
	{
		if (owningPlugin == null)
			throw new IllegalArgumentException("owningPlugin can't be null");
		if (clazz == null)
			throw new IllegalArgumentException("clazz can't be null");
		this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
		this.clazz = clazz;
		data = null;
	}
	
	/**
	 * Creates a {@link GenericMetadata GenericMetadata&lt;T&gt;} initialized to the provided {@code value}.
	 * <br><b>To store a <code>null</code> value see {@link #GenericMetadata(Plugin, Class) GenericMetadata(Plugin, Class&lt;T&gt;)}</b>
	 * @param owningPlugin {@link Plugin} that owns this {@link MetadataValue}
	 * @param value a <u>non-<code>null</code></u> value to store
	 * @throws IllegalArgumentException if any argument is <code>null</code>
	 * @see #GenericMetadata(Plugin, Class) GenericMetadata(Plugin, Class&lt;T&gt;)
	 */
	public GenericMetadata(Plugin owningPlugin, T value)
	{
		if (owningPlugin == null)
			throw new IllegalArgumentException("owningPlugin can't be null");
		if (value == null)
			throw new IllegalArgumentException("value can't be null - use the GenericMetadata(Plugin, Class<T>) constructor if you want a null value");
		this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
		clazz = value.getClass();
		data = value;
	}
	
	/**
	 * Generic set method.
	 * @param value a {@link GenericMetadata T} reference to store in the Metadata
	 */
	public void setValue(T value) {
		data = value;
	}
	
	/**
	 * Generic get method.
	 */
	public T getValue() {
		return data;
	}
	
	// -----
	
	@Override
	public Plugin getOwningPlugin() {
		return owningPlugin.get();
	}
	
	/**
	 * (Does nothing)
	 */
	@Override
	public void invalidate()
	{}
	
	/**
	 * {@inheritDoc}
	 * </p><b>HINT: use {@link #getValue()} instead</b>
	 * @see #getValue()
	 */
	@Override
	public Object value() {
		return data;
	}
	
	@Override
	public String asString() {
		return String.valueOf(data);
	}
	
	// -----
	
	@Override
	public boolean asBoolean() {
		if (data instanceof Boolean)
			return (Boolean)data;
		else if (data instanceof Number)
			return ((Number)data).doubleValue() != 0d;
		else
			return data != null;
	}
	
	@Override
	public double asDouble() {
		return (data instanceof Number) ? ((Number)data).doubleValue() : 0d;
	}
	
	@Override
	public float asFloat() {
		return (data instanceof Number) ? ((Number)data).floatValue() : 0f;
	}
	
	/**
	 * {@inheritDoc}
	 * <br>(If this is a {@link Number} outside the range of {@link Byte} it will be clamped.)
	 */
	@Override
	public byte asByte() {
		return (byte) ((data instanceof Number) ? Math.min(Byte.MAX_VALUE, Math.max(Byte.MIN_VALUE, ((Number)data).longValue())) : 0);
	}
	
	/**
	 * {@inheritDoc}
	 * <br>(If this is a {@link Number} outside the range of {@link Short} it will be clamped.)
	 */
	@Override
	public short asShort() {
		return (short) ((data instanceof Number) ? Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, ((Number)data).longValue())) : 0);
	}
	
	/**
	 * {@inheritDoc}
	 * <br>(If this is a {@link Number} outside the range of {@link Integer} it will be clamped.)
	 */
	@Override
	public int asInt() {
		return (int) ((data instanceof Number) ? Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, ((Number)data).longValue())) : 0);
	}
	
	@Override
	public long asLong() {
		return (data instanceof Number) ? ((Number)data).longValue() : 0L;
	}
	
	// -----
	
	/**
	 * Searches the {@link Metadatable} argument for a {@link GenericMetadata GenericMetadata&lt;T&gt;} with the
	 * specified {@link String metaKey} (optionally owned by {@link Plugin} argument) and returns it, or
	 * {@code null} if no such object exists (or any argument other than {@code plugin} is <code>null</code>).
	 * @param metadatable A {@link Metadatable} to search for metadata.
	 * @param metaKey     The key that was used to store the sought metadata.
	 * @param valueClass  The generic {@link Class Class&lt;T&gt;}  of the type stored inside the sought
	 *                    {@link GenericMetadata GenericMetadata&lt;T&gt;} (exact match only!)
	 * @param plugin      The {@link Plugin} that owns the sought metadata, or {@code null} to search for any 
	 *                    {@link GenericMetadata GenericMetadata&lt;T&gt;} with matching {@code metaKey} regardless of
	 *                    owning {@link Plugin}.
	 * @return A {@link GenericMetadata GenericMetadata&lt;T&gt;} or {@code null} if no {@link GenericMetadata GenericMetadata&lt;T&gt;}
	 *  with the specified {@code metaKey} (owned by {@code plugin}) was found. If {@code plugin} is {@code null} and
	 *  the {@link Metadatable} argument has multiple {@link GenericMetadata GenericMetadata&lt;T&gt;} stored
	 *  {@link #getMetadataValue(Metadatable, String, Class, Plugin)} will return the first one found.
	 *  Will also return {@code null} if any parameter other than {@code plugin} is {@code null}!
	 */
	public static <T> GenericMetadata<T> getMetadataValue(final Metadatable metadatable, final String metaKey, final Class<T> valueClass, final Plugin plugin)
	{
		if(metadatable == null || metaKey == null || valueClass == null)
			return null;
		
		List<MetadataValue> dlist = metadatable.getMetadata(metaKey);
		if (dlist.size() == 0)
		{
			return null;
		}
		else if (dlist.size() == 1)
		{
			MetadataValue mv = dlist.get(0);
			if ((plugin == null || mv.getOwningPlugin() == plugin) && (mv instanceof GenericMetadata<?>))
			{
				GenericMetadata<?> gm = (GenericMetadata<?>) mv;
				@SuppressWarnings("unchecked")
				GenericMetadata<T> gmt = gm.clazz == valueClass ? (GenericMetadata<T>)gm : null;
				return gmt;
			}
			else
				return null;	
		}
		else if (plugin == null)
		{
			for (MetadataValue mv : dlist) {
				if (mv instanceof GenericMetadata<?>)
				{
					GenericMetadata<?> gm = (GenericMetadata<?>) mv;
					@SuppressWarnings("unchecked")
					GenericMetadata<T> gmt = (gm.clazz == valueClass) ? (GenericMetadata<T>)gm : null;
					return gmt;
				}
			}
			return null;
		}
		else
		{
			for (MetadataValue mv : dlist) {
				if (mv.getOwningPlugin() == plugin && mv instanceof GenericMetadata<?>)
				{
					GenericMetadata<?> gm = (GenericMetadata<?>) mv;
					@SuppressWarnings("unchecked")
					GenericMetadata<T> gmt = gm.clazz == valueClass ? (GenericMetadata<T>)gm : null;
					return gmt;
				}
			}
			return null;
		}
	}
}
