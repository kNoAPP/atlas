# DataHandler Utility
### Compatible with 1.6.4-1.14.1
Most plugins need a way to handle configuration data in the form of a `YamlConfiguration`. The aim of the 
[DataHandler](https://github.com/GodComplexMC/godcomplex-core/blob/master/src/main/java/org/godcomplex/core/data/DataHandler.java) 
class is to provide developers with a simple way to generate, read, cache, and save to .yml, .json, and .properties files. 
`DataHandler` can also read in generic files to a `File` instance. But most importantly, `DataHandler` will allow you to 
create default configuration files and save them to a plugin data folder from your `/src/main/resources` folder.

In this README, we will cover the following topics:
- Setup
- Usage
- Examples

## Setup
The first step is to generate your default configuration files and place them inside the `src/main/resources` folder 
within your Java project. Once you've done that, you can access your configuration file by generating an instance of 
`DataHandler`.

**Constructors**
```java
/**
 * A base constructor for each file type
 * @param pl - A plugin instance to determine where to access data files from
 * @param path - Where to drop/access the configuration file usually preceded by a "/"
 * @param filename - The name of a configuration file stored in src/main/resources usually preceded by a "/"
 * @param loadDefaults - If the file is missing, should we load a default from the plugin?
*/
public DataHandler(Plugin pl, String path, String filename, boolean loadDefaults); //Does not append "/plugins/"

public DataHandler(Plugin pl, String filename) {
	this(pl, pl.getDataFolder().getAbsolutePath(), filename, true);
}

public DataHandler(Plugin pl, String path, String filename) {
	this(pl, path, filename, false);
}
```

**Creating Instances**
```java
YML config = new YML(this, "/config.yml"); //Creates config.yml if it doesn't exist from default copy.
JSON playerdata = new JSON(this, "/playerdata.json"); //Creates playerdata.json if it doesn't exist from default copy.
PROPS settings = new PROPS(this, "/settings.properties"); //Creates settings.properties if it doesn't exist from default copy.
```

Store these DataHandler instances somewhere because you'll be able to re-use them and save the cost of re-initializing 
the instance. Typically, I like to store them within the Main class as a `private static` member. I'll then create a 
getter to access them. If your project is very modular, you could pass these as parameters to other objects.

## Usage
Each extension of `DataHandler` has some similarities. Each have a way to directly access the data within the file. 
Each have a caching system that does not reference the data from the file, but instead return an existing copy of the 
data (much faster). Each have a way to save data to the file which includes the cache. Additionally, `DataHandler` 
provides some shared functionality of its own listed below.

But, there are also some important differences between `YML`, `JSON`, and `PROPS` classes. For example, `JSON` needs 
the ability to write parse to an `Object` and write from an `Object` instance to a file. Those will also be listed below.

### DataHandler Functions
- `File getFile()`
    > Used to get the `File` object of the file.
- `boolean wasCreated()`
    > Returns true if this instantiation cause the default file to be written from `src/main/resources`.

### YML Functions
- `FileConfiguration getYML()`
    > Used to get a `FileConfiguration` directly from existing file data. Will override the cache with an updated copy.
- `FileConfiguration getCachedYML()`
    > Used to get a cached version of `FileConfiguration` (much faster). Calls `getYML()` if cache is null.
- `void saveYML(FileConfiguration fc)`
    > Used to immediately save a `FileConfiguration` to the .yml file (which could overwrite existing data). Will 
    automatically update the cache.
    
### JSON Functions
- `Object getJSON(Class<?> parse)`
    > Used to get a `Object` instance directly from existing file data based on the passed class. Will override the cache 
    with an updated copy. *Note: The `JSON` class uses `Gson` for parsing. `Gson` requires classes to include a empty 
    constructors, getters, and setters for properties being parsed. See examples for more information.
- `Object getCachedJSON(Class<?> parse)`
    > Used to get a cached version of a `Object` instance based on the passed class (much faster). Calls 
    `getJSON(Class<?> parse)` if cache is null.
- `void saveJSON(Object obj)`
    > Used to immediately save a `Object` to the .json file (which could overwrite existing data). Will automatically 
    update the cache.
    
### PROPS Functions
- `Properties getProperties()`
    > Used to get a `Properties` directly from existing file data. Will override the cache with an updated copy.
- `Properties getCachedProperties()`
    > Used to get a cached version of `Properties` (much faster). Calls `getProperties()` if cache is null.
- `void saveProperties(Properties props)`
    > Used to immediately save a `Properties` to the .properties file (which could overwrite existing data). Will 
    automatically update the cache.
    
## Examples
The following locations are great places to see `DataHandler` in action:
- [Salem](https://github.com/GodComplexMC/Salem/tree/master/src/main/java/com/kNoAPP/salem/Salem.java)
- [OnTheRun](https://github.com/GodComplexMC/OnTheRun/tree/master/src/main/java/com/kNoAPP/ontherun/OnTheRun.java)
- [P3](https://github.com/GodComplexMC/P3/tree/master/src/main/java/com/kNoAPP/P3/commands.java)

### Example loading from JSON to Object
```java
public ChunkLoaderCommand(JSON fromFile) {
	ChunkLocation[] chunks = (ChunkLocation[]) fromFile.getCachedJSON(ChunkLocation[].class);
	if(chunks != null) {
		for(ChunkLocation cl : chunks) {
			frozen.add(cl);
			World w = Bukkit.getWorld(cl.getWorld());
			if(w != null)
				w.getChunkAt(cl.getX(), cl.getZ()).setForceLoaded(true);
		}
	}
}
```

### Example saving Object to JSON
```java
public void save(JSON toFile) {
	toFile.saveJSON(frozen.toArray(new ChunkLocation[0]));
}
```

### Example Gson-safe ChunkLocation class
```java
public static class ChunkLocation {
	
	private String world;
	private int x, z;
	
	public ChunkLocation() {}
	
	public ChunkLocation(Chunk c) {
		this.x = c.getX();
		this.z = c.getZ();
		this.world = c.getWorld().getName();
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}
	
	@Override
	public int hashCode() {
		int hash = 3;

		hash = 19 * hash + (this.world != null ? this.world.hashCode() : 0);
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.x/16) ^ (Double.doubleToLongBits(this.x/16) >>> 32));
		hash = 19 * hash + (int) (Double.doubleToLongBits(this.z/16) ^ (Double.doubleToLongBits(this.z/16) >>> 32));
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ChunkLocation))
			return false;

		ChunkLocation c = (ChunkLocation) obj;
		return c.getX() == x && c.getZ() == z && c.getWorld().equals(world);
	}
}
```	