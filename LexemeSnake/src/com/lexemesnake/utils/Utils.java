package com.lexemesnake.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * �������� ������ ����������� ������
 * @author ��������� ������� (medhaid)
 */
public class Utils
{
	/**
	 * @param angle ���� �� -������������� �� +�������������
	 * @return ���� �� 0 �� 360
	 */
	static public float convert360Angle(float angle)
	{
		float a = angle;
		a = a % 360;
		if (a < 0) a += 360;
		return a;
	}
	
	/**
	 * @param cA ������� ����
	 * @param nA ����������� ����
	 * @return �������� ������������ �������� ����
	 */
	static public float getDegreesFromAngle(float cA, float nA)
	{
		cA = convert360Angle(cA);
		nA = convert360Angle(nA);
		float a = convert360Angle(nA - cA);
		return (a > 180) ? a - 360 : a;
	}
	
	/**
	 * @param rect
	 * @return ������� (x, y)
	 */
	static public float[] getRectangleVertex(Rectangle rect)
	{
		float[] vertices = new float[8];
		
		//����� ������
		vertices[0] = rect.x;
		vertices[1] = rect.y;
		//����� �������
		vertices[2] = rect.x;
		vertices[3] = rect.y + rect.height;
		//������ �������
		vertices[4] = rect.x + rect.width;
		vertices[5] = rect.y + rect.height;
		//������ ������
		vertices[6] = rect.x + rect.width;
		vertices[7] = rect.y;
		
		return vertices;
	}
	
	/** 
	 * �������� ����������� �������� (����� std::random_shuffle) 
	 */
	static public <T> void randomShuffle(T[] m)
	{
		int last = m.length - 1;
		for (int i = 0; i < m.length; i++)
		{
			int j = MathUtils.random(i, last);
			
			//swap
			T s = m[j];
			m[j] = m[i];
			m[i] = s;
		}
	}
	
	/**
	 * ������� ��������� ������� �� m
	 * @param m ������
	 * @return ��������� �������
	 */
	static public <T> T randomSelect(T...m)
	{
		int i = MathUtils.random(m.length - 1);
		return m[i];
	}
}