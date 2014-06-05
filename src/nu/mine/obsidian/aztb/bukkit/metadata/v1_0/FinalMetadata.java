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

import org.apache.commons.lang.Validate;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

/**
 * A {@link MetadataValue} implementation intended as a more powerful replacement for {@link FixedMetadataValue}.
 * <br>Most notable difference is its static data-retrieval method
 * that can optionally create the data if it wasn't already there.
 * @author AnorZaken
 * @version 1.0
 *
 * @param <E> The custom data type you want to store with this class.
 * @see GenericMetadata
 * @see #getData(Metadatable, String, Plugin, Class)
 * @see #getValue()
 */
public final class FinalMetadata<E> implements MetadataValue
{
	private final WeakReference<Plugin> owningPlugin;
	
	private final Class<?> clazz;
	private final E data;
	
	/**
	 * Creates a new {@link FinalMetadata} instance with the provided value.
	 * </p><i>Note that if <code>value</code> is <code>null</code> that the type of
	 * <code>value</code> wont be known at runtime, thus using
	 * {@link #getData(Metadatable, String, Class, boolean, Plugin)} on a {@link Metadatable}
	 * with such a {@link FinalMetadata} stored in it will accept is as
	 * valid for any type and (if key match) will return <code>null</code>.
	 * @param owningPlugin {@link Plugin} that owns this {@link MetadataValue}
	 * @param value data to store
	 * @see #getData(Metadatable, String, Class, boolean, Plugin)
	 */
	public FinalMetadata(Plugin owningPlugin, E value)
	{
		Validate.notNull(owningPlugin, "owningPlugin cannot be null");
		this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
		
		this.data = value;
		this.clazz = value == null ? null : value.getClass();
	}
	
	/**
	 * Generic get method.
	 */
	public E getValue() {
		return data;
	}

	//-------------------------------------------------
	
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
	
	// -----------------------------------------------
	
	/**
	 * Enum used to specify when {@link FinalMetadata#getData(Metadatable, String, Class, TryCreate, Plugin)}
	 * should try to create new data.
	 * @author AnorZaken
	 * @see FinalMetadata#getData(Metadatable, String, Class, TryCreate, Plugin)
	 * @see TryCreate#UnlessMetaFromPlugin
	 * @see TryCreate#UnlessNonNullFinalMeta
	 * @see TryCreate#UnlessNullFinalMeta
	 * @see TryCreate#No
	 */
	public static enum TryCreate
	{
		/** 
		 * Least restrictive. Tries to create new data unless:
		 * <ul><li>A MetadataValue belonging to the specified Plugin exists.</li></ul>
		 */
		UnlessMetaFromPlugin,
		
		/** 
		 * Somewhat restrictive. Tries to create new data unless:
		 * <ul><li>A MetadataValue belonging to the specified Plugin exists.</li>
		 * <li>A {@link FinalMetadata FinalMetadata&lt;F&gt;} with a non-<code>null</code> value exists.</li></ul>
		 */
		UnlessNonNullFinalMeta,
		
		/** 
		 * More restrictive. Tries to create new data unless:
		 * <ul><li>A MetadataValue belonging to the specified Plugin exists</li>
		 * <li>A {@link FinalMetadata FinalMetadata&lt;F&gt;} with a non-<code>null</code> value exists.</li>
		 * <li>A {@link FinalMetadata FinalMetadata&lt;?&gt;} with a <code>null</code> value exists.</li></ul>
		 */
		UnlessNullFinalMeta,
		
		/** 
		 * No.
		 */
		No,
	}
	
