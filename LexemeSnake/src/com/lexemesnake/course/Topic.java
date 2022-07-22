package com.lexemesnake.course;

import org.apache.commons.lang3.StringEscapeUtils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Topic
{
	private Array<Element> xmlTasks;
	private int indexTask;
	
	private Task currentTask;
	
	private String name;
	
	public Topic(Element xmlTopic)
	{
		name = xmlTopic.getAttribute("Name");
		name = StringEscapeUtils.unescapeXml(name);
		
		xmlTasks = xmlTopic.getChildrenByName("Task");
		resetProgress();
	}

	public void resetProgress()
	{
		indexTask = 0;
		nextTask();
	}
	
	public Task nextTask()
	{
		currentTask = new Task(xmlTasks.get(indexTask++));
		return currentTask;
	}
	
	public boolean hasTask()
	{
		return indexTask < xmlTasks.size;
	}
	
	public Task getCurrentTask()
	{
		return currentTask;
	}
	
	public int getCurrentTaskIndex()
	{
		return indexTask;
	}
	
	public int getCountTask()
	{
		return xmlTasks.size;
	}
	
	public String getName()
	{
		return name;
	}
}
