package nu.mine.obsidian.aztb.bukkit.recipes.v1_1;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

//TODO: testing

/**
 * Tool class with static methods for comparing ingredients of Bukkit {@link Recipe}s,
 * comparing {@link ItemStack}s and adding/removing {@link Recipe}s from a {@link Server}.
 * @author AnorZaken
 * @version 1.1b
 */
public class RecipeHelper
{
	private MetaCheckerHelper checker;
	
	public RecipeHelper() { this(false); }
	
	public RecipeHelper(final boolean compareFurnaceStackSizes)
	{
		this.b_compareFurnaceStackSize = compareFurnaceStackSizes;
	}
	
	
	// -------- Constants --------
	/**
	 * {@link ItemStack} durability value {@value #WILDCARD_DURABILITY} is used by bukkit to represent an item-subtype wildcard.
	 * <br><i>(Since bukkit version: ??)</i> 
	 */
	final public static short WILDCARD_DURABILITY = (short) 32767; //TODO: Since what version of bukkit is this implemented???
	
	
	// -------- Furnace recipe stack-size settings --------
	private boolean b_compareFurnaceStackSize = false; //Added this toggle in case bukkit starts supporting this
	
	/**
	 * Set the compareFurnaceStackSizes setting. (<code>false</code> by default)
	 * <br>Setting this to <code>true</code> enables stack-size comparisons when checking similarity of {@link FurnaceRecipe}s.
	 * @param compareStackSize
	 * @see #compareFurnaceStackSizes()
	 */
	
	public void compareFurnaceStackSizes(final boolean compareStackSize) {
		b_compareFurnaceStackSize = compareStackSize;
	}
	/**
	 * Get the current setting for compareFurnaceStackSizes.
	 * @return <code>true</code> if the {@link RecipeHelper} methods are set to take {@link ItemStack} stack-size into
	 *  account when comparing {@link FurnaceRecipe}s, <code>false</code> if not.
	 * @see #compareFurnaceStackSizes(boolean)
	 */
	public boolean compareFurnaceStackSizes() {
		return b_compareFurnaceStackSize;
	}
	
	
	// -------- MetaChecker get & set --------
	
	/**
	 * Creates (or removes) an {@link IMetaChecker} used to compare {@link ItemMeta} of {@link ItemStack}s.
	 * </p>If your class already implements {@link IMetaChecker} use {@link #setMetaChecker(IMetaChecker)} instead!
	 * </p><b>Only set this if the object/class has these methods:</b><br>
	 * <ul>
	 *  <li><code>public boolean areItemMetaIdentical(ItemMeta meta1, ItemMeta meta2)</code></li>
	 *  <li><b>(OPTIONAL)</b> <code>public boolean isValidItemMeta(ItemMeta meta)</code></li>
	 * </ul>
	 * <b>See {@link #itemStacksMatch(ItemStack, ItemStack, boolean)} for a description of how these are used!</b> 
	 * </p>Note1: If both arguments are <code>null</code> any current {@link IMetaChecker} instance will be
	 * removed (if one exists).
	 * </p>Note2: If the {@link Class} implements none of these methods as instance methods the
	 * {@link Object} can be <code>null</code>.
	 * </p>Note3: If the {@link Object} isn't <code>null</code> the {@link Class} argument will be
	 * ignored. (Class inferred from {@link Object} instance).
	 * @return <code>true</code> if both arguments are <code>null</code> or a valid and accessible
	 *  <code>areItemMetaIdentical</code> method exists, otherwise <code>false</code>
	 * @see IMetaChecker
	 * @see #setMetaChecker(IMetaChecker)
	 */
	public boolean setMetaChecker(final Class<? extends Object> metaCheckerClass, final Object metaCheckerInstance)
	{
		final Class<? extends Object> clazz;
		clazz = metaCheckerInstance == null ? metaCheckerClass : metaCheckerInstance.getClass();
		return TrySetMetaChecker(null, clazz, metaCheckerInstance);
	}
	
	/**
	 * Sets (or removes) the {@link IMetaChecker} used to compare {@link ItemMeta} of {@link ItemStack}s.
	 * </p>Note1: If you want to use a class that doesn't implement the {@link IMetaChecker} interface
	 * see {@link #setMetaChecker(Class, Object)}.
	 * </p>Note2: If you want a simple customizable implementation of {@link IMetaChecker} see
	 * {@link MetaChecker}.
	 * @param metaChecker the {@link IMetaChecker} to set, or <code>null</code> to remove the current
	 *  {@link IMetaChecker} instance (thus disabling all meta-checks).
	 * @see IMetaChecker
	 * @see MetaChecker
	 * @see #setMetaChecker(Class, Object)
	 */
	public void setMetaChecker(final IMetaChecker metaChecker) {
		TrySetMetaChecker(metaChecker, null, null);
	}
	
