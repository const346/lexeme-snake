package com.lexemesnake;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.lexemesnake.course.Course;
import com.lexemesnake.course.Task;
import com.lexemesnake.course.Topic;
import com.lexemesnake.course.Task.TaskState;
import com.lexemesnake.ui.BaseUI;
import com.lexemesnake.ui.MenuUI.SelectTopicPage.SelectedListener;
import com.lexemesnake.utils.Utils;
import com.lexemesnake.world.Item;
import com.lexemesnake.world.Snake;
import com.lexemesnake.world.World;
import com.lexemesnake.world.Item.ItemStyle;
import com.lexemesnake.world.World.IListenerHitBarrier;
import com.lexemesnake.world.World.IListenerTookItem;
import com.lexemesnake.world.draw.Camera2D;
import com.lexemesnake.world.draw.WorldRenderer;

/**
 * @author Кравченко Алексей (medhaid)
 *
 */
public class Base implements ApplicationListener 
{
	private SpriteBatch spriteBatch;
	
	private AssetManager asset;
	
	private Stage stageUI;
	private BaseUI ui;
	
	private Course course;
	private World world;
	private WorldRenderer worldRenderer;
	
	private int scores;
	
	private ItemStyle itemStyle;
	private ItemStyle wrongItemStyle;
	
	private boolean enableStep = true;
	private boolean visibleUI = true;
	private boolean visibleWorld = true;
	private boolean visibleDebug = false;
	
	private void loadResourse()
	{
		asset.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
		
		asset.load("data/maps/1.tmx", TiledMap.class);
		asset.load("data/base.atlas", TextureAtlas.class);
		asset.load("data/base.skin", Skin.class);
		
		/*asset.load("data/sound/chew1.mp3", Sound.class);
		asset.load("data/sound/chew2.mp3", Sound.class);
		asset.load("data/sound/chew3.mp3", Sound.class);
		asset.load("data/sound/UI_SFX_Set(MP3)/rollover1.mp3", Sound.class);*/
		
		asset.finishLoading();
	}
	
