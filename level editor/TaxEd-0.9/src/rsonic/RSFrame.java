package rsonic;

public class RSFrame
{
	private int imageNum;
	private boolean flag1;
	private boolean flag2;
	private int xPos;
	private int yPos;
	private int width;
	private int height;
	private int hsX;
	private int hsY;
	
	public RSFrame(int imageNum, boolean flag1, boolean flag2,
					int xPos, int yPos, int width, int height, int hsX, int hsY)
	{
		this.imageNum = imageNum;
		this.flag1 = flag1;
		this.flag2 = flag2;
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.hsX = hsX;
		this.hsY = hsY;
	}
	
	public RSFrame newInstance()
	{
		return new RSFrame(this.imageNum, this.flag1, this.flag2,
							this.xPos, this.yPos, this.width, this.height,
							this.hsX, this.hsY);
	}
	
	public int getImageNum() { return this.imageNum; }
	public boolean getFlag1() { return this.flag1; }
	public boolean getFlag2() { return this.flag2; }
	public int getXPos() { return this.xPos; }
	public int getYPos() { return this.yPos; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public int getHsX() { return this.hsX; }
	public int getHsY() { return this.hsY; }
	
	public void setImageNum(int imageNum) { this.imageNum = imageNum; }
	public void setFlag1(boolean flag1) { this.flag1 = flag1; }
	public void setFlag2(boolean flag2) { this.flag2 = flag2; }
	public void setXPos(int xPos) { this.xPos = xPos; }
	public void setYPos(int yPos) { this.yPos = yPos; }
	public void setWidth(int width) { this.width = width; }
	public void setHeight(int height) { this.height = height; }
	public void setHsX(int hsX) { this.hsX = hsX; }
	public void setHsY(int hsY) { this.hsY = hsY; }
}