	/**
	 * Get the {@link Class} that currently supplies {@link IMetaChecker} functionality.<br>
	 * <b>This is not guaranteed to be a {@link Class} that implements {@link IMetaChecker}!</b>
	 * @return <code>null</code> if there currently doesn't exist one (meta-checking disabled).
	 * @see #setMetaChecker(Class, Object)
	 * @see #setMetaChecker(IMetaChecker)
	 */
	public Class<? extends Object> getMetaCheckerClass() {
		final MetaCheckerHelper checker = this.checker; //Localize the reference (tsafety)
		return checker == null ? null : checker.getMetaCheckerClass();
	}
	
	/**
	 * Get the instance Object that currently supplies {@link IMetaChecker} functionality.<br>
	 * <b>This is not guaranteed to be an instance of {@link IMetaChecker}!</b>
	 * @return <code>null</code> if there currently doesn't exist one (meta-checking disabled
	 *  <u><b>OR</b></u> meta-checking provided via static methods).
	 * @see #setMetaChecker(Class, Object)
	 * @see #setMetaChecker(IMetaChecker)
	 * @see #getMetaChecker()
	 */
	public Object getMetaCheckerInstance() {
		final MetaCheckerHelper checker = this.checker; //Localize the reference (tsafety)
		return checker == null ? null : checker.getMetaCheckerRaw();
	}
	
	/**
	 * Get the current {@link IMetaChecker} instance.
	 * </p>Note: This can be a wrapper that implements {@link IMetaChecker} functionality using
	 * method invocation (reflection) on an class/object that doesn't implement the {@link IMetaChecker}
	 * interface itself. (Stack-traces might get printed if invocation fails.)
	 * @return the current {@link IMetaChecker} instance, or <code>null</code> if there currently
	 * doesn't exist one (meta-checking disabled).
	 * @see #setMetaChecker(Class, Object)
	 * @see #setMetaChecker(IMetaChecker)
	 * @see IMetaChecker
	 */
	public IMetaChecker getMetaChecker() {
		final MetaCheckerHelper checker = this.checker; //Localize the reference (tsafety)
		return checker == null ? null : checker.getMetaChecker();
	}
	
	// -------- Higher abstraction methods -------- 
	
	/**
	 * Checks if trying to add a {@link Recipe} to a {@link Server} would silently fail due to collision with any existing
	 * {@link Recipe}s and optionally removes the existing {@link Recipe}s it collides with.
	 * </p>Note1: Multiple collisions can happen only if the {@link Recipe} argument is a wildcard {@link Recipe}.
	 * </p>Note2: The {@link Recipe} argument is <u>not</u> added to the server by this operation!
	 * </p>Note3: Meta-data comparisons will only be performed if an {@link IMetaChecker} has been set!
	 * @param server
	 * @param removeIfFound if this is <code>false</code> this operation is read-only
	 * @param recipe
	 * @return <code>null</code> if {@link Recipe} argument is <code>null</code>, otherwise a {@link List}&lt;{@link Recipe}&gt;
	 *  with the {@link Recipe}s on the {@link Server} that the {@link Recipe} argument collided with.
	 * @throws IllegalArgumentException if {@link Server} is <code>null</code>
	 * @see #isWildcardRecipe(Recipe)
	 * @see #addRecipesToServer(Server, boolean, Recipe...)
	 * @see #setMetaChecker(IMetaChecker)
	 */
	public List<Recipe> serverHasSimilarIngredientRecipe(final Server server, final boolean removeIfFound, final Recipe recipe)
	{
		if (server == null)
			throw new IllegalArgumentException("server is null");
		else if (recipe == null)
			return null;
		else
		{
			final boolean isWild = isWildcardRecipe(recipe);
			final List<Recipe> collisionList = isWild ? new ArrayList<Recipe>() : new ArrayList<Recipe>(1);
			final Iterator<Recipe> bukk_it = server.recipeIterator();
			while (bukk_it.hasNext())
			{
				final Recipe recipe2 = bukk_it.next();
				if (ingredientsMatch(recipe, recipe2))
				{
					collisionList.add(recipe2);
					if (removeIfFound)
						bukk_it.remove();
					if (!isWild)
						return collisionList;
				}
			}
			return collisionList;
		}
	}
	
