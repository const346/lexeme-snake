package com.lexemesnake.world.draw;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public interface DrawableBatch
{
	public void draw(Batch batch);
	
	/** 
	 * Для отсечения невидимых объектов
	 * @return Возвращает область рисования 
	 * (если возвращает null то объект рисуется без проверки на отсечение)
	 */
	public Rectangle getDrawingRectagle();
	
	/**
	 * Для отладочного рисования
	 * @param renderer класс для вывода геометрических примитивов
	 */
	public void debugDraw(ShapeRenderer renderer);
}