	@Override
	public void create() 
	{
		asset = new AssetManager();
		loadResourse();
		
		//Инициализация базы заданий
		XmlReader xml = new XmlReader();
		Element xmlRoot = xml.parse(Gdx.files.internal("data/course.xml").readString());
		course = new Course(xmlRoot);
		
		//Содержит в себе стиль интерфейса
		Skin skin = asset.get("data/base.skin");
		
		//Инициализация холста
		spriteBatch = new SpriteBatch(5200);
		
		//Инициализация интерфейса
		stageUI = new Stage(new ScreenViewport(), spriteBatch);
		ui = new BaseUI(skin);
		stageUI.addActor(ui);	
		
		ui.level.newRecordDialog.setVisible(true);
		
		//Инициализация страниц меню (только после загрузки course)
		initMenuPages();		
		
		//Инициализация змейки
		TextureAtlas atlas = asset.get("data/base.atlas");
		TextureRegion texHead = atlas.findRegion("head");
		TextureRegion texTail = atlas.findRegion("tail");
		Snake snake = new Snake(new Vector2(350, 350), 0, 50, 3.5f, texHead, texTail);
		
		//Инициализация мира
		TiledMap map = asset.get("data/maps/1.tmx");
		IntArray idTiledBarriers = new IntArray();
		TiledMapTileSet tileSet = map.getTileSets().getTileSet("BaseTileSet");
		for (TiledMapTile tile : tileSet)
		{
			String key = "collision";
			MapProperties properties = tile.getProperties();
			if (properties.containsKey(key) && properties.get(key).equals("true"))
			{
				idTiledBarriers.add(tile.getId());
			}
		}
		world = new World(snake, map, "TiledLayer", idTiledBarriers.toArray());
		worldRenderer = new WorldRenderer(world, spriteBatch);	
		
		//Создание стилей для item
		ParticleEffect effect = new ParticleEffect();
		effect.load(Gdx.files.internal("data/particles/item(remove).part"), atlas);
		itemStyle = new ItemStyle();
		itemStyle.fontHit = skin.getFont("defaultFont");
		itemStyle.fontColor = Color.BLACK;
		itemStyle.backgrountHit = skin.getDrawable("panel(shadow)");
		itemStyle.textureBase = atlas.findRegion("apple");
		itemStyle.removeEffect = effect;
		
		ParticleEffect wrongEffect = new ParticleEffect();
		wrongEffect.load(Gdx.files.internal("data/particles/wrongItem(remove).part"), atlas);
		wrongItemStyle = new ItemStyle();
		wrongItemStyle.fontHit = skin.getFont("defaultFont");
		wrongItemStyle.fontColor = Color.RED;
		wrongItemStyle.backgrountHit = skin.getDrawable("panel(shadow)");
		wrongItemStyle.textureBase = atlas.findRegion("apple");
		wrongItemStyle.removeEffect = wrongEffect;
		
		
		//Далее назначение обработчиков на UI
		
		//Обработчик на тачпат
		SnakeController snakeController = new SnakeController(snake);
		ui.level.touchpad.addListener(snakeController);
		
		//Обработка события кнопки возврата из игры в меню
		ClickListener backMenuListener = new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	world.pause();
		    	
		    	ui.menu.showPage(ui.menu.mainPage);
		    	ui.showPage(ui.menu);
		    }
		};
		ui.level.backMenuButton.addListener(backMenuListener);
		ui.level.winDialog.backMenuButton.addListener(backMenuListener);
		ui.level.loseDialog.backMenuButton.addListener(backMenuListener);
		
		ui.level.winDialog.continueButton.addListener(new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	ui.menu.showPage(ui.menu.selectTopicPage);
		    	ui.showPage(ui.menu);
		    }
		});
		
		ui.menu.mainPage.сontinueButton.addListener(new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	continueGame();
		    }
		});
		
		ui.level.winDialog.replayTopicButton.addListener(new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	course.getTopic().resetProgress();
		    	newGame(course.getTopic().getName());
		    }
		});
		
		ui.level.loseDialog.replayTaskButton.addListener(new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	if (scores > 0)
		    	{
		    		scores /= 2;
		    		ui.level.showMessage("OOPS!!! You lost half scores", Color.BLACK, 2);
		    	}
		    	
		    	course.getTopic().getCurrentTask().resetProgress();
		    	
		    	resetGame();
		    }
		});
		
		ui.level.newRecordDialog.continueButton.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				String topic = course.getTopic().getName();
				String name = ui.level.newRecordDialog.nameField.getText();
				setRecord(topic, scores, name);
				
				//Показываем диалог победы
				ui.level.newRecordDialog.setVisible(false);
				
				ui.level.winDialog.setContent(scores, topic);
				ui.level.winDialog.setVisible(true);
			}
		});
		
		InputMultiplexer input = new InputMultiplexer();
		input.addProcessor(new InputAdapter()
		{
			@Override
			public boolean keyDown(int keycode)
			{
				switch (keycode)
				{
					case Input.Keys.F1: enableStep = ! enableStep;
					break;
					case Input.Keys.F2: visibleUI = !visibleUI;
					break;
					case Input.Keys.F3: visibleWorld = !visibleWorld;
					break;
					case Input.Keys.F4: visibleDebug = !visibleDebug;
					break;
					case Input.Keys.F5:
					{
						String[] tokens  = course.getTopic().getCurrentTask().getNextVariants();
						if (tokens.length > 0)
						{
							takeItem(Utils.randomSelect(tokens));
						}
					}
					break;
					case Input.Keys.F6:
					{
						//world.getCharacter().init(new Vector2(200, 200), new Vector2(200, 280));
						world.getCharacter().init(new Vector2(350, 350), 45, 5);
					}
				}
				
				return false;
			}
		});
		
		ui.menu.selectTopicPage.setSelectedListener(new SelectedListener()
		{
			@Override
			public void select(String topic)
			{
				newGame(topic);
			}
		});
		
		input.addProcessor(stageUI);
		Gdx.input.setInputProcessor(input);
		
		//Далее идут события игры
		
		//Змейка встретила препятствие
		world.setListenerHitBarrier(new IListenerHitBarrier()
		{
			@Override
			public void event(World world, Snake character)
			{
				finishGame(false);
			}
		});
		
		//Змейка подобрала item
		world.setListenerTookItem(new IListenerTookItem()
		{
			@Override
			public void event(World world, Snake snake, Item item)
			{
				//тестирование звука
				//Utils.randomSelect(chew1S, chew2S, chew3S).play();
				
				takeItem(item.getText());
			}
		});
	}
	
	private void takeItem(String token)
	{
		Snake snake = world.getCharacter();
		
		final Topic topic = course.getTopic();
		Task task = topic.getCurrentTask();
		TaskState state = task.testNext(token); 
		
		switch(state)
		{
			case Processing:
			{	
				ui.level.answerLabel.setText(task.getDesignCode());
				ui.level.showMessage("NICE WORK! PROCEED!", Color.BLACK, 2);
				
				//Обновляем items
				world.clearItems();
				generateItems();
				
				//Удлинить змейку
				snake.grow(4);
			}
			break;
			case Success:
			{
				//Удлинить змейку
				snake.grow(4);
				
				//Добавить очки
				scores += task.getScores();
				
				//Обновить UI
				updateLevelUI();
				
				//Удалить все items
				world.clearItems();
				
				ui.level.showMessage("TASK IS COMPLETED!!!", Color.BLACK, 2, new Runnable()
				{
					@Override
					public void run()
					{
						//switch1S.play();
								
						if (topic.hasTask())
						{
							topic.nextTask();
						}
						else
						{
							finishGame(true);
							return;
						}
						
						generateItems();
						updateLevelUI();
					}
				});
			}
			break;
			case Fail:
			{
				finishGame(false);
			}
		}
	}
	
	//Наполнение содержимым страниц меню
	private void initMenuPages()
	{
		//selectTopicPage
		ui.menu.selectTopicPage.setContent(course);
		
		//recordsPage
		ui.menu.recordsPage.setContent(course);
		
		//aboutPage
		String aboutText = Gdx.files.internal("data/about.txt").readString();
		ui.menu.aboutPage.setContent(aboutText);
		
		//helpPage
		String helpText = Gdx.files.internal("data/help.txt").readString();
		ui.menu.helpPage.setContent(helpText);
	}
	
	private void finishGame(boolean win)
	{
		world.pause();
		ui.menu.mainPage.hideContinueButton();
		
		if (win)
		{
			String topic = course.getTopic().getName();
			if (newRecord(topic, scores))
			{
				//Показываем диалог ввода рекорда
				ui.level.newRecordDialog.setVisible(true);
			}
			else
			{
				//Показываем диалог победы
				ui.level.winDialog.setContent(scores, topic);
				ui.level.winDialog.setVisible(true);
			}
		}
		else
		{
			//Показываем диалог поражения
			ui.level.loseDialog.setVisible(true);
		}
	}
	
	private void continueGame()
	{
		ui.showPage(ui.level);
		world.resume();
	}
	
	private void newGame(String topic)
	{
		scores = 0;
		course.setTopic(topic);
		resetGame();
	}
	
	private void resetGame()
	{
		ui.level.hideAllDialogs();
    	ui.level.removeMessage();
		
		ui.menu.mainPage.showContinueButton();
		ui.showPage(ui.level);
		
		world.reset();
		updateLevelUI();
		generateItems();
		world.resume();
	}
	
	private void updateLevelUI()
	{
		Topic topic = course.getTopic();
		Task task = topic.getCurrentTask();
		
		ui.level.topicLabel.setText("Topic: " + topic.getName());
		ui.level.taskIndexLabel.setText("Task: " + topic.getCurrentTaskIndex() + "/" + topic.getCountTask());
		ui.level.scoresLabel.setText("Scores: " + scores);
		
		ui.level.taskLabel.setText(task.getDescription());
		ui.level.answerLabel.setText(task.getDesignCode());
	}
	
	private void generateItems()
	{
		final int countWrongItems = MathUtils.random(6, 12);
		Task task = course.getTopic().getCurrentTask();
		
		//добавление правильных ответов
		String[] rightTokens = task.getNextVariants();
		world.addItems(rightTokens, itemStyle);
		
		//добавление не правильных ответов
		String[] wrongTokens = course.getRandomTokens(countWrongItems, rightTokens);
		world.addItems(wrongTokens, wrongItemStyle);
	}

	private boolean newRecord(String topic, float scores)
	{
		Preferences pref = Gdx.app.getPreferences("Records");
		if (pref.contains(topic))
		{
			String[] params = pref.getString(topic).split("\\|");
			
			float prefScores = Float.valueOf(params[1]);
			if (prefScores < scores)
				return true; //(рекорд побит)
		}
		else
			return true; //(рекорда не было)
		
		return false;
	}
	
	private void setRecord(String topic, float scores, String name)
	{
		//Записать рекорд
		Preferences pref = Gdx.app.getPreferences("Records");
		pref.putString(topic, name + "|" + scores);
		pref.flush();
		
		//Обновить таблицу рекордов
		ui.menu.recordsPage.setContent(course);
	}
	
	@Override
	public void render() 
	{
		if (enableStep) 
			step(Gdx.graphics.getDeltaTime());
		
		draw();
	}
	
	private void draw()
	{
        if (ui.getWidget() == ui.level)
        {
        	//Рисование мира игры
        	if (visibleWorld)
        	{
        		worldRenderer.draw();
        	}
        	else
        	{
        		Gdx.gl.glClearColor(1, 1, 1, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        	}
        }
        else
        {
        	//Рисование фона меню
        	Gdx.gl.glClearColor(0.8f, 1f, 0.8f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
		
        if (visibleDebug)
        {
        	//Отладочное рисование мира игры
        	worldRenderer.debugDraw();
        }
     
        
		if (visibleUI)
		{
			//Рисование интерфейса
			stageUI.act();
			stageUI.draw();
			
			Table.drawDebug(stageUI);
		}
		
       
		//Gdx.graphics.setTitle("renderCalls: " + spriteBatch.renderCalls);
		
		//Вывод fps
		int fps = Gdx.graphics.getFramesPerSecond();
		Gdx.graphics.setTitle("LexemeSnake -> fps: " + fps);
	}
	
	private void step()
	{
		//Слежение камеры за игроком
		Camera2D camera = worldRenderer.getCamera();
		camera.moveToPoint(world.getCharacter().getPosition());
		camera.step();
		
		//Игровой шаг
		world.step();
	}
	
	private float accumulator = 0.0f;
	private float deltaStep = 1.0f/60.0f;
	private void step(float delta)
	{
		final float MAX_DELTA = deltaStep * 3f;
		
		float frameTime = Math.min(delta, MAX_DELTA);
		
		accumulator += frameTime;
	    while (accumulator >= deltaStep )
	    {
	    	step();
	    	accumulator -= deltaStep;
	    }
	}

	@Override
	public void resize(int width, int height) 
	{ 
		stageUI.getViewport().update(width, height, true);
		worldRenderer.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose() 
	{
		asset.dispose();
		spriteBatch.dispose();
	}
	
	@Override
	public void pause() 
	{
		
	}
	
	@Override
	public void resume() 
	{ 
		
	}
}
