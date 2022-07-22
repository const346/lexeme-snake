package com.lexemesnake.world.draw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lexemesnake.world.Item;
import com.lexemesnake.world.World;

public class WorldRenderer
{
	private World world;
	
	private Batch batch;
	private OrthogonalTiledMapRenderer mapRenderer;
	private ShapeRenderer debugRenderer;
	
	private Rectangle viewBounds;
	
	private Viewport viewport;
	private Camera2D camera;
	
	public WorldRenderer(World world, Batch batch)
	{
		this.world = world;
		this.batch = batch;
		
		this.viewBounds = new Rectangle();
		
		MapProperties params = world.getTiledMap().getProperties();
		int mapW = 	params.get("width", Integer.class);
		int mapH = 	params.get("height", Integer.class);
		int tileW = params.get("tilewidth", Integer.class);
		int tileH = params.get("tileheight", Integer.class);
		camera = new Camera2D(new Rectangle(0, 0, mapW*tileW, mapH*tileH));
		viewport = new ScreenViewport(camera);
		
		debugRenderer = new ShapeRenderer();
		
		mapRenderer = new OrthogonalTiledMapRenderer(world.getTiledMap(), batch);
	}
	
	public Viewport getViewport()
	{
		return viewport;
	}
	
	public Camera2D getCamera()
	{
		return camera;
	}
	
	public void draw()
	{
		camera.update();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		//Рисование плиточного фона
		mapRenderer.setView(camera);
		mapRenderer.renderTileLayer(world.getTileLayer());
		
		//Рисование персонажа
		world.getCharacter().draw(batch);
		
		//Рисование items ( с отсечением лишнего )
		viewBounds.x = camera.position.x - camera.viewportWidth / 2;
		viewBounds.y = camera.position.y - camera.viewportHeight / 2;
		viewBounds.width = camera.viewportWidth;
		viewBounds.height = camera.viewportHeight;
		
		SnapshotArray<Item> items = world.getItems();
		DrawableBatch[] objects = items.begin();
		for (int i = 0, n = items.size; i < n; i++)
		{
			Rectangle drawingRect = objects[i].getDrawingRectagle();
			if (drawingRect == null || viewBounds.overlaps(drawingRect))
			{
				objects[i].draw(batch);
			}
		}
		items.end();
		
		//Рисование эффектов
		for (ParticleEffect effect : world.getEffects())
		{
			effect.draw(batch, Gdx.graphics.getDeltaTime());
		}
		
		batch.end();
	}
	
	public void debugDraw()
	{
		camera.update();
		debugRenderer.setProjectionMatrix(camera.combined);

		//-------------------------------------------
		
		TiledMapTileLayer layer = world.getTileLayer(); 
		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();
		final float tileW = layer.getTileWidth();
		final float tileH = layer.getTileHeight();
		
		mapRenderer.setView(camera); //для того чтобы обновить ViewBounds плиточной карты
		Rectangle rect = mapRenderer.getViewBounds();
		final int x1 = Math.max(0, (int)(rect.x / tileW));
		final int x2 = Math.min(layerWidth, (int)((rect.x + rect.width + tileW) / tileW));
		final int y1 = Math.max(0, (int)(rect.y / tileH));
		final int y2 = Math.min(layerHeight, (int)((rect.y + rect.height + tileH) / tileH));
		
		debugRenderer.begin(ShapeType.Line);
		for (int iy = y1; iy < y2; iy++)
		{
			for (int ix = x1; ix < x2; ix++)
			{
				Cell cell = layer.getCell(ix, iy);
				if (world.getCellFilter().filter(cell))
				{
					debugRenderer.rect(ix*tileW, iy*tileH, tileW, tileH);
				}
			}
		}
		debugRenderer.end();
		
		//-------------------------------------------
		
		for(Item item : world.getItems())
			item.debugDraw(debugRenderer);
		
		//-------------------------------------------
		
		world.getCharacter().debugDraw(debugRenderer);
	}
}
