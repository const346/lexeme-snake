package com.lexemesnake.utils;

import java.util.ArrayList;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;

/**
 * �������� ������ ����������� ������
 * @author ��������� ������� (medhaid)
 */
public class TileLayerOverlap
{
	static public interface CellFilter
	{
		public boolean filter(Cell cell);
	}

	static private float[] cellVertex = new float[8];
	
	/**
	 * @param layer	- ����
	 * @param convexPolygon - �������� �������
	 * @param cellFilter - ������
	 * @return true ���� ���� �� ���� ������ �������� convexPolygon
	 */
	static public boolean overlapCell(TiledMapTileLayer layer, Polygon convexPolygon, CellFilter cellFilter)
	{
		Rectangle rect = convexPolygon.getBoundingRectangle();
		
		int tileW = (int)layer.getTileWidth();
		int tileH = (int)layer.getTileHeight();
		
		int x1 = (int)rect.x / tileW;
		int y1 = (int)rect.y / tileH;
		int x2 = (int)(rect.x + rect.width) / tileW;
		int y2 = (int)(rect.y + rect.height) / tileH;
		
		for (int iy = y1; iy <= y2; iy++)
		{
			for (int ix = x1; ix <= x2; ix++)
			{
				Cell cell = layer.getCell(ix, iy);
				if (cell != null)
				{
					if (cellFilter == null || cellFilter.filter(cell))
					{
						//����� ������
						cellVertex[0] = (ix+0) * tileW; //x1
						cellVertex[1] = (iy+0) * tileH; //y1
						//����� �������
						cellVertex[2] = (ix+0) * tileW; //x2
						cellVertex[3] = (iy+1) * tileH; //y2
						//������ �������
						cellVertex[4] = (ix+1) * tileW; //x3
						cellVertex[5] = (iy+1) * tileH; //y3
						//������ ������
						cellVertex[6] = (ix+1) * tileW; //x4
						cellVertex[7] = (iy+0) * tileH; //y4
						
						float[] vertex = convexPolygon.getTransformedVertices();
						if (Intersector.overlapConvexPolygons(cellVertex, vertex, null))
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * @param layer	- ����
	 * @param convexPolygon - �������� �������
	 * @return true ���� ���� �� ���� ������ �������� convexPolygon
	 */
	static public boolean overlapCell(TiledMapTileLayer layer, Polygon convexPolygon)
	{
		return overlapCell(layer, convexPolygon, null);
	}
	
	/**
	 * @param layer	- ����
	 * @param convexPolygon - �������� �������
	 * @param cellFilter - ������
	 * @return ��� ������ �� ���� ������� ��������� ��� �������� ����������� �������� �������
	 */
	static public ArrayList<Cell> overlapCell2(TiledMapTileLayer layer, Polygon convexPolygon, CellFilter cellFilter)
	{
		Rectangle rect = convexPolygon.getBoundingRectangle();
		ArrayList<Cell> cells = new ArrayList<Cell>();
			
		int tileW = (int)layer.getTileWidth();
		int tileH = (int)layer.getTileHeight();
		
		int x1 = (int)rect.x / tileW;
		int y1 = (int)rect.y / tileH;
		int x2 = (int)(rect.x + rect.width) / tileW;
		int y2 = (int)(rect.y + rect.height) / tileH;
		
		for (int iy = y1; iy <= y2; iy++)
		{
			for (int ix = x1; ix <= x2; ix++)
			{
				Cell cell = layer.getCell(ix, iy);
				if (cell != null)
				{
					if (cellFilter == null || cellFilter.filter(cell))
					{
						//����� ������
						cellVertex[0] = (ix+0) * tileW; //x1
						cellVertex[1] = (iy+0) * tileH; //y1
						//����� �������
						cellVertex[2] = (ix+0) * tileW; //x2
						cellVertex[3] = (iy+1) * tileH; //y2
						//������ �������
						cellVertex[4] = (ix+1) * tileW; //x3
						cellVertex[5] = (iy+1) * tileH; //y3
						//������ ������
						cellVertex[6] = (ix+1) * tileW; //x4
						cellVertex[7] = (iy+0) * tileH; //y4
						
						float[] vertex = convexPolygon.getTransformedVertices();
						if (Intersector.overlapConvexPolygons(cellVertex, vertex, null))
						{
							cells.add(cell);
						}
					}
				}
			}
		}
		
		return cells;
	}
	
	/**
	 * @param layer	- ����
	 * @param convexPolygon - �������� �������
	 * @return ��� ������ �� ���� ������� ��������� ��� �������� ����������� �������� �������
	 */
	static public ArrayList<Cell> overlapCell2(TiledMapTileLayer layer, Polygon convexPolygon)
	{
		return overlapCell2(layer, convexPolygon, null);
	}
}