	/**
	 * Adds a bunch of {@link Recipe}s to a {@link Server}, checking each for collision against an existing {@link Recipe}.
	 * </p>Note1: This method <u>assumes</u> there are no collisions <i>within</i> the {@link Recipe} arguments to be added.
	 * If there are argument-to-argument collisions some {@link Recipe} add operations may silently fail!
	 * <br><b>It is the responsibility of the caller to ensure that no collisions exists among the provided {@link Recipe}
	 * arguments themselves!</b>
	 * </p>Note2: For performance reasons the order in which the {@link Recipe}s are actually added can differ from the
	 * order of the {@link Recipe} arguments. (Thus the outcome of any argument-to-argument collisions are unpredictable!)
	 * </p>Note3: This operation is roughly <b>{@code O}</b><code>(n*m + m*x)</code> where {@code n} and {@code m} is the
	 * pre-existing and to-be-added recipes respectively, and <code>x</code> is the cost of calling
	 * {@link Server#addRecipe(Recipe)}. (This method is intended as a faster alternative to checking each {@link Recipe}
	 * you want to add with {@link #serverHasSimilarIngredientRecipe(Server, boolean, Recipe)} and then adding it manually.)
	 * </p>Note4: <code>null</code> {@link Recipe}s are silently ignored.
	 * </p>Note5: Meta-data comparisons will only be performed if an {@link IMetaChecker} has been set!
	 * @param server
	 * @param allowOverwriteExisting if this is <code>true</code> collisions will be resolved by removal of the existing recipe
	 * @param recipes
	 * @return a {@link List}&lt;{@link Recipe}&gt; - if {@code allowOverwriteExisting} is <code>true</code> this will contain all
	 *  the old recipes that where removed, if {@code allowOverwriteExisting} is <code>false</code> this will return all the
	 *  new recipes that wasn't added to the {@link Server} due to collisions. In other words it returns all the "casualties" of
	 *  collisions.<br>(Returns <code>null</code> if {@code recipes} argument is <code>null</code> or a zero-length array.)
	 * @throws IllegalArgumentException if {@link Server} is <code>null</code>
	 * @see #serverHasSimilarIngredientRecipe(Server, boolean, Recipe)
	 * @see #setMetaChecker(IMetaChecker)
	 * @see #isWildcardRecipe(Recipe)
	 */
	public List<Recipe> addRecipesToServer(final Server server, final boolean allowOverwriteExisting, final Recipe... recipes)
	{
		if (server == null)
			throw new IllegalArgumentException("server is null");
		else if (recipes == null || recipes.length == 0)
			return null;
		else
		{
			/* This code works by first iterating through all existing recipes, checking for collisions against the "recipes" arguments
			 * Any removals that are required due to collisions are performed (resolved) during this iteration.
			 * (Also the casualties are recorded in a List<Recipe>.)
			 * Once all collisions have been resolved a small loop adds all (survived) "recipes" arguments.
			 * List<Recipe> of casualties are returned.
			 */
			final List<Recipe> deadRecipes = new LinkedList<Recipe>(); //Casualties of collision
			final LinkedList<Recipe> toAdd = allowOverwriteExisting ? new LinkedList<Recipe>() : null; //(only used with overwrite)
			int arrSize = recipes.length; //(this will shrink as entries gets removed from the array)
			int wildIndexNext = 0; //Any recipe below this index is a wildcard recipe (only used with overwrite)
			final Iterator<Recipe> bukk_it = server.recipeIterator();
			//Collision resolution phase:
			while (bukk_it.hasNext())
			{
				final Recipe recipe1 = bukk_it.next();
				for (int i = 0; i < arrSize; ++i)
				{
					final Recipe recipe2 = recipes[i];
					if (recipe2 == null) //remove null entries to speed up operation...
					{
						--arrSize;
						recipes[i] = recipes[arrSize]; //"Fast delete"
						--i; // <-- make sure we dont accidentally skip a recipe argument when fast-deleting!
					}
					if (ingredientsMatch(recipe1, recipe2)) //Collision!
					{
						if (allowOverwriteExisting)
						{
							bukk_it.remove();
							deadRecipes.add(recipe1); //add the removed recipe to the collision list
							if (i >= wildIndexNext) //we have not checked if this is a wildcard recipe
							{
								if (isWildcardRecipe(recipe2))
								{
									recipes[i] = recipes[wildIndexNext];
									recipes[wildIndexNext] = recipe2;
									++wildIndexNext;
									//since i >= wildIndexNext we dont need to adjust "i" (and we cant get out of bounds)
								}
								else //recipe2 is non-wildcard and has collided already => no need to check it against remaining recipes on server
								{   //this removal and adding to a separate list is purely optimization to reduce the number of iterations needed
									--arrSize;
									recipes[i] = recipes[arrSize]; //"Fast delete"
									--i; // <-- make sure we dont accidentally skip a recipe argument when fast-deleting!
									toAdd.add(recipe2); //move surviving non-wildcard recipe argument to separate list for performance reasons
								}
							}
							break; //server recipe removed => no point in checking it against remaining recipe arguments
						}
						else //!allowOverwriteExisting
						{
							deadRecipes.add(recipe2); //This recipe could not be added due to collision
							//recipe2 has collided already => no need to check it against remaining recipes on server:
							--arrSize;
							recipes[i] = recipes[arrSize]; //"Fast delete"
							--i; // <-- make sure we dont accidentally skip a recipe argument when fast-deleting!
							//we DONT break; here because due to wildcard mechanics the server recipe could collide with multiple arguments!
						}
					}
				}
			} //All collisions resolved...
			//Add-to-Server phase:
			for (int i = 0; i < arrSize; ++i) //adds wildcard recipes and recipes that didn't collide with anything
				server.addRecipe(recipes[i]);
			if (allowOverwriteExisting)
				for (Recipe r : toAdd) //adds non-wildcard recipes that where involved in collisions (but "survived" since overwrite enabled)
					server.addRecipe(r);
			//Return casualties:
			return deadRecipes;
		}
	}
	
	
	// -------- Recipe ingredients matching methods -------- 
	
