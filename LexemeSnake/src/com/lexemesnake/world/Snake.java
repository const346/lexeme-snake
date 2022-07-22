package com.lexemesnake.world;

import java.util.ArrayDeque;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import com.lexemesnake.world.draw.DrawableBatch;

/**
 * Основной игровой персонаж (Змейка)
 * @author	Кравченко Алексей (medhaid)
 * @see Телевизор
 */
public class Snake implements DrawableBatch
{
	private float SPAN_DISTANCE = 14*1;	//Минимальная дистанция между статическими костями скелета
	private float DELTA_DEGREES = 16*1;	//Максимальный угол поворота за шаг
	private float MAX_WIDTH 	= 16*1;	//Максимальная ширина змеи
	private float NECK_WIDTH 	= 11*1;	//Ширина в месте стыка головы с хвостом
	private float REPEAT_SIZE 	= 40*1;	//Размер повторяемой текстуры змейки (не менее чем SPAN_DISTANCE)
	
	/** Кость, содержит в себе координаты и данные для 
	 * генерации шкуры, которые можно вычислить при создании */
	static private class  Bone
	{
		Bone()
		{
			this.position = new Vector2();
			this.normal = new Vector2();
		}
		
		Vector2 position;
		Vector2 normal;
	}
	
	//Скелет содержит в себе минимум 3 кости
	//две движимые с краёв и N статичных внутри
	private ArrayDeque<Bone>	skeleton;
	
	private float 	shiftFirst;
	private float	shiftLast;
	private float 	angle;
	private float 	deprees;
	private int 	neededSize;
	
	private float velocity;
	private float velocityFactor;
	
	//Для рисования змейки и проверки столкновений
	//(формат хранения вершин стандартный для libgdx при рисовании на SpriteBatch.)
	private FloatArray vertices;
	
	//Текстуры
	private TextureRegion texTail;
	private TextureRegion texHead;
	
	//Форма головы (для проверки столкновений)
	private Polygon headShape;
	
	public Snake(final Vector2 pos, float angle, int neededSize, float velocity, TextureRegion texHead, TextureRegion texTail)
	{
		vertices = new FloatArray(2000);
		skeleton = new ArrayDeque<Bone>();
		
		setVelocity(velocity);
		
		this.texTail = texTail;
		this.texHead = texHead;
		
		//Фигура головы змейки
		float neck = (NECK_WIDTH /MAX_WIDTH);
		float[] vertices = new float[14];
		vertices[0]  = 0;		//x1
		vertices[1]  = -neck;	//y1
		vertices[2]  = 0;		//x2
		vertices[3]  = +neck;	//y2
		vertices[4]  = 1.5f;	//x3
		vertices[5]  = 1;		//y3
		vertices[6]  = 3;		//x4
		vertices[7]  = 0.45f;	//y4
		vertices[8]  = 3.14f;	//x5
		vertices[9]  = 0;		//y5
		vertices[10] = 3;		//x6
		vertices[11] = -0.45f;	//y6
		vertices[12] = 1.5f;	//x7
		vertices[13] = -1;		//y7	
		headShape = new Polygon(vertices);
		
		init(pos, angle, neededSize);
	}
	
	public void init(Vector2 position, float angle, int neededSize)
	{
		this.deprees = 0;
		this.angle = angle;
		this.neededSize = neededSize;
		this.shiftFirst = 0;
		this.shiftLast = SPAN_DISTANCE - shiftFirst;
		
		skeleton.clear();
		
		Vector2 normal = new Vector2(1, 0);
		normal.setAngle(angle + 90).nor();
		
		Bone firstBone = new Bone();
		firstBone.position.set(position);
		firstBone.normal.set(normal);
		skeleton.addLast(firstBone);
		
		Vector2 v = new Vector2(SPAN_DISTANCE, 0).setAngle(angle);
		for (int i = 0; i < neededSize; i++)
		{
			Bone staticBone = new Bone();
			staticBone.position.set(position);
			staticBone.normal.set(normal);
			skeleton.addLast(staticBone);
			
			position.sub(v);
		}
		
		Bone lastBone = new Bone();
		lastBone.position.set(position);
		lastBone.normal.set(normal);
		skeleton.addLast(lastBone);
		
		updateVertices(); 
		updateHeadShape();
	}
	
