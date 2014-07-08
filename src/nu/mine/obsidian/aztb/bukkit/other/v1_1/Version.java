package nu.mine.obsidian.aztb.bukkit.other.v1_1;

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

import javax.annotation.concurrent.Immutable;

import org.bukkit.Server;

/**
 * Greatly simplifies {@link Server} version checking tasks (Bukkit/Spigot/Cauldron).
 * </p>
 * <b>There is no public constructor - use {@link #getVersion(Server)} instead!</b>
 * <br>(Instances of {@link Version} are {@link Immutable}!)
 * </p>
 * To get the version as numbers, e.g. from "1.7.2-R0.3", use<br>{@link #getMcMajor()} ,
 *  {@link #getMcMinor()} , {@link #getMcRevision()} , {@link #getReMajor()} ,
 *  {@link #getReMinor()}<br> - in that order.
 * </p>
 * To get the raw server version string use {@link #getServerVersion()}
 * <br>Example output: <code>"git-Bukkit-1.7.2-R0.3-b3020jnks (MC: 1.7.2)"</code>
 * <br>(This is the same as calling {@link Server#getVersion()})
 * </p>
 * To get the server software string use {@link #getServerSoftware()}
 * <br>Example output: <code>"Spigot"</code>
 * <br>(This is the second part of the server version string)
 * </p>
 * To get the raw server API version string use {@link #getServerAPIVersion()}
 * <br>Example output: <code>"1.7.2-R0.3"</code>
 * <br>(This is the same as calling {@link Server#getBukkitVersion()})
 * </p>
 * To get the build name use {@link #getServerBuildName()}
 * <br>Example output: <code>"b3020jnks"</code>
 * <br>(This is the second last part of the server version string)
 * </p>
 * To get the build number use {@link #getServerBuildNumber()}
 * <br>Example output: <code>3020</code>
 * <br>(This is the last set of numbers inside the build-name)
 * </p>
 * To get the server type use {@link #getServerType()}
 * <br>Example output: <code>"Cauldron-MCPC-Plus"</code>
 * <br>(This is the same as calling {@link Server#getName()})
 * </p>
 * To get a nice summary use {@link #toString()}
 * <br>Example output:
 * <br><code>
 * "CraftBukkit server git-Spigot-1529 (MC: 1.7.9) (Implementing API version 1.7.9-R0.3-SNAPSHOT)"
 * </code>
 * 
 * @see Version#getVersion(Server)
 * @author AnorZaken
 * @version 1.1b
 */
@Immutable
public class Version
{
	// "The standard GNU version numbering scheme is major.minor.revision"
	
	private final String version, api, software, type, build;
	private final int mc1, mc2, mc3, r1, r2, b;
	
	private Version(final String version
			, final int m1, final int m2, final int m3, final int r1, final int r2, final int b
			, final String api, final String type, final String software, final String build)
	{
		this.version = version;
		this.mc1 = m1;
		this.mc2 = m2;
		this.mc3 = m3;
		this.r1 = r1;
		this.r2 = r2;
		this.b = b;
		this.api = api;
		this.software = software;
		this.type = type;
		this.build = build;
	}
	
	/**
	 * Get the raw server version string.
	 * <br>Example output: <code>"git-Bukkit-1.7.2-R0.3-b3020jnks (MC: 1.7.2)"</code>
	 * <br>(This is the same as calling {@link Server#getVersion()})
	 */
	public String getServerVersion() { return version; }
	
	/**
	 * Get the Minecraft servers major version number. (API)
	 * </p>Example: 1.7.2-R0.3
	 * <br>&nbsp; 1 - McMajor
	 * <br>&nbsp; 7 - McMinor
	 * <br>&nbsp; 2 - McRevision
	 * <br>&nbsp; 0 - ReMajor
	 * <br>&nbsp; 3 - ReMinor
	 * @return major version (int)
	 * @see #getMcMajor()
	 * @see #getMcMinor()
	 * @see #getMcRevision()
	 * @see #getReMajor()
	 * @see #getReMinor()
	 */
	public int getMcMajor() { return mc1; }
	
