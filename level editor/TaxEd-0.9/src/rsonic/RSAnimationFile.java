package rsonic;

import java.io.*;

public class RSAnimationFile
{
	private int imageCount;
	private String imagePaths[ ];
	private int animationCount;
	private RSAnimation animations[ ];
	
	public RSAnimationFile()
	{
		this.imageCount = 0;
		this.animationCount = 0;
		this.imagePaths = new String[0];
		this.animations = new RSAnimation[0];
	}
	
	public RSAnimationFile(String pathname)
		throws Exception
	{
		this(new File(pathname));
	}
	
	public RSAnimationFile(File file)
		throws Exception
	{		
		RandomAccessFile aniFile = 
			new RandomAccessFile(file, "r");

// Read number of image paths		
		this.imageCount = aniFile.readUnsignedByte();
		
		this.imagePaths = new String[this.imageCount];
		
		byte byteBuf[] = null;

		for(int i = 0; i < this.imageCount; i++)
		{
			int sLen = aniFile.readUnsignedByte();
			byteBuf = new byte[sLen];
			
			aniFile.read(byteBuf);

			imagePaths[i] = new String(byteBuf);
		}

// Read number of animations		
		this.animationCount = aniFile.readUnsignedByte();
		this.animations = new RSAnimation[this.animationCount];
		
		for(int i = 0; i < this.animationCount; i++)
		{
			int sLen = aniFile.readUnsignedByte();
			byteBuf = new byte[sLen];
			
			aniFile.read(byteBuf);
			
			String name = new String(byteBuf);

// Length of animation data - 4 bytes + (8 bytes * number_of_frames)
			
// In the 4 bytes:
// byte 1 - Number of frames
// byte 2 - Animation speed
// byte 3 - Frame to start looping from, when looping
// byte 4 - A flag of some kind
//		In Sonic 1, Sonic 2 and Sonic CD, it has value 3 for walking & running animations
//		Coincidentally, for those animations, the frames for the first half of the 
//		animation have the  normal graphics, while the second half 
//		has the rotated sprites
//		(that are displayed when going up a loop or a slope)
//		In Sonic 2, for Twirl H it has value 2

// read frame count			
			int frameCount = aniFile.readUnsignedByte();
			int animationSpeed = aniFile.readUnsignedByte();
			int loopFrom = aniFile.readUnsignedByte();
			
			int buf = aniFile.readUnsignedByte();
			
			boolean flag1 = (buf & 1) > 0;
			boolean flag2 = (buf & 2) > 0;
			
			this.animations[i] = new RSAnimation(name, animationSpeed,
				loopFrom, flag1, flag2);

			RSFrame aniFrames[ ] = new RSFrame[frameCount];

			for(int j = 0; j < frameCount; j++)
			{
// byte 1 - Number of image the frame is located in
// byte 2 - A flag mask of some kind
//		In Sonic 1 and Sonic 2, it has value 3 when crouching down, value 1 when jumping
//		In Sonic CD, it has value 1 for 3D Ramp 7, and value 2 for Size change,
//		and value 1 when jumping.
//		The bitmask most likely is composed by two bits.			
// byte 3 - X position in image of the frame
// byte 4 - Y position in image of the frame
// byte 5 - Width of frame
// byte 6 - Height of frame
// byte 7 - Hot spot horizontal displacement (signed)
// byte 8 - Hot spot vertical displacement (signed)				
				int imageNum = aniFile.readUnsignedByte();
				buf = aniFile.readUnsignedByte();
				flag1 = (buf & 1) > 0;
				flag2 = (buf & 2) > 0;
				int xPos = aniFile.readUnsignedByte();
				int yPos = aniFile.readUnsignedByte();
				int width = aniFile.readUnsignedByte();
				int height = aniFile.readUnsignedByte();
				int hsX = aniFile.readByte();
				int hsY = aniFile.readByte();

				aniFrames[j] =
					new RSFrame(imageNum, flag1, flag2, xPos, yPos,
						width, height, hsX, hsY);
			}
			
			this.animations[i].setFrames(aniFrames);
			
		/*	animations[i] = new RSAnimation( new String(byteBuf), frameCount);
			animations[i].setDebug(debugBuf);
			animations[i].setDebug2(debugBuf2);*/
		}

// Close animation file		
		aniFile.close();
	}
	
	public void write(String pathName)
		throws Exception
	{
		write(new File(pathName));
	}
	
