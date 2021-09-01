package rsonic;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.EOFException;
import java.io.IOException;

public class RSMap {
/** TYPE_AUTO = Autodetect map type */
	public final static int TYPE_AUTO = 0;
/** TYPE_v1 = Sonic Nexus and Sonic CD */
	public final static int TYPE_v1 = 1;
/** TYPE_v2 = Sonic 1 */	
	public final static int TYPE_v2 = 2;
/** TYPE_RS = Retro-Sonic */
	public final static int TYPE_RS = 3;

	private int width;
	private int height;
	private int map[][];
	private byte dispBytes[];
	private byte sixBytes_RS[];
	private ArrayList<RSObject> objects;
	private ArrayList<String> objectTypeNames;
	private String zoneName;
	private int loadType;
	
	public RSMap(String zoneName, int width, int height)
	{
		this.width = width;
		this.height = height;
		this.map = new int[height][width];
		this.objects = new ArrayList<RSObject>();
		this.objectTypeNames = new ArrayList<String>();
		this.objectTypeNames.add("Type zero");
		this.zoneName = zoneName;
		this.dispBytes = new byte[5];
		
// Set the five "display bytes" to the default value they have for Sonic CD level maps		
		this.dispBytes[0] = 1;
		this.dispBytes[1] = 0;
		this.dispBytes[2] = 0;
		this.dispBytes[3] = 3;
		this.dispBytes[4] = 9;

// By default, empty maps are assumed to be in Sonic 1 format		
		this.loadType = TYPE_v2;
	}
	
	public RSMap(File map)
		throws Exception
	{
		this(map, TYPE_AUTO);
	}
	
