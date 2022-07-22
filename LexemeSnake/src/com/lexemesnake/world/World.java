package com.lexemesnake.world;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.SnapshotArray;
import com.lexemesnake.utils.TileLayerOverlap;
import com.lexemesnake.utils.Utils;
import com.lexemesnake.utils.TileLayerOverlap.CellFilter;
import com.lexemesnake.world.Item.ItemStyle;

/**
 * Проверка столкновений с персонажем порождает события:
 * 1) Персонаж врезался в барьер (HitBarrier)
 * 2) Персонаж взял item		 (TookItem)
 * 
 * @author Кравченко Алексей (medhaid)
 */

public class World
{
	public interface IListenerHitBarrier
	{
		void event(World world, Snake character);
	}
	
	public interface IListenerTookItem
	{
		void event(World world, Snake character, Item item);
	}
	
	private IListenerHitBarrier listenerHitBarrier;
	private IListenerTookItem listenerTookItem;
	
	private TiledMap tiledMap;
	private TiledMapTileLayer layer;
	private CellFilter cellFilter;
	
	private SnapshotArray<ParticleEffect> effects;
	
	private Snake snake;
	
	private SnapshotArray<Item> items;
	
	private boolean running;
	
	public World(Snake snake, TiledMap map, String layerName, int[] idTiledCollision)
	{
		effects = new SnapshotArray<ParticleEffect>(ParticleEffect.class);
		items = new SnapshotArray<Item>(Item.class);
		
		setCharacter(snake);
		setCollisionTileLayer(map, layerName, idTiledCollision);
		
		running = false;
	}
	
