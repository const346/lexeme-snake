package com.lexemesnake.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class BaseUI extends Page
{
	public LevelUI level;
	public MenuUI menu;
	
	public BaseUI(Skin skin)
	{
		setFillParent(true);
		setTransform(false);
		fill();
		
		level = new LevelUI(skin);
		menu = new MenuUI(skin);
		
		showPage(menu);
	}
	
	public void showPage(Page page)
	{
		setWidget(page);
		page.show();
	}
}
