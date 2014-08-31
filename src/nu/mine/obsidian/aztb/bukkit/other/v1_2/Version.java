package nu.mine.obsidian.aztb.bukkit.other.v1_2;

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

import org.bukkit.Server;

/**
 * Greatly simplifies {@link Server} version checking tasks (Bukkit/Spigot/Cauldron).
 * <p>
 * <b>There is no public constructor - use {@link #getVersion(Server)} instead!</b>
 * <p>
 * To get the version as numbers, e.g. from "1.7.2-R0.3", use<br>{@link #getMcMajor()} ,
 *  {@link #getMcMinor()} , {@link #getMcRevision()} , {@link #getReMajor()} ,
 *  {@link #getReMinor()}<br> - in that order.
 * <p>
 * To get all version numbers in one easy to store/compare object, use {@link #getVersionNumbers()}
 * <br>See {@link VersionNumber} for more info.
 * <p>
 * To get the raw server version string use {@link #getServerVersion()}
 * <br>Example output: <code>"git-Bukkit-1.7.2-R0.3-b3020jnks (MC: 1.7.2)"</code>
 * <br>(This is the same as calling {@link Server#getVersion()})
 * <p>
 * To get the server software string use {@link #getServerSoftware()}
 * <br>Example output: <code>"Spigot"</code>
 * <br>(This is the second part of the server version string)
 * <p>
 * To get the raw server API version string use {@link #getServerAPIVersion()}
 * <br>Example output: <code>"1.7.2-R0.3"</code>
 * <br>(This is the same as calling {@link Server#getBukkitVersion()})
 * <p>
 * To get the build name use {@link #getServerBuildName()}
 * <br>Example output: <code>"b3020jnks"</code>
 * <br>(This is the second last part of the server version string)
 * <p>
 * To get the build number use {@link #getServerBuildNumber()}
 * <br>Example output: <code>3020</code>
 * <br>(This is the last set of numbers inside the build-name)
 * <p>
 * To get the server type use {@link #getServerType()}
 * <br>Example output: <code>"Cauldron-MCPC-Plus"</code>
 * <br>(This is the same as calling {@link Server#getName()})
 * <p>
 * To get a nice summary use {@link #toString()}
 * <br>Example output:
 * <br><code>
 * "CraftBukkit server git-Spigot-1529 (MC: 1.7.9) (Implementing API version 1.7.9-R0.3-SNAPSHOT)"
 * </code>
 * <p>
 * To easily compare versions, e.g. for version requirement checks in a plugin, use the compare-methods
 * <br> {@link #compareTo(VersionNumber)}
 * <br> {@link #compareTo(int, int, int)}
 * <br> {@link #compareTo(int, int, int, int, int)}
 * <br> {@link #compareTo(int, int, int, int, int, int)}
 * 
 * @see Version#getVersion(Server)
 * @author AnorZaken
 * @version 1.2
 */
public class Version //IMMUTABLE!
{
	// "The standard GNU version numbering scheme is major.minor.revision"
	
	/**
	 * An immutable class containing server version numbers.
	 * 
	 * @see Version#compareTo(VersionNumber)
	 * @author AnorZaken
	 * @version 1.0
	 */
	public static class VersionNumber //IMMUTABLE!
	{
		protected final int mc1, mc2, mc3, r1, r2, b;
		
		/**
		 * Creates a {@link VersionNumber} instance usable for {@link Version} comparison.
		 * @param major in 1.7.2-R0.3 this would be the 1
		 * @param minor in 1.7.2-R0.3 this would be the 7
		 * @param revision in 1.7.2-R0.3 this would be the 2
		 * @param reMajor in 1.7.2-R0.3 this would be the 0
		 * @param reMinor in 1.7.2-R0.3 this would be the 3
		 * @param build the server build number
		 */
		public VersionNumber(final int major, final int minor, final int revision,
				final int reMajor, final int reMinor, final int build)
		{
			this.mc1 = major;
			this.mc2 = minor;
			this.mc3 = revision;
			this.r1 = reMajor;
			this.r2 = reMinor;
			this.b = build;
		}
		
		/**
		 * Creates a {@link VersionNumber} instance usable for {@link Version} comparison.
		 * <p>VersionNumbers created with this constructor ignores build number (gets set to -1).
		 * @param major in 1.7.2-R0.3 this would be the 1
		 * @param minor in 1.7.2-R0.3 this would be the 7
		 * @param revision in 1.7.2-R0.3 this would be the 2
		 * @param reMajor in 1.7.2-R0.3 this would be the 0
		 * @param reMinor in 1.7.2-R0.3 this would be the 3
		 */
		public VersionNumber(final int major, final int minor, final int revision, final int reMajor, final int reMinor) {
			this(major, minor, revision, reMajor, reMinor, -1);
		}
		
