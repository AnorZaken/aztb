package nu.mine.obsidian.aztb.commandflag.wip;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import nu.mine.obsidian.aztb.commandflag.v1_0.CommandFlag;
import nu.mine.obsidian.aztb.commandflag.v1_0.CommandFlag.FlagValues;

/**
 * wip - not ready for use!!
 * @author AnorZaken
 * @version 0.1
 */
public class CommandFlagGroup //TODO: javadoc
{
	/**
	 * Class to implement the tree-structure used to quickly match queries against multiple flags.
	 * @author AnorZaken
	 */
	protected static final class FlagTreeNode //"IMMUTABLE" (except if sub-classed)
	{
		//Important relations to take note of: (keep this in mind or the code will be hard to understand)
		// leaf == null <=> branches != null
		// leaf != null <=> branches == null
		
		/**
		 * The <code>char</code> of this tree-node (i.e. its key).
		 */
		public final char c;
		/**
		 * The stem (i.e. parent) of this branch.
		 */
		public final FlagTreeNode stem;
		protected final ArrayList<FlagTreeNode> branches; //Never manipulated outside this class (/child-classes)!
		protected final CommandFlag leaf;
		
		// -----
		
		/**
		 * Constructor to create a root-node for a {@link FlagTreeNode}.
		 * @param initialBranchCapacity initial branch capacity of the root
		 */
		public FlagTreeNode(final int initialBranchCapacity) //Used by root only (externally by CommandFlagGroup)
		{
			if (initialBranchCapacity < 0)
				throw new IllegalArgumentException("initialBranchCapacity negative");
			
			this.branches = new ArrayList<FlagTreeNode>(initialBranchCapacity);
			this.c = ' ';
			this.stem = null;
			this.leaf = null;
		}
		
		/**
		 * Constructor used internally for branch and leaf creation. 
		 * @param c <code>char</code> of the new branch
		 * @param stem {@link FlagTreeNode} branch this will get connected to (<u>Must be handled by the caller</u>)
		 * @param cf a {@link CommandFlag} if this is to be a leaf-node, otherwise <code>null</code>.
		 */
		protected FlagTreeNode(final char c, final FlagTreeNode stem, final CommandFlag  cf) //Used internally (branch creation)
		{
			if (stem == null)
				throw new AssertionError("Internal branching error: no stem", new NullPointerException("stem is null"));
			this.branches = cf == null ? new ArrayList<FlagTreeNode>(1) : null;
			this.c = c;
			this.stem = stem;
			this.leaf = cf;
		}
		
		// -----
		
		/**
		 * Get the {@link CommandFlag} leaf of this {@link FlagTreeNode} branch.
		 * @return {@link CommandFlag} leaf, or <code>null</code> if this branch isn't a leaf.
		 */
		public CommandFlag getLeaf() {
			return leaf;
		}
		
//		public void setLeaf(final CommandFlag flag) {
//			leaf = flag;
//		}
		
//		protected List<Char2Flags> getBranches() {
//			return branches;
//		}
		
		/**
		 * Try to get a branch for a <code>char</code>.
		 * @param c <code>char</code> of branch
		 * @return the {@link FlagTreeNode} branch with the requested <code>char</code>, or <code>null</code> if no
		 *  such branch exists.
		 */
		public FlagTreeNode getBranch(final char c)
		{
			if (branches == null)
				return null;
			for (final FlagTreeNode c2f : branches)
				if (c2f.c == c)
					return c2f;
			return null;
		}
		
		/**
		 * Creates a new or reuses an existing branch provided it wont cause a {@link CommandFlag} collision.
		 * @param c {@code char} for which a branch is requested
		 * @param cf a {@link CommandFlag} if the requested branch should be a leaf, otherwise <code>null</code>
		 * @return requested {@link FlagTreeNode} branch (new or existing), or <code>null</code> if adding the requested
		 *  branch would result in a flag collision.
		 */
		public FlagTreeNode tryBranch(final char c, final CommandFlag cf)
		{
			if (leaf != null) //this is a leaf = not OK. (Can't branch from a leaf!)
				return null;
			FlagTreeNode c2f = getBranch(c);
			if (c2f == null) {
				c2f = new FlagTreeNode(c, this, cf); //no branch(/leaf) existed, so create it = OK!
				branches.add(c2f);
				return c2f;
			}
			else if (c2f.leaf == null && cf == null) //branch exists and a non-leaf was requested = OK!
				return c2f;
			else
				return null; //branch exists and (is a leaf or leaf requested) = not OK.
		}
		
