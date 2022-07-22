package com.lexemesnake.course;

import org.apache.commons.lang3.StringEscapeUtils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Task
{
	public enum TaskState {Processing, Success, Fail} 
	
	private Element taskElement;
	
	private Element tokenElement;
	private String designCode;
	private int scores;
	
	private TaskState state;
	
	public Task(Element taskElement)
	{
		this.taskElement = taskElement;
		resetProgress();
	}
	
	public String getDescription()
	{
		String description = taskElement.getAttribute("Description");
		return StringEscapeUtils.unescapeXml(description);
	}
		
	public void resetProgress()
	{
		tokenElement = taskElement;
		scores = 0;
		designCode = "";
		state = TaskState.Processing;
	}
	
	public String[] getNextVariants()
	{
		Array<String> tokenNames = new Array<String>();
		
		Array<Element> elems = tokenElement.getChildrenByName("Token"); 
		for(Element element : elems)
		{
			String text = element.getAttribute("Text");
			text = StringEscapeUtils.unescapeXml(text);
			
			tokenNames.add(text);
		}
		
		return tokenNames.toArray(String.class);
	}

	public TaskState testNext(String tokenName)
	{
		Array<Element> elems = tokenElement.getChildrenByName("Token"); 
		for(Element element : elems)
		{
			String text = element.getAttribute("Text");
			text = StringEscapeUtils.unescapeXml(text);
			
			if (text.equals(tokenName))
			{
				//-------------------------------------
				scores += element.getInt("Price");
				
				//ќформление 
				if (element.getBoolean("NewLine"))
					designCode += "\n";
				
				designCode += text;
				
				int tabs = element.getInt("Tabs");
				for (int i = 0; i < tabs; i++)
					designCode += "\t";
				
				int spaces = element.getInt("Spaces");
				for (int i = 0; i < spaces; i++)
					designCode += " ";
				//-------------------------------------
				
				tokenElement = element;
				
				state = element.getChildrenByName("Token").size == 0 ?
						TaskState.Success : TaskState.Processing; 
				
				return state;
			}
		}
		
		state = TaskState.Fail;
		return state;
	}
	
	public TaskState getState()
	{
		return state;
	}
	
	public int getScores()
	{
		return scores;
	}
	
	public String getDesignCode()
	{
		return designCode;
	}
}
