package com.lexemesnake.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.lexemesnake.utils.Utils;
import com.lexemesnake.world.draw.DrawableBatch;

public class Item implements DrawableBatch
{
	static public class ItemStyle
	{
		public TextureRegion	textureBase;
		public Drawable			backgrountHit;
		public BitmapFont		fontHit;
		public Color 			fontColor;
		public ParticleEffect	removeEffect;
	}
	
	private Rectangle hitRectagle;
	private Rectangle baseRectagle;
	private Rectangle drawingRectagle;
	
	private ItemStyle style;
	public Container widget;
	private Label label;
	
	public Item(String text, ItemStyle style)
	{
		float w = style.textureBase.getRegionWidth();
		float h = style.textureBase.getRegionHeight();
		Rectangle rectangle = new Rectangle(0, 0, w, h);
		initialization(text, style, rectangle);
	}
	
	public Item(String text, ItemStyle style, float x, float y)
	{
		float w = style.textureBase.getRegionWidth();
		float h = style.textureBase.getRegionHeight();
		Rectangle rectangle = new Rectangle(x, y, w, h);
		initialization(text, style, rectangle);
	}
	
	public Item(String text, ItemStyle style, Rectangle rectangle)
	{
		initialization(text, style, rectangle);
	}
	
	private void initialization(String text, ItemStyle style, Rectangle rectangle)
	{
		this.drawingRectagle = new Rectangle();
		this.hitRectagle = new Rectangle();
		
		this.baseRectagle = rectangle;
		this.style = style;
		
		LabelStyle labelStyle = new LabelStyle(style.fontHit, style.fontColor);
		label = new Label("", labelStyle);
		widget = new Container(label);
		widget.setBackground(style.backgrountHit);
		
		setText(text);
	}
	
	public ItemStyle getStyle()
	{
		return style;
	}
	
	public Rectangle getHitRectagle()
	{
		return hitRectagle;
	}
	
	public Rectangle getBaseRectagle()
	{
		return baseRectagle;
	}
	
	@Override
	public Rectangle getDrawingRectagle()
	{
		return drawingRectagle;
	}
	
	@Override
	public void draw(Batch batch)
	{
		float w = baseRectagle.width;
		float h = baseRectagle.height;
		float x = baseRectagle.x;
		float y = baseRectagle.y;
		
		batch.setColor(Color.WHITE);
		batch.draw(style.textureBase, x, y, 0, 0, w, h, 1, 1, 0);
		
		//Рисование подсказки
		widget.draw(batch, 1);
	}
	
	@Override
	public void debugDraw(ShapeRenderer renderer)
	{
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		renderer.begin(ShapeType.Line);
		renderer.setColor(Color.BLUE);
		renderer.rect(hitRectagle.x, hitRectagle.y, hitRectagle.width, hitRectagle.height);
		renderer.setColor(Color.RED);
		renderer.rect(baseRectagle.x, baseRectagle.y, baseRectagle.width, baseRectagle.height);
		renderer.setColor(0, 0, 0, 0.4f);
		renderer.rect(drawingRectagle.x, drawingRectagle.y, drawingRectagle.width, drawingRectagle.height);
		renderer.end();
		
		renderer.begin(ShapeType.Filled);
		renderer.setColor(Color.RED);
		renderer.circle(getX(), getY(), 3);
		renderer.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	public boolean overlap(Polygon convexPolygon)
	{
		float[] vertex = convexPolygon.getTransformedVertices();
		float[] rect = Utils.getRectangleVertex(baseRectagle);
		return Intersector.overlapConvexPolygons(vertex, rect, null);
	}
	
	public String getText()
	{
		return label.getText().toString();
	}
	
	private void calculate()
	{
		//Пересчитываем размер контейнера для текста
		widget.pack();
		
		//Вычисляем новую позицию контейнера
		float w = baseRectagle.width;
		float h = baseRectagle.height;
		float x = baseRectagle.x;
		float y = baseRectagle.y;
		x = Math.round(x + (w/2) - (widget.getWidth() / 2));
		y = Math.round(y + h);
		widget.setPosition(x, y);
		
		//Область показа подсказки над Item
		hitRectagle.x = widget.getX();
		hitRectagle.y = widget.getY();
		hitRectagle.width = widget.getWidth();
		hitRectagle.height = widget.getHeight();
		
		//Вычисляем новую область рисования
		drawingRectagle.set(hitRectagle);
		drawingRectagle.merge(baseRectagle);
	}
	
	public void setText(String text)
	{
		label.setText(text);
		
		calculate();
	}
	
	public void setPosition(float x, float y)
	{
		x = Math.round(x - baseRectagle.width / 2);
		y = Math.round(y);
		
		baseRectagle.setPosition(x, y);
		
		calculate();
	}
	
	public Vector2 getPosition()
	{
		Vector2 position = new Vector2();
		return position.set(getX(), getY());
	}
	
	public float getX()
	{
		return baseRectagle.x + baseRectagle.width / 2f;
	}
	
	public float getY()
	{
		return baseRectagle.y;
	}
}
