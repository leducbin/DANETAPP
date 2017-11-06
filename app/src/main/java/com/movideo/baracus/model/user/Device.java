package com.movideo.baracus.model.user;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class Device implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String id;
	private String object;
	private String name;
	private String model;
	@SerializedName("system_name")
	private String systemName;
	@SerializedName("system_version")
	private String systemVersion;
	private String nickname;
	@SerializedName("created_at")
	private Date created;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getObject()
	{
		return object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel(String model)
	{
		this.model = model;
	}

	public String getSystemName()
	{
		return systemName;
	}

	public void setSystemName(String systemName)
	{
		this.systemName = systemName;
	}

	public String getSystemVersion()
	{
		return systemVersion;
	}

	public void setSystemVersion(String systemVersion)
	{
		this.systemVersion = systemVersion;
	}

	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Data[").append("id : ").append(id).append(",\n").append("object : ").append(object).append(",\n").append("name : ").append(name).append(",\n").append("model : ").append(model).append(",\n").append("systemName : ").append(systemName).append(",\n").append("systemVersion : ").append(systemVersion).append(",\n").append("nickname : ").append(nickname).append(",\n")
				.append("created : ").append(created).append(",\n").append("]");
		return builder.toString();
	}

}
