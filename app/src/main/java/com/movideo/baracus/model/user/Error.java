package com.movideo.baracus.model.user;

import java.io.Serializable;

public class Error implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String message;
	private String status;
	private String code;

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Error[").append("message : ").append(message).append(",\n").append("status : ").append(status).append(",\n").append("code : ").append(code).append(",\n").append("]");
		return builder.toString();
	}
}