	/**
	 * Checks if two {@link Recipe}s ingredients are similar* in such a way that both can't exist on a {@link Server}
	 * at the same time.
	 * </p>Note1: {@link ShapedRecipe}s with a 1x1 shape can collide with {@link ShapelessRecipe}s with only 1
	 * ingredient - this is also checked by this method!
	 * </p>Note2: Meta-data comparisons will only be performed if an {@link IMetaChecker} has been set!
	 * </p>&nbsp;<b>*</b><i>is wildcard recipe aware, see {@link #isWildcardRecipe(Recipe)}</i>
	 * @param recipe1
	 * @param recipe2
	 * @return <code>true</code> if both {@link Recipe}s are <code>null</code> or are both references to the same object
	 *  or (of the same type and) have similar ingredients, otherwise <code>false</code>.
	 * @see #compareFurnaceStackSizes()
	 * @see #setMetaChecker(IMetaChecker)
	 * @see #ingredientsMatchS(ShapedRecipe, ShapedRecipe)
	 * @see #ingredientsMatchSL(ShapelessRecipe, ShapelessRecipe)
	 * @see #ingredientsMatchF(FurnaceRecipe, FurnaceRecipe)
	 */
	public boolean ingredientsMatch(final Recipe recipe1, final Recipe recipe2)
	{
		if (recipe1 == recipe2)
			return true;
		else if (recipe1 == null || recipe2 == null)
			return false;
		else if (recipe1 instanceof ShapedRecipe) //ShapedRecipes are probably most common so check that first
		{
			if (recipe2 instanceof ShapedRecipe)
				return ingredientsMatchS0((ShapedRecipe)recipe1, (ShapedRecipe)recipe2);
			else if (recipe2 instanceof ShapelessRecipe)
				return ingredientsMatchSLS0((ShapelessRecipe) recipe2, (ShapedRecipe) recipe1);
		}
		else if (recipe1 instanceof ShapelessRecipe)
		{
			if (recipe2 instanceof ShapedRecipe)
				return ingredientsMatchSLS0((ShapelessRecipe) recipe1, (ShapedRecipe) recipe2);
			else if (recipe2 instanceof ShapelessRecipe)
				return ingredientsMatchSL0((ShapelessRecipe)recipe1, (ShapelessRecipe)recipe2);
		}
		else if (recipe1 instanceof FurnaceRecipe)
		{
			if (recipe2 instanceof FurnaceRecipe)
				return ingredientsMatchF0((FurnaceRecipe)recipe1, (FurnaceRecipe)recipe2, b_compareFurnaceStackSize);
		}
		//else {}
			//Unknown recipe1 type! //TODO: log? throw?

		return false;
	}
	
	/**
	 * @see #ingredientsMatch(Recipe, Recipe)
	 */
	public boolean ingredientsMatchS(final ShapedRecipe shaped1, final ShapedRecipe shaped2)
	{
		return (shaped1 == shaped2) ? true : (shaped1 == null || shaped2 == null) ? false : ingredientsMatchS0(shaped1, shaped2);
	}
	
	/**
	 * @see #ingredientsMatch(Recipe, Recipe)
	 */
	public boolean ingredientsMatchSL(final ShapelessRecipe shapeless1, final ShapelessRecipe shapeless2)
	{
		return (shapeless1 == shapeless2) ? true : (shapeless1 == null || shapeless2 == null) ? false : ingredientsMatchSL0(shapeless1, shapeless2);
	}
	
	/**
	 * Compares ingredients in a {@link ShapelessRecipe} to a {@link ShapedRecipe}.
	 * <br><i>(They can be similar only if the {@link ShapedRecipe} is 1x1 and the {@link ShapelessRecipe}
	 * has exactly 1 ingredient.)</i>
	 * @see #ingredientsMatch(Recipe, Recipe)
	 */
	public boolean ingredientsMatchSLS(final ShapelessRecipe shapeless, final ShapedRecipe shaped)
	{
		if(shapeless == null)
			return shaped == null;
		else if (shaped == null)
			return false;
		else
			return ingredientsMatchSLS0(shapeless, shaped);
	}
	
	/**
	 * @see #ingredientsMatch(Recipe, Recipe)
	 * @see #compareFurnaceStackSizes()
	 */
	public boolean ingredientsMatchF(final FurnaceRecipe fRecipe1, final FurnaceRecipe fRecipe2)
	{
		return (fRecipe1 == fRecipe2) ? true : (fRecipe1 == null || fRecipe2 == null) ? false : ingredientsMatchF0(fRecipe1, fRecipe2, b_compareFurnaceStackSize);
	}
	
	
	// -------- Recipe ingredients matching methods - without reference-equality and null checks --------
	