	public RSMap(File map, int type)
		throws Exception
	{
// Create ArrayLists.
		objects	= new ArrayList<RSObject>();
		objectTypeNames = new ArrayList<String>();
		this.objectTypeNames.add("Type zero");
	
// Open file	
		RandomAccessFile mapFile = new RandomAccessFile(map, "r");

// Detect Retro-Sonic map file
// This autodetection is not failure-proof, but should practically work fine
// We get the first two bytes (respectively map width and height)
// multiply them, and add 2 to the result
// If the result is equal to the file size of the map, we are
// probably dealing with a map from Retro-Sonic		
		
		if(type == TYPE_AUTO)
		{
			int _chk_width = mapFile.readUnsignedByte();
			int _chk_height = mapFile.readUnsignedByte();
		
			if(mapFile.length() == ( (_chk_width * _chk_height) + 2) )
				type = TYPE_RS;
			
// Another auto-detection hint:
// this one is to detect some maps (r2, r3, r4, r5) from the Dreamcast version.
// these maps are longer than (width*height)+2, for some strange reason
// if width == 0x80 and height == 0x10 then it's a Retro-Sonic map
// quite safe considering that it'd be very unlikely to find a name 128 bytes long			
// and with 0x10 as first byte in a v1 or v2 map!			
			if(_chk_width == 0x80 && _chk_height == 0x10)
				type = TYPE_RS;
		}
			
		mapFile.seek(0);
		
		if(type == TYPE_RS)
		{
			type_rs_constructor_part_2(map, mapFile);
			return;
		}

// Create six bytes array with NULL bytes
// This is needed when saving to Retro-Sonic format
		sixBytes_RS = new byte[6];
		
// Read zone name
		
		int sLen = mapFile.readUnsignedByte();
		byte byteBuf[] = new byte[sLen];
		
		mapFile.read(byteBuf);
		
		this.zoneName = new String(byteBuf);

// Read the five "display bytes"
// These five bytes' purpose is not fully understood but they 
// are probably display settings - modifying the bytes toggles the display 
// of layers...
// For now, we load and store them, and save them back when saving the map again

		this.dispBytes = new byte[5];
		
		mapFile.read(this.dispBytes);

// Map width in 128 pixel units
// In Sonic Nexus and Sonic CD, it's one byte long
// In Sonic 1 it's two bytes long, little-endian
		
		this.width = mapFile.readUnsignedByte();
		
		if(type == TYPE_v2)
			this.width |= mapFile.readUnsignedByte() << 8;
		
// Map height in 128 pixel units
// field size like width
		this.height = mapFile.readUnsignedByte();
		
// Map version autodetection code		
		
		if(type == TYPE_AUTO && this.height == 0)
		{
		
// Height = 0? Probably this map is a Sonic 1 map
// and we're in the high byte of the width field.
// This is quite safe because there are no Nexus/CD maps
// with a width of zero, and there are no S1 maps with a
// width higher than 255.
			type = TYPE_v2;
			
			this.width |= this.height << 8;
			this.height = mapFile.readUnsignedByte();
		}
		else
			type = TYPE_v1;
		
		if(type == TYPE_v2)
			this.height |= mapFile.readUnsignedByte() << 8;
			
// Load map data
		this.map = new int[height][width];
		
		for(int y = 0; y < this.height; y++)
		{
			for(int x = 0; x < this.width; x++)
			{

// 128x128 Block number is 16-bit
// Big-Endian in Sonic Nexus and Sonic CD
// Little-Endian in Sonic 1			
			
				if(type == TYPE_v2)
				{
					this.map[y][x] = mapFile.readUnsignedByte();
					this.map[y][x] |= mapFile.readUnsignedByte() << 8;
				}
				else
				{
					this.map[y][x] = mapFile.readUnsignedByte() << 8;
					this.map[y][x] |= mapFile.readUnsignedByte();
				}
			}
		}

// Read object type names
// Only v1 supports this.

		if(type == TYPE_v1)		
		{
		// Read number of object types			
			int num_of_object_types = mapFile.readUnsignedByte();
			
			for(int n = 0; n < num_of_object_types; n++)
			{
		// Read length of string
				sLen = mapFile.readUnsignedByte();	
		
		// Add name to list	
				byteBuf = new byte[sLen];
		
				mapFile.read(byteBuf);
				
				objectTypeNames.add(new String(byteBuf));
			}
		}
		
// Read number of objects
		int num_of_objects = 0;
		
		if(type == TYPE_v2)
		{
// 4 bytes, little-endian, unsigned
			num_of_objects = mapFile.readUnsignedByte();
			num_of_objects|= mapFile.readUnsignedByte()<<8;
			num_of_objects|= mapFile.readUnsignedByte()<<16;
			num_of_objects|= mapFile.readUnsignedByte()<<24;
		}
		else
		{
// 2 bytes, big-endian, unsigned
			num_of_objects = mapFile.readUnsignedByte() << 8;
			num_of_objects|= mapFile.readUnsignedByte();
		}
		
// Read object data

// The data for each object is 6 bytes long in v1,
// 12 bytes long in v2		

		int obj_type = 0;
		int obj_subtype = 0;
		int obj_xPos = 0;
		int obj_yPos = 0;

		try
		{
			if(type == TYPE_v2)
			{
				for(int n = 0; n < num_of_objects; n++)
				{
					// Object type, 1 byte, unsigned 
					obj_type = mapFile.readUnsignedByte();
				//	obj_type|= mapFile.readUnsignedByte() << 8;
			
					// Object subtype, 1 byte, unsigned
					obj_subtype = mapFile.readUnsignedByte();
				//	obj_subtype|= mapFile.readUnsignedByte() << 8;
				
					mapFile.skipBytes(2);
				
					// X Position, 4 bytes, little-endian, unsigned
					obj_xPos = mapFile.readUnsignedByte();
					obj_xPos|= mapFile.readUnsignedByte() << 8;
					obj_xPos|= mapFile.readUnsignedByte() << 16;
					obj_xPos|= mapFile.readUnsignedByte() << 24;
					
					// Y Position, 4 bytes, little-endian, unsigned
					obj_yPos = mapFile.readUnsignedByte();
					obj_yPos|= mapFile.readUnsignedByte() << 8;
					obj_yPos|= mapFile.readUnsignedByte() << 16;
					obj_yPos|= mapFile.readUnsignedByte() << 24;
					
					// Add object
					objects.add(new RSObject(obj_type, obj_subtype, obj_xPos, obj_yPos));
				}
			}
			else
			{
				for(int n = 0; n < num_of_objects; n++)
				{
					// Object type, 1 byte, unsigned
					obj_type = mapFile.readUnsignedByte();
					// Object subtype, 1 byte, unsigned
					obj_subtype = mapFile.readUnsignedByte();
	
					// X Position, 2 bytes, big-endian, signed			
					obj_xPos = mapFile.readByte() << 8;
					obj_xPos |= mapFile.readUnsignedByte();
			
					// Y Position, 2 bytes, big-endian, signed
					obj_yPos = mapFile.readByte() << 8;
					obj_yPos |= mapFile.readUnsignedByte();
					
					// Add object
					objects.add(new RSObject(obj_type, obj_subtype, obj_xPos, obj_yPos));
				}
			}
		}		
		
		catch(Exception exc)
		{
			if(!(exc instanceof EOFException))
				throw exc;
		}
		
		finally
		{
			mapFile.close();
		}
		
		this.loadType = type;
	}
	