	/**
	 * Удаление костей (используется в step)
	 */
	private void curtail()
	{
		Bone bone = skeleton.pollLast();
		Vector2 position = bone.position;
		
		//Удаление N точек
		int count = skeleton.size() - neededSize - 1;
		for (int i = 0; i < count; i++)
			position = skeleton.pollLast().position;
		
		//вычисление позиции последней кости
		Vector2 px = skeleton.getLast().position.cpy();
		Vector2 v = px.cpy().sub(position);
		v.nor().scl(shiftLast);
		
		px.sub(v);
		bone.position.set(px);
		
		v.nor();
		bone.normal.set(-v.y, v.x);
		
		skeleton.addLast(bone);
	}
	
	/**
	 * Создание новых костей (используется в step)
	 * @param count количество добавляемых статических костей
	 */
	private void move(int count)
	{
		Vector2 normal = new Vector2();
		Bone shiftBone = skeleton.pollFirst();
		
		for (int i = 0; i < count; i++)
		{	
			normal.set(1, 0).rotate(angle+90); 
			
			//первая фиксированная кость
			Bone cbone = skeleton.getFirst();
			cbone.normal.add(normal).nor();

			//новая фиксированная кость
			Bone nbone = new Bone();
			nbone.position.set(SPAN_DISTANCE, 0).rotate(angle);
			nbone.position.add(cbone.position);
			nbone.normal.set(normal);
			skeleton.addFirst(nbone);
			
			rotate();	//пересчитать angle
		}
		
		normal.set(1, 0).rotate(angle+90);
		
		//первая фиксированная кость 
		Bone firstBone = skeleton.getFirst();
		if (count > 0) firstBone.normal.add(normal).nor();
		
		//движимая кость (перемещаемая)
		shiftBone.position.set(shiftFirst, 0).rotate(angle);
		shiftBone.position.add(firstBone.position);
		shiftBone.normal.set(normal);
		skeleton.addFirst(shiftBone);
	}
	
	/**
	 * Вычисляет новое значение угла (используется в step)
	 */
	private void rotate()
	{
		if (deprees != 0)
		{
			float signedDelta = (deprees > 0 ? 1 : -1) * DELTA_DEGREES;
			if (Math.abs(deprees) < DELTA_DEGREES)
			{
				angle += deprees;
				deprees = 0;
			}
			else
			{
				angle += signedDelta;
				deprees -= signedDelta;	
			}
		}
	}
	
	/**
	 * Двигает и поворачивает змейку исходя из скорости и необходимого угла
	 * (вычисляет новые кости скелета, убирая лишние) 
	 */
	public void step()
	{
		//Движение/Поворот
		float currentLen = getLength();
		float neededLen = neededSize * SPAN_DISTANCE;
		
		float v = shiftFirst + (velocityFactor * velocity);
		int count = (int)Math.floor(v / SPAN_DISTANCE);
		
		shiftFirst = v - (SPAN_DISTANCE * count);
		if (neededLen <= currentLen)
			shiftLast = SPAN_DISTANCE - shiftFirst;
		
		move(count);
		curtail();
		
		//Обновление вершин
		updateVertices();
		
		//Обновление координат, угла и размера головы 
		updateHeadShape();
	}
	
	/**
	 * Вычисление ширины змейки
	 * @param distance расстояние от хвоста, на котором нужно узнать ширину
	 */
	private float getWidth(float distance)
	{
		float length = getLength();
		float lenLift	= 200;
		float lenLower	= 120;
		
		float q = length / (lenLift + lenLower);
		if (q < 1)
		{
			lenLift *= q;
			lenLower *= q;
		}
		float x = MAX_WIDTH;
		if (distance < lenLift)
		{
			float precent = distance / lenLift;
			x = Interpolation.pow2Out.apply(0, MAX_WIDTH, precent);
		}
		float rdistance = length - distance;
		if (rdistance < lenLower)
		{ 
			float precent = rdistance / lenLower;
			x = Interpolation.pow2In.apply(NECK_WIDTH, MAX_WIDTH, precent);
		}
		
		return x;
	}
	
