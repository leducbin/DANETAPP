package com.movideo.baracus.exceptions;

import org.apache.http.HttpStatus;

public class UnauthorisedException extends Exception
{

	private static final long serialVersionUID = 1L;

	private Integer responseCode = HttpStatus.SC_UNAUTHORIZED;

	public UnauthorisedException()
	{
		super();
	}

	public UnauthorisedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UnauthorisedException(String message)
	{
		super(message);
	}

	public UnauthorisedException(Throwable cause)
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
