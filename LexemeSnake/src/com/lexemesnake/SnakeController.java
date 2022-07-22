package com.lexemesnake;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.lexemesnake.utils.Utils;
import com.lexemesnake.world.Snake;

public class SnakeController extends ChangeListener
{
	private Snake snake;
	
	public SnakeController(Snake snake)
	{
		this.snake = snake;
	}
	
	@Override
	public void changed(ChangeEvent event, Actor actor)
	{
		Touchpad touchpad = (Touchpad) actor;
		Vector2 vector = new Vector2(touchpad.getKnobPercentX(), touchpad.getKnobPercentY());
		if (vector.isZero() == false)
		{
			snake.setFactorVelocity(0.2f + vector.len());
			snake.rotate(Utils.getDegreesFromAngle(snake.getAngle(), vector.angle()));
			event.handle();
		}
	}
}
