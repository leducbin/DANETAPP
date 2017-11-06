package com.movideo.baracus.exceptions;

import org.apache.http.HttpStatus;

public class NotFoundException extends Exception
{

	private static final long serialVersionUID = 1L;
	private Integer responseCode = HttpStatus.SC_NOT_FOUND;

	public NotFoundException()
	{
		super();
	}

	public NotFoundException(String message)
	{
		super(message);
	}

	public NotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public NotFoundException(Throwable cause)
	{
		super(cause);
	}

	public Integer getResponseCode()
	{
		return responseCode;
	}

	public String getMessage()
	{
		return String.valueOf(responseCode);
	}
}