		/**
		 * Get all* {@link CommandFlag CommandFlags} connected to this branch. 
		 * <br><i>Note: Does a depth-first (non-recursive) search for leaf-nodes.</i>
		 * @param all *if this is <code>false</code> search will stop after the first leaf is found
		 * @return a {@link List List&ltCommandFlag&gt} with the requested leafs.
		 */
		public List<CommandFlag> getFlagsDeep(final boolean all) //Can be used for tab-completion!
		{
			final ArrayList<CommandFlag> cfArr;
			FlagTreeNode c2f = this;
			if (!all)
			{
				cfArr = new ArrayList<CommandFlag>(1);
				for(; c2f.leaf == null; c2f = c2f.branches.get(0));
				cfArr.add(c2f.leaf);
				return cfArr;
			}
			else //Perform a depth-first style search for flags
			{
				cfArr = new ArrayList<CommandFlag>();
				final Stack<FlagTreeNode> stack = new Stack<FlagTreeNode>();
				do
				{
					if (c2f.leaf == null) //only stack.push when branch actually splits
					{
						final ArrayList<FlagTreeNode> b = c2f.branches;
						c2f = b.get(0); 
						for (int i = b.size() - 1; i > 0 ; ++i) // "i > 0" is NOT a typo
							stack.push(b.get(i));
					}
					else //only stack.pop when leaf processed
					{
						cfArr.add(c2f.leaf);
						c2f = stack.isEmpty() ? null : stack.pop(); // <-- Exit point
					}
				}
				while (c2f != null);
				return cfArr;
			}
		}
		
		/**
		 * Remove this leaf and any dead branch-nodes resulting from this leaf removal.
		 * @throws IllegalStateException if this is not a leaf.
		 */
		public void removeLeafBranch() //Better name??
		{
			if (leaf == null)
				throw new IllegalStateException("Remove attempted on non-leaf branch.");
			
			FlagTreeNode c2f = this;
			ArrayList<FlagTreeNode> b;
			for(b = c2f.stem.branches; b.size() == 1; b = c2f.stem.branches)
				c2f = c2f.stem;
			//b.remove(c2f); //Don't care about element order! This is a waste!
			final int i = b.indexOf(c2f);
			final int j = b.size() - 1;
			if (i != j)
				b.set(i, b.get(j));
			b.remove(j);
		}
	}
	
	// =====================
	// =====================
	
	protected final FlagTreeNode root;
	protected final HashMap<String, FlagTreeNode> name2Char2Flags;
	
	//TODO: javadoc
	public CommandFlagGroup(final int initialFlagCapacity, final int initialRootCapacity)
	{
		root = new FlagTreeNode(initialRootCapacity);
		name2Char2Flags = new HashMap<String, FlagTreeNode>(initialFlagCapacity);
	}
	
	/**
	 * Create a {@link CommandFlagGroup} with default initialFlagCapacity 8, and default initialRootCapacity 4.
	 */
	public CommandFlagGroup() {
		this(8, 4);
	}
	
	// =====================
	
	/**
	 * Get number of {@link CommandFlag CommandFlags} in this {@link CommandFlagGroup}.
	 */
	public int size() {
		return name2Char2Flags.size();
	}
	
	// =====================
	
