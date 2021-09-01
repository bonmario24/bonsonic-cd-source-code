package rsonic;

public class RSObject {
	private int type;
	private int subtype;
	private int xPos;
	private int yPos;
	static int cur_obj_id = 0;
	int id;
	
	public RSObject(int type, int subtype, int xPos, int yPos)
	{
		this(type, subtype, xPos, yPos, cur_obj_id++);
	}
	
	private RSObject(int type, int subtype, int xPos, int yPos, int id)
	{
		this.type = type;
		this.subtype = subtype;
		this.xPos = xPos;
		this.yPos = yPos;
		this.id = id;
	}
	
	public int getType() { return this.type; }
	public void setType(int type) { this.type = type; }
	
	public int getSubtype() { return this.subtype; }
	public void setSubtype(int subtype) { this.subtype = subtype; }
	
	public int getXPos() { return this.xPos; }
	public void setXPos(int xPos) { this.xPos = xPos; }
	
	public int getYPos() { return this.yPos; }
	public void setYPos(int yPos) { this.yPos = yPos; }
	
	public RSObject newInstance()
	{
		return new RSObject(this.type, this.subtype, this.xPos, this.yPos, this.id);
	}
	
	public boolean equals(Object o)
	{
		if(o.getClass() != this.getClass())
			return false;
			
		RSObject obj = (RSObject) o;
		
		return (this.type == obj.type) && (this.subtype == obj.subtype) &&
			(this.xPos == obj.xPos) && (this.yPos == obj.yPos) &&
				(this.id == obj.id);
	}
	
	public String toString()
	{
		return "Type: " + type + ", subtype: " + subtype + ", X pos: " + xPos +
			", Y pos: " + yPos;
	}
}