	/**
	 * Get the Minecraft servers minor version number. (API)
	 * </p>Example: 1.7.2-R0.3
	 * <br>&nbsp; 1 - McMajor
	 * <br>&nbsp; 7 - McMinor
	 * <br>&nbsp; 2 - McRevision
	 * <br>&nbsp; 0 - ReMajor
	 * <br>&nbsp; 3 - ReMinor
	 * @return minor version (int)
	 * @see #getMcMajor()
	 * @see #getMcMinor()
	 * @see #getMcRevision()
	 * @see #getReMajor()
	 * @see #getReMinor()
	 */
	public int getMcMinor() { return mc2; }
	
	/**
	 * Get the Minecraft servers version revision number. (API)
	 * </p>Example: 1.7.2-R0.3
	 * <br>&nbsp; 1 - McMajor
	 * <br>&nbsp; 7 - McMinor
	 * <br>&nbsp; 2 - McRevision
	 * <br>&nbsp; 0 - ReMajor
	 * <br>&nbsp; 3 - ReMinor
	 * @return version revision (int)
	 * @see #getMcMajor()
	 * @see #getMcMinor()
	 * @see #getMcRevision()
	 * @see #getReMajor()
	 * @see #getReMinor()
	 */
	public int getMcRevision() { return mc3; }
	
	/**
	 * Get the Minecraft servers major revision version number. (API)
	 * </p>Example: 1.7.2-R0.3
	 * <br>&nbsp; 1 - McMajor
	 * <br>&nbsp; 7 - McMinor
	 * <br>&nbsp; 2 - McRevision
	 * <br>&nbsp; 0 - ReMajor
	 * <br>&nbsp; 3 - ReMinor
	 * @return major revision version (int)
	 * @see #getMcMajor()
	 * @see #getMcMinor()
	 * @see #getMcRevision()
	 * @see #getReMajor()
	 * @see #getReMinor()
	 */
	public int getReMajor() { return r1; }
	
	/**
	 * Get the Minecraft servers minor revision version number. (API)
	 * </p>Example: 1.7.2-R0.3
	 * <br>&nbsp; 1 - McMajor
	 * <br>&nbsp; 7 - McMinor
	 * <br>&nbsp; 2 - McRevision
	 * <br>&nbsp; 0 - ReMajor
	 * <br>&nbsp; 3 - ReMinor
	 * @return minor revision version (int)
	 * @see #getMcMajor()
	 * @see #getMcMinor()
	 * @see #getMcRevision()
	 * @see #getReMajor()
	 * @see #getReMinor()
	 */
	public int getReMinor() { return r2; }
	
	/**
	 * Get the raw server API version string.
	 * <br>Example output: <code>"1.7.2-R0.3"</code>
	 * <br>(This is the same as calling {@link Server#getBukkitVersion()})
	 */
	public String getServerAPIVersion() { return api; }
	
	/**
	 * Get the server software string use {@link #getServerSoftware()}
	 * <br>Example output: <code>"Spigot"</code>
	 * <br>(This is the second part of the server version string)
	 */
	public String getServerSoftware() { return software; }
	
	/**
	 * Get the server type.
	 * <br>Example output: <code>"Cauldron-MCPC-Plus"</code>
	 * <br>(This is the same as calling {@link Server#getName()})
	 */
	public String getServerType() { return type; }
	
	/**
	 * Get the build name.
	 * <br>Example output: <code>"b3020jnks"</code>
	 * <br>(This is the second last part of the server version string)
	 */
	public String getServerBuildName() { return build; }
	
	/**
	 * Get the build number.
	 * <br>Example output: <code>3020</code>
	 * <br>(This is the last set of numbers inside the build-name)
	 */
	public int getServerBuildNumber() { return b; }
	