		/**
		 * Creates a {@link VersionNumber} instance usable for {@link Version} comparison.
		 * <p>VersionNumbers created with this constructor ignores 'R' and build numbers (gets set to -1).
		 * @param major in 1.7.2 this would be the 1
		 * @param minor in 1.7.2 this would be the 7
		 * @param revision in 1.7.2 this would be the 2
		 */
		public VersionNumber(final int major, final int minor, final int revision) {
			this(major, minor, revision, -1, -1, -1);
		}
		
		// ---
		
		/**
		 * Get the major version. (In 1.7.2 this would be the 1)
		 */
		public int getMcMajor() { return mc1; }
		
		/**
		 * Get the minor version. (In 1.7.2 this would be the 7)
		 */
		public int getMcMinor() { return mc2; }
		
		/**
		 * Get the revision number. (In 1.7.2 this would be the 2)
		 */
		public int getMcRevision() { return mc3; }
		
		/**
		 * Get the R-major number. (In 1.7.2-R0.3 this would be the 0)
		 * @return the R-major number, or -1 if unknown
		 */
		public int getReMajor() { return r1; }
		
		/**
		 * Get the R-minor number. (In 1.7.2-R0.3 this would be the 3)
		 * @return the R-minor number, or -1 if unknown
		 */
		public int getReMinor() { return r2; }
		
		/**
		 * Get the server build number. 
		 * <br>(This is the last set of numbers inside the build-name)
		 * @return the server build number, or -1 if unknown
		 */
		public int getServerBuildNumber() { return b; }
		
		// -----
		
		/**
		 * Compares this {@link VersionNumber} to a version described with <code>int</code> arguments.
		 * <p><i>Warning: If the R or build numbers are unknown they will not be checked!</i>
		 * 
		 * @param mcMajor Example: in 1.7.2-R0.3 - this would be the 1
		 * @param mcMinor Example: in 1.7.2-R0.3 - this would be the 7
		 * @param mcRevision Example: in 1.7.2-R0.3 - this would be the 2
		 * @param reMajor Example: in 1.7.2-R0.3 - this would be the 0
		 *  <br>(use -1 to ignore this - <i>will ignore {@code reMinor} and {@code build} too!</i>)
		 * @param reMinor Example: in 1.7.2-R0.3 - this would be the 3
		 *  <br>(use -1 to ignore this - <i>will ignore {@code build} too!</i>)
		 * @param build the server build version
		 *  <br>(use -1 to ignore this)
		 * @return the value 0 if this {@link VersionNumber} matches the arguments;
		 *  a value less than 0 if this {@link VersionNumber} describes something older than the arguments;
		 *  and a value greater than 0 if this {@link VersionNumber} describes something newer than the arguments
		 */
		public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision
				, final int reMajor, final int reMinor, final int build)
		{
			if(mcMajor == this.mc1) {
				if(mcMinor == this.mc2) {
					if(mcRevision == this.mc3) {
						if(reMajor == -1 || this.r1 == -1)
							return 0;
						else if(reMajor == this.r1) {
							if(reMinor == -1 || this.r2 == -1)
								return 0;
							else if(reMinor == this.r2)
							{
								if (build == -1 || this.b == -1 || build == this.b)
									return 0;
								else
									return this.b < build ? -1 : 1;
							} else
								return this.r2 < reMinor ? -1 : 1;
						} else
							return this.r1 < reMajor ? -1 : 1;
					} else
						return this.mc3 < mcRevision ? -1 : 1;
				} else
					return this.mc2 < mcMinor ? -1 : 1;
			} else
				return this.mc1 < mcMajor ? -1 : 1;
		}
		