	/**
	 * Adds a {@link CommandFlag} to this {@link CommandFlagGroup}. <br>
	 * <i>This is (close to) an <code>O(k)</code> operation, where <code>k</code> is the length of the {@link CommandFlag}
	 * to add. (In an extreme unlikely worst-case scenario this is <code>O(k+n)</code> where <code>n</code> is the number
	 * of {@link CommandFlag CommandFlags} in this {@link CommandFlagGroup}.</i>
	 * @param cFlag {@link CommandFlag} to add
	 * @throws IllegalStateException if adding the provided {@link CommandFlag} would cause processing ambiguity
	 *  due to name "collision" with one or more {@link CommandFlag CommandFlags} already added to this {@link CommandFlagGroup}.
	 *  <br><i>In technical term this undesired condition would be equivalent to</i><br>
	 *  &nbsp{@link CommandFlag cFlag}.{@link CommandFlag#getFlag() getFlag()}.{@link String#startsWith(String) startsWith}<code>(</code>{@link CommandFlag otherFlag}.{@link CommandFlag#getFlag() getFlag()}<code>)</code>
	 *  <br>&nbsp&nbsp <i>or</i><br>
	 *  &nbsp{@link CommandFlag otherFlag}.{@link CommandFlag#getFlag() getFlag()}.{@link String#startsWith(String) startsWith}<code>(</code>{@link CommandFlag cFlag}.{@link CommandFlag#getFlag() getFlag()}<code>)</code>
	 *  <br><i> - however the actual code does not use these methods.</i>
	 * @see #tryAddFlag(CommandFlag)
	 */
	public void addFlag(final CommandFlag cFlag)
	{
		final CommandFlag cf = tryAddFlag(cFlag);
		if (cf != null)
			throw new IllegalStateException("Trying to add CommandFlag that would collide with existing CommandFlag! {" +
						cFlag.toString() + "} and {" + cf.toString() + "}");
	}
	
	/**
	 * Tries to add a {@link CommandFlag} to this {@link CommandFlagGroup}. <br>
	 * <i>This is (close to) an <code>O(k)</code> operation, where <code>k</code> is the length of the {@link CommandFlag}
	 * to add. (In an extreme unlikely worst-case scenario this is <code>O(k+n)</code> where <code>n</code> is the number
	 * of {@link CommandFlag CommandFlags} in this {@link CommandFlagGroup}.</i>
	 * <p/>The add-operation will fail if adding the provided {@link CommandFlag} would cause processing ambiguity
	 *  due to name "collision" with one or more {@link CommandFlag CommandFlags} already added to this {@link CommandFlagGroup}.
	 *  <br><i>In technical term this undesired condition would be equivalent to</i><br>
	 *  &nbsp{@link CommandFlag cFlag}.{@link CommandFlag#getFlag() getFlag()}.{@link String#startsWith(String) startsWith}<code>(</code>{@link CommandFlag otherFlag}.{@link CommandFlag#getFlag() getFlag()}<code>)</code>
	 *  <br>&nbsp&nbsp <i>or</i><br>
	 *  &nbsp{@link CommandFlag otherFlag}.{@link CommandFlag#getFlag() getFlag()}.{@link String#startsWith(String) startsWith}<code>(</code>{@link CommandFlag cFlag}.{@link CommandFlag#getFlag() getFlag()}<code>)</code>
	 *  <br><i> - however the actual code does not use these methods.</i>
	 * @param cFlag {@link CommandFlag} to add
	 * @return <code>null</code> if the {@link CommandFlag} was added successfully, otherwise it returns (one of) the
	 *  {@link CommandFlag}(s) it "collided" with.
	 * @see #addFlag(CommandFlag)
	 */
	public CommandFlag tryAddFlag(final CommandFlag cFlag)
	{
		if (cFlag == null)
			throw new IllegalArgumentException("cFlag is null");
		
		FlagTreeNode c2f = root;
		final String name = cFlag.getFlag();
		for(int i = 0; i < name.length() - 1; ++i) //For creating branches
		{
			char c = name.charAt(i);
			final FlagTreeNode c2f2 = c2f.tryBranch(c, null);
			if (c2f2 == null) //Collision!
				return c2f.getFlagsDeep(false).get(0); //return any of the (possibly multiple) flags we are colliding with
			else
				c2f = c2f2;
		}
		//If we arrive here all necessary branches where created (or existed)
		//Create leaf
		final FlagTreeNode c2f2 = c2f.tryBranch(name.charAt(name.length() - 1), cFlag);
		if (c2f2 == null) //Collision!
			return c2f.getFlagsDeep(false).get(0); //return any of the (possibly multiple) flags we are colliding with
		else
			return null; //OK!
	}
	