	/**
	 * Compares this {@link Version} to a version described with <code>int</code> arguments.
	 * @param mcMajor Example: in 1.7.2-R0.3 - this would be the 1
	 * @param mcMinor Example: in 1.7.2-R0.3 - this would be the 7
	 * @param mcRevision Example: in 1.7.2-R0.3 - this would be the 2
	 * @param reMajor Example: in 1.7.2-R0.3 - this would be the 0
	 *  <br>(use {@link Integer#MIN_VALUE} to ignore this - <i>will ignore {@code reMinor} too!</i>)
	 * @param reMinor Example: in 1.7.2-R0.3 - this would be the 3
	 *  <br>(use {@link Integer#MIN_VALUE} to ignore this)
	 * @return the value 0 if this {@link Version} matches the arguments;
	 *  a value less than 0 if this {@link Version} describes something older than the arguments;
	 *  and a value greater than 0 if this {@link Version} describes something newer than the arguments
	 */
	public int compareTo(final int mcMajor, final int mcMinor, final int mcRevision
			, final int reMajor, final int reMinor)
	{
		if(mc1 == mcMajor) {
			if(mc2 == mcMinor) {
				if(mc3 == mcRevision) {
					if(r1 == Integer.MIN_VALUE)
						return 0;
					else if(r1 == reMajor) {
						if(r2 == Integer.MIN_VALUE || r2 == reMinor)
							return 0;
						else
							return r2 < reMinor ? -1 : 1;
					} else
						return r1 < reMajor ? -1 : 1;
				} else
					return mc3 < mcRevision ? -1 : 1;
			} else
				return mc2 < mcMinor ? -1 : 1;
		} else
			return mc1 < mcMajor ? -1 : 1;
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
		return compareTo(mcMajor, mcMinor, mcRevision, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}
	
	/**
	 * Complete version information in a user friendly and display ready string.
	 * <br><i>Example output:
	 * <br>"CraftBukkit server git-Spigot-1529 (MC: 1.7.9) (Implementing API version 1.7.9-R0.3-SNAPSHOT)"</i>
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
	 * Compares this {@link Version} with another.
	 * @param version other {@link Version}
	 * @return <code>true</code> if both share the same raw version string, otherwise <code>false</code>.
	 */
	public boolean equals(final Version version) {
		return this == version || (version != null && this.version.equals(version.version));
	}
	
	@Override
	public int hashCode() {
		return version.hashCode();
	}
	
	// =================================================
	// =================== STATICS =====================
	
	private static Version instance = null;
	
	/**
	 * Get {@link Version} information about a {@link Server}. 
	 * </p>
	 * <i>This method caches the latest {@link Version} object and reuses it if its server-version
	 * string matches the version string of the provided {@link Server} argument.</i>
	 * @param server {@link Server} to get version information about
	 * @return a {@link Version} object or <code>null</code> if the method failed to extract MC-version.
	 * @throws IllegalArgumentException if {@code server} is <code>null</code>
	 */
	public static Version getVersion(final Server server)
	{
		if (server == null)
			throw new IllegalArgumentException("server == null");
		final String versionServer = server.getVersion(); //Example: "git-Bukkit-1.7.2-R0.3-b3020jnks (MC: 1.7.2)"
		if (instance != null && instance.version.equals(versionServer))
			return instance;
		final String versionAPI = server.getBukkitVersion(); //Example: "1.7.2-R0.3"
		final String type = server.getName(); //Example: "CraftBukkit"
		return instance = createVersion(versionServer, versionAPI, type);
	}
	
	/**
	 * Creates a {@link Version} object from version strings.
	 * @param versionServer the server version ( {@link Server#getVersion()} )
	 * @param versionAPI the Bukkit-API version ( {@link Server#getBukkitVersion()} )
	 * @param serverType the server software used
	 * @return a {@link Version} object or <code>null</code> if the method failed to extract
	 *  MC-version information. (Other extraction fails will still yield a {@link Version}
	 *  object but some information might be missing - refer to the documentation for each of
	 *  the get-methods for more information!)
	 */
	private static Version createVersion(final String versionServer, final String versionAPI, final String serverType)
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
		
		return new Version(versionServer, mMaj, mMin, mRev, rMaj, rMin, b, versionAPI, serverType, software, build);
	}
	
	
//	/**
//	 * FOR TESTING Version.java
//	 * @param server a {@link Server}
//	 * @return a string containing all version data after processing (or <code>null</code>)
//	 */
//	public static String debug(final Server server)
//	{
//		final Version version = getVersion(server);
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