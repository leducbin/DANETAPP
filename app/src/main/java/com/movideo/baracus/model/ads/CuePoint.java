package com.movideo.baracus.model.ads;

/**
 * Created by rranawaka on 3/12/2015.
 */
public class CuePoint
{
	public enum CuePointType
	{
		seconds,
		percent
	}

	private int index;
	private CuePointType type;
	private int value;

	public CuePointType getType()
	{
		return type;
	}

	public void setType(CuePointType type)
	{
		this.type = type;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
