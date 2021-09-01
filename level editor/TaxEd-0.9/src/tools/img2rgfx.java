package tools;

import rsonic.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class img2rgfx
{
	public static void main(String args[])
	{
		boolean dcGfx = false;
		
		if(args.length < 1)
		{
			System.out.println("img2rgfx [options] <image file> <gfx file>");
			System.out.println();
			System.out.println("Options:");
			System.out.println("    -dc - Output will be in Dreamcast version");
			System.out.println();
			System.out.println("This program converts an image file to Retro-Sonic GFX format.");
			System.out.print("Supported input formats:");
			
			for(String fmt : ImageIO.getReaderFileSuffixes())
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
			System.out.println("Converting...");
			
			BufferedImage inImage = ImageIO.read(new File(args[arg_start]));
			
			if(inImage == null)
				throw new Exception("The format of the input image is not supported.");
			
			RSGfxImage.writeGfx(args[arg_start + 1], inImage, dcGfx);
			
			System.out.println("Done.");
		}
		
		catch(Exception exc)
		{
			System.out.println("Error: " + exc.getMessage());
			System.exit(-1);
		}
	}
}
