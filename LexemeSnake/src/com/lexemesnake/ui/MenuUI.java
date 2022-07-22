package com.lexemesnake.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.tablelayout.Cell;
import com.lexemesnake.course.Course;
import com.lexemesnake.ui.MenuUI.RecordsPage.RecordRow;

/**
 * Инициализация и управление всеми страницами главного меню
 * @author Кравченко Алексей (medhaid)
 */

public class MenuUI extends Page
{
	static public class MainPage extends Page
	{
		public Button сontinueButton;
		public Button newGameButton;
		public Button recordsButton;
		public Button helpButton;
		public Button aboutButton;
		public Button exitButton;
			
		private Cell<Button> сontinueCell;
		
		public MainPage(Skin skin)
		{
			setFillParent(true);
			setTransform(false);
			fill();
			
			Image titleImage = new Image(skin, "lexemeSnake(title)");
			сontinueButton	= new TextButton("Continue", skin);
			newGameButton = new TextButton("New game", skin);
			recordsButton = new TextButton("Records", skin);
			helpButton = new TextButton("Help", skin);
			aboutButton	= new TextButton("About", skin);
			exitButton = new TextButton("Exit", skin);
			
			Table table = new Table();
			//table.debug();
			table.setFillParent(true);
			table.setTransform(false);
			table.add(titleImage).colspan(6);
			table.row();	
			сontinueCell = table.add();
			table.add(newGameButton).pad(10);
			table.add(recordsButton).pad(10);
			table.add(helpButton).pad(10);
			table.add(aboutButton).pad(10);
			table.add(exitButton).pad(10);
			
			setWidget(table);
		}
		
		public void showContinueButton()
		{
			сontinueCell.pad(10);
			сontinueCell.setWidget(сontinueButton);
		}
		
		public void hideContinueButton()
		{
			сontinueCell.pad(0);
			сontinueCell.setWidget(null);
		}
	}
	
	static public class SelectTopicPage extends Page
	{
		public Button backButton;
		private VerticalGroup group;
		private Skin skin;
		
		public SelectTopicPage(Skin skin)
		{
			this.skin = skin;
			setFillParent(true);
			setTransform(false);
			fill();
			
			//Элементы
			Image titlePage = new Image(skin, "selectTopic(title)");
			backButton = new ImageButton(skin, "backButton");
			
			group = new VerticalGroup();
			
			//Выравнивание
			Table table = new Table();
			//table.debug();
			table.setFillParent(true);
			table.setTransform(false);
			table.add(titlePage).center().padTop(60);
			table.row();
			table.add(group).expand();
			table.row().padBottom(30);
			table.add(backButton).bottom().left().padLeft(30);
			setWidget(table);
		}
		
		public void setContent(Course course)
		{
			setContent(course.getTopicsName());
		}
		
		public void setContent(String[] topics)
		{
			group.clear();
			for (String topic : topics)
			{
				TextButton button = new TextButton(topic, skin);
				button.setName(topic);
				
				button.addListener(new ClickListener()
				{
					public void clicked(InputEvent event, float x, float y) 
				    {
						String name = event.getListenerActor().getName();
						
						if (selectedListener != null)
							selectedListener.select(name);
				    }
				});
				
				group.addActor(button);
			}
		}
		
		public interface SelectedListener
		{
			public void select(String topic);
		}
		
		private SelectedListener selectedListener;
		public void setSelectedListener(SelectedListener listener)
		{
			selectedListener = listener;
		}
	}
	
	static public class RecordsPage extends Page
	{
		public Button backButton;
		private Table tableRecords;
		private Skin skin;
		
		public RecordsPage(Skin skin)
		{
			this.skin = skin;
			setFillParent(true);
			setTransform(false);
			fill();
			
			//Элементы
			Image titlePage = new Image(skin, "records(title)");
			backButton = new ImageButton(skin, "backButton");
			tableRecords = new Table();
			
			//Выравнивание
			Table table = new Table();
			//table.debug();
			table.setFillParent(true);
			table.setTransform(false);
			table.add(titlePage).center().padTop(60);
			table.row();
			table.add(tableRecords).expand().top().padTop(30);
			table.row().padBottom(30);
			table.add(backButton).bottom().left().padLeft(30);
			setWidget(table);
		}
		
		static public class RecordRow
		{
			public String topic;
			public String name;
			public String scores;
			
			public RecordRow(String topic, String name, String scores)
			{
				this.topic = topic;
				this.name = name;
				this.scores = scores;
			}
		}
		
		public void setContent(Course course)
		{
			Preferences pref = Gdx.app.getPreferences("Records");
			
			String[] topics = course.getTopicsName();
			RecordRow[] recordRows = new RecordRow[topics.length];
			for (int i = 0; i < recordRows.length; i++)
			{
				String[] params = pref.getString(topics[i]).split("\\|");
				if (params.length == 2)
				{
					recordRows[i] = new RecordRow(topics[i], params[0], params[1]);
				}
				else
				{
					recordRows[i] = new RecordRow(topics[i], "...", "...");
				}
			}
			setContent(recordRows);
		}
		
