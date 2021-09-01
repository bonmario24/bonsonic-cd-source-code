/**
 * This class exports an animation file to JSON format.
 * The JSON data is exported directly, so every change to the public API of
 * rsonic.RSAnimationFile needs to be reflected in this file, too.
 */

package taxani;

import rsonic.*;
import java.io.*;
import java.util.*;

public class AniJSON
{
	public static void write(String pathname, RSAnimationFile aniFile)
		throws IOException
	{
		write(new File(pathname), aniFile);
	}

	public static void write(File file, RSAnimationFile aniFile)
		throws IOException
	{
		PrintStream out = new PrintStream(file);
		write(out, aniFile);
		out.flush();
		out.close();
	}
	
	public static String stringify(RSAnimationFile aniFile)
	{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		
		try
		{
			write(out, aniFile);
		}
		
		catch(IOException e)
		{
			// No exception should happen..
		}
			
		return outStream.toString();
	}
	
	public static void write(PrintStream out, RSAnimationFile aniFile)
		throws IOException
	{
		String ind = "";
		
		out.println(ind+"{");
		
// Write image paths
		ind="\t";
		out.println(ind+"\"imagePaths\":");
		out.println(ind+"[");
		
		ind="\t\t";
		
		int imageCount = aniFile.getImageCount();
		String imagePaths[] = aniFile.getImagePaths();
		
		for(int i = 0; i < imageCount; i++)
		{
			out.print(ind);
// Write path
			out.print("\"" + imagePaths[i] + "\"");
			
			if((i + 1) < imageCount)
				out.println(",");
			else
				out.println();
		}
		
		ind = "\t";
		out.println(ind+"]");
		out.println();
		
		out.println(ind +"\"animations\":");
		out.println(ind + "[");
		
// Write animations		
		RSAnimation[] animations = aniFile.getAnimations();
		int animationCount = aniFile.getAnimationCount();
		
		for(int i = 0; i < animationCount; i++)
		{
			ind = "\t\t";
			out.println(ind + "{");
			
			ind = "\t\t\t";
			out.print(ind);
			out.println("\"name\": \""+ animations[i].getName() + "\"");
			out.print(ind);
			out.println("\"speed\": " + animations[i].getSpeed() );
			out.print(ind);
			out.println("\"loopStart\": " + animations[i].getLoopStart() );
			out.print(ind);
			out.println("\"flag1\": " + (animations[i].getFlag1() ? "true" : "false") );
			out.print(ind);
			out.println("\"flag2\": " + (animations[i].getFlag2() ? "true" : "false") );
			
			out.println(ind + "\"frames\":");
			out.println(ind + "[");
			
			int frameCount = animations[i].getFrameCount();
			
			// Write frames
			RSFrame[] frames = animations[i].getFrames();
			
			for(int j = 0; j < frameCount; j++)
			{
				RSFrame frame = frames[j];

				ind = "\t\t\t\t";
				
				out.println(ind + "{");

				ind = "\t\t\t\t\t";
				
				out.println(ind + "\"imageNum\": " + frame.getImageNum());
				out.println(ind + "\"flag1\": " + (frame.getFlag1() ? "true" : "false" ) );
				out.println(ind + "\"flag2\": " + (frame.getFlag2() ? "true" : "false") );
				out.println(ind + "\"xPos\": " + frame.getXPos());
				out.println(ind + "\"yPos\": " + frame.getYPos());
				out.println(ind + "\"width\": " + frame.getWidth());
				out.println(ind + "\"height\": " + frame.getHeight());
				out.println(ind + "\"hsX\": " + frame.getHsX());
				out.println(ind + "\"hsY\": " + frame.getHsY());
				
				ind = "\t\t\t\t";
				out.print(ind + "}");
				
				if((j + 1) < frameCount)
					out.println(",");
				else
					out.println();
			}
			
			ind = "\t\t\t";
			out.println(ind + "]");
			ind = "\t\t";
			out.print(ind + "}");
			
			if((i + 1) < animationCount)
				out.println(",");
			else
				out.println();
		}
		
		ind = "\t";
		out.println(ind + "]");
		ind = "";
		out.println("}");		
	}
}