	// =====================
	
	/**
	 * Removes a {@link CommandFlag} from this {@link CommandFlagGroup} based on name.
	 * @param name of the {@link CommandFlag} to remove
	 * @return Returns the removed {@link CommandFlag} or <code>null</code> if no such flag was found.
	 */
	public CommandFlag removeFlag(final String name)
	{
		if (name == null)
			return null;
		final FlagTreeNode c2f = name2Char2Flags.remove(name);
		if (c2f == null)
			return null;
		c2f.removeLeafBranch();
		return c2f.leaf;
	}
	
//	public boolean removeFlag(final CommandFlag cFlag) //Somewhat counter-intuitively this is actually the slower remove method
//	{
//		if(cFlag == null)
//			return false;
//		final Char2Flags c2f = name2Char2Flags.get(cFlag.flag);
//		if (c2f == null || c2f.leaf != cFlag)
//			return false;
//		name2Char2Flags.remove(cFlag.flag);
//		removeFlagInternal(c2f);
//		return true;
//	}
	
//	protected void removeFlagInternal(final Char2Flags c2Flag) //Must be non-null!
//	{
//		Char2Flags c2f = c2Flag;
//		ArrayList<Char2Flags> b;
//		for(b = c2f.stem.branches; b.size() == 1; b = c2f.stem.branches)
//			c2f = c2f.stem;
//		//b.remove(c2f); //Don't care about element order! This is a waste!
//		final int i = b.indexOf(c2f);
//		final int j = b.size() - 1;
//		if (i != j)
//			b.set(i, b.get(j));
//		b.remove(j);
//	}
	
	// =====================
	
	public static class FlagResult //TODO: javadoc...
	{
		private final FlagValues[] valuesArr;
		private final HashMap<String, ArrayList<FlagValues>> valuesMap; //null-key is non-flag arguments
		
		public FlagResult(final FlagValues[] valuesArr, final HashMap<String, ArrayList<FlagValues>> valuesMap)
		{
			//TODO: checks and throws
			this.valuesArr = valuesArr;
			this.valuesMap = valuesMap;
		}
		
		// -----
		
		public int size() {
			return valuesArr.length;
		}
		
		public boolean contains(final String flag) {
			return valuesMap.containsKey(flag);
		}
		
		public int flagCount(final String flag) {
			ArrayList<FlagValues> vArr = valuesMap.get(flag);
			return vArr == null ? 0 : vArr.size();
		}
	}
	