		public void setContent(RecordRow[] rows)
		{
			Label topic = new Label("Topic", skin, "title"); 
			Label name = new Label("Name", skin, "title"); 
			Label scores = new Label("Scores", skin, "title"); 
			
			tableRecords.clear();
			//table.debug();
			tableRecords.top();
			tableRecords.add(topic).left().padRight(80).padBottom(20);
			tableRecords.add(name).left().padRight(80).padBottom(20);
			tableRecords.add(scores).left().padBottom(20);
			
			for (RecordRow row : rows)
			{
				tableRecords.row();
				tableRecords.add(new Label(row.topic, skin, "title")).left().padRight(80);
				tableRecords.add(new Label(row.name, skin, "title")).left().padRight(80);
				tableRecords.add(new Label(row.scores, skin, "title")).left();
			}
		}
	}
	
	static public class AboutPage extends Page
	{
		public Button backButton;
		public Label infoLabel;
		
		public AboutPage(Skin skin)
		{
			setFillParent(true);
			setTransform(false);
			fill();
			
			//Элементы
			Image titlePage = new Image(skin, "about(title)");
			backButton = new ImageButton(skin, "backButton");
			infoLabel = new Label("no content", skin);
			infoLabel.setAlignment(Align.center);
			
			//Выравнивание
			Table table = new Table();
			//table.debug();
			table.setFillParent(true);
			table.setTransform(false);
			table.add(titlePage).center().padTop(60);
			table.row();
			table.add(infoLabel).expand();
			table.row().padBottom(30);
			table.add(backButton).bottom().left().padLeft(30);
			setWidget(table);
		}
		
		public void setContent(String text)
		{
			infoLabel.setText(text);
		}
	}
	
	static public class HelpPage extends Page
	{
		public Button backButton;
		public Label infoLabel;
		
		public HelpPage(Skin skin)
		{
			setFillParent(true);
			setTransform(false);
			fill();
			
			//Элементы
			Image titlePage = new Image(skin, "help(title)");
			backButton = new ImageButton(skin, "backButton");
			infoLabel = new Label("no content", skin);
			
			//Выравнивание
			Table table = new Table();
			//table.debug();
			table.setFillParent(true);
			table.setTransform(false);
			table.add(titlePage).center().padTop(60);
			table.row();
			table.add(infoLabel).expand();
			table.row().padBottom(30);
			table.add(backButton).bottom().left().padLeft(30);
			setWidget(table);
		}
		
		public void setContent(String text)
		{
			infoLabel.setText(text);
		}
	}
	
	public MainPage mainPage;
	public SelectTopicPage selectTopicPage;
	public RecordsPage recordsPage;
	public AboutPage aboutPage;
	public HelpPage helpPage;
	
	public MenuUI(Skin skin)
	{
		setFillParent(true);
		setTransform(false);
		fill();
		
		mainPage = new MainPage(skin);
		selectTopicPage = new SelectTopicPage(skin);
		recordsPage = new RecordsPage(skin);
		aboutPage = new AboutPage(skin);
		helpPage = new HelpPage(skin);
		
		initListeners();
		
		showPage(mainPage);
	}
	
	public void showPage(Page page)
	{
		setWidget(page);
		page.show();
	}
	
	private void initListeners()
	{
		selectTopicPage.backButton.setUserObject(mainPage);
		recordsPage.backButton.setUserObject(mainPage);
		aboutPage.backButton.setUserObject(mainPage);
		helpPage.backButton.setUserObject(mainPage);
		
		mainPage.newGameButton.setUserObject(selectTopicPage);
		mainPage.recordsButton.setUserObject(recordsPage);
		mainPage.helpButton.setUserObject(helpPage);
		mainPage.aboutButton.setUserObject(aboutPage);
		
		ClickListener changePageListener =  new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	Object object = event.getListenerActor().getUserObject();
		    	
		    	if (object instanceof Page)
		    	{
		    		Page page = (Page) object;
			    	showPage(page);
		    	}
		    		
		    }
		};
		
		selectTopicPage.backButton.addListener(changePageListener);
		recordsPage.backButton.addListener(changePageListener);
		aboutPage.backButton.addListener(changePageListener);
		helpPage.backButton.addListener(changePageListener);
		
		mainPage.newGameButton.addListener(changePageListener);
		mainPage.recordsButton.addListener(changePageListener);
		mainPage.helpButton.addListener(changePageListener);
		mainPage.aboutButton.addListener(changePageListener);
		
		mainPage.exitButton.addListener(new ClickListener() 
		{
		    public void clicked(InputEvent event, float x, float y) 
		    {
		    	Gdx.app.exit();
		    }
		});
	}
}