	/**
	 * Tries to retrieve a {@link FinalMetadata FinalMetadata&lt;F&gt;} from {@link Metadatable} with key {@code metaKey} and
	 * return the value it holds, optionally creating both.</p>
	 * 
	 * If the sought {@link FinalMetadata FinalMetadata&lt;F&gt;} doesn't exist it will try to create one, holding a new
	 * instance of the type described by {@code valueClass}, add it to the {@link Metadatable} argument and return this new
	 * [type described by {@code valueClass}]-instance.</p>
	 * 
	 * If this fails, for example because {@code valueClass} or {@link Plugin} is <code>null</code>, or if the type that
	 * {@code valueClass} describes doesn't have a public parameterless constructor, or the key {@code metaKey} is already
	 * occupied by {@link Plugin} (but cant be retrieved because the stored {@link MetadataValue} isn't a
	 * {@link FinalMetadata FinalMetadata&lt;F&gt;}) it will return <code>null</code>.</p>
	 * 
	 * If data creation is undesirable set the {@code allowCreate} argument to <code>false</code> (or let the {@code plugin}
	 * argument be <code>null</code> - however this also has other effects).</p>
	 * 
	 * The method works like this: Searching for {@link MetadataValue}s matching the key {@link String metaKey}...
	 * <ol>
	 *  <li>Can it find Metadata owned by {@code plugin}? (Requires <code>plugin != null</code>.)</li>
	 *   <ul><li> Return its value if it's a {@link FinalMetadata FinalMetadata&lt;?&gt;} - else return {@code null}.</li></ul>
	 *  <li>{@link TryCreate#UnlessMetaFromPlugin} ?</li>
	 *   <ul><li>Try to instantiate new data. Return it if successful, otherwise continue...</li></ul>
	 *  <li>Can it find a {@link FinalMetadata FinalMetadata&lt;F&gt;} with a non-<code>null</code> value?</li>
	 *   <ul><li>Return its value.</li></ul>
	 *  <li>{@link TryCreate#UnlessNonNullFinalMeta} ?</li>
	 *   <ul><li>Try to instantiate new data. Return it if successful, otherwise continue...</li></ul>
	 *  <li>Can it find a {@link FinalMetadata FinalMetadata&lt;?&gt;} with a <code>null</code> value?</li>
	 *   <ul><li>Return its value.</li></ul>
	 *  <li>{@link TryCreate#UnlessNullFinalMeta} ?</li>
	 *   <ul><li>Try to instantiate new data. Return it if successful, otherwise continue...</li></ul>
	 *  <li>Return <code>null</code>.</li>
	 * </ol>
	 * Note1: <u>This method will never overwrite existing Metadata!</u></p>
	 * 
	 * Note2: The type of {@code valueClass} doesn't have to exactly match the type of a stored value, the stored
	 * value only needs to be an instance of the type described by {@code valueClass}.
	 * 
	 * @param metadatable object that is supposed to have the requested data
	 * @param metaKey key for the {@link MetadataValue}
	 * @param valueClass class of the data we are looking for - will be used to instantiate if data doesn't exist
	 * @param tryCreate {@link TryCreate} enum used to specify if and when the method is allowed to create new Metadata
	 *  (If this is <code>null</code> it is the same as {@link TryCreate#No}.)
	 * @param plugin {@link Plugin} that will own the {@link FinalMetadata FinalMetadata&lt;F&gt;} if a new one gets
	 *  created - also if multiple {@link FinalMetadata FinalMetadata&lt;F&gt;} with the specified {@code metaKey} exists
	 *  and one of them was created by {@code plugin} that one will be used.
	 * 
	 * @return Returns the data, existing or newly created, or <code>null</code> if {@code metadatable}, {@code metaKey}
	 *  or {@code valueClass} is <code>null</code> or data creation wasn't possible for some other reason.
	 *  (Note that the stored value can also be <code>null</code>!)
	 *  
	 *  @see TryCreate
	 */
	
