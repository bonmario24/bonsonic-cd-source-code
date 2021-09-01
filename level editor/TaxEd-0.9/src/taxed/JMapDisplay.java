package taxed;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.*;
import java.io.*;
import java.util.*;
import rsonic.*;

public class JMapDisplay extends JPanel 
{
	private String directory;
	boolean ready;
	BufferedImage blockImages[];
	Image scaledBlockImages[];
	private int[][] mapData;
	private java.util.List<RSObject> objList;
	private int mapWidth;
	private int mapHeight;
	private int selX, selY;
	private int selObjX, selObjY;
	private boolean selObj;
	private Image testImage;

	private void draw16(Graphics g, int x, int y, int t, Image tilesImg[])
	{
		int n = t & 1023;
		boolean h = ((t & 1024)) > 0;
		boolean v = ((t & 2048)) > 0;

		if(v)
			n = 1023 - n;

		g.drawImage(tilesImg[(h?1:0)+(v?2:0)], x, y, x+16, y+16, 0, n * 16, 16, (n*16)+16, this); 
	}
	
	private void draw128(Graphics g, int x, int y, int n, Image tilesImg[],
		int _128Tiles[][][])
	{
		assert((n >= 0) && (n <= 511));
		
		for(int y2 = 0; y2 < 8; y2++)
			for(int x2 = 0; x2 < 8; x2++)
				draw16(g, x+(x2*16), y+(y2*16), _128Tiles[n][y2][x2], tilesImg);
	}
	
	public void paintMap(Graphics g)
	{
		g.setColor(Color.blue);
		g.fillRect(0, 0, getWidth(), getHeight());		
			
		int mapX = 0;
		int mapY = 0;
			
		for(int y = 0; y < mapHeight; y++)
			for(int x = 0; x < mapWidth; x++)
				g.drawImage(scaledBlockImages[mapData[y][x]], mapX + (x * 32), 
					mapY + (y * 32), this);
			
		g.setColor(Color.green);
	}
	