	//from inclusive, to exclusive //TODO: this whole method...
	public FlagResult processInput(final String[] args, final int fromIndex, final int toIndex) //TODO: javadoc
	{
		if (args == null || args.length == 0)
			return null;
		if (fromIndex < 0 || fromIndex >= toIndex || toIndex > args.length)
			throw new IllegalArgumentException("indexes out of bounds or fromIndex >= toIndex");
		
		final String NON_FLAG = null; //For easier to read code
		
		final ArrayList<FlagValues> vArr = new ArrayList<FlagValues>(toIndex - fromIndex);
		final HashMap<String, ArrayList<FlagValues>> vMap = new HashMap<String, ArrayList<FlagValues>>();
		
		FlagTreeNode fNode = root;
		CommandFlag cf = null;
		ArrayList<String> multiArr = null;
		int multiIndex = fromIndex;
		
		for (int i = fromIndex; true; ++i)
		{
			if(i == toIndex)
			{
				//Is multi-word cleanup needed?
				if (cf != null || fNode != root)
				{
					//Book all args from multiIndex(inclusive) -> toIndex(exclusive) as flag-less
					
					//TODO !!!
					
				}
				break;
			}
			
			final String arg = args[i];
			final int argL = arg.length();
			
			//Searching f-tree
			for (int k = 0; true; ++k)
			{
				final char c;
				final FlagTreeNode f;
				
				//run out of char
				if(k == argL)
				{
					//search for ' '
					c = ' ';
					f = fNode.getBranch(c);
					
					//run out of f-node
					if(f == null)
					{
						//TODO
					}
					
					//f-node found
					else
					{
						//continue on next arg
						fNode = f;
						break;
					}
				}
				// !(run out of char)
				else
				{
					//(search for char)
					c = arg.charAt(k);
					f = fNode.getBranch(c);
					
					//run out of f-node
					if(f == null)
					{
						
					}
				}
			}
		}
		
		return null; //TODO
	}
	
//	//from inclusive, to exclusive
//	public FlagResult processInput(final String[] args, final int fromIndex, final int toIndex) //TODO: javadoc
//	{
//		if (args == null || args.length == 0)
//			return null;
//		if (fromIndex < 0 || fromIndex >= toIndex || toIndex > args.length)
//			throw new IllegalArgumentException("indexes out of bounds or fromIndex >= toIndex");
//		
//		final String NON_FLAG = null; //For easier to read code
//		
//		final ArrayList<FlagValues> vArr = new ArrayList<>(toIndex - fromIndex);
//		final HashMap<String, ArrayList<FlagValues>> vMap = new HashMap<>();
//		
//		FlagTreeNode c2f = root;
//		CommandFlag cf = null;
//		ArrayList<String> multiArr = null;
//		
//		//TODO: post loop cleanup: if last argument(s) where part of a (possible) whitespace "multiword" flag,
//		// but never reached leaf-node, they doesn't qualify and should be added as non-flag arguments
//		// (so track index of last fully processed arg so we know how many trailing args didn't resolve into a flag/argument)
//		
//		for (int i = fromIndex; i < toIndex; ++i)
//		{
//			final String arg = args[i];
//			int k;
//			for (k = 0; true; ++k)
//			{
//				final char c;
//				if (k == arg.length() + 1)
//					break; //whitespace was part of a flag-branch: continue flag-search into next argument! (i.e. progress outer loop)
//				else if (k == arg.length())
//					c = ' ';
//				else
//					c = arg.charAt(k);
//				c2f = c2f.getBranch(c);
//				if (c2f == null)
//				{
//					//arg is not a flag
//					if (cf == null)
//					{
//						//this is argument doesn't belong to a flag
//						if (multiArr == null) //TODO: move this to flag-detection code
//						{
//							//this is the first non-flag argument (in a row)
//							//create non-flag flag-value:
//							multiArr = new ArrayList<>();
//							multiArr.add(arg);
//							final FlagValues f = new FlagValues(multiArr);
//							vArr.add(f);
//							//add to map:
//							ArrayList<FlagValues> fArr = vMap.get(NON_FLAG);
//							if (fArr == null)
//							{
//								//this is the very very first non-flag value
//								fArr = new ArrayList<>(2);
//								vMap.put(NON_FLAG, fArr);
//							}
//							fArr.add(f);
//							break;
//						}
//						else
//						{
//							//this is a subsequent random argument (in a row)
//							//add to existing sequence:
//							multiArr.add(arg);
//							break;
//						}
//					}
//					else
//					{
//						//this argument belong to a whitespace-type flag
//						if (multiArr == null)
//						{
//							//this is the first argument of this whitespace (started) flag
//							//create flag-value:
//							multiArr = new ArrayList<>();
//							final FlagValues f = cf.new ValueInner(multiArr);
//							vArr.add(f);
//							//add to map:
//							ArrayList<FlagValues> fArr = vMap.get(NON_FLAG);
//							if (fArr == null)
//							{
//								//this is the very very first value for this flag
//								fArr = new ArrayList<>(2);
//								vMap.put(NON_FLAG, fArr);
//							}
//							fArr.add(f);
//							
//							//add value(s):
//							final Character sep = cf.separator;
//							if (sep == null || sep.charValue() == ' ')
//							{
//								//is single argument, or whitespace-separated whitespace flag
//								multiArr.add(arg);
//							}
//							else
//							{
//								//process argument according to separator
//								final char sep2 = sep.charValue();
//								//TODO
//							}
//						}
//					}
//				}
//				else if(c2f.leaf != null)
//				{
//					//arg is a flag
//					
//					//TODO: check if prev arg was a whitespace (started) flag that didn't get any values - handle that...
//					
//					cf = c2f.leaf;
//					//TODO
//				}
//			}
//		}
//		
//		return null; //TODO
//	}
}
