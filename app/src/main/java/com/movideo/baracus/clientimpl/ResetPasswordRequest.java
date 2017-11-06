package com.movideo.baracus.clientimpl;

public class ResetPasswordRequest
{
	private String identifier;

	public ResetPasswordRequest(String identifier)
	{
		this.identifier = identifier;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

}