	public void step()
	{
		if (running)
		{
			snake.step();
			detectCollision();
		}
		
		//Удаляем все выполненные эффекты
		ParticleEffect[] effSnap = effects.begin();
		for (int i = 0, n = effects.size; i < n; i++)
		{
			if (effSnap[i].isComplete())
				effects.removeValue(effSnap[i], true);
		}
		effects.end();
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void resume()
	{
		snake.setFactorVelocity(0);
		running = true;
	}
	
	public void pause()
	{
		running = false;
	}
	
	/*
	 * Проверка столкновений
	 * (персонажа с клетками и item'ми)
	 */
	private void detectCollision()
	{
		Polygon head = snake.getHeadShape();
		
		//Snake <--> TileLayer
		if (TileLayerOverlap.overlapCell(layer, head, cellFilter))
		{
			//Вызов события
			if (listenerHitBarrier != null) 
				listenerHitBarrier.event(this, snake);
		}
		
		//Snake <internal>
		if (snake.internalOverlap())
		{
			//Вызов события
			if (listenerHitBarrier != null) 
				listenerHitBarrier.event(this, snake);
		}
		
		//Snake <--> Item
		Item[] snapshotItems = items.begin();
		for (int i = 0, n = items.size; i < n; i++)
		{
			Item item = snapshotItems[i];
			if (item.overlap(head))
			{
				//Вызов события
				if (listenerTookItem != null)
				{
					listenerTookItem.event(this, snake, item);	
				}
				
				//Удаление item
				items.removeValue(item, true);
				
				//Добавляем эффект исчезновения
				ParticleEffect effect = new ParticleEffect(item.getStyle().removeEffect);
				effect.setPosition(item.getX(), item.getY());
				effect.start();
				
				effects.add(effect);
			}
		}
		items.end();
	}
	
	public void setCharacter(Snake snake)
	{
		this.snake = snake;
	}
	
	public Snake getCharacter()
	{
		return snake;
	}
	
	public void setCollisionTileLayer(TiledMap map, String layerName, final int[] idTiledBarriers)
	{
		this.layer = (TiledMapTileLayer) map.getLayers().get(layerName);
		this.tiledMap = map;
		
		Arrays.sort(idTiledBarriers);

		cellFilter = new CellFilter()
		{
			@Override
			public boolean filter(Cell cell)
			{
				int index = Arrays.binarySearch(idTiledBarriers, cell.getTile().getId());	
				return index >= 0;
			}
		};
	}
	
	public void reset()
	{
		items.clear();
		snake.init(new Vector2(350, 250), 15, 10);
		snake.setFactorVelocity(0);
	}
	
	/**
	 * Проверка возможности разместить item
	 * @param item позиционируемый объект
	 * @return true если позиция корректна
	 */
	private boolean checkPosition(Item item)
	{
		float tileW = layer.getTileWidth();
		float tileH = layer.getTileHeight();
		
		Rectangle base = item.getBaseRectagle();
		float itemW = base.width;
		float itemH = base.height;
		
		float x = item.getX();
		float y = item.getY();
		
		//1. проверка свободного места (на сетке)
		final int BORDER = 1;
		
		int tileX1 = (int)((x - itemW / 2) / tileW);
		int tileY1 = (int)(y / tileH);
		int tileX2 = (int)((x + itemW / 2) / tileW);
		int tileY2 = (int)((y + itemH) / tileH);
		
		tileX1 -= BORDER;
		tileY1 -= BORDER;
		tileX2 += BORDER;
		tileY2 += BORDER;
		
		for (int iy = tileY1; iy <= tileY2; iy++)
		{
			for (int ix = tileX1; ix <= tileX2; ix++)
			{
				Cell cell = layer.getCell(ix, iy);
				if (cell == null || cellFilter.filter(cell))
				{
					return false;
				}
			}
		}
	
		//2. Проверка c Item
		Rectangle hit = item.getHitRectagle();
		for (Item testItem : items)
		{
			Rectangle base2 = testItem.getBaseRectagle();
			Rectangle hit2 = testItem.getHitRectagle();
			
			boolean res1 = base.overlaps(base2);
			boolean res2 = base.overlaps(hit2);
			boolean res3 = hit.overlaps(base2);
			boolean res4 = hit.overlaps(hit2);
			
			if (res1 || res2 || res3 || res4)
			{
				return false;
			}
		}
		
		//3. Проверка с snake
		float[] rect = Utils.getRectangleVertex(base);
		if (snake.overlap(rect)) return false;
		
		//4. Чтобы не появлялись рядом с головой змеи
		float radius = 300f;
		Vector2 pos1 = snake.getPosition();
		Vector2 pos2 = item.getPosition();
		if (pos1.dst(pos2) < radius) return false;
		
		return true;
	}
	
	/**
	 * Поиск позиции
	 * @param item позиционируемый объект
	 * @param maxCount максимальное количество проверок
	 * @return true если для объекта была найдена позиция 
	 */
	private boolean primarySearch(Item item, int maxCount)
	{
		//Границы проверки положения
		int layerW = layer.getWidth();
		int layerH = layer.getHeight();
		float tileW = layer.getTileWidth();
		float tileH = layer.getTileHeight();
		Rectangle rect = item.getDrawingRectagle();
		
		float x1 = rect.width / 2f;
		float y1 = 0;
		float x2 = (layerW * tileW) - (rect.width / 2f);
		float y2 = (layerH * tileH) - rect.height;
		
		//Поиск позиции
		for (int i = 0; i < maxCount; i++)
		{
			//Задаём случайную позицию
			float x = MathUtils.random(x1, x2);
			float y = MathUtils.random(y1, y2);
			item.setPosition(x, y);
			
			//Проверка позиции
			if (checkPosition(item))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Поиск позиции полным перебором
	 * @param item позиционируемый объект
	 * @return true если для объекта была найдена позиция 
	 */
	private boolean secondarySearch(Item item)
	{
		//Задаём границы обхода
		int layerW = layer.getWidth();
		int layerH = layer.getHeight();
		float tileW = layer.getTileWidth();
		float tileH = layer.getTileHeight();
		Rectangle rect = item.getDrawingRectagle();
		
		float x1 = rect.width / 2f;
		float y1 = 0;
		float x2 = (layerW * tileW) - (rect.width / 2f);
		float y2 = (layerH * tileH) - rect.height;
		
		float delta = 32;
		
		for (float y = y1; y <= y2; y += delta)
		{
			for (float x = x1; x <= x2; x += delta)
			{
				//Задаём позицию
				item.setPosition(x, y);
				
				//Проверка позиции
				if (checkPosition(item))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Поиск позиции
	 * @param item позиционируемый объект
	 */
	private void searchPosition(Item item)
	{
		if (primarySearch(item, 50) == false)
		{
			item.widget.setColor(Color.ORANGE); //(для отладки)
			if (secondarySearch(item) == false)
			{
				item.widget.setColor(Color.RED); //(для отладки)
			}
		}
	}
	
	public void clearItems()
	{
		items.clear();
	}
	
	public void addItems(String[] itemsText, ItemStyle style)
	{
		for (String text : itemsText)
		{
			Item item  = new Item(text, style);
			addItem(item, true);
		}
	}
	
	public void addItems(Item[] items, boolean searchPosition)
	{
		for (Item item : items)
			addItem(item, searchPosition);
	}
	
	public void addItem(Item item, boolean searchPosition)
	{
		if (searchPosition)
			searchPosition(item);
		
		items.add(item);
	}
	
	public void removeItem(Item item)
	{
		items.removeValue(item, true);
	}

	public IListenerHitBarrier getListenerHitBarrier()
	{
		return listenerHitBarrier;
	}

	public void setListenerHitBarrier(IListenerHitBarrier listenerHitBarrier)
	{
		this.listenerHitBarrier = listenerHitBarrier;
	}
	
	public IListenerTookItem getListenerTookItem()
	{
		return listenerTookItem;
	}

	public void setListenerTookItem(IListenerTookItem listenerTookItem)
	{
		this.listenerTookItem = listenerTookItem;
	}
	
	public TiledMap getTiledMap()
	{
		return tiledMap;
	}
	
	public TiledMapTileLayer getTileLayer()
	{
		return layer;
	}
	
	public CellFilter getCellFilter()
	{
		return cellFilter;
	}
	
	public SnapshotArray<Item> getItems()
	{
		return items;
	}
	
	public SnapshotArray<ParticleEffect> getEffects()
	{
		return effects;
	}
}
