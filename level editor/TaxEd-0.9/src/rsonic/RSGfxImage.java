package rsonic;

import java.awt.image.*;
import java.awt.*;
import java.io.*;

/**
 * This class reads and writes images in the "gfx" format
 * used by old versions of the Retro-Engine.
 */

public class RSGfxImage
{	
	public static Image loadGfx(String pathname)
		throws Exception
	{
		return loadGfx(pathname, false);
	}
	
	public static Image loadGfx(String pathname, boolean dcGfx)
		throws Exception
	{
// Open file		
		RandomAccessFile gfxFile =
			new RandomAccessFile(pathname, "r");
		
// If this is a graphic from the Dreamcast demo of Retro-Sonic, skip the first byte
		if(dcGfx)
			gfxFile.skipBytes(1);
		
// Read width and height
		int width = gfxFile.readUnsignedByte() << 8;
		width|= gfxFile.readUnsignedByte();
		
		int height = gfxFile.readUnsignedByte() << 8;
		height|= gfxFile.readUnsignedByte();
		
// Allocate arrays for palette		
		byte red[ ] = new byte[256];
		byte green[ ] = new byte[256];
		byte blue[ ] = new byte[256];
		
// Read palette
		for(int i = 0; i < 255; i++)
		{
			red[i] = gfxFile.readByte();
			green[i] = gfxFile.readByte();
			blue[i] = gfxFile.readByte();
		}
		
		IndexColorModel palette = new IndexColorModel(8, 256, red, green, blue, 0);
		
// Create image
		BufferedImage gfxImage = new BufferedImage(width, height,
			BufferedImage.TYPE_BYTE_INDEXED, palette);
		
// Read data
		int buf[ ] = new int[3];
		boolean finished = false;
		int cnt = 0;
		int loop = 0;
		
		int data[] = new int[width * height];
		
		try
		{
		
			while(!finished)
			{
				buf[0] = gfxFile.readUnsignedByte();
		
				if(buf[0] != 0xFF)
					data[cnt++] = buf[0];
				else
				{
					buf[1] = gfxFile.readUnsignedByte();
			
					if(buf[1] != 0xFF)
					{
						buf[2] = gfxFile.readUnsignedByte();
						loop = 0;
			
						 // Repeat value needs to decreased by one to decode 
						 // the graphics from the Dreamcast demo
						if(dcGfx)
							buf[2]--;
						
						while(loop < buf[2])
						{
							data[cnt++] = buf[1];
							loop++;
						}
					}
					else
						finished = true;
				}
			}
		}
		
		catch(Exception exc)
		{
			if(!(exc instanceof EOFException))
			{
				if(exc instanceof ArrayIndexOutOfBoundsException)
					throw new RSDataException("Malformed input data");
				else
					throw exc;
			}
		}
				
// Write data to image
		gfxImage.getRaster().setPixels(0, 0, width, height, data);
// Done. Now close the file and return the image		
		gfxFile.close();

		return gfxImage;
	}
	
	private static void rle_write(RandomAccessFile file, int pixel, int count, boolean dcGfx)
		throws Exception
	{
		if(count <= 2)
		{
			for(int y = 0; y < count; y++)
				file.writeByte(pixel);
		}
		else
		{
			while(count > 0)
			{
				file.writeByte(0xFF);
				
				file.writeByte(pixel);
				
				if(dcGfx)
				{
					file.writeByte((count>253) ? 254 : (count + 1));
					count -= 253;
				}
				else
				{
					file.writeByte((count>254) ? 254 : count);
					count -= 254;
				}
			}
		}
	}
	
	public static void writeGfx(String pathname, Image image)
		throws Exception
	{
		writeGfx(pathname, image, false);
	}
	
	public static void writeGfx(String pathname, Image image, boolean dcGfx)
		throws Exception
	{
		if(!(image instanceof BufferedImage))
			throw new RuntimeException("This method only works with images of type BufferedImage.");
		
		BufferedImage bufImage = (BufferedImage)image;
		
		if(bufImage.getType() != BufferedImage.TYPE_BYTE_INDEXED)
			throw new RSDataException("Only indexed images can be converted to GFX format.");
		
		if(bufImage.getWidth() > 65535)
			throw new RSDataException("Images to be converted to GFX format can't be wider than 65535 pixels");
		
		if(bufImage.getHeight() > 65535)
			throw new RSDataException("Images to be converted to GFX format can't be higher than 65535 pixels");
		
		IndexColorModel palette = (IndexColorModel)bufImage.getColorModel();

		int num_pixels = bufImage.getWidth() * bufImage.getHeight();
		int pixels[] = new int[num_pixels];
		
		bufImage.getRaster().getPixels(0, 0, bufImage.getWidth(),
			bufImage.getHeight(), pixels);

// Images can't contain index 255
		for(int x = 0; x < num_pixels; x++)
		{
			if(pixels[x] == 255)
				throw new RSDataException("Images to be converted to GFX format can't contain index 255.");
		}
		
// Open output file
		RandomAccessFile gfxFile = new RandomAccessFile(pathname, "rw");

// If we are saving in Dreamcast format, we need to output a NULL byte		
		if(dcGfx)
			gfxFile.writeByte(0);
		
// Output width and height
		gfxFile.writeByte(bufImage.getWidth() >> 8);
		gfxFile.writeByte(bufImage.getWidth() & 0xff);
		
		gfxFile.writeByte(bufImage.getHeight() >> 8);
		gfxFile.writeByte(bufImage.getHeight() & 0xff);
		
// Output palette
		byte reds[ ] = new byte[256];
		byte greens[ ] = new byte[256];
		byte blues[ ] = new byte[256];
		
		palette.getReds(reds);
		palette.getGreens(greens);
		palette.getBlues(blues);
		
		for(int x = 0; x < 255; x++)
		{
			gfxFile.writeByte(reds[x]);
			gfxFile.writeByte(greens[x]);
			gfxFile.writeByte(blues[x]);
		}
		
// Output data
		int p = 0;
		int cnt = 0;
		
		for(int x = 0; x < num_pixels; x++)
		{			
			if(pixels[x] != p && x > 0)
			{
				rle_write(gfxFile, p, cnt, dcGfx);
				cnt = 0;
			}
			
			p = pixels[x];
			cnt++;
		}
		
		rle_write(gfxFile, p, cnt, dcGfx);

// End of GFX file		
		gfxFile.writeByte(0xFF);
		gfxFile.writeByte(0xFF);
		
		gfxFile.setLength(gfxFile.getFilePointer());
		gfxFile.close();
	}
}