	/**
	 * @see #ingredientsMatchS(ShapedRecipe, ShapedRecipe)
	 */
	protected boolean ingredientsMatchS0(final ShapedRecipe shaped1, final ShapedRecipe shaped2)
	{
		//Check shape dimensions
		final String[] sh1 = shaped1.getShape(); //shape
		final String[] sh2 = shaped2.getShape();
		final int h1 = sh1.length; //height
		final int h2 = sh2.length;
		final int w1; //width
		final int w2;
		if (h1 != h2) 
			return false; //different height
		else if (h1 != 0) //(not 0x0)
		{
			w1 = sh1[0].length();
			w2 = sh2[0].length();
			if (w1 != w2)
				return false; //different width
		} else
			return true; //neither recipe had any ingredients... (0x0)
		
		//Shape dimensions match, proceed to Check actual items
		final Map<Character, ItemStack> im1 = shaped1.getIngredientMap();
		final Map<Character, ItemStack> im2 = shaped2.getIngredientMap();
		for(int i = 0; i < h1; ++i)
		{
			for(int j = 0; j < w1; ++j)
			{
				final char c1 = sh1[i].charAt(j);
				final char c2 = sh2[i].charAt(j);
				final ItemStack is1 = im1.get(c1);
				final ItemStack is2 = im2.get(c2);
				if (!itemStacksMatch(is1, is2, false))
					return false; //recipes differ on grid i:j
			}
		}
		
		return true;
	}
	
	/**
	 * @see #ingredientsMatchSL(ShapelessRecipe, ShapelessRecipe)
	 */
	protected boolean ingredientsMatchSL0(final ShapelessRecipe shapeless1, final ShapelessRecipe shapeless2)
	{
		final List<ItemStack> list1 = shapeless1.getIngredientList();
		final List<ItemStack> list2 = shapeless2.getIngredientList();
		//return list1.size() == list2.size() && list1.containsAll(list2); //will use ItemStack.equals
		//^That would have been sooo nice, but doesn't account for durability 32767
		if (list1.size() == list2.size())
		{
			int arr2size = list2.size(); //used for "fast-delete"
			final ItemStack[] is2arr = shapeless2.getIngredientList().toArray(new ItemStack[arr2size]);
			
			for (final ItemStack is1 : list1)
			{
				for (int i = 0; /*i < arr2size*/ true; ++i)
				{
					if (i >= arr2size) //negated loop condition
						return false;
					else if (itemStacksMatch(is1, is2arr[i], true))
					{
						--arr2size;
						is2arr[i] = is2arr[arr2size]; //"Fast delete"
						--i; // <-- make sure we dont accidentally skip a recipe argument when fast-deleting!
						break;
					}
				}
			}
			//return arr2size == 0; //This is always true...
			return true;
		}
		else
			return false;
	}
	
	/**
	 * @see #ingredientsMatchSLS(ShapelessRecipe, ShapedRecipe)
	 */
	protected boolean ingredientsMatchSLS0(final ShapelessRecipe shapeless, final ShapedRecipe shaped)
	{
		final List<ItemStack> listSL = shapeless.getIngredientList();
		if (listSL.size() != 1)
			return false; //shapeless has more than 1 ingredient!
		
		final String[] shape = shaped.getShape();
		if (shape.length != 1 || shape[0].length() != 1) 
			return false; //shape not 1x1!
		
		final ItemStack isS = shaped.getIngredientMap().get(shape[0].charAt(0));
		final ItemStack isSL = listSL.get(0);
		return itemStacksMatch(isS, isSL, true);
	}
	
	/**
	 * @see #ingredientsMatchF(FurnaceRecipe, FurnaceRecipe)
	 */
	protected boolean ingredientsMatchF0(final FurnaceRecipe fRecipe1, final FurnaceRecipe fRecipe2, final boolean compareStackSize)
	{
		return itemStacksMatch(fRecipe1.getInput(), fRecipe2.getInput(), compareStackSize);
	}
	
	
	// -------- ItemStack Helper methods --------
	
