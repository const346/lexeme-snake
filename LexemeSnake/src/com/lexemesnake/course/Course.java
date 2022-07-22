package com.lexemesnake.course;

import org.apache.commons.lang3.StringEscapeUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader.Element;

/**
 * ��������� ����� ������ course.xml � �����
 * @author ��������� ������� (medhaid)
 */
public class Course
{
	private Array<Element> xmlTopics;
	private Array<String> tokens;
	private Topic topic;
	
	public Course(Element xmlRoot)
	{
		Element xmlCourse = xmlRoot.getChildByName("Course");
		xmlTopics =  xmlCourse.getChildrenByName("Topic");
		
		readTokens(xmlRoot);
	}
	
	public void setTopic(String nameTopic)
	{ 
		for(Element xmlTopic : xmlTopics)
		{
			String name = xmlTopic.get("Name");
			if (nameTopic.equals(name))
			{
				topic = new Topic(xmlTopic);
				return;
			}
		}
		
		throw new IllegalArgumentException("Topic � ����� ������ �� ����������");
	}
	
	public Topic getTopic()
	{
		return topic;
	}
	
	public String[] getTopicsName()
	{
		Array<String> names = new Array<String>();
		
		for(Element xmlTopic : xmlTopics)
		{
			String name = xmlTopic.get("Name");
			names.add(name);
		}
		
		return names.toArray(String.class);
	}
	
	private void readTokens(Element xmlRoot)
	{
		Element xmlTokens = xmlRoot.getChildByName("Tokens");
		tokens = new Array<String>();
		for (Element element : xmlTokens.getChildrenByName("Token"))
		{
			String text = element.getAttribute("Text");
			text = StringEscapeUtils.unescapeXml(text);
			tokens.add(text);
		}
	}
	
	/**
	 * @return ��� ������� � ���� Tokens 
	 */
	public String[] getTokens()
	{
		return tokens.toArray(String.class);
	}
	
	/**
	 * @param count - ���������� ��������� ������
	 * @param exceptTokens - ������� ������� ����� ��������� �� ����������
	 * @return ����������� ���-�� ��������� ������
	 */
	public String[] getRandomTokens(int count, String[] exceptTokens)
	{
		Array<String> sourse = new Array<String>(tokens);
		
		for (String name : exceptTokens)
			sourse.removeValue(name, false);

		sourse.shuffle();
		sourse.truncate(count);
		
		return sourse.toArray(String.class);
	}
}
