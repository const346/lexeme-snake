package com.lexemesnake.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Container;

public class Page extends Container
{	
	public interface ShowListener
	{
		public void show(Page page);
	}
	
	private ShowListener listener;
	
	public Page() {}
	
	public void setShowListener(ShowListener listener)
	{
		this.listener = listener;
	}
	
	public void show()
	{
		if (listener != null)
			listener.show(this);
	}
}