	/**
	 * Checks if two {@link ItemStack}s are similar*.
	 * </p>Meta-checks (if enabled) are performed thusly:
	 * <ul>
	 *  <li>Both {@link ItemStack}s have meta-data:
	 *   <ul>
	 *    <li>Use the {@link IMetaChecker#areItemMetaIdentical(ItemMeta, ItemMeta)
	 *        areItemMetaIdentical(ItemMeta, ItemMeta)} method to test meta similarity.</li>
	 *   </ul>
	 *  </li>
	 *  <li>None of the {@link ItemStack}s have meta-data:
	 *   <ul>
	 *    <li>Consider {@link ItemStack}s meta similar.</li>
	 *   </ul>
	 *  </li>
	 *  <li>Only one of the {@link ItemStack}s have meta-data:
	 *   <ul>
	 *    <li>Does the metaChecker implement the {@link IMetaChecker#isValidItemMeta(ItemMeta)
	 *        isValidItemMeta(ItemMeta)} method?
	 *     <ul>
	 *      <li>No**: Fail the meta similarity check (method will return <code>false</code>)</li>
	 *      <li>Yes: Fail the meta similarity check if {@link IMetaChecker#isValidItemMeta(ItemMeta)
	 *          isValidItemMeta(ItemMeta)} returns <code>true</code>,
	 *          <br>otherwise consider {@link ItemStack} metas similar. (None of them has "valid" meta-data).
	 *      </li>
	 *     </ul>
	 *    </li>
	 *   </ul>
	 *  </li>
	 * </ul>
	 * </p>Note: Meta-data comparisons will only be performed if an {@link IMetaChecker} has been set!
	 * </p>&nbsp;<b>*</b><i>is wildcard aware, see {@link #isWildcardItemStack(ItemStack)}</i>
	 * <br>&nbsp;<b>**</b><i>see {@link #setMetaChecker(Class, Object)} to understand how this can happen</i>
	 * @param is1
	 * @param is2
	 * @param checkStackSize if this is <code>false</code> stack-sizes wont be compared.
	 * @return <code>true</code> if both {@link ItemStack}s are <code>null</code> or both reference the same object
	 *  or they passed all <u>performed</u> similarity comparisons, otherwise <code>false</code>.
	 * @see #isWildcardItemStack(ItemStack)
	 * @see #setMetaChecker(Class, Object)
	 * @see #setMetaChecker(IMetaChecker)
	 * @see IMetaChecker
	 */
	public boolean itemStacksMatch(final ItemStack is1, final ItemStack is2, final boolean checkStackSize)
	{
		if (is1 == is2)
			return true;
		else if (is1 == null || is2 == null) //(these simple checks could be replaced with "isValidStack"-checks)
			return false;
		else
		{   //neither is1 nor is2 is null
		
			final Material m1 = is1.getType();
			final Material m2 = is2.getType();
			if (m1 != m2)
				return false;
			
			final short d1 = is1.getDurability();
			final short d2 = is2.getDurability();
			if (d1 != d2 && d1 != WILDCARD_DURABILITY && d2 != WILDCARD_DURABILITY)
				return false;
			
			if (checkStackSize)
			{
				final int ss1 = is1.getAmount();
				final int ss2 = is2.getAmount();
				if (ss1 != ss2)
					return false;
			}
			
			if (isMetaCheckerAvailable()) //Check meta-data?
			{
				//Verbose code:
	//			if (is1.hasItemMeta()) {
	//				if(is2.hasItemMeta())
	//					return MetaCheckerHelper.isMetaSimilar(is1.getItemMeta(), is2.getItemMeta()); //Both have meta
	//				else if (MetaCheckerHelper.isMetaValid(is1.getItemMeta()))
	//					return false; //#1 had valid meta, #2 had no meta
	//				else
	//					return true; //#1 has invalid meta, #2 had no meta //is TRUE the expected result here??
	//			} else if (is2.hasItemMeta()) {
	//				if (MetaCheckerHelper.isMetaValid(is2.getItemMeta()))
	//					return false; //#2 had valid meta, #1 had no meta
	//				else
	//					return true; //#2 had invalid meta, #1 had no meta //is TRUE the expected result here??
	//			} else
	//				return true; //None of them had any meta
				
				//^Same code compacted:
				if (is1.hasItemMeta()) {
					if(is2.hasItemMeta())
						return isMetaSimilar(is1.getItemMeta(), is2.getItemMeta()); //Both have meta
					else if (isMetaValid(is1.getItemMeta()))
						return false; //#1 had valid meta, #2 had no meta
				} else if (is2.hasItemMeta() && isMetaValid(is2.getItemMeta()))
					return false; //#2 had valid meta, #1 had no meta
			}
			
	        return true; //Passed all (performed) similarity checks
		}
	}
	
	/**
	 * Checks if an {@link ItemStack} is a wildcard {@link ItemStack}.
	 * </p>(A wildcard {@link ItemStack} has the durability {@value #WILDCARD_DURABILITY}.)
	 * @param itemStack
	 * @return
	 * @see #isWildcardRecipe(Recipe)
	 */
	public boolean isWildcardItemStack(final ItemStack itemStack)
	{
		return itemStack != null && itemStack.getDurability() == WILDCARD_DURABILITY; 
	}
	
	// -------- Recipe Wildcard Helper methods --------
	
	/**
	 * Checks if {@link Recipe} contains any wildcard {@link ItemStack}s.
	 * (This enables a single {@link Recipe} to match several similar ingredient types.)
	 * </p>(A wildcard {@link ItemStack} has the durability {@value #WILDCARD_DURABILITY}.)
	 * @param recipe
	 * @return
	 * @see #isWildcardItemStack(ItemStack)
	 */
	public boolean isWildcardRecipe(final Recipe recipe)
	{
		if (recipe == null)
			return false;
		else if (recipe instanceof ShapedRecipe)
			return isWildcardRecipeS((ShapedRecipe)recipe);
		else if (recipe instanceof ShapelessRecipe)
			return isWildcardRecipeSL((ShapelessRecipe)recipe);
		else if (recipe instanceof FurnaceRecipe)
			return isWildcardRecipeF((FurnaceRecipe)recipe);
		else
			return false; //Unknown recipe type... //TODO: log? throw?
	}
	