	public void write(File file)
		throws Exception
	{
		RandomAccessFile aniFile = new RandomAccessFile(file, "rw");
		
// Write number of image paths
		aniFile.writeByte( this.imageCount );
		
// Write image paths
		for(int i = 0; i < this.imageCount; i++)
		{
// Write path length			
			aniFile.writeByte( this.imagePaths[i].length() );
// Write path
			aniFile.writeBytes(this.imagePaths[i]);
		}
	
// Write number of animations
		aniFile.writeByte(this.animationCount);

// Write animations		
		for(int i = 0; i < this.animationCount; i++)
		{
			// Write animation name
			aniFile.writeByte( this.animations[i].getName().length() );
			aniFile.writeBytes( this.animations[i].getName() );
			
			// Write frame count
			int frameCount = this.animations[i].getFrameCount();
			aniFile.writeByte(frameCount);
			// Write animation speed
			aniFile.writeByte(this.animations[i].getSpeed());
			// Write loop start
			aniFile.writeByte(this.animations[i].getLoopStart());
			// Pack & write flags
			int buf = this.animations[i].getFlag1()?1:0;
			buf+= this.animations[i].getFlag2()?2:0;
			aniFile.writeByte(buf);
	
			// Write frames
			for(int j = 0; j < frameCount; j++)
			{
				RSFrame frame = this.animations[i].frames[j];
				
				// Write image number
				aniFile.writeByte(frame.getImageNum());
				
				// Pack & write flags
				buf = frame.getFlag1()?1:0;
				buf+= frame.getFlag2()?2:0;
				
				aniFile.writeByte(buf);
				
				// Write X position
				aniFile.writeByte(frame.getXPos());
				
				// Write Y position
				aniFile.writeByte(frame.getYPos());
				
				// Write width
				aniFile.writeByte(frame.getWidth());
				
				// Write height
				aniFile.writeByte(frame.getHeight());
				
				// Write hotspot horizontal displacement
				aniFile.writeByte(frame.getHsX());
				
				// Write hotspot vertical displacement
				aniFile.writeByte(frame.getHsY());
			}
		}
			
// Close file		
		aniFile.setLength(aniFile.getFilePointer());
		aniFile.close();
	}
	
	public static RSAnimationFile importRSFormat(File file)
		throws Exception
	{
// Imports the old format used in Retro-Sonic (SAGE 2007 DEMO)
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		RSAnimationFile animationFile = new RSAnimationFile();
		String imagePaths[] = new String[3];
		byte byteBuf[] = null;
		
		rf.skipBytes(2); // Skip two bytes

// Read number of animations		
		int numAnim = rf.readUnsignedByte();

// Read image paths (there are always three image paths in this format)		
		for(int i = 0; i < 3; i++)
		{
			int len = rf.readUnsignedByte();
			byteBuf = new byte[len];
			rf.read(byteBuf);
			imagePaths[i] = new String(byteBuf);
		}
		
// Read animations
		RSAnimation animations[] = new RSAnimation[numAnim];
				
		for(int i = 0; i < numAnim; i++)
		{
			// Read number of frames
			int numFrames = rf.readUnsignedByte();
			// Read speed
			// Speed values are roughly a fourth of those used in Sonic CD/1/2
			int speed = rf.readUnsignedByte() * 4;
			// Read loop start
			int loopStart = rf.readUnsignedByte();
			
			animations[i] = new RSAnimation("Animation #"+(i+1), speed, loopStart, false, false);
			RSFrame frames[] = new RSFrame[numFrames];
			
			int v[] = new int[6];
			
			for(int j = 0; j < numFrames; j++)
			{
				int xPos = rf.readUnsignedByte();
				int yPos = rf.readUnsignedByte();
				int width = rf.readUnsignedByte();
				int height = rf.readUnsignedByte();
				int imageNum = rf.readUnsignedByte();
				boolean flag1 = false; // UNKNOWN
				boolean flag2 = false; // UNKNOWN
				
				// the meaning of v[0] and v[1] is currently unknown
				
				for(int k = 0; k < 6; k++)
					v[k] = rf.readByte();
				
				// Compute hotspot displacements
				int hsX = v[2] - v[4];
				int hsY = v[3] - v[5];
				
				// This is odd, but it means that the animation is empty.
				if(width == 0 && height == 0)
				{
					frames = new RSFrame[0];
					break;
				}
				
				frames[j] = new RSFrame(imageNum, flag1, flag2, 
					xPos, yPos, width, height, hsX, hsY);
			}
			
			animations[i].setFrames(frames);
		}
		
		animationFile.setImagePaths(imagePaths);
		animationFile.setAnimations(animations);
		
		return animationFile;
	}

