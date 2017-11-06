package com.movideo.baracus.model.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String id;
	private String object;
	private String identifier;
	private String provider;
	private String password;
	private String phone;
	@SerializedName("password_confirmation")
	private String confirmPassword;
	@SerializedName("given_name")
	private String givenName;
	@SerializedName("family_name")
	private String familyName;
	private String gender;
	private String email;
	private String location;
	@SerializedName("date_of_birth")
	private String dateOfBirth;
	private String avatar;
	private String currency;
	@SerializedName("created_at")
	private Date created;
	// @SerializedName("access_token")
	private String accessToken;
	private List<Device> devices = new ArrayList<Device>();
	private Double credits;
	private Subscription subscription;
	private Error error;

	public Error getError()
	{
		return error;
	}

	public void setError(Error error)
	{
		this.error = error;
	}

	public List<Device> getDevices()
	{
		return devices;
	}

	public void setDevices(List<Device> devices)
	{
		this.devices = devices;
	}

	public Double getCredits()
	{
		return credits;
	}

	public void setCredits(Double credits)
	{
		this.credits = credits;
	}

	public Subscription getSubscription()
	{
		return subscription;
	}

	public void setSubscription(Subscription subscription)
	{
		this.subscription = subscription;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getConfirmPassword()
	{
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword = confirmPassword;
	}

	public String getProvider()
	{
		return provider;
	}

	public void setProvider(String provider)
	{
		this.provider = provider;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

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

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public String getGivenName()
	{
		return givenName;
	}

	public void setGivenName(String givenName)
	{
		this.givenName = givenName;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public void setFamilyName(String familyName)
	{
		this.familyName = familyName;
	}

	public String getGender()
	{
		return gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getLocation()
	{
		return location;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}

	public String getDateOfBirth()
	{
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	public String getAvatar()
	{
		return avatar;
	}

	public void setAvatar(String avatar)
	{
		this.avatar = avatar;
	}

	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public String getAccessToken()
	{
		return accessToken;
	}

	public void setAccessToken(String accessToken)
	{
		this.accessToken = accessToken;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("User[").append("id : ").append(id).append(",\n").append("object : ").append(object).append(",\n").append("identifier : ").append(identifier).append(",\n").append("phone : ").append(phone).append(",\n").append("provider : ").append(provider).append(",\n").append("password : ").append(password).append(",\n").append("confirmPassword : ").append(confirmPassword).append(",\n").append("givenName : ").append(givenName)
				.append(",\n").append("familyName : ").append(familyName).append(",\n").append("gender : ").append(gender).append(",\n").append("email : ").append(email).append(",\n").append("location : ").append(location).append(",\n").append("dateOfBirth : ").append(dateOfBirth).append(",\n").append("avatar : ").append(avatar).append(",\n").append("currency : ").append(currency).append(",\n")
				.append("created : ").append(created).append(",\n").append("accessToken : ").append(accessToken).append(",\n").append("devices : ").append(devices).append(",\n").append("credits : ").append(credits).append(",\n").append("subscription : ").append(subscription).append(",\n").append("error : ").append(error).append(",\n").append("]");
		return builder.toString();
	}

}