		/**
		 * Compares this {@link VersionNumber} to a version described with <code>int</code> arguments.
		 * <p><i>Warning: If the R numbers are unknown they will not be checked!</i>
		 * 
		 * @param mcMajor Example: in 1.7.2-R0.3 - this would be the 1
		 * @param mcMinor Example: in 1.7.2-R0.3 - this would be the 7
		 * @param mcRevision Example: in 1.7.2-R0.3 - this would be the 2
		 * @param reMajor Example: in 1.7.2-R0.3 - this would be the 0
		 * @param reMinor Example: in 1.7.2-R0.3 - this would be the 3
		 * @return the value 0 if this {@link VersionNumber} matches the arguments;
		 *  a value less than 0 if this {@link VersionNumber} describes something older than the arguments;
		 *  and a value greater than 0 if this {@link VersionNumber} describes something newer than the arguments
		 */
		public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision
				, final int reMajor, final int reMinor) {
			return compareTo(mcMajor, mcMinor, mcRevision, reMajor, reMinor, -1);
		}
		
		/**
		 * Compares this {@link VersionNumber} to a version described with <code>int</code> arguments.
		 * 
		 * @param mcMajor Example: 1.7.2 - this would be the 1
		 * @param mcMinor Example: 1.7.2 - this would be the 7
		 * @param mcRevision Example: 1.7.2 - this would be the 2
		 * @return the value 0 if this {@link VersionNumber} matches the arguments;
		 *  a value less than 0 if this {@link VersionNumber} describes something older than the arguments;
		 *  and a value greater than 0 if this {@link VersionNumber} describes something newer than the arguments
		 */
		public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision) {
			return compareTo(mcMajor, mcMinor, mcRevision, -1, -1, -1);
		}
		
		/**
		 * Compares this {@link VersionNumber} against another {@link VersionNumber}.
		 * <p><i>Warning: If the R or build numbers are unknown they will not be checked!</i>
		 * 
		 * @param other a {@link VersionNumber} to compare against
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @return the value 0 if this {@link VersionNumber} matches the {@link VersionNumber} argument;
		 *  a value less than 0 if this {@link VersionNumber} describes an older version;
		 *  and a value greater than 0 if this {@link VersionNumber} describes a newer version
		 */
		public int compareTo(final VersionNumber other) {
			return compareTo(other.mc1, other.mc2, other.mc3,
					other.r1, other.r2, other.b);
		}
		
		/**
		 * Checks this {@link VersionNumber} against another for equality.
		 * <p><i>Unlike {@link #compareTo(VersionNumber)} this method requires <u>all</u>
		 * values to be equal - even unknown ones!
		 * <br>(This means that if a number is unknown in one of the VersionNumber-instances
		 * it must be unknown in both for this method to return <code>true</code>.)</i>
		 * 
		 * @param other {@link VersionNumber} to check against
		 * @return <code>true</code> if the argument is non-<code>null</code> and equal to
		 *  this {@link VersionNumber}
		 */
		public boolean equals(final VersionNumber other)
		{
			return other == this || (other != null
					&& this.mc1 == other.mc1 && this.mc2 == other.mc2 && this.mc3 == other.mc3
					&& this.r1 == other.r1 && this.r2 == other.r2 && this.b == other.b);
		}
	}
	
	// -----
	
	/**
	 * An immutable class that encapsulates and simplifies version checking.
	 * <p>
	 * Use the factory 'create...(...)'-methods to create different {@link VersionRequirement VersionRequirements}.
	 * 
	 * @see #createExactRequirement(VersionNumber)
	 * @see #createMaximumRequirement(VersionNumber)
	 * @see #createMinimumRequirement(VersionNumber)
	 * @see #createRangeRequirement(VersionNumber, VersionNumber)
	 * 
	 * @author AnorZaken
	 * @version 1.0
	 */
	public static abstract class VersionRequirement //IMMUTABLE!
	{
//		protected transient Version cachedVersion = null; //Performance optimization
//		protected transient boolean cachedResult = false; //Performance optimization
		
		
		protected static class MinRequirement extends VersionRequirement
		{
			protected final VersionNumber vn;
			
			protected MinRequirement(final VersionNumber vn) {
				this.vn = vn;
			}
			
			@Override
			public boolean meetsRequirements(Version version)
			{
//				final Boolean optimized = meetsRequirementsOptimizer(version); //Performance optimization
//				if (optimized != null)
//					return optimized;
//				return meetsRequirementsOptimizer(version, version.compareTo(vn) >= 0);
				return version.compareTo(vn) >= 0;
			}
		}
		// ---
		protected static class MaxRequirement extends VersionRequirement
		{
			protected final VersionNumber vn;
			
			protected MaxRequirement(final VersionNumber vn) {
				this.vn = vn;
			}
			
			@Override
			public boolean meetsRequirements(Version version)
			{
//				final Boolean optimized = meetsRequirementsOptimizer(version); //Performance optimization
//				if (optimized != null)
//					return optimized;
//				return meetsRequirementsOptimizer(version, version.compareTo(vn) <= 0);
				return version.compareTo(vn) <= 0;
			}
		}
		// ---
		protected static class ExactRequirement extends VersionRequirement
		{
			protected final VersionNumber vn;
			
			protected ExactRequirement(final VersionNumber vn) {
				this.vn = vn;
			}
			
			@Override
			public boolean meetsRequirements(Version version)
			{
//				final Boolean optimized = meetsRequirementsOptimizer(version); //Performance optimization
//				if (optimized != null)
//					return optimized;
//				return meetsRequirementsOptimizer(version, version.compareTo(vn) == 0);
				return version.compareTo(vn) == 0;
			}
		}
		// ---
		protected static class RangeRequirement extends VersionRequirement
		{
			protected final VersionNumber vnMin, vnMax;
			
			protected RangeRequirement(final VersionNumber vnMin, final VersionNumber vnMax) {
				this.vnMin = vnMin;
				this.vnMax = vnMax;
			}
			
			@Override
			public boolean meetsRequirements(Version version)
			{
//				final Boolean optimized = meetsRequirementsOptimizer(version); //Performance optimization
//				if (optimized != null)
//					return optimized;
//				return meetsRequirementsOptimizer(version, version.compareTo(vnMin) >= 0 && version.compareTo(vnMax) <= 0);
				return version.compareTo(vnMin) >= 0 && version.compareTo(vnMax) <= 0;
			}
		}
		// ---
		protected static class STReqDecorator extends VersionRequirement //Software+Type Decorator
		{
			protected transient Version cachedVersion = null; //Performance optimization
			protected transient Boolean cachedResult = null; //Performance optimization
			
			protected final VersionRequirement vr;
			protected final String software, type;
			
			protected STReqDecorator(final VersionRequirement vr, final String software, final String type) {
				this.vr = vr;
				this.software = software;
				this.type = type;
			}
			
			@Override
			public boolean meetsRequirements(Version version)
			{
				final Boolean optimized = meetsRequirementsOptimizer(version); //Performance optimization
				if (optimized != null)
					return optimized;
				return meetsRequirementsOptimizer(version, vr.meetsRequirements(version)
						&& (software == null || software.equals(version.software))
						&& (type == null || type.equals(version.type)));
			}
			
			/**
			 * Updates the cached result - for internal use by {@link #meetsRequirements(Version)}.
			 * @param version the version to cache
			 * @param result the result to cache
			 * @return the result parameter
			 */
			protected synchronized final boolean meetsRequirementsOptimizer(final Version version, final boolean result)
			{
				this.cachedVersion = version;
				this.cachedResult = result;
				return result;
			}
			
			/**
			 * Retrieves the cached result for the version argument (if it exists)
			 * - for internal use by {@link #meetsRequirements(Version)}.
			 * @param version {@link Version} to find cached result for
			 * @return a {@link Boolean} with the cached result, or <code>null</code> if not found
			 */
			protected synchronized final Boolean meetsRequirementsOptimizer(final Version version)
			{
				return (version != null && version == cachedVersion) ? cachedResult : null;
			}
		}
		
		// -----
		
		//Factory methods
		
		/**
		 * Creates a minimum version requirement.
		 * 
		 * @param minimumVersion {@link VersionNumber} of the minimum version required (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @param software required software (case-sensitive) (can be <code>null</code> to ignore)
		 * @param type required server type (case-sensitive) (can be <code>null</code> to ignore)
		 * @return a {@link VersionRequirement} or <code>null</code> if the {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber)
		 * @see #createMaximumRequirement(VersionNumber)
		 * @see #createExactRequirement(VersionNumber)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber)
		 */
		public static VersionRequirement createMinimumRequirement(final VersionNumber minimumVersion,
				final String software, final String type) {
			return minimumVersion == null ? null : new STReqDecorator(new MinRequirement(minimumVersion), software, type);
		}
		
		/**
		 * Creates a minimum version requirement.
		 * 
		 * @param minimumVersion {@link VersionNumber} of the minimum version required (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @return a {@link VersionRequirement} or <code>null</code> if the {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber, String, String)
		 * @see #createMaximumRequirement(VersionNumber, String, String)
		 * @see #createExactRequirement(VersionNumber, String, String)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber, String, String)
		 */
		public static VersionRequirement createMinimumRequirement(final VersionNumber minimumVersion) {
			return minimumVersion == null ? null : new MinRequirement(minimumVersion);
		}
		
		/**
		 * Creates a maximum version requirement.
		 * 
		 * @param maximumVersion {@link VersionNumber} of the required maximum version (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @param software required software (case-sensitive) (can be <code>null</code> to ignore)
		 * @param type required server type (case-sensitive) (can be <code>null</code> to ignore)
		 * @return a {@link VersionRequirement} or <code>null</code> if the {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber)
		 * @see #createMaximumRequirement(VersionNumber)
		 * @see #createExactRequirement(VersionNumber)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber)
		 */
		public static VersionRequirement createMaximumRequirement(final VersionNumber maximumVersion,
				final String software, final String type) {
			return maximumVersion == null ? null : new STReqDecorator(new MaxRequirement(maximumVersion), software, type);
		}
		
		/**
		 * Creates a maximum version requirement.
		 * 
		 * @param maximumVersion {@link VersionNumber} of the required maximum version (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @return a {@link VersionRequirement} or <code>null</code> if the {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber, String, String)
		 * @see #createMaximumRequirement(VersionNumber, String, String)
		 * @see #createExactRequirement(VersionNumber, String, String)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber, String, String)
		 */
		public static VersionRequirement createMaximumRequirement(final VersionNumber maximumVersion) {
			return maximumVersion == null ? null : new MaxRequirement(maximumVersion);
		}
		
		/**
		 * Creates an exact version requirement.
		 * 
		 * @param exactVersion {@link VersionNumber} of the required version (exact)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @param software required software (case-sensitive) (can be <code>null</code> to ignore)
		 * @param type required server type (case-sensitive) (can be <code>null</code> to ignore)
		 * @return a {@link VersionRequirement} or <code>null</code> if the {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber)
		 * @see #createMaximumRequirement(VersionNumber)
		 * @see #createExactRequirement(VersionNumber)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber)
		 */
		public static VersionRequirement createExactRequirement(final VersionNumber exactVersion,
				final String software, final String type) {
			return exactVersion == null ? null : new STReqDecorator(new ExactRequirement(exactVersion), software, type);
		}
		
		/**
		 * Creates an exact version requirement.
		 * 
		 * @param exactVersion {@link VersionNumber} of the required version (exact)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @return a {@link VersionRequirement} or <code>null</code> if the {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber, String, String)
		 * @see #createMaximumRequirement(VersionNumber, String, String)
		 * @see #createExactRequirement(VersionNumber, String, String)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber, String, String)
		 */
		public static VersionRequirement createExactRequirement(final VersionNumber exactVersion) {
			return exactVersion == null ? null : new ExactRequirement(exactVersion);
		}
		
		/**
		 * Creates a version range requirement.
		 * 
		 * @param minVersion {@link VersionNumber} of the minimum required version (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @param maxVersion {@link VersionNumber} of the required maximum version (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @param software required software (case-sensitive) (can be <code>null</code> to ignore)
		 * @param type required server type (case-sensitive) (can be <code>null</code> to ignore)
		 * @return a {@link VersionRequirement} or <code>null</code> if any {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber)
		 * @see #createMaximumRequirement(VersionNumber)
		 * @see #createExactRequirement(VersionNumber)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber)
		 */
		public static VersionRequirement createRangeRequirement(final VersionNumber minVersion, final VersionNumber maxVersion,
				final String software, final String type) {
			return minVersion == null || maxVersion == null ? null :
				new STReqDecorator(new RangeRequirement(minVersion, maxVersion), software, type);
		}
		
		/**
		 * Creates a version range requirement.
		 * 
		 * @param minVersion {@link VersionNumber} of the minimum required version (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @param maxVersion {@link VersionNumber} of the required maximum version (inclusive)
		 *  <i>(Must be non-<code>null</code>!)</i>
		 * @return a {@link VersionRequirement} or <code>null</code> if any {@link VersionNumber} is <code>null</code>
		 * @see #createMinimumRequirement(VersionNumber, String, String)
		 * @see #createMaximumRequirement(VersionNumber, String, String)
		 * @see #createExactRequirement(VersionNumber, String, String)
		 * @see #createRangeRequirement(VersionNumber, VersionNumber, String, String)
		 */
		public static VersionRequirement createRangeRequirement(final VersionNumber minVersion, final VersionNumber maxVersion) {
			return minVersion == null || maxVersion == null ? null : new RangeRequirement(minVersion, maxVersion);
		}
		
		// == = ==
		
		/**
		 * Checks if the provided {@link Version} meets the requirements of this {@link VersionRequirement}.
		 * <p>
		 * <i>This method caches the last checked {@link Version} object and result for performance.
		 * <br>This method is thread safe!</i>
		 * 
		 * @param version {@link Version} to check requirements against <i>(must be non-<code>null</code>!)</i>
		 * @return <code>true</code> if the {@link Version} meets this requirement
		 */
		public abstract boolean meetsRequirements(final Version version);
//		{
//			{ //Performance optimization
//				final Boolean optimized = meetsRequirementsOptimizer(version);
//				if (optimized != null)
//					return optimized;
//			}
//			
//			if (vnMin != null) {
//				final int i = version.compareTo(vnMin);
//				if (i < 0 || (i != 0 && vnMax == vnMin)) //second part is the exact match case
//					return meetsRequirementsOptimizer(version, false);
//			}
//			if (vnMax != null) {
//				if (version.compareTo(vnMax) > 0)
//					return meetsRequirementsOptimizer(version, false);
//			}
//			if (software != null && !software.equals(version.getServerSoftware()))
//				return meetsRequirementsOptimizer(version, false);
//			if (type != null && !type.equals(version.getServerType()))
//				return meetsRequirementsOptimizer(version, false);
//			
//			return meetsRequirementsOptimizer(version, true);
//		}
		
//		/**
//		 * Updates the cached result - for internal use by {@link #meetsRequirements(Version)}.
//		 * @param version the version to cache
//		 * @param result the result to cache
//		 * @return the result parameter
//		 */
//		protected synchronized final boolean meetsRequirementsOptimizer(final Version version, final boolean result)
//		{
//			this.cachedVersion = version;
//			this.cachedResult = result;
//			return result;
//		}
//		
//		/**
//		 * Retrieves the cached result for the version argument (if it exists)
//		 * - for internal use by {@link #meetsRequirements(Version)}.
//		 * @param version {@link Version} to find cached result for
//		 * @return a {@link Boolean} with the cached result, or <code>null</code> if not found
//		 */
//		protected synchronized final Boolean meetsRequirementsOptimizer(final Version version)
//		{
//			return (version != null && version == cachedVersion) ? cachedResult : null;
//		}
	}
	
	
	// -----
	
	protected final String version, api, software, type, build;
	protected final VersionNumber vNumbers;
	
	/**
	 * Version constructor - only for internal use by {@link Version#createVersion(String, String, String)}.
	 */
	protected Version(final VersionNumber versionNumbers,
			final String version, final String api, final String type, final String software, final String build)
	{
		this.vNumbers = versionNumbers;
		this.version = version;
//		this.mc1 = m1;
//		this.mc2 = m2;
//		this.mc3 = m3;
//		this.r1 = r1;
//		this.r2 = r2;
//		this.b = b;
		this.api = api;
		this.software = software;
		this.type = type;
		this.build = build;
	}
	
	/**
	 * Get the raw server version string.
	 * <br>(This is the same as calling {@link Server#getVersion()})
	 */
	public String getServerVersion() { return version; }
	
	/**
	 * Get the major version. (In 1.7.2 this would be the 1)
	 */
	public int getMcMajor() { return vNumbers.mc1; }
	
	/**
	 * Get the minor version. (In 1.7.2 this would be the 7)
	 */
	public int getMcMinor() { return vNumbers.mc2; }
	
	/**
	 * Get the revision number. (In 1.7.2 this would be the 2)
	 */
	public int getMcRevision() { return vNumbers.mc3; }
	
	/**
	 * Get the R-major number. (In 1.7.2-R0.3 this would be the 0)
	 * @return the R-major number, or -1 if unknown
	 */
	public int getReMajor() { return vNumbers.r1; }
	
	/**
	 * Get the R-minor number. (In 1.7.2-R0.3 this would be the 3)
	 * @return the R-minor number, or -1 if unknown
	 */
	public int getReMinor() { return vNumbers.r2; }
	
	/**
	 * Get the server software name.
	 * <br>Example output: <code>"Spigot"</code>
	 * <br>(This is the second part of the server version string)
	 * @return the server software, or <code>null</code> if failed to extract software information
	 */
	public String getServerSoftware() { return software; }
	
	/**
	 * Get the raw server API version string.
	 * <br>Example output: <code>"1.7.2-R0.3"</code>
	 * <br>(This is the same as calling {@link Server#getBukkitVersion()})
	 */
	public String getServerAPIVersion() { return api; }
	
	/**
	 * Get the server type.
	 * <br>Example output: <code>"Cauldron-MCPC-Plus"</code>
	 * <br>(This is the same as calling {@link Server#getName()})
	 */
	public String getServerType() { return type; }
	
	/**
	 * Get the server build name.
	 * <br>Example output: <code>"b3020jnks"</code>
	 * <br>(This is the second last part of the server version string)
	 * @return the build name, or <code>null</code> if failed to extract build information
	 */
	public String getServerBuildName() { return build; }
	
	/**
	 * Get the server build number. 
	 * <br>Example output: <code>3020</code>
	 * <br>(This is the last set of numbers inside the build-name)
	 * @return the build number, or -1 if build number unknown
	 */
	public int getServerBuildNumber() { return vNumbers.b; }
	
	/**
	 * Get the {@link VersionNumber} object corresponding to this {@link Version}.
	 */
	public VersionNumber getVersionNumbers() { return vNumbers; }
	
	// -----
	
	/**
	 * Compares this {@link Version} to a version described with <code>int</code> arguments.
	 * <p><i>Warning: If the R or build numbers are unknown they will not be checked!</i>
	 * @param mcMajor Example: in 1.7.2-R0.3 - this would be the 1
	 * @param mcMinor Example: in 1.7.2-R0.3 - this would be the 7
	 * @param mcRevision Example: in 1.7.2-R0.3 - this would be the 2
	 * @param reMajor Example: in 1.7.2-R0.3 - this would be the 0
	 *  <br>(use -1 to ignore this - <i>will ignore {@code reMinor} and {@code build} too!</i>)
	 * @param reMinor Example: in 1.7.2-R0.3 - this would be the 3
	 *  <br>(use -1 to ignore this - <i>will ignore {@code build} too!</i>)
	 * @param build the server build version
	 *  <br>(use -1 to ignore this)
	 * @return the value 0 if this {@link Version} matches the arguments;
	 *  a value less than 0 if this {@link Version} describes something older than the arguments;
	 *  and a value greater than 0 if this {@link Version} describes something newer than the arguments
	 */
	public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision
			, final int reMajor, final int reMinor, final int build) {
		return vNumbers.compareTo(mcMajor, mcMinor, mcRevision, reMajor, reMinor, build);
	}
	
	/**
	 * Compares this {@link Version} to a version described with <code>int</code> arguments.
	 * <p><i>Warning: If the R numbers are unknown they will not be checked!</i>
	 * @param mcMajor Example: in 1.7.2-R0.3 - this would be the 1
	 * @param mcMinor Example: in 1.7.2-R0.3 - this would be the 7
	 * @param mcRevision Example: in 1.7.2-R0.3 - this would be the 2
	 * @param reMajor Example: in 1.7.2-R0.3 - this would be the 0
	 * @param reMinor Example: in 1.7.2-R0.3 - this would be the 3
	 * @return the value 0 if this {@link Version} matches the arguments;
	 *  a value less than 0 if this {@link Version} describes something older than the arguments;
	 *  and a value greater than 0 if this {@link Version} describes something newer than the arguments
	 */
	public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision
			, final int reMajor, final int reMinor) {
		return vNumbers.compareTo(mcMajor, mcMinor, mcRevision, reMajor, reMinor, -1);
	}
	
	/**
	 * Compares this {@link Version} to a version described with <code>int</code> arguments.
	 * @param mcMajor Example: 1.7.2 - this would be the 1
	 * @param mcMinor Example: 1.7.2 - this would be the 7
	 * @param mcRevision Example: 1.7.2 - this would be the 2
	 * @return the value 0 if this {@link Version} matches the arguments;
	 *  a value less than 0 if this {@link Version} describes something older than the arguments;
	 *  and a value greater than 0 if this {@link Version} describes something newer than the arguments
	 */
	public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision) {
		return vNumbers.compareTo(mcMajor, mcMinor, mcRevision, -1, -1, -1);
	}
	
	/**
	 * Compares this {@link Version} against a {@link VersionNumber}.
	 * <p><i>Warning: If the R or build numbers are unknown they will not be checked!</i>
	 * @param versionNumber a {@link VersionNumber} to compare against
	 *  <i>(Must be non-<code>null</code>!)</i>
	 * @return the value 0 if this {@link Version} matches the {@link VersionNumber} argument;
	 *  a value less than 0 if this {@link Version} describes an older version;
	 *  and a value greater than 0 if this {@link Version} describes a newer version
	 */
	public int compareTo(final VersionNumber versionNumber) {
		return vNumbers.compareTo(versionNumber);
	}
	
	/**
	 * Checks if this {@link Version} meets the requirements of the provided {@link VersionRequirement}.
	 * @param requirement {@link VersionRequirement} to check against
	 * @return <code>true</code> if {@code requirement == null} or this {@link Version} meets the requirements
	 */
	public boolean meetsRequirements(final VersionRequirement requirement) {
		return requirement == null || requirement.meetsRequirements(this);
	}
	
	// -----
	
	/**
	 * Get a String describing the type, version and api-verison.
	 */
	@Override
	public String toString() {
		return type + " server " + version + " (Implementing API version " + api + ")";
	}
	
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Version && version.equals(((Version)obj).version));
	}
	
	/**
	 * Checks two versions for equality.
	 * @param version other {@link Version} to compare against this one 
	 * @return <code>true</code> if they both describe the same version
	 */
	public boolean equals(final Version version) {
		return this == version || (version != null && this.version.equals(version.version));
	}
	
	@Override
	public int hashCode() {
		return version.hashCode();
	}
	
	// ------------------
	
	protected static Version cached = null; //Performance optimization
	
	/**
	 * Get {@link Version} information about a {@link Server}. 
	 * <p>
	 * <i>This method caches the latest {@link Version} object and reuses it if its server-version
	 * string matches the version string of the provided {@link Server} argument.
	 * <br>This method is thread-safe.</i>
	 * 
	 * @param server {@link Server} to get version information about
	 * @return a {@link Version} object or <code>null</code> if the method failed to extract
	 *  MC-major, MC-minor or MC-revision numbers
	 * @throws IllegalArgumentException if {@code server} is <code>null</code>
	 */
	public static Version getVersion(final Server server)
	{
		if (server == null)
			throw new IllegalArgumentException("server == null");
		final String versionServer = server.getVersion(); //Example: "git-Bukkit-1.7.2-R0.3-b3020jnks (MC: 1.7.2)"
		final Version instance = Version.cached;
		if (instance != null && instance.version.equals(versionServer)) //Performance optimization
			return instance;
		final String versionAPI = server.getBukkitVersion(); //Example: "1.7.2-R0.3"
		final String type = server.getName(); //Example: "CraftBukkit"
		return Version.cached = createVersion(versionServer, versionAPI, type);
	}
	
	/**
	 * Creates a {@link Version} object from version strings - only for internal use by
	 * {@link Version#getVersion(Server)}.
	 * @param versionServer the server version ( {@link Server#getVersion()} )
	 * @param versionAPI the Bukkit-API version ( {@link Server#getBukkitVersion()} )
	 * @param serverType the server software used
	 * @return a {@link Version} object or <code>null</code> if the method failed to extract
	 *  MC-version information. (Other extraction fails will still yield a {@link Version}
	 *  object but some information might be missing - refer to the documentation for each of
	 *  the get-methods for more information!)
	 */
	protected static Version createVersion(final String versionServer, final String versionAPI, final String serverType)
	{
		final int mMaj, mMin, mRev;
		final int rMaj, rMin;
		
		{
			final String[] sArr1 = versionAPI.split("-", 3);
			
			try
			{
				final String[] sArr2 = sArr1[0].split("\\.", 4);
				if (sArr2.length == 3)
				{
					mMaj = Integer.parseInt(sArr2[0]);
					mMin = Integer.parseInt(sArr2[1]);
					mRev = Integer.parseInt(sArr2[2]);
				}
				else
					return null;
			}
			catch(NumberFormatException e) { return null; }
			
			{
				int r1 = -1, r2 = -1;
				if (sArr1.length != 1)
				try
				{
					final String[] sArr2 = sArr1[1].split("\\D+", 3);
					if (sArr2.length == 3)
					{
						r1 = Integer.parseInt(sArr2[1]);
						r2 = Integer.parseInt(sArr2[2]);
					}
				}
				catch(NumberFormatException e) { r1 = -1; r2 = -1; }
				rMaj = r1;
				rMin = r2;
			}
		}
		
		final String build;
		final int b;
		{
			final String[] buildArr = versionServer.split("(.+-)|( \\(MC: .+)", 3);
			if (buildArr.length == 3)
			{
				build = buildArr[1];
				int b0 = -1;
				try
				{
					final String[] sArr2 = build.split("\\D+", 0);
					if (sArr2.length == 1)
						b0 = Integer.parseInt(build);
					else if (sArr2.length == 2)
						b0 = Integer.parseInt(sArr2[1]);
					else
						b0 = Integer.parseInt(sArr2[sArr2.length-1]);
				}
				catch(NumberFormatException e) { b0 = -1; }
				b = b0;
			}
			else
			{
				build = null;
				b = -1;
			}
		}
		
		final String software;
		{
			int i = versionServer.indexOf('-');
			if (i != -1)
			{
				int j = versionServer.indexOf('-', i+3);
				if (j == -1)
					software = null;
				else
					software = versionServer.substring(i+1, j);
			}
			else
				software = null;
		}
		
		return new Version( new VersionNumber(mMaj, mMin, mRev, rMaj, rMin, b),
				versionServer, versionAPI, serverType, software, build);
	}
	
	
//	/**
//	 * FOR TESTING Version.java
//	 * @param server a {@link Server}
//	 * @return a string containing all version data after processing (or <code>null</code>)
//	 */
//	public static String test(final Server server)
//	{
//		final Version version = getVersion(server);
//		if (version == null)
//			return null;
//		
//		return  "version: " + version.getServerVersion() + "\n" +
//				"type: " + version.getServerType() + "\n" +
//				"api: " + version.getServerAPIVersion() + "\n" +
//				"software: " + version.getServerSoftware() + "\n" +
//				"buildName: " + version.getServerBuildName() + "\n" +
//				"buildNum: " + version.getServerBuildNumber() + "\n" +
//				"from numbers... " + version.getMcMajor() + "." + version.getMcMinor() + "." +
//				version.getMcRevision() + "-R" + version.getReMajor() + "." +
//				version.getReMinor() + "\n" +
//				"toString: " + version;
//	}
}