	public static <F> F getData(final Metadatable metadatable, final String metaKey, final Class<F> valueClass, TryCreate tryCreate, final Plugin plugin)
	{
		if(metadatable == null || metaKey == null || valueClass == null)
			return null;
		
		if(tryCreate == null)
			tryCreate = TryCreate.No;
		
		List<MetadataValue> dlist = metadatable.getMetadata(metaKey);
		if(dlist.size() == 0)
		{
			if (tryCreate == TryCreate.No)
				return null;
			else
				return getData_TryCreateData(metadatable, metaKey, valueClass, plugin);
		}
		else if(dlist.size() == 1)
		{
			final MetadataValue mdv = dlist.get(0);
			final FinalMetadata<?> fm = MetadataValue_As_FinalMetadata(mdv);
			if (fm == null) //a MetadataValue existed for that key, but it wasn't a FM<?>
			{
				if (plugin == null || tryCreate == TryCreate.No)
					return null; //data creation not possible / allowed
				else if (mdv.getOwningPlugin() == plugin)
					return null; //data creation not allowed (key already occupied!)
				else // - create data!
					return getData_TryCreateData(metadatable, metaKey, valueClass, plugin);
			}
			else if (plugin == null || tryCreate == TryCreate.No || fm.getOwningPlugin() == plugin) //data creation not possible / allowed
			{
				if (fm.clazz == null) //FM<?> existed and the stored value was null
					return null;
				else if (fm.clazz == valueClass || valueClass.isInstance(fm.data)) //left half is optimization based on most likely usage scenario
				{
					@SuppressWarnings("unchecked")
					final F data = (F) fm.data;
					return data; //FM<F> existed - returning data
				}
				else //FM<G> existed, but F != G and F is not a super-class of G!
					return null;
			}
			else if (tryCreate == TryCreate.UnlessMetaFromPlugin)
			{
				return getData_TryCreateData(metadatable, metaKey, valueClass, plugin);
			}
			else if (fm.clazz == null)
			{
				if (tryCreate == TryCreate.UnlessNullFinalMeta)
					return null;
				else
					return getData_TryCreateData(metadatable, metaKey, valueClass, plugin);
			}
			else if (fm.clazz == valueClass || valueClass.isInstance(fm.data))
			{
				@SuppressWarnings("unchecked")
				final F data = (F) fm.data;
				return data; //FM<F> existed - returning data
			}
			else //FM<G> existed, but F != G and F is not a super-class of G!
			{
				return getData_TryCreateData(metadatable, metaKey, valueClass, plugin);
			}
		}
		else //multiple MetaDataValue exists
		{
			FinalMetadata<?> fmeta = null;
			for (final MetadataValue mdv : dlist)
			{
				final FinalMetadata<?> fm = MetadataValue_As_FinalMetadata(mdv);
				if (plugin != null && mdv.getOwningPlugin() == plugin) //If meta made by plugin is found we can stop search (return)
				{
					if (fm == null || fm.clazz == null) //non-FM or null-FM<?> found
						return null;
					else if (fm.clazz == valueClass || valueClass.isInstance(fm.data)) //left half is optimization based on most likely usage scenario
					{
						@SuppressWarnings("unchecked")
						final F data = (F) fm.data;
						return data; //FM<F> owned by plugin existed - returning data
					}
					else //FM<G> owned by plugin existed, but F != G and F is not a super-class of G!
						return null;
				}
				else if (fm != null)
				{
					if (fm.clazz == null) {
						if (fmeta == null)
							fmeta = fm;
					}
					else if (fm.clazz == valueClass || valueClass.isInstance(fm.data)) //left half is optimization based on most likely usage scenario
					{
						if(plugin == null) //plugin is null so this is as good as it's going to get - we can stop searching (return)
						{
							@SuppressWarnings("unchecked")
							final F data = (F) fmeta.data;
							return data; //FM<F> existed - returning data
						}
						else
							fmeta = fm;
					}
				}
			}
			
			if (plugin == null) //create not possible
			{
				return null; //we either didn't find any FM<F> or null-FM<?> was the best we could find
				//...(otherwise we would have returned already)
			}
			else if (tryCreate == TryCreate.UnlessMetaFromPlugin) //If there was any meta from plugin we would have...
				return getData_TryCreateData(metadatable, metaKey, valueClass, plugin); //...already returned in the for-loop
			else if (fmeta == null) //no FM<F> or null-FM<?> found
			{
				if (tryCreate == TryCreate.No) //create not allowed
					return null;
				else
					return getData_TryCreateData(metadatable, metaKey, valueClass, plugin);
			}
			else if (fmeta.clazz == null) //null-FM<?> was the best we could find
			{
				if (tryCreate == TryCreate.UnlessNonNullFinalMeta) //(UnlessMetaFromPlugin has already been addressed above)
					return getData_TryCreateData(metadatable, metaKey, valueClass, plugin); //create allowed
				else
					return null; //create not allowed
			}
			else //we found FM<F>!
			{
				@SuppressWarnings("unchecked")
				final F data = (F) fmeta.data;
				return data;
			}
		}
	}
	
	private static FinalMetadata<?> MetadataValue_As_FinalMetadata(MetadataValue metadataValue) {
		return (metadataValue instanceof FinalMetadata<?>) ? (FinalMetadata<?>)metadataValue : null;
	}
	
	/**
	 * Private helper method for instantiating <code>valueClass</code>, attaching it to <code>metadatable</code>
	 * and returning the new instance.
	 * @param metadatable WARNING assumed not null!
	 * @param metaKey WARNING assumed not null!
	 * @param valueClass WARNING assumed not null!
	 * @param plugin
	 * @return The created data or null if unsuccessful.
	 */
	private static <F> F getData_TryCreateData(Metadatable metadatable, String metaKey, Class<F> valueClass, Plugin plugin)
	{
		if(plugin == null)
			return null;
		
		final FinalMetadata<F> fm;
		try
		{
			fm = new FinalMetadata<F>(plugin, valueClass.newInstance());
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
		metadatable.setMetadata(metaKey, fm);
		return fm.getValue();
	}
}