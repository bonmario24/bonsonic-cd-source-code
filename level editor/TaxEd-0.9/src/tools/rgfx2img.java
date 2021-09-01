package tools;

import rsonic.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class rgfx2img
{
	public static void main(String args[])
	{
		boolean dcGfx = false;
		
		if(args.length < 1)
		{
			System.out.println("rgfx2img [options] <gfx file> <image file>");
			System.out.println();
			System.out.println("This program converts an image in Retro-Sonic GFX format to an image file.");
			System.out.println();
			System.out.println("Options:");
			System.out.println("    -dc - Input is in Dreamcast version");
			System.out.println();
			System.out.println("The format of the output image file is determined by the extension.");
			System.out.print("Supported output formats:");
			
			for(String fmt : ImageIO.getWriterFileSuffixes())
				System.out.print(" " + fmt);
			
			System.out.println();
			
			System.exit(0);
		}
		
		int arg_start = 0;
		
		if(args.length >= 3)
		{
			if(args[0].equals("-dc"))
			{
				dcGfx = true;
				arg_start++;
			}
		}
		
		try
		{
			int i = 0;
			
			if((i = args[arg_start+1].lastIndexOf('.')) == -1)
				throw new Exception("Path of output image file has no extension.");
			
			String extension = args[arg_start+1].substring(i+1);

			System.out.println("Converting...");
			if(ImageIO.write((RenderedImage)RSGfxImage.loadGfx(args[arg_start], dcGfx), extension, new File(args[arg_start+1])))
				System.out.println("Done.");
			else
				throw new Exception("Format not supported for writing.");
		}
		
		catch(Exception exc)
		{
			System.out.println("Error: " + exc.getMessage());
			System.exit(-1);
		}
	}
}
