package rsonic;

public class RSAnimation
{
	private String name;
	private int frameCount;
	private int speed;
	private int loopStart;
	private boolean flag1;
	private boolean flag2;
//	private byte[] debugData;
//	private byte[][] debugData2;
	RSFrame[ ] frames;
	
	public RSAnimation()
	{
	}
	
	public RSAnimation(String name, int speed,
					int loopStart, boolean flag1, boolean flag2)
	{
		this.name = name;
		this.speed = speed;
		this.loopStart = loopStart;
		this.flag1 = flag1;
		this.flag2 = flag2;
		this.frameCount = frameCount;
		this.frames = new RSFrame[0];
	}
	
	public RSAnimation newInstance()
	{
		RSAnimation newAnimation = 
			new RSAnimation(this.name, this.speed, this.loopStart,
					this.flag1, this.flag2);
		
		newAnimation.frameCount = this.frameCount;
		newAnimation.frames = new RSFrame[this.frameCount];
		
		for(int i = 0; i < this.frameCount; i++)
			newAnimation.frames[i] = this.frames[i].newInstance();
		
		return newAnimation;
		
	/*	byte[ ] debugData = new byte[this.debugData.length];
		byte[ ][ ] debugData2 = new byte[this.debugData2.length][8];
		
		for(int i = 0; i < this.debugData.length; i++)
			debugData[i] = this.debugData[i];
		
		for(int j = 0; j < this.debugData2.length; j++)
			for(int i = 0; i < 8; i++)
				debugData2[j][i] = this.debugData2[j][i];
		
		newAnimation.setDebug(debugData);
		newAnimation.setDebug2(debugData2);*/
	}
	
	public String getName() { return this.name; }
	public int getSpeed() { return this.speed; }
	public boolean getFlag1() { return this.flag1; }
	public boolean getFlag2() { return this.flag2; }
	public int getFrameCount() { return this.frameCount; }
	public int getLoopStart() { return this.loopStart; }
	
	public RSFrame[ ] getFrames()
	{
		if(this.frames == null)
			return null;
		
		RSFrame newArr[ ] = new RSFrame[this.frameCount];
		
		for(int i = 0; i < this.frameCount; i++)
			newArr[i] = this.frames[i].newInstance();
		
		return newArr;
	}
		
	public void setName(String name) { this.name = name; }
	public void setSpeed(int speed) { this.speed = speed; }
	public void setFlag1(boolean flag1) { this.flag1 = flag1; }
	public void setFlag2(boolean flag2) { this.flag2 = flag2; }
	public void setLoopStart(int loopStart) { this.loopStart = loopStart; }
	
	public void setFrames(RSFrame[ ] frames)
	{
		RSFrame newArr[ ] = new RSFrame[frames.length];
	
		for(int i = 0; i < frames.length; i++)
			newArr[i] = frames[i].newInstance();
		
		this.frameCount = frames.length;
		this.frames = newArr;
	}
		
	/*void setDebug(byte[] data)
	{
		this.debugData = data;
	}
	
	void setDebug2(byte[][] data)
	{
		this.debugData2 = data;
	}
	
	public void printDebug()
	{
		System.out.print("Debug:\n");
		for(byte d : debugData)
			System.out.printf("%02X ", (int)d & 0xff);
		
		System.out.print("\nDebug2:\n");
		
		for(byte[] da : debugData2)
		{
			for(byte d : da)
				System.out.printf("%02X ", (int)d & 0xff);
		
			System.out.println();
		}
		
		System.out.println();
	}*/
}