	/**
	 * @see #isWildcardRecipe(Recipe)
	 */
	public boolean isWildcardRecipeS(final ShapedRecipe shaped)
	{
		if (shaped == null)
			return false;
		else {
			final Collection<ItemStack> items = shaped.getIngredientMap().values();
			for (ItemStack is : items)
				if (isWildcardItemStack(is))
					return true;
			return false;
		}
	}

	/**
	 * @see #isWildcardRecipe(Recipe)
	 */
	public boolean isWildcardRecipeSL(final ShapelessRecipe shapeless)
	{
		if (shapeless == null)
			return false;
		else {
			for (ItemStack is : shapeless.getIngredientList())
				if (isWildcardItemStack(is))
					return true;
			return false;
		}
	}

	/**
	 * @see #isWildcardRecipe(Recipe)
	 */
	public boolean isWildcardRecipeF(final FurnaceRecipe fRecipe)
	{
		return fRecipe != null && isWildcardItemStack(fRecipe.getInput());
	}
	
	
	// -------- MetaChecker interface / classes --------
	
	/**
	 * Optional interface for implementing MetaChecker methods.
	 * <br><i>(Implementing this is preferred since it avoids reflection!)</i>
	 * </p>(These method names is intentionally the same as CraftBooks ItemUtil.java methods.)
	 * @author AnorZaken
	 * @see RecipeHelper#itemStacksMatch(ItemStack, ItemStack, boolean)
	 * @see RecipeHelper#setMetaChecker(Class, Object)
	 * @see RecipeHelper#setMetaChecker(IMetaChecker)
	 */
	public static interface IMetaChecker
	{
		/**
		 * Determines if two {@link ItemMeta} objects should be considered identical.
		 * </p>Note1: This method is <u>not</u> required to be able to handle <code>null</code> arguments!
		 * </p>Note2: See {@link RecipeHelper#itemStacksMatch(ItemStack, ItemStack, boolean)} for a
		 * description of how this method is used by the {@link RecipeHelper} class.
		 * @param meta1
		 * @param meta2
		 * @return <code>true</code> if the two {@link ItemMeta}s should be considered identical by the
		 *  methods in the {@link RecipeHelper} class, otherwise <code>false</code>.
		 * @see RecipeHelper#itemStacksMatch(ItemStack, ItemStack, boolean)
		 */
		public boolean areItemMetaIdentical(ItemMeta meta1, ItemMeta meta2);
		
		/**
		 * Determines if an {@link ItemMeta} should be considered a valid {@link ItemMeta}.
		 * </p>Note1: This method is <u>not</u> required to be able to handle <code>null</code> as argument!
		 * </p>Note2: See {@link RecipeHelper#itemStacksMatch(ItemStack, ItemStack, boolean)} for a
		 * description of how this method is used by the {@link RecipeHelper} class.
		 * @param meta
		 * @return <code>false</code> if the {@link ItemStack} with this {@link ItemMeta} should be considered
		 *  (meta-)equivalent to an {@link ItemStack} that has no meta data, otherwise <code>true</code>.
		 * @see RecipeHelper#itemStacksMatch(ItemStack, ItemStack, boolean)
		 */
		public boolean isValidItemMeta(ItemMeta meta);
	}
	
	// ===========================
	// Moved out of the helper:
	
	// ----- Helper methods
	
	/**
	 * Checks if an {@link IMetaChecker} is available.
	 */
	public boolean isMetaCheckerAvailable() {
		return checker != null;
	}
	
	/**
	 * Wraps the call to {@link IMetaChecker#areItemMetaIdentical(ItemMeta, ItemMeta)} or to a reflected equivalent.
	 * @param meta1
	 * @param meta2
	 * @return (<code>true</code> if no metaChecker available)
	 */
	public boolean isMetaSimilar(ItemMeta meta1, ItemMeta meta2)
	{
		final IMetaChecker metaChecker = getMetaChecker(); //Localize the reference (tsafety)
		return metaChecker == null ? true : metaChecker.areItemMetaIdentical(meta1, meta2);
	}
	
	/**
	 * Wraps the call to {@link IMetaChecker#isValidItemMeta(ItemMeta)} or to a reflected equivalent.
	 * @param meta
	 * @return (<code>true</code> if no metaChecker available)
	 */
	public boolean isMetaValid(ItemMeta meta)
	{
		final IMetaChecker metaChecker = getMetaChecker(); //Localize the reference (tsafety)
		return metaChecker == null ? true : metaChecker.isValidItemMeta(meta);
	}
	
	// ----- "Wrapper Factory" / MetaChecker setter
	
