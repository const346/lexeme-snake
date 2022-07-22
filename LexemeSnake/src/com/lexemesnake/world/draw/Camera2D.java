package com.lexemesnake.world.draw;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Игровая камера расширяет возможности OrthographicCamera функциями
 * движения в определённую позицию, и ограничение движения камеры в
 * заданной области.
 * 
 * (без учёта zoom)
 * 
 * @author Кравченко Алексей (medhaid)
 */
public class Camera2D extends OrthographicCamera
{
	private float		SPEED_RATIO		= 0.045f;

	private Vector2		target;
	private Vector2		velocity;
	private Rectangle	boundingRect;

	public Camera2D(Rectangle boundingRect)
	{
		this.target 		= null;
		this.velocity 		= new Vector2();
		this.boundingRect 	= boundingRect;
	}

	public void moveToPoint(Vector2 target)
	{
		this.target = target;
	}
	
	public void step()
	{
		if (target != null)
		{
			velocity.set(target);
			velocity.sub(position.x, position.y).scl(SPEED_RATIO);
			move(velocity);
			
			update();
		}
	}
	
	public void move(Vector2 v)
	{
		float box_x1 = boundingRect.x;
		float box_y1 = boundingRect.y;
		float box_x2 = boundingRect.x + boundingRect.getWidth();
		float box_y2 = boundingRect.y + boundingRect.getHeight();

		float cam_x1 = v.x + (position.x - (viewportWidth / 2));
		float cam_y1 = v.y + (position.y - (viewportHeight / 2));
		float cam_x2 = v.x + (position.x + (viewportWidth / 2));
		float cam_y2 = v.y + (position.y + (viewportHeight / 2));

		Vector2 shift = new Vector2(0, 0);

		float left = box_x1 - cam_x1;
		float right = box_x2 - cam_x2;

		if (viewportWidth > boundingRect.width)
		{
			position.x = boundingRect.x + (boundingRect.width / 2.0f);
			v.x = 0;
		}
		else if (left > 0 && right > 0)
			shift.x = left;
		else if (left < 0 && right < 0)
			shift.x = right;

		float up = box_y1 - cam_y1;
		float down = box_y2 - cam_y2;

		if (viewportHeight > boundingRect.height)
		{
			position.y = boundingRect.y + (boundingRect.height / 2.0f);
			v.y = 0;
		}
		else if (up > 0 && down > 0)
			shift.y = up;
		else if (up < 0 && down < 0)
			shift.y = down;
		
		translate(v.x + shift.x, v.y + shift.y);
	}
}