	public static RSAnimationFile importRSDCFormat(File file)
		throws Exception
	{
// Imports the old format used in Retro-Sonic Dreamcast (SAGE 2006 DEMO)
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		RSAnimationFile animationFile = new RSAnimationFile();
		String imagePaths[] = new String[2];
		byte byteBuf[] = null;
		
		rf.skipBytes(2); // Skip two bytes

// Read number of animations		
		int numAnim = rf.readUnsignedByte();

// Read image paths (there are always two image paths in this format)		
		for(int i = 0; i < 2; i++)
		{
			int len = rf.readUnsignedByte();
			byteBuf = new byte[len];
			rf.read(byteBuf);
			imagePaths[i] = new String(byteBuf);
		}
		
// Read animations
		RSAnimation animations[] = new RSAnimation[numAnim];
				
		for(int i = 0; i < numAnim; i++)
		{
			// Read number of frames
			int numFrames = rf.readUnsignedByte();
			// Read speed
			// Speed values are roughly a fourth of those used in Sonic CD/1/2
			int speed = rf.readUnsignedByte() * 4;
			// Read loop start
			int loopStart = rf.readUnsignedByte();
			
			animations[i] = new RSAnimation("Animation #"+(i+1), speed, loopStart, false, false);
			RSFrame frames[] = new RSFrame[numFrames];
			
			int v[] = new int[6];
			
			for(int j = 0; j < numFrames; j++)
			{
				int xPos = rf.readUnsignedByte();
				int yPos = rf.readUnsignedByte();
				int width = rf.readUnsignedByte();
				int height = rf.readUnsignedByte();
				int imageNum = rf.readUnsignedByte();
				boolean flag1 = false; // UNKNOWN
				boolean flag2 = false; // UNKNOWN
				
				// the meaning of v[0] and v[1] is currently unknown
				
				for(int k = 0; k < 6; k++)
					v[k] = rf.readByte();
				
				// Compute hotspot displacements
				int hsX = v[2] - v[4];
				int hsY = v[3] - v[5];
				
				// This is odd, but it means that the animation is empty.
				if(width == 0 && height == 0)
				{
					frames = new RSFrame[0];
					break;
				}
				
				frames[j] = new RSFrame(imageNum, flag1, flag2, 
					xPos, yPos, width, height, hsX, hsY);
			}
			
			animations[i].setFrames(frames);
		}
		
		animationFile.setImagePaths(imagePaths);
		animationFile.setAnimations(animations);
		
		return animationFile;
	}
	
	public static RSAnimationFile importNexusFormat(File file)
		throws Exception
	{
// Imports the old format used in Sonic Nexus SAGE 2008 DEMO
// This format looks more like the one used in Sonic CD/1/2,
// compared to the one used by Retro-Sonic 2007 and DC.		
		
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		RSAnimationFile animationFile = new RSAnimationFile();
		String imagePaths[] = new String[3];
		byte byteBuf[] = null;
		
		rf.skipBytes(5); // Skip five bytes

// Read image paths (there are always three image paths in this format)		
		for(int i = 0; i < 3; i++)
		{
			int len = rf.readUnsignedByte();
			byteBuf = new byte[len];
			rf.read(byteBuf);
			imagePaths[i] = new String(byteBuf);
		}

// Skip a byte
		rf.skipBytes(1);
		
// Read number of animations		
		int numAnim = rf.readUnsignedByte();
		
// Read animations
		RSAnimation animations[] = new RSAnimation[numAnim];
				
		for(int i = 0; i < numAnim; i++)
		{
			// Read number of frames
			int numFrames = rf.readUnsignedByte();
			// Read speed
			int speed = rf.readUnsignedByte();
			// Read loop start
			int loopStart = rf.readUnsignedByte();
			
			animations[i] = new RSAnimation("Animation #"+(i+1), speed, loopStart, false, false);
			RSFrame frames[] = new RSFrame[numFrames];
			
			int v[] = new int[6];
			
			for(int j = 0; j < numFrames; j++)
			{
				int imageNum = rf.readUnsignedByte();
				rf.skipBytes(1);
				int xPos = rf.readUnsignedByte();
				int yPos = rf.readUnsignedByte();
				int width = rf.readUnsignedByte();
				int height = rf.readUnsignedByte();
				int hsX = rf.readByte();
				int hsY = rf.readByte();
				boolean flag1 = false; // UNKNOWN
				boolean flag2 = false; // UNKNOWN

				frames[j] = new RSFrame(imageNum, flag1, flag2, 
					xPos, yPos, width, height, hsX, hsY);
			}
			
			animations[i].setFrames(frames);
		}
		
		animationFile.setImagePaths(imagePaths);
		animationFile.setAnimations(animations);
		
		return animationFile;
	}
	
	public int getImageCount()
	{
		return imageCount;
	}
	
	public String[ ] getImagePaths()
	{
		String newArr[ ] = new String[imageCount];
		
		for(int i = 0; i < imageCount; i++)
			newArr[i] = new String(imagePaths[i]);
		
		return newArr;
	}
	
	public  void setImagePaths(String[] paths)
	{
		String newArr[ ] = new String[paths.length];
		
		for(int i = 0; i < paths.length; i++)
			newArr[i] = new String(paths[i]);
		
		imageCount = paths.length;
		this.imagePaths = newArr;
	}
	
	public int getAnimationCount()
	{
		return animationCount;
	}
	
	public RSAnimation[ ] getAnimations()
	{
		if(animations == null)
			return null;
		
		RSAnimation newArr[ ] = new RSAnimation[animationCount];
		
		for(int i = 0; i < animationCount; i++)
			newArr[i] = animations[i].newInstance();
		
		return newArr;
	}
	
	public void setAnimations(RSAnimation[] animations)
	{
		RSAnimation newArr[ ] = new RSAnimation[animations.length];
		
		for(int i = 0; i < animations.length; i++)
			newArr[i] = animations[i].newInstance();
		
		animationCount = animations.length;
		this.animations = newArr;
	}
}