	public void save(File file, int type)
		throws Exception
	{
		if(type == TYPE_AUTO)
			throw new RuntimeException("Can't specify TYPE_AUTO as type, it is ambigous.");

		if(type == TYPE_RS) // Retro-Sonic 
			save_RS(file);
		else if(type == TYPE_v1) // Sonic Nexus and Sonic CD
			save_v1(file);
		else if(type == TYPE_v2) // Sonic 1 and Sonic 2
			save_v2(file);
		else
			throw new RSDataException("Unknown type specified");
	}
	
	public int getWidth()
	{
		return this.width;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public boolean resize(int width, int height)
	{			
		int newMap[][] = new int[height][width];	
			
		for(int h = 0; h < height; h++)
		{
			for(int w = 0; w < width; w++)
			{
				if(w >= this.width || h >= this.height)
					newMap[h][w] = 0;
				else
					newMap[h][w] = map[h][w];
			}
		}
		
		this.width = width;
		this.height = height;
		
		this.map = newMap;
		
		return true;		
	}
	
	public int[][] getTileMap()
	{
		int copiedMap[][] = new int[this.height][this.width];
		
		for(int h = 0; h < this.height; h++)
			for(int w = 0; w < this.width; w++)
				copiedMap[h][w] = map[h][w];
		
		return copiedMap;
	}
	
	public RSObject[] getObjects()
	{
		RSObject obj[] = new RSObject[objects.size()];
		
		for(int i = 0; i < obj.length; i++)
			obj[i] = objects.get(i).newInstance();
			
		return obj;
	}
	
	public String[] getObjectTypeNames()
	{
		String[] typeNames = new String[objectTypeNames.size()];
	
		for(int i = 0; i < typeNames.length; i++)	
			typeNames[i] = new String(objectTypeNames.get(i));
			
		return typeNames;
	}
	
	public String getZoneName()
	{
		return this.zoneName;
	}
	
	public boolean setTileMap(int[][] map)
	{
		int newMap[][] = new int[map.length][map[0].length];
	
		for(int h = 0; h < map.length; h++)
			for(int w = 0; w < map[0].length; w++)
				newMap[h][w] = map[h][w];
		
		this.map = newMap;
		
		return true;	
	}
	
	public List<RSObject> getObjectList()
	{
		List<RSObject> newList = new ArrayList<RSObject>();
		
		for(RSObject obj : objects)
			newList.add(obj);
			
		return newList;
	}
	
	public void setObjects(RSObject[] objs)
	{
		ArrayList<RSObject> newObjects = 
			new ArrayList<RSObject>();
			
		for(int i = 0; i < objs.length; i++)
			newObjects.add(objs[i]);
			
		this.objects = newObjects;
	}
	
	public void setObjectList(List<RSObject> objList)
	{
		objects = new ArrayList<RSObject>();
		
		for(RSObject obj : objList)
			objects.add(obj.newInstance());
	}

	public void setObjectTypeNames(String[] typeNames)
	{
		this.objectTypeNames = new ArrayList<String>();
		
		for(int i = 0; i < typeNames.length; i++)
			this.objectTypeNames.add(typeNames[i]);		
	}

	public void setZoneName(String zoneName)
	{
		this.zoneName = zoneName;
	}
	
	public int getLoadType()
	{
		return this.loadType;
	}
	
	private void type_rs_constructor_part_2(File map, RandomAccessFile mapFile)
		throws Exception
	{
// Separate path components			
		String dirname = map.getParent();
		String basename = map.getName();
		
		int i = basename.indexOf('.');
		
		String withoutExt = (i == -1)?basename:basename.substring(0, i);

// Make the path for the item file
		
		String itmPath = dirname + File.separator + withoutExt + ".itm";

// Open the item file
		
		RandomAccessFile itmFile = new RandomAccessFile(itmPath, "r");
		
// Read zone name
// In this version of the format, the zone name is in the item file
		
		int sLen = itmFile.readUnsignedByte();
		byte byteBuf[] = new byte[sLen];
		
		itmFile.read(byteBuf);
		
		this.zoneName = new String(byteBuf);

// Read map width and height [map file]
		this.width = mapFile.readUnsignedByte();
		this.height = mapFile.readUnsignedByte();
		
// Allocate map
		this.map = new int[this.height][this.width];

		try
		{
// Read map data [map file]
			for(int y = 0; y < this.height; y++)
				for(int x = 0; x < this.width; x++)
					this.map[y][x] = mapFile.readUnsignedByte();
		}
		
		catch(Exception exc)
		{
			if(!(exc instanceof EOFException))
			{
				mapFile.close();
				itmFile.close();
				throw exc;	
			}
		}

// Read the six bytes, and keep them
// Their function is still unknown, but in this way we can save them back
		sixBytes_RS = new byte[6];
		itmFile.read(sixBytes_RS);
		
// Read objects [item file]
		int num_of_objects = itmFile.readUnsignedByte() << 8;
		num_of_objects |= itmFile.readUnsignedByte();

		try
		{
			for(i = 0; i < num_of_objects; i++)
			{
				// Object type, 1 byte, unsigned
				int obj_type = itmFile.readUnsignedByte();
				// Object subtype, 1 byte, unsigned
				int obj_subtype = itmFile.readUnsignedByte();
	
				// X Position, 2 bytes, big-endian, signed			
				int obj_xPos = itmFile.readByte() << 8;
				obj_xPos |= itmFile.readUnsignedByte();
			
				// Y Position, 2 bytes, big-endian, signed
				int obj_yPos = itmFile.readByte() << 8;
				obj_yPos |= itmFile.readUnsignedByte();
					
				// Add object
				objects.add(new RSObject(obj_type, obj_subtype, obj_xPos, obj_yPos));
			}
		}
		
		catch(Exception exc)
		{
			if(!(exc instanceof EOFException))
				throw exc;			
		}
		
		finally
		{
// Close map and item files
			mapFile.close();
			itmFile.close();
		}
			
// Set load type to Retro-Sonic format
		this.loadType = TYPE_RS;		
		
// Set the five "display bytes" to the default value they have for Sonic CD level maps,
// so they get saved when saving to the newer v1/v2 formats
		this.dispBytes = new byte[5];
		
		this.dispBytes[0] = 1;
		this.dispBytes[1] = 0;
		this.dispBytes[2] = 0;
		this.dispBytes[3] = 3;
		this.dispBytes[4] = 9;
	}
	
	private void save_v1(File file)
		throws Exception
	{
		if(this.width > 255)
			throw new RSDataException("Cannot save as Type v1. Width in tiles > 255");
		
		if(this.height > 255)
			throw new RSDataException("Cannot save as Type v1. Height in tiles > 255");
		
		int num_of_objects = objects.size();
		
		if(num_of_objects > 65535)
			throw new RSDataException("Cannot save as Type v1. Number of objects > 65535");
			
		for(int n = 0; n < num_of_objects; n++)
		{
			RSObject obj = objects.get(n);
			
			int obj_type = obj.getType();
			int obj_subtype = obj.getSubtype();
			int obj_xPos = obj.getXPos();
			int obj_yPos = obj.getYPos();
			
			if(obj_type > 255)
				throw new RSDataException("Cannot save as Type v1. Object type > 255");
				
			if(obj_subtype > 255)
				throw new RSDataException("Cannot save as Type v1. Object subtype > 255");
				
			if(obj_xPos < -32768 || obj_xPos > 32767)
				throw new RSDataException("Cannot save as Type v1. Object X Position can't fit in 16-bits");
				
			if(obj_yPos < -32768 || obj_yPos > 32767)
				throw new RSDataException("Cannot save as Type v1. Object Y Position can't fit in 16-bits");
		}
		
		RandomAccessFile mapFile = new RandomAccessFile(file, "rw");
		
		// Write zone name		
		mapFile.writeByte(this.zoneName.length());
		mapFile.writeBytes(this.zoneName);
			
		// Write the five "display" bytes we kept
		mapFile.write(this.dispBytes);
			
		// Write width and height
		mapFile.writeByte(this.width);
		mapFile.writeByte(this.height);
			
		// Write tile map
			
		for(int h = 0; h < this.height; h++)
		{
			for(int w = 0; w < this.width; w++)
			{
				mapFile.writeByte(map[h][w] >> 8);
				mapFile.writeByte(map[h][w] & 0xff);	
			}
		}
			
		// Write number of object type names
		int num_of_objtype_names = this.objectTypeNames.size();
			
		mapFile.writeByte(num_of_objtype_names - 1);
			
		// Write object type names
		// Ignore first object type "Type zero", it is not stored.
		for(int n = 1; n < num_of_objtype_names; n++)
		{
			mapFile.writeByte(objectTypeNames.get(n).length());
			mapFile.writeBytes(objectTypeNames.get(n));
		}
			
		// Write number of objects
		mapFile.writeByte(num_of_objects >> 8);
		mapFile.writeByte(num_of_objects & 0xFF);

		// Write object data
		for(int n = 0; n < num_of_objects; n++)
		{
			RSObject obj = objects.get(n);
				
			int obj_type = obj.getType();
			int obj_subtype = obj.getSubtype();
			int obj_xPos = obj.getXPos();
			int obj_yPos = obj.getYPos();
			
			mapFile.writeByte(obj_type);
			mapFile.writeByte(obj_subtype);
			
			mapFile.writeByte(obj_xPos >> 8);
			mapFile.writeByte(obj_xPos & 0xFF);

			mapFile.writeByte(obj_yPos >> 8);
			mapFile.writeByte(obj_yPos & 0xFF);
		}
		
		mapFile.setLength(mapFile.getFilePointer());
		mapFile.close();
	}
	
	private void save_v2(File file)
		throws Exception
	{
		RandomAccessFile mapFile = new RandomAccessFile(file, "rw");
		
		// Write zone name		
		mapFile.writeByte(this.zoneName.length());
		mapFile.writeBytes(this.zoneName);
			
		// Write the five "display" bytes we kept
		mapFile.write(this.dispBytes);
			
		// Write width
		mapFile.writeByte(this.width & 0xff);
		mapFile.writeByte(this.width >> 8);
			
		// Write height
		mapFile.writeByte(this.height & 0xff);
		mapFile.writeByte(this.height >> 8);
			
		// Write tilemap
		for(int h = 0; h < this.height; h++)
		{
			for(int w = 0; w < this.width; w++)
			{
				mapFile.writeByte(this.map[h][w] & 0xff);	
				mapFile.writeByte(this.map[h][w] >> 8);
			}
		}
			
		// Write number of objects
		int num_of_obj = objects.size();
			
		mapFile.writeByte(num_of_obj & 0xff);
		mapFile.writeByte((num_of_obj >> 8) & 0xff);
		mapFile.writeByte((num_of_obj >> 16) & 0xff);
		mapFile.writeByte((num_of_obj >> 24) & 0xff);
			
		// Write objects
			
		for(int n = 0; n < num_of_obj; n++)
		{
			RSObject obj = objects.get(n);
			int obj_type = obj.getType();
			int obj_subtype = obj.getSubtype();
			int obj_xPos = obj.getXPos();
			int obj_yPos = obj.getYPos();

// Most likely the type and subtypes are still one byte long in v2
// and the two other bytes are for an empty field
				
			mapFile.writeByte(obj_type & 0xff);
				//mapFile.writeByte(obj_type >> 8);
				
			mapFile.writeByte(obj_subtype & 0xff);
				//mapFile.writeByte(obj_subtype >> 8);
				
			mapFile.writeByte(0);
			mapFile.writeByte(0);
				
			mapFile.writeByte(obj_xPos & 0xff);
			mapFile.writeByte((obj_xPos >> 8) & 0xff);
			mapFile.writeByte((obj_xPos >> 16) & 0xff);
			mapFile.writeByte((obj_xPos >> 24) & 0xff);
				
			mapFile.writeByte(obj_yPos & 0xff);
			mapFile.writeByte((obj_yPos >> 8) & 0xff);
			mapFile.writeByte((obj_yPos >> 16) & 0xff);
			mapFile.writeByte((obj_yPos >> 24) & 0xff);
		}
				
		mapFile.setLength(mapFile.getFilePointer());
		mapFile.close();
	}
	
	private void save_RS(File file)
		throws Exception
	{
		if(width >= 256)
			throw new RSDataException("Cannot save as Retro-Sonic map. Level width in tiles > 255.");
		
		if(height >= 256)
			throw new RSDataException("Cannot save as Retro-Sonic map. Level height in tiles > 255.");
		
		int num_of_objects = objects.size();
		
		if(num_of_objects > 65535)
			throw new RSDataException("Cannot save as Retro-Sonic map. Number of objects > 65535");
		
		for(int n = 0; n < num_of_objects; n++)
		{
			RSObject obj = objects.get(n);
			
			int obj_type = obj.getType();
			int obj_subtype = obj.getSubtype();
			int obj_xPos = obj.getXPos();
			int obj_yPos = obj.getYPos();
			
			if(obj_type > 255)
				throw new RSDataException("Cannot save as Retro-Sonic map. Object type > 255");
				
			if(obj_subtype > 255)
				throw new RSDataException("Cannot save as Retro-Sonic map. Object subtype > 255");
				
			if(obj_xPos < -32768 || obj_xPos > 32767)
				throw new RSDataException("Cannot save as Retro-Sonic. Object X Position can't fit in 16-bits");
				
			if(obj_yPos < -32768 || obj_yPos > 32767)
				throw new RSDataException("Cannot save as Retro-Sonic. Object Y Position can't fit in 16-bits");
		}
		
// Separate path components			
		String dirname = file.getParent();
		String basename = file.getName();
		
		int i = basename.indexOf('.');
		
		String withoutExt = (i == -1)?basename:basename.substring(0, i);

// Make the path for the item file		
		String itmPath = dirname + File.separator + withoutExt + ".itm";
		
// Create map file		
		RandomAccessFile mapFile = new RandomAccessFile(file, "rw");
		
// Save width and height
		mapFile.writeByte(width);
		mapFile.writeByte(height);

// Save map data
		for(int y = 0; y < height; y++)
			for(int x = 0; x < width; x++)
				mapFile.writeByte(map[y][x]);

// Close map file
		mapFile.setLength(mapFile.getFilePointer());
		mapFile.close();
		
// Create item file
		RandomAccessFile itmFile = new RandomAccessFile(itmPath, "rw");
		
// Save zone name
		mapFile.writeByte(zoneName.length());
		mapFile.writeBytes(zoneName);
		
// Write the six bytes we kept
		mapFile.write(sixBytes_RS);
		
// Write number of objects
		mapFile.writeByte(num_of_objects >> 8);
		mapFile.writeByte(num_of_objects & 0xFF);

// Write object data
		for(int n = 0; n < num_of_objects; n++)
		{
			RSObject obj = objects.get(n);
				
			int obj_type = obj.getType();
			int obj_subtype = obj.getSubtype();
			int obj_xPos = obj.getXPos();
			int obj_yPos = obj.getYPos();
			
			mapFile.writeByte(obj_type);
			mapFile.writeByte(obj_subtype);
			
			mapFile.writeByte(obj_xPos >> 8);
			mapFile.writeByte(obj_xPos & 0xFF);

			mapFile.writeByte(obj_yPos >> 8);
			mapFile.writeByte(obj_yPos & 0xFF);
		}		
	}
}