	/**
	 * Tries to set the {@link IMetaChecker} instance, either directly or by creating a wrapper with reflection.
	 * </p>If {@link IMetaChecker} is <code>null</code> it will try using reflection on the provided {@link Class}
	 * argument and, if reflection succeeds, create a wrapper that wraps the needed reflection code as an
	 * {@link IMetaChecker} (and then set that as the active {@link IMetaChecker} instance).</i>
	 * </p>If both the {@link IMetaChecker} and {@link Class} arguments are <code>null</code> it will remove the
	 * currently set {@link IMetaChecker} instance (if one existed), thus disabling all meta-data checks in
	 * {@link RecipeHelper}.
	 * </p><i>Note: Make sure a suitable instance Object is provided if reflection is used (unless the methods are
	 * static, in which case no instance Object is needed for method invocations).</i>
	 * @param metaChecker
	 * @param metaCheckerClass
	 * @param metaCheckerRaw instance object for non-static method calls using reflection
	 * @return <code>false</code> if reflection was needed but failed (also results in a stack-trace), otherwise
	 *  <code>true</code>
	 * @see RecipeHelper#setMetaChecker(Class, Object)
	 * @see RecipeHelper#setMetaChecker(IMetaChecker)
	 */
	public boolean TrySetMetaChecker(final IMetaChecker metaChecker, final Class<? extends Object> metaCheckerClass, final Object metaCheckerRaw)
	{
		if (metaChecker != null)
		{
			checker = new MetaCheckerHelper(metaChecker);
			return true;
		}
	    else if (metaCheckerClass == null)
		{
	    	checker = null;
			return true;
		}
		else
		{
			final Method aim;
            try {
                aim = metaCheckerRaw.getClass().getMethod("areItemMetaIdentical", ItemMeta.class, ItemMeta.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            } catch (SecurityException e) {
                e.printStackTrace();
                return false;
            }

            Method ivm;
            try {
            	ivm = metaCheckerRaw.getClass().getMethod("isValidItemMeta", ItemMeta.class);
            } catch (NoSuchMethodException e) {
	            ivm = null;
	        } catch (SecurityException e) {
	            e.printStackTrace();
	            return false;
	        }
            
            checker = new MetaCheckerHelper(metaCheckerClass, metaCheckerRaw, aim, ivm);
            return true;
        }
	}
	
	// ===============================
	
	/**
	 * Helper class to wrap the reflection code related to the metaChecker functionality.
	 * Thus the rest of the {@link RecipeHelper} class doesn't need to know or care about any reflection related issues.
	 * @see RecipeHelper#setMetaChecker(Class, Object)
	 * @see MetaCheckerHelper#TrySetMetaChecker(Class, Object)
	 * @author AnorZaken
	 */
	protected final class MetaCheckerHelper implements IMetaChecker
	{
		// ----- Instance variables (all private and immutable)
		
		final private Class<? extends Object> metaCheckerClass; //(never null!)
		final private Object metaCheckerRaw; //(never null!)
		final private Method areIdenticalMethod;
		final private Method isValidMethod;
		final private IMetaChecker metaChecker; //(never null!)
		
		
		// ----- Constructors
		
		//For reflection:
		private MetaCheckerHelper(final Class<? extends Object> metaCheckerClass, final Object metaCheckerInstance, final Method aim, final Method ivm)
		{
			this.metaCheckerClass = metaCheckerClass;
			this.metaCheckerRaw = metaCheckerInstance;
			this.areIdenticalMethod = aim;
			this.isValidMethod = ivm;
			this.metaChecker = this;
		}
		//For IMetaChecker:
		private MetaCheckerHelper(final IMetaChecker metaChecker)
		{
			this.metaCheckerClass = metaChecker.getClass();
			this.metaCheckerRaw = metaChecker;
			this.areIdenticalMethod = null;
			this.isValidMethod = null;
			this.metaChecker = metaChecker;
		}
		
		
		// ----- Raw get accessors
		
		public Class<? extends Object> getMetaCheckerClass()
		{
			return metaCheckerClass;
		}
		
		public Object getMetaCheckerRaw()
		{
			return metaCheckerRaw;
		}
		
		public IMetaChecker getMetaChecker()
		{
			return metaChecker;
		}
		
		
		// ----- Invocation wrapper methods
		
		/**
		 * @return the returned value from calling {@code areItemMetaIdentical(ItemMeta, ItemMeta)} with reflection,
		 *  or <code>false</code> if the method invocation fails (there will be a stack-trace).
		 * @see #areItemMetaIdentical(ItemMeta, ItemMeta)
		 */
		private boolean reflectSimilar(final ItemMeta im1, final ItemMeta im2)
		{
			try {
				return (Boolean) areIdenticalMethod.invoke(metaCheckerRaw, im1, im2);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
			return false;
		}
		
		/**
		 * @return the returned value from calling {@code isValidItemMeta(ItemMeta)} with reflection,
		 *  or <code>true</code> if the method invocation fails (there will be a stack-trace).
		 * @see #isValidItemMeta(ItemMeta)
		 */
		private boolean reflectValid(final ItemMeta im)
		{
			try {
                return (Boolean) isValidMethod.invoke(metaCheckerRaw, im);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            return true;
		}
		
		
		// ----- IMetaChecker methods

		@Override
		public boolean areItemMetaIdentical(ItemMeta meta1, ItemMeta meta2) {
			return reflectSimilar(meta1, meta2);
		}

		@Override
		public boolean isValidItemMeta(ItemMeta meta) {
			return reflectValid(meta);
		}
	}
}