	protected void paintComponent(Graphics g)
	{	
		if(!ready)
		{
			g.setColor(Color.black);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.white);
			g.drawString("No map loaded."+
						 " To open a map, select File->Open", 32, 32);
		}
		else
		{	
			//g.drawImage(testImage, 0, 0, this);
			
			g.setColor(Color.blue);
			g.fillRect(0, 0, getWidth(), getHeight());		
			
			int mapX = 0;
			int mapY = 0;
			
			for(int y = 0; y < mapHeight; y++)
				for(int x = 0; x < mapWidth; x++)
					g.drawImage(blockImages[mapData[y][x]], mapX + (x * 128), 
						mapY + (y * 128), this);
			
			g.setColor(Color.green);
			
			for(RSObject obj : objList)
				g.fillRect(mapX + obj.getXPos(), mapY + obj.getYPos(), 16, 32);
						
			g.setColor(Color.red);
			
			g.draw3DRect(mapX + (selX * 128), mapY + (selY * 128), 128, 128, true);

			if(selObj)
			{
				g.setColor(Color.blue);
				g.draw3DRect(mapX + selObjX, mapY + selObjY, 16, 32, true);
			}
		}
	}
	
	public void setMapData(int[][] mapData)
	{
		this.mapData = mapData;
		this.mapWidth = mapData[0].length;
		this.mapHeight = mapData.length;
		
		setPreferredSize(new Dimension(mapWidth * 128, mapHeight * 128));
	}
	
	public void setObjectList(java.util.List<RSObject> objList)
	{
		this.objList = objList;
	}
	
	public void loadOtherData(File file, int loadType)
		throws Exception
	{		
		directory = file.getParent();
		
		if(loadType == RSMap.TYPE_RS)
			reloadOtherData_RS();
		else
			reloadOtherData();
	}
	
	public void reloadOtherData()
		throws Exception
	{
		BufferedImage tilesImage[] = new BufferedImage[4];
		int _128Tiles[][][] = new int[512][8][8];
	
		BufferedImage _16tiles = ImageIO.read(new File(directory + File.separator + "16x16Tiles.gif"));
	
		tilesImage[0] = new BufferedImage(_16tiles.getWidth(), _16tiles.getHeight(),
			BufferedImage.TYPE_INT_ARGB);
		
		int _width = _16tiles.getWidth();
		int _height = _16tiles.getHeight();
		
		tilesImage[0].getGraphics().drawImage(_16tiles, 0, 0, this);
		
		for(int y = 0; y < _height; y++)
		{
			for(int x = 0; x < _width; x++)
			{
				int p = tilesImage[0].getRGB(x, y);
				
				if(p == 0xFFFF00FF)
					p = 0x00FF00FF;
				
				tilesImage[0].setRGB(x, y, p);
			}
		}
		
		tilesImage[1] = flipImage(tilesImage[0], true, false);
		tilesImage[2] = flipImage(tilesImage[0], false, true);
		tilesImage[3] = flipImage(tilesImage[0], true, true);	
		
		RandomAccessFile file = new RandomAccessFile(directory + File.separator + "128x128Tiles.bin", "r");
		
		_128Tiles = new int[512][8][8];
		
		for(int n = 0; n < 512; n++)
		{
			for(int y = 0; y < 8; y++)
			{
				for(int x = 0; x < 8; x++)
				{
					_128Tiles[n][y][x] = file.readUnsignedByte() << 8;
					_128Tiles[n][y][x] |= file.readUnsignedByte();
					file.skipBytes(1);
				}
			}
		}
		
		file.close();
		
		blockImages = new BufferedImage[512];
		scaledBlockImages = new Image[512];
		int type = ((BufferedImage)tilesImage[0]).getType();
		ColorModel colorModel = ((BufferedImage)tilesImage[0]).getColorModel();
		
		for(int n = 0; n < 512; n++)
		{
			if(colorModel instanceof IndexColorModel)
				blockImages[n] = new BufferedImage(128, 128, type, 
					(IndexColorModel)colorModel);
			else
				blockImages[n] = new BufferedImage(128, 128, type);
	
			draw128(blockImages[n].getGraphics(), 0, 0, n, tilesImage, _128Tiles);
			
			scaledBlockImages[n] = blockImages[n].getScaledInstance(32, 32, Image.SCALE_FAST);
		}
		
		repaint();
		
		ready = true;
	}
	
	public void reloadOtherData_RS()
		throws Exception
	{
		Image tilesImage[] = new Image[4];
		int _128Tiles[][][] = new int[512][8][8];
	
		try
		{
			tilesImage[0] = RSGfxImage.loadGfx(directory + File.separator + "Zone.gfx", false);
		}
		
		catch(Exception exc1)
		{
			tilesImage[0] = RSGfxImage.loadGfx(directory + File.separator + "Zonel1.gfx", true);
		}
		
		testImage = tilesImage[0];
		tilesImage[1] = flipImage(tilesImage[0], true, false);
		tilesImage[2] = flipImage(tilesImage[0], false, true);
		tilesImage[3] = flipImage(tilesImage[0], true, true);	
		
		RandomAccessFile file = new RandomAccessFile(directory + File.separator + "Zone.til", "r");
		
		_128Tiles = new int[256][8][8];
		
		for(int n = 0; n < 256; n++)
		{
			for(int y = 0; y < 8; y++)
			{
				for(int x = 0; x < 8; x++)
				{
					_128Tiles[n][y][x] = file.readUnsignedByte() << 8;
					_128Tiles[n][y][x] |= file.readUnsignedByte();
					file.skipBytes(1);
				}
			}
		}
		
		file.close();
		
		blockImages = new BufferedImage[512];
		scaledBlockImages = new Image[512];
		ColorModel colorModel = ((BufferedImage)tilesImage[0]).getColorModel();
		
		for(int n = 0; n < 256; n++)
		{
			blockImages[n] = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			
			blockImages[n+256] = blockImages[n];
	
			draw128(blockImages[n].getGraphics(), 0, 0, n, tilesImage, _128Tiles);
			
			scaledBlockImages[n] = blockImages[n].getScaledInstance(32, 32, Image.SCALE_FAST);
			scaledBlockImages[n+256] = scaledBlockImages[n];
		}
		
		repaint();
		
		ready = true;
	}
	
	public BufferedImage flipImage(Image image, boolean h, boolean v)
	{	
		if(!(image instanceof BufferedImage))
			throw new RuntimeException("Not a BufferedImage!");
			
		BufferedImage bufImage = (BufferedImage)image;	
			
		ColorModel colorModel = bufImage.getColorModel();
		BufferedImage newImage = null;
		int width = bufImage.getWidth();
		int height = bufImage.getHeight();
		int type = bufImage.getType();
		
		if(colorModel instanceof IndexColorModel)
			newImage = new BufferedImage(width, height, type,
				(IndexColorModel)colorModel);
		else
			newImage = new BufferedImage(width, height, type);
		
		Raster inRaster = bufImage.getRaster();
		WritableRaster outRaster = newImage.getRaster();
			
		int px[] = new int[4];
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				inRaster.getPixel(h?(width-1-x):x, v?(height-1-y):y, px);
				outRaster.setPixel(x, y, px);
			}
		}
		
		return newImage;
	}
	
	public void setSelectedCoords(int x, int y)
	{
		this.selX = x;
		this.selY = y;
		
		repaint();
	}
	
	public void setSelectedObjectCoords(int x, int y)
	{
		this.selObjX = x;
		this.selObjY = y;
		this.selObj = true;
		
		repaint();
	}
	
	public int getSelectedX()
	{
		return this.selX;
	}
	
	public int getSelectedY()
	{
		return this.selY;
	}
	
	public int getSelectedObjectX()
	{
		return this.selObjX;
	}
	
	public int getSelectedObjectY()
	{
		return this.selObjY;
	}
	
	public boolean isObjectedSelected()
	{
		return this.selObj;
	}
	
	public void setNoObjectSelected()
	{
		this.selObj = false;
		
		repaint();
	}
	
	public JMapDisplay()
	{
		this.selX = 0;
		this.selY = 0;
	}
}
