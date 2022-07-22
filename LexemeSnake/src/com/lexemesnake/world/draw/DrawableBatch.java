package com.lexemesnake.world.draw;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public interface DrawableBatch
{
	public void draw(Batch batch);
	
	/** 
	 * ��� ��������� ��������� ��������
	 * @return ���������� ������� ��������� 
	 * (���� ���������� null �� ������ �������� ��� �������� �� ���������)
	 */
	public Rectangle getDrawingRectagle();
	
	/**
	 * ��� ����������� ���������
	 * @param renderer ����� ��� ������ �������������� ����������
	 */
	public void debugDraw(ShapeRenderer renderer);
}