	/**
	 * Вычисление вершин хвоста.
	 * Формат хранения вершин стандартный для libgdx при рисовании на SpriteBatch.
	 * (x, y, u, v, color) группируемые по 4 вершины
	 */
	private void updateVertices()
	{
		final float color = Color.WHITE.toFloatBits();
		Iterator<Bone> i = skeleton.descendingIterator();
		Bone bone1 = i.next();
		
		//v ширина змеи
		float v1 = texTail.getV();
		float v2 = texTail.getV2();
		
		//u программный повтор
		float u1 = texTail.getU();
		float u2 = texTail.getU2();
		
		//расстояние от хвоста 
		float distance = 0;
		
		//вычисление ширины от расстояния
		float width1 = getWidth(distance);
		
		float repeatU1 = 0;

		vertices.clear();
		
		while (i.hasNext())
		{
			Bone bone2 = i.next();
			
			float span = bone2.position.dst(bone1.position);
			
			//вычисление текстурных координат
			float repeatU2 = repeatU1 + (span / REPEAT_SIZE) / 2;
			
			distance += span;
			float width2 = getWidth(distance);
			
			vertices.add(bone1.position.x - bone1.normal.x * width1);
			vertices.add(bone1.position.y - bone1.normal.y * width1);
			vertices.add(color);
			vertices.add(u1 + (u2-u1) * repeatU1);
			vertices.add(v1);
			
			vertices.add(bone1.position.x + bone1.normal.x * width1);
			vertices.add(bone1.position.y + bone1.normal.y * width1);
			vertices.add(color);
			vertices.add(u1 + (u2-u1) * repeatU1);
			vertices.add(v2);
			
			vertices.add(bone2.position.x + bone2.normal.x * width2);
			vertices.add(bone2.position.y + bone2.normal.y * width2);
			vertices.add(color);
			vertices.add(u1 + (u2-u1) * repeatU2);
			vertices.add(v2);
			
			vertices.add(bone2.position.x - bone2.normal.x * width2);
			vertices.add(bone2.position.y - bone2.normal.y * width2);
			vertices.add(color);
			vertices.add(u1 + (u2-u1) * repeatU2);
			vertices.add(v1);
			
			bone1 = bone2;
			width1 = width2;
			repeatU1 = repeatU2 >= 0.5 ? repeatU2 - 0.5f : repeatU2;
		}
	}
	
	/**
	 * Обновление координат, угла и размера головы
	 */
	private void updateHeadShape()
	{
		Bone first = skeleton.getFirst();
		Vector2 p = first.position;
		Vector2 n = first.normal;

		headShape.setPosition(p.x, p.y);
		headShape.setScale(MAX_WIDTH, MAX_WIDTH);
		headShape.setRotation(n.angle() - 90);
	}
	
	/** 
	 * Внутренние перекрытия
	 * @return возвращает true если голова змеи перекрывает хвост
	 */	
	public boolean internalOverlap()
	{
		float[] poly1 = headShape.getTransformedVertices();
		float[] poly2 = new float[8];
		
		//это косяк -> (начиная со второго прямоугольника, исключая два последних)
		for (int i = 1*20; i < vertices.size - 2*20; i += 20)
		{
			poly2[0] = vertices.items[i+Batch.X1];
			poly2[1] = vertices.items[i+Batch.Y1];
			poly2[2] = vertices.items[i+Batch.X2];
			poly2[3] = vertices.items[i+Batch.Y2];
			poly2[4] = vertices.items[i+Batch.X3];
			poly2[5] = vertices.items[i+Batch.Y3];
			poly2[6] = vertices.items[i+Batch.X4];
			poly2[7] = vertices.items[i+Batch.Y4];
			
			boolean result = Intersector.overlapConvexPolygons(poly1, poly2, null);
			if (result == true) return true;
		}
		
		return false;
	}
	
	public boolean overlap(float[] convexPolygon)
	{
		float[] poly = new float[8];
		
		//Проверка на перекрытие с хвостом
		//это косяк -> (начиная со второго прямоугольника, исключая два последних)
		for (int i = 1*20; i < vertices.size - 1*20; i += 20)
		{
			poly[0] = vertices.items[i+Batch.X1];
			poly[1] = vertices.items[i+Batch.Y1];
			poly[2] = vertices.items[i+Batch.X2];
			poly[3] = vertices.items[i+Batch.Y2];
			poly[4] = vertices.items[i+Batch.X3];
			poly[5] = vertices.items[i+Batch.Y3];
			poly[6] = vertices.items[i+Batch.X4];
			poly[7] = vertices.items[i+Batch.Y4];
			
			boolean result = Intersector.overlapConvexPolygons(convexPolygon, poly, null);
			if (result == true) return true;
		}

		//Проверка на перекрытие с головой змейки
		float[] head = headShape.getTransformedVertices();
		boolean result = Intersector.overlapConvexPolygons(convexPolygon, head, null);
		if (result == true) return true;
		
		return false;
	}
	
	@Override
	public Rectangle getDrawingRectagle()
	{
		return null;
	}
	
