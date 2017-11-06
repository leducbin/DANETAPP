package com.movideo.baracus.clientimpl;

public class UserAuthenticationRequest
{
	private String provider;
	private String identifier;
	private String password;
	private String access_token;

	public UserAuthenticationRequest(String provider, String identifier, String password, String access_token) {
		this.provider = provider;
		this.identifier = identifier;
		this.password = password;
		this.access_token = access_token;
	}

	public UserAuthenticationRequest(String provider, String identifier, String password)
	{
		this.provider = provider;
		this.identifier = identifier;
		this.password = password;
	}

	public String getProvider()
	{
		return provider;
	}

	public void setProvider(String provider)
	{
		this.provider = provider;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

}