	/**
	 * Рисование змейки на Batch
	 * @param batch холст
	 */
	@Override
	public void draw(Batch batch)
	{
		//Рисование хвоста змеи
		batch.draw(texTail.getTexture(), vertices.items, 0, vertices.size);
		
		//Рисование головы змеи
		float w = ((float)texHead.getRegionWidth() / texHead.getRegionHeight()) * MAX_WIDTH*2;
		float h = MAX_WIDTH*2;
		
		Bone first = skeleton.getFirst();
		Vector2 p = first.position;
		Vector2 n = first.normal;
		
		batch.setColor(Color.WHITE);
		batch.draw(texHead, p.x, p.y - h/2, 0, h/2, w, h, 1, 1, n.angle() - 90);
	}
	
	/**
	 * Отладочный вывод скелета и других деталей змейки
	 */
	@Override
	public void debugDraw(ShapeRenderer renderer)
	{
		renderer.begin(ShapeType.Line);
		renderer.setColor(0.0f, 0.0f, 1.0f, 1);
		for (int i = 0; i < vertices.size; i += 20)
		{
			float x1 = vertices.items[i+Batch.X1];
			float y1 = vertices.items[i+Batch.Y1];
			float x2 = vertices.items[i+Batch.X2];
			float y2 = vertices.items[i+Batch.Y2];
			float x3 = vertices.items[i+Batch.X3];
			float y3 = vertices.items[i+Batch.Y3];
			float x4 = vertices.items[i+Batch.X4];
			float y4 = vertices.items[i+Batch.Y4];
			
			renderer.line(x1, y1, x2, y2);
			renderer.line(x3, y3, x4, y4);
			renderer.line(x1, y1, x4, y4);
			renderer.line(x2, y2, x3, y3);
		}
		
		renderer.setColor(1.0f, 0.0f, 0.0f, 1);
		renderer.polygon(headShape.getTransformedVertices());
		renderer.end();
		
		renderer.begin(ShapeType.Filled);
		renderer.setColor(0.0f, 0.0f, 0.0f, 1);
		for(Bone bone : skeleton)
			renderer.circle(bone.position.x, bone.position.y, 2);
		renderer.setColor(1.0f, 0.0f, 0.0f, 1);
		Vector2 position = getPosition();
		renderer.circle(position.x, position.y, 3);
		renderer.end();
	}
	
	//============================================================================================
	
	/** 
	 * Повернуть
	 * @param deprees смешение относительно текущего угла движения
	 * (положительный deprees ==> против часовой стрелки)
	 */
	public void rotate(float deprees)
	{
		this.deprees = deprees;
	}
	
	/** 
	 * Узнать длину
	 * @return текущая длина змейки
	 */
	public float getLength()
	{
		return ((skeleton.size()-3) * SPAN_DISTANCE) + shiftLast + shiftFirst;
	}
	
	/** 
	 * Узнать необходимый размер
	 * @return количество костей в змейке
	 */
	public int getNeededSize()
	{
		return neededSize;
	}
	
	/** 
	 * Задать необходимый размер
	 * @param size - количество костей в змейке
	 */
	public void setNeededSize(int size)
	{
		if (size > 0)
			neededSize = size;
	}
	
	public void grow(int len)
	{
		setNeededSize(neededSize + len);
	}
	
	/** 
	 * Узнать скорость
	 * @return скорость движения
	 */
	public float getVelocity()
	{
		return velocity;
	}
	
	/**
	 * Задать скорость
	 * @param velocity - скорость движения
	 * @param range - учитывать ли ограничения
	 */
	public void setVelocity(float velocity)
	{
		this.velocity = velocity;
	}
	
	public float getFactorVelocity()
	{
		return velocityFactor;
	}
	
	public void setFactorVelocity(float factor)
	{
		velocityFactor = factor;
	}
	
	/** 
	 * Узнать угол движения змейки
	 * @return угол движения
	 */
	public float getAngle()
	{
		return angle;
	}
	
	/** 
	 * Узнать позицию головы змейки
	 * @return позицию головы
	 */
	public Vector2 getPosition()
	{
		return skeleton.getFirst().position.cpy();
	}
	
	/**
	 * Задать позицию змейки
	 */
	public void setPosition(float x, float y)
	{
		Vector2 pos = getPosition().sub(x, y);
		
		for (Bone bone : skeleton)
		{
			bone.position.sub(pos);
		}
		
		updateVertices();
		updateHeadShape();
	}
	
	/** 
	 * Возвращает форму головы
	 * @return форму головы в виде выпуклого полигона
	 */
	public Polygon getHeadShape()
	{
		return headShape;